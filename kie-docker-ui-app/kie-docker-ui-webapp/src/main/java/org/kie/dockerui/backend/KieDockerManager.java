package org.kie.dockerui.backend;

public class KieDockerManager {

    private static KieDockerManager instance;

    public static KieDockerManager getInstance() {
        if (instance == null) {
            instance = new KieDockerManager();
        }
        return instance;
    }
    
    public void initApplication() {

        doLog("Initializing KIE Docker UI application....");
        
        // Obtain status for running containers.
        doLog("Building KIE containers application status cache....");
        final KieStatusManager kieStatusManager = KieStatusManager.getInstance();
        kieStatusManager.build();
        doLog("KIE containers application status cache built successfully.");

        doLog("Initialization of KIE Docker UI application completed!");
        
    }

    private static void doLog(String message) {
        System.out.println(message);
    }
    
}
