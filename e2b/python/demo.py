import os

# E2B_API_KEY and E2B_DOMAIN are read from environment variables (set in env.sh)
# validate_key=False skips E2B SDK's local key format check (e2b>=2.25.0 enforces e2b_+hex)
# The backend will handle actual authentication

# Import and patch the E2B SDK
import time
from e2b_code_interpreter import Sandbox
from kruise_agents.patch_e2b import patch_e2b
from kruise_agents.patch_traffic_token import patch_traffic_access_token

# Patch 1: protocol conversion (E2B native routing -> OpenKruise private path prefix)
# https=True when SSL_CERT_FILE is set; validate_key=False bypasses E2B's local key format check
use_https = bool(os.environ.get("SSL_CERT_FILE"))
patch_e2b(use_https, validate_key=False)

# Patch 2: Traffic JWT auto-refresh (requires e2b>=2.35.0,<2.38.0)
# This injects token refresh into envd gRPC interceptors, HTTP event hooks, and Jupyter client
patch_traffic_access_token()

    
sandbox: Sandbox = Sandbox.create(
    template="code-interpreter",
    timeout=600,
    metadata={"test_case": "test_pause_connect_kill"},
)
print(f"sandbox created: {sandbox.sandbox_id}")

# Test envd gRPC data plane (commands go through envd gRPC interceptors with traffic token)
result = sandbox.commands.run("echo 'hello from sandbox'")
print(f"command output: {result.stdout}")

# Test Jupyter HTTP data plane (code interpreter goes through Jupyter client with traffic token)
execution = sandbox.run_code("print('hello from jupyter')")
print(f"jupyter output: {''.join(execution.logs.stdout)}")

sandbox.pause(request_timeout=300)
print(f"sandbox {sandbox.sandbox_id} paused, wait 30s before resuming")
time.sleep(30)

input("press Enter to connect (resume) the sandbox...")
# request_timeout: HTTP wait for the connect (resume) call, default 60s — resume
# re-schedules the sandbox pod, so give it room. NOTE: connect(timeout=...) is
# a DIFFERENT knob — the sandbox lifetime after resume, not the wait time.
sandbox.connect(request_timeout=300)
print("sandbox resumed")

# Verify data plane still works after resume (token should refresh if expired)
result = sandbox.commands.run("echo 'hello after resume'")
print(f"command output after resume: {result.stdout}")

# envd (the commands channel) boots before the pod turns Ready, but the Jupyter
# server behind run_code (sandbox port 49999) needs a few more seconds: resume
# recreates the pod, so Jupyter starts fresh. The upstream E2E suite hides this
# window with retries (test/e2b/utils.py run_code_sandbox: 5 attempts, 5s apart);
# a single shot right after resume can hit the gateway's 503 "connection refused".
execution = None
for attempt in range(5):
    try:
        execution = sandbox.run_code("print('hello from jupyter after resume')")
        break
    except Exception as exc:
        print(f"jupyter not ready yet (attempt {attempt + 1}/5): {exc}")
        if attempt == 0:
            ports = sandbox.commands.run(
                "ss -ltn 2>/dev/null || netstat -ltn 2>/dev/null || true",
                timeout=10,
            ).stdout
            print(f"    sandbox listening ports (49999 = jupyter):\n{ports}")
        time.sleep(5)
if execution is None:
    raise RuntimeError("jupyter did not come back after resume")
print(f"jupyter output after resume: {''.join(execution.logs.stdout)}")

input("press Enter to kill the sandbox...")
sandbox.kill()
print(f"sandbox {sandbox.sandbox_id} killed")
