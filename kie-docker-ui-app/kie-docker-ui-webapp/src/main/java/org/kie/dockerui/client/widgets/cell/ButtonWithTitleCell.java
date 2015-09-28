package org.kie.dockerui.client.widgets.cell;

import com.github.gwtbootstrap.client.ui.ButtonCell;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ButtonWithTitleCell  extends ButtonCell {

    private String title;

    public ButtonWithTitleCell(IconType icon) {
        super(icon);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        final ButtonType type = getType();
        final IconType icon = getIcon();
        final ButtonSize size = getSize();
        final String _title = this.title != null ? " title=\"" + this.title + "\" " : "";
        sb.appendHtmlConstant("<button " + _title + " type=\"button\" class=\"btn "
                + (type != null ? type.get() : "") + (size != null ? " " + size.get() : "") + "\" tabindex=\"-1\">");
        if (data != null) {
            if (icon != null) {
                sb.appendHtmlConstant("<i class=\"" + icon.get() + "\"></i> ");
            }
            sb.append(data);
        }
        sb.appendHtmlConstant("</button>");
    }
}
