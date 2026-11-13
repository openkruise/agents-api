package v1alpha1

import (
	"encoding/json"
	"reflect"
	"testing"
)

func TestSecurityProfileTCPRulesRoundTripAndDeepCopy(t *testing.T) {
	const payload = `{"selector":{},"tcpRules":[{"name":"host","match":[{"cidrs":["140.205.162.5","2001:db8::1"],"ports":[443]}],"action":"Allow"}]}`
	var spec SecurityProfileSpec
	if err := json.Unmarshal([]byte(payload), &spec); err != nil {
		t.Fatal(err)
	}
	if spec.TCPRules[0].Action != TCPActionAllow {
		t.Fatalf("unexpected action: %q", spec.TCPRules[0].Action)
	}
	encoded, err := json.Marshal(spec)
	if err != nil {
		t.Fatal(err)
	}
	var got, want any
	if err := json.Unmarshal(encoded, &got); err != nil {
		t.Fatal(err)
	}
	if err := json.Unmarshal([]byte(payload), &want); err != nil {
		t.Fatal(err)
	}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("round trip changed TCP rules: %s", encoded)
	}
	profile := &SecurityProfile{Spec: spec}
	global := &GlobalSecurityProfile{Spec: spec}
	for _, copy := range []*SecurityProfileSpec{&profile.DeepCopy().Spec, &global.DeepCopy().Spec} {
		copy.TCPRules[0].Action = TCPActionDeny
		copy.TCPRules[0].Match[0].CIDRs[0] = "10.0.0.0/8"
		copy.TCPRules[0].Match[0].Ports[0] = 80
	}
	if !reflect.DeepEqual(profile.Spec, spec) || !reflect.DeepEqual(global.Spec, spec) || spec.TCPRules[0].Action != TCPActionAllow || spec.TCPRules[0].Match[0].CIDRs[0] != "140.205.162.5" || spec.TCPRules[0].Match[0].Ports[0] != 443 {
		t.Fatal("deep copy shares TCP rule storage with the original")
	}
}
