package e2b

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/openkruise/agents-api/runtime"
)

func TestGetCodeInterpreterURLNative(t *testing.T) {
	cfg := NewConnectionConfig(
		WithDomain("example.com"),
		WithScheme("https"),
	)

	if got, want := cfg.GetSandboxURL("sbx"), "https://49983-sbx.example.com"; got != want {
		t.Errorf("GetSandboxURL() = %q, want %q", got, want)
	}
	// The code interpreter URL embeds its own port (49999), not the runtime port.
	if got, want := cfg.GetCodeInterpreterURL("sbx"), "https://49999-sbx.example.com"; got != want {
		t.Errorf("GetCodeInterpreterURL() = %q, want %q", got, want)
	}
}

func TestGetCodeInterpreterURLPrivate(t *testing.T) {
	cfg := NewConnectionConfig(
		WithDomain("gw.example.com"),
		WithScheme("https"),
		WithProtocol(ProtocolPrivate),
	)

	if got, want := cfg.GetSandboxURL("sbx"), "https://gw.example.com/kruise/sbx/49983"; got != want {
		t.Errorf("GetSandboxURL() = %q, want %q", got, want)
	}
	if got, want := cfg.GetCodeInterpreterURL("sbx"), "https://gw.example.com/kruise/sbx/49999"; got != want {
		t.Errorf("GetCodeInterpreterURL() = %q, want %q", got, want)
	}
}

func TestGetCodeInterpreterURLCustomPort(t *testing.T) {
	cfg := NewConnectionConfig(
		WithDomain("example.com"),
		WithCodeInterpreterPort(50001),
	)
	if got, want := cfg.GetCodeInterpreterURL("sbx"), "https://50001-sbx.example.com"; got != want {
		t.Errorf("GetCodeInterpreterURL() = %q, want %q", got, want)
	}
}

func TestGetCodeInterpreterURLBaseURLOverride(t *testing.T) {
	cfg := NewConnectionConfig(
		WithDomain("example.com"),
		WithSandboxBaseURL("https://gw.example.com"),
	)
	// With an explicit gateway override, both services share the same URL
	// and the port is routed via the "e2b-sandbox-port" header.
	if got, want := cfg.GetSandboxURL("sbx"), "https://gw.example.com/sbx"; got != want {
		t.Errorf("GetSandboxURL() = %q, want %q", got, want)
	}
	if got, want := cfg.GetCodeInterpreterURL("sbx"), "https://gw.example.com/sbx"; got != want {
		t.Errorf("GetCodeInterpreterURL() = %q, want %q", got, want)
	}
}

func TestCodeInterpreterPortDefaults(t *testing.T) {
	cfg := NewConnectionConfig()
	if cfg.CodeInterpreterPort != 49999 {
		t.Errorf("default CodeInterpreterPort = %d, want 49999", cfg.CodeInterpreterPort)
	}
	// Zero value (struct built without NewConnectionConfig) falls back.
	cfg = &ConnectionConfig{}
	if got := cfg.codeInterpreterPort(); got != 49999 {
		t.Errorf("fallback codeInterpreterPort() = %d, want 49999", got)
	}
}

func TestToEnvdConfigPropagatesCodeInterpreter(t *testing.T) {
	cfg := NewConnectionConfig(
		WithDomain("example.com"),
		WithProtocol(ProtocolNative),
		WithCodeInterpreterPort(50002),
	)

	envd := cfg.toEnvdConfig("sbx")

	if envd.CodeInterpreterPort != 50002 {
		t.Errorf("CodeInterpreterPort = %d, want 50002", envd.CodeInterpreterPort)
	}
	if got, want := envd.CodeInterpreterBaseURL, "https://50002-sbx.example.com"; got != want {
		t.Errorf("CodeInterpreterBaseURL = %q, want %q", got, want)
	}
	// The runtime URL stays on the runtime port.
	if got, want := envd.SandboxBaseURL, "https://49983-sbx.example.com"; got != want {
		t.Errorf("SandboxBaseURL = %q, want %q", got, want)
	}

	// The resolved runtime.Config routes code-interpreter requests to the
	// dedicated URL and sends the port in the header.
	if got, want := envd.CodeInterpreterURL("sbx"), "https://50002-sbx.example.com"; got != want {
		t.Errorf("CodeInterpreterURL() = %q, want %q", got, want)
	}
	if got := envd.CodeInterpreterHeaders("sbx")["e2b-sandbox-port"]; got != "50002" {
		t.Errorf("e2b-sandbox-port = %q, want 50002", got)
	}
	if got := envd.SandboxHeaders("sbx")["e2b-sandbox-port"]; got != "49983" {
		t.Errorf("runtime e2b-sandbox-port = %q, want 49983", got)
	}
}

func TestToEnvdConfigCodeInterpreterEndToEnd(t *testing.T) {
	var gotPath, gotPort, gotSandboxID string
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		gotPort = r.Header.Get("e2b-sandbox-port")
		gotSandboxID = r.Header.Get("e2b-sandbox-id")
		w.Header().Set("Content-Type", "application/json")
		if _, err := w.Write([]byte(`[]`)); err != nil {
			t.Errorf("failed to write response: %v", err)
		}
	}))
	t.Cleanup(server.Close)

	// Full chain: e2b config -> toEnvdConfig -> runtime.NewWithConfig ->
	// CodeInterpreter request must reach the gateway URL (<base>/<sandboxID>,
	// port routed via the header) with the code-interpreter port header.
	cfg := NewConnectionConfig(WithSandboxBaseURL(server.URL))
	client := runtime.NewWithConfig("sbx-e2e", cfg.toEnvdConfig("sbx-e2e"))
	if _, err := client.CodeInterpreter.ListContexts(context.Background()); err != nil {
		t.Fatalf("ListContexts() error = %v", err)
	}
	if gotPath != "/sbx-e2e/contexts" {
		t.Errorf("request path = %q, want /sbx-e2e/contexts", gotPath)
	}
	if gotPort != "49999" {
		t.Errorf("e2b-sandbox-port = %q, want 49999", gotPort)
	}
	if gotSandboxID != "sbx-e2e" {
		t.Errorf("e2b-sandbox-id = %q, want sbx-e2e", gotSandboxID)
	}
}

func TestGetSandboxURLRuntimePortFallback(t *testing.T) {
	// An unset or invalid runtime port falls back to the default instead of
	// producing a bogus "0-<id>.<domain>" URL.
	cfg := &ConnectionConfig{Domain: "example.com", Scheme: "https"}
	if got, want := cfg.GetSandboxURL("sbx"), "https://49983-sbx.example.com"; got != want {
		t.Errorf("GetSandboxURL() = %q, want %q", got, want)
	}
}
