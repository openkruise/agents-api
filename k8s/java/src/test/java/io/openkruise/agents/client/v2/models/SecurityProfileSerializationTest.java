package io.openkruise.agents.client.v2.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SecurityProfileSerializationTest {
    @Test
    public void proxyActionsRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = "{\"spec\":{\"selector\":{},\"rules\":[{\"actions\":{"
                + "\"proxy\":{\"url\":\"https://proxy.example:443\"},\"bypass\":false}}]}}";
        JsonNode expected = mapper.readTree(payload).get("spec").get("rules");
        for (Class<?> type : new Class<?>[]{SecurityProfile.class, GlobalSecurityProfile.class}) {
            Object resource = mapper.readValue(payload, type);
            assertEquals(expected, mapper.valueToTree(resource).get("spec").get("rules"));
        }
    }

    @Test
    public void startupModeAdmissionResponseRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        for (String mode : new String[]{"Immediate", "Deferred"}) {
            String payload = "{\"spec\":{\"startupMode\":\"" + mode + "\"}}";
            for (Class<?> type : new Class<?>[]{Sandbox.class, SandboxSet.class, SandboxTemplate.class}) {
                Object resource = mapper.readValue(payload, type);
                assertEquals(mode, mapper.valueToTree(resource).get("spec").get("startupMode").asText());
            }
        }
    }

    @Test
    public void tcpRulesRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = "{\"spec\":{\"selector\":{},\"tcpRules\":["
                + "{\"name\":\"host\",\"match\":[{\"cidrs\":[\"140.205.162.5\",\"2001:db8::1\"],\"ports\":[443]}],\"action\":\"Allow\"},"
                + "{\"name\":\"deny-other\",\"match\":[{\"cidrs\":[\"0.0.0.0/0\",\"::/0\"]}],\"action\":\"Deny\"}]}}";
        JsonNode expected = mapper.readTree(payload).get("spec").get("tcpRules");
        for (Class<?> type : new Class<?>[]{SecurityProfile.class, GlobalSecurityProfile.class}) {
            Object resource = mapper.readValue(payload, type);
            assertEquals(expected, mapper.valueToTree(resource).get("spec").get("tcpRules"));
        }
    }
}
