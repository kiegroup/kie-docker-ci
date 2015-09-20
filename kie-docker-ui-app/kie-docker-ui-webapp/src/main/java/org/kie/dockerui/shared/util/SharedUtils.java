package org.kie.dockerui.shared.util;

import org.kie.dockerui.shared.model.*;
import org.kie.dockerui.shared.settings.Settings;

import java.util.List;

public class SharedUtils {

    public static boolean isKieApp(final KieImage image) {
        return image != null && image.getType() != null &&
                KieImageCategory.KIEAPP.equals(image.getType().getCategory());
    }

    public static boolean isKieApp(final KieContainer container) {
        return container != null && container.getType() != null &&
                KieImageCategory.KIEAPP.equals(container.getType().getCategory());
    }

    public static boolean supportsDatabase(final KieImageType type) {
        return type != null && KieImageCategory.KIEAPP.equals(type.getCategory()) &&
               type.getSupportedCategories() != null &&
               type.getSupportedCategories().contains(KieImageCategory.DBMS);
    }
    
    public static boolean supportsDatabase(final KieContainer container) {
        return container != null && container.getType() != null &&
                KieImageCategory.KIEAPP.equals(container.getType().getCategory()) &&
                container.getType().getSupportedCategories() != null &&
                container.getType().getSupportedCategories().contains(KieImageCategory.DBMS);
    }
    
    public static boolean getContainerStatus(final KieContainer container) {
        // TODO: Improve status detection.
        return container.getStatus() != null && container.getStatus().contains("Up");
    }

    public static int getPublicPort(final int port, final KieContainer container) {
        if (container != null) {
            List<KieContainerPort> ports = container.getPorts();
            if (ports != null) {
                for (final KieContainerPort _port : ports) {
                    if (_port.getPrivatePort() == port) return _port.getPublicPort();
                }
            }
        }

        return -1;
    }

    public static String getRepository(final String image) {
        final int repoPos = image.lastIndexOf("/");
        String _n = repoPos > -1 ? image.substring(repoPos + 1) : image;
        final String imageName = _n.substring(0, _n.indexOf(":") - 1 );
        return imageName;
    }
    
    public static String getImage(final String registry, final String repository, final String tag) {
        String _r = (registry != null && registry.trim().length() > 0) ? registry + "/" : "";
        String _p = (repository != null && repository.trim().length() > 0) ? repository : "";
        String _t = (tag != null && tag.trim().length() > 0) ? ":" + tag: "";
        return _r + _p + _t;
    }
 
    public static String getPullAddress(final KieImage image, final Settings settings) {
        final String host = settings.getPublicHost();
        final int registryPort = settings.getRegistryPort();
        return "docker pull " + host + ":" + registryPort + "/" + image.getRepository() + ":" + image.getTags().iterator().next();
    }
}
