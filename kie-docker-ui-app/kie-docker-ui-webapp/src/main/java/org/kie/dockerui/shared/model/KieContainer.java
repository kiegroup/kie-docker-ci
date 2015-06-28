package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;
import java.util.List;

public class KieContainer implements IsSerializable, Comparable<KieContainer> {
    private String id;
    private String truncId;
    private String image;
    // The repository part of the full image
    private String repository;
    // Registry
    private String registry;
    // Tag name
    private String tag;
    private String name;
    private String command;
    private Date created;
    private List<KieContainerPort> ports;
    private String status;
    private KieImageType type;
    private List<KieImageType> subTypes;
    private KieAppStatus appStatus;

    public KieContainer() {
        
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTruncId() {
        return truncId;
    }

    public void setTruncId(String truncId) {
        this.truncId = truncId;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<KieContainerPort> getPorts() {
        return ports;
    }

    public void setPorts(List<KieContainerPort> ports) {
        this.ports = ports;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public KieImageType getType() {
        return type;
    }

    public void setType(KieImageType type) {
        this.type = type;
    }

    public List<KieImageType> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(List<KieImageType> subTypes) {
        this.subTypes = subTypes;
    }

    public KieAppStatus getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(KieAppStatus appStatus) {
        this.appStatus = appStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (id == null) return false;

        try {
            KieContainer d = (KieContainer) obj;
            return id.equals(d.id);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public int compareTo(KieContainer o) {
        return (o == null || o.getId() == null) ? -1 : -o.getId().compareTo(getId());
    }
}
