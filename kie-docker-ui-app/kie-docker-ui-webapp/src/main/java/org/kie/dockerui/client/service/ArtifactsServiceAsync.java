package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kie.dockerui.shared.model.KieArtifact;

import java.util.List;

public interface ArtifactsServiceAsync {
    void list(AsyncCallback<List<KieArtifact>> async);
}
