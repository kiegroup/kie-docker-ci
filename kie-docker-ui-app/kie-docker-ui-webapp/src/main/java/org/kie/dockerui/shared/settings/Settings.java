package org.kie.dockerui.shared.settings;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Settings implements IsSerializable{

    private String protocol;
    private String privateHost;
    private String publicHost;
    private String user;
    private int restApiPort;
    private boolean registryEnabled;
    private int registryPort;
    private String kieRepositoryName;
    private String artifactsPath;
    private String jenkinsJobURL;
    private boolean isStatusManagerEnabled;
    private boolean pullEnabled;
    private long pullInterval;
    private String mysqlImage;
    private String postgresImage;

    public Settings() {
        
    }

    public String getPrivateHost() {
        return privateHost;
    }

    public void setPrivateHost(String privateHost) {
        this.privateHost = privateHost;
    }

    public String getPublicHost() {
        return publicHost;
    }

    public void setPublicHost(String publicHost) {
        this.publicHost = publicHost;
    }

    public void setRestApiPort(int restApiPort) {
        this.restApiPort = restApiPort;
    }

    public void setKieRepositoryName(String kieRepositoryName) {
        this.kieRepositoryName = kieRepositoryName;
    }

    public int getRestApiPort() {
        return restApiPort;
    }

    public String getKieRepositoryName() {
        return kieRepositoryName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isRegistryEnabled() {
        return registryEnabled;
    }

    public void setRegistryEnabled(boolean registryEnabled) {
        this.registryEnabled = registryEnabled;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getArtifactsPath() {
        return artifactsPath;
    }

    public void setArtifactsPath(String artifactsPath) {
        this.artifactsPath = artifactsPath;
    }

    public String getJenkinsJobURL() {
        return jenkinsJobURL;
    }

    public void setJenkinsJobURL(String jenkinsJobURL) {
        this.jenkinsJobURL = jenkinsJobURL;
    }

    public boolean isStatusManagerEnabled() {
        return isStatusManagerEnabled;
    }

    public void setIsStatusManagerEnabled(boolean isStatusManagerEnabled) {
        this.isStatusManagerEnabled = isStatusManagerEnabled;
    }

    public void setPullEnabled(boolean pullEnabled) {
        this.pullEnabled = pullEnabled;
    }

    public void setPullInterval(long pullInterval) {
        this.pullInterval = pullInterval;
    }

    public boolean isPullEnabled() {
        return pullEnabled;
    }

    public long getPullInterval() {
        return pullInterval;
    }

    public String getMysqlImage() {
        return mysqlImage;
    }

    public void setMysqlImage(String mysqlImage) {
        this.mysqlImage = mysqlImage;
    }

    public String getPostgresImage() {
        return postgresImage;
    }

    public void setPostgresImage(String postgresImage) {
        this.postgresImage = postgresImage;
    }
}
