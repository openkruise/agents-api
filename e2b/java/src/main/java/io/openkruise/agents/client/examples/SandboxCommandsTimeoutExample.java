package io.openkruise.agents.client.examples;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.e2b.Sandbox;
import io.openkruise.agents.client.e2b.SandboxApi;
import io.openkruise.agents.client.runtime.commands.CommandExitException;
import io.openkruise.agents.client.runtime.commands.CommandHandle;
import io.openkruise.agents.client.runtime.commands.CommandResult;
import io.openkruise.agents.client.runtime.commands.Commands;
import io.openkruise.agents.client.runtime.commands.Commands.RunOptions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Command timeout / cancel semantics verification for the Java SDKs. One
 * example covers both SDK flavors because the data plane is shared: Sandbox
 * exposes the embedded runtime copy's {@code RuntimeClient} ({@code commands}
 * is its field), so every check below exercises the same RunOptions
 * timeout wiring while the sandbox itself is managed through the e2b
 * control plane.
 *
 * <p>Verified semantics — the checks marked "official" follow the official
 * E2B SDK contract; the rest are local extensions beyond it:
 *
 * <ul>
 *   <li>{@code requestTimeoutMs(0)} is legal and disables the wait for the
 *       start event (the official explicit-disable semantics). (official)</li>
 *   <li>{@code requestTimeoutMs(1)} must abort a slow start with a readable
 *       timeout error naming the option. (official)</li>
 *   <li>{@code timeoutMs(2000)} bounds the whole stream: waitForCompletion
 *       fails at the deadline with an error naming
 *       {@code RunOptions.timeoutMs}. (official)</li>
 *   <li>A non-zero exit fails run()/waitForCompletion() with
 *       {@link CommandExitException} carrying the exit code, stdout/stderr
 *       and the end-event error (when envd delivered one) — the official
 *       throw-on-non-zero contract; previously the caller had to read the
 *       exit code off the returned result. (official)</li>
 *   <li>{@code CommandHandle.disconnect()} stops reading without killing
 *       the process; a blocked waitForCompletion fails with a
 *       disconnected-stream error. (naming aligned with the official
 *       disconnect() operation; local API)</li>
 *   <li>{@code ConnectOptions} applies the same two per-call limits to
 *       {@code connect(int, ConnectOptions)}: {@code requestTimeoutMs}
 *       bounds the wait for the connect start event, {@code timeoutMs}
 *       bounds the whole connect stream (previously it had no overall
 *       limit at all), both with attribution naming
 *       {@code ConnectOptions}. (official)</li>
 * </ul>
 *
 * <p>Environment variables (defaults in parentheses) so a prebuilt jar can
 * be pointed at any environment without recompiling:
 *
 * <ul>
 *   <li>{@code E2B_TEMPLATE} -> template ({@code code-interpreter})
 *   <li>{@code E2B_PROTOCOL} -> protocol ({@code PRIVATE}; {@code NATIVE} for subdomain routing)
 *   <li>{@code E2B_SCHEME}   -> scheme (derived: {@code https} when SSL_CERT_FILE is set, else {@code http})
 *   <li>{@code SSL_CERT_FILE} -> CA bundle trusted for https (PEM)
 * </ul>
 *
 * <p>{@code E2B_API_KEY} and {@code E2B_DOMAIN} are read by
 * {@link ConnectionConfig.Builder} defaults.
 */
public class SandboxCommandsTimeoutExample {
    private static final String DEFAULT_TEMPLATE = "code-interpreter";

    /** Number of checks verify() performs; the result line and the
     *  exit-code decision both key off it. */
    private static final int TOTAL_CHECKS = 9;

    /** Marker embedded in every long-running command so the process can be
     *  found again via list() (and cleaned up) after its stream is cut. */
    private static final String MARKER = "commands-timeout-demo";

    /** Long enough to outlive every check; the checks cut the stream long
     *  before the process would exit on its own. */
    private static final String SLEEP_CMD = "sleep 30 # " + MARKER;

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    private static int passed = 0;

