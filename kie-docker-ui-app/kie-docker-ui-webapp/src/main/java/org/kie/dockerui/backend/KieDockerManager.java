package org.kie.dockerui.backend;

import org.kie.dockerui.backend.service.SettingsServiceImpl;
import org.kie.dockerui.shared.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieDockerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieDockerManager.class.getName());
    
    private static KieDockerManager instance;
    private static final SettingsServiceImpl settingsService = new SettingsServiceImpl();
    private static final KieStatusManager statusManager = KieStatusManager.getInstance();

    public static synchronized KieDockerManager getInstance() {
        if (instance == null) {
            instance = new KieDockerManager();
        }
        return instance;
    }

    public KieDockerManager() {
    }

    public void init() {

        LOGGER.info("Initializing KIE Docker UI application....");
        
        // Run the status manager, using daemon mode or just a single run.
        if (isStatusManagerEnabled()) {
            initStatusManager();
        } else {
            disableStatusManager();
        }

        LOGGER.info("Initialization of KIE Docker UI application completed!");
    }

    public void shutdown() {
        if (isStatusManagerEnabled()) {
            shutdownStatusManager();
        }
    }
    
    
    private void disableStatusManager() {
        statusManager.disable();
    }
    
    private void initStatusManager() {
        if (isStatusManagerDaemon()) {
            runStatusManagerAsDaemon();
        } else {
            runStatusManager();
        }
    }

    private void shutdownStatusManager() {
        if (isStatusManagerDaemon()) {
            LOGGER.info("Shutting down the Status Manager daemon.");
            statusManager.shutdown();
        }
    }
    
    private void runStatusManagerAsDaemon() {
        Settings settings = settingsService.getSettings();
        long pullInterval = settings.getPullInterval();
        LOGGER.info("Status Manager will run as a daemon using a pulling interval of [" + pullInterval + "] minutes.");
        statusManager.start(pullInterval);
    }
    
    private void runStatusManager() {
        // Obtain status for running containers.
        LOGGER.info("Status Manager will perform a single run .");
        statusManager.run();
    }
    
    private boolean isStatusManagerEnabled() {
        Settings settings = settingsService.getSettings();
        return settings.isStatusManagerEnabled();
    } 
    
    private boolean isStatusManagerDaemon() {
        Settings settings = settingsService.getSettings();
        return settings.isPullEnabled();
    }

}
