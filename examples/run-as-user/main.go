// Command run-as-user verification for the Go SDKs. One binary covers both
// SDK flavors because the data plane is shared: e2b.Sandbox embeds the
// runtime SDK's *runtime.Client, so every check below exercises the runtime
// package's RunOpts.User implementation while the sandbox itself is managed
// through the e2b control plane.
//
// Environment variables (defaults in parentheses) so a prebuilt binary can
// be pointed at any environment without recompiling:
//
//	E2B_API_KEY   -> apiKey    (your-apiKey)
//	E2B_DOMAIN    -> domain    (your.domain.com)
//	E2B_TEMPLATE  -> template  (code-interpreter)
//	RUN_AS_USER   -> runAsUser (nobody, must exist in the image, must not contain ':')
//	E2B_PROTOCOL  -> protocol  (private; native for subdomain routing)
//	E2B_SCHEME    -> scheme    (derived: https when SSL_CERT_FILE is set, else http)
//	SSL_CERT_FILE -> CA bundle trusted for https (PEM)
package main

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	sandbox "github.com/openkruise/agents-api/e2b"
	"github.com/openkruise/agents-api/runtime"
)

const (
	defaultAPIKey    = "your-apiKey"
	defaultDomain    = "your.domain.com"
	defaultTemplate  = "code-interpreter"
	defaultRunAsUser = "nobody"

	// noSuchUser is expected to be absent from every image; the negative
	// check relies on envd rejecting an unknown username.
	noSuchUser = "no-such-user-run-as-user-demo"

	// totalChecks is the number of checks verify() performs; the result
	// line and the exit-code decision both key off it.
	totalChecks = 5
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
	os.Exit(run())
}

// run performs the verification and returns the process exit code: 0 when
// all checks pass, 1 otherwise. The main/exit-code split exists so cleanup
// can be deferred — deferred calls do not run on os.Exit, and a panic in
// verify must not leak the sandbox.
func run() int {
	ctx := context.Background()

	apiKey := envOr("E2B_API_KEY", defaultAPIKey)
	domain := envOr("E2B_DOMAIN", defaultDomain)
	template := envOr("E2B_TEMPLATE", defaultTemplate)
	runAsUser := envOr("RUN_AS_USER", defaultRunAsUser)

	// Default to the private path-gateway routing ({domain}/kruise/...)
	// like the other verification demos; set E2B_PROTOCOL=native for
	// subdomain-based routing.
	protocol := envOr("E2B_PROTOCOL", string(sandbox.ProtocolPrivate))

	// Derive scheme from SSL_CERT_FILE presence unless set explicitly.
	scheme := envOr("E2B_SCHEME", "")
	certFile := os.Getenv("SSL_CERT_FILE")
	if scheme == "" {
		if certFile != "" {
			scheme = "https"
		} else {
			scheme = "http"
		}
	}

	fmt.Println("========== Run-as-User Verification (Go e2b + runtime SDK) ==========")
	fmt.Printf("Config: domain=%s, template=%s, user=%s, protocol=%s, scheme=%s\n",
		domain, template, runAsUser, protocol, scheme)

	configOpts := []sandbox.ConnectionConfigOption{
		sandbox.WithAPIKey(apiKey),
		sandbox.WithDomain(domain),
		sandbox.WithProtocol(sandbox.Protocol(protocol)),
		sandbox.WithScheme(scheme),
	}

	// Trust the CA bundle from SSL_CERT_FILE (self-signed environments).
	if certFile != "" && scheme == "https" {
		pem, err := os.ReadFile(certFile)
		if err != nil {
			log.Fatalf("Failed to read SSL_CERT_FILE %s: %v", certFile, err)
		}
		pool := x509.NewCertPool()
		if !pool.AppendCertsFromPEM(pem) {
			log.Fatalf("No certificates found in SSL_CERT_FILE %s", certFile)
		}
		configOpts = append(configOpts, sandbox.WithHTTPClient(&http.Client{
			Transport: &http.Transport{TLSClientConfig: &tls.Config{RootCAs: pool}},
		}))
	}

	sb, err := sandbox.Create(ctx, template,
		sandbox.WithConfig(configOpts...),
	)
	if err != nil {
		log.Fatalf("Failed to create sandbox: %v", err)
	}
	fmt.Printf("Sandbox created: %s\n", sb.SandboxID())

	// Kill the sandbox no matter how verify exits (early return or panic).
	defer cleanup(ctx, sb)

	passed := verify(ctx, sb, runAsUser)

	fmt.Printf("\n========== Result: %d/%d checks passed ==========\n", passed, totalChecks)
	if passed != totalChecks {
		return 1
	}
	return 0
}

