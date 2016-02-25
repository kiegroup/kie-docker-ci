package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kie.dockerui.shared.model.KieImageType;

@RemoteServiceRelativePath("databaseService")
public interface DatabaseService extends RemoteService {
    
    void createDatabase(String containerId, KieImageType imageType, String dbName) throws Exception;

}
