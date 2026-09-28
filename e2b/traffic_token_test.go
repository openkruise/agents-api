package e2b

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"math"
	"net/http"
	"net/http/httptest"
	"strings"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"github.com/openkruise/agents-api/runtime"
)

// fakeClock drives deterministic time in traffic token manager tests.
type fakeClock struct {
	now float64
}

func (c *fakeClock) Now() float64 { return c.now }

func (c *fakeClock) Advance(seconds float64) { c.now += seconds }

// staticRandom returns a fixed jitter value (0 keeps refresh times
// deterministic).
func staticRandom() float64 { return 0 }

// signTestJWT builds a unsigned JWT carrying the given claims, mirroring
// the tokens issued by sandbox-manager for traffic auth (signature is not
// verified client-side).
func signTestJWT(claims map[string]any) string {
	payload, _ := json.Marshal(claims)
	body := base64.RawURLEncoding.EncodeToString(payload)
	return fmt.Sprintf("header.%s.signature", body)
}

// tokenWithValidity returns a JWT expiring at now+validity seconds.
func tokenWithValidity(now float64, validity float64) string {
	return signTestJWT(map[string]any{
		"exp": now + validity,
		"iat": now,
	})
}

// mockRefresh counts calls and hands out successive tokens.
type mockRefresh struct {
	calls  atomic.Int64
	tokens chan string // each element is the token for one call; blocks when empty
	err    error
}

func (r *mockRefresh) Refresh(ctx context.Context) (TrafficAccessToken, error) {
	r.calls.Add(1)
	if r.err != nil {
		return TrafficAccessToken{}, r.err
	}
	select {
	case token := <-r.tokens:
		return TrafficAccessToken{
			Token:     token,
			ExpiresAt: time.Unix(int64(math.Round(jwtExp(token))), 0).UTC(),
		}, nil
	case <-ctx.Done():
		return TrafficAccessToken{}, ctx.Err()
	}
}

// jwtExp extracts the exp claim of a test token.
func jwtExp(token string) float64 {
	claims, err := parseTrafficJWTExpiration(token)
	if err != nil {
		panic(err)
	}
	return claims.expiresAt
}

// pushTokens queues tokens for the mock to hand out.
func pushTokens(tokens ...string) chan string {
	ch := make(chan string, len(tokens))
	for _, t := range tokens {
		ch <- t
	}
	return ch
}

func TestParseTrafficJWTExpiration(t *testing.T) {
	now := 1_000_000.0
	claims, err := parseTrafficJWTExpiration(tokenWithValidity(now, 60))
	if err != nil {
		t.Fatalf("parse: %v", err)
	}
	if claims.expiresAt != now+60 {
		t.Errorf("exp = %v, want %v", claims.expiresAt, now+60)
	}
	if !claims.hasIat || claims.issuedAt != now {
		t.Errorf("iat = %v (hasIat %v), want %v", claims.issuedAt, claims.hasIat, now)
	}

	// Missing exp / invalid JSON / non-numeric exp are all parse errors.
	for _, bad := range []string{
		"not-a-jwt",
		"onlytwo",
		signTestJWT(map[string]any{"iat": now}),
		signTestJWT(map[string]any{"exp": "later"}),
	} {
		if _, err := parseTrafficJWTExpiration(bad); err == nil {
			t.Errorf("parse(%q) unexpectedly succeeded", bad)
		}
	}

	// iat is optional.
	claims, err = parseTrafficJWTExpiration(signTestJWT(map[string]any{"exp": now + 60}))
	if err != nil || claims.hasIat {
		t.Errorf("optional iat: err=%v hasIat=%v", err, claims.hasIat)
	}
}

func TestParseTrafficTokenExpiration(t *testing.T) {
	parsed, err := parseTrafficTokenExpiration("2026-01-02T03:04:05Z")
	if err != nil {
		t.Fatalf("parse: %v", err)
	}
	if want := time.Date(2026, 1, 2, 3, 4, 5, 0, time.UTC); !parsed.Equal(want) {
		t.Errorf("parsed = %v, want %v", parsed, want)
	}
	for _, bad := range []string{
		"",
		"not-a-time",
		"2026-01-02T03:04:05", // missing timezone
	} {
		if _, err := parseTrafficTokenExpiration(bad); err == nil {
			t.Errorf("parse(%q) unexpectedly succeeded", bad)
		}
	}
}

