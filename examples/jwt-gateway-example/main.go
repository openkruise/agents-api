// Command jwt-gateway-example is an end-to-end demo of Traffic JWT against a
// sandbox-gateway with enable-jwt-auth — the Go counterpart of the Python
// SDK's demo_jwt_gateway.py.
//
// Deployment prerequisites (same as the Python demo):
//
//  1. jwt-e2e-oidc-provider deployed in sandbox-system, reachable in-cluster.
//  2. sandbox-gateway upgraded with:
//     gateway.envoy.pluginConfig.enableJwtAuth=true
//     gateway.envoy.oidc.discoveryUrl=https://jwt-e2e-oidc-provider.sandbox-system.svc:8443/...
//     gateway.envoy.oidc.caConfigMap.namespace/name pointing at the provider CA.
//  3. kubectl on PATH with access to the sandbox resources.
//
// Environment variables:
//
//	E2B_DOMAIN                 sandbox domain (required): the management API and
//	                           the data plane are both derived from it, exactly
//	                           like the Python demo's patch_e2b derivation
//	E2B_API_URL                management API URL override (optional; defaults
//	                           to {scheme}://{E2B_DOMAIN}/kruise/api)
//	E2B_API_KEY                management API key (optional)
//	JWT_PROVIDER_SERVICE       OIDC provider service (default jwt-e2e-oidc-provider)
//	JWT_PROVIDER_NAMESPACE     OIDC provider namespace (default sandbox-system)
//	JWT_PROVIDER_FORWARD_PORT  local port for kubectl port-forward (default 18080)
//	JWT_TOKEN_VALIDITY_SECONDS token validity issued by the provider (default 65)
//	SSL_CERT_FILE              when set, https is used (data plane and proxy upstream)
//
// Because the OSS sandbox-manager issues opaque UUID tokens, this demo mirrors
// the upstream E2E suite: real JWTs are issued through the external provider
// and injected via e2b.WithTrafficAccessToken (the Go equivalent of rebuilding
// the Python Sandbox with traffic_access_token). With a legacy UUID seed the
// SDK degrades to the legacy behavior — no traffic header — so the gateway
// rejects the data-plane call either way; the Python demo shows the same
// rejection while carrying the UUID as the header value.
//
// Unlike the Python demo — which monkey-patches the refresh function to point
// at the external issuer — this demo runs a local translating proxy: the
// sandbox's refresh endpoint (POST /sandboxes/{id}/traffic-access-token) is
// translated into the provider's /issue call, while every other management
// request is reverse-proxied to the real manager. The full SDK refresh path
// (headers, Retry-After handling, response validation) is therefore exercised
// end to end.
//
// Run on a host with kubectl access to the cluster:
//
//	go run ./examples/jwt-gateway-example
package main

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/openkruise/agents-api/e2b"
)

