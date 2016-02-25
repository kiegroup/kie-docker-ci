package org.kie.dockerui.backend;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class KieDockerContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        
        // Initialize the application.
        KieDockerManager.getInstance().init();
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        // Shutdown the application.
        KieDockerManager.getInstance().shutdown();
        
    }
}
