package org.kie.dockerui.client.widgets.cell;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.kie.dockerui.client.util.KieImageTypesSafeHtmlRenderer;
import org.kie.dockerui.shared.model.KieImage;

public class ImageTypesCell extends AbstractSafeHtmlCell<KieImage> {

    public ImageTypesCell() {
        super(KieImageTypesSafeHtmlRenderer.getInstance());
    }

    @Override
    protected void render(Context context, SafeHtml safeHtml, SafeHtmlBuilder safeHtmlBuilder) {
        if (safeHtml != null) {
            safeHtmlBuilder.append(safeHtml);
        }
    }

    
}