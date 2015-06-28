package org.kie.dockerui.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import org.kie.dockerui.shared.model.KieImageType;

public class KieContainerTypeSafeHtmlRenderer implements  SafeHtmlRenderer<KieImageType> {
    private static KieContainerTypeSafeHtmlRenderer instance;
    private static Template template;

    public static KieContainerTypeSafeHtmlRenderer getInstance() {
        if (template == null) {
            template = GWT.create(Template.class);
        }
        if (instance == null) {
            instance = new KieContainerTypeSafeHtmlRenderer();
        }
        return instance;
    }
    
    @Override
    public SafeHtml render(KieImageType type) {
        final SafeUri uri = ClientUtils.getImageUri(type);
        return template.img(uri.asString(), "32px","32px", type.getName());
    }

    @Override
    public void render(KieImageType type, SafeHtmlBuilder safeHtmlBuilder) {
        safeHtmlBuilder.append(render(type));
    }
    
    interface Template extends SafeHtmlTemplates {
        @com.google.gwt.safehtml.client.SafeHtmlTemplates.Template("<div style=\"float: left\" title=\"{3}\"><img style=\"display: block; background:url({0}) no-repeat 0px 0px ; background-size: {1} {2}; width: {1}; height: {2};\"/></div>")
        SafeHtml img(String url, String w, String h, String title);
    }
}
