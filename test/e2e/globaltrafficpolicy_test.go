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

var _ = Describe("GlobalTrafficPolicy", func() {
	Context("CRUD operations", func() {
		var gtpName string

		BeforeEach(func() {
			gtpName = generateTestName("test-gtp")
		})

		AfterEach(func() {
			By("Cleaning up GlobalTrafficPolicy")
			gtp := &agentsv1alpha1.GlobalTrafficPolicy{
				ObjectMeta: metav1.ObjectMeta{
					Name: gtpName,
				},
			}
			_ = k8sClient.Delete(ctx, gtp)
		})

		It("should create, get, list, and delete a GlobalTrafficPolicy", func() {
			By("Creating GlobalTrafficPolicy")
			port := int32(443)
			gtp := &agentsv1alpha1.GlobalTrafficPolicy{
				ObjectMeta: metav1.ObjectMeta{
					Name: gtpName,
					Labels: map[string]string{
						"app":        "e2e-test",
						"managed-by": "ginkgo",
					},
				},
				Spec: agentsv1alpha1.TrafficPolicySpec{
					Priority: 500,
					Selector: metav1.LabelSelector{
						MatchLabels: map[string]string{
							"app": "global-agent",
						},
					},
					Ingress: &agentsv1alpha1.TrafficPolicyDirection{
						Rules: []agentsv1alpha1.TrafficPolicyRule{
							{
								Action: agentsv1alpha1.RuleActionAllow,
								From: []agentsv1alpha1.TrafficPolicyPeer{
									{CIDR: "192.168.0.0/16"},
								},
								Ports: []agentsv1alpha1.TrafficPolicyPort{
									{Protocol: "TCP", Port: ptr.To(port)},
								},
							},
						},
					},
				},
			}
			Expect(k8sClient.Create(ctx, gtp)).To(Succeed())

			By("Getting and verifying spec")
			got := &agentsv1alpha1.GlobalTrafficPolicy{}
			Expect(k8sClient.Get(ctx, client.ObjectKey{Name: gtpName}, got)).To(Succeed())
			Expect(got.Spec.Priority).To(Equal(int32(500)))
			Expect(got.Spec.Ingress).ToNot(BeNil())
			Expect(got.Spec.Ingress.Rules).To(HaveLen(1))

			By("Listing by label")
			list := &agentsv1alpha1.GlobalTrafficPolicyList{}
			Expect(k8sClient.List(ctx, list, client.MatchingLabels{"app": "e2e-test"})).To(Succeed())
			Expect(list.Items).ToNot(BeEmpty())

			By("Deleting GlobalTrafficPolicy")
			Expect(k8sClient.Delete(ctx, gtp)).To(Succeed())

			By("Verifying deletion")
			Eventually(func() bool {
				err := k8sClient.Get(ctx, client.ObjectKey{Name: gtpName}, &agentsv1alpha1.GlobalTrafficPolicy{})
				return errors.IsNotFound(err)
			}, time.Second*10, time.Second).Should(BeTrue())
		})

		It("should create a GlobalTrafficPolicy with workload peer", func() {
			By("Creating GlobalTrafficPolicy with workload selector")
			gtp := &agentsv1alpha1.GlobalTrafficPolicy{
				ObjectMeta: metav1.ObjectMeta{
					Name: gtpName,
				},
				Spec: agentsv1alpha1.TrafficPolicySpec{
					Selector: metav1.LabelSelector{
						MatchLabels: map[string]string{"app": "global-agent"},
					},
					Egress: &agentsv1alpha1.TrafficPolicyDirection{
						Rules: []agentsv1alpha1.TrafficPolicyRule{
							{
								Action: agentsv1alpha1.RuleActionAllow,
								To: []agentsv1alpha1.TrafficPolicyPeer{
									{
										Workload: &agentsv1alpha1.TrafficPolicyWorkloadRef{
											Namespace: "default",
											Selector:  map[string]string{"app": "backend"},
										},
									},
								},
							},
						},
					},
				},
			}
			Expect(k8sClient.Create(ctx, gtp)).To(Succeed())

			By("Verifying workload peer")
			got := &agentsv1alpha1.GlobalTrafficPolicy{}
			Expect(k8sClient.Get(ctx, client.ObjectKey{Name: gtpName}, got)).To(Succeed())
			Expect(got.Spec.Egress.Rules[0].To[0].Workload).ToNot(BeNil())
			Expect(got.Spec.Egress.Rules[0].To[0].Workload.Namespace).To(Equal("default"))
		})
	})
})
