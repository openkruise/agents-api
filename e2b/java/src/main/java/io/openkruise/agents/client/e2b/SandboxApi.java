package io.openkruise.agents.client.e2b;

import io.openkruise.agents.client.e2b.api.SandboxesApi;
import io.openkruise.agents.client.e2b.api.invoker.ApiException;
import io.openkruise.agents.client.e2b.api.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages sandbox lifecycle (control plane): create, connect, list, kill, pause, etc.
 */
public class SandboxApi {

    private static final Logger LOG = Logger.getLogger(SandboxApi.class.getName());

    private final ConnectionConfig config;
    private final SandboxesApi lowLevelApi;

    public SandboxApi(ConnectionConfig config) {
        this.config = config;
        this.lowLevelApi = config.getOrCreateSandboxesApi();
    }

    public Sandbox create(NewSandbox body) throws ApiException {
        if (body.getTemplateID() == null || body.getTemplateID().isEmpty()) {
            body.setTemplateID("code-interpreter");
        }
        if (body.getTimeout() == null || body.getTimeout() <= 0) {
            body.setTimeout(ConnectionConfig.DEFAULT_SANDBOX_TIMEOUT);
        }

        SandboxResponse resp = lowLevelApi.sandboxesPost(body);

        return new Sandbox(resp.getSandboxID(), resp.getEnvdAccessToken(), config);
    }

    public Sandbox create(String template) throws ApiException {
        return create(new NewSandbox().templateID(template).timeout(ConnectionConfig.DEFAULT_SANDBOX_TIMEOUT));
    }

    public Sandbox connect(String sandboxID, int timeout) throws ApiException {
        if (timeout <= 0) {
            timeout = ConnectionConfig.DEFAULT_SANDBOX_TIMEOUT;
        }
        try {
            ConnectSandbox body = new ConnectSandbox();
            body.setTimeout(timeout);
            SandboxResponse resp = lowLevelApi.sandboxesSandboxIDConnectPost(sandboxID, body);

            return new Sandbox(resp.getSandboxID(), resp.getEnvdAccessToken(), config);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new SandboxNotFoundException(sandboxID, e);
            }
            throw e;
        }
    }

    public Sandbox connect(String sandboxID) throws ApiException {
        return connect(sandboxID, 0);
    }

    public List<SandboxInfo> list() throws ApiException {
        return list(null);
    }

    /**
     * Lists sandboxes filtered by metadata.
     */
    public List<SandboxInfo> list(String metadata) throws ApiException {
        return list(metadata, null, null, null);
    }

    /**
     * Lists sandboxes (v2) with support for metadata, state filtering, and pagination.
     */
    public List<SandboxInfo> list(String metadata, List<SandboxState> state,
        String nextToken, Integer limit) throws ApiException {
        List<SandboxesGet200ResponseInner> resp = lowLevelApi.v2SandboxesGet(metadata, state, nextToken, limit);
        List<SandboxInfo> result = new ArrayList<>();
        if (resp == null) {
            return result;
        }
        for (SandboxesGet200ResponseInner sb : resp) {
            result.add(toSandboxInfo(sb));
        }
        return result;
    }

    public SandboxInfo getInfo(String sandboxID) throws ApiException {
        try {
            SandboxDetail resp = lowLevelApi.sandboxesSandboxIDGet(sandboxID);
            return SandboxInfo.fromDetail(resp);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new SandboxNotFoundException(sandboxID, e);
            }
            throw e;
        }
    }

    /**
     * Terminates a sandbox. Returns true if successful, false if not found.
     */
    public boolean kill(String sandboxID) throws ApiException {
        if (config.isDebug()) {
            LOG.log(Level.WARNING, "Debug mode: skipping kill for sandbox {0}", sandboxID);
            return true;
        }
        try {
            lowLevelApi.sandboxesSandboxIDDelete(sandboxID);
            return true;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Pauses a sandbox. 409 Conflict is treated as already paused.
     */
    public String pause(String sandboxID) throws ApiException {
        try {
            lowLevelApi.sandboxesSandboxIDPausePost(sandboxID);
            return sandboxID;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new SandboxNotFoundException(sandboxID, e);
            }
            // 409 Conflict indicates already paused, treat as success
            if (e.getCode() == 409) {
                return sandboxID;
            }
            throw e;
        }
    }

    public void setTimeout(String sandboxID, int timeout) throws ApiException {
        if (config.isDebug()) {
            LOG.log(Level.WARNING, "Debug mode: skipping setTimeout({0}s) for sandbox {1}",
                new Object[] {timeout, sandboxID});
            return;
        }
        try {
            SandboxesSandboxIDTimeoutPostRequest body = new SandboxesSandboxIDTimeoutPostRequest();
            body.setTimeout(timeout);
            lowLevelApi.sandboxesSandboxIDTimeoutPost(sandboxID, body);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new SandboxNotFoundException(sandboxID, e);
            }
            throw e;
        }
    }

    private SandboxInfo toSandboxInfo(SandboxesGet200ResponseInner sb) {
        return SandboxInfo.fromListResponse(sb);
    }
}
