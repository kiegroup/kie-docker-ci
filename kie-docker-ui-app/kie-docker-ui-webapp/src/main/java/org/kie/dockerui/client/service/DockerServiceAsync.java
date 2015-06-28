package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kie.dockerui.shared.model.*;

import java.util.Collection;
import java.util.List;

public interface DockerServiceAsync {

    void listContainers(AsyncCallback<List<KieContainer>> callback);

    void listContainers(Collection<String> ids, AsyncCallback<List<KieContainer>> async);

    void getContainer(String containerId, AsyncCallback<KieContainer> async);

    void stop(String containerId, AsyncCallback<Void> async);

    void start(String containerId, AsyncCallback<Void> async);

    void restart(String containerId, AsyncCallback<Void> async);

    void remove(String containerId, AsyncCallback<Void> async);

    void logs(String containerId, AsyncCallback<String> async);

    void inspect(String containerId, AsyncCallback<KieContainerDetails> async);

    void updateStatus(String containerId, AsyncCallback<Void> async);

    void create(String image, String name, String[] env, String[] linking, AsyncCallback<String> async);

    void listImages(AsyncCallback<List<KieImage>> async);
    
    void getImage(String imageId, AsyncCallback<KieImage> async);

    void summary(AsyncCallback<KieDockerSummary> async);

    void list(AsyncCallback<KieListCommandResponse> async);
}
