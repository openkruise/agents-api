package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/openkruise/agents-api/sdk/runtime"
)

const (
	sandboxName = "openclaw-advanced-k8s-sbs-lght9"
	namespace   = "default"
	gatewayUrl  = "127.0.0.1:7788"
)

func main() {
	ctx := context.Background()

	fmt.Println("\n========== runtime direct client example ==========")

	// Build a runtime client directly from the K8s Sandbox CR.
	// NewFromK8s automatically resolves sandboxID and runtimeToken.
	c, err := runtime.NewFromK8s(ctx, namespace, sandboxName,
		runtime.WithDomain(gatewayUrl),
	)
	if err != nil {
		fmt.Printf("    Error creating runtime client: %v\n", err)
		return
	}

	fmt.Printf("Sandbox ID: %s\n", c.SandboxID())
	fmt.Printf("runtime URL: %s\n", c.RuntimeURL())

	// ========== 1. Command Operations Demo ==========
	fmt.Println("\n--- Command Operations Demo ---")
	demonstrateCommandOperations(ctx, c.Commands)

	// ========== 2. Filesystem Operations Demo ==========
	fmt.Println("\n--- Filesystem Operations Demo ---")
	demonstrateFileOperations(ctx, c.Files)

	// ========== 2. Write And Read File ==========
	fmt.Println("\n--- Filesystem Write And Read File Demo ---")
	writeAndReadFile(ctx, c.Files)

	fmt.Println("\n========== done ==========")
}

func writeAndReadFile(ctx context.Context, files *runtime.Filesystem) {
	fmt.Println("\n--- Test: Write File ---")
	testPath := fmt.Sprintf("/tmp/go_sdk_test_%d.txt", time.Now().UnixNano())
	testContent := "Hello from Go SDK! 你好世界! " + time.Now().Format(time.RFC3339)

	fmt.Printf("[Write] Path: %s\n", testPath)
	fmt.Printf("[Write] Content: %s\n", testContent)

	writeInfo, err := files.WriteText(ctx, testPath, testContent)
	if err != nil {
		log.Fatalf("Failed to write file: %v", err)
	}
	fmt.Printf("[Write] Success! WriteInfo: %+v\n", writeInfo)

	// ========== 5. Test file read ==========
	fmt.Println("\n--- Test: Read File ---")
	readContent, err := files.ReadText(ctx, testPath)
	if err != nil {
		log.Fatalf("Failed to read file: %v", err)
	}
	fmt.Printf("[Read] Content: %s\n", readContent)

	// ========== 6. Verify ==========
	fmt.Println("\n--- Verification ---")
	if readContent == testContent {
		fmt.Println("PASS: Written content matches read content!")
	} else {
		fmt.Printf("FAIL: Content mismatch!\n  Expected: %s\n  Got:      %s\n", testContent, readContent)
		os.Exit(1)
	}

	// ========== 7. Cleanup ==========
	fmt.Println("\n--- Cleanup ---")
	if err := files.Remove(ctx, testPath); err != nil {
		fmt.Printf("Warning: Failed to remove test file: %v\n", err)
	} else {
		fmt.Printf("Removed test file: %s\n", testPath)
	}

	// Verify removal
	exists, err := files.Exists(ctx, testPath)
	if err != nil {
		fmt.Printf("Warning: Failed to check existence: %v\n", err)
	} else {
		fmt.Printf("File exists after removal: %v\n", exists)
	}

	fmt.Println("\n========== Test Complete ==========")
}

// demonstrateCommandOperations exercises the Commands API surface.
func demonstrateCommandOperations(ctx context.Context, commands *runtime.Commands) {
	// 1. Run a simple foreground command.
	fmt.Println("\n[1] Running 'pwd'...")
	if result, err := commands.Run(ctx, "pwd"); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Printf("    Exit: %d, Stdout: %s", result.ExitCode, result.Stdout)
	}

	// 2. Run a command with custom envs and cwd.
	fmt.Println("\n[2] Running with env vars and cwd...")
	if result, err := commands.Run(ctx, "echo $TEST_VAR && pwd", runtime.RunOpts{
		Cwd:  "/tmp",
		Envs: map[string]string{"TEST_VAR": "hello-from-go-sdk"},
	}); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Printf("    Exit: %d, Stdout: %s", result.ExitCode, result.Stdout)
	}

	// 3. List current processes.
	fmt.Println("\n[3] Listing current processes...")
	listProcesses(ctx, commands)

	// 4. Start a long-running background process.
	fmt.Println("\n[4] Starting background process: sleep 60...")
	handle, err := commands.Start(ctx, "sleep 60")
	if err != nil {
		fmt.Printf("    Error starting background process: %v\n", err)
		return
	}
	pid := handle.Pid()
	fmt.Printf("    Started background process with PID: %d\n", pid)
	time.Sleep(time.Second)

	// 5. Verify the process appears in the list.
	fmt.Println("\n[5] Listing processes again (should include new one)...")
	listProcesses(ctx, commands)

	// 6. Try sending input (expected to be a no-op for `sleep`).
	fmt.Println("\n[6] Sending stdin to background process...")
	if err := commands.SendStdin(ctx, pid, "sample input\n"); err != nil {
		fmt.Printf("    Send stdin failed (expected for non-interactive process): %v\n", err)
	} else {
		fmt.Printf("    Sent input to PID: %d\n", pid)
	}

	// 7. Kill the background process.
	fmt.Println("\n[7] Killing background process...")
	if killed, err := commands.Kill(ctx, pid); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Printf("    Kill PID %d: %v\n", pid, killed)
	}

	// 8. Verify the process is gone.
	time.Sleep(time.Second)
	fmt.Println("\n[8] Verifying process termination...")
	if running := isProcessRunning(ctx, commands, pid); running {
		fmt.Printf("    Warning: Process %d still running\n", pid)
	} else {
		fmt.Printf("    Verified: Process %d is no longer running\n", pid)
	}
}

