package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class KieContainerPort implements IsSerializable {
    private String ip;
    private int privatePort;
    private int publicPort;
    private String type;

    public KieContainerPort() {
        
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPrivatePort() {
        return privatePort;
    }

    public void setPrivatePort(int privatePort) {
        this.privatePort = privatePort;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
