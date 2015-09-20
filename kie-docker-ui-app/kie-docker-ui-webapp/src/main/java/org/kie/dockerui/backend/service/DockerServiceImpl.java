package org.kie.dockerui.backend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Link;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.kie.dockerui.backend.KieStatusManager;
import org.kie.dockerui.backend.service.builder.KieDockerArtifactBuilder;
import org.kie.dockerui.backend.util.Timer;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.shared.model.*;
import org.kie.dockerui.shared.util.SharedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DockerServiceImpl extends RemoteServiceServlet implements DockerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerServiceImpl.class.getName());
    
    private static final int TIMEOUT = 30000;
    private static final String NO_VALUE = "<none>";

    private final SettingsService settingsService = new SettingsServiceImpl();

    public DockerServiceImpl() {
        
    }

    public DockerServiceImpl(Object delegate) {
        super(delegate);
    }

    @Override
    public KieListCommandResponse list() {

        Timer timer = new Timer();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start listing images and containers.");
            timer.start();
        }
        
        final List<KieImage> images = listImages();
        final List<KieContainer> containers = listContainers();

        if (LOGGER.isDebugEnabled()) {
            timer.end();
            LOGGER.debug("Images and containers listed. Elapsed time = " + timer.getTotalTime());
        }
        
        return new KieListCommandResponse(images, containers);
    }

    @Override
    public List<KieContainer> listContainers() {

        Timer timer = new Timer();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start listing containers.");
            timer.start();
        }

        List<KieContainer> result = null;
        DockerClient dockerClient = getClient();
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        if (containers != null) {
            result = new LinkedList<KieContainer>();
            for (Container container : containers) {
                KieContainer kieContainer = KieDockerArtifactBuilder.build(container);
                if (kieContainer != null) result.add(kieContainer);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            timer.end();
            LOGGER.debug("Containers listed. Elapsed time = " + timer.getTotalTime());
        }
        
        return result;
    }

    @Override
    public List<KieContainer> listContainers(Collection<String> ids) {
        List<KieContainer> result = null;

        if (ids != null && !ids.isEmpty()) {

            Timer timer = new Timer();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Start listing containers.");
                timer.start();
            }
            
            DockerClient dockerClient = getClient();
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            if (containers != null) {
                result = new LinkedList<KieContainer>();
                for (Container container : containers) {
                    KieContainer kieContainer = KieDockerArtifactBuilder.build(container);
                    if (ids.contains(container.getId())) result.add(kieContainer);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                timer.end();
                LOGGER.debug("Containers listed. Elapsed time = " + timer.getTotalTime());
            }
            
        }

        return result;
    }

    @Override
    public KieContainer getContainer(String containerId) {
        DockerClient dockerClient = getClient();
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        if (containers != null) {
            for (Container container : containers) {
                if (container.getId().equals(containerId)) return KieDockerArtifactBuilder.build(container); 
            }
        }

        return null;
    }

    @Override
    public KieImage getImage(String imageId) {
        List<KieImage> images= listImages();
        if (images != null) {
            for (KieImage image : images) {
                if (image.getId().equals(imageId)) return image;
            }
        }

        return null;
    }

    @Override
    public List<KieImage> listImages() {
        List<KieImage> result = null;

        Timer timer = new Timer();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start listing images.");
            timer.start();
        }
        
        DockerClient dockerClient = getClient();
        List<Image> images = dockerClient.listImagesCmd().withShowAll(true).exec();
        if (images != null) {
            result = new LinkedList<KieImage>();
            for (Image image : images) {
                KieImage kieImage = KieDockerArtifactBuilder.build(image);
                
                if (kieImage != null && !NO_VALUE.equals(kieImage.getRepository())) {
                    result.add(kieImage);
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            timer.end();
            LOGGER.debug("Images listed. Elapsed time = " + timer.getTotalTime());
        }
        
        return result;
    }

    @Override
    public void updateStatus(String containerId) {
        if (containerId != null) {
            KieStatusManager statusManager = KieStatusManager.getInstance();
            KieContainer container = getContainer(containerId);
            statusManager.updateStatus(container);
        }
    }

    @Override
    public KieDockerSummary summary() {
        final List<KieImage> images = listImages();
        final List<KieContainer> containers = listContainers();

        // Obtain KIE images.
        final List<KieImage> kieImages = new LinkedList<KieImage>();
        if (images != null) {
            for (final KieImage image : images) {
                if (SharedUtils.isKieApp(image)) kieImages.add(image);
            }
        }

        // Obtain KIE containers.
        final List<KieContainer> kieContainers = new LinkedList<KieContainer>();
        if (containers != null) {
            for (final KieContainer container : containers) {
                if (SharedUtils.isKieApp(container)) kieContainers.add(container);
            }
        }
        
        // Create and populate the summary model.
        final int imagesCount = images != null ? images.size() : 0;
        final int containersCount = containers != null ? containers.size() : 0;
        KieDockerSummary summary = new KieDockerSummary();
        summary.setImagesCount(imagesCount);
        summary.setContainersCount(containersCount);
        summary.setKieImagesCount(kieImages.size());
        summary.setKieContainersCount(kieContainers.size());
        return summary;
    }

    @Override
    public String create(String image, String name, String[] env, String[] linking) {
        DockerClient dockerClient = getClient();
        
        if (image == null  || image.trim().length() == 0) {
            LOGGER.error("Container's image name to create is empty");
            return null;
        }
        
        final CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image)
                .withPublishAllPorts(true)
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withAttachStdin(true)
                .withTty(true);
        if (linking != null) {
            final String linkName = linking[0];
            final String linkAlias = linking[1];
            final Link link = new Link(linkName, linkAlias);
            createContainerCmd.withLinks(link);
        }
        if (name != null && name.trim().length() > 0) createContainerCmd.withName(name);
        if (env != null && env.length > 0) createContainerCmd.withEnv(env);
        
        final CreateContainerResponse createContainerResponse = createContainerCmd.exec();
        final String id = createContainerResponse.getId();
        
        return id;
    }

    @Override
    public void stop(String containerId) {
        DockerClient dockerClient = getClient();
        dockerClient.stopContainerCmd(containerId).withTimeout(TIMEOUT).exec();
        
        // TODO: Wait for stopping ok.
        // dockerClient.waitContainerCmd(container.getId()).exec();
    }

    @Override
    public void start(String containerId) {
        DockerClient dockerClient = getClient();
        dockerClient.startContainerCmd(containerId).exec();
    }

    @Override
    public void restart(String containerId) {
        DockerClient dockerClient = getClient();
        dockerClient.restartContainerCmd(containerId).withtTimeout(TIMEOUT).exec();
    }

    @Override
    public void remove(String containerId) {
        DockerClient dockerClient = getClient();
        dockerClient.removeContainerCmd(containerId).withForce(true).withRemoveVolumes(true).exec();
    }

    @Override
    public String logs(String containerId) {
        InputStream response = getLogs(containerId);
        return toString(response);
    }

    @Override
    public KieContainerDetails inspect(String containerId) {

        Timer timer = new Timer();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start inspecting container with id = " + containerId);
            timer.start();
        }
        
        final DockerClient dockerClient = getClient();
        final InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
        final InspectContainerResponse.NetworkSettings networkSettings = response.getNetworkSettings();
        final ContainerConfig config = response.getConfig();
        final String[] envVars = config.getEnv();
        final InspectContainerResponse.ContainerState state = response.getState();
        final String ipAddress = networkSettings.getIpAddress();
        final int containerPid = state.getPid();
        final String containerStartedAt = state.getStartedAt();
        final String containerFinishedAt = state.getFinishedAt();
        final Map<String, Map<String, String>> portMappings = networkSettings.getPortMapping();
        final String[] containerArgs = response.getArgs();
        
        final KieContainerDetails details = new KieContainerDetails();
        details.setIpAddress(ipAddress);
        details.setContainerPid(containerPid);
        details.setContainerStartedAt(containerStartedAt);
        details.setContainerFinishedAt(containerFinishedAt);
        details.setPortMappings(portMappings);
        details.setContainerArgs(containerArgs);
        details.setEnvVars(escapeEnvVars(envVars));

        if (LOGGER.isDebugEnabled()) {
            timer.end();
            LOGGER.debug("container with id = " + containerId + " inspected. Elapsed time = " + timer.getTotalTime());
        }
        
        return details;
    }
    
    private String[] escapeEnvVars(final String[] envVars) {
        if (envVars == null || envVars.length == 0) return  null;
        
        String[] result = new String[envVars.length];
        int x = 0;
        for(final String envVar : envVars) {
            result[x] = StringEscapeUtils.escapeJava(envVar);
            x++;
        }
        return result;
    }

    private DockerClient getClient() {
        final String url = "http://" + settingsService.getSettings().getPrivateHost() + ":" + settingsService.getSettings().getRestApiPort();
        return DockerClientBuilder.getInstance(url).build();
    }
    
    private InputStream getLogs(String containerId) {
        DockerClient dockerClient = getClient();
        // TODO: Error stream.
        InputStream response = dockerClient.logContainerCmd(containerId).withStdOut(true).withStdErr(true).exec();
        return response;
    }
    
    private String toString(InputStream stream) {
        try {
            return IOUtils.toString(stream, "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Error reading the input stream.", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }

        return null;
    }
    
}
