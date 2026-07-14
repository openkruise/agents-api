package io.openkruise.agents.client.examples;

import java.util.List;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.e2b.Sandbox;
import io.openkruise.agents.client.e2b.SandboxApi;
import io.openkruise.agents.client.runtime.filesystem.Filesystem;
import io.openkruise.agents.client.runtime.filesystem.Filesystem.EntryInfo;
import io.openkruise.agents.client.runtime.filesystem.Filesystem.WriteInfo;

/**
 * E2B mode Files complete usage example
 */
public class SandboxFilesExample {
    private static final String TEMPLATE = "code-interpreter";

    public static void main(String[] args) {
        System.out.println("========== E2B Sandbox Files Java SDK Example ==========");

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

            // ========== 4. File operations demo ==========
            System.out.println("\n--- File Operations Demo ---");
            demonstrateFileOperations(sandbox.files);

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

    // ======================== File operations demo ========================

    private static void demonstrateFileOperations(Filesystem files) {
        String testDir = "/tmp/test_" + System.nanoTime();
        String subDir = testDir + "/subdir";
        String renamedDir = testDir + "/renamed_subdir";

        // [1] Create directory
        System.out.printf("%n[1] Create directory: %s%n", testDir);
        try {
            boolean created = files.makeDir(testDir);
            System.out.printf("    Directory created: %s%n", created);
        } catch (Exception e) {
            System.err.println("    Creation failed: " + e.getMessage());
            return;
        }

        // [2] Check if exists
        System.out.printf("%n[2] Check if directory exists: %s%n", testDir);
        try {
            boolean exists = files.exists(testDir);
            System.out.printf("    Exists: %s%n", exists);
        } catch (Exception e) {
            System.err.println("    Check failed: " + e.getMessage());
        }

        // [3] Get directory info
        System.out.printf("%n[3] Get directory info: %s%n", testDir);
        try {
            EntryInfo info = files.getInfo(testDir);
            System.out.printf("    Name: %s, Type: %s, Size: %d%n", info.getName(), info.getType(), info.getSize());
        } catch (Exception e) {
            System.err.println("    Failed to get info: " + e.getMessage());
        }

        // [4] Create subdirectory and list parent directory
        System.out.printf("%n[4] Create subdirectory: %s%n", subDir);
        try {
            files.makeDir(subDir);
            listEntries(files, testDir);
        } catch (Exception e) {
            System.err.println("    Operation failed: " + e.getMessage());
        }

        // [5] Rename subdirectory
        System.out.printf("%n[5] Rename %s -> %s%n", subDir, renamedDir);
        try {
            files.move(subDir, renamedDir);
            System.out.println("    Renamed successfully");
            listEntries(files, testDir);
        } catch (Exception e) {
            System.err.println("    Rename failed: " + e.getMessage());
        }

        // [6] Write text file
        String testFile = testDir + "/hello.txt";
        System.out.printf("%n[6] Write file: %s%n", testFile);
        try {
            WriteInfo writeInfo = files.writeText(testFile, "Hello from Java SDK!\n你好，世界！\n");
            System.out.printf("    Write succeeded, path: %s, type: %s%n", writeInfo.getPath(), writeInfo.getType());
        } catch (Exception e) {
            System.err.println("    Write failed: " + e.getMessage());
        }

        // [7] Read text file
        System.out.printf("%n[7] Read file: %s%n", testFile);
        try {
            String content = files.readText(testFile);
            System.out.printf("    File content:%n%s", content);
        } catch (Exception e) {
            System.err.println("    Read failed: " + e.getMessage());
        }

        // [8] Write binary file
        String binFile = testDir + "/data.bin";
        System.out.printf("%n[8] Write binary file: %s%n", binFile);
        try {
            byte[] binaryData = new byte[]{0x00, 0x01, 0x02, 0x03, (byte) 0xFF};
            WriteInfo writeInfo = files.write(binFile, binaryData);
            System.out.printf("    Write succeeded, path: %s%n", writeInfo.getPath());
        } catch (Exception e) {
            System.err.println("    Write failed: " + e.getMessage());
        }

        // [9] Read binary file and verify
        System.out.printf("%n[9] Read binary file: %s%n", binFile);
        try {
            byte[] readBack = files.read(binFile);
            System.out.printf("    Read %d bytes, first byte: 0x%02X, last byte: 0x%02X%n",
                readBack.length, readBack[0], readBack[readBack.length - 1]);
        } catch (Exception e) {
            System.err.println("    Read failed: " + e.getMessage());
        }

        // [10] Overwrite existing file
        System.out.printf("%n[10] Overwrite: %s%n", testFile);
        try {
            files.writeText(testFile, "Overwritten content\n");
            String newContent = files.readText(testFile);
            System.out.printf("    Content after overwrite: %s", newContent);
        } catch (Exception e) {
            System.err.println("    Overwrite failed: " + e.getMessage());
        }

        // [11] List directory (verify files created)
        System.out.printf("%n[11] List directory contents: %s%n", testDir);
        listEntries(files, testDir);

        // [12] Delete directory
        System.out.printf("%n[12] Delete directory: %s%n", testDir);
        try {
            files.remove(testDir);
            System.out.println("    Directory deleted");
        } catch (Exception e) {
            System.err.println("    Delete failed: " + e.getMessage());
        }

        // [13] Verify deletion
        System.out.printf("%n[13] Verify deletion: %s%n", testDir);
        try {
            boolean exists = files.exists(testDir);
            System.out.printf("    Exists after deletion: %s%n", exists);
        } catch (Exception e) {
            System.err.println("    Verification failed: " + e.getMessage());
        }
    }

    private static void listEntries(Filesystem files, String path) {
        try {
            List<EntryInfo> entries = files.listDir(path);
            System.out.printf("    Entries in %s: %d%n", path, entries.size());
            for (EntryInfo e : entries) {
                System.out.printf("      - %s %s (size: %d)%n", e.getType(), e.getName(), e.getSize());
            }
        } catch (Exception e) {
            System.err.println("    Failed to list directory: " + e.getMessage());
        }
    }
}
