package org.kie.dockerui.client.widgets;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.kie.dockerui.client.util.ImageResourceRenderer;

public class ClickableImageResourceCell extends AbstractCell<ImageResource> {
    private static ImageResourceRenderer renderer;
    private String tooltip;

    public ClickableImageResourceCell () {
        super("click");
        if (renderer == null) {
            renderer = new ImageResourceRenderer();
        }
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, final Element parent, ImageResource value, NativeEvent event,
                               ValueUpdater<ImageResource> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (event.getType().equals("click")) {
            if(valueUpdater != null) {
                valueUpdater.update(value);
            }
        }
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
                       ImageResource value, final SafeHtmlBuilder sb) {
        sb.append(renderer.renderWithTooltip(value, tooltip));
    }
}
