package org.kie.dockerui.client.widgets;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.kie.dockerui.client.util.KieContainerTypesSafeHtmlRenderer;
import org.kie.dockerui.shared.model.KieContainer;

public class ContainerTypesCell extends AbstractSafeHtmlCell<KieContainer> {

    public ContainerTypesCell() {
        super(KieContainerTypesSafeHtmlRenderer.getInstance());
    }

    @Override
    protected void render(Context context, SafeHtml safeHtml, SafeHtmlBuilder safeHtmlBuilder) {
        if (safeHtml != null) {
            safeHtmlBuilder.append(safeHtml);
        }
    }

    
}