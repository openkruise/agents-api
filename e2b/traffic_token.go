package e2b

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"math"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"time"
)

// JWTAuthMetadataKey is the sandbox metadata entry that marks a sandbox as
// protected by sandbox-gateway JWT auth. When its value is exactly "true",
// data-plane requests must carry a valid Traffic JWT in
// runtime.TrafficTokenHeader.
const JWTAuthMetadataKey = "security.agents.kruise.io/enable-jwt-auth"

// Traffic token refresh tuning, mirroring the Python kruise_agents patch.
const (
	// trafficTokenMinRefreshAhead is the minimum lead time before expiry at
	// which a refresh is scheduled.
	trafficTokenMinRefreshAhead = 60 * time.Second
	// trafficTokenMaxRefreshAhead caps the refresh lead time.
	trafficTokenMaxRefreshAhead = 300 * time.Second
	// trafficTokenRefreshAheadFraction of the token's validity decides the
	// refresh lead time (bounded by the constants above).
	trafficTokenRefreshAheadFraction = 0.2
	// trafficTokenJitterFraction of the refresh lead time is randomized to
	// spread refreshes across sandboxes.
	trafficTokenJitterFraction = 0.1
	// trafficTokenInitialBackoff is the retry delay after a first failed
	// refresh; it doubles on each further failure.
	trafficTokenInitialBackoff = time.Second
	// trafficTokenMaxBackoff caps the exponential backoff.
	trafficTokenMaxBackoff = 30 * time.Second
	// trafficTokenExpirationTolerance is the maximum allowed difference
	// between the reported expiration and the JWT "exp" claim.
	trafficTokenExpirationTolerance = 5 * time.Second
)

// TrafficAccessTokenError is the base error for local traffic access token
// handling: JWT parse failures, malformed refresh responses and expiry.
type TrafficAccessTokenError struct {
	msg string
}

func (e *TrafficAccessTokenError) Error() string { return e.msg }

// TrafficAccessTokenExpired is returned before a data-plane request would
// use an expired token (fail-closed: the SDK refuses to send a request whose
// token is expired until a refresh succeeds).
type TrafficAccessTokenExpired struct {
	*TrafficAccessTokenError
	// Err is the refresh failure that left the token expired, if any.
	Err error
}

func (e *TrafficAccessTokenExpired) Unwrap() error { return e.Err }

func newTrafficAccessTokenExpired(cause error) *TrafficAccessTokenExpired {
	return &TrafficAccessTokenExpired{
		TrafficAccessTokenError: &TrafficAccessTokenError{
			msg: "traffic access token expired before it could be refreshed",
		},
		Err: cause,
	}
}

// TrafficAccessTokenRefreshError reports a failed refresh attempt against
// the management API. The HTTP response body is intentionally neither
// captured nor rendered, to avoid leaking server error details into client
// logs; the status code and requested retry delay are preserved.
type TrafficAccessTokenRefreshError struct {
	*TrafficAccessTokenError
	// StatusCode is the refresh endpoint's HTTP status (0 for
	// transport-level failures).
	StatusCode int
	// RetryAfter is the delay requested via the Retry-After header, if any.
	RetryAfter time.Duration
	// Err is the underlying transport error, if any.
	Err error
}

func (e *TrafficAccessTokenRefreshError) Unwrap() error { return e.Err }

// TrafficAccessToken is a sandbox traffic access token together with its
// expiration.
type TrafficAccessToken struct {
	// Token is the token string (a JWT issued by sandbox-manager).
	Token string
	// ExpiresAt is the token's expiration (with timezone, always UTC).
	ExpiresAt time.Time
}

// trafficJWTClaims carries the expiration-related claims of a Traffic JWT.
type trafficJWTClaims struct {
	// expiresAt is the "exp" claim as unix seconds.
	expiresAt float64
	// issuedAt is the "iat" claim as unix seconds; valid only when hasIat.
	issuedAt float64
	hasIat   bool
}

