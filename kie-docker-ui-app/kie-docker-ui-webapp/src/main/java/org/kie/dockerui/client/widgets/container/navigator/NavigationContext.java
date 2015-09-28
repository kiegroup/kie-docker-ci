package org.kie.dockerui.client.widgets.container.navigator;

import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;

import java.util.List;
import java.util.Map;

public interface NavigationContext {

    Map<String, String> getContext();
    List<KieContainer> getContainers();
    List<KieImage> getImages();
    
}
