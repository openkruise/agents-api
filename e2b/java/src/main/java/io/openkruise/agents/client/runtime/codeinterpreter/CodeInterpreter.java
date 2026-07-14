package io.openkruise.agents.client.runtime.codeinterpreter;

import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Code Interpreter for executing code in sandbox.
 * Uses port 49999 (vs envd port 49983) for code execution.
 * Response is NDJSON stream: each line is a separate JSON event.
 */
public class CodeInterpreter {
    private static final Logger log = LoggerFactory.getLogger(CodeInterpreter.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final String sandboxID;
    private final RuntimeConfig runtimeConfig;
    private final OkHttpClient httpClient;

    public CodeInterpreter(String sandboxID, RuntimeConfig runtimeConfig) {
        this.sandboxID = sandboxID;
        this.runtimeConfig = runtimeConfig;
        this.httpClient = runtimeConfig.getOrCreateStreamingHttpClient();
    }

    /**
     * Execute Python code in the sandbox (default language).
     *
     * @param code The Python code to execute
     * @return Execution result containing stdout, stderr, results, and error
     * @throws SandboxException if execution fails
     */
    public Execution runCode(String code) throws SandboxException {
        return runCode(code, RunCodeLanguage.PYTHON.getValue());
    }

    /**
     * Execute code in the sandbox (blocking, collects all events into Execution).
     *
     * @param code     The code to execute
     * @param language The programming language (python, javascript, typescript, r, java, bash)
     * @return Execution result containing stdout, stderr, results, and error
     * @throws SandboxException if execution fails
     */
    public Execution runCode(String code, String language) throws SandboxException {
        return runCode(new RunCodeRequest(code, language));
    }

    /**
     * Execute code in the sandbox with options (blocking, collects all events into Execution).
     * If options contain callbacks (onStdout/onStderr/onResult/onError), they will be invoked in real-time.
     *
     * @param code     The code to execute
     * @param language The programming language
     * @param options  Execution options (working directory, environment variables, callbacks, etc.)
     * @return Execution result
     * @throws SandboxException if execution fails
     */
    public Execution runCode(String code, String language, RunCodeOptions options) throws SandboxException {
        RunCodeRequest request = new RunCodeRequest(code, language, options);
        Execution execution = new Execution();

        Consumer<ExecutionEvent> handler = event -> {
            applyEventToExecution(execution, event);
            if (options != null) {
                invokeUserCallbacks(event, options);
            }
        };

        runCodeStreaming(request, handler);
        log.debug("Code execution completed, results: {}, error: {}",
            execution.getResults().size(), execution.getError() != null);
        return execution;
    }

    /**
     * Execute code using a RunCodeRequest (blocking, collects all events into Execution).
     */
    public Execution runCode(RunCodeRequest request) throws SandboxException {
        log.debug("Executing code in sandbox {}, language: {}", sandboxID, request.getLanguage());
        Execution execution = new Execution();
        runCodeStreaming(request, event -> applyEventToExecution(execution, event));
        log.debug("Code execution completed, results: {}, error: {}",
            execution.getResults().size(), execution.getError() != null);
        return execution;
    }

    /**
     * Invoke user-defined callbacks from RunCodeOptions based on event type.
     */
    private void invokeUserCallbacks(ExecutionEvent event, RunCodeOptions options) {
        if (event instanceof StdoutEvent && options.getOnStdout() != null) {
            options.getOnStdout().accept((StdoutEvent) event);
        } else if (event instanceof StderrEvent && options.getOnStderr() != null) {
            options.getOnStderr().accept((StderrEvent) event);
        } else if (event instanceof ResultEvent && options.getOnResult() != null) {
            options.getOnResult().accept(((ResultEvent) event).getResult());
        } else if (event instanceof ErrorEvent && options.getOnError() != null) {
            options.getOnError().accept(((ErrorEvent) event).getError());
        }
    }

    /**
     * Execute code and process events as they arrive (streaming, low memory footprint).
     *
     * @param request      The code execution request
     * @param eventHandler Callback to handle each event
     * @throws SandboxException if execution fails
     */
    public void runCodeStreaming(RunCodeRequest request, Consumer<ExecutionEvent> eventHandler)
        throws SandboxException {
        String url = runtimeConfig.getCodeInterpreterURL(sandboxID) + "/execute";
        log.debug("Sending streaming request to: {}", url);

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
            .url(url)
            .post(RequestBody.create(request.toJson(), JSON_MEDIA_TYPE))
            .build();
        httpRequest = addCodeInterpreterHeaders(httpRequest);

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new SandboxException("Code execution failed: " + response.code() + " - " + errorBody);
            }

            ResponseBody body = response.body();
            if (body == null) {
                return;
            }

            // Parse NDJSON stream
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {continue;}
                    ExecutionEvent event = parseNDJSONLine(line);
                    if (event != null) {
                        eventHandler.accept(event);
                    }
                }
            }

        } catch (IOException e) {
            throw new SandboxException("Failed to execute code", e);
        }
    }

    /**
     * Apply a stream event to the Execution object.
     */
    private void applyEventToExecution(Execution execution, ExecutionEvent event) {
        if (event instanceof NumberOfExecutionsEvent) {
            execution.setExecutionCount(((NumberOfExecutionsEvent)event).getExecutionCount());
        } else if (event instanceof StdoutEvent) {
            execution.getLogs().getStdout().add(((StdoutEvent)event).getText());
        } else if (event instanceof StderrEvent) {
            execution.getLogs().getStderr().add(((StderrEvent)event).getText());
        } else if (event instanceof ResultEvent) {
            execution.getResults().add(((ResultEvent)event).getResult());
        } else if (event instanceof ErrorEvent) {
            execution.setError(((ErrorEvent)event).getError());
        }
    }

    /**
     * Parse a single NDJSON line into an ExecutionEvent.
     */
    private ExecutionEvent parseNDJSONLine(String line) {
        try {
            JsonObject json = JsonParser.parseString(line).getAsJsonObject();
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type) {
                case "number_of_executions":
                    int count = json.has("execution_count") ? json.get("execution_count").getAsInt() : 0;
                    return new NumberOfExecutionsEvent(count);

                case "stdout":
                    String stdoutText = json.has("text") ? json.get("text").getAsString() : "";
                    String stdoutTimestamp = json.has("timestamp") ? json.get("timestamp").getAsString() : "";
                    return new StdoutEvent(stdoutText, stdoutTimestamp);

                case "stderr":
                    String stderrText = json.has("text") ? json.get("text").getAsString() : "";
                    String stderrTimestamp = json.has("timestamp") ? json.get("timestamp").getAsString() : "";
                    return new StderrEvent(stderrText, stderrTimestamp);

                case "result":
                    return new ResultEvent(parseResult(json));

                case "error":
                    String name = json.has("name") ? json.get("name").getAsString() : "";
                    String value = json.has("value") ? json.get("value").getAsString() : "";
                    String traceback = json.has("traceback") ? json.get("traceback").getAsString() : "";
                    return new ErrorEvent(new ExecutionError(name, value, traceback));

                case "end_of_execution":
                    return new EndOfExecutionEvent();

                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Failed to parse NDJSON line: {}, error: {}", line, e.getMessage());
            return null;
        }
    }

    /**
     * Parse a result object from JSON fields.
     */
    private Result parseResult(JsonObject json) {
        Result result = new Result();

        if (json.has("text")) {result.setText(json.get("text").getAsString());}
        if (json.has("html")) {result.setHtml(json.get("html").getAsString());}
        if (json.has("markdown")) {result.setMarkdown(json.get("markdown").getAsString());}
        if (json.has("svg")) {result.setSvg(json.get("svg").getAsString());}
        if (json.has("png")) {result.setPng(json.get("png").getAsString());}
        if (json.has("jpeg")) {result.setJpeg(json.get("jpeg").getAsString());}
        if (json.has("pdf")) {result.setPdf(json.get("pdf").getAsString());}
        if (json.has("latex")) {result.setLatex(json.get("latex").getAsString());}
        if (json.has("javascript")) {result.setJavascript(json.get("javascript").getAsString());}

        if (json.has("json") && json.get("json").isJsonObject()) {
            result.setJson(parseJsonObject(json.getAsJsonObject("json")));
        }
        if (json.has("data") && json.get("data").isJsonObject()) {
            result.setData(parseJsonObject(json.getAsJsonObject("data")));
        }
        if (json.has("extra") && json.get("extra").isJsonObject()) {
            result.setExtra(parseJsonObject(json.getAsJsonObject("extra")));
        }
        if (json.has("is_main_result")) {
            result.setMainResult(json.get("is_main_result").getAsBoolean());
        }

        return result;
    }

    /**
     * Parse a JSON object into a Map.
     */
    private Map<String, Object> parseJsonObject(JsonObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                if (value.getAsJsonPrimitive().isString()) {
                    map.put(entry.getKey(), value.getAsString());
                } else if (value.getAsJsonPrimitive().isNumber()) {
                    map.put(entry.getKey(), value.getAsNumber());
                } else if (value.getAsJsonPrimitive().isBoolean()) {
                    map.put(entry.getKey(), value.getAsBoolean());
                }
            } else if (value.isJsonObject()) {
                map.put(entry.getKey(), parseJsonObject(value.getAsJsonObject()));
            } else if (value.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonElement element : value.getAsJsonArray()) {
                    if (element.isJsonPrimitive()) {
                        if (element.getAsJsonPrimitive().isString()) {
                            list.add(element.getAsString());
                        } else if (element.getAsJsonPrimitive().isNumber()) {
                            list.add(element.getAsNumber());
                        } else if (element.getAsJsonPrimitive().isBoolean()) {
                            list.add(element.getAsBoolean());
                        }
                    } else if (element.isJsonObject()) {
                        list.add(parseJsonObject(element.getAsJsonObject()));
                    } else if (element.isJsonNull()) {
                        list.add(null);
                    }
                }
                map.put(entry.getKey(), list);
            } else if (value.isJsonNull()) {
                map.put(entry.getKey(), null);
            }
        }
        return map;
    }

    private Request addCodeInterpreterHeaders(Request original) {
        Map<String, String> hdrs = runtimeConfig.getCodeInterpreterHeaders(sandboxID);
        Request.Builder builder = original.newBuilder();
        for (Map.Entry<String, String> entry : hdrs.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * Create a new code context with specified working directory and language.
     *
     * @param cwd      Working directory for the context (optional, defaults to /home/user)
     * @param language Programming language for the context (optional, defaults to python)
     * @return Context object with ID, language, and cwd
     * @throws SandboxException if creation fails
     */
    public Context createCodeContext(String cwd, String language) throws SandboxException {
        String url = runtimeConfig.getCodeInterpreterURL(sandboxID) + "/contexts";
        log.debug("Creating code context at: {}, cwd: {}, language: {}", url, cwd, language);

        JsonObject json = new JsonObject();
        if (cwd != null) {
            json.addProperty("cwd", cwd);
        }
        if (language != null) {
            json.addProperty("language", language);
        }

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
            .url(url)
            .post(RequestBody.create(json.toString(), JSON_MEDIA_TYPE))
            .build();
        httpRequest = addCodeInterpreterHeaders(httpRequest);

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new SandboxException("Create context failed: " + response.code() + " - " + errorBody);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new SandboxException("Create context failed: empty response");
            }

            JsonObject respJson = JsonParser.parseString(body.string()).getAsJsonObject();
            String id = respJson.has("id") ? respJson.get("id").getAsString() : "";
            String lang = respJson.has("language") ? respJson.get("language").getAsString() : "python";
            String contextCwd = respJson.has("cwd") ? respJson.get("cwd").getAsString() : "/home/user";

            return new Context(id, lang, contextCwd);
        } catch (IOException e) {
            throw new SandboxException("Failed to create context", e);
        }
    }

    /**
     * Remove a code context by ID.
     *
     * @param contextId Context ID to remove
     * @throws SandboxException if removal fails
     */
    public void removeCodeContext(String contextId) throws SandboxException {
        String url = runtimeConfig.getCodeInterpreterURL(sandboxID) + "/contexts/" + contextId;
        log.debug("Removing code context: {}", url);

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
            .url(url)
            .delete()
            .build();
        httpRequest = addCodeInterpreterHeaders(httpRequest);

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new SandboxException("Remove context failed: " + response.code() + " - " + errorBody);
            }
        } catch (IOException e) {
            throw new SandboxException("Failed to remove context", e);
        }
    }

    /**
     * List all code contexts.
     *
     * @return List of Context objects
     * @throws SandboxException if listing fails
     */
    public List<Context> listCodeContexts() throws SandboxException {
        String url = runtimeConfig.getCodeInterpreterURL(sandboxID) + "/contexts";
        log.debug("Listing code contexts: {}", url);

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
            .url(url)
            .get()
            .build();
        httpRequest = addCodeInterpreterHeaders(httpRequest);

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new SandboxException("List contexts failed: " + response.code() + " - " + errorBody);
            }

            ResponseBody body = response.body();
            if (body == null) {
                return new ArrayList<>();
            }

            List<Context> contexts = new ArrayList<>();
            com.google.gson.JsonArray arr = JsonParser.parseString(body.string()).getAsJsonArray();
            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.has("id") ? obj.get("id").getAsString() : "";
                String lang = obj.has("language") ? obj.get("language").getAsString() : "python";
                String cwd = obj.has("cwd") ? obj.get("cwd").getAsString() : "/home/user";
                contexts.add(new Context(id, lang, cwd));
            }
            return contexts;
        } catch (IOException e) {
            throw new SandboxException("Failed to list contexts", e);
        }
    }
}
