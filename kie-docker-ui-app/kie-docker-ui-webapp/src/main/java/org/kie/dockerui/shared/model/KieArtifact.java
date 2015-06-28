package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class KieArtifact implements IsSerializable {
    
    private String absoluteFilePath;
    private String fileName;
    private String timestamp;
    private String artifactId;
    private String version;
    private String type;
    private String classifier;

    public KieArtifact() {
    }

    public KieArtifact(String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
    }

    public KieArtifact(String absoluteFilePath, String timestamp, String fileName, String artifactId, String version, String type, String classifier) {
        this.absoluteFilePath = absoluteFilePath;
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    @Override
    public String toString() {
        return new StringBuilder("**KieArtifact**  ")
                .append(absoluteFilePath).append(" / ").append(fileName).append(" / ").append(timestamp)
                .append(" / ").append(artifactId).append(" / ").append(version).append(" / ")
                .append(type).append(" / ").append(classifier).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (absoluteFilePath == null) return false;

        try {
            KieArtifact d = (KieArtifact) obj;
            return absoluteFilePath.equals(d.absoluteFilePath);
        } catch (ClassCastException e) {
            return false;
        }
    }

}
