package runtime

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"
)

func TestCodeInterpreterURLAndHeaders(t *testing.T) {
	cfg := NewConfig(WithDomain("gw.example.com:7788"))

	if got, want := cfg.CodeInterpreterURL("sbx"), "http://gw.example.com:7788"; got != want {
		t.Errorf("CodeInterpreterURL() = %q, want %q", got, want)
	}

	headers := cfg.CodeInterpreterHeaders("sbx")
	if got := headers["e2b-sandbox-port"]; got != "49999" {
		t.Errorf("e2b-sandbox-port = %q, want 49999", got)
	}
	if got := headers["e2b-sandbox-id"]; got != "sbx" {
		t.Errorf("e2b-sandbox-id = %q, want %q", got, "sbx")
	}
	if _, ok := headers["Authorization"]; !ok {
		t.Error("Authorization header missing")
	}
}

func TestCodeInterpreterHeadersCustomPort(t *testing.T) {
	cfg := NewConfig(WithCodeInterpreterPort(50001))
	if got := cfg.CodeInterpreterHeaders("sbx")["e2b-sandbox-port"]; got != "50001" {
		t.Errorf("custom port = %q, want 50001", got)
	}
	// Sandbox headers stay on the runtime port.
	if got := cfg.SandboxHeaders("sbx")["e2b-sandbox-port"]; got != "49983" {
		t.Errorf("runtime port = %q, want 49983", got)
	}
}

func TestCodeInterpreterHeadersInvalidPortFallback(t *testing.T) {
	cfg := NewConfig()
	cfg.CodeInterpreterPort = -1
	if got := cfg.CodeInterpreterHeaders("sbx")["e2b-sandbox-port"]; got != "49999" {
		t.Errorf("fallback port = %q, want 49999", got)
	}
}

func TestWithConfigCopiesCodeInterpreterPort(t *testing.T) {
	src := NewConfig(WithCodeInterpreterPort(50010))
	dst := NewConfig(WithConfig(src))
	if dst.CodeInterpreterPort != 50010 {
		t.Errorf("CodeInterpreterPort = %d, want 50010", dst.CodeInterpreterPort)
	}
	if dst.RuntimePort != 49983 {
		t.Errorf("RuntimePort = %d, want 49983", dst.RuntimePort)
	}
}

func TestCodeInterpreterURLBaseURLOverride(t *testing.T) {
	// Default: code interpreter shares the runtime (gateway) URL.
	cfg := NewConfig(WithDomain("gw.example.com:7788"))
	if got, want := cfg.CodeInterpreterURL("sbx"), "http://gw.example.com:7788"; got != want {
		t.Errorf("default CodeInterpreterURL() = %q, want %q", got, want)
	}

	// Override: dedicated code-interpreter base URL wins (e.g. E2B NATIVE
	// protocol embeds the port in the URL).
	cfg = NewConfig(
		WithDomain("gw.example.com:7788"),
		WithCodeInterpreterBaseURL("https://49999-sbx.example.com"),
	)
	if got, want := cfg.CodeInterpreterURL("sbx"), "https://49999-sbx.example.com"; got != want {
		t.Errorf("overridden CodeInterpreterURL() = %q, want %q", got, want)
	}
	// The runtime URL is unaffected by the code-interpreter override.
	if got, want := cfg.SandboxURL("sbx"), "http://gw.example.com:7788"; got != want {
		t.Errorf("SandboxURL() = %q, want %q", got, want)
	}
}

func TestWithConfigCopiesCodeInterpreterBaseURL(t *testing.T) {
	src := NewConfig(WithCodeInterpreterBaseURL("https://49999-sbx.example.com"))
	dst := NewConfig(WithConfig(src))
	if dst.CodeInterpreterBaseURL != "https://49999-sbx.example.com" {
		t.Errorf("CodeInterpreterBaseURL = %q, want copied", dst.CodeInterpreterBaseURL)
	}
}

func TestBuildRunCodePayload(t *testing.T) {
	timeout := 90 * time.Second
	payload := buildRunCodePayload("print(1)", RunCodeOpts{
		Language: LanguageJavaScript,
		Cwd:      "/tmp",
		Envs:     map[string]string{"A": "1"},
		Timeout:  timeout,
	})

	if payload.Code != "print(1)" {
		t.Errorf("Code = %q", payload.Code)
	}
	if payload.Language != "javascript" {
		t.Errorf("Language = %q, want javascript", payload.Language)
	}
	if payload.ContextID != "" {
		t.Errorf("ContextID = %q, want empty", payload.ContextID)
	}
	if payload.Timeout == nil || *payload.Timeout != 90 {
		t.Errorf("Timeout = %v, want 90", payload.Timeout)
	}
}

func TestBuildRunCodePayloadDefaults(t *testing.T) {
	payload := buildRunCodePayload("print(1)", RunCodeOpts{})
	if payload.Language != "python" {
		t.Errorf("default Language = %q, want python", payload.Language)
	}
	if payload.Timeout != nil {
		t.Errorf("default Timeout = %v, want nil", payload.Timeout)
	}
}

func TestBuildRunCodePayloadContextOverridesLanguage(t *testing.T) {
	payload := buildRunCodePayload("print(1)", RunCodeOpts{
		Language:  LanguageBash,
		ContextID: "ctx-1",
	})
	if payload.ContextID != "ctx-1" {
		t.Errorf("ContextID = %q, want ctx-1", payload.ContextID)
	}
	if payload.Language != "" {
		t.Errorf("Language = %q, want empty when context is set", payload.Language)
	}
}

func TestClientCodeInterpreterUsesBaseURL(t *testing.T) {
	var gotPath string
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		w.Header().Set("Content-Type", "application/json")
		if _, err := w.Write([]byte(`[]`)); err != nil {
			t.Errorf("failed to write response: %v", err)
		}
	}))
	t.Cleanup(server.Close)

	// A Client built from a config with a dedicated code-interpreter base
	// URL must send code-interpreter requests there (not to the runtime URL).
	cfg := NewConfig(WithCodeInterpreterBaseURL(server.URL))
	client := NewWithConfig("sbx-1", cfg)
	if _, err := client.CodeInterpreter.ListContexts(context.Background()); err != nil {
		t.Fatalf("ListContexts() error = %v", err)
	}
	if gotPath != "/contexts" {
		t.Errorf("request path = %q, want /contexts", gotPath)
	}
}

func TestSandboxHeadersRuntimePortFallback(t *testing.T) {
	// An unset or invalid runtime port falls back to the default instead of
	// producing a bogus "e2b-sandbox-port: 0" header.
	cfg := &Config{}
	if got := cfg.SandboxHeaders("sbx")["e2b-sandbox-port"]; got != "49983" {
		t.Errorf("e2b-sandbox-port = %q, want 49983", got)
	}
}
