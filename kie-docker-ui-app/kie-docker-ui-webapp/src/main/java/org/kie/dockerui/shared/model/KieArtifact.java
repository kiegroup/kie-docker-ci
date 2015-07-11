package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class KieArtifact implements IsSerializable {
    
    private String absoluteFilePath;
    private String fileName;
    private String extension;
    private String timestamp;

    public KieArtifact() {
    }

    public KieArtifact(String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
    }

    public KieArtifact(String absoluteFilePath, String timestamp, String fileName, String extension) {
        this.absoluteFilePath = absoluteFilePath;
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.extension = extension;
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

    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return new StringBuilder("**KieArtifact**  ")
                .append(absoluteFilePath).append(" / ").append(fileName).append(" / ").append(timestamp).toString();
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
