package org.kie.dockerui.client.widgets.container.navigator;

import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;

import java.util.Collection;

public interface NavigationWorkflowStep {

    String getTitle(NavigationContext context);
    int getItemsPerRow();
    Collection<NavigationItem> items(NavigationContext context);
    boolean accepts(NavigationContext context, KieContainer container);
    boolean accepts(NavigationContext context, KieImage image);
    NavigationWorkflowStep navigate(String navigationItemId, NavigationContext context);
    void reset(NavigationContext context);
    
}
