package org.kie.dockerui.backend;

import org.kie.dockerui.backend.service.DockerServiceImpl;
import org.kie.dockerui.backend.service.SettingsServiceImpl;
import org.kie.dockerui.shared.model.KieAppStatus;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieContainerDetails;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.SharedUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Persistence?
public class KieStatusManager {
    private static final DockerServiceImpl dockerService = new DockerServiceImpl();
    private static final SettingsServiceImpl settingsService = new SettingsServiceImpl();
    
    private static KieStatusManager instance;
    private static final Map<String, KieAppStatus> statusMap = new HashMap<String, KieAppStatus>();

    public static KieStatusManager getInstance() {
        if (instance == null) {
            instance = new KieStatusManager();
        }
        return instance;
    }

    public KieStatusManager() {
        
    }
    
    public void build() {
        doLog("Building status caché for KIE images...");
        statusMap.clear();
        final List<KieContainer> containers = dockerService.listContainers();
        if (containers != null) {
            for (final KieContainer container : containers) {
                final KieAppStatus status = getStatus(container);
                addStatus(container.getImage(), status);
            }
        }
        doLog("Status caché for KIE images build completed.");
    }
    
    public void addStatus(final String image, final KieAppStatus status) {
        if (image != null && status != null) {
            statusMap.put(image, status);
            doLog("Added or updated status " + status.name() + " for image " + image);
        }
    }

    public void updateStatus(final KieContainer container) {
        if (container != null && SharedUtils.isKieApp(container)) {
            final String image = container.getImage();
            statusMap.remove(image);
            final KieAppStatus s = getStatus(container);
            statusMap.put(image, s);
            doLog("Updated status to " + s.name() + " for image " + image);
        }
    }

    public KieAppStatus getStatus(final String image) {
        if (image == null) return null;
        return statusMap.get(image);
    }

    public KieAppStatus getStatus(final KieContainer container)  {
        if (container == null) return null;
        if (!SharedUtils.getContainerStatus(container)) return null;
        if (!SharedUtils.isKieApp(container)) return  null;
        
        final String image = container.getImage();
        KieAppStatus s = statusMap.get(image);
        if (s == null) {
            final KieContainerDetails details = dockerService.inspect(container.getId());
            final Settings settings = settingsService.getSettings();
            final String url = getWebAddress(container, details, settings);
            return getStatusByURL(url);
        }
        
        return s;
    }
    
    public KieAppStatus getStatusByURL(final String url)  {

        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            doLog("Ping to URL '" + url + "' // Response:" + code);
            // A 200OK or 401 Unauthorized means that application is up.
            if (code == 200 || code == 401) {
                return KieAppStatus.OK;
            }
        } catch (Exception e) {
            doLog("Error getting status for URL '" + url + "'. Message: " + e.getMessage());
            e.printStackTrace();
            return KieAppStatus.NOT_STARTING_UP;
        }
        return KieAppStatus.NOT_STARTING_UP;
    }

    public static String getWebAddress(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings)  {
        final String protocol = settings.getProtocol();
        final String host = settings.getPublicHost();
        final int httpPublicPort = SharedUtils.getPublicPort(8080, container);
        if (httpPublicPort < 0) throw new IllegalStateException("No ports available. Is the container running?");
        final String contextPath = container.getType().getContextPath();

        try {
            return new URL(protocol, host, httpPublicPort, "/" + contextPath).toString();
        } catch (MalformedURLException e) {
            doLog("Error getting web address for application. Message: " + e.getMessage());
        }
        return null;
    }

    private static void doLog(String message) {
        System.out.println(message);
    }

}
