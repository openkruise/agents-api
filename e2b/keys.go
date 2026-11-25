package e2b

import (
	"crypto/sha256"
	"encoding/hex"
	"fmt"
)

// E2B SDK key-compat encoding constants. These must stay in sync with the
// Python kruise_agents keys module and the upstream sandbox-manager's
// pkg/servers/e2b/keys/compat.go.
const (
	e2bSDKCompatPrefix = "e2b_"
	// e2bSDKCompatMagic hex-encodes the ASCII bytes "okag" ("ok" + "ag[ents]").
	e2bSDKCompatMagic = "6f6b6167"
	// e2bSDKCompatVersion is the single released format version.
	e2bSDKCompatVersion = "01"
	// e2bSDKCompatChecksumSalt namespaces the checksum so encoded keys from
	// unrelated schemes cannot collide.
	e2bSDKCompatChecksumSalt = "openkruise-agents/e2b-key-compat/v1"
)

// EncodeForE2BSDK wraps a raw OpenKruise Agents API key (UUID or admin key)
// in an E2B SDK-compatible form. Use it when feeding a raw key to a client
// that enforces the official E2B key format; this SDK sends raw keys as-is
// and never needs the wrapper itself.
//
// Inputs larger than 4 GB would overflow the 8-hex-character length field;
// real keys are far below that and the overflow is part of the wire format
// definition (no runtime guard, matching the reference implementations).
func EncodeForE2BSDK(raw string) string {
	rawBytes := []byte(raw)

	checksumPayload := append([]byte(e2bSDKCompatChecksumSalt), rawBytes...)
	digest := sha256.Sum256(checksumPayload)

	// Length is rendered as 8 lowercase hex digits; lowercase is stable for
	// every value because %08x never emits uppercase letters.
	body := hex.EncodeToString(rawBytes)
	checksum := hex.EncodeToString(digest[:8])
	return fmt.Sprintf("%s%s%s%08x%s%s",
		e2bSDKCompatPrefix,
		e2bSDKCompatMagic,
		e2bSDKCompatVersion,
		len(rawBytes),
		body,
		checksum,
	)
}
