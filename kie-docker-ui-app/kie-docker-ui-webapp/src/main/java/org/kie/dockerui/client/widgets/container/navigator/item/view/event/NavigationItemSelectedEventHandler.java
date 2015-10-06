package org.kie.dockerui.client.widgets.container.navigator.item.view.event;


import com.google.gwt.event.shared.EventHandler;

public interface NavigationItemSelectedEventHandler extends EventHandler {
    void onNavigationItemSelected(NavigationItemSelectedEvent event);
}
