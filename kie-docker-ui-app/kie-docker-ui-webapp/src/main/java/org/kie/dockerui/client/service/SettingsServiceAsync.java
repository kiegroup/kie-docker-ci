package org.kie.dockerui.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.settings.Settings;

import java.util.List;

public interface SettingsServiceAsync {

    public void getSettings(AsyncCallback<Settings> callback);

    void getDefaultImageForType(KieImageType type, AsyncCallback<String> async);
}
