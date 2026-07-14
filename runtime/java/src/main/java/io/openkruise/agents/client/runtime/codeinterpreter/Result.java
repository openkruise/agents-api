package io.openkruise.agents.client.runtime.codeinterpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the data to be displayed as a result of executing code.
 * Similar to Jupyter notebook output structure.
 */
public class Result {
    private String text;
    private String html;
    private String markdown;
    private String svg;
    private String png;
    private String jpeg;
    private String pdf;
    private String latex;
    private Map<String, Object> json;
    private String javascript;
    private Map<String, Object> data;
    private boolean mainResult;
    private Map<String, Object> extra;

    public Result() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public String getPng() {
        return png;
    }

    public void setPng(String png) {
        this.png = png;
    }

    public String getJpeg() {
        return jpeg;
    }

    public void setJpeg(String jpeg) {
        this.jpeg = jpeg;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getLatex() {
        return latex;
    }

    public void setLatex(String latex) {
        this.latex = latex;
    }

    public Map<String, Object> getJson() {
        return json;
    }

    public void setJson(Map<String, Object> json) {
        this.json = json;
    }

    public String getJavascript() {
        return javascript;
    }

    public void setJavascript(String javascript) {
        this.javascript = javascript;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public boolean isMainResult() {
        return mainResult;
    }

    public void setMainResult(boolean mainResult) {
        this.mainResult = mainResult;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        if (text != null) {
            return text;
        }
        return "Result(formats: " + getFormats() + ")";
    }

    public List<String> getFormats() {
        List<String> formats = new ArrayList<>();
        if (text != null) {formats.add("text");}
        if (html != null) {formats.add("html");}
        if (markdown != null) {formats.add("markdown");}
        if (svg != null) {formats.add("svg");}
        if (png != null) {formats.add("png");}
        if (jpeg != null) {formats.add("jpeg");}
        if (pdf != null) {formats.add("pdf");}
        if (latex != null) {formats.add("latex");}
        if (json != null) {formats.add("json");}
        if (javascript != null) {formats.add("javascript");}
        if (data != null) {formats.add("data");}
        return formats;
    }
}