func TestParseRetryAfter(t *testing.T) {
	cases := []struct {
		in   string
		want time.Duration
	}{
		{"2", 2 * time.Second},
		{"1.5", 1500 * time.Millisecond},
		{" 3 ", 3 * time.Second},
		{"", 0},
		{"-1", 0},
		{"abc", 0},          // HTTP-date form is not understood
		{"NaN", 0},          // invalid float
		{"1e999", 0},        // +Inf
		{"not-a-number", 0}, // invalid
	}
	for _, tc := range cases {
		if got := parseRetryAfter(tc.in); got != tc.want {
			t.Errorf("parseRetryAfter(%q) = %v, want %v", tc.in, got, tc.want)
		}
	}
}

func TestRequiresTrafficToken(t *testing.T) {
	cases := []struct {
		metadata map[string]string
		want     bool
	}{
		{map[string]string{JWTAuthMetadataKey: "true"}, true},
		{map[string]string{JWTAuthMetadataKey: "True"}, false}, // exact match only
		{map[string]string{JWTAuthMetadataKey: "false"}, false},
		{map[string]string{"other": "true"}, false},
		{nil, false},
	}
	for _, tc := range cases {
		if got := requiresTrafficToken(tc.metadata); got != tc.want {
			t.Errorf("requiresTrafficToken(%v) = %v, want %v", tc.metadata, got, tc.want)
		}
	}
}

func TestNewTrafficTokenManagerRejectsNonJWT(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	refresh := &mockRefresh{tokens: pushTokens()}

	if _, err := newTrafficTokenManager("opaque-uuid-style-token", refresh.Refresh, clock.Now, staticRandom); err == nil {
		t.Error("non-JWT seed token should be rejected")
	}

	// Empty token is fine (lazy bootstrap).
	if _, err := newTrafficTokenManager("", refresh.Refresh, clock.Now, staticRandom); err != nil {
		t.Errorf("empty seed token: %v", err)
	}
}

func TestTrafficTokenManagerRefreshesBeforeExpiry(t *testing.T) {
	// 3600s validity -> refresh ahead = min(300, max(60, 720)) = 300s.
	clock := &fakeClock{now: 1_000_000}
	first := tokenWithValidity(clock.now, 3600)
	second := tokenWithValidity(clock.now+3000, 3600)
	refresh := &mockRefresh{tokens: pushTokens(second)}

	m, err := newTrafficTokenManager(first, refresh.Refresh, clock.Now, staticRandom)
	if err != nil {
		t.Fatalf("manager: %v", err)
	}

	// Half the validity is well before the refresh point: no refresh yet.
	if got, _ := m.EnsureValidToken(context.Background(), false); got != first {
		t.Fatalf("token unexpectedly replaced before refresh window")
	}
	if refresh.calls.Load() != 0 {
		t.Fatalf("refresh calls = %d, want 0", refresh.calls.Load())
	}

	// Crossing refresh_at (exp - 300s) triggers the refresh.
	clock.Advance(3301)
	got, err := m.EnsureValidToken(context.Background(), false)
	if err != nil || got != second {
		t.Fatalf("EnsureValidToken = (%q, %v), want second token", got, err)
	}
	if refresh.calls.Load() != 1 {
		t.Fatalf("refresh calls = %d, want 1", refresh.calls.Load())
	}
}

func TestTrafficTokenManagerEmptyTokenBootstraps(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	token := tokenWithValidity(clock.now, 600)
	refresh := &mockRefresh{tokens: pushTokens(token)}

	m, err := newTrafficTokenManager("", refresh.Refresh, clock.Now, staticRandom)
	if err != nil {
		t.Fatalf("manager: %v", err)
	}
	if got, _ := m.EnsureValidToken(context.Background(), false); got != token {
		t.Fatalf("bootstrap token = %q, want %q", got, token)
	}
}

func TestTrafficTokenManagerForcedRefresh(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	first := tokenWithValidity(clock.now, 3600)
	second := tokenWithValidity(clock.now, 3600)
	refresh := &mockRefresh{tokens: pushTokens(second)}

	m, _ := newTrafficTokenManager(first, refresh.Refresh, clock.Now, staticRandom)

	got, err := m.EnsureValidToken(context.Background(), true)
	if err != nil || got != second {
		t.Fatalf("forced refresh = (%q, %v), want second", got, err)
	}
	if refresh.calls.Load() != 1 {
		t.Fatalf("refresh calls = %d, want 1", refresh.calls.Load())
	}
}

