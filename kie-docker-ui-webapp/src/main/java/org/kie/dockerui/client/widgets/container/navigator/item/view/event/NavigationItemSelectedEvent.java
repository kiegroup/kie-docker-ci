package org.kie.dockerui.client.widgets.container.navigator.item.view.event;

import com.google.gwt.event.shared.GwtEvent;

public class NavigationItemSelectedEvent extends GwtEvent<NavigationItemSelectedEventHandler>  {
    
    public static Type<NavigationItemSelectedEventHandler> TYPE = new Type<NavigationItemSelectedEventHandler>();

    private String id;

    public NavigationItemSelectedEvent(final String id) {
        super();
        this.id = id;
    }

    @Override
    public Type getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NavigationItemSelectedEventHandler handler) {
        handler.onNavigationItemSelected(this);
    }

    public String getId() {
        return id;
    }
    
}