// envOr returns the value of the environment variable key, or fallback when
// unset or empty.
func envOr(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func main() {
	ctx := context.Background()

	domain := os.Getenv("E2B_DOMAIN")
	if domain == "" {
		log.Fatal("E2B_DOMAIN must be set")
	}
	apiKey := os.Getenv("E2B_API_KEY")

	// https when SSL_CERT_FILE is set — the Go HTTP stack honors that variable
	// natively for custom CAs — matching the Python demo's scheme selection.
	scheme := "http"
	if os.Getenv("SSL_CERT_FILE") != "" {
		scheme = "https"
	}

	// The Python demo runs off just E2B_DOMAIN because patch_e2b derives the
	// management URL itself and exports it as E2B_API_URL
	// ({scheme}://{domain}/kruise/api — private path routing). Default to the
	// same derivation here so the same env.sh works unchanged; E2B_API_URL
	// remains available as an explicit override.
	managerURL := os.Getenv("E2B_API_URL")
	if managerURL == "" {
		managerURL = scheme + "://" + domain + "/kruise/api"
	}

	// Shared connection options: private path routing, with the management
	// API at {scheme}://{domain}/kruise/api and the data plane at
	// {scheme}://{domain}/kruise/{sandboxID}/{port} (see WithProtocol).
	baseOpts := []e2b.ConnectionConfigOption{
		e2b.WithDomain(domain),
		e2b.WithScheme(scheme),
		e2b.WithProtocol(e2b.ProtocolPrivate),
		e2b.WithAPIKey(apiKey),
	}
	if managerOverride := os.Getenv("E2B_API_URL"); managerOverride != "" {
		baseOpts = append(baseOpts, e2b.WithAPIURL(managerOverride))
	}

	providerService := envOr("JWT_PROVIDER_SERVICE", "jwt-e2e-oidc-provider")
	providerNamespace := envOr("JWT_PROVIDER_NAMESPACE", "sandbox-system")
	forwardPort := envOr("JWT_PROVIDER_FORWARD_PORT", "18080")
	validity := 65
	if v := os.Getenv("JWT_TOKEN_VALIDITY_SECONDS"); v != "" {
		n, err := strconv.Atoi(v)
		if err != nil || n <= 0 {
			log.Fatalf("invalid JWT_TOKEN_VALIDITY_SECONDS %q", v)
		}
		validity = n
	}
	providerBase := "http://127.0.0.1:" + forwardPort
	issueURL := providerBase + "/issue"

	fmt.Println("========== Traffic JWT gateway demo (Go) ==========")
	fmt.Printf("Manager: %s, domain: %s, scheme: %s\n", managerURL, domain, scheme)
	fmt.Printf("Provider: %s/%s (kubectl port-forward %s), token validity: %ds\n",
		providerNamespace, providerService, forwardPort, validity)

	// [setup] port-forward the OIDC provider.
	fmt.Println("\n[setup] port-forwarding the OIDC provider")
	forward, err := startPortForward(providerService, providerNamespace, forwardPort)
	if err != nil {
		log.Fatalf("port-forward: %v", err)
	}
	defer stopPortForward(forward)
	if err := waitForProvider(providerBase + "/"); err != nil {
		log.Fatalf("provider did not become ready: %v", err)
	}

	// [setup] local refresh-translating proxy. The sandbox UID is looked up
	// with kubectl on first use and cached (it never changes).
	fmt.Println("[setup] starting the refresh-translating proxy")
	var uidCache sync.Map // sandbox ID -> UID
	uidFor := func(sandboxID string) (string, error) {
		if v, ok := uidCache.Load(sandboxID); ok {
			return v.(string), nil
		}
		uid, err := sandboxUID(sandboxID)
		if err != nil {
			return "", err
		}
		uidCache.Store(sandboxID, uid)
		return uid, nil
	}
	proxyURL, stopProxy, err := startRefreshProxy(managerURL, issueURL, uidFor, validity)
	if err != nil {
		log.Fatalf("refresh proxy: %v", err)
	}
	defer stopProxy()
	fmt.Printf("       proxy listening on %s (refresh -> %s)\n", proxyURL, issueURL)

	// [setup] create the sandbox with the JWT opt-in annotation. The OSS
	// sandbox-manager answers with an opaque UUID token, so no manager is
	// installed: data-plane calls send no traffic header until a real JWT
	// is injected below.
	fmt.Println("\n[setup] creating sandbox with the JWT opt-in annotation")
	sb, err := e2b.Create(ctx, "code-interpreter",
		e2b.WithConfig(baseOpts...),
		e2b.WithMetadata(map[string]string{e2b.JWTAuthMetadataKey: "true"}),
		e2b.WithTimeout(600),
	)
	if err != nil {
		log.Fatalf("create sandbox: %v", err)
	}
	fmt.Printf("       sandbox created: %s\n", sb.SandboxID())
	fmt.Println("       (the OSS manager issued an opaque UUID token; the SDK degraded to legacy behavior)")
	defer cleanup(ctx, sb)

	// [1] Without a valid Traffic JWT the gateway must reject the request.
	fmt.Println("\n[1] data plane without a valid Traffic JWT: the gateway must reject it")
	if _, err := sb.Commands.Run(ctx, "echo should-not-pass"); err != nil {
		fmt.Printf("    rejected as expected: %v\n", err)
	} else {
		fmt.Println("    WARNING: command succeeded — is enableJwtAuth active on the gateway?")
	}

	// [2] Issue a real JWT through the external provider and seed it.
	fmt.Println("\n[2] data plane with an externally issued Traffic JWT")
	uid, err := uidFor(sb.SandboxID())
	if err != nil {
		log.Fatalf("sandbox UID: %v", err)
	}
	fmt.Printf("    sandbox UID: %s\n", uid)

	issued, err := issueTrafficToken(sb.SandboxID(), uid, validity, issueURL)
	if err != nil {
		log.Fatalf("issue token: %v", err)
	}
	fmt.Printf("    issued JWT: %s\n", describeToken(issued.Token))

	// Connect through the translating proxy and seed the issued JWT: the
	// manager attaches it to every data-plane request and refreshes it via
	// the proxy, which routes to the external issuer (playing the role of
	// the enterprise identity provider a JWT-enabled manager would use).
	// The connection differs from baseOpts only in the management URL.
	proxyOpts := append(append([]e2b.ConnectionConfigOption{}, baseOpts...), e2b.WithAPIURL(proxyURL))
	sb2, err := e2b.Connect(ctx, sb.SandboxID(),
		e2b.WithConfig(proxyOpts...),
		e2b.WithTrafficAccessToken(issued.Token),
	)
	if err != nil {
		log.Fatalf("connect with seed: %v", err)
	}
	if res, err := sb2.Commands.Run(ctx, "echo 'hello with traffic jwt'"); err != nil {
		log.Fatalf("command with traffic jwt: %v", err)
	} else {
		fmt.Printf("    command output: %s", res.Stdout)
	}
	if execution, err := sb2.CodeInterpreter.RunCode(ctx, "print('hello from jupyter with traffic jwt')"); err != nil {
		log.Fatalf("run_code with traffic jwt: %v", err)
	} else {
		// print() output lands in the stdout logs, not the result text
		// (the Python demo reads execution.logs.stdout likewise).
		fmt.Printf("    jupyter output: %s\n", strings.TrimSpace(strings.Join(execution.Logs.Stdout, "")))
	}

	// [3] Watch the token rotate automatically while the data plane stays up.
	fmt.Printf("\n[3] automatic refresh with %ds validity\n", validity)
	fmt.Println("    (the OSS manager's refresh endpoint returns UUIDs, so the local")
	fmt.Println("     proxy routes refreshes to the external issuer)")
	deadline := time.Now().Add(time.Duration(max(90, validity*2)) * time.Second)
	current := sb2.TrafficAccessToken()
	refreshes := 0
	for time.Now().Before(deadline) {
		res, err := sb2.Commands.Run(ctx, "echo keepalive")
		if err != nil || strings.TrimSpace(res.Stdout) != "keepalive" {
			log.Fatalf("data plane broke during rotation watch: %v", err)
		}
		if token := sb2.TrafficAccessToken(); token != current {
			refreshes++
			fmt.Printf("    refreshed -> %s (data plane still up)\n", describeToken(token))
			current = token
			if refreshes >= 2 {
				break
			}
		}
		time.Sleep(2 * time.Second)
	}
	fmt.Printf("    observed %d automatic refresh(es); data plane stayed alive\n", refreshes)
}

// kubectl runs kubectl and returns its stdout.
func kubectl(args ...string) (string, error) {
	out, err := exec.Command("kubectl", args...).Output()
	if err != nil {
		if ee, ok := err.(*exec.ExitError); ok {
			return "", fmt.Errorf("kubectl %s: %s", strings.Join(args, " "), ee.Stderr)
		}
		return "", fmt.Errorf("kubectl %s: %v", strings.Join(args, " "), err)
	}
	return strings.TrimSpace(string(out)), nil
}

// sandboxUID looks up the Kubernetes UID of a sandbox. Sandbox IDs have the
// form "<namespace>--<name>".
func sandboxUID(sandboxID string) (string, error) {
	namespace, name, found := strings.Cut(sandboxID, "--")
	if !found {
		return "", fmt.Errorf("sandbox ID %q is not in <namespace>--<name> form", sandboxID)
	}
	return kubectl("get", "sandbox", name, "-n", namespace, "-o", "jsonpath={.metadata.uid}")
}

// issuedToken mirrors the provider's /issue response.
type issuedToken struct {
	Token      string
	Expiration string // raw provider value, passed through to the SDK
}

// issueTrafficToken mints a Traffic JWT through the external provider.
func issueTrafficToken(sandboxID, sandboxUID string, validity int, issueURL string) (issuedToken, error) {
	body, err := json.Marshal(map[string]any{
		"sandboxId":       sandboxID,
		"sandboxUid":      sandboxUID,
		"validitySeconds": validity,
	})
	if err != nil {
		return issuedToken{}, err
	}
	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Post(issueURL, "application/json", bytes.NewReader(body))
	if err != nil {
		return issuedToken{}, err
	}
	defer resp.Body.Close()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return issuedToken{}, fmt.Errorf("issue returned status %d", resp.StatusCode)
	}
	var payload struct {
		AccessToken           string `json:"accessToken"`
		AccessTokenExpiration string `json:"accessTokenExpiration"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&payload); err != nil {
		return issuedToken{}, err
	}
	return issuedToken{Token: payload.AccessToken, Expiration: payload.AccessTokenExpiration}, nil
}

// describeToken renders a JWT tail plus its exp/iat claims for display.
func describeToken(token string) string {
	tail := token
	if len(tail) > 16 {
		tail = tail[len(tail)-16:]
	}
	if exp, iat, ok := jwtTimes(token); ok {
		issued := ""
		if !iat.IsZero() {
			issued = fmt.Sprintf(", issued %s", iat.Format("15:04:05"))
		}
		return fmt.Sprintf("...%s (expires %s%s)", tail, exp.Format("15:04:05"), issued)
	}
	return "..." + tail
}

// jwtTimes extracts the exp/iat claims from a JWT payload.
func jwtTimes(token string) (exp, iat time.Time, ok bool) {
	parts := strings.Split(token, ".")
	if len(parts) != 3 {
		return time.Time{}, time.Time{}, false
	}
	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return time.Time{}, time.Time{}, false
	}
	var claims struct {
		Exp float64 `json:"exp"`
		Iat float64 `json:"iat"`
	}
	if err := json.Unmarshal(payload, &claims); err != nil {
		return time.Time{}, time.Time{}, false
	}
	return time.Unix(int64(claims.Exp), 0).UTC(), time.Unix(int64(claims.Iat), 0).UTC(), true
}

// startPortForward opens `kubectl port-forward` for the provider service.
func startPortForward(service, namespace, port string) (*exec.Cmd, error) {
	cmd := exec.Command("kubectl", "port-forward", "svc/"+service, port+":8080", "-n", namespace)
	if err := cmd.Start(); err != nil {
		return nil, err
	}
	return cmd, nil
}

// stopPortForward terminates a port-forward process.
func stopPortForward(cmd *exec.Cmd) {
	_ = cmd.Process.Kill()
	_, _ = cmd.Process.Wait()
}

// waitForProvider polls the forwarded provider until it answers.
func waitForProvider(baseURL string) error {
	client := &http.Client{Timeout: time.Second}
	for i := 0; i < 30; i++ {
		resp, err := client.Get(baseURL)
		if err == nil {
			resp.Body.Close()
			return nil
		}
		time.Sleep(time.Second)
	}
	return fmt.Errorf("port-forward did not become ready within 30s")
}

// startRefreshProxy runs the local translating proxy: refresh requests are
// rewritten into provider /issue calls, everything else is reverse-proxied to
// the real management API. It returns the proxy's URL and a stop function.
func startRefreshProxy(apiURL, issueURL string, uidFor func(string) (string, error), validity int) (string, func(), error) {
	upstream, err := url.Parse(apiURL)
	if err != nil {
		return "", nil, fmt.Errorf("parse API URL: %w", err)
	}
	reverse := httputil.NewSingleHostReverseProxy(upstream)

	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost && strings.HasSuffix(r.URL.Path, "/traffic-access-token") {
			translateRefresh(w, r, issueURL, uidFor, validity)
			return
		}
		// Present the real manager's host instead of the local proxy's.
		r.Host = upstream.Host
		reverse.ServeHTTP(w, r)
	})

	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		return "", nil, err
	}
	srv := &http.Server{Handler: handler}
	go func() { _ = srv.Serve(ln) }()
	return "http://" + ln.Addr().String(), func() { _ = srv.Close() }, nil
}

// translateRefresh answers an SDK refresh request by issuing a JWT through
// the external provider and renaming the response fields.
func translateRefresh(w http.ResponseWriter, r *http.Request, issueURL string, uidFor func(string) (string, error), validity int) {
	sandboxID := strings.TrimSuffix(r.URL.Path, "/traffic-access-token")
	sandboxID = strings.TrimPrefix(sandboxID, "/sandboxes/")
	if unescaped, err := url.PathUnescape(strings.Trim(sandboxID, "/")); err == nil {
		sandboxID = unescaped
	}
	uid, err := uidFor(sandboxID)
	if err != nil {
		http.Error(w, "sandbox UID lookup failed: "+err.Error(), http.StatusInternalServerError)
		return
	}

	token, err := issueTrafficToken(sandboxID, uid, validity, issueURL)
	if err != nil {
		http.Error(w, "token issuance failed: "+err.Error(), http.StatusBadGateway)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]any{
		"trafficAccessToken":           token.Token,
		"trafficAccessTokenExpiration": token.Expiration,
	})
}

// cleanup kills the sandbox at the end of execution.
func cleanup(ctx context.Context, sb *e2b.Sandbox) {
	fmt.Println("\n[cleanup] killing sandbox")
	if _, err := sb.Kill(ctx); err != nil {
		fmt.Printf("    kill failed (sandbox may have timed out already): %v\n", err)
	}
}
