package org.kie.dockerui.backend.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kie.dockerui.client.service.DatabaseService;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.DatabaseUtils;

import java.sql.*;

/** 
 * Commands for connection to db container:
 *  - mysql -u root -pmysql --host=localhost --port=49155 --protocol=TCP
 *  - psql -U postgres -h localhost -p 49157 -W
 */
// TODO: security, override properties by using java system properties, etc.
public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

    private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    private static final String DRIVER_POSTGRES = "org.postgresql.Driver";
    
    private static final DockerServiceImpl dockerService = new DockerServiceImpl();
    private static final SettingsServiceImpl settingsService = new SettingsServiceImpl();
    
    @Override
    public void createDatabase(String containerId, KieImageType imageType, String dbName) throws Exception {
        if (isEmpty(containerId)) {
            doLog("No container id specified for creating the database.");
            return;
        }
        if (isEmpty(dbName)) {
            doLog("No database name specified.");
            return;
        }
        if (imageType == null) {
            doLog("No database type specified.");
            return;
        }
        
        if (!KieImageTypeManager.KIE_MYSQL.equals(imageType) && !KieImageTypeManager.KIE_POSTGRESQL.equals(imageType)) {
            doLog("Database type not supported. Type: " + imageType.getName());
            return;
        }

        final KieContainer container = dockerService.getContainer(containerId);
        if (container == null) {
            doLog("Container with id " + containerId + "not found.");
            return;
        }

        final boolean isMySQL = KieImageTypeManager.KIE_MYSQL.equals(imageType);
        final String driver = isMySQL ? DRIVER_MYSQL : DRIVER_POSTGRES;
        final String user = DatabaseUtils.getConnectionUser(imageType);
        final String password = DatabaseUtils.getConnectionPassword(imageType);
        final String host = settingsService.getSettings().getPrivateHost();
        final int port = DatabaseUtils.getPublicPort(container);
        final String url = DatabaseUtils.getJdbcUrl(isMySQL ? KieImageTypeManager.KIE_MYSQL : KieImageTypeManager.KIE_POSTGRESQL, host, port, "");
        createDatabase(driver, url, user, password, dbName);
    }

    private void createDatabase(String driver, String url, String user, String password, String dbName) throws Exception {
        Class.forName(driver);
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            statement = conn.createStatement();
            final String createDbSQL = "CREATE DATABASE " + dbName;
            statement.executeUpdate(createDbSQL);
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw e;  
                } 
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw e;
                } 
            }
        }
    }
    
    private boolean isEmpty(final String string) {
        return string == null || string.trim().length() == 0;
    }
    
    private static void doLog(String message) {
        System.out.println(message);
    }

}
