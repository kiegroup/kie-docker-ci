package org.kie.dockerui.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KieDockerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieDockerUtils.class.getName());
    
    public static Properties getProperties(final String name) {
        InputStream appPropertiesStream = KieDockerUtils.class.getClassLoader().getResourceAsStream(name);
        if (appPropertiesStream == null) {
            LOGGER.error("Application properties not found at " + name);
            return  null;
        }

        try {
            final Properties p = new Properties();
            p.load(appPropertiesStream);
            appPropertiesStream.close();
            return p;
        } catch (IOException e) {
            LOGGER.error("Error getting application properties.", e);
        }

        return  null;
    }

    public static long getMillis(long timeInMinutes) {
        return timeInMinutes * 60 * 1000;
    }

}
