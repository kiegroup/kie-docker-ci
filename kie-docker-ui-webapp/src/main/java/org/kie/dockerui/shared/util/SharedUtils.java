package org.kie.dockerui.shared.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.kie.dockerui.shared.model.*;
import org.kie.dockerui.shared.settings.Settings;

import java.util.List;

public class SharedUtils {

    public static final String IMAGE_TAG_PATTERN = "(\\d+.\\d+.\\d+.*)\\.(.*)";
    
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
                supportsCategory(type, KieImageCategory.DBMS);
    }
    
    public static boolean supportsDatabase(final KieContainer container) {
        return container != null && container.getType() != null &&
                KieImageCategory.KIEAPP.equals(container.getType().getCategory()) &&
                supportsCategory(container.getType(), KieImageCategory.DBMS);
    }
    
    public static boolean supportsCategory(final KieImageType type, final KieImageCategory category) {
        final KieImageCategory[] supported = type.getSupportedCategories();
        if (supported != null && supported.length > 0) {
            for (final KieImageCategory category1 : supported) {
                if (category1.equals(category)) return true;
            }
        }
        return false;
    } 
    
    public static boolean getContainerStatus(final KieContainer container) {
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

    public static String[] parseTag(final String tag) {
        String[] result = null;
        if (tag != null) {
            RegExp regExp = RegExp.compile(IMAGE_TAG_PATTERN);
            MatchResult matcher = regExp.exec(tag);
            boolean matchFound = matcher != null; 

            if (matchFound) {
                result = new String[matcher.getGroupCount()];
                // Get all groups for this match
                for (int i = 0; i < matcher.getGroupCount(); i++) {
                    String groupStr = matcher.getGroup(i);
                    result[i] = groupStr;
                }
            }
        }
        return result;
    }
 
    public static String getPullAddress(final KieImage image, final Settings settings) {
        final String host = settings.getPublicHost();
        final int registryPort = settings.getRegistryPort();
        return "docker pull " + host + ":" + registryPort + "/" + image.getRepository() + ":" + image.getTags().iterator().next();
    }
}