func TestTrafficTokenManagerFailureKeepsCurrentToken(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	current := tokenWithValidity(clock.now, 3600)
	refresh := &mockRefresh{
		err: &TrafficAccessTokenRefreshError{
			TrafficAccessTokenError: &TrafficAccessTokenError{msg: "boom"},
		},
	}

	m, _ := newTrafficTokenManager(current, refresh.Refresh, clock.Now, staticRandom)

	// Not yet due: current token, no refresh.
	if got, _ := m.EnsureValidToken(context.Background(), false); got != current {
		t.Fatal("unexpected refresh before refresh window")
	}

	clock.Advance(3301) // past refresh_at
	got, err := m.EnsureValidToken(context.Background(), false)
	if err != nil || got != current {
		t.Fatalf("failed refresh should keep current token, got (%q, %v)", got, err)
	}

	// Backoff: a second immediate call must not hammer the endpoint.
	got, err = m.EnsureValidToken(context.Background(), false)
	if err != nil || got != current {
		t.Fatalf("backoff should keep current token, got (%q, %v)", got, err)
	}
	if refresh.calls.Load() != 1 {
		t.Fatalf("refresh calls = %d, want 1 (backoff)", refresh.calls.Load())
	}

	// After the backoff elapses the retry happens; success replaces the token.
	clock.Advance(1)
	second := tokenWithValidity(clock.now, 3600)
	refresh.err = nil
	refresh.tokens = pushTokens(second)
	got, err = m.EnsureValidToken(context.Background(), false)
	if err != nil || got != second {
		t.Fatalf("retry after backoff = (%q, %v), want second", got, err)
	}
}

func TestTrafficTokenManagerFailClosedAfterExpiry(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	shortLived := tokenWithValidity(clock.now, 60)
	refresh := &mockRefresh{
		err: &TrafficAccessTokenRefreshError{
			TrafficAccessTokenError: &TrafficAccessTokenError{msg: "down"},
		},
	}

	m, _ := newTrafficTokenManager(shortLived, refresh.Refresh, clock.Now, staticRandom)

	// 60s validity -> refresh ahead = max(60, 12) = 60 -> refreshAt = exp - 60
	// = now. The first call already refreshes and fails but the token is
	// still valid, so it is returned.
	if got, err := m.EnsureValidToken(context.Background(), false); err != nil || got != shortLived {
		t.Fatalf("initial = (%q, %v), want current token", got, err)
	}

	clock.Advance(61) // token expired
	_, err := m.EnsureValidToken(context.Background(), false)
	var expired *TrafficAccessTokenExpired
	if !errors.As(err, &expired) {
		t.Fatalf("want TrafficAccessTokenExpired after expiry, got %v", err)
	}
}

func TestTrafficTokenManagerRetryAfterHonored(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	current := tokenWithValidity(clock.now, 3600)
	refresh := &mockRefresh{
		err: &TrafficAccessTokenRefreshError{
			TrafficAccessTokenError: &TrafficAccessTokenError{msg: "rate limited"},
			RetryAfter:              10 * time.Second,
		},
	}

	m, _ := newTrafficTokenManager(current, refresh.Refresh, clock.Now, staticRandom)
	clock.Advance(3301) // past refresh_at

	// First failure schedules the retry 10s out (server's Retry-After).
	if _, err := m.EnsureValidToken(context.Background(), false); err != nil {
		t.Fatalf("first failure should keep the current token: %v", err)
	}

	// 9s later still in backoff.
	clock.Advance(9)
	if got, _ := m.EnsureValidToken(context.Background(), false); got != current {
		t.Fatal("expected current token during Retry-After backoff")
	}
	if refresh.calls.Load() != 1 {
		t.Fatalf("refresh calls = %d, want 1 during Retry-After", refresh.calls.Load())
	}

	// 1s more: retry succeeds.
	clock.Advance(1)
	second := tokenWithValidity(clock.now, 3600)
	refresh.err = nil
	refresh.tokens = pushTokens(second)
	if got, err := m.EnsureValidToken(context.Background(), false); err != nil || got != second {
		t.Fatalf("retry = (%q, %v), want second", got, err)
	}
}

