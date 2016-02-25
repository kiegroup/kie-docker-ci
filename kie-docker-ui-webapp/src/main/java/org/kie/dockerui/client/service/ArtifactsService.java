package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kie.dockerui.shared.model.KieArtifact;

import java.util.List;

@RemoteServiceRelativePath("artifactsService")
public interface ArtifactsService extends RemoteService {

    List<KieArtifact> list();
    
}
