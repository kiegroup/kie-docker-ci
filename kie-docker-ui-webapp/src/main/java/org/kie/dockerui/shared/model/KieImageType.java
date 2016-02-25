package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class KieImageType implements IsSerializable {
    // MUST match with the image name to categorize.
    private String id;
    private String name;
    private String artifactId;
    private KieImageCategory category;
    private KieImageCategory[] supportedCategories;
    private String contextPath;
    private String siteContextPath;
    private Scope scope = Scope.NAMING_CONVENTION;
    
    public enum Scope {
        NAMING_CONVENTION,
        RUNTIME;
    }
    
    public KieImageType() {
        
    }

    public KieImageType(String id, String name, KieImageCategory category) {
        this.category = category;
        this.name = name;
        this.id = id;
    }

    public KieImageType(final String id, final String name, final KieImageCategory category, final String contextPath) {
        this.category = category;
        this.name = name;
        this.id = id;
        this.contextPath = contextPath;
    }

    public String getId() {
        return id;
    }

    public KieImageType setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public KieImageType setName(String name) {
        this.name = name;
        return this;
    }

    public KieImageCategory getCategory() {
        return category;
    }

    public KieImageType setCategory(KieImageCategory category) {
        this.category = category;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public KieImageType setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getSiteContextPath() {
        return siteContextPath;
    }

    public KieImageType setSiteContextPath(String siteContextPath) {
        this.siteContextPath = siteContextPath;
        return this;
    }

    public KieImageCategory[] getSupportedCategories() {
        return supportedCategories;
    }

    public KieImageType setSupportedCategories(KieImageCategory[] supportedCategories) {
        this.supportedCategories = supportedCategories;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public KieImageType setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public KieImageType setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (id == null) return false;

        try {
            KieImageType d = (KieImageType) obj;
            return id.equals(d.id);
        } catch (ClassCastException e) {
            return false;
        }
    }
}