// verify runs the run-as-user checks against the sandbox and returns how
// many of them passed.
func verify(ctx context.Context, sb *sandbox.Sandbox, runAsUser string) int {
	passed := 0

	// Phase 1 — e2b SDK surface (sb.Commands is the embedded runtime
	// client exposed through the e2b package).

	// [1] Baseline: without User, commands still run as the default user
	// (root), proving the new option is opt-in only.
	fmt.Println("\n[1] e2b SDK: 'whoami' without User (expect root)...")
	if res, err := sb.Commands.Run(ctx, "whoami"); err != nil {
		fmt.Printf("    FAIL: %v\n", err)
	} else if got := strings.TrimSpace(res.Stdout); got != "root" {
		fmt.Printf("    FAIL: whoami = %q, want \"root\"\n", got)
	} else {
		fmt.Println("    PASS: whoami = root")
		passed++
	}

	// [2] 'whoami' with User must report the requested user. An explicit
	// cwd is required because envd falls back to the user's home directory
	// when cwd is empty, and some users (e.g. Debian's nobody ->
	// /nonexistent) have no existing home.
	fmt.Printf("\n[2] e2b SDK: 'whoami' as %s (expect %s)...\n", runAsUser, runAsUser)
	if res, err := sb.Commands.Run(ctx, "whoami", runtime.RunOpts{User: runAsUser, Cwd: "/tmp"}); err != nil {
		fmt.Printf("    FAIL: %v\n", err)
	} else if got := strings.TrimSpace(res.Stdout); got != runAsUser {
		fmt.Printf("    FAIL: whoami = %q, want %q\n", got, runAsUser)
	} else {
		fmt.Printf("    PASS: whoami = %s\n", runAsUser)
		passed++
	}

	// [3] A file created by the user must be owned by that user, proving
	// the process really runs under the user's uid (not just a changed
	// $USER env).
	marker := fmt.Sprintf("/tmp/run_as_user_demo_%d", time.Now().UnixNano())
	fmt.Printf("\n[3] e2b SDK: file created by %s is owned by %s...\n", runAsUser, runAsUser)
	owner := ""
	if _, err := sb.Commands.Run(ctx, "touch "+marker, runtime.RunOpts{User: runAsUser, Cwd: "/tmp"}); err != nil {
		fmt.Printf("    FAIL: touch as %s: %v\n", runAsUser, err)
	} else if res, err := sb.Commands.Run(ctx, "stat -c %U "+marker); err != nil {
		fmt.Printf("    FAIL: stat %s: %v\n", marker, err)
	} else {
		owner = strings.TrimSpace(res.Stdout)
	}
	_, _ = sb.Commands.Run(ctx, "rm -f "+marker)
	if owner != runAsUser {
		fmt.Printf("    FAIL: owner = %q, want %q\n", owner, runAsUser)
	} else {
		fmt.Printf("    PASS: %s is owned by %s\n", marker, runAsUser)
		passed++
	}

	// [4] Negative: an unknown user must be rejected by envd (the username
	// is looked up server-side), not silently ignored.
	fmt.Printf("\n[4] e2b SDK: 'true' as unknown user %s (expect an error)...\n", noSuchUser)
	if res, err := sb.Commands.Run(ctx, "true", runtime.RunOpts{User: noSuchUser}); err == nil {
		fmt.Printf("    FAIL: expected an error, got exit code %d\n", res.ExitCode)
	} else {
		fmt.Printf("    PASS: rejected as expected: %v\n", err)
		passed++
	}

	// Phase 2 — runtime SDK surface. sb.Client is the embedded
	// *runtime.Client; using it directly is the plain runtime SDK usage.
	// Re-check the core assertion through this surface for good measure.
	rc := sb.Client
	fmt.Printf("\n[5] runtime SDK (sb.Client): 'whoami' as %s (expect %s)...\n", runAsUser, runAsUser)
	if res, err := rc.Commands.Run(ctx, "whoami", runtime.RunOpts{User: runAsUser, Cwd: "/tmp"}); err != nil {
		fmt.Printf("    FAIL: %v\n", err)
	} else if got := strings.TrimSpace(res.Stdout); got != runAsUser {
		fmt.Printf("    FAIL: whoami = %q, want %q\n", got, runAsUser)
	} else {
		fmt.Printf("    PASS: whoami = %s\n", runAsUser)
		passed++
	}

	return passed
}

// cleanup kills the sandbox at the end of execution.
func cleanup(ctx context.Context, sb *sandbox.Sandbox) {
	fmt.Println("\n--- Cleaning up ---")
	killed, err := sb.Kill(ctx)
	if err != nil {
		fmt.Printf("Failed to kill sandbox: %v\n", err)
		return
	}
	fmt.Printf("Sandbox %s killed: %v\n", sb.SandboxID(), killed)
}
