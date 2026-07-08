package e2e

import (
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	agentsv1alpha1 "github.com/openkruise/agents-api/agents/v1alpha1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/utils/ptr"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

var _ = Describe("TrafficPolicy", func() {
	Context("CRUD operations", func() {
		var tpName string

		BeforeEach(func() {
			tpName = generateTestName("test-tp")
		})

		AfterEach(func() {
			By("Cleaning up TrafficPolicy")
			tp := &agentsv1alpha1.TrafficPolicy{
				ObjectMeta: metav1.ObjectMeta{
					Name:      tpName,
					Namespace: Namespace,
				},
			}
			_ = k8sClient.Delete(ctx, tp)
		})

		It("should create, get, list, and delete a TrafficPolicy", func() {
			By("Creating TrafficPolicy with ingress rules")
			port := int32(80)
			tp := &agentsv1alpha1.TrafficPolicy{
				ObjectMeta: metav1.ObjectMeta{
					Name:      tpName,
					Namespace: Namespace,
					Labels: map[string]string{
						"app":        "e2e-test",
						"managed-by": "ginkgo",
					},
				},
				Spec: agentsv1alpha1.TrafficPolicySpec{
					Priority: 1000,
					Selector: metav1.LabelSelector{
						MatchLabels: map[string]string{
							"app": "test-agent",
						},
					},
					Ingress: &agentsv1alpha1.TrafficPolicyDirection{
						Rules: []agentsv1alpha1.TrafficPolicyRule{
							{
								Action: agentsv1alpha1.RuleActionAllow,
								From: []agentsv1alpha1.TrafficPolicyPeer{
									{CIDR: "10.0.0.0/8"},
								},
								Ports: []agentsv1alpha1.TrafficPolicyPort{
									{Protocol: "TCP", Port: ptr.To(port)},
								},
							},
						},
					},
				},
			}
			Expect(k8sClient.Create(ctx, tp)).To(Succeed())

			By("Getting and verifying spec")
			got := &agentsv1alpha1.TrafficPolicy{}
			Expect(k8sClient.Get(ctx, client.ObjectKey{Name: tpName, Namespace: Namespace}, got)).To(Succeed())
			Expect(got.Spec.Priority).To(Equal(int32(1000)))
			Expect(got.Spec.Ingress).ToNot(BeNil())
			Expect(got.Spec.Ingress.Rules).To(HaveLen(1))
			Expect(got.Spec.Ingress.Rules[0].Action).To(Equal(agentsv1alpha1.RuleActionAllow))

			By("Listing by label")
			list := &agentsv1alpha1.TrafficPolicyList{}
			Expect(k8sClient.List(ctx, list, client.InNamespace(Namespace), client.MatchingLabels{"app": "e2e-test"})).To(Succeed())
			Expect(list.Items).ToNot(BeEmpty())

			By("Deleting TrafficPolicy")
			Expect(k8sClient.Delete(ctx, tp)).To(Succeed())

			By("Verifying deletion")
			Eventually(func() bool {
				err := k8sClient.Get(ctx, client.ObjectKey{Name: tpName, Namespace: Namespace}, &agentsv1alpha1.TrafficPolicy{})
				return errors.IsNotFound(err)
			}, time.Second*10, time.Second).Should(BeTrue())
		})

		It("should create a TrafficPolicy with egress reject rule", func() {
			By("Creating TrafficPolicy with egress rules")
			tp := &agentsv1alpha1.TrafficPolicy{
				ObjectMeta: metav1.ObjectMeta{
					Name:      tpName,
					Namespace: Namespace,
				},
				Spec: agentsv1alpha1.TrafficPolicySpec{
					Selector: metav1.LabelSelector{
						MatchLabels: map[string]string{"app": "test-agent"},
					},
					Egress: &agentsv1alpha1.TrafficPolicyDirection{
						Rules: []agentsv1alpha1.TrafficPolicyRule{
							{
								Action: agentsv1alpha1.RuleActionReject,
								To: []agentsv1alpha1.TrafficPolicyPeer{
									{FQDN: "*.evil.com"},
								},
							},
						},
					},
				},
			}
			Expect(k8sClient.Create(ctx, tp)).To(Succeed())

			By("Verifying egress spec")
			got := &agentsv1alpha1.TrafficPolicy{}
			Expect(k8sClient.Get(ctx, client.ObjectKey{Name: tpName, Namespace: Namespace}, got)).To(Succeed())
			Expect(got.Spec.Egress).ToNot(BeNil())
			Expect(got.Spec.Egress.Rules[0].Action).To(Equal(agentsv1alpha1.RuleActionReject))
			Expect(got.Spec.Egress.Rules[0].To[0].FQDN).To(Equal("*.evil.com"))
		})
	})
})
