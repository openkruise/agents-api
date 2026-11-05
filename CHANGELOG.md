# Changelog

All notable changes to the Go and Java e2b/runtime SDKs are documented in
this file. The format is based on [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Breaking changes

- **Go — `runtime.RunOpts.Background` field removed** (`runtime/commands.go`).
  The field was declared but never read by `Start`/`Run`/`Wait` — a no-op
  documented as "reserved". Foreground/background behavior is expressed by
  the method choice: `Commands.Run` waits for completion, `Commands.Start`
  returns a `CommandHandle`. Code compiled against `v0.4.0-alpha3` through
  `v0.6.0-alpha2` that references the field must drop it to compile against
  this version. The field had no runtime effect, so no behavioral migration
  is needed.
- **Java — `CommandHandle.waitForCompletion()` now throws on truncated
  streams** (`runtime/java .../commands/CommandHandle.java`). When the
  command stream ends without an end event, it previously returned the
  partial output silently; it now throws a `SandboxException` describing
  the truncation (canceled, failed, or ended without an end event). This
  matches the official E2B `wait()` contract. The foreground
  `Commands.run(String)` and `Commands.run(String, RunOptions)` overloads
  delegate to `waitForCompletion()`, so they propagate the same
  `SandboxException`. Consumers relying on partial
  results from broken streams must handle the exception.
- **Java — `CommandHandle.waitForCompletion()` now throws
  `CommandExitException` on non-zero exit codes** (`runtime/java
  .../commands/CommandHandle.java`, new class `CommandExitException`).
  The official E2B SDKs throw on non-zero exits (Python
  `CommandExitException`, JS `CommandExitError`); the Go SDK of this
  repository returns `CommandExitError`. The Java side previously
  returned the result for any completed command. The exception carries
  the full result (stdout, stderr, exit code) via getters, matching the
  Go shape. The foreground `Commands.run(String)` and
  `Commands.run(String, RunOptions)` overloads delegate to
  `waitForCompletion()`, so they throw `CommandExitException` on a
  non-zero exit as well. Consumers reading the exit code from the
  returned result — including from `run()` — must catch the exception
  instead.
- **Go — `runtime.NewCommandHandle` gains a `cancelStream func()`
  parameter** (`runtime/command_handle.go`). The exported constructor went
  from three to four parameters so the handle can release the child
  context the output stream runs on when `Wait` completes or
  `Disconnect` is called. Source-level break for external callers
  constructing handles directly; the supported path —
  `Commands.Start` / `Commands.ConnectToProcess` — is unaffected, and
  `NewCommandHandleFromConnect` keeps its signature.

### Added

- Java: the first behavior test suite for the command paths
  (`CommandsTest`, runtime/java and its embedded copy in e2b/java). An
  in-process mock envd speaking the same Connect Protocol wire format as
  the Go-side httptest suites locks in: truncated streams fail loudly,
  failed waits are sticky, kill() stays usable after a failed wait,
  non-zero exit codes throw with the result attached, the wait for a
  command to start is bounded for both Start and Connect, and
  `disconnect()` never issues a kill.
- Go: `Commands.StreamRPC` field and `NewCommandsWithStreaming` constructor
  — the entry points for the streaming-client split behind the
  Start/ConnectToProcess migration (see Fixed). `Commands` now carries a
  second client dedicated to server-streaming RPCs (no overall HTTP
  timeout); `NewCommands` keeps its signature and wires the same client
  into both roles, so existing callers are unaffected, and a struct
  literal without `StreamRPC` falls back to `Rpc` instead of panicking.
- Per-command options aligning with the official E2B `CommandStartOpts`:
  - `RunOpts.Shell` (Go) — override the default `/bin/bash` shell. New in
    Go; aligns with the existing Java `RunOptions.shell()`, which shipped
    with #62 (run-as-user) and is unchanged in this release.
  - `RunOpts.Timeout` (Go) / `RunOptions.timeoutMs` (Java) — limit the whole
    command stream; on expiry the stream is cut with a clear error while the
    process keeps running in the sandbox. Zero or unset means no limit (the
    official SDK defaults to 60s; set it explicitly if you want that);
    Java accepts `timeoutMs(0)` as the explicit no-limit form, matching the
    official `timeout_to_ms`.
  - `RunOpts.RequestTimeout` (Go) / `RunOptions.requestTimeoutMs` (Java) —
    limit the wait for the command to start; falls back to the client-level
    request timeout. Java: `0` disables the wait, matching the official
    explicit-disable semantics. Go: zero falls back to the client level
    and there is no explicit-disable value; to relax the wait, set
    `RequestTimeout` itself to a larger value (a context deadline does
    not affect this wait). Note: a client-level request timeout of
    zero disables the timeout for single requests (Go), while the wait
    for a command to start still falls back to its default — see
    `WithRequestTimeout`.
  - `CommandHandle.disconnect()` (Java) — stop reading command output
    without killing the process, the same operation the official E2B
    handle exposes as `disconnect()` (the Go SDK names it
    `CommandHandle.Disconnect()`). Dropped-stream waits fail with a
    distinct disconnected-stream error.
  - Per-connect options: `ConnectOpts` (Go) / `Commands.ConnectOptions`
    (Java) — the official E2B `connect` offers the same two per-call
    limits as `start`, and previously only the client-level request
    timeout applied to the connect path. `ConnectOpts.RequestTimeout` /
    `ConnectOptions.requestTimeoutMs` bound the wait for the connect
    start event (Go zero falls back to the client level; Java `0`
    disables the wait, matching the Start path), and
    `ConnectOpts.Timeout` / `ConnectOptions.timeoutMs` bound the whole
    connect stream — previously the connect stream had no overall limit
    at all (zero or unset keeps it unlimited on both sides). On expiry
    the stream is cut with a clear error naming the
    option while the process keeps running in the sandbox.

### Fixed

- Go: `Commands.Start` and `Commands.ConnectToProcess` now run on the
  streaming client (see `Commands.StreamRPC` in Added). Previously both
  used the single-request client, whose overall HTTP timeout cut any long-running
  command stream or connect subscription at the client-level request
  timeout (60s by default) — a command producing output for longer than
  that failed with a stream error even though the process kept running
  in the sandbox.
- Java: `connect()` now bounds the wait for the connect confirmation
  (the same wait the Start path applies). Previously a
  server that accepted the connection but never answered blocked
  `connect()` forever — with no handle returned yet for the caller to
  cancel. The client-level `RuntimeConfig.requestTimeoutMs` applies (0
  disables the wait, matching the Start path); on expiry the process keeps
  running in the sandbox. Aligns Java with the Go `ConnectToProcess` and
  the official E2B `connect` `request_timeout` semantics.
- Go: `CommandHandle.Kill()` keeps working after the output stream was cut
  by a timeout or cancellation — the kill closure now uses a context
  detached from the stream. Java needed no change on this axis: `kill()`
  has always been an independent single request, unaffected by stream
  cancellation (`disconnect()` only stops reading output). The Java
  `kill()` semantics did change elsewhere in this release — its
  interaction with `waitForCompletion()` is now concurrency-safe and its
  unblocking semantics are pinned; see the next entry.
- Java: `kill()` racing a blocked `waitForCompletion()` is now
  concurrency-safe, and its semantics are pinned. Previously the kill
  claim could overwrite the real exit code with the synthetic -1, two
  concurrent kills could both return true, and a wait unblocked by the
  claim on a data frame threw the misleading sticky `Command ended
  without an end event`, discarding the real end event. Now the claim and
  the synthetic code are written under a lock shared with the end-event
  branch: a kill racing the final end event resolves deterministically.
  If the end event is processed first, kill() returns false and the real
  exit code is kept; if the kill claims first, a real end event still
  processed before the stream is released overwrites the synthetic code.
  The claim does not cancel the stream: a blocked wait is released
  the next time its read loop checks the claim, and it returns the
  synthetic -1 whenever it exits on the claim without having processed an
  end event — whether it broke on a non-end (e.g. data) frame while the
  claim was active, in which case a later end event is never read because
  the stream is closed on exit, or the stream ended with no end event (the
  one case where a stream that ended without an end event does not fail
  loudly). A real end event (e.g. exit code 137) wins, and throws
  `CommandExitException` like any non-zero end event, only when it is
  actually processed before the stream is released. If the stream never
  yields another frame (envd stall or crash, a dropped connection), the
  blocked wait stays blocked unless `timeoutMs` bounds it. Only one of N
  concurrent kill() calls returns true, and a failed kill RPC rolls the
  claim back (an `Error` thrown in the same window rolls back too). The
  rollback restores the completion state so the kill RPC can be retried,
  but it does not reopen the output stream: a concurrent waiter may have
  consumed a data frame while the RPC was in flight, observed the claim,
  and closed the stream before the failure, so a fresh
  `waitForCompletion()` after such a rollback is not guaranteed to be
  usable. A kill that finds no process to
  signal (envd's 404) now surfaces as an `IllegalStateException`
  ("Process <pid> not found; no signal was sent") and rolls the claim
  back instead of silently claiming completion. This reports the same
  real outcome the Go `handleKill` does, but not with the same calling
  contract: Go's `CommandHandle.Kill()` returns `false` and leaves the
  handle untouched, while Java throws so the caller cannot miss it, and
  rolls the claim back so the kill can be retried. Once the
  kill RPC settles successfully the claim is permanent, and a
  `waitForCompletion()` returning from
  the kill-then-wait fast path now releases the output stream instead
  of pinning the OkHttp connection until GC. Intentional divergence
  from Go: `CommandHandle.Kill()` in the Go SDK does not claim
  completion, so any subsequent `Wait` reads the
  real end event and fails with `CommandExitError` (e.g. code 137).
  The Java claim mainly changes what a `waitForCompletion()` call made
  after `kill()` sees — it returns the synthetic -1 immediately from
  the entry fast path instead of waiting for envd's end event; a wait
  already blocked when the claim lands returns the synthetic -1 unless
  the real end event happens to be the next frame it processes before
  the stream is released.
- Go/Java: timeout messages now name the source of the timeout
  (per-call option vs client-level fallback), and a caller's own
  context deadline expiring is no longer misattributed to
  `RunOpts.Timeout` (Go `Start` path). The Go `ConnectToProcess`
  path now also names `ConnectOpts.Timeout` when the deadline fires
  before the connect start event (the opening RPC or the first
  stream read), closing the last unnamed timeout window.
- Java: a `RunOptions.timeoutMs` expiry during `waitForCompletion()` now
  fails with `Timed out after Xms (RunOptions.timeoutMs; the process may
  still be running in the sandbox)` instead of a generic
  `Command stream failed` IO error — matching Go's `RunOpts.Timeout`
  attribution for consistent cross-language debugging. Detection is
  structural (the call's canceled state), not error-text matching, so a
  network error whose message happens to contain "timeout" is not
  misattributed. The same attribution now also covers an expiry before
  the start event in `runBackground()` (previously a generic
  `Failed to start process`), matching Go's `overallTimedOut`.
- Java: `runBackground()` and `connect()` now close the response stream
  exactly once on every failure path (previously each error branch closed
  manually and an unexpected exception could have left the connection
  open).
- Java: canceled request-timeout tasks are now removed from the shared
  scheduler's delay queue immediately (remove-on-cancel) instead of
  lingering until their original delay expires, each one pinning the
  captured OkHttp call — and through it the request/response — against
  GC. With large `RunOptions.requestTimeoutMs` /
  `ConnectOptions.requestTimeoutMs` values and a high call rate the old
  behavior retained calls linearly.
- Go: a `Commands` value built via struct literal (`&Commands{Rpc: ...}`)
  instead of the constructors no longer fails every Start/ConnectToProcess
  with `timed out after 0s` — the zero request timeout now falls back to
  the package default, matching `NewCommands`.
- Go: a `Commands` value built via struct literal (`&Commands{Rpc: ...}`
  without `StreamRPC`) no longer panics on a nil interface in
  Start/ConnectToProcess — before the streaming-client split this
  construction worked (both paths ran on `Rpc`), so the missing field
  now falls back to `Rpc`.
- Java: a mid-stream server error delivered in the Connect Protocol
  end-of-stream trailer now surfaces in `waitForCompletion()` as
  `Command stream failed: <server error>` instead of the misleading
  `Command ended without an end event` — with the message extracted
  from the trailer's `error.message` JSON field rather than reported
  as the raw JSON payload (a trailer without the expected shape falls
  back to the raw text). The same trailer check now also
  covers a server error delivered during connect stream setup:
  `connect()` reports `Failed to connect to process: <server error>`
  instead of the generic `no response from server` (the start path
  already consumed it; the Go side reads no trailer on either path, so
  this is Java-only fidelity, not a cross-language divergence).
  (`MessageStream` gained
  `getTrailerError()` and `getLastError()` as default methods — existing
  external implementors need no change; an implementation that does not
  track errors degrades gracefully to the "no end event" message.)
- Java: the startup-wait and overall-timeout errors thrown while opening a
  Start/Connect stream now carry the underlying OkHttp `IOException` as
  their cause, preserving the original exception for diagnostics; the
  message text is unchanged.
- Java: a failed `waitForCompletion()` is now sticky. Previously the
  `finally` block marked the handle completed even when the stream failed,
  so a second call silently returned a partial result (exit code -1) and
  `kill()` became a no-op — contradicting the fail-loudly contract. Now
  every subsequent call replays the same `SandboxException`, and `kill()`
  stays usable because the process may still be running in the sandbox.
  (`completed` is only set by a real end event or an explicit `kill()`.)
- Java: a recorded stream failure now wins over `kill()` in
  `waitForCompletion()`. Previously `kill()` after a failed wait marked
  the handle completed, and a subsequent `waitForCompletion()` silently
  returned a synthetic exit-code -1 result, swallowing the real stream
  failure. The official SDKs likewise let the stream error take
  priority over any result.
- Java: both SDK modules (runtime/java and its embedded copy in
  e2b/java) compile out of the box on modern JDKs again. The generated
  gRPC stubs reference `javax.annotation.Generated`, removed from the
  JDK since 11; the poms now carry `javax.annotation-api` (provided
  scope), so the 1.8 bytecode target builds on JDK 9+ without the
  end-user adding the dependency.
- Java: the end event's server-side error message is no longer dropped.
  The generated proto carries `EndEvent.error` (envd reports e.g. spawn
  failures there) and the official SDKs preserve it (Python
  `CommandResult.error`, JS `CommandExitError.error`, Go
  `CommandExitError.ErrorMessage`), but the Java side read only the
  exit code. `CommandResult` gained an `error` field (new 4-arg
  constructor; the existing 3-arg constructor keeps it empty), and
  `CommandExitException` exposes it via `getError()` with the message
  extended to the Go shape "command exited with code X: <error>".

### Known limitations

- Java: command output chunks are decoded to strings per data event
  (`ByteString.toStringUtf8`), not with an incremental UTF-8 decoder
  like the official SDKs. A multi-byte character split across two data
  frames surfaces as U+FFFD replacement characters — in both the
  streaming callbacks and the accumulated result. The Go side is
  unaffected (strings preserve the raw bytes and the buffer is only
  interpreted as a whole). A future fix would carry an incremental
  `java.nio.charset.CharsetDecoder` across frames.
- Go: when `RunOpts.Timeout` is set and the caller's own context deadline
  expires first during `Wait`, the propagated `DeadlineExceeded` is
  misattributed to `RunOpts.Timeout` (the `Start` path guards against this
  via a `ctx.Err()` check; the handle does not keep the caller's context).
  Only observable when both timeouts are in play and the caller's fires
  first. A future fix would snapshot the caller context on the handle.
- Go: `Disconnect()` has no dedicated error attribution. It cancels the
  child context the output stream runs on, so a concurrent or later `Wait`
  reports the generic `stream error: context canceled`, which is
  indistinguishable from the caller canceling its own context. The Java
  side tracks a `disconnected` flag and fails with the explicit
  `Command stream disconnected; the process may still be running in the
  sandbox`. A future fix would add the same flag to the Go handle.
- The streaming client does not yet send the official SDK's
  `Keepalive-Ping-Interval` header. Without periodic keepalive frames, an
  idle output stream (a long-running command producing no output) may be
  cut by an intermediate ingress/LB idle timeout (commonly 60–75s),
  surfacing as a stream error rather than a timeout. Whether the Kruise
  envd honors the header still needs verification; until then, keep
  long-idle consumers in mind when relying on unbounded streams.
