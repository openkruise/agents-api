package io.openkruise.agents.client.examples;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.e2b.Sandbox;
import io.openkruise.agents.client.e2b.SandboxApi;
import io.openkruise.agents.client.runtime.codeinterpreter.CodeInterpreter;
import io.openkruise.agents.client.runtime.codeinterpreter.Context;
import io.openkruise.agents.client.runtime.codeinterpreter.Execution;
import io.openkruise.agents.client.runtime.codeinterpreter.RunCodeLanguage;
import io.openkruise.agents.client.runtime.codeinterpreter.RunCodeOptions;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * E2B Sandbox Code Interpreter Example
 * Demonstrates basic code execution with Python and JavaScript.
 */
public class SandboxCodeInterpreterExample {
    private static final String TEMPLATE = "code-interpreter";

    public static void main(String[] args) {
        System.out.println("========== E2B Sandbox Code Interpreter Example ==========\n");

        OkHttpClient customClient=new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120,TimeUnit.SECONDS)
            .build();

        // Reads E2B_API_KEY and E2B_DOMAIN from environment variables as defaults
        ConnectionConfig config = new ConnectionConfig.Builder().httpClient(customClient).build();

        SandboxApi api = new SandboxApi(config);
        String sandboxId = null;

        try {
            Sandbox sandbox = api.create(TEMPLATE);
            sandboxId = sandbox.getSandboxID();
            System.out.printf("✓ Sandbox created: %s%n%n", sandboxId);

            CodeInterpreter interpreter = sandbox.codeInterpreter;

            // Python example
            System.out.println("--- Python Execution ---");
            String pythonCode = "print('Hello from Python!')\nresult = 2 + 3\nprint(f'Result: {result}')";
            Execution pythonResult = interpreter.runCode(pythonCode);

            System.out.printf("Code: %s%n", pythonCode.replace("\n", "\\n"));
            System.out.printf("Output:%n");
            for (String line : pythonResult.getLogs().getStdout()) {
                System.out.printf("  %s%n", line);
            }
            System.out.printf("Execution count: %d%n%n", pythonResult.getExecutionCount());

            // JavaScript example
            System.out.println("--- JavaScript Execution ---");
            String jsCode
                = "console.log('Hello from JavaScript!');\nconst result = 5 * 7;\nconsole.log(`Result: ${result}`);";
            Execution jsResult = interpreter.runCode(jsCode, RunCodeLanguage.JAVASCRIPT.getValue());

            System.out.printf("Code: %s%n", jsCode.replace("\n", "\\n"));
            System.out.printf("Output:%n");
            for (String line : jsResult.getLogs().getStdout()) {
                System.out.printf("  %s%n", line);
            }
            System.out.printf("Execution count: %d%n", jsResult.getExecutionCount());

            // Python with options example
            System.out.println("\n--- Python with Options ---");
            Map<String, String> envVars = new HashMap<>();
            envVars.put("MY_VAR", "Hello from env!");
            
            RunCodeOptions opts = new RunCodeOptions()
                .setEnvVars(envVars)
                .setTimeoutMs(10000L)
                .setOnStdout(event -> System.out.print("[REALTIME] " + event.getText()))
                .setOnStderr(event -> System.err.print("[ERROR] " + event.getText()));
            
            String codeWithEnv = "import os\nprint('ENV:', os.environ.get('MY_VAR'))";
            Execution envResult = interpreter.runCode(codeWithEnv, RunCodeLanguage.PYTHON.getValue(), opts);
            
            System.out.printf("%nFinal execution count: %d%n", envResult.getExecutionCount());

            // Context API example
            System.out.println("\n--- Context API Example ---");
            Context ctx = interpreter.createCodeContext("/tmp", RunCodeLanguage.PYTHON.getValue());
            System.out.printf("✓ Context created: %s%n", ctx);
            
            // List all contexts
            List<Context> contexts = interpreter.listCodeContexts();
            System.out.printf("✓ Active contexts: %d%n", contexts.size());
            for (Context c : contexts) {
                System.out.printf("  - %s%n", c);
            }
            
            RunCodeOptions ctxOpts = new RunCodeOptions()
                .setContextId(ctx.getId())
                .setOnStdout(event -> System.out.print("[CTX] " + event.getText()));
            
            String ctxCode = "import os\nprint('CWD:', os.getcwd())";
            interpreter.runCode(ctxCode, RunCodeLanguage.PYTHON.getValue(), ctxOpts);
            
            interpreter.removeCodeContext(ctx.getId());
            System.out.printf("✓ Context removed: %s%n", ctx.getId());

            System.out.println("\n========== Example Completed Successfully ==========");

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sandboxId != null) {
                try {
                    api.kill(sandboxId);
                    System.out.println("✓ Sandbox cleaned up: " + sandboxId);
                } catch (Exception e) {
                    System.err.println("✗ Cleanup failed: " + e.getMessage());
                }
            }
        }
    }
}
