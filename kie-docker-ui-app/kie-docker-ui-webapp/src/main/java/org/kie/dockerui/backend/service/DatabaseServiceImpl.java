package org.kie.dockerui.backend.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kie.dockerui.client.service.DatabaseService;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServiceImpl.class.getName());
    
    private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    private static final String DRIVER_POSTGRES = "org.postgresql.Driver";
    
    private static final DockerServiceImpl dockerService = new DockerServiceImpl();
    private static final SettingsServiceImpl settingsService = new SettingsServiceImpl();
    
    @Override
    public void createDatabase(String containerId, KieImageType imageType, String dbName) throws Exception {
        if (isEmpty(containerId)) {
            LOGGER.error("No container id specified for creating the database.");
            return;
        }
        if (isEmpty(dbName)) {
            LOGGER.error("No database name specified.");
            return;
        }
        if (imageType == null) {
            LOGGER.error("No database type specified.");
            return;
        }
        
        if (!KieImageTypeManager.KIE_MYSQL.equals(imageType) && !KieImageTypeManager.KIE_POSTGRESQL.equals(imageType)) {
            LOGGER.error("Database type not supported. Type: " + imageType.getName());
            return;
        }

        final KieContainer container = dockerService.getContainer(containerId);
        if (container == null) {
            LOGGER.error("Container with id " + containerId + "not found.");
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
    
}
