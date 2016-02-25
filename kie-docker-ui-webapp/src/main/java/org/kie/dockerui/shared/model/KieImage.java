package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class KieImage implements IsSerializable, Comparable<KieImage> {
    private String id;
    private String truncId;
    // The repository part of the full image
    private String repository;
    // Registry
    private String registry;
    // Tag name
    private Set<String> tags;
    private Date created;
    private long size;
    private long virtualSize;
    private KieImageType type;
    private List<KieImageType> subTypes;
    private KieAppStatus appStatus;

    public KieImage() {
        
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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
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
            KieImage d = (KieImage) obj;
            return id.equals(d.id);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public int compareTo(KieImage o) {
        return (o == null || o.getId() == null) ? -1 : -o.getId().compareTo(getId());
    }
}
