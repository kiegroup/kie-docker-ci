package org.kie.dockerui.backend.service.util;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.kie.dockerui.backend.KieStatusManager;
import org.kie.dockerui.backend.service.DockerServiceImpl;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.*;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KieDockerArtifactBuilder {

    private static final Pattern IMAGE_TAG_PATTERN = Pattern.compile("^(.+?)(?::([^:/]+))?$");

    private static final DockerServiceImpl dockerService = new DockerServiceImpl();
    private static final KieStatusManager statusManager = KieStatusManager.getInstance();
    
    public static KieImage build(Image image) {
        if (image == null) return null;
        
        KieImage kieImage = new KieImage();
        kieImage.setId(image.getId());
        kieImage.setTruncId(image.getId().substring(0,12));
        final long cDateTime = image.getCreated();
        final Date cDate = new Date(cDateTime * 1000);
        kieImage.setCreated(cDate);
        kieImage.setSize(image.getSize());
        kieImage.setVirtualSize(image.getVirtualSize());
        
        final String[] repoTags = image.getRepoTags();
        final String[] _n = parseImageName(image.getRepoTags()[0], null);
        kieImage.setRegistry(_n[0]);
        kieImage.setRepository(_n[1]);
        if (repoTags != null && repoTags.length > 0) {
            Set<String> tags = new LinkedHashSet<String>();
            for (final String repoTag : repoTags) {
                final String[] _t = parseImageName(repoTag, null);
                tags.add(_t[2]);
            }
            kieImage.setTags(tags);
        }

        // Container types.
        final KieImageType kieAppType = KieImageTypeManager.getKIEAppType(kieImage.getRepository());
        final KieImageType appServerType = KieImageTypeManager.getAppServerType(kieImage.getRepository());

        KieImageType type = null;
        final List<KieImageType> types = new LinkedList<KieImageType>();
        if (kieAppType != null) type = kieAppType;
        if (appServerType != null && type != null) types.add(appServerType);
        else type = appServerType;
        if (type == null) type = KieImageTypeManager.KIE_OTHER;
        kieImage.setType(type);
        if (!types.isEmpty()) kieImage.setSubTypes(types);


        // Application status.
        if (SharedUtils.isKieApp(kieImage)) {
            final String i = SharedUtils.getImage(kieImage.getRegistry(), kieImage.getRepository(), kieImage.getTags().iterator().next());
            final KieAppStatus status = statusManager.getStatus(i);
            kieImage.setAppStatus(status != null ? status : KieAppStatus.NOT_EVALUATED);
        }
        
        return kieImage;
    }
    
    public static KieContainer build(Container container) {
        if (container == null) return null;
        
        KieContainer kieContainer = new KieContainer();
        kieContainer.setId(container.getId());
        kieContainer.setTruncId(container.getId().substring(0, 12));
        kieContainer.setImage(container.getImage());
        final String cName = container.getNames()[0];
        kieContainer.setName(cName.substring(1, cName.length()));
        kieContainer.setCommand(container.getCommand());
        final long cDateTime = container.getCreated();
        final Date cDate = new Date(cDateTime * 1000);
        kieContainer.setCreated(cDate);
        kieContainer.setStatus(container.getStatus());
        final String[] _n = parseImageName(container.getImage(), null);
        kieContainer.setRegistry(_n[0]);
        kieContainer.setRepository(_n[1]);
        kieContainer.setTag(_n[2]);
        
        // Ports.
        final Container.Port[] ports = container.getPorts();
        if (ports != null) {
            final List<KieContainerPort> kiePorts = new LinkedList<KieContainerPort>();
            for (final Container.Port port : ports) {
                final KieContainerPort kiePort = new KieContainerPort();
                kiePort.setIp(port.getIp());
                kiePort.setPrivatePort(port.getPrivatePort());
                if (port.getPublicPort() != null) kiePort.setPublicPort(port.getPublicPort());
                kiePort.setType(port.getType());
                kiePorts.add(kiePort);
            }
            kieContainer.setPorts(kiePorts);
        }
        
        // Container types.
        final KieImageType kieAppType = KieImageTypeManager.getKIEAppType(kieContainer.getRepository());
        final KieImageType appServerType = KieImageTypeManager.getAppServerType(kieContainer.getRepository());

        KieImageType dbmsType = null;
        if (SharedUtils.supportsDatabase(kieAppType)) {
            dbmsType = KieImageTypeManager.getDBMSType(container.getId(), kieContainer.getRepository(), new String[]{kieContainer.getTag()},
                    dockerService.inspect(container.getId()));    
        }
        
        KieImageType type = null;
        final List<KieImageType> types = new LinkedList<KieImageType>();
        if (kieAppType != null) type = kieAppType;
        if (appServerType != null && type != null) types.add(appServerType);
        else type = appServerType;
        if (dbmsType != null && type != null) types.add(dbmsType);
        if (type == null) type = KieImageTypeManager.KIE_OTHER;
        kieContainer.setType(type);
        if (!types.isEmpty()) kieContainer.setSubTypes(types);

        // Application status.
        if (SharedUtils.isKieApp(kieContainer)) {
            KieAppStatus status = statusManager.getStatus(kieContainer.getImage());
            if (status == null) {
                status = statusManager.getStatus(kieContainer);
                statusManager.addStatus(kieContainer.getImage(), status);
            }
            kieContainer.setAppStatus(status != null ? status : KieAppStatus.NOT_EVALUATED);
        }
        
        return kieContainer;
    }

    /**
     * Create an image name with a tag. If a tag is provided (i.e. is not null) then this tag is used.
     * Otherwise the tag of the provided name is used (if any).
     *
     * @param fullName The fullname of the image in Docker format. I
     * @param givenTag tag to use. Can be null in which case the tag specified in fullName is used.
     */
    private static String[] parseImageName(String fullName,String givenTag) {
        if (fullName == null) {
            throw new NullPointerException("Image name must not be null");
        }

        String registry = "";
        String repository = "";
        String tag = "";
        Matcher matcher = IMAGE_TAG_PATTERN.matcher(fullName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(fullName + " is not a proper image name ([registry/][repo][:port]");
        }
        tag = givenTag != null ?
                givenTag :
                matcher.groupCount() > 1 ? matcher.group(2) : null;
        String rest = matcher.group(1);

        String[] parts = rest.split("\\s*/\\s*");
        if (parts.length == 1) {
            registry = null;
            repository = parts[0];
        } else if (parts.length >= 2) {
            if (isRegistry(parts[0])) {
                registry = parts[0];
                repository = joinTail(parts);
            } else {
                registry = null;
                repository = rest;
            }
        }
        
        return new String[] {registry, repository, tag};
    }

    private static String joinTail(String[] parts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1;i < parts.length; i++) {
            builder.append(parts[i]);
            if (i < parts.length - 1) {
                builder.append("/");
            }
        }
        return builder.toString();
    }

    private static boolean isRegistry(String part) {
        return part.contains(".") || part.contains(":");
    }
    
    
    
    
}