// parseTrafficJWTExpiration extracts the "exp" (required) and "iat"
// (optional) claims from a JWT without verifying its signature: the token is
// only used client-side for refresh scheduling, authenticity is enforced by
// the sandbox-gateway.
func parseTrafficJWTExpiration(token string) (trafficJWTClaims, error) {
	parts := strings.Split(token, ".")
	if len(parts) < 2 {
		return trafficJWTClaims{}, trafficTokenParseError()
	}
	// Accept padded or unpadded base64url payloads.
	payload, err := base64.RawURLEncoding.DecodeString(strings.TrimRight(parts[1], "="))
	if err != nil {
		return trafficJWTClaims{}, trafficTokenParseError()
	}
	var claims map[string]any
	if err := json.Unmarshal(payload, &claims); err != nil {
		return trafficJWTClaims{}, trafficTokenParseError()
	}
	exp, ok := numericClaim(claims, "exp")
	if !ok {
		return trafficJWTClaims{}, trafficTokenParseError()
	}
	parsed := trafficJWTClaims{expiresAt: exp}
	if iat, ok := numericClaim(claims, "iat"); ok {
		parsed.issuedAt = iat
		parsed.hasIat = true
	}
	return parsed, nil
}

// numericClaim extracts a numeric claim from decoded JWT claims. Only JSON
// numbers are accepted (real JWTs encode numeric dates as numbers).
func numericClaim(claims map[string]any, name string) (float64, bool) {
	value, present := claims[name]
	if !present {
		return 0, false
	}
	number, ok := value.(float64)
	return number, ok
}

func trafficTokenParseError() error {
	return &TrafficAccessTokenError{
		msg: "traffic access token is not a JWT with a valid exp claim",
	}
}

// parseTrafficTokenExpiration parses an RFC3339 timestamp with a mandatory
// timezone, the Go counterpart of the Python patch's parse_expiration.
func parseTrafficTokenExpiration(value string) (time.Time, error) {
	parsed, err := time.Parse(time.RFC3339, strings.TrimSpace(value))
	if err != nil {
		return time.Time{}, &TrafficAccessTokenError{
			msg: "invalid traffic access token expiration",
		}
	}
	// time.RFC3339 requires a zone offset, so a timezone is always present.
	return parsed.UTC(), nil
}

// parseRetryAfter parses the Retry-After header as delay seconds; the only
// form the manager understands (HTTP-date values are ignored, matching the
// Python implementation).
func parseRetryAfter(value string) time.Duration {
	seconds, err := strconv.ParseFloat(strings.TrimSpace(value), 64)
	if err != nil || seconds < 0 || math.IsNaN(seconds) || math.IsInf(seconds, 0) {
		return 0
	}
	return time.Duration(seconds * float64(time.Second))
}

// trafficTokenManager keeps a sandbox's Traffic JWT valid for data-plane
// requests: it refreshes the token before expiry (coalescing concurrent
// refreshes into one, with exponential backoff on failure) and fails closed
// once the token has expired. It is the Go counterpart of the Python
// kruise_agents TrafficTokenManager.
type trafficTokenManager struct {
	// refresh obtains a new token from the management API.
	refresh func(ctx context.Context) (TrafficAccessToken, error)
	// now returns the current unix time in seconds (fractional seconds
	// preserved so tests can advance time precisely).
	now func() float64
	// randomValue returns a pseudo-random float in [0, 1) for jitter.
	randomValue func() float64

	mu sync.RWMutex
	// generation counts successful refreshes; attempts counts refresh
	// attempts (successful or not). Both are atomic so callers can snapshot
	// them before acquiring the write lock and detect that another
	// goroutine already refreshed (forced-refresh de-duplication).
	generation atomic.Uint64
	attempts   atomic.Uint64
	// busy is set while a refresh is in flight (under mu); read atomically
	// before acquiring the write lock for the same de-duplication.
	busy atomic.Bool

	// state below is guarded by mu.
	token     string
	expiresAt float64 // unix seconds; 0 while no token is known
	refreshAt float64 // unix seconds; refresh once now >= refreshAt
	nextRetry float64 // unix seconds; no retry before this time
	backoff   time.Duration
}

