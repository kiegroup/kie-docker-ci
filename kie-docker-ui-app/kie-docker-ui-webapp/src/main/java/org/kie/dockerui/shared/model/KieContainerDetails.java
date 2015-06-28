package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Map;

public class KieContainerDetails implements IsSerializable {
    private String ipAddress;
    private int containerPid;
    private String containerStartedAt;
    private String containerFinishedAt;
    private Map<String, Map<String, String>> portMappings;
    private String[] containerArgs;
    private String[] envVars;
    
    public KieContainerDetails() {

    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getContainerPid() {
        return containerPid;
    }

    public void setContainerPid(int containerPid) {
        this.containerPid = containerPid;
    }

    public String getContainerStartedAt() {
        return containerStartedAt;
    }

    public void setContainerStartedAt(String containerStartedAt) {
        this.containerStartedAt = containerStartedAt;
    }

    public String getContainerFinishedAt() {
        return containerFinishedAt;
    }

    public void setContainerFinishedAt(String containerFinishedAt) {
        this.containerFinishedAt = containerFinishedAt;
    }

    public Map<String, Map<String, String>> getPortMappings() {
        return portMappings;
    }

    public void setPortMappings(Map<String, Map<String, String>> portMappings) {
        this.portMappings = portMappings;
    }

    public String[] getContainerArgs() {
        return containerArgs;
    }

    public void setContainerArgs(String[] containerArgs) {
        this.containerArgs = containerArgs;
    }

    public String[] getEnvVars() {
        return envVars;
    }

    public void setEnvVars(String[] envVars) {
        this.envVars = envVars;
    }
}
