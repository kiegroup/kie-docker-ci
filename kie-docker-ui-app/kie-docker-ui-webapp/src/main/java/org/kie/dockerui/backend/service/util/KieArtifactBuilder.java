package org.kie.dockerui.backend.service.util;

import org.kie.dockerui.shared.model.KieArtifact;

import java.nio.file.Path;
import java.nio.file.Paths;

public class KieArtifactBuilder {

    public static KieArtifact build(final String filePath) {
        if (isEmpty(filePath)) return null;
        Path path = Paths.get(filePath);
        final String fileName = path.getFileName().toString();
        final String timestamp = path.getParent().getFileName().toString();
        String[] coords = parseMavenCoordinates(fileName);
        if (coords != null) {
            return new KieArtifact(filePath, nullSafe(timestamp), nullSafe(fileName), nullSafe(coords[0]), nullSafe(coords[1]), nullSafe(coords[2]), nullSafe(coords[3]));
        }
        return new KieArtifact(filePath, "", "", "", "", "", "");
    }
    
    private static String nullSafe(final String s) {
        return s != null ? s : "";
    }
    
    private static String[] getFileExtension(final String f) {
        if (!isEmpty(f)) {
            final int lastDot = f.lastIndexOf(".");
            final String name = f.substring(0, lastDot);
            final String ext = f.substring(lastDot + 1, f.length());
            if (lastDot > 0) return new String[] { name, ext  };
        }
        return null;
    }
    
    private static String[] parseMavenCoordinates(final String fileName) {
        if (isEmpty(fileName)) return null;
        
        String artifactId = null;
        String version = null;
        String type = null;
        String classifer = null;

        final String[] _ne = getFileExtension(fileName);
        if (_ne == null) return null;

        final String name = _ne[0];
        type = _ne[1];
        final int lastDash = fileName.lastIndexOf("-");
        if (lastDash > 0) {
            artifactId = fileName.substring(0, lastDash);
            version = fileName.substring(lastDash + 1, fileName.length());
        } else {
            artifactId = name;
        }
        
        return new String[] {artifactId, version, type, classifer};
    }
    
    private static boolean isEmpty(final String s) {
        return s == null || s.trim().length() == 0;
    }

    public static void main(String[] args) {
        String[] files = { "/home/docker/git/kie-docker-integration/kie-artifacts/target/artifacts/20150621_1902/plexus-container-default-1.0-alpha-9-stable-1.jar" ,
                            "/home/docker/git/kie-docker-integration/kie-artifacts/target/artifacts/20150621_1902/kie-wb-distribution-wars-cdi1.0-6.3.0-SNAPSHOT-eap6_4.war",
                            "/home/docker/git/kie-docker-integration/kie-artifacts/target/artifacts/20150621_1902/kie-drools-wb-distribution-wars-6.3.0-SNAPSHOT-wildfly8.war"};

        for (String f : files) {
            KieArtifact artifact = build(f);
            if (artifact != null) System.out.println(artifact.toString());
            else System.out.println("** Artifact is null for '" + f + "'");
        }
        
    }
}
