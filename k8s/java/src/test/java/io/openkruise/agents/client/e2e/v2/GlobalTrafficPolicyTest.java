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

import io.openkruise.agents.client.v2.models.GlobalTrafficPolicy;
import io.openkruise.agents.client.v2.models.GlobalTrafficPolicySpec;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.Ingress;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.Egress;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.Selector;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.ingress.Rules;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.ingress.rules.From;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.ingress.rules.Ports;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.egress.rules.To;
import io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.egress.rules.to.Workload;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * E2E tests for GlobalTrafficPolicy CRD (cluster-scoped).
 */
public class GlobalTrafficPolicyTest extends BaseE2eTest {

    @Test
    public void testGlobalTrafficPolicyCRUD() throws Exception {
        System.out.println("=== GlobalTrafficPolicy: CRUD operations ===");
        String name = uniqueName("test-gtp");

        by("Creating a new GlobalTrafficPolicy");
        GlobalTrafficPolicy gtp = new GlobalTrafficPolicy();
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "e2e-test-java");

        io.fabric8.kubernetes.api.model.ObjectMeta meta = new io.fabric8.kubernetes.api.model.ObjectMeta();
        meta.setName(name);
        meta.setLabels(labels);
        gtp.setMetadata(meta);

        GlobalTrafficPolicySpec spec = new GlobalTrafficPolicySpec();
        spec.setPriority(500);

        Selector selector = new Selector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", "global-agent");
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);

        Rules rule = new Rules();
        rule.setAction(Rules.Action.ALLOW);

        From from = new From();
        from.setCidr("192.168.0.0/16");
        rule.setFrom(Collections.singletonList(from));

        Ports port = new Ports();
        port.setProtocol(Ports.Protocol.TCP);
        port.setPort(443);
        rule.setPorts(Collections.singletonList(port));

        Ingress ingress = new Ingress();
        ingress.setRules(Collections.singletonList(rule));
        spec.setIngress(ingress);

        gtp.setSpec(spec);

        GlobalTrafficPolicy created = client.resources(GlobalTrafficPolicy.class).resource(gtp).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(GlobalTrafficPolicy.class).withName(name).delete());

        by("Verifying the global traffic policy is created with correct spec");
        GlobalTrafficPolicy got = client.resources(GlobalTrafficPolicy.class).withName(name).get();
        assertNotNull(got);
        assertEquals(Integer.valueOf(500), got.getSpec().getPriority());
        assertNotNull(got.getSpec().getIngress());
        assertEquals(1, got.getSpec().getIngress().getRules().size());

        by("Listing global traffic policies by label");
        List<GlobalTrafficPolicy> list = client.resources(GlobalTrafficPolicy.class)
                .withLabel("app", "e2e-test-java").list().getItems();
        assertTrue("Should have at least 1 GlobalTrafficPolicy", list.size() >= 1);

        by("Deleting the global traffic policy and waiting for it to be removed");
        client.resources(GlobalTrafficPolicy.class).withName(name).delete();
        eventuallyGone("global traffic policy deleted", TIMEOUT_SECONDS,
                () -> client.resources(GlobalTrafficPolicy.class).withName(name).get());
    }

    @Test
    public void testGlobalTrafficPolicyWithWorkloadPeer() throws Exception {
        System.out.println("=== GlobalTrafficPolicy: with workload peer ===");
        String name = uniqueName("test-gtp-wl");

        by("Creating a GlobalTrafficPolicy with workload peer");
        GlobalTrafficPolicy gtp = new GlobalTrafficPolicy();

        io.fabric8.kubernetes.api.model.ObjectMeta meta = new io.fabric8.kubernetes.api.model.ObjectMeta();
        meta.setName(name);
        gtp.setMetadata(meta);

        GlobalTrafficPolicySpec spec = new GlobalTrafficPolicySpec();
        Selector selector = new Selector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put("app", "global-agent");
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);

        io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.egress.Rules rule =
            new io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.egress.Rules();
        rule.setAction(io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.egress.Rules.Action.ALLOW);

        To to = new To();
        Workload workload = new Workload();
        workload.setNamespace("default");
        Map<String, String> wlSelector = new HashMap<>();
        wlSelector.put("app", "backend");
        workload.setSelector(wlSelector);
        to.setWorkload(workload);
        rule.setTo(Collections.singletonList(to));

        Egress egress = new Egress();
        egress.setRules(Collections.singletonList(rule));
        spec.setEgress(egress);

        gtp.setSpec(spec);

        GlobalTrafficPolicy created = client.resources(GlobalTrafficPolicy.class).resource(gtp).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(GlobalTrafficPolicy.class).withName(name).delete());

        by("Verifying workload peer");
        GlobalTrafficPolicy got = client.resources(GlobalTrafficPolicy.class).withName(name).get();
        assertNotNull(got);
        assertNotNull(got.getSpec().getEgress().getRules().get(0).getTo().get(0).getWorkload());
        assertEquals("default", got.getSpec().getEgress().getRules().get(0).getTo().get(0).getWorkload().getNamespace());
    }
}
