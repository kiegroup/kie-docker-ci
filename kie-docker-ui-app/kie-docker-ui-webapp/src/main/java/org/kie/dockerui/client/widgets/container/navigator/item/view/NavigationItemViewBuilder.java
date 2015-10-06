package org.kie.dockerui.client.widgets.container.navigator.item.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.widgets.container.navigator.item.CompositeNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.DateNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.DefaultNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEventHandler;

public class NavigationItemViewBuilder {

    public static Widget build(final NavigationItem item, final boolean isActive, final NavigationItemSelectedEventHandler handler) {
        if (item instanceof  CompositeNavigationItem) {
            return buildCompositeItemView((CompositeNavigationItem) item,isActive, handler);
        } else if (item instanceof  DateNavigationItem) {
            return buildDateItemView((DateNavigationItem) item, handler);
        } if (item instanceof  DefaultNavigationItem) {
            return buildDefaultItemView((DefaultNavigationItem) item, handler);
        }
        
        return null;
    }
    
    public static NavigationDateItemView buildDateItemView(final DateNavigationItem item, final NavigationItemSelectedEventHandler handler) {
        final NavigationDateItemView navigationItemView = GWT.create(NavigationDateItemView.class);
        navigationItemView.setEmptyText(Constants.INSTANCE.newLiteral(), Constants.INSTANCE.createNewForThisType());
        final Style style = navigationItemView.getElement().getStyle();
        style.setWidth(200, Style.Unit.PX);
        style.setHeight(200, Style.Unit.PX);
        navigationItemView.addNavigationItemSelectedEventHandler(handler);
        navigationItemView.show(item);
        return navigationItemView;
    }
    
    public static CompositeNavigationItemView buildCompositeItemView(final CompositeNavigationItem item, final boolean isDefaultOpen, final NavigationItemSelectedEventHandler handler) {
        final CompositeNavigationItemView navigationItemView = GWT.create(CompositeNavigationItemView.class);
        navigationItemView.addNavigationItemSelectedEventHandler(handler);
        navigationItemView.accordionGroup.setDefaultOpen(isDefaultOpen);
        navigationItemView.show(item);
        return navigationItemView;
    }
    
    public static NavigationItemView buildDefaultItemView(final DefaultNavigationItem item, final NavigationItemSelectedEventHandler handler) {
        final NavigationItemView navigationItemView = GWT.create(NavigationItemView.class);
        navigationItemView.setEmptyText(Constants.INSTANCE.newLiteral(), Constants.INSTANCE.createNewForThisType());
        final Style style = navigationItemView.getElement().getStyle();
        style.setWidth(200, Style.Unit.PX);
        style.setHeight(200, Style.Unit.PX);
        navigationItemView.setImageSizePx(150);
        navigationItemView.addNavigationItemSelectedEventHandler(handler);
        navigationItemView.show(item);
        return navigationItemView;
    }
}
