package org.kie.dockerui.client.widgets.container.navigator.item;

import java.util.List;

public interface CompositeNavigationItem extends NavigationItem {
    
    List<NavigationItem> getItems();

}
