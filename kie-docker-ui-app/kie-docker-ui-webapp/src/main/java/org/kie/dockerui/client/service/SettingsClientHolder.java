package org.kie.dockerui.client.service;

import org.kie.dockerui.shared.settings.Settings;

public class SettingsClientHolder {
    private static SettingsClientHolder instance;
    private Settings settings;
    
    public static SettingsClientHolder getInstance() {
        if (instance == null) {
            instance = new SettingsClientHolder();
        }
        return instance;
    }

    public SettingsClientHolder() {
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