func TestTrafficTokenManagerResponseValidation(t *testing.T) {
	// Each subtest gets a fresh clock and current token: they mutate the
	// shared fake clock by advancing past the refresh window.
	newManager := func(t *testing.T, respond func(now float64) TrafficAccessToken) (*trafficTokenManager, *fakeClock, string) {
		t.Helper()
		clock := &fakeClock{now: 1_000_000}
		current := tokenWithValidity(clock.now, 3600)
		m, _ := newTrafficTokenManager(current, func(ctx context.Context) (TrafficAccessToken, error) {
			return respond(clock.now), nil
		}, clock.Now, staticRandom)
		return m, clock, current
	}

	t.Run("mismatched expiration", func(t *testing.T) {
		// Token says exp = now+3600 but the response reports now+1200.
		m, clock, current := newManager(t, func(now float64) TrafficAccessToken {
			return TrafficAccessToken{
				Token:     tokenWithValidity(now, 3600),
				ExpiresAt: time.Unix(int64(now+1200), 0).UTC(),
			}
		})
		clock.Advance(3301)
		got, err := m.EnsureValidToken(context.Background(), false)
		if err != nil || got != current {
			t.Fatalf("mismatched exp should be rejected and keep current, got (%q, %v)", got, err)
		}
	})

	t.Run("already expired", func(t *testing.T) {
		m, clock, current := newManager(t, func(now float64) TrafficAccessToken {
			// Token and reported expiration agree, but both are in the past.
			return TrafficAccessToken{
				Token:     tokenWithValidity(now-10, 5),
				ExpiresAt: time.Unix(int64(now-5), 0).UTC(),
			}
		})
		clock.Advance(3301)
		got, err := m.EnsureValidToken(context.Background(), false)
		if err != nil || got != current {
			t.Fatalf("expired refresh response should keep current, got (%q, %v)", got, err)
		}
	})

	t.Run("empty token", func(t *testing.T) {
		m, clock, current := newManager(t, func(now float64) TrafficAccessToken {
			return TrafficAccessToken{
				Token:     "",
				ExpiresAt: time.Unix(int64(now+3600), 0).UTC(),
			}
		})
		clock.Advance(3301)
		got, err := m.EnsureValidToken(context.Background(), false)
		if err != nil || got != current {
			t.Fatalf("empty refresh response should keep current, got (%q, %v)", got, err)
		}
	})
}

func TestTrafficTokenManagerConcurrentRefreshCoalesced(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	current := tokenWithValidity(clock.now, 3600)

	// Slow refresh: hand out the token only after all callers are blocked.
	gate := make(chan struct{})
	var refreshCalls atomic.Int64
	refreshFunc := func(ctx context.Context) (TrafficAccessToken, error) {
		refreshCalls.Add(1)
		<-gate
		token := tokenWithValidity(clock.now, 3600)
		return TrafficAccessToken{
			Token:     token,
			ExpiresAt: time.Unix(int64(jwtExp(token)), 0).UTC(),
		}, nil
	}

	m, _ := newTrafficTokenManager(current, refreshFunc, clock.Now, staticRandom)
	clock.Advance(3301) // everyone needs a refresh

	const callers = 8
	var wg sync.WaitGroup
	start := make(chan struct{})
	for i := 0; i < callers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			<-start
			_, _ = m.EnsureValidToken(context.Background(), false)
		}()
	}
	close(start)
	// Let the callers pile up behind the write lock, then release the refresh.
	time.Sleep(50 * time.Millisecond)
	close(gate)
	wg.Wait()

	if got := refreshCalls.Load(); got != 1 {
		t.Errorf("concurrent refresh calls = %d, want 1 (coalesced)", got)
	}
}

