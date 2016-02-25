package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kie.dockerui.shared.model.KieImageType;

public interface DatabaseServiceAsync {

    void createDatabase(String containerId, KieImageType imageType, String dbName, AsyncCallback<Void> async) throws Exception;

}
