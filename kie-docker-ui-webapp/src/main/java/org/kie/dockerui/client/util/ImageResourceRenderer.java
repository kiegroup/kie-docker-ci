package org.kie.dockerui.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class ImageResourceRenderer extends AbstractSafeHtmlRenderer<ImageResource> {
    private static final ImageResourceRenderer.Template TEMPLATE = (ImageResourceRenderer.Template) GWT.create(ImageResourceRenderer.Template.class);

    public ImageResourceRenderer() {
    }

    public SafeHtml renderWithTooltip(ImageResource image, String tooltip) {
        final String t = tooltip != null ? tooltip : "";
        return image instanceof ImageResourcePrototype.Bundle ? AbstractImagePrototype.create(image).getSafeHtml():TEMPLATE.image(image.getSafeUri(), t, image.getWidth(), image.getHeight());
    }
    
    public SafeHtml render(ImageResource image) {
        return renderWithTooltip(image, null);
    }

    interface Template extends SafeHtmlTemplates {
        @com.google.gwt.safehtml.client.SafeHtmlTemplates.Template("<img src=\'{0}\' alt='{1}' title='{1}' border=\'0\' width=\'{2}\' height=\'{3}\'>")
        SafeHtml image(SafeUri var1, String tooltip, int var2, int var3);
    }
}