func TestTrafficTokenManagerForcedRefreshCoalescedWhileInFlight(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	current := tokenWithValidity(clock.now, 3600)

	entered := make(chan struct{})
	gate := make(chan struct{})
	var refreshCalls atomic.Int64
	refreshFunc := func(ctx context.Context) (TrafficAccessToken, error) {
		refreshCalls.Add(1)
		// Signal that a refresh is in flight, then hold it open until the
		// test releases the gate.
		close(entered)
		<-gate
		token := tokenWithValidity(clock.now, 3600)
		return TrafficAccessToken{
			Token:     token,
			ExpiresAt: time.Unix(int64(jwtExp(token)), 0).UTC(),
		}, nil
	}

	m, _ := newTrafficTokenManager(current, refreshFunc, clock.Now, staticRandom)

	// Caller 1 starts a forced refresh and parks inside it (holding the
	// write lock, exactly like a real in-flight refresh).
	var wg sync.WaitGroup
	wg.Add(1)
	go func() {
		defer wg.Done()
		if _, err := m.EnsureValidToken(context.Background(), true); err != nil {
			t.Errorf("caller 1: %v", err)
		}
	}()
	<-entered

	// While the refresh is in flight, concurrent forced callers must
	// coalesce onto it instead of starting their own refresh: they snapshot
	// the in-flight state before parking on the lock. (A caller arriving
	// after a refresh completes legitimately refreshes again — that is the
	// "forced refresh is new" test below.)
	const lateCallers = 4
	var lateEntered atomic.Int64
	for i := 0; i < lateCallers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			lateEntered.Add(1)
			if _, err := m.EnsureValidToken(context.Background(), true); err != nil {
				t.Errorf("in-flight caller: %v", err)
			}
		}()
	}
	// Let every late caller take its pre-lock snapshot before releasing the
	// refresh. (Snapshotting is the first thing EnsureValidToken does, right
	// after the entry count above.)
	for lateEntered.Load() < lateCallers {
		time.Sleep(time.Millisecond)
	}
	time.Sleep(25 * time.Millisecond)

	close(gate)
	wg.Wait()
	if got := refreshCalls.Load(); got != 1 {
		t.Errorf("refresh calls = %d, want 1 (in-flight callers coalesced)", got)
	}
}

func TestTrafficTokenManagerForcedRefreshAfterCompletionRefreshesAgain(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	current := tokenWithValidity(clock.now, 3600)

	var refreshCalls atomic.Int64
	refreshFunc := func(ctx context.Context) (TrafficAccessToken, error) {
		refreshCalls.Add(1)
		token := tokenWithValidity(clock.now, 3600)
		return TrafficAccessToken{
			Token:     token,
			ExpiresAt: time.Unix(int64(jwtExp(token)), 0).UTC(),
		}, nil
	}

	m, _ := newTrafficTokenManager(current, refreshFunc, clock.Now, staticRandom)

	// "Force" means "give me a new token": a forced call after a completed
	// refresh must refresh again (each call yields a fresh token).
	if _, err := m.EnsureValidToken(context.Background(), true); err != nil {
		t.Fatalf("first forced refresh: %v", err)
	}
	if _, err := m.EnsureValidToken(context.Background(), true); err != nil {
		t.Fatalf("second forced refresh: %v", err)
	}
	if got := refreshCalls.Load(); got != 2 {
		t.Errorf("refresh calls = %d, want 2", got)
	}
}

func TestTrafficTokenManagerRefreshDetachedFromCallerCancel(t *testing.T) {
	clock := &fakeClock{now: 1_000_000}
	current := tokenWithValidity(clock.now, 3600)

	// The refresh completes only after the caller's context is cancelled.
	gate := make(chan struct{})
	var refreshed atomic.Bool
	refreshFunc := func(ctx context.Context) (TrafficAccessToken, error) {
		<-gate
		if err := ctx.Err(); err != nil {
			return TrafficAccessToken{}, err
		}
		refreshed.Store(true)
		token := tokenWithValidity(clock.now, 3600)
		return TrafficAccessToken{
			Token:     token,
			ExpiresAt: time.Unix(int64(jwtExp(token)), 0).UTC(),
		}, nil
	}

	m, _ := newTrafficTokenManager(current, refreshFunc, clock.Now, staticRandom)
	clock.Advance(3301)

	ctx, cancel := context.WithCancel(context.Background())
	done := make(chan struct{})
	go func() {
		defer close(done)
		_, _ = m.EnsureValidToken(ctx, false)
	}()
	time.Sleep(50 * time.Millisecond)
	cancel()
	close(gate)
	<-done

	if !refreshed.Load() {
		t.Error("refresh must not be cancelled by the caller's context")
	}
}

// --- HTTP integration: refresh endpoint + data-plane header injection ---

