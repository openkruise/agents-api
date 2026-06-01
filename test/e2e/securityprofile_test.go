package e2e

import (
	"fmt"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	agentsv1alpha1 "github.com/openkruise/agents-api/agents/v1alpha1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

var _ = Describe("SecurityProfile", func() {
	var profile *agentsv1alpha1.SecurityProfile

	BeforeEach(func() {
		profile = &agentsv1alpha1.SecurityProfile{
			ObjectMeta: metav1.ObjectMeta{
				Name:      fmt.Sprintf("test-sp-%d", time.Now().UnixNano()),
				Namespace: Namespace,
				Labels: map[string]string{
					"app":        "e2e-test",
					"managed-by": "ginkgo",
				},
			},
			Spec: agentsv1alpha1.SecurityProfileSpec{
				Selector: metav1.LabelSelector{
					MatchLabels: map[string]string{
						"app": "test-agent",
					},
				},
				Rules: []agentsv1alpha1.SecurityRule{
					{
						Name: "block-external",
						Match: []agentsv1alpha1.RuleMatch{
							{
								Domains: []string{"*.evil.com"},
							},
						},
						Actions: agentsv1alpha1.SecurityRuleActions{
							Block: &agentsv1alpha1.BlockAction{
								StatusCode: 403,
							},
						},
					},
				},
			},
		}
	})

	AfterEach(func() {
		By("Cleaning up SecurityProfile")
		_ = k8sClient.Delete(ctx, profile)
	})

	Context("CRUD operations", func() {
		It("should create, get, update, list, and delete securityprofile", func() {
			By("Creating SecurityProfile")
			Expect(k8sClient.Create(ctx, profile)).To(Succeed())

			By("Verifying the securityprofile is created")
			Eventually(func() error {
				return k8sClient.Get(ctx, types.NamespacedName{
					Name:      profile.Name,
					Namespace: profile.Namespace,
				}, profile)
			}, time.Second*5, time.Millisecond*500).Should(Succeed())

			By("Updating labels")
			Eventually(func() error {
				if err := k8sClient.Get(ctx, types.NamespacedName{
					Name:      profile.Name,
					Namespace: profile.Namespace,
				}, profile); err != nil {
					return err
				}
				profile.Labels["updated"] = "true"
				return k8sClient.Update(ctx, profile)
			}, time.Second*10, time.Millisecond*500).Should(Succeed())

			By("Listing SecurityProfiles by label")
			list := &agentsv1alpha1.SecurityProfileList{}
			Expect(k8sClient.List(ctx, list, client.InNamespace(Namespace), client.MatchingLabels{"app": "e2e-test"})).To(Succeed())
			Expect(list.Items).ToNot(BeEmpty())

			By("Deleting SecurityProfile")
			Expect(k8sClient.Delete(ctx, profile)).To(Succeed())

			By("Verifying deletion")
			Eventually(func() bool {
				err := k8sClient.Get(ctx, types.NamespacedName{
					Name:      profile.Name,
					Namespace: profile.Namespace,
				}, &agentsv1alpha1.SecurityProfile{})
				return errors.IsNotFound(err)
			}, time.Second*10, time.Second).Should(BeTrue())
		})
	})

	Context("Spec validation", func() {
		It("should create a securityprofile with bypass rule", func() {
			By("Creating SecurityProfile with bypass action")
			profile.Spec.Rules = []agentsv1alpha1.SecurityRule{
				{
					Name: "allow-internal",
					Match: []agentsv1alpha1.RuleMatch{
						{
							Domains: []string{"*.internal.company.com"},
						},
					},
					Actions: agentsv1alpha1.SecurityRuleActions{
						Bypass: true,
					},
				},
			}
			Expect(k8sClient.Create(ctx, profile)).To(Succeed())

			By("Verifying the spec is persisted correctly")
			got := &agentsv1alpha1.SecurityProfile{}
			Expect(k8sClient.Get(ctx, types.NamespacedName{
				Name:      profile.Name,
				Namespace: profile.Namespace,
			}, got)).To(Succeed())
			Expect(got.Spec.Rules).To(HaveLen(1))
			Expect(got.Spec.Rules[0].Name).To(Equal("allow-internal"))
			Expect(got.Spec.Rules[0].Actions.Bypass).To(Equal(true))
		})
	})
})
