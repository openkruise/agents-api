package e2e

import (
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	agentsv1alpha1 "github.com/openkruise/agents-api/agents/v1alpha1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

var _ = Describe("Commit", func() {
	Context("CRUD operations", func() {
		var commitName string

		BeforeEach(func() {
			commitName = generateTestName("test-commit")
		})

		AfterEach(func() {
			By("Cleaning up Commit")
			cmt := &agentsv1alpha1.Commit{
				ObjectMeta: metav1.ObjectMeta{
					Name:      commitName,
					Namespace: Namespace,
				},
			}
			_ = k8sClient.Delete(ctx, cmt)
		})

		It("should create, get, list, and delete a Commit", func() {
			By("Creating Commit")
			commit := &agentsv1alpha1.Commit{
				ObjectMeta: metav1.ObjectMeta{
					Name:      commitName,
					Namespace: Namespace,
					Labels: map[string]string{
						"app":        "e2e-test",
						"managed-by": "ginkgo",
					},
				},
				Spec: agentsv1alpha1.CommitSpec{
					PodName:       "test-pod",
					ContainerName: "test-container",
					Image:         "registry.example.com/test-image:v1",
				},
			}
			Expect(k8sClient.Create(ctx, commit)).To(Succeed())

			By("Getting and verifying spec")
			got := &agentsv1alpha1.Commit{}
			Expect(k8sClient.Get(ctx, client.ObjectKey{Name: commitName, Namespace: Namespace}, got)).To(Succeed())
			Expect(got.Spec.PodName).To(Equal("test-pod"))
			Expect(got.Spec.ContainerName).To(Equal("test-container"))
			Expect(got.Spec.Image).To(Equal("registry.example.com/test-image:v1"))

			By("Listing by label")
			list := &agentsv1alpha1.CommitList{}
			Expect(k8sClient.List(ctx, list, client.InNamespace(Namespace), client.MatchingLabels{"app": "e2e-test"})).To(Succeed())
			Expect(list.Items).ToNot(BeEmpty())

			By("Deleting Commit")
			Expect(k8sClient.Delete(ctx, commit)).To(Succeed())

			By("Verifying deletion")
			Eventually(func() bool {
				err := k8sClient.Get(ctx, client.ObjectKey{Name: commitName, Namespace: Namespace}, &agentsv1alpha1.Commit{})
				return errors.IsNotFound(err)
			}, time.Second*10, time.Second).Should(BeTrue())
		})

		It("should create a Commit with optional fields", func() {
			By("Creating Commit with registryAuth and timeout")
			commit := &agentsv1alpha1.Commit{
				ObjectMeta: metav1.ObjectMeta{
					Name:      commitName,
					Namespace: Namespace,
				},
				Spec: agentsv1alpha1.CommitSpec{
					PodName:        "test-pod",
					ContainerName:  "test-container",
					Image:          "registry.example.com/test-image:v2",
					SquashLayer:    0,
					TimeoutSeconds: 300,
					RegistryAuth: &agentsv1alpha1.RegistryAuth{
						Secrets: []string{"my-registry-secret"},
					},
				},
			}
			Expect(k8sClient.Create(ctx, commit)).To(Succeed())

			By("Verifying optional fields")
			got := &agentsv1alpha1.Commit{}
			Expect(k8sClient.Get(ctx, client.ObjectKey{Name: commitName, Namespace: Namespace}, got)).To(Succeed())
			Expect(got.Spec.TimeoutSeconds).To(Equal(int32(300)))
			Expect(got.Spec.RegistryAuth).ToNot(BeNil())
			Expect(got.Spec.RegistryAuth.Secrets).To(ContainElement("my-registry-secret"))
		})
	})
})