// refreshEndpointSetup spins up a mock management API answering the refresh
// endpoint and a fake data-plane recording the traffic token header.
func refreshEndpointSetup(t *testing.T, tokens chan string, apiCalls *atomic.Int64) (apiURL string, dataPlaneURL string, dataPlaneCalls *atomic.Int64, lastTrafficHeader func() string) {
	t.Helper()

	var mu sync.Mutex
	var dpCalls atomic.Int64
	lastHeader := ""

	dataPlane := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		dpCalls.Add(1)
		mu.Lock()
		lastHeader = r.Header.Get(runtime.TrafficTokenHeader)
		mu.Unlock()
		// "[]" lets CodeInterpreter.ListContexts decode an empty result.
		_, _ = w.Write([]byte("[]"))
	}))
	t.Cleanup(dataPlane.Close)

	api := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		apiCalls.Add(1)
		if r.Method != http.MethodPost {
			t.Errorf("refresh method = %s, want POST", r.Method)
		}
		if !strings.HasSuffix(r.URL.Path, "/traffic-access-token") {
			t.Errorf("refresh path = %s, want .../traffic-access-token", r.URL.Path)
		}
		if got := r.Header.Get("X-API-Key"); got != "test-key" {
			t.Errorf("X-API-Key = %q, want test-key", got)
		}
		select {
		case token := <-tokens:
			exp := jwtExp(token)
			w.Header().Set("Content-Type", "application/json")
			_ = json.NewEncoder(w).Encode(map[string]any{
				"trafficAccessToken":           token,
				"trafficAccessTokenExpiration": time.Unix(int64(exp), 0).UTC().Format(time.RFC3339),
			})
		default:
			w.Header().Set("Retry-After", "60")
			w.WriteHeader(http.StatusTooManyRequests)
		}
	}))
	t.Cleanup(api.Close)

	return api.URL, dataPlane.URL, &dpCalls, func() string {
		mu.Lock()
		defer mu.Unlock()
		return lastHeader
	}
}

func TestSandboxApiRefreshTrafficAccessToken(t *testing.T) {
	now := float64(time.Now().Unix())
	token := tokenWithValidity(now, 3600)
	// One token per call: the escaped-ID call performs its own refresh.
	tokens := pushTokens(token, token)
	var apiCalls atomic.Int64
	apiURL, _, _, _ := refreshEndpointSetup(t, tokens, &apiCalls)

	cfg := NewConnectionConfig(
		WithDomain("unused"),
		WithAPIKey("test-key"),
		WithAPIURL(apiURL),
	)
	api := NewSandboxApi(cfg)

	got, err := api.RefreshTrafficAccessToken(context.Background(), "sbx-1")
	if err != nil {
		t.Fatalf("refresh: %v", err)
	}
	if got.Token != token {
		t.Errorf("token = %q, want %q", got.Token, token)
	}
	if apiCalls.Load() != 1 {
		t.Errorf("api calls = %d, want 1", apiCalls.Load())
	}

	// Sandbox IDs are URL-escaped into the path.
	_, err = api.RefreshTrafficAccessToken(context.Background(), "sbx/needs-escaping")
	if err != nil {
		t.Fatalf("refresh with escaped ID: %v", err)
	}
}

func TestSandboxApiRefreshTrafficAccessTokenError(t *testing.T) {
	tokens := pushTokens() // empty: endpoint replies 429 + Retry-After
	var apiCalls atomic.Int64
	apiURL, _, _, _ := refreshEndpointSetup(t, tokens, &apiCalls)

	cfg := NewConnectionConfig(
		WithDomain("unused"),
		WithAPIKey("test-key"),
		WithAPIURL(apiURL),
	)
	api := NewSandboxApi(cfg)

	_, err := api.RefreshTrafficAccessToken(context.Background(), "sbx-1")
	var refreshErr *TrafficAccessTokenRefreshError
	if !errors.As(err, &refreshErr) {
		t.Fatalf("want TrafficAccessTokenRefreshError, got %v", err)
	}
	if refreshErr.StatusCode != http.StatusTooManyRequests {
		t.Errorf("status = %d, want 429", refreshErr.StatusCode)
	}
	if refreshErr.RetryAfter != 60*time.Second {
		t.Errorf("retry-after = %v, want 60s", refreshErr.RetryAfter)
	}
	// The error must not embed the response body.
	if strings.Contains(err.Error(), "body") {
		t.Errorf("error message should not leak response body: %v", err)
	}
}

