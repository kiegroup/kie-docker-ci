package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.settings.Settings;

@RemoteServiceRelativePath("settingsService")
public interface SettingsService extends RemoteService {
    
    Settings getSettings();
    
    String getDefaultImageForType(KieImageType type);
}
