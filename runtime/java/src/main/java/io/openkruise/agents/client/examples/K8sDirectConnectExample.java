package io.openkruise.agents.client.examples;

import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.RuntimeClient;
import io.openkruise.agents.client.runtime.codeinterpreter.Execution;
import io.openkruise.agents.client.runtime.commands.CommandResult;
import io.openkruise.agents.client.runtime.filesystem.Filesystem.EntryInfo;

import java.util.List;

public class K8sDirectConnectExample {

    public static void main(String[] args) {
        // Sandbox gateway address
        // In cluster: "sandbox-gateway.sandbox-system.svc:7788"
        // Local development: "127.0.0.1:7788"
        String domain = "sandbox-gateway.sandbox-system.svc:7788";
        String namespace = "default";
        String sandboxName = "your-sandbox-name";

        // Build RuntimeConfig
        RuntimeConfig config = new RuntimeConfig.Builder()
            .domain(domain)
            .scheme("http")
            .build();

        try (RuntimeClient client = RuntimeClient.newFromK8s(namespace, sandboxName, config)) {
            System.out.println("Runtime URL: " + client.getRuntimeURL());
            System.out.println("Sandbox ID: " + client.getSandboxID());

            // Execute command
            CommandResult result = client.commands.run("uname -a");
            System.out.println("uname: " + result.getStdout());

            // List directory
            List<EntryInfo> entries = client.files.listDir("/");
            System.out.println("Root directory file count: " + entries.size());
            for (EntryInfo entry : entries) {
                System.out.println("  " + entry);
            }

            // Create directory
            String testDir = "/tmp/test-dir" + System.nanoTime();
            client.files.makeDir(testDir);

            // Check if file exists
            boolean exists = client.files.exists(testDir);
            System.out.println("exists: " + testDir + " -> " + exists);

            // Execute multiple commands
            CommandResult whoami = client.commands.run("whoami");
            System.out.println("Current user: " + whoami.getStdout().trim());

            CommandResult pwd = client.commands.run("pwd");
            System.out.println("Working directory: " + pwd.getStdout().trim());

            // run code
            String pythonCode = "print('Hello from Python!')\nresult = 2 + 3\nprint(f'Result: {result}')";
            Execution pythonResult = client.codeInterpreter.runCode(pythonCode);

            System.out.printf("Code: %s%n", pythonCode.replace("\n", "\\n"));
            System.out.printf("Output:%n");
            for (String line : pythonResult.getLogs().getStdout()) {
                System.out.printf("  %s%n", line);
            }
            System.out.printf("Execution count: %d%n%n", pythonResult.getExecutionCount());
        } catch (Exception e) {
            System.err.println("K8s direct connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
