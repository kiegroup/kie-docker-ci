package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kie.dockerui.shared.model.*;

import java.util.Collection;
import java.util.List;

@RemoteServiceRelativePath("dockerService")
public interface DockerService extends RemoteService {

    KieListCommandResponse list();
    
    List<KieContainer> listContainers();

    List<KieContainer> listContainers(Collection<String> ids);
    
    KieContainer getContainer(String containerId);

    List<KieImage> listImages();

    KieImage getImage(String imageId);

    void stop(String containerId);

    void start(String containerId);
    
    void restart(String containerId);
    
    void remove(String containerId);
    
    String logs(String containerId);
    
    KieContainerDetails inspect(String containerId);

    void updateStatus(String containerId);

    String create(String image, String name, String[] env, String[] linking);
    
    KieDockerSummary summary();

}
