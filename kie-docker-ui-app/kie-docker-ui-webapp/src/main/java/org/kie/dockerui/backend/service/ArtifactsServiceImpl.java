package org.kie.dockerui.backend.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kie.dockerui.backend.service.util.KieArtifactBuilder;
import org.kie.dockerui.client.service.ArtifactsService;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.shared.model.KieArtifact;
import org.kie.dockerui.shared.settings.Settings;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

// TODO: Cache.
public class ArtifactsServiceImpl extends RemoteServiceServlet implements ArtifactsService {

    private static final int TIMEOUT = 30000;
    private static final String NO_VALUE = "<none>";

    private final SettingsService settingsService = new SettingsServiceImpl();

    public ArtifactsServiceImpl() {
        
    }

    public ArtifactsServiceImpl(Object delegate) {
        super(delegate);
    }

    @Override
    public List<KieArtifact> list() {
        final Settings settings = settingsService.getSettings();
        Path path= Paths.get(settings.getArtifactsPath());
        final List<Path> files=new LinkedList<Path>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            doLog("[ERROR] ArtifactsServiceImpl#list() - Message: " + e.getMessage());
            e.printStackTrace();
        }
        
        final List<KieArtifact> artifacts = new LinkedList<KieArtifact>();
        for (final Path file : files) {
            final KieArtifact artifact = KieArtifactBuilder.build(file.toString());
            if (artifact != null) artifacts.add(artifact);
        }
        return artifacts;
    }
    
    private static void doLog(String message) {
        System.out.println(message);
    }

}
