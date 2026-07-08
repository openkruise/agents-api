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

import io.openkruise.agents.client.v2.models.Commit;
import io.openkruise.agents.client.v2.models.CommitSpec;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * E2E tests for Commit CRD.
 */
public class CommitTest extends BaseE2eTest {

    @Test
    public void testCommitCRUD() throws Exception {
        System.out.println("=== Commit: CRUD operations ===");
        String name = uniqueName("test-commit");

        by("Creating a new Commit");
        Commit commit = new Commit();
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "e2e-test-java");
        commit.setMetadata(buildMeta(name, labels));

        CommitSpec spec = new CommitSpec();
        spec.setPodName("test-pod");
        spec.setContainerName("test-container");
        spec.setImage("registry.example.com/test-image:v1");
        commit.setSpec(spec);

        Commit created = client.resources(Commit.class).inNamespace(NAMESPACE).resource(commit).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(Commit.class).inNamespace(NAMESPACE).withName(name).delete());

        by("Verifying the commit is created with correct spec");
        Commit got = client.resources(Commit.class).inNamespace(NAMESPACE).withName(name).get();
        assertNotNull(got);
        assertEquals("test-pod", got.getSpec().getPodName());
        assertEquals("test-container", got.getSpec().getContainerName());
        assertEquals("registry.example.com/test-image:v1", got.getSpec().getImage());

        by("Listing commits by label");
        List<Commit> list = client.resources(Commit.class).inNamespace(NAMESPACE)
                .withLabel("app", "e2e-test-java").list().getItems();
        assertTrue("Should have at least 1 Commit", list.size() >= 1);

        by("Deleting the commit and waiting for it to be removed");
        client.resources(Commit.class).inNamespace(NAMESPACE).withName(name).delete();
        eventuallyGone("commit deleted", TIMEOUT_SECONDS,
                () -> client.resources(Commit.class).inNamespace(NAMESPACE).withName(name).get());
    }

    @Test
    public void testCommitWithOptionalFields() throws Exception {
        System.out.println("=== Commit: with optional fields ===");
        String name = uniqueName("test-commit-opts");

        by("Creating a Commit with timeoutSeconds and registryAuth");
        Commit commit = new Commit();
        commit.setMetadata(buildMeta(name));

        CommitSpec spec = new CommitSpec();
        spec.setPodName("test-pod");
        spec.setContainerName("test-container");
        spec.setImage("registry.example.com/test-image:v2");
        spec.setTimeoutSeconds(300);
        spec.setSquashLayer(0);
        commit.setSpec(spec);

        Commit created = client.resources(Commit.class).inNamespace(NAMESPACE).resource(commit).create();
        assertNotNull(created);
        cleanupActions.add(() -> client.resources(Commit.class).inNamespace(NAMESPACE).withName(name).delete());

        by("Verifying optional fields");
        Commit got = client.resources(Commit.class).inNamespace(NAMESPACE).withName(name).get();
        assertNotNull(got);
        assertEquals(Integer.valueOf(300), got.getSpec().getTimeoutSeconds());
    }
}
