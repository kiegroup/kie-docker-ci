package org.kie.dockerui.shared.model;

import java.util.Map;

public class KieContainerStartArguments {

    private KieImageType imageType;
    private String image;
    private String containerName;
    private KieImageType dbImageType;
    private String dbImage;
    private String dbContainerName;
    private Map<String, String> envs;

    public KieContainerStartArguments(KieImageType imageType, String image, String containerName, Map<String, String> envs) {
        this.imageType = imageType;
        this.image = image;
        this.containerName = containerName;
        this.envs = envs;
    }

    public KieContainerStartArguments(KieImageType imageType, String image, String containerName, KieImageType dbImageType, String dbImage, String dbContainerName, Map<String, String> envs) {
        this.imageType = imageType;
        this.image = image;
        this.containerName = containerName;
        this.dbImageType = dbImageType;
        this.dbImage = dbImage;
        this.dbContainerName = dbContainerName;
        this.envs = envs;
    }

    public String getImage() {
        return image;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getDbImage() {
        return dbImage;
    }

    public String getDbContainerName() {
        return dbContainerName;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public KieImageType getImageType() {
        return imageType;
    }

    public KieImageType getDbImageType() {
        return dbImageType;
    }
}