    public static void main(String[] args) {
        String template = envOr("E2B_TEMPLATE", DEFAULT_TEMPLATE);

        // Default to the private path-gateway routing ({domain}/kruise/...)
        // like the other verification demos; set E2B_PROTOCOL=NATIVE for
        // subdomain-based routing.
        String protocol = envOr("E2B_PROTOCOL", "PRIVATE");

        // Derive scheme from SSL_CERT_FILE presence unless set explicitly.
        String scheme = envOr("E2B_SCHEME", "");
        String certFile = System.getenv("SSL_CERT_FILE");
        if (scheme.isEmpty()) {
            scheme = (certFile != null && !certFile.isEmpty()) ? "https" : "http";
        }

        System.out.println("========== Commands Timeout Verification (Java e2b SDK) ==========");
        System.out.printf("Config: template=%s, protocol=%s, scheme=%s%n",
            template, protocol, scheme);

        // Reads E2B_API_KEY and E2B_DOMAIN from environment variables.
        ConnectionConfig.Builder builder = new ConnectionConfig.Builder()
            .protocol(ConnectionConfig.Protocol.valueOf(protocol.toUpperCase()))
            .scheme(scheme);

        // Trust the CA bundle from SSL_CERT_FILE (self-signed environments).
        if (certFile != null && !certFile.isEmpty() && "https".equals(scheme)) {
            try {
                builder.httpClient(trustingClient(certFile));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load SSL_CERT_FILE " + certFile, e);
            }
        }

        ConnectionConfig config = builder.build();
        SandboxApi api = new SandboxApi(config);
        String sandboxId = null;

        try {
            Sandbox sandbox = api.create(template);
            sandboxId = sandbox.getSandboxID();
            System.out.println("Sandbox created: " + sandboxId);

            verify(sandbox);
        } catch (Exception e) {
            System.err.println("Operation error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sandboxId != null) {
                try {
                    api.kill(sandboxId);
                    System.out.println("Sandbox cleaned up: " + sandboxId);
                } catch (Exception ignored) {
                    System.out.println("Sandbox cleanup error: " + sandboxId);
                }
            }
        }

        System.out.printf("%n========== Result: %d/%d checks passed ==========%n", passed, TOTAL_CHECKS);
        if (passed != TOTAL_CHECKS) {
            System.exit(1);
        }
    }

    private static void verify(Sandbox sandbox) {
        Commands commands = sandbox.commands;

        // [1] requestTimeoutMs(0) must be legal and disable the wait for
        // the command to start rather than break the call.
        System.out.println("\n[1] run with requestTimeoutMs(0) (expect success)...");
        try {
            CommandResult result = commands.run("echo ok",
                new RunOptions().requestTimeoutMs(0));
            if (result.getExitCode() == 0 && result.getStdout().trim().equals("ok")) {
                System.out.println("    PASS: exit 0, stdout = ok");
                passed++;
            } else {
                System.out.printf("    FAIL: exit %d, stdout = \"%s\"%n",
                    result.getExitCode(), result.getStdout().trim());
            }
        } catch (Exception e) {
            System.out.println("    FAIL: " + e.getMessage());
        }

        // [2] A 1ms request timeout must abort the wait for the start event
        // with a readable error naming the option.
        System.out.println("\n[2] runBackground with requestTimeoutMs(1) (expect a start-event timeout error)...");
        try {
            CommandHandle stray = commands.runBackground(SLEEP_CMD, new RunOptions().requestTimeoutMs(1));
            // Not the expected outcome: close the stray stream so it does not
            // linger until GC; the marker sweep below is the kill backstop.
            stray.close();
            System.out.println("    FAIL: expected a timeout error, got a handle");
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("waiting for the start event") && msg.contains("requestTimeoutMs")) {
                System.out.println("    PASS: " + msg);
                passed++;
            } else {
                System.out.println("    FAIL: unexpected error: " + msg);
            }
        }