// listProcesses prints the currently running processes.
func listProcesses(ctx context.Context, commands *runtime.Commands) {
	processes, err := commands.List(ctx)
	if err != nil {
		fmt.Printf("    Error: %v\n", err)
		return
	}
	fmt.Printf("    Running processes: %d\n", len(processes))
	for _, p := range processes {
		fmt.Printf("      - PID: %d, Cmd: %s, Cwd: %s\n", p.Pid, p.Cmd, p.Cwd)
	}
}

// isProcessRunning checks whether a process with the given PID is in the list.
func isProcessRunning(ctx context.Context, commands *runtime.Commands, pid uint32) bool {
	processes, err := commands.List(ctx)
	if err != nil {
		return false
	}
	for _, p := range processes {
		if p.Pid == pid {
			return true
		}
	}
	return false
}

// demonstrateFileOperations exercises the official envd Filesystem gRPC API
// (Stat / MakeDir / Move / ListDir / Remove). File content read/write is not
// part of the protobuf contract, so it is intentionally not demonstrated here.
func demonstrateFileOperations(ctx context.Context, files *runtime.Filesystem) {
	testDir := fmt.Sprintf("/tmp/test_%d", time.Now().UnixNano())
	subDir := testDir + "/subdir"
	renamedDir := testDir + "/renamed_subdir"

	// 1. Create a test directory.
	fmt.Printf("\n[1] Creating directory: %s\n", testDir)
	if created, err := files.MakeDir(ctx, testDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
		return
	} else {
		fmt.Printf("    Directory created: %v\n", created)
	}

	// 2. Check existence.
	fmt.Printf("\n[2] Checking directory exists: %s\n", testDir)
	if exists, err := files.Exists(ctx, testDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Printf("    Exists: %v\n", exists)
	}

	// 3. Get directory info.
	fmt.Printf("\n[3] Getting info: %s\n", testDir)
	if info, err := files.GetInfo(ctx, testDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Printf("    Name: %s, Type: %s, Size: %d\n", info.Name, info.Type, info.Size)
	}

	// 4. Create a subdirectory and list parent.
	fmt.Printf("\n[4] Creating subdirectory: %s\n", subDir)
	if _, err := files.MakeDir(ctx, subDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
	}
	listEntries(ctx, files, testDir)

	// 5. Rename the subdirectory.
	fmt.Printf("\n[5] Renaming %s -> %s\n", subDir, renamedDir)
	if _, err := files.Rename(ctx, subDir, renamedDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Println("    Rename successful")
	}
	listEntries(ctx, files, testDir)

	// 6. Remove the test directory recursively.
	fmt.Printf("\n[6] Removing directory: %s\n", testDir)
	if err := files.Remove(ctx, testDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Println("    Directory removed")
	}

	// 7. Verify removal.
	fmt.Printf("\n[7] Verifying removal: %s\n", testDir)
	if exists, err := files.Exists(ctx, testDir); err != nil {
		fmt.Printf("    Error: %v\n", err)
	} else {
		fmt.Printf("    Exists after removal: %v\n", exists)
	}
}

// listEntries lists the entries in a directory.
func listEntries(ctx context.Context, files *runtime.Filesystem, path string) {
	entries, err := files.List(ctx, path)
	if err != nil {
		fmt.Printf("    Error listing %s: %v\n", path, err)
		return
	}
	fmt.Printf("    Entries in %s: %d\n", path, len(entries))
	for _, e := range entries {
		fmt.Printf("      - %s %s (size: %d)\n", e.Type, e.Name, e.Size)
	}
}
