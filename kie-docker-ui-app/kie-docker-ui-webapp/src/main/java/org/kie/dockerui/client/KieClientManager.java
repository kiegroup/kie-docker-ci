package org.kie.dockerui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieListCommandResponse;

import java.util.List;

public class KieClientManager {
    private static KieClientManager instance;
    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);

    private List<KieImage> images;
    private List<KieContainer> containers;
    
    public static KieClientManager getInstance() {
        if (instance == null) {
            instance = new KieClientManager();
        }
        return instance;
    }

    public KieClientManager() {
        
    }

    public void reload(final KieClientManagerCallback callback) {
        dockerService.list(new AsyncCallback<KieListCommandResponse>() {
            @Override
            public void onFailure(final Throwable caught) {
                if (callback != null) callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final KieListCommandResponse result) {
                KieClientManager.this.containers = result.getContainers();
                KieClientManager.this.images = result.getImages();
                if (callback != null) callback.onSuccess();
            }
        });
    }
    
    
    public List<KieImage> getImages() {
        return images;
    }

    public List<KieContainer> getContainers() {
        return containers;
    }
    
    public interface KieClientManagerCallback {
        void onFailure(final Throwable caught);
        void onSuccess();
    }
}
