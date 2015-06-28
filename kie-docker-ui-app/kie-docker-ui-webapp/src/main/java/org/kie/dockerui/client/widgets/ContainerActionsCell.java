package org.kie.dockerui.client.widgets;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.util.SharedUtils;

public class ContainerActionsCell extends AbstractSafeHtmlCell<String> {

    private static final String DEFAULT_STYLE = "float:left;cursor:hand;cursor:pointer;";
    private static final String DISABLED_STYLE = "filter: alpha(opacity=5);opacity: 0.5;";
    private static final String ENABLED_STYLE = "filter: alpha(opacity=10);opacity: 1;";
    private ContainersProvider containersProvider;
    private DoContainerActionCallback callback;

    public interface DoContainerActionCallback
    {
        void onStart(final KieContainer container);
        void onStop(final KieContainer container);
        void onRestart(final KieContainer container);
        void onRemove(final KieContainer container);
        void onViewLogs(final KieContainer container);
        void onViewDetails(final KieContainer container);
        void onNavigate(final KieContainer container);
    }
    
    public interface ContainersProvider {
        KieContainer getContainer(final String id);
    } 

    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
        /**
         * The template for this Cell, which includes styles and a value.
         *
         * @param styles
         *            the styles to include in the style attribute of the div
         * @param value
         *            the safe value. Since the value type is {@link SafeHtml},
         *            it will not be escaped before including it in the
         *            template. Alternatively, you could make the value type
         *            String, in which case the value would be escaped.
         * @return a {@link SafeHtml} instance
         */
        @SafeHtmlTemplates.Template("<div title=\"{3}\" name=\"{0}\" style=\"{1}\">{2}</div>")
        SafeHtml cell(String name, SafeStyles styles, SafeHtml value, String title);
    }/**/

    private static SafeStyles enabledStyle = SafeStylesUtils
            .fromTrustedString( DEFAULT_STYLE + ENABLED_STYLE );
    private static SafeStyles disabledStyle = SafeStylesUtils
            .fromTrustedString( DEFAULT_STYLE + DISABLED_STYLE );
    
    public ContainerActionsCell(final ContainersProvider containersProvider, final DoContainerActionCallback callback) {
        super(SimpleSafeHtmlRenderer.getInstance(), "click", "keydown");
        this.containersProvider = containersProvider;
        this.callback = callback;
    }

    public ContainerActionsCell(final ContainersProvider containersProvider, final DoContainerActionCallback callback, final SafeHtmlRenderer<String> renderer) {
        super(renderer, "click", "keydown");
        this.containersProvider = containersProvider;
        this.callback = callback;
    }

    /**
     * Create a singleton instance of the templates used to render the cell.
     */
    private static Templates templates = GWT.create(Templates.class);

    public static final String PLAY = "PLAY";
    public static final String STOP = "STOP";
    public static final String RELOAD = "RELOAD";
    public static final String REMOVE = "REMOVE";
    public static final String VIEW_LOGS = "VIEW_LOGS";
    public static final String VIEW_DETAILS = "VIEW_DETAILS";
    public static final String NAVIGATE = "NAVIGATE";
    public static final SafeHtml ICON_PLAY = makeImage(Images.INSTANCE.playIcon());
    public static final SafeHtml ICON_STOP = makeImage(Images.INSTANCE.stopIcon());
    public static final SafeHtml ICON_RELOAD = makeImage(Images.INSTANCE.reloadIconBlue());
    public static final SafeHtml ICON_REMOVE = makeImage(Images.INSTANCE.removeIcon());
    public static final SafeHtml ICON_VIEW_LOGS = makeImage(Images.INSTANCE.logsIcon());
    public static final SafeHtml ICON_NAVIGATE = makeImage(Images.INSTANCE.internetIcon());
    public static final SafeHtml ICON_VIEW_DETAILS = makeImage(Images.INSTANCE.detailsIcon());

    /**
     * Called when an event occurs in a rendered instance of this Cell. The
     * parent element refers to the element that contains the rendered cell, NOT
     * to the outermost element that the Cell rendered.
     */
    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context,
                               Element parent, String value, NativeEvent event,
                               com.google.gwt.cell.client.ValueUpdater<String> valueUpdater) {

        // Let AbstractCell handle the keydown event.
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Handle the click event.
        if ("click".equals(event.getType())) {

            // Ignore clicks that occur outside of the outermost element.
            EventTarget eventTarget = event.getEventTarget();

            if (parent.isOrHasChild(Element.as(eventTarget))) {
                // if (parent.getFirstChildElement().isOrHasChild(
                // Element.as(eventTarget))) {

                // use this to get the selected element!!
                Element el = Element.as(eventTarget);

                // check if we really click on the image
                if (callback != null && el.getNodeName().equalsIgnoreCase("IMG")) {
                    final String s = el.getParentElement().getAttribute("name");
                    final KieContainer container = containersProvider.getContainer(value);
                    final boolean isUp = SharedUtils.getContainerStatus(container);
                    final boolean isKieApp = container.getType() != null && KieImageCategory.KIEAPP.equals(container.getType().getCategory());
                    if (ContainerActionsCell.PLAY.equals(s) && !isUp) {
                        callback.onStart(container);
                    } else if (ContainerActionsCell.STOP.equals(s) && isUp) {
                        callback.onStop(container);
                    } else if (ContainerActionsCell.RELOAD.equals(s) && isUp) {
                        callback.onRestart(container);
                    } else if (ContainerActionsCell.REMOVE.equals(s)) {
                        callback.onRemove(container);
                    } else if (ContainerActionsCell.VIEW_LOGS.equals(s)) {
                        callback.onViewLogs(container);
                    } else if (ContainerActionsCell.VIEW_DETAILS.equals(s) && isUp && isKieApp) {
                        callback.onViewDetails(container);
                    } else if (ContainerActionsCell.NAVIGATE.equals(s) && isUp && isKieApp) {
                        callback.onNavigate(container);
                    }    
                }

            }
        }

    };

    /**
     * onEnterKeyDown is called when the user presses the ENTER key will the
     * Cell is selected. You are not required to override this method, but its a
     * common convention that allows your cell to respond to key events.
     */
    @Override
    protected void onEnterKeyDown(Context context, Element parent,
                                  String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        doAction(value, valueUpdater);
    }

    /**
     * Intern action
     *
     * @param value
     *            selected value
     * @param valueUpdater
     *            value updater or the custom value update to be called
     */
    private void doAction(String value, ValueUpdater<String> valueUpdater) {
        // Trigger a value updater. In this case, the value doesn't actually
        // change, but we use a ValueUpdater to let the app know that a value
        // was clicked.
        if (valueUpdater != null)
            valueUpdater.update(value);
    }

    @Override
    protected void render(com.google.gwt.cell.client.Cell.Context context,
                          SafeHtml data, SafeHtmlBuilder sb) {
        /*
         * Always do a null check on the value. Cell widgets can pass null to
         * cells if the underlying data contains a null, or if the data arrives
         * out of order.
         */
        if (data == null) {
            return;
        }

        final KieContainer container = containersProvider.getContainer(data.asString());
        final boolean isUp = SharedUtils.getContainerStatus(container);
        final boolean isKieApp = container.getType() != null && KieImageCategory.KIEAPP.equals(container.getType().getCategory());
        
        // If the value comes from the user, we escape it to avoid XSS attacks.
        // SafeHtml safeValue = SafeHtmlUtils.fromString(data.asString());

        // Use the template to create the Cell's html.
        // SafeStyles styles = SafeStylesUtils.fromTrustedString(safeValue
        // .asString());

        // generate the image cell
        SafeHtml rendered = templates.cell(PLAY, isUp ? disabledStyle : enabledStyle, ICON_PLAY, Constants.INSTANCE.start());
        sb.append(rendered);

        /* 
            -- Disabled STOP container button, as it sometimes fails from the Docker remote API. -- 
        rendered = templates.cell(STOP, isUp ? enabledStyle : disabledStyle, ICON_STOP, Constants.INSTANCE.stop());
        sb.append(rendered);
        */

        rendered = templates.cell(RELOAD, isUp ? enabledStyle : disabledStyle, ICON_RELOAD, Constants.INSTANCE.restart());
        sb.append(rendered);

        rendered = templates.cell(REMOVE, enabledStyle, ICON_REMOVE, Constants.INSTANCE.remove());
        sb.append(rendered);
        
        rendered = templates.cell(VIEW_LOGS, enabledStyle, ICON_VIEW_LOGS, Constants.INSTANCE.viewLogs());
        sb.append(rendered);

        if (isKieApp) {
            rendered = templates.cell(VIEW_DETAILS, isUp ? enabledStyle : disabledStyle, ICON_VIEW_DETAILS, Constants.INSTANCE.viewDetails());
            sb.append(rendered);

            rendered = templates.cell(NAVIGATE, isUp ? enabledStyle : disabledStyle, ICON_NAVIGATE, Constants.INSTANCE.navigate());
            sb.append(rendered);    
        }

    }

    /**
     * Make icons available as SafeHtml
     *
     * @param resource
     * @return
     */
    private static SafeHtml makeImage(ImageResource resource) {
        AbstractImagePrototype proto = AbstractImagePrototype.create(resource);

        // String html = proto.getHTML().replace("style='",
        // "style='left:0px;top:0px;"); // position:absolute;
        //
        // return SafeHtmlUtils.fromTrustedString(html);

        return proto.getSafeHtml();
    }
    
}