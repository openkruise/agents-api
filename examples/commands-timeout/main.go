// Command timeout / shell / disconnect semantics verification for the Go
// SDKs. One binary covers both SDK flavors because the data plane is shared:
// e2b.Sandbox embeds the runtime SDK's *runtime.Client, so every check below
// exercises the runtime package's RunOpts.Shell / Timeout / RequestTimeout
// wiring while the sandbox itself is managed through the e2b control plane.
//
// Verified semantics — the checks marked "official" follow the official
// E2B SDK contract; the rest are local extensions beyond it:
//
//   - RunOpts.RequestTimeout bounds only the wait for the start event: a 1ms
//     timeout must abort with a readable error naming the option. (official)
//   - RunOpts.Timeout bounds the whole stream: Wait fails at the deadline
//     with an error naming the option. (official)
//   - A timeout cuts the stream only: the SDK never kills implicitly, and
//     the unary RPCs (List/Kill) keep working on the cut handle. Process
//     survival after the cut is envd's call — the official E2B envd keeps
//     it running, while the Kruise envd observed on the test bench reaps
//     it shortly after the cut — so the checks below assert the SDK
//     contract and report survival informationally. (official SDK
//     contract; envd-side survival is environment-dependent)
//   - RunOpts.Shell="/bin/sh" executes the command via that shell; unset
//     keeps the /bin/bash default. (local extension: the official SDK
//     hardcodes /bin/bash and has no shell option)
//   - CommandHandle.Disconnect() drops the subscription without killing;
//     the official SDK names the same operation disconnect(). (naming
//     aligned here; local API)
//   - ConnectOpts applies the same two per-call limits to
//     ConnectToProcess: RequestTimeout bounds the wait for the connect
//     start event, Timeout bounds the whole connect stream (previously it
//     had no overall limit at all), both with attribution naming
//     ConnectOpts. (official)
//
// Environment variables (defaults in parentheses) so a prebuilt binary can
// be pointed at any environment without recompiling:
//
//	E2B_API_KEY   -> apiKey   (your-apiKey)
//	E2B_DOMAIN    -> domain   (your.domain.com)
//	E2B_TEMPLATE  -> template (code-interpreter)
//	E2B_PROTOCOL  -> protocol (private; native for subdomain routing)
//	E2B_SCHEME    -> scheme   (derived: https when SSL_CERT_FILE is set, else http)
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
	defaultAPIKey   = "your-apiKey"
	defaultDomain   = "your.domain.com"
	defaultTemplate = "code-interpreter"

	// totalChecks is the number of checks verify() performs; the result
	// line and the exit-code decision both key off it.
	totalChecks = 9

	// marker is embedded in every long-running command so the process can
	// be found again via List (and cleaned up) after its stream is cut.
	marker = "commands-timeout-demo"

	// sleepCmd outlives every check; the checks cut the stream long before
	// the process would exit on its own.
	sleepCmd = "sleep 30 # " + marker
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

	fmt.Println("========== Commands Timeout Verification (Go e2b + runtime SDK) ==========")
	fmt.Printf("Config: domain=%s, template=%s, protocol=%s, scheme=%s\n",
		domain, template, protocol, scheme)

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

	passed := verify(ctx, sb)

	fmt.Printf("\n========== Result: %d/%d checks passed ==========\n", passed, totalChecks)
	if passed != totalChecks {
		return 1
	}
	return 0
}

// cleanup kills the sandbox. It is deferred from run, so it runs even when
// verify panics.
func cleanup(ctx context.Context, sb *sandbox.Sandbox) {
	// The kill RPC must survive any stream state; use a fresh context so a
	// cancelled stream context cannot block sandbox cleanup.
	killCtx, cancel := context.WithTimeout(context.WithoutCancel(ctx), 30*time.Second)
	defer cancel()
	killed, err := sb.Kill(killCtx)
	if err != nil {
		fmt.Printf("Sandbox cleanup error: %v\n", err)
		return
	}
	fmt.Printf("Sandbox %s killed: %v\n", sb.SandboxID(), killed)
}

