/*
Copyright 2025 The OpenKruise Authors.

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

package io.openkruise.agents.client.e2e.v2;

import io.openkruise.agents.client.v2.models.SecurityProfile;
import io.openkruise.agents.client.v2.models.SecurityProfileSpec;
import io.openkruise.agents.client.v2.models.securityprofilespec.Selector;
import io.openkruise.agents.client.v2.models.securityprofilespec.Rules;
import io.openkruise.agents.client.v2.models.securityprofilespec.rules.Actions;
import io.openkruise.agents.client.v2.models.securityprofilespec.rules.Match;
import io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Block;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * E2E tests for SecurityProfile CRD.
 */
public class SecurityProfileTest extends BaseE2eTest {

    @Test
    public void testSecurityProfileCreateAndGet() throws Exception {
        System.out.println("=== SecurityProfile: creation and get ===");
        String name = uniqueName("test-sp");

        by("Creating a new SecurityProfile");
        SecurityProfile sp = new SecurityProfile();
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "e2e-test-java");
        sp.setMetadata(buildMeta(name, labels));

        SecurityProfileSpec spec = new SecurityProfileSpec();

        // Set selector
        Selector selector = new Selector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", "test-agent");
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);

        // Set rules with block action
        Block block = new Block();
        block.setStatusCode(403);

        Actions actions = new Actions();
        actions.setBlock(block);

        Match matchItem = new Match();
        matchItem.setDomains(Arrays.asList("*.evil.com"));

        Rules rule = new Rules();
        rule.setName("block-external");
        rule.setMatch(Collections.singletonList(matchItem));
        rule.setActions(actions);

        spec.setRules(Collections.singletonList(rule));
        sp.setSpec(spec);

        SecurityProfile created = client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).resource(sp).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).withName(name).delete());

        by("Verifying the SecurityProfile is created");
        SecurityProfile got = client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).withName(name).get();
        assertNotNull(got);
        assertEquals(name, got.getMetadata().getName());
        assertEquals("test-agent", got.getSpec().getSelector().getMatchLabels().get("app"));
        assertEquals("block-external", got.getSpec().getRules().get(0).getName());
    }

    @Test
    public void testSecurityProfileUpdateAndDelete() throws Exception {
        System.out.println("=== SecurityProfile: update and delete ===");
        String name = uniqueName("test-sp-update");

        by("Creating a new SecurityProfile");
        SecurityProfile sp = new SecurityProfile();
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "e2e-test-java");
        sp.setMetadata(buildMeta(name, labels));

        SecurityProfileSpec spec = new SecurityProfileSpec();
        Selector selector = new Selector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", "test-agent");
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);
        sp.setSpec(spec);

        client.resources(SecurityProfile.class).inNamespace(NAMESPACE).resource(sp).create();
        cleanupActions.add(() -> client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).withName(name).delete());

        by("Updating SecurityProfile labels");
        SecurityProfile got = client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).withName(name).get();
        got.getMetadata().getLabels().put("updated", "true");
        SecurityProfile updated = client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).resource(got).update();
        assertEquals("true", updated.getMetadata().getLabels().get("updated"));

        by("Listing SecurityProfiles by label");
        List<SecurityProfile> list = client.resources(SecurityProfile.class)
                .inNamespace(NAMESPACE).withLabel("app", "e2e-test-java").list().getItems();
        assertTrue("Should have at least 1 SecurityProfile", list.size() >= 1);

        by("Deleting the SecurityProfile and waiting for it to be removed");
        client.resources(SecurityProfile.class).inNamespace(NAMESPACE).withName(name).delete();
        eventuallyGone("securityprofile deleted", TIMEOUT_SECONDS,
                () -> client.resources(SecurityProfile.class).inNamespace(NAMESPACE).withName(name).get());
    }
}
