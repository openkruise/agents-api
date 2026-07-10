package io.openkruise.agents.client.e2b;

import io.openkruise.agents.client.e2b.api.models.SandboxDetail;
import io.openkruise.agents.client.e2b.api.models.SandboxLifecycle;
import io.openkruise.agents.client.e2b.api.models.SandboxNetworkConfig;
import io.openkruise.agents.client.e2b.api.models.SandboxVolumeMount;
import io.openkruise.agents.client.e2b.api.models.SandboxesGet200ResponseInner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable sandbox information, used uniformly for list() and getInfo() return values.
 * Fields unique to detail are null when returned by list().
 */
public class SandboxInfo {

    private final String sandboxID;
    private final String templateID;
    private final String alias;
    private final String clientID;
    private final OffsetDateTime startedAt;
    private final OffsetDateTime endAt;
    private final Integer cpuCount;
    private final Integer memoryMB;
    private final Integer diskSizeMB;
    private final String envdVersion;
    private final Map<String, String> metadata;
    private final String state;
    private final List<SandboxVolumeMount> volumeMounts;

    private final String envdAccessToken;
    private final String domain;
    private final Boolean allowInternetAccess;
    private final SandboxNetworkConfig network;
    private final SandboxLifecycle lifecycle;

    private SandboxInfo(Builder builder) {
        this.sandboxID = builder.sandboxID;
        this.templateID = builder.templateID;
        this.alias = builder.alias;
        this.clientID = builder.clientID;
        this.startedAt = builder.startedAt;
        this.endAt = builder.endAt;
        this.cpuCount = builder.cpuCount;
        this.memoryMB = builder.memoryMB;
        this.diskSizeMB = builder.diskSizeMB;
        this.envdVersion = builder.envdVersion;
        this.state = builder.state;
        this.metadata = builder.metadata != null
            ? Collections.unmodifiableMap(new HashMap<>(builder.metadata)) : null;
        this.volumeMounts = builder.volumeMounts != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.volumeMounts)) : null;
        this.envdAccessToken = builder.envdAccessToken;
        this.domain = builder.domain;
        this.allowInternetAccess = builder.allowInternetAccess;
        this.network = builder.network;
        this.lifecycle = builder.lifecycle;
    }

    static SandboxInfo fromListResponse(SandboxesGet200ResponseInner sb) {
        return new Builder()
            .sandboxID(sb.getSandboxID())
            .templateID(sb.getTemplateID())
            .alias(sb.getAlias())
            .clientID(sb.getClientID())
            .startedAt(sb.getStartedAt())
            .endAt(sb.getEndAt())
            .cpuCount(sb.getCpuCount())
            .memoryMB(sb.getMemoryMB())
            .diskSizeMB(sb.getDiskSizeMB())
            .envdVersion(sb.getEnvdVersion())
            .state(sb.getState() != null ? sb.getState().getValue() : null)
            .metadata(sb.getMetadata())
            .volumeMounts(sb.getVolumeMounts())
            .build();
    }

    static SandboxInfo fromDetail(SandboxDetail resp) {
        return new Builder()
            .sandboxID(resp.getSandboxID())
            .templateID(resp.getTemplateID())
            .alias(resp.getAlias())
            .clientID(resp.getClientID())
            .startedAt(resp.getStartedAt())
            .endAt(resp.getEndAt())
            .cpuCount(resp.getCpuCount())
            .memoryMB(resp.getMemoryMB())
            .diskSizeMB(resp.getDiskSizeMB())
            .envdVersion(resp.getEnvdVersion())
            .state(resp.getState() != null ? resp.getState().getValue() : null)
            .metadata(resp.getMetadata())
            .volumeMounts(resp.getVolumeMounts())
            .envdAccessToken(resp.getEnvdAccessToken())
            .domain(resp.getDomain())
            .allowInternetAccess(resp.getAllowInternetAccess())
            .network(resp.getNetwork())
            .lifecycle(resp.getLifecycle())
            .build();
    }

    // Getters only — no setters, immutable by design

    public String getSandboxID() { return sandboxID; }

    public String getTemplateID() { return templateID; }

    public String getAlias() { return alias; }

    public String getClientID() { return clientID; }

    public OffsetDateTime getStartedAt() { return startedAt; }

    public OffsetDateTime getEndAt() { return endAt; }

    public Integer getCpuCount() { return cpuCount; }

    public Integer getMemoryMB() { return memoryMB; }

    public Integer getDiskSizeMB() { return diskSizeMB; }

    public String getEnvdVersion() { return envdVersion; }

    public Map<String, String> getMetadata() { return metadata; }

    public String getState() { return state; }

    public List<SandboxVolumeMount> getVolumeMounts() { return volumeMounts; }

    public String getEnvdAccessToken() { return envdAccessToken; }

    public String getDomain() { return domain; }

    public Boolean getAllowInternetAccess() { return allowInternetAccess; }

    public SandboxNetworkConfig getNetwork() { return network; }

    public SandboxLifecycle getLifecycle() { return lifecycle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        SandboxInfo that = (SandboxInfo) o;
        return Objects.equals(sandboxID, that.sandboxID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sandboxID);
    }

    @Override
    public String toString() {
        return "SandboxInfo{" +
            "sandboxID='" + sandboxID + '\'' +
            ", templateID='" + templateID + '\'' +
            ", alias='" + alias + '\'' +
            ", state='" + state + '\'' +
            ", cpuCount=" + cpuCount +
            ", memoryMB=" + memoryMB +
            ", diskSizeMB=" + diskSizeMB +
            '}';
    }

    /**
     * Builder for constructing SandboxInfo instances.
     */
    public static class Builder {
        private String sandboxID;
        private String templateID;
        private String alias;
        private String clientID;
        private OffsetDateTime startedAt;
        private OffsetDateTime endAt;
        private Integer cpuCount;
        private Integer memoryMB;
        private Integer diskSizeMB;
        private String envdVersion;
        private Map<String, String> metadata;
        private String state;
        private List<SandboxVolumeMount> volumeMounts;
        private String envdAccessToken;
        private String domain;
        private Boolean allowInternetAccess;
        private SandboxNetworkConfig network;
        private SandboxLifecycle lifecycle;

        public Builder sandboxID(String sandboxID) {
            this.sandboxID = sandboxID;
            return this;
        }

        public Builder templateID(String templateID) {
            this.templateID = templateID;
            return this;
        }

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder clientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public Builder startedAt(OffsetDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder endAt(OffsetDateTime endAt) {
            this.endAt = endAt;
            return this;
        }

        public Builder cpuCount(Integer cpuCount) {
            this.cpuCount = cpuCount;
            return this;
        }

        public Builder memoryMB(Integer memoryMB) {
            this.memoryMB = memoryMB;
            return this;
        }

        public Builder diskSizeMB(Integer diskSizeMB) {
            this.diskSizeMB = diskSizeMB;
            return this;
        }

        public Builder envdVersion(String envdVersion) {
            this.envdVersion = envdVersion;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder volumeMounts(List<SandboxVolumeMount> volumeMounts) {
            this.volumeMounts = volumeMounts;
            return this;
        }

        public Builder envdAccessToken(String envdAccessToken) {
            this.envdAccessToken = envdAccessToken;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder allowInternetAccess(Boolean allowInternetAccess) {
            this.allowInternetAccess = allowInternetAccess;
            return this;
        }

        public Builder network(SandboxNetworkConfig network) {
            this.network = network;
            return this;
        }

        public Builder lifecycle(SandboxLifecycle lifecycle) {
            this.lifecycle = lifecycle;
            return this;
        }

        public SandboxInfo build() {
            return new SandboxInfo(this);
        }
    }
}
