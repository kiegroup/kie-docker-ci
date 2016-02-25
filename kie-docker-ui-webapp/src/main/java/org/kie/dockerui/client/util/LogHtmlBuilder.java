package org.kie.dockerui.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public class LogHtmlBuilder {

    private final static RegExp SEVERE_PATTERN = RegExp.compile(".*\\sSEVERE\\s.*");
    private final static RegExp ERROR_PATTERN = RegExp.compile(".*\\sERROR\\s.*");
    private final static RegExp WARN_PATTERN = RegExp.compile(".*\\sWARN\\s.*");

    interface Template extends SafeHtmlTemplates {
        @com.google.gwt.safehtml.client.SafeHtmlTemplates.Template("<p style=\"color: {1}; line-height: {2};\">{0}</p><br>")
        SafeHtml logLine(String line, String color, String lineHeight);
    }

    private static final Template lineLogTemplate = GWT.create(Template.class);
    private final StringBuilder sb = new StringBuilder();
    private String lineHeight = "20px";

    public LogHtmlBuilder() {
    }

    public void setLineHeight(final String lineHeight) {
        this.lineHeight = lineHeight;
    }

    public LogHtmlBuilder append(String logText) {

        if (logText != null) {
            final String[] logLines = logText.split("\n");
            for (final String logLine : logLines) {
                String color = "white";
                if (WARN_PATTERN.test(logLine)) {
                    color = "yellow";
                } else if (ERROR_PATTERN.test(logLine)) {
                    color = "red";
                } else if (SEVERE_PATTERN.test(logLine)) {
                    color = "red";
                }
                this.sb.append(lineLogTemplate.logLine(logLine, color, lineHeight).asString());
            }
        }
        
        return this;
    }

    public SafeHtml toSafeHtml() {
        return new SafeHtml() {
            @Override
            public String asString() {
                return sb.toString();
            }
        };
    }

}