// verify runs the timeout/shell checks against the sandbox and returns how
// many of them passed.
func verify(ctx context.Context, sb *sandbox.Sandbox) int {
	passed := 0
	commands := sb.Commands

	// [1] Shell override: the command must run via /bin/sh.
	fmt.Println("\n[1] run with Shell=/bin/sh (expect success)...")
	if res, err := commands.Run(ctx, "echo ok", runtime.RunOpts{Shell: "/bin/sh"}); err != nil {
		fmt.Printf("    FAIL: %v\n", err)
	} else if got := strings.TrimSpace(res.Stdout); got != "ok" {
		fmt.Printf("    FAIL: stdout = %q, want \"ok\"\n", got)
	} else {
		fmt.Println("    PASS: exit 0, stdout = ok")
		passed++
	}

	// [2] Baseline: without Shell the /bin/bash default still works,
	// proving the option is opt-in only.
	fmt.Println("\n[2] run without Shell (expect the /bin/bash default to work)...")
	if res, err := commands.Run(ctx, "echo ok"); err != nil {
		fmt.Printf("    FAIL: %v\n", err)
	} else if got := strings.TrimSpace(res.Stdout); got != "ok" {
		fmt.Printf("    FAIL: stdout = %q, want \"ok\"\n", got)
	} else {
		fmt.Println("    PASS: exit 0, stdout = ok")
		passed++
	}

	// [3] A 1ms request timeout must abort the wait for the start event
	// with a readable error naming the option.
	fmt.Println("\n[3] Start with RequestTimeout=1ms (expect a start-event timeout error)...")
	if stray, err := commands.Start(ctx, sleepCmd, runtime.RunOpts{RequestTimeout: time.Millisecond}); err == nil {
		// Not the expected outcome: drop the stray stream instead of letting
		// it linger until the parent context ends.
		stray.Disconnect()
		fmt.Println("    FAIL: expected a timeout error, got a handle")
	} else if msg := err.Error(); strings.Contains(msg, "waiting for the start event") && strings.Contains(msg, "RunOpts.RequestTimeout") {
		fmt.Printf("    PASS: %v\n", msg)
		passed++
	} else {
		fmt.Printf("    FAIL: unexpected error: %v\n", err)
	}

	// [4] After the request-timeout cut the unary RPC path must stay
	// usable — the timeout cut the stream, not the client — and the SDK
	// must not have issued any kill. Whether the marker process shows up
	// in List is informational: with a 1ms budget the watchdog cancels
	// the request before the gateway can deliver it, so the process
	// usually never started; if the request slipped through anyway, its
	// presence proves the timeout did not kill it.
	fmt.Println("\n[4] List stays usable after the request-timeout cut...")
	listCtx, listCancel := context.WithTimeout(context.WithoutCancel(ctx), 15*time.Second)
	procs, err := commands.List(listCtx)
	listCancel()
	if err != nil {
		fmt.Printf("    FAIL: List: %v\n", err)
	} else if pid, ok := findMarkerPid(procs); ok {
		fmt.Printf("    PASS: List OK; marker process (pid %d) still running — the timeout did not kill it\n", pid)
		passed++
	} else {
		fmt.Println("    PASS: List OK; no marker process (the 1ms request never reached envd)")
		passed++
	}

	// [5] Timeout bounds the whole stream: the start event arrives, then
	// Wait fails at the deadline with an error naming RunOpts.Timeout.
	fmt.Println("\n[5] Start with Timeout=2s + Wait (expect a stream timeout error)...")
	handle, err := commands.Start(ctx, sleepCmd, runtime.RunOpts{Timeout: 2 * time.Second})
	if err != nil {
		fmt.Printf("    FAIL: Start: %v\n", err)
	} else if _, err := handle.Wait(nil, nil); err == nil {
		fmt.Println("    FAIL: expected Wait to fail at the 2s deadline")
		handle.Disconnect()
	} else if msg := err.Error(); strings.Contains(msg, "RunOpts.Timeout") {
		fmt.Printf("    PASS: %v\n", msg)
		passed++
		handle.Disconnect()
	} else {
		fmt.Printf("    FAIL: unexpected error: %v\n", err)
		handle.Disconnect()
	}

	// [6] After the stream-timeout cut an explicit Kill must still reach
	// envd on the dead stream's handle — the kill RPC is an independent
	// unary call. Whether the process is still alive at this point is
	// envd's call (see the header note): killed=true proves it survived
	// the timeout, while killed=false with no error means envd already
	// reaped it after the cut — either way the kill itself keeps working
	// and the SDK issued no implicit kill.
	fmt.Println("\n[6] explicit Kill stays usable after the stream-timeout cut...")
	if handle == nil {
		fmt.Println("    FAIL: no handle left from check [5]")
	} else {
		killCtx, killCancel := context.WithTimeout(context.WithoutCancel(ctx), 15*time.Second)
		killed, err := commands.Kill(killCtx, handle.Pid())
		killCancel()
		switch {
		case err != nil:
			fmt.Printf("    FAIL: Kill: %v\n", err)
		case killed:
			fmt.Printf("    PASS: marker process (pid %d) survived the timeout; explicit kill succeeded\n", handle.Pid())
			passed++
		default:
			fmt.Printf("    PASS: Kill RPC delivered; envd reports pid %d gone (reaped after the stream cut — envd-side behavior; the SDK issued no kill)\n", handle.Pid())
			passed++
		}
	}

	// [7] Disconnect drops the subscription without killing: the process
	// must stay in List afterwards (the Go-idiomatic per-call cancel).
	fmt.Println("\n[7] Start + Disconnect (expect the process to survive)...")
	handle, err = commands.Start(ctx, sleepCmd)
	if err != nil {
		fmt.Printf("    FAIL: Start: %v\n", err)
	} else {
		handle.Disconnect()
		if ok, msg := checkMarkerRunning(ctx, commands); !ok {
			fmt.Printf("    FAIL: %s\n", msg)
		} else {
			fmt.Println("    PASS: marker process still running after Disconnect")
			passed++
		}
	}

	// [8] ConnectOpts.RequestTimeout bounds the wait for the connect start
	// event with per-call attribution — the connect counterpart of [3].
	// A fresh marker process is started first so the connect has a real
	// target; it stays alive for [9] on its open start stream.
	fmt.Println("\n[8] Connect with RequestTimeout=1ms (expect a connect-event timeout error)...")
	var connProc *runtime.CommandHandle
	if cProc, err := commands.Start(ctx, sleepCmd); err != nil {
		fmt.Printf("    FAIL: Start: %v\n", err)
	} else {
		connProc = cProc
		if stray, err := commands.ConnectToProcess(ctx, connProc.Pid(), runtime.ConnectOpts{RequestTimeout: time.Millisecond}); err == nil {
			// Not the expected outcome: drop the stray stream instead of
			// letting it linger until the parent context ends.
			stray.Disconnect()
			fmt.Println("    FAIL: expected a timeout error, got a handle")
		} else if msg := err.Error(); strings.Contains(msg, "waiting for the connect event") && strings.Contains(msg, "ConnectOpts.RequestTimeout") {
			fmt.Printf("    PASS: %v\n", msg)
			passed++
		} else {
			fmt.Printf("    FAIL: unexpected error: %v\n", err)
		}
	}

	// [9] ConnectOpts.Timeout bounds the whole connect stream: the connect
	// start event arrives, then Wait on the connected handle fails at the
	// deadline with ConnectOpts.Timeout attribution — new in this release;
	// previously the connect stream had no overall limit at all.
	fmt.Println("\n[9] Connect with Timeout=2s + Wait (expect a connect stream timeout error)...")
	if connProc == nil {
		fmt.Println("    FAIL: no marker process left from check [8]")
	} else if cHandle, err := commands.ConnectToProcess(ctx, connProc.Pid(), runtime.ConnectOpts{Timeout: 2 * time.Second}); err != nil {
		fmt.Printf("    FAIL: ConnectToProcess: %v\n", err)
	} else {
		_, werr := cHandle.Wait(nil, nil)
		cHandle.Disconnect()
		if werr == nil {
			fmt.Println("    FAIL: expected Wait to fail at the 2s deadline")
		} else if msg := werr.Error(); strings.Contains(msg, "ConnectOpts.Timeout") {
			fmt.Printf("    PASS: %v\n", msg)
			passed++
		} else {
			fmt.Printf("    FAIL: unexpected error: %v\n", werr)
		}
		connProc.Kill()
	}

	// Cleanup: kill every marker process left behind by the checks.
	killMarker(ctx, commands)
	fmt.Println("\nMarker processes cleaned up")

	return passed
}