func TestSandboxDataPlaneHeaderInjection(t *testing.T) {
	now := float64(time.Now().Unix())
	first := tokenWithValidity(now, 3600)
	second := tokenWithValidity(now+3599, 3600)
	tokens := pushTokens(first, second)
	var apiCalls atomic.Int64
	apiURL, dataPlaneURL, dataPlaneCalls, lastHeader := refreshEndpointSetup(t, tokens, &apiCalls)

	// Point the sandbox data-plane at the recording server: a native-style
	// subdomain is impossible with httptest, so override the sandbox URL
	// wholesale (the sandbox ID then rides only in the header).
	cfg := NewConnectionConfig(
		WithDomain("unused"),
		WithAPIKey("test-key"),
		WithAPIURL(apiURL),
		WithSandboxBaseURL(dataPlaneURL),
	)

	sb := &Sandbox{
		templateID: "code-interpreter",
		config:     cfg,
		api:        NewSandboxApi(cfg),
	}
	envdConfig := cfg.toEnvdConfig("sbx-1")
	envdConfig.TrafficTokenProvider = sb.trafficTokenProvider
	sb.Client = runtime.NewWithConfig("sbx-1", envdConfig)

	// Without a manager the data-plane request carries no traffic header.
	if _, err := sb.CodeInterpreter.ListContexts(context.Background()); err != nil {
		t.Fatalf("data-plane call without manager: %v", err)
	}
	if got := lastHeader(); got != "" {
		t.Errorf("traffic header = %q, want empty without manager", got)
	}
	before := dataPlaneCalls.Load()

	// Installing the manager bootstraps the token on the next request: the
	// transport asks the provider, which refreshes and injects the header.
	sb.installTrafficTokenManager("")
	if _, err := sb.CodeInterpreter.ListContexts(context.Background()); err != nil {
		t.Fatalf("data-plane call with manager: %v", err)
	}
	if got, want := lastHeader(), first; got != want {
		t.Errorf("traffic header = %q, want %q", got, want)
	}
	if apiCalls.Load() != 1 {
		t.Errorf("refresh calls = %d, want 1", apiCalls.Load())
	}
	if dataPlaneCalls.Load() != before+1 {
		t.Errorf("data-plane calls = %d, want %d", dataPlaneCalls.Load(), before+1)
	}

	// A forced refresh swaps the token for subsequent requests.
	if _, err := sb.RefreshTrafficAccessToken(context.Background()); err != nil {
		t.Fatalf("forced refresh: %v", err)
	}
	if _, err := sb.CodeInterpreter.ListContexts(context.Background()); err != nil {
		t.Fatalf("data-plane call after forced refresh: %v", err)
	}
	if got, want := lastHeader(), second; got != want {
		t.Errorf("traffic header after forced refresh = %q, want %q", got, want)
	}

	// The cached token is exposed for diagnostics.
	if got := sb.TrafficAccessToken(); got != second {
		t.Errorf("TrafficAccessToken() = %q, want %q", got, second)
	}
}

func TestSandboxNonJWTTokenKeepsLegacyBehavior(t *testing.T) {
	cfg := NewConnectionConfig(WithDomain("unused"))

	sb := &Sandbox{
		config: cfg,
		api:    NewSandboxApi(cfg),
	}
	envdConfig := cfg.toEnvdConfig("sbx-1")
	envdConfig.TrafficTokenProvider = sb.trafficTokenProvider
	sb.Client = runtime.NewWithConfig("sbx-1", envdConfig)

	// Legacy opaque tokens are rejected at install time: no manager, no
	// refresh, no traffic header.
	sb.installTrafficTokenManager("5b14a58f-93f4-4d3e-9a92-2f3e0e1a9e33")
	if got := sb.TrafficAccessToken(); got != "" {
		t.Errorf("legacy token should not install a manager, got %q", got)
	}
	if _, err := sb.RefreshTrafficAccessToken(context.Background()); err == nil {
		t.Error("RefreshTrafficAccessToken should fail without a manager")
	}
}

func TestSandboxTrafficTokenProviderWithoutManager(t *testing.T) {
	cfg := NewConnectionConfig(WithDomain("unused"))
	sb := &Sandbox{
		config: cfg,
		api:    NewSandboxApi(cfg),
	}

	// No manager installed: the provider returns an empty token (the
	// transport then sends no header) instead of failing the request.
	token, err := sb.trafficTokenProvider(context.Background())
	if err != nil {
		t.Fatalf("provider without manager: %v", err)
	}
	if token != "" {
		t.Errorf("token = %q, want empty", token)
	}
}