// newTrafficTokenManager builds a manager seeded with an initial token.
//
// A non-empty token must parse as a JWT; a token that does not (e.g. a
// legacy opaque UUID) returns an error and the caller keeps the legacy
// behavior (no traffic header, no refresh). An empty token creates a lazily
// bootstrapping manager whose first use triggers the initial refresh.
func newTrafficTokenManager(
	token string,
	refresh func(ctx context.Context) (TrafficAccessToken, error),
	now func() float64,
	randomValue func() float64,
) (*trafficTokenManager, error) {
	m := &trafficTokenManager{
		refresh:     refresh,
		now:         now,
		randomValue: randomValue,
		backoff:     trafficTokenInitialBackoff,
	}
	if token == "" {
		return m, nil
	}
	claims, err := parseTrafficJWTExpiration(token)
	if err != nil {
		return nil, err
	}
	issuedAt := now()
	if claims.hasIat {
		issuedAt = claims.issuedAt
	}
	m.token = token
	m.expiresAt = claims.expiresAt
	m.refreshAt = m.calculateRefreshAt(claims.expiresAt, issuedAt)
	return m, nil
}

// EnsureValidToken returns a token that is safe to send on a data-plane
// request, refreshing it first when needed. With force it bypasses the
// refresh window and backoff, but concurrent forced refreshes are coalesced
// so only one refresh runs per burst.
//
// If a refresh fails while the current token is still valid, the current
// token is returned (the failure is retried after the backoff). Once the
// token has expired, TrafficAccessTokenExpired is returned instead (the SDK
// fails closed rather than sending a request the gateway will reject).
func (m *trafficTokenManager) EnsureValidToken(ctx context.Context, force bool) (string, error) {
	// Snapshot the de-duplication state before touching any lock, mirroring
	// the Python implementation's pre-lock reads: a caller that observes a
	// refresh in progress (or a completed one, or a newer attempt) must not
	// trigger a second refresh. Taking the snapshot here — before the shared
	// read lock — matters: while a refresh is in flight the write lock is
	// held, so callers park on the read lock and would otherwise only
	// observe post-completion state.
	generation := m.generation.Load()
	attempts := m.attempts.Load()
	busy := m.busy.Load()

	// Fast path under a shared read lock: no refresh is due.
	m.mu.RLock()
	if !m.needsRefreshLocked(force) {
		token := m.token
		m.mu.RUnlock()
		return token, nil
	}
	if m.inBackoffLocked(force) {
		token, err := m.useCurrentOrRaiseLocked()
		m.mu.RUnlock()
		return token, err
	}
	m.mu.RUnlock()

	m.mu.Lock()
	defer m.mu.Unlock()

	if force {
		if m.generation.Load() != generation {
			// Another goroutine completed a successful refresh since the
			// snapshot: the forced refresh is already satisfied.
			return m.token, nil
		}
		if busy || m.attempts.Load() != attempts {
			// Another goroutine already attempted (or is attempting) a
			// refresh since the snapshot: fall back to the current token.
			return m.useCurrentOrRaiseLocked()
		}
	}
	if !m.needsRefreshLocked(force) {
		return m.token, nil
	}
	if m.inBackoffLocked(force) {
		return m.useCurrentOrRaiseLocked()
	}
	m.attempts.Add(1)
	m.busy.Store(true)
	defer m.busy.Store(false)

	result, err := m.refreshDetached(ctx)
	if err == nil {
		err = m.recordSuccessLocked(result)
	}
	if err != nil {
		m.recordFailureLocked(err)
		if m.now() >= m.expiresAt {
			return "", newTrafficAccessTokenExpired(err)
		}
		return m.token, nil
	}
	return m.token, nil
}

// refreshDetached runs the refresh with cancellation detached from the
// caller's context: the refreshed token is shared state, so a caller that
// gives up mid-request must not abort an in-flight refresh for everyone
// else. The refresh still respects its own HTTP timeout.
func (m *trafficTokenManager) refreshDetached(ctx context.Context) (TrafficAccessToken, error) {
	return m.refresh(context.WithoutCancel(ctx))
}

// tokenProvider adapts the manager to runtime.TrafficTokenProvider: every
// data-plane request obtains a valid token (refreshing when needed) right
// before it is sent.
func (m *trafficTokenManager) tokenProvider(ctx context.Context) (string, error) {
	return m.EnsureValidToken(ctx, false)
}

// currentToken returns the currently cached token without refreshing.
func (m *trafficTokenManager) currentToken() string {
	m.mu.RLock()
	defer m.mu.RUnlock()
	return m.token
}

