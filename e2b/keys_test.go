package e2b

import "testing"

// Golden values byte-for-byte identical to the Python kruise_agents keys
// module tests (e2b/python/tests/test_keys.py), locking wire-format parity
// across the two SDKs and the upstream sandbox-manager compat encoder.
var encodeForE2BSDKGolden = map[string]string{
	"":                                     "e2b_6f6b61670100000000425929dd6afb855b",
	"admin-key":                            "e2b_6f6b6167010000000961646d696e2d6b6579c4341091192d130a",
	"5b14a58f-93f4-4d3e-9a92-2f3e0e1a9e33": "e2b_6f6b6167010000002435623134613538662d393366342d346433652d396139322d3266336530653161396533331d5464e1669224e7",
}

func TestEncodeForE2BSDKGolden(t *testing.T) {
	for raw, want := range encodeForE2BSDKGolden {
		if got := EncodeForE2BSDK(raw); got != want {
			t.Errorf("EncodeForE2BSDK(%q) = %q, want %q", raw, got, want)
		}
	}
}

func TestEncodeForE2BSDKStructure(t *testing.T) {
	encoded := EncodeForE2BSDK("some-random-key")

	if len(encoded) < len(e2bSDKCompatPrefix)+len(e2bSDKCompatMagic)+len(e2bSDKCompatVersion)+8+16 {
		t.Fatalf("encoded key %q too short for header + checksum", encoded)
	}
	// header = prefix + magic + version + 8-hex length ("some-random-key" is
	// 15 bytes = 0x0f)
	headerLen := len(e2bSDKCompatPrefix) + len(e2bSDKCompatMagic) + len(e2bSDKCompatVersion) + 8
	if got, want := encoded[:headerLen], "e2b_6f6b6167010000000f"; got != want {
		t.Errorf("header = %q, want %q", got, want)
	}
	// The checksum is always the last 16 hex characters (8 bytes).
	if got, want := len(encoded)-headerLen, 2*len("some-random-key")+16; got != want {
		t.Errorf("body+checksum length = %d, want %d", got, want)
	}
}
