package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Execution result event (text, html, image, etc.).
 */
public class ResultEvent extends ExecutionEvent {
    private final Result result;

    public ResultEvent(Result result) {
        super("result");
        this.result = result;
    }

    public Result getResult() { return result; }

    @Override
    public String toString() {
        return "ResultEvent{result=" + result + "}";
    }
}
