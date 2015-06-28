package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kie.dockerui.shared.model.KieArtifact;

import java.util.List;

public interface ArtifactsServiceAsync {
    void list(AsyncCallback<List<KieArtifact>> async);
}
