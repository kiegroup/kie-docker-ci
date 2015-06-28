package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

public class KieListCommandResponse implements IsSerializable {
    private List<KieImage> images;
    private List<KieContainer> containers;

    public KieListCommandResponse(List<KieImage> images, List<KieContainer> containers) {
        this.images = images;
        this.containers = containers;
    }

    public KieListCommandResponse() {
    }

    public List<KieImage> getImages() {
        return images;
    }

    public List<KieContainer> getContainers() {
        return containers;
    }
}
