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

import io.openkruise.agents.client.v2.models.TrafficPolicy;
import io.openkruise.agents.client.v2.models.TrafficPolicySpec;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.Ingress;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.Egress;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.Selector;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.Rules;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.From;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.Ports;
import io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.To;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * E2E tests for TrafficPolicy CRD.
 */
public class TrafficPolicyTest extends BaseE2eTest {

    @Test
    public void testTrafficPolicyCRUD() throws Exception {
        System.out.println("=== TrafficPolicy: CRUD operations ===");
        String name = uniqueName("test-tp");

        by("Creating a new TrafficPolicy with ingress rules");
        TrafficPolicy tp = new TrafficPolicy();
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "e2e-test-java");
        tp.setMetadata(buildMeta(name, labels));

        TrafficPolicySpec spec = new TrafficPolicySpec();
        spec.setPriority(1000);

        Selector selector = new Selector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", "test-agent");
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);

        Rules rule = new Rules();
        rule.setAction(Rules.Action.ALLOW);

        From from = new From();
        from.setCidr("10.0.0.0/8");
        rule.setFrom(Collections.singletonList(from));

        Ports port = new Ports();
        port.setProtocol(Ports.Protocol.TCP);
        port.setPort(80);
        rule.setPorts(Collections.singletonList(port));

        Ingress ingress = new Ingress();
        ingress.setRules(Collections.singletonList(rule));
        spec.setIngress(ingress);

        tp.setSpec(spec);

        TrafficPolicy created = client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).resource(tp).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).withName(name).delete());

        by("Verifying the traffic policy is created with correct spec");
        TrafficPolicy got = client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).withName(name).get();
        assertNotNull(got);
        assertEquals(Integer.valueOf(1000), got.getSpec().getPriority());
        assertNotNull(got.getSpec().getIngress());
        assertEquals(1, got.getSpec().getIngress().getRules().size());
        assertEquals(Rules.Action.ALLOW, got.getSpec().getIngress().getRules().get(0).getAction());

        by("Listing traffic policies by label");
        List<TrafficPolicy> list = client.resources(TrafficPolicy.class).inNamespace(NAMESPACE)
                .withLabel("app", "e2e-test-java").list().getItems();
        assertTrue("Should have at least 1 TrafficPolicy", list.size() >= 1);

        by("Deleting the traffic policy and waiting for it to be removed");
        client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).withName(name).delete();
        eventuallyGone("traffic policy deleted", TIMEOUT_SECONDS,
                () -> client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).withName(name).get());
    }

    @Test
    public void testTrafficPolicyEgressReject() throws Exception {
        System.out.println("=== TrafficPolicy: egress reject rule ===");
        String name = uniqueName("test-tp-egress");

        by("Creating a TrafficPolicy with egress reject rule");
        TrafficPolicy tp = new TrafficPolicy();
        tp.setMetadata(buildMeta(name));

        TrafficPolicySpec spec = new TrafficPolicySpec();
        Selector selector = new Selector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", "test-agent");
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);

        io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.Rules rule =
            new io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.Rules();
        rule.setAction(io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.Rules.Action.REJECT);

        To to = new To();
        to.setFqdn("*.evil.com");
        rule.setTo(Collections.singletonList(to));

        Egress egress = new Egress();
        egress.setRules(Collections.singletonList(rule));
        spec.setEgress(egress);

        tp.setSpec(spec);

        TrafficPolicy created = client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).resource(tp).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).withName(name).delete());

        by("Verifying egress spec");
        TrafficPolicy got = client.resources(TrafficPolicy.class).inNamespace(NAMESPACE).withName(name).get();
        assertNotNull(got);
        assertNotNull(got.getSpec().getEgress());
        assertEquals(io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.Rules.Action.REJECT,
            got.getSpec().getEgress().getRules().get(0).getAction());
        assertEquals("*.evil.com", got.getSpec().getEgress().getRules().get(0).getTo().get(0).getFqdn());
    }
}
