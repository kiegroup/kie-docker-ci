package org.kie.dockerui.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;

import java.util.List;

public class KieImageTypesSafeHtmlRenderer implements SafeHtmlRenderer<KieImage> {

    private static KieImageTypesSafeHtmlRenderer instance;
    
    public static KieImageTypesSafeHtmlRenderer getInstance() {
        if (instance == null) {
            instance = new KieImageTypesSafeHtmlRenderer();
        }
        return instance;
    }
    
    @Override
    public SafeHtml render(KieImage image) {
        final KieImageType type = image.getType();
        if (type != null) {
            final KieContainerTypeSafeHtmlRenderer r = KieContainerTypeSafeHtmlRenderer.getInstance();
            final SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
            final SafeHtml typeHtml = r.render(type);
            htmlBuilder.append(typeHtml);
            final List<KieImageType> subTypes = image.getSubTypes();
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
    public void render(KieImage image, SafeHtmlBuilder safeHtmlBuilder) {
        safeHtmlBuilder.append(render(image));
    }
}
