package runtime

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"sync"

	"github.com/openkruise/agents-api/agents/v1alpha1"
	kruiseclient "github.com/openkruise/agents-api/client/clientset/versioned"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
)

var (
	sharedK8sClient     *kruiseclient.Clientset
	sharedK8sClientOnce sync.Once
	sharedK8sClientErr  error
)

// NewFromK8s creates a runtime Client by looking up the Sandbox CR in Kubernetes.
// It resolves sandboxID and runtimeToken automatically.
func NewFromK8s(ctx context.Context, namespace, name string, opts ...Option) (*Client, error) {
	k8sClient, err := buildK8sClient()
	if err != nil {
		return nil, fmt.Errorf("failed to create k8s client: %w", err)
	}

	sandbox, err := k8sClient.AgentsV1alpha1().Sandboxes(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("failed to get sandbox %s/%s: %w", namespace, name, err)
	}

	sandboxID := namespace + "--" + name
	runtimeToken := sandbox.Annotations[v1alpha1.AnnotationRuntimeAccessToken]
	if runtimeToken != "" {
		opts = append(opts, WithRuntimeToken(runtimeToken))
	}

	return New(sandboxID, opts...), nil
}

func buildK8sClient() (*kruiseclient.Clientset, error) {
	sharedK8sClientOnce.Do(func() {
		cfg, err := buildK8sConfig()
		if err != nil {
			sharedK8sClientErr = err
			return
		}
		sharedK8sClient, sharedK8sClientErr = kruiseclient.NewForConfig(cfg)
	})
	return sharedK8sClient, sharedK8sClientErr
}

// buildK8sConfig resolves kubeconfig: KUBECONFIG env → ~/.kube/config → in-cluster.
func buildK8sConfig() (*rest.Config, error) {
	kubeconfigPath := os.Getenv("KUBECONFIG")

	if kubeconfigPath == "" {
		if home := homedir.HomeDir(); home != "" {
			defaultPath := filepath.Join(home, ".kube", "config")
			if _, err := os.Stat(defaultPath); err == nil {
				kubeconfigPath = defaultPath
			}
		}
	}

	if kubeconfigPath != "" {
		cfg, err := clientcmd.BuildConfigFromFlags("", kubeconfigPath)
		if err == nil {
			return cfg, nil
		}
	}

	return rest.InClusterConfig()
}
