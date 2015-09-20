package org.kie.dockerui.backend;

import org.kie.dockerui.backend.service.DockerServiceImpl;
import org.kie.dockerui.backend.service.SettingsServiceImpl;
import org.kie.dockerui.backend.util.KieDockerUtils;
import org.kie.dockerui.shared.model.KieAppStatus;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieContainerDetails;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.SharedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KieStatusManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KieStatusManager.class.getName());
    
    private static final DockerServiceImpl dockerService = new DockerServiceImpl();
    private static final SettingsServiceImpl settingsService = new SettingsServiceImpl();
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);
    private static final int TIMEOUT = 30000;
    private static KieStatusManager instance;
    private static final Map<String, KieAppStatus> statusMap = new HashMap<String, KieAppStatus>();
    private boolean isStarted;

    public static synchronized KieStatusManager getInstance() {
        if (instance == null) {
            instance = new KieStatusManager();
        }
        return instance;
    }

    public KieStatusManager() {
        isStarted = false;
    }

    /**
     * Starts the daemon for pull remote containers using Docker REST services.
     */
    public void start() {
        start(null);
    }

    /**
     * Starts the daemon for pull remote containers using Docker REST services.
     *
     * @param pullInterval The pulling interval, in seconds.
     */
    public void start(Long pullInterval) {
        if (isStarted) {
            throw new RuntimeException("Status Manager Daemon has been already started");
        }
        
        KieStatusManagerDaemonWork work = pullInterval != null ? new KieStatusManagerDaemonWork(pullInterval) :
                new KieStatusManagerDaemonWork();
        executor.execute(work);
        isStarted = true;
    }

    /**
     * Shutdown the daemon. As only one in the executor service, shutdown the executor.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Runs a single status request to the Docker remote service.
     */
    public synchronized void run() {
        LOGGER.info("Building status caché for KIE images...");
        statusMap.clear();
        final List<KieContainer> containers = dockerService.listContainers();
        if (containers != null) {
            for (final KieContainer container : containers) {
                final KieAppStatus status = getStatus(container);
                addStatus(container.getImage(), status);
            }
        }
        LOGGER.info("Status caché for KIE images build completed.");
    }

    public void addStatus(final String image, final KieAppStatus status) {
        if (image != null && status != null) {
            statusMap.put(image, status);
            LOGGER.info("Added or updated status " + status.name() + " for image " + image);
        }
    }

    public synchronized void updateStatus(final KieContainer container) {
        if (container != null && SharedUtils.isKieApp(container)) {
            final boolean isUp = SharedUtils.getContainerStatus(container);
            if (isUp) {
                final String image = container.getImage();
                statusMap.remove(image);
                final KieAppStatus s = getStatus(container);
                statusMap.put(image, s);
                LOGGER.info("Updated status to " + s.name() + " for image " + image);    
            }
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
            final Settings settings = settingsService.getSettings();
            final String url = getWebAddress(container, settings);
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
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.connect();

            int code = connection.getResponseCode();
            LOGGER.debug("Ping to URL '" + url + "' // Response:" + code);
            // A 200OK or 401 Unauthorized means that application is up.
            if (code == 200 || code == 401) {
                return KieAppStatus.OK;
            }
        } catch (Exception e) {
            LOGGER.error("Error getting status for URL '" + url + "'.", e);
            return KieAppStatus.FAILED;
        }
        return KieAppStatus.FAILED;
    }

    private static String getWebAddress(final KieContainer container, final Settings settings)  {
        final String protocol = settings.getProtocol();
        final String host = settings.getPublicHost();
        final int httpPublicPort = SharedUtils.getPublicPort(8080, container);
        if (httpPublicPort < 0) throw new IllegalStateException("No ports available. Is the container running?");
        final String contextPath = container.getType().getContextPath();

        try {
            return new URL(protocol, host, httpPublicPort, "/" + contextPath).toString();
        } catch (MalformedURLException e) {
            LOGGER.error("Error getting web address for application.", e);
        }
        return null;
    }
    
    /*
        ************** DAEMON ******************
    
     */

    private class KieStatusManagerDaemonWork implements Runnable {

        /* Pulling interval defaults to 10 minutes. */
        private long pullInterval = 10;

        public KieStatusManagerDaemonWork() {

        }

        /**
         * Constructor for a given custom pulling interval.
         * @param pullInterval The pulling interval, in minutes.
         */
        public KieStatusManagerDaemonWork(long pullInterval) {
            this.pullInterval = pullInterval;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Run the status manager sync with remote docker containers.
                    final long interval = KieDockerUtils.getMillis(pullInterval);
                    LOGGER.info("Running status manager daemon work [interval=" + pullInterval + "min].");
                    final KieStatusManager manager = KieStatusManager.getInstance();
                    manager.run();
                    Thread.sleep(interval);
                }
            } catch (InterruptedException e) {
                LOGGER.error("Error running status manager daemon.", e);
            }
        }
    }

}
