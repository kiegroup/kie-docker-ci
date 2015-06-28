package org.kie.dockerui.backend;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class KieDockerContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        
        // Initialize the application.
        final KieDockerManager kieDockerManager = KieDockerManager.getInstance();
        kieDockerManager.initApplication();
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
