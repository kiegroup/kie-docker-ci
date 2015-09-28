package org.kie.dockerui.client.widgets.container.navigator.item;

import com.google.gwt.safehtml.shared.SafeUri;

public interface NavigationItem {
    
    String getId();

    String getTitle();

    String getText();

    SafeUri getImageUri();

    int getContainersCount();
}