// calculateRefreshAt computes the time at which a token issued at issuedAt
// and expiring at expiresAt should be refreshed: a fraction of its validity
// ahead of expiry (bounded to [60s, 300s]) minus a small random jitter.
func (m *trafficTokenManager) calculateRefreshAt(expiresAt, issuedAt float64) float64 {
	validity := expiresAt - issuedAt
	if validity < 0 {
		validity = 0
	}
	refreshAhead := validity * trafficTokenRefreshAheadFraction
	if refreshAhead < trafficTokenMinRefreshAhead.Seconds() {
		refreshAhead = trafficTokenMinRefreshAhead.Seconds()
	}
	if refreshAhead > trafficTokenMaxRefreshAhead.Seconds() {
		refreshAhead = trafficTokenMaxRefreshAhead.Seconds()
	}
	jitter := refreshAhead * trafficTokenJitterFraction * m.randomValue()
	return expiresAt - refreshAhead - jitter
}

func (m *trafficTokenManager) needsRefreshLocked(force bool) bool {
	return force || m.token == "" || m.now() >= m.refreshAt
}

func (m *trafficTokenManager) inBackoffLocked(force bool) bool {
	return !force && m.now() < m.nextRetry
}

// useCurrentOrRaiseLocked returns the current token, failing closed with
// TrafficAccessTokenExpired when there is none or it has expired.
func (m *trafficTokenManager) useCurrentOrRaiseLocked() (string, error) {
	if m.token == "" || m.now() >= m.expiresAt {
		return "", newTrafficAccessTokenExpired(nil)
	}
	return m.token, nil
}

// recordFailureLocked schedules the next retry, honoring the server's
// Retry-After delay when it exceeds the local backoff, and doubles the
// backoff up to the cap.
func (m *trafficTokenManager) recordFailureLocked(err error) {
	delay := m.backoff
	var refreshErr *TrafficAccessTokenRefreshError
	if errors.As(err, &refreshErr) && refreshErr.RetryAfter > delay {
		delay = refreshErr.RetryAfter
	}
	m.nextRetry = m.now() + delay.Seconds()
	m.backoff = min(m.backoff*2, trafficTokenMaxBackoff)
}

// recordSuccessLocked validates a refresh response and installs it as the
// current token. Validation failures (empty token, JWT that does not parse,
// expiration mismatching the "exp" claim, already-expired token) are
// returned as errors and handled by the caller like refresh failures.
func (m *trafficTokenManager) recordSuccessLocked(result TrafficAccessToken) error {
	if result.Token == "" {
		return &TrafficAccessTokenError{
			msg: "traffic access token refresh returned an empty token",
		}
	}
	expiresAt := float64(result.ExpiresAt.UnixNano()) / 1e9
	claims, err := parseTrafficJWTExpiration(result.Token)
	if err != nil {
		return err
	}
	if math.Abs(claims.expiresAt-expiresAt) > trafficTokenExpirationTolerance.Seconds() {
		return &TrafficAccessTokenError{
			msg: "traffic access token expiration does not match its exp claim",
		}
	}
	return m.setTokenLocked(result.Token, expiresAt, m.now())
}

// setTokenLocked installs a token and resets the retry state. A token that
// is already expired at install time is rejected.
func (m *trafficTokenManager) setTokenLocked(token string, expiresAt, issuedAt float64) error {
	if expiresAt <= m.now() {
		return &TrafficAccessTokenError{
			msg: "traffic access token refresh returned an expired token",
		}
	}
	m.token = token
	m.expiresAt = expiresAt
	m.refreshAt = m.calculateRefreshAt(expiresAt, issuedAt)
	m.nextRetry = 0
	m.backoff = trafficTokenInitialBackoff
	m.generation.Add(1)
	return nil
}

// requiresTrafficToken reports whether sandbox metadata marks it as
// protected by sandbox-gateway JWT auth (exact "true" value, matching the
// Python implementation).
func requiresTrafficToken(metadata map[string]string) bool {
	return metadata != nil && metadata[JWTAuthMetadataKey] == "true"
}

// trafficNow returns the current unix time in fractional seconds.
func trafficNow() float64 {
	return float64(time.Now().UnixNano()) / 1e9
}
