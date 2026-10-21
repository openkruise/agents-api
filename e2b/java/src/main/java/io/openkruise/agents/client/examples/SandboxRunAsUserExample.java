package io.openkruise.agents.client.examples;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.e2b.Sandbox;
import io.openkruise.agents.client.e2b.SandboxApi;
import io.openkruise.agents.client.runtime.commands.CommandResult;
import io.openkruise.agents.client.runtime.commands.Commands;
import io.openkruise.agents.client.runtime.commands.Commands.RunOptions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Command run-as-user verification for the Java SDKs. One example covers
 * both SDK flavors because the data plane is shared: Sandbox exposes the
 * embedded runtime copy's {@code RuntimeClient} ({@code commands} is its
 * field), so every check below exercises the same RunOptions.user(String)
 * implementation while the sandbox itself is managed through the e2b
 * control plane.
 *
 * <p>Environment variables (defaults in parentheses) so a prebuilt jar can
 * be pointed at any environment without recompiling:
 *
 * <ul>
 *   <li>{@code E2B_TEMPLATE} -> template ({@code code-interpreter})
 *   <li>{@code RUN_AS_USER}  -> runAsUser ({@code nobody}, must exist in the image)
 *   <li>{@code E2B_PROTOCOL} -> protocol ({@code PRIVATE}; {@code NATIVE} for subdomain routing)
 *   <li>{@code E2B_SCHEME}   -> scheme (derived: {@code https} when SSL_CERT_FILE is set, else {@code http})
 *   <li>{@code SSL_CERT_FILE} -> CA bundle trusted for https (PEM)
 * </ul>
 *
 * <p>{@code E2B_API_KEY} and {@code E2B_DOMAIN} are read by
 * {@link ConnectionConfig.Builder} defaults.
 */
public class SandboxRunAsUserExample {
    private static final String DEFAULT_TEMPLATE = "code-interpreter";
    private static final String DEFAULT_RUN_AS_USER = "nobody";

    /** Expected to be absent from every image; the negative check relies on
     *  envd rejecting an unknown username. */
    private static final String NO_SUCH_USER = "no-such-user-run-as-user-demo";

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    private static int passed = 0;

    public static void main(String[] args) {
        String template = envOr("E2B_TEMPLATE", DEFAULT_TEMPLATE);
        String runAsUser = envOr("RUN_AS_USER", DEFAULT_RUN_AS_USER);

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

        System.out.println("========== Run-as-User Verification (Java e2b SDK) ==========");
        System.out.printf("Config: template=%s, user=%s, protocol=%s, scheme=%s%n",
            template, runAsUser, protocol, scheme);

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

            verify(sandbox, runAsUser);
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

        System.out.printf("%n========== Result: %d/5 checks passed ==========%n", passed);
        if (passed != 5) {
            System.exit(1);
        }
    }

    private static void verify(Sandbox sandbox, String runAsUser) {
        Commands commands = sandbox.commands;
        // [1] Baseline: without user, commands still run as the default
        // user (root), proving the new option is opt-in only.
        System.out.println("\n[1] e2b SDK: 'whoami' without user (expect root)...");
        try {
            CommandResult result = commands.run("whoami");
            checkEquals("whoami", result.getStdout().trim(), "root");
        } catch (Exception e) {
            System.out.println("    FAIL: " + e.getMessage());
        }

        // [2] 'whoami' with user must report the requested user. An explicit
        // cwd is required because envd falls back to the user's home
        // directory when cwd is empty, and some users (e.g. Debian's nobody
        // -> /nonexistent) have no existing home.
        System.out.printf("%n[2] e2b SDK: 'whoami' as %s (expect %s)...%n", runAsUser, runAsUser);
        try {
            CommandResult result = commands.run("whoami", new RunOptions().user(runAsUser).cwd("/tmp"));
            checkEquals("whoami", result.getStdout().trim(), runAsUser);
        } catch (Exception e) {
            System.out.println("    FAIL: " + e.getMessage());
        }

        // [3] A file created by the user must be owned by that user,
        // proving the process really runs under the user's uid (not just a
        // changed $USER env).
        String marker = "/tmp/run_as_user_demo_" + System.nanoTime();
        System.out.printf("%n[3] e2b SDK: file created by %s is owned by %s...%n", runAsUser, runAsUser);
        String owner = "";
        try {
            commands.run("touch " + marker, new RunOptions().user(runAsUser).cwd("/tmp"));
            CommandResult result = commands.run("stat -c %U " + marker);
            owner = result.getStdout().trim();
        } catch (Exception e) {
            System.out.println("    FAIL: " + e.getMessage());
        }
        try {
            commands.run("rm -f " + marker);
        } catch (Exception ignored) {
        }
        checkEquals("owner", owner, runAsUser);

        // [4] Negative: an unknown user must be rejected by envd (the
        // username is looked up server-side), not silently ignored.
        System.out.printf("%n[4] e2b SDK: 'true' as unknown user %s (expect an error)...%n", NO_SUCH_USER);
        try {
            CommandResult result = commands.run("true", new RunOptions().user(NO_SUCH_USER));
            System.out.println("    FAIL: expected an error, got exit code " + result.getExitCode());
        } catch (Exception e) {
            System.out.println("    PASS: rejected as expected: " + e.getMessage());
            passed++;
        }

        // Phase 2 — runtime SDK surface. getRuntimeClient() returns the
        // embedded RuntimeClient; using it directly is the plain runtime
        // SDK usage. Re-check the core assertion through this surface for
        // good measure.
        System.out.printf("%n[5] runtime SDK (getRuntimeClient): 'whoami' as %s (expect %s)...%n",
            runAsUser, runAsUser);
        try {
            CommandResult result = sandbox.getRuntimeClient().commands
                .run("whoami", new RunOptions().user(runAsUser).cwd("/tmp"));
            checkEquals("whoami", result.getStdout().trim(), runAsUser);
        } catch (Exception e) {
            System.out.println("    FAIL: " + e.getMessage());
        }
    }

    /** Builds an OkHttpClient that trusts the PEM CA bundle at the given
     *  path on top of the default trust store (self-signed environments). */
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

    private static void checkEquals(String what, String got, String want) {
        if (want.equals(got)) {
            System.out.printf("    PASS: %s = %s%n", what, got);
            passed++;
        } else {
            System.out.printf("    FAIL: %s = \"%s\", want \"%s\"%n", what, got, want);
        }
    }
}
