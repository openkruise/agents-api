package v1alpha1

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	rest "k8s.io/client-go/rest"
)

func TestSandboxUpdateOpsListUsesConfiguredResourcePath(t *testing.T) {
	t.Parallel()

	const expectedPath = "/apis/agents.kruise.io/v1alpha1/namespaces/default/sandboxupdateops"

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			t.Fatalf("unexpected method %q", r.Method)
		}
		if r.URL.Path != expectedPath {
			t.Fatalf("unexpected request path %q, want %q", r.URL.Path, expectedPath)
		}

		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`{"apiVersion":"agents.kruise.io/v1alpha1","kind":"SandboxUpdateOpsList","items":[]}`))
	}))
	defer server.Close()

	client, err := NewForConfig(&rest.Config{Host: server.URL})
	if err != nil {
		t.Fatalf("NewForConfig() error = %v", err)
	}

	if _, err := client.SandboxUpdateOps("default").List(context.Background(), metav1.ListOptions{}); err != nil {
		t.Fatalf("List() error = %v", err)
	}
}
