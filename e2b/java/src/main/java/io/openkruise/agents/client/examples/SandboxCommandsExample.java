package io.openkruise.agents.client.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.e2b.Sandbox;
import io.openkruise.agents.client.e2b.SandboxApi;
import io.openkruise.agents.client.runtime.commands.CommandHandle;
import io.openkruise.agents.client.runtime.commands.CommandResult;
import io.openkruise.agents.client.runtime.commands.Commands;
import io.openkruise.agents.client.runtime.commands.Commands.ProcessInfo;

/**
 * E2B mode Commands complete usage example
 */
public class SandboxCommandsExample {
    private static final String TEMPLATE = "desktop";

    public static void main(String[] args) {
        System.out.println("========== E2B Sandbox Commands Java SDK Example ==========");

        // ========== 1. Connection configuration ==========
        // Reads E2B_API_KEY and E2B_DOMAIN from environment variables as defaults
        ConnectionConfig config = new ConnectionConfig.Builder().build();

        SandboxApi api = new SandboxApi(config);
        String sandboxId = null;

        try {
            Sandbox sandbox = api.create(TEMPLATE);
            sandboxId = sandbox.getSandboxID();
            System.out.println("Sandbox created successfully, sandboxId: " + sandboxId);

            System.out.printf("Created successfully, sandboxId: %s%n", sandbox.getSandboxID());
            System.out.printf("Sandbox URL: %s%n", sandbox.getSandboxURL());

            // ========== 4. Command operations demo ==========
            System.out.println("\n--- Command Operations Demo ---");
            demonstrateCommandOperations(sandbox.commands);

            System.out.println("\n========== Example Completed ==========");
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

    // ======================== Command operations demo ========================

    private static void demonstrateCommandOperations(Commands commands) {
        // [1] Execute simple command
        System.out.println("\n[1] Execute 'pwd'...");
        try {
            CommandResult result = commands.run("pwd");
            System.out.printf("    Exit: %d, Stdout: %s", result.getExitCode(), result.getStdout());
        } catch (Exception e) {
            System.err.println("    Error: " + e.getMessage());
        }

        // [2] Execute command with environment variables and working directory
        System.out.println("\n[2] Execute command with env vars and cwd...");
        try {
            Map<String, String> envs = new HashMap<>();
            envs.put("TEST_VAR", "hello-from-java-sdk");

            CommandResult result = commands.run("echo $TEST_VAR && pwd",
                new Commands.RunOptions().envs(envs).cwd("/tmp"));
            System.out.printf("    Exit: %d, Stdout: %s", result.getExitCode(), result.getStdout());
        } catch (Exception e) {
            System.err.println("    Error: " + e.getMessage());
        }

        // [3] List current processes
        System.out.println("\n[3] List current processes...");
        listProcesses(commands);

        // [4] Start background process
        System.out.println("\n[4] Start background process: sleep 60...");
        CommandHandle handle = null;
        long pid = 0;
        try {
            handle = commands.runBackground("sleep 60", new Commands.RunOptions());
            pid = handle.getPid();
            System.out.printf("    Background process started, PID: %d%n", pid);
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("    Failed to start background process: " + e.getMessage());
            return;
        }

        // [5] Verify process appears in list
        System.out.println("\n[5] List processes again (should include new process)...");
        listProcesses(commands);

        // [6] Send stdin
        System.out.println("\n[6] Send stdin to background process...");
        try {
            commands.sendInput((int) pid, "sample input\n");
            System.out.printf("    Sent input to PID %d%n", pid);
        } catch (Exception e) {
            System.out.printf("    Failed to send stdin (expected for non-interactive process): %s%n", e.getMessage());
        }

        // [7] Kill background process
        System.out.println("\n[7] Kill background process...");
        try {
            boolean killed = commands.kill((int) pid);
            System.out.printf("    Kill PID %d: %s%n", pid, killed);
        } catch (Exception e) {
            System.err.println("    Kill failed: " + e.getMessage());
        }

        // [8] Verify process terminated
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        System.out.println("\n[8] Verify process terminated...");
        if (isProcessRunning(commands, pid)) {
            System.out.printf("    Warning: Process %d is still running%n", pid);
        } else {
            System.out.printf("    Confirmed: Process %d has terminated%n", pid);
        }

        // 关闭 handle
        if (handle != null) {
            handle.close();
        }
    }

    private static void listProcesses(Commands commands) {
        try {
            List<ProcessInfo> processes = commands.list();
            System.out.printf("    Running processes: %d%n", processes.size());
            for (ProcessInfo p : processes) {
                System.out.printf("      - PID: %d, Cmd: %s, Cwd: %s%n",
                    p.getPid(), p.getCmd(), p.getCwd());
            }
        } catch (Exception e) {
            System.err.println("    Failed to list processes: " + e.getMessage());
        }
    }

    private static boolean isProcessRunning(Commands commands, long pid) {
        try {
            List<ProcessInfo> processes = commands.list();
            for (ProcessInfo p : processes) {
                if (p.getPid() == pid) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
