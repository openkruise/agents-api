package io.openkruise.agents.client.examples;

import java.util.List;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.e2b.Sandbox;
import io.openkruise.agents.client.e2b.SandboxApi;
import io.openkruise.agents.client.e2b.SandboxInfo;
import io.openkruise.agents.client.e2b.api.invoker.ApiException;

/**
 * SandboxApi usage example
 * <p>
 * Demonstrates complete sandbox lifecycle management via SandboxApi,
 * including: create, list, pause, get info, reconnect, set timeout, destroy.
 */
public class SandboxApiManagerExample {
    private static final String TEMPLATE = "code-interpreter";

    public static void main(String[] args) throws ApiException {
        // 1. Build connection configuration
        // Reads E2B_API_KEY and E2B_DOMAIN from environment variables as defaults
        ConnectionConfig config = new ConnectionConfig.Builder().build();

        SandboxApi api = new SandboxApi(config);
        String sandboxId = null;

        try {
            // 2. Create sandbox
            Sandbox sandbox = api.create(TEMPLATE);
            sandboxId = sandbox.getSandboxID();
            System.out.println("Sandbox created successfully, sandboxId: " + sandboxId);

            // 3. List sandboxes
            List<SandboxInfo> sandboxList = api.list();
            System.out.println("Current sandbox count: " + sandboxList.size());

            // 4. Get sandbox details
            SandboxInfo info = api.getInfo(sandboxId);
            System.out.println("Sandbox status: " + info);

            // 5. Set timeout (in seconds)
            api.setTimeout(sandboxId, 600);
            System.out.println("Timeout set to 600 seconds");

            // 6. Pause sandbox
            api.pause(sandboxId);
            System.out.println("Sandbox paused");

            // 7. Reconnect sandbox (resume from pause)
            api.connect(sandboxId);
            System.out.println("Sandbox reconnected");

            // 8. Destroy sandbox
            api.kill(sandboxId);
            sandboxId = null;
            System.out.println("Sandbox destroyed");

        } catch (Exception e) {
            System.err.println("Operation error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure sandbox resources are released
            if (sandboxId != null) {
                try {
                    api.kill(sandboxId);
                    System.out.println("Sandbox cleaned up: " + sandboxId);
                } catch (Exception ignored) {
                    System.out.println("Sandbox cleanup error: " + sandboxId);
                }
            }
        }
    }
}
