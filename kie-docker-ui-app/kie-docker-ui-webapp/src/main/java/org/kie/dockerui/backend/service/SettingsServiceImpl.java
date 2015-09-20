package org.kie.dockerui.backend.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kie.dockerui.backend.util.KieDockerUtils;
import org.kie.dockerui.backend.util.Timer;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SettingsServiceImpl extends RemoteServiceServlet implements SettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsServiceImpl.class.getName());
    
    public static final String APP_PROPERTIES = "org/kie/dockerui/backend/service/app.properties";
    /* If this webapp is inside a container, can have different network settings. */
    public static final String APP_PROPERTY_HOST_PRIVATE = "docker.host.private";
    public static final String APP_PROPERTY_HOST_PUBLIC = "docker.host.public";
    public static final String APP_PROPERTY_SSH_USER = "docker.host.ssh.user";
    public static final String APP_PROPERTY_REGISTRY_ENABLED = "docker.registry.enabled";
    public static final String APP_PROPERTY_REGISTRY_PORT = "docker.registry.port";
    public static final String APP_PROPERTY_RESTAPI_PROTOCOL = "docker.restapi.protocol";
    public static final String APP_PROPERTY_RESTAPI_PORT = "docker.restapi.port";
    public static final String APP_PROPERTY_KIE_REPOSITORY = "docker.kie.images.repository";
    public static final String APP_PROPERTY_MYSQL_IMAGE  = "docker.image.type.mysql";
    public static final String APP_PROPERTY_POSTGRES_IMAGE = "docker.image.type.postgresql";
    public static final String APP_PROPERTY_JENKINS_URL = "docker.jenkins.url";
    public static final String APP_PROPERTY_ARTIFACTS_PATH = "docker.kie.artifacts.path";
    public static final String APP_PROPERTY_PULL_ENABLED = "docker.pull.enabled";
    public static final String APP_PROPERTY_PULL_INTERVAL = "docker.pull.interval";
    
    private static Settings settings;
    
    @Override
    public synchronized Settings getSettings() {
        if (settings == null) {
            settings = readSettings();
            if (settings == null) {
                LOGGER.error("ERROR - Cannot read application settings.");
                return null;
            }
        }
        return settings;
    }

    @Override
    public String getDefaultImageForType(KieImageType type) {
        final Properties p = KieDockerUtils.getProperties(APP_PROPERTIES);
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return p.getProperty(APP_PROPERTY_MYSQL_IMAGE);
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return p.getProperty(APP_PROPERTY_POSTGRES_IMAGE);
        if (KieImageTypeManager.KIE_H2_IN_MEMORY.equals(type)) return KieImageTypeManager.TYPE_IN_MEMORY_DB_ID;
        return null;
    }

    private Settings readSettings() {
        final Properties appProperties = KieDockerUtils.getProperties(APP_PROPERTIES);

        Timer timer = new Timer();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting to read settings from environment or configuration file.");
            timer.start();
        }
        
        // Application properties.
        String hostPrivate = readSetting(appProperties, APP_PROPERTY_HOST_PRIVATE);
        String hostPublic = readSetting(appProperties, APP_PROPERTY_HOST_PUBLIC);
        String user = readSetting(appProperties, APP_PROPERTY_SSH_USER);
        String artifactsPath = readSetting(appProperties, APP_PROPERTY_ARTIFACTS_PATH);
        String jenkinsJobURL = readSetting(appProperties, APP_PROPERTY_JENKINS_URL);
        String registryPort = readSetting(appProperties, APP_PROPERTY_REGISTRY_PORT);
        String _registryEnabled = readSetting(appProperties, APP_PROPERTY_REGISTRY_ENABLED);
        boolean registryEnabled = Boolean.valueOf(_registryEnabled);
        String restProtocol = readSetting(appProperties, APP_PROPERTY_RESTAPI_PROTOCOL);
        String restPort = readSetting(appProperties, APP_PROPERTY_RESTAPI_PORT);
        String kieRepo = readSetting(appProperties, APP_PROPERTY_KIE_REPOSITORY);
        String _pullEnabled = readSetting(appProperties, APP_PROPERTY_PULL_ENABLED);
        boolean pullEnabled = Boolean.valueOf(_pullEnabled);
        String _pullInterval = readSetting(appProperties, APP_PROPERTY_PULL_INTERVAL);
        long pullInterval = Long.decode(_pullInterval);

        final Settings settings = new Settings(restProtocol, hostPrivate, hostPublic, user, Integer.decode(restPort),
                registryEnabled, Integer.decode(registryPort), kieRepo, artifactsPath, jenkinsJobURL,
                pullEnabled, pullInterval);

        if (LOGGER.isDebugEnabled()) {
            timer.end();
            LOGGER.debug("Settings read. Elapsed time = " + timer.getTotalTime());
        }
        
        return settings;
    }
    
    private String readSetting(final Properties appProperties, final String key) {
        String result = System.getProperty(key);
        if (result == null || result.trim().length() == 0) {
            result = appProperties.getProperty(key);
        }
        return result;
    }
    
}