// findMarkerPid returns the pid of the first process whose args carry the
// marker, if any is visible in the given list.
func findMarkerPid(procs []runtime.ProcessInfo) (uint32, bool) {
	for _, p := range procs {
		for _, arg := range p.Args {
			if strings.Contains(arg, marker) {
				return p.Pid, true
			}
		}
	}
	return 0, false
}

// checkMarkerRunning reports whether at least one marker process is visible
// via List, polling briefly (up to ~5s) because the process list may lag a
// moment behind the cut stream. The list RPC uses a fresh context so a dead
// stream context on the caller side cannot mask the result.
func checkMarkerRunning(ctx context.Context, commands *runtime.Commands) (bool, string) {
	for attempt := 0; attempt < 10; attempt++ {
		if attempt > 0 {
			time.Sleep(500 * time.Millisecond)
		}
		listCtx, cancel := context.WithTimeout(context.WithoutCancel(ctx), 15*time.Second)
		procs, err := commands.List(listCtx)
		cancel()
		if err != nil {
			return false, fmt.Sprintf("List: %v", err)
		}
		for _, p := range procs {
			for _, arg := range p.Args {
				if strings.Contains(arg, marker) {
					return true, ""
				}
			}
		}
	}
	return false, "marker process not found in List after polling for 5s"
}

// killMarker kills every marker process and reports whether at least one
// was killed.
func killMarker(ctx context.Context, commands *runtime.Commands) bool {
	killed := false
	listCtx, cancel := context.WithTimeout(context.WithoutCancel(ctx), 15*time.Second)
	defer cancel()
	procs, err := commands.List(listCtx)
	if err != nil {
		return false
	}
	for _, p := range procs {
		isMarker := false
		for _, arg := range p.Args {
			if strings.Contains(arg, marker) {
				isMarker = true
				break
			}
		}
		if !isMarker {
			continue
		}
		killCtx, killCancel := context.WithTimeout(context.WithoutCancel(ctx), 15*time.Second)
		if ok, err := commands.Kill(killCtx, p.Pid); err == nil && ok {
			killed = true
		}
		killCancel()
	}
	return killed
}
