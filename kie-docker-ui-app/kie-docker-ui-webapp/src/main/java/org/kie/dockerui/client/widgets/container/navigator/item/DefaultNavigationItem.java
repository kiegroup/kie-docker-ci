package org.kie.dockerui.client.widgets.container.navigator.item;

import com.google.gwt.safehtml.shared.SafeUri;

public interface DefaultNavigationItem extends NavigationItem {
    
    String getText();

    SafeUri getImageUri();

}
