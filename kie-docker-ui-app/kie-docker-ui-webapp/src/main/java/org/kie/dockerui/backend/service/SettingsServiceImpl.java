package org.kie.dockerui.backend.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SettingsServiceImpl extends RemoteServiceServlet implements SettingsService {

    private static final String APP_PROPERTIES = "app.properties";
    /* If this webapp is inside a container, can have different network settings. */
    private static final String APP_PROPERTY_HOST_PRIVATE = "docker.host.private";
    private static final String APP_PROPERTY_HOST_PUBLIC = "docker.host.public";
    private static final String APP_PROPERTY_SSH_USER = "docker.host.ssh.user";
    private static final String APP_PROPERTY_REGISTRY_ENABLED = "docker.registry.enabled";
    private static final String APP_PROPERTY_REGISTRY_PORT = "docker.registry.port";
    private static final String APP_PROPERTY_RESTAPI_PROTOCOL = "docker.restapi.protocol";
    private static final String APP_PROPERTY_RESTAPI_PORT = "docker.restapi.port";
    private static final String APP_PROPERTY_KIE_REPOSITORY = "docker.kie.images.repository";
    private static final String APP_PROPERTY_MYSQL_IMAGE  = "docker.image.type.mysql";
    private static final String APP_PROPERTY_POSTGRES_IMAGE = "docker.image.type.postgresql";
    private static final String APP_PROPERTY_JENKINS_URL = "docker.jenkins.url";
    private static final String APP_PROPERTY_ARTIFACTS_PATH = "docker.kie.artifacts.path";
    
    private static Settings settings;
    
    @Override
    public synchronized Settings getSettings() {
        if (settings == null) {
            settings = readSettings();
            if (settings == null) return null;
        }
        return settings;
    }

    @Override
    public String getDefaultImageForType(KieImageType type) {
        final Properties p = getProperties(APP_PROPERTIES);
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return p.getProperty(APP_PROPERTY_MYSQL_IMAGE);
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return p.getProperty(APP_PROPERTY_POSTGRES_IMAGE);
        if (KieImageTypeManager.KIE_H2_IN_MEMORY.equals(type)) return KieImageTypeManager.TYPE_IN_MEMORY_DB_ID;
        return null;
    }

    private Settings readSettings() {
        final Properties appProperties = getProperties(APP_PROPERTIES);
        
        // Application properties.
        String hostPrivate = System.getProperty(APP_PROPERTY_HOST_PRIVATE);
        if (hostPrivate == null || hostPrivate.trim().length() == 0) {
            hostPrivate = appProperties.getProperty(APP_PROPERTY_HOST_PRIVATE);
        }
        String hostPublic = System.getProperty(APP_PROPERTY_HOST_PUBLIC);
        if (hostPublic == null || hostPublic.trim().length() == 0) {
            hostPublic = appProperties.getProperty(APP_PROPERTY_HOST_PUBLIC);
        }
        String user = System.getProperty(APP_PROPERTY_SSH_USER);
        if (user == null || user.trim().length() == 0) {
            user = appProperties.getProperty(APP_PROPERTY_SSH_USER);
        }
        String artifactsPath = System.getProperty(APP_PROPERTY_ARTIFACTS_PATH);
        if (artifactsPath == null || artifactsPath.trim().length() == 0) {
            artifactsPath = appProperties.getProperty(APP_PROPERTY_ARTIFACTS_PATH);
        }
        String jenkinsJobURL = System.getProperty(APP_PROPERTY_JENKINS_URL);
        if (jenkinsJobURL == null || jenkinsJobURL.trim().length() == 0) {
            jenkinsJobURL = appProperties.getProperty(APP_PROPERTY_JENKINS_URL);
        }
        final String registryPort = appProperties.getProperty(APP_PROPERTY_REGISTRY_PORT);
        final boolean registryEnabled = Boolean.valueOf(appProperties.getProperty(APP_PROPERTY_REGISTRY_ENABLED));
        final String restProtocol = appProperties.getProperty(APP_PROPERTY_RESTAPI_PROTOCOL);
        final String restPort = appProperties.getProperty(APP_PROPERTY_RESTAPI_PORT);
        final String kieRepo = appProperties.getProperty(APP_PROPERTY_KIE_REPOSITORY);
        final Settings settings = new Settings(restProtocol, hostPrivate, hostPublic, user, Integer.decode(restPort),
                registryEnabled, Integer.decode(registryPort), kieRepo, artifactsPath, jenkinsJobURL);
        
        return settings;
    }
    
    private Properties getProperties(final String name) {
        InputStream appPropertiesStream = getClass().getResourceAsStream(name);
        if (appPropertiesStream == null) {
            doLog("Application properties not found.");
            return  null;
        }

        try {
            final Properties p = new Properties();
            p.load(appPropertiesStream);
            appPropertiesStream.close();
            return p;
        } catch (IOException e) {
            doLog("Error getting application properties. Message: " + e.getMessage());
        }

        return  null;
    }

    private static void doLog(String message) {
        System.out.println(message);
    }
}
