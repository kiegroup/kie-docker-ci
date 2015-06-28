package org.kie.dockerui.shared.util;

import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieContainerPort;
import org.kie.dockerui.shared.model.KieImageType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtils {

    public static final String KIE_CONNECTION_URL = "KIE_CONNECTION_URL";
    public static final String KIE_CONNECTION_DRIVER = "KIE_CONNECTION_DRIVER";
    public static final String KIE_CONNECTION_USER = "KIE_CONNECTION_USER";
    public static final String KIE_CONNECTION_PASSWORD = "KIE_CONNECTION_PASSWORD";
    public static final String KIE_CONNECTION_DATABASE = "KIE_CONNECTION_DATABASE";

    public static String getDefaultDatabaseName() {
        return "kie";
    }

    public static String getDriver(final KieImageType type) {
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return "mysql";
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return "postgres";
        if (KieImageTypeManager.KIE_H2_IN_MEMORY.equals(type)) return "h2";
        return null;
    }
    
    public static String getAlias(final KieImageType type) {
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return "mysql";
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return "postgres";
        return null;
    }

    public static String getConnectionUser(final KieImageType type) {
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return "root";
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return "postgres";
        return "sa";
    }

    public static String getConnectionPassword(final KieImageType type) {
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return "mysql";
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return "postgres";
        return "sa";
    }
    
    public static String[] buildPasswordEnvVar(final KieImageType type) {
        String pass = getConnectionPassword(type);
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return new String[] {"MYSQL_ROOT_PASSWORD", pass};
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return new String[] {"POSTGRES_PASSWORD", pass};
        return null;
    }

    public static int getDefaultPort(final KieImageType type) {
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return 3306;
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return 5432;
        return -1;
    }

    public static int getPublicPort(final KieContainer container) {
        
        if (container != null) {
            final int port = getDefaultPort(container.getType());
            if (port > -1) {
                List<KieContainerPort> ports = container.getPorts();
                if (ports != null) {
                    for (final KieContainerPort _port : ports) {
                        if (_port.getPrivatePort() == port) return _port.getPublicPort();
                    }
                }
            }
        }

        return -1;
    }
    
    public static int getPublicPort(final int port, final KieContainer container) {
        if (container != null) {
            List<KieContainerPort> ports = container.getPorts();
            if (ports != null) {
                for (final KieContainerPort _port : ports) {
                    if (_port.getPrivatePort() == port) return _port.getPublicPort();
                }
            }
        }

        return -1;
    }

    public static String getJdbcUrl(final KieImageType type, final String host , final int port, final String dbName) {
        if (KieImageTypeManager.KIE_MYSQL.equals(type)) return "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        if (KieImageTypeManager.KIE_POSTGRESQL.equals(type)) return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        return "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    }

    public static Map<String, String> buildEnvsForDbConnection(final KieImageType type, final String host , final int port, final String dbName) {
        Map<String, String> envs = new LinkedHashMap<String, String>();
        envs.put(KIE_CONNECTION_URL, getJdbcUrl(type, host, port, dbName));
        envs.put(KIE_CONNECTION_DRIVER, getDriver(type));
        envs.put(KIE_CONNECTION_USER, getConnectionUser(type));
        envs.put(KIE_CONNECTION_PASSWORD, getConnectionPassword(type));
        if (dbName != null) envs.put(KIE_CONNECTION_DATABASE, dbName);
        return envs;
    }
    
}
