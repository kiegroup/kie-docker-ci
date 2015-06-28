package org.kie.dockerui.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import org.kie.dockerui.shared.model.KieContainer;

public class KieContainerSafeHtmlRenderer implements  SafeHtmlRenderer<KieContainer> {
    private static KieContainerSafeHtmlRenderer instance;
    private static Template template;

    public static KieContainerSafeHtmlRenderer getInstance() {
        if (template == null) {
            template = GWT.create(Template.class);
        }
        if (instance == null) {
            instance = new KieContainerSafeHtmlRenderer();
        }
        return instance;
    }
    
    @Override
    public SafeHtml render(final KieContainer container) {
        return template.img(container.getId());
    }

    @Override
    public void render(final KieContainer container, final SafeHtmlBuilder safeHtmlBuilder) {
        safeHtmlBuilder.append(render(container));
    }
    
    interface Template extends SafeHtmlTemplates {
        @Template("<span style=\"display:none\">{0}</span>")
        SafeHtml img(final String id);
    }
}
