package org.kie.dockerui.backend.service.builder;

import org.kie.dockerui.shared.model.KieArtifact;

import java.nio.file.Path;
import java.nio.file.Paths;

public class KieArtifactBuilder {

    public static KieArtifact build(final String filePath) {
        if (isEmpty(filePath)) return null;
        Path path = Paths.get(filePath);
        final String fileName = path.getFileName().toString();
        final String[] extension = getFileExtension(fileName);
        final String timestamp = path.getParent().getFileName().toString();
        return new KieArtifact(filePath, timestamp, extension[0], extension[1]);
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
