/*
Copyright 2026.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// ConditionReady is the condition every kind in this group reports. True means
// the current generation passed validation and the resources it depends on are
// available, so a consumer that requires a resource to be usable can check this
// one condition without knowing which kind it is looking at.
const ConditionReady = "Ready"

// AuthenticationConfigReference names an AgentAuthenticationConfig in the same
// namespace.
//
// Cross-namespace references are not supported: a namespace must not be able to
// borrow another namespace's trusted issuer.
type AuthenticationConfigReference struct {
	// APIGroup of the referent. Only security.agents.kruise.io is accepted.
	// +kubebuilder:validation:Required
	// +kubebuilder:validation:Enum=security.agents.kruise.io
	APIGroup string `json:"apiGroup"`

	// Kind of the referent. Only AgentAuthenticationConfig is accepted.
	// +kubebuilder:validation:Required
	// +kubebuilder:validation:Enum=AgentAuthenticationConfig
	Kind string `json:"kind"`

	// Name of the referent in the same namespace.
	// +kubebuilder:validation:Required
	// +kubebuilder:validation:MinLength=1
	// +kubebuilder:validation:MaxLength=253
	Name string `json:"name"`
}

// AgentIdentitySpec declares a logical agent.
//
// Token lifetimes are deliberately absent. They are server-side policy in the
// identity provider, so a namespace cannot widen its own token validity by
// editing a resource it controls.
type AgentIdentitySpec struct {
	// AuthenticationRefs lists the issuers allowed to authenticate end users
	// delegating to this agent. At least one is required.
	//
	// An identity that names no issuer cannot take part in a principal token
	// exchange, so it is not usable and must not report Ready. The constraint is
	// declared here rather than left to the controller so the API server rejects
	// an empty list outright, but the controller still fails an existing object
	// closed: an object created before this constraint can carry an empty list
	// that admission never saw.
	// +kubebuilder:validation:Required
	// +kubebuilder:validation:MinItems=1
	// +kubebuilder:validation:MaxItems=32
	// +listType=atomic
	AuthenticationRefs []AuthenticationConfigReference `json:"authenticationRefs"`
}

// AgentIdentityStatus reports whether the identity is usable.
type AgentIdentityStatus struct {
	// ObservedGeneration is the most recent generation observed by the
	// controller.
	// +optional
	ObservedGeneration int64 `json:"observedGeneration,omitempty"`

	// Conditions holds the observations of the identity's state. Ready is True
	// when the current generation validated and every referenced
	// AgentAuthenticationConfig is itself Ready.
	// +optional
	// +listType=map
	// +listMapKey=type
	Conditions []metav1.Condition `json:"conditions,omitempty"`
}

// +kubebuilder:object:root=true
// +kubebuilder:subresource:status
// +kubebuilder:resource:shortName=aid,categories=agents
// +kubebuilder:printcolumn:name="Ready",type=string,JSONPath=`.status.conditions[?(@.type=="Ready")].status`
// +kubebuilder:printcolumn:name="Age",type=date,JSONPath=`.metadata.creationTimestamp`

// AgentIdentity declares a logical agent that sandboxes can assume.
//
// A Sandbox selects one through the security.agents.kruise.io/agent-name
// annotation. The name must resolve to a Ready AgentIdentity in the sandbox's
// own namespace; issuance fails closed rather than falling back to another
// identity or an unsigned token.
// +genclient
type AgentIdentity struct {
	metav1.TypeMeta `json:",inline"`
	// +optional
	metav1.ObjectMeta `json:"metadata,omitempty"`

	// Spec defines the agent identity.
	Spec AgentIdentitySpec `json:"spec"`

	// Status is the current state of the identity.
	// +optional
	Status AgentIdentityStatus `json:"status,omitempty"`
}

// +kubebuilder:object:root=true

// AgentIdentityList contains a list of AgentIdentity.
type AgentIdentityList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []AgentIdentity `json:"items"`
}

func init() {
	SchemeBuilder.Register(&AgentIdentity{}, &AgentIdentityList{})
}