        // [3] After the request-timeout cut the unary RPC path must stay
        // usable — the timeout cut the stream, not the client. Whether the
        // marker process shows up in list() is informational: with a 1ms
        // budget the watchdog cancels the request before the gateway can
        // deliver it, so the process usually never started.
        System.out.println("\n[3] list() stays usable after the request-timeout cut...");
        try {
            if (!listMarkerPids(commands).isEmpty()) {
                System.out.println("    PASS: list() OK; marker process still running — the timeout did not kill it");
            } else {
                System.out.println("    PASS: list() OK; no marker process (the 1ms request never reached envd)");
            }
            passed++;
        } catch (Exception e) {
            System.out.println("    FAIL: list(): " + e.getMessage());
        }

        // [4] timeoutMs bounds the whole stream: the start event arrives,
        // then waitForCompletion fails at the deadline.
        System.out.println("\n[4] runBackground + waitForCompletion with timeoutMs(2000) (expect a stream timeout)...");
        CommandHandle handle = null;
        try {
            handle = commands.runBackground(SLEEP_CMD, new RunOptions().timeoutMs(2000));
            handle.waitForCompletion();
            System.out.println("    FAIL: expected waitForCompletion to fail at the 2000ms deadline");
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("RunOptions.timeoutMs")) {
                System.out.println("    PASS: " + msg);
                passed++;
            } else {
                System.out.println("    FAIL: unexpected error: " + msg);
            }
        } finally {
            if (handle != null) {
                handle.close();
            }
        }

        // [5] disconnect() stops reading without killing: a blocked
        // waitForCompletion fails with a disconnected-stream error.
        System.out.println("\n[5] runBackground + disconnect() (expect a disconnected-stream error from waitForCompletion)...");
        handle = null;
        try {
            handle = commands.runBackground(SLEEP_CMD, new RunOptions());
            handle.disconnect();
            handle.waitForCompletion();
            System.out.println("    FAIL: expected waitForCompletion to fail after disconnect()");
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("disconnected")) {
                System.out.println("    PASS: " + msg);
                passed++;
            } else {
                System.out.println("    FAIL: unexpected error: " + msg);
            }
        } finally {
            if (handle != null) {
                handle.close();
            }
        }

        // [6] The disconnected process must keep running in the sandbox as well.
        System.out.println("\n[6] process survives disconnect() (expect it in list())...");
        checkMarkerRunning(commands, "[6]");

        // [7] ConnectOptions.requestTimeoutMs(1) bounds the wait for the
        // connect start event with per-call attribution — the connect
        // counterpart of [2]. A fresh marker process is started first so
        // the connect has a real target; it stays alive for [8] on its
        // open start stream.
        System.out.println("\n[7] connect with ConnectOptions.requestTimeoutMs(1) (expect a connect-event timeout error)...");
        CommandHandle connProc = null;
        try {
            connProc = commands.runBackground(SLEEP_CMD, new RunOptions());
            CommandHandle conn = commands.connect((int) connProc.getPid(),
                new Commands.ConnectOptions().requestTimeoutMs(1));
            // Not the expected outcome: close the stray stream so it does not
            // linger until GC; the marker sweep below is the kill backstop.
            conn.close();
            System.out.println("    FAIL: expected a timeout error, got a handle");
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("waiting for the connect event") && msg.contains("ConnectOptions.requestTimeoutMs")) {
                System.out.println("    PASS: " + msg);
                passed++;
            } else {
                System.out.println("    FAIL: unexpected error: " + msg);
            }
        }

        // [8] ConnectOptions.timeoutMs bounds the whole connect stream: the
        // connect start event arrives, then waitForCompletion on the
        // connected handle fails at the deadline with ConnectOptions
        // attribution — new in this release; previously the connect stream
        // had no overall limit at all.
        System.out.println("\n[8] connect with ConnectOptions.timeoutMs(2000) + waitForCompletion (expect a connect stream timeout)...");
        if (connProc == null) {
            System.out.println("    FAIL: no marker process left from check [7]");
        } else {
            CommandHandle cHandle = null;
            try {
                cHandle = commands.connect((int) connProc.getPid(),
                    new Commands.ConnectOptions().timeoutMs(2000));
                cHandle.waitForCompletion();
                System.out.println("    FAIL: expected waitForCompletion to fail at the 2000ms deadline");
            } catch (Exception e) {
                String msg = String.valueOf(e.getMessage());
                if (msg.contains("ConnectOptions.timeoutMs")) {
                    System.out.println("    PASS: " + msg);
                    passed++;
                } else {
                    System.out.println("    FAIL: unexpected error: " + msg);
                }
            } finally {
                if (cHandle != null) {
                    cHandle.close();
                }
                try {
                    connProc.kill();
                } catch (Exception ignored) {
                    // Cleanup only; the marker sweep below is the backstop.
                }
                // kill() does not close the handle's start stream — release
                // the connection now instead of leaving it to GC.
                connProc.close();
            }
        }

        // [9] A non-zero exit must surface as a CommandExitException
        // carrying the exit code, stdout/stderr and the end-event error
        // (when envd delivered one) — the official throw-on-non-zero
        // contract; previously waitForCompletion returned the result and
        // the caller had to read the code off it.
        System.out.println("\n[9] run with exit 3 (expect CommandExitException with exitCode/stderr)...");
        try {
            commands.run("echo boom >&2; exit 3");
            System.out.println("    FAIL: expected CommandExitException, got a normal return");
        } catch (CommandExitException e) {
            if (e.getExitCode() == 3 && e.getStderr().contains("boom")) {
                System.out.println("    PASS: exitCode=" + e.getExitCode()
                    + ", stderr contains 'boom', end-event error='" + e.getError() + "'");
                passed++;
            } else {
                System.out.println("    FAIL: exitCode=" + e.getExitCode()
                    + ", stderr=\"" + e.getStderr().trim() + "\"");
            }
        } catch (Exception e) {
            System.out.println("    FAIL: unexpected exception: " + e.getMessage());
        }

        // Cleanup: kill every marker process left behind by the checks.
        try {
            for (Commands.ProcessInfo p : commands.list()) {
                if (hasMarker(p)) {
                    commands.kill((int) p.getPid());
                }
            }
            System.out.println("\nMarker processes cleaned up");
        } catch (Exception e) {
            System.out.println("Marker cleanup warning: " + e.getMessage());
        }
    }

    /** Shared assertion for the "process still running" checks. Polls
     *  list() for a few seconds because the process list may lag a moment
     *  behind the cut stream. */
    private static void checkMarkerRunning(Commands commands, String label) {
        for (int attempt = 0; attempt < 10; attempt++) {
            if (attempt > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                if (!listMarkerPids(commands).isEmpty()) {
                    System.out.println("    PASS: marker process still running");
                    passed++;
                    return;
                }
            } catch (Exception e) {
                System.out.println("    " + label + " FAIL: " + e.getMessage());
                return;
            }
        }
        System.out.println("    FAIL: marker process not found in list() after polling for 5s");
    }

    private static List<Long> listMarkerPids(Commands commands) throws Exception {
        java.util.ArrayList<Long> pids = new java.util.ArrayList<>();
        for (Commands.ProcessInfo p : commands.list()) {
            if (hasMarker(p)) {
                pids.add(p.getPid());
            }
        }
        return pids;
    }

    /** Matches our sleep command: bash -l -c "sleep 30 # commands-timeout-demo". */
    private static boolean hasMarker(Commands.ProcessInfo p) {
        if (p.getArgs() == null) {
            return false;
        }
        for (String arg : p.getArgs()) {
            if (arg.contains(MARKER)) {
                return true;
            }
        }
        return false;
    }

    /** Builds an OkHttpClient that trusts only the PEM CA bundle at the
     *  given path, replacing the platform default trust store
     *  (self-signed environments). */
    private static OkHttpClient trustingClient(String certFile) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        try (InputStream in = new FileInputStream(certFile)) {
            int i = 0;
            for (Certificate cert : cf.generateCertificates(in)) {
                keyStore.setCertificateEntry("ca-" + i++, cert);
            }
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        return new OkHttpClient.Builder()
            .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
            .build();
    }
}
