package org.kie.dockerui.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImageType;

import java.util.List;

public class KieContainerTypesSafeHtmlRenderer implements SafeHtmlRenderer<KieContainer> {

    private static KieContainerTypesSafeHtmlRenderer instance;
    
    public static KieContainerTypesSafeHtmlRenderer getInstance() {
        if (instance == null) {
            instance = new KieContainerTypesSafeHtmlRenderer();
        }
        return instance;
    }
    
    @Override
    public SafeHtml render(KieContainer container) {
        final KieImageType type = container.getType();
        if (type != null) {
            final KieContainerTypeSafeHtmlRenderer r = KieContainerTypeSafeHtmlRenderer.getInstance();
            final SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
            final SafeHtml typeHtml = r.render(type);
            htmlBuilder.append(typeHtml);
            final List<KieImageType> subTypes = container.getSubTypes();
            if (subTypes != null && !subTypes.isEmpty()) {
                for (final KieImageType _suType : subTypes) {
                    final SafeHtml sbuTypeHtml = r.render(_suType);
                    htmlBuilder.append(sbuTypeHtml);
                }
            }
            return htmlBuilder.toSafeHtml();
        }
        
        return null;
    }

    @Override
    public void render(KieContainer container, SafeHtmlBuilder safeHtmlBuilder) {
        safeHtmlBuilder.append(render(container));
    }
}
