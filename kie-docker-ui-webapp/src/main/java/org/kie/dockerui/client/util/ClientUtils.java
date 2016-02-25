package org.kie.dockerui.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import org.kie.dockerui.backend.servlet.KieArtifactsDownloadServlet;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.*;
import org.kie.dockerui.shared.model.impl.*;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.*;

public class ClientUtils {

    public static final KieContainerTemplates TEMPLATES = GWT.create(KieContainerTemplates.class);
    private static final DateTimeFormat IMAGE_TAG_DATE_FORMAT = DateTimeFormat.getFormat("yyyyMMdd-HHmmss");
    private static final String DOWNLOAD_SERVLET_MAPPING = "download";
    private static final long MILLISECONDS_IN_SECOND = 1000l;
    private static final long SECONDS_IN_MINUTE = 60l;
    private static final long MINUTES_IN_HOUR = 60l;
    private static final long HOURS_IN_DAY = 24l;
    private static final long MILLISECONDS_IN_DAY = MILLISECONDS_IN_SECOND *
            SECONDS_IN_MINUTE *
            MINUTES_IN_HOUR *
            HOURS_IN_DAY;

    public static String getPullAddress(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) {
        final String host = settings.getPublicHost();
        final int registryPort = settings.getRegistryPort();
        final String image = container.getImage();

        return TEMPLATES.pullAddress(host, registryPort, image).asString();
    }

    public static String getWebAddress(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) throws IllegalStateException {
        final String protocol = settings.getProtocol();
        final String host = settings.getPublicHost();
        final int httpPublicPort = SharedUtils.getPublicPort(8080, container);
        if (httpPublicPort < 0) throw new IllegalStateException("No ports available. Is the container running?");
        final String contextPath = container.getType().getContextPath();

        return TEMPLATES.webAddress(protocol, host, httpPublicPort, contextPath).asString();
    }

    public static String getSiteAddress(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) throws IllegalStateException {
        final String protocol = settings.getProtocol();
        final String host = settings.getPublicHost();
        final int httpPublicPort = SharedUtils.getPublicPort(8080, container);
        if (httpPublicPort < 0) throw new IllegalStateException("No ports available. Is the container running?");
        final String contextPath = "site/dependencies.html";

        return TEMPLATES.webAddress(protocol, host, httpPublicPort, contextPath).asString();
    }

    public static String getDownloadURL(final Settings settings, final KieImage image) {
        final String tag = image.getTags().iterator().next();
        final String downloadURL = ClientUtils.getDownloadURL(settings, image.getType(),
                    image.getRepository(),
                    tag);
        return downloadURL;
    }

    public static String getDownloadURL(final Settings settings, final KieContainer container) {
        final String downloadURL = ClientUtils.getDownloadURL(settings, container.getType(), 
                    container.getRepository(),
                    container.getTag());
        return downloadURL;
    }
    
    private static String getDownloadURL(final Settings settings, final KieImageType kieImageType, final String repository, final String tag) {
        final String artifactsPath = settings.getArtifactsPath();
        final String artifactQualifier = repository.substring( repository.lastIndexOf("-") + 1 , repository.length() );
        final String absolutePath = artifactsPath + "/" + tag + "/" + kieImageType.getArtifactId() + "-" + tag + "-" + artifactQualifier + ".war";
        return _getDownloadURL(absolutePath);
    }

    public static String getDownloadURL(final KieArtifact artifact) {
        return _getDownloadURL(artifact.getAbsoluteFilePath());
    }

    private static String _getDownloadURL(final String absolutePath) {
        final String moduleURL = GWT.getModuleBaseURL();
        final String path = URL.encodeQueryString(absolutePath);

        final StringBuilder contextPath = new StringBuilder(moduleURL)
                .append(DOWNLOAD_SERVLET_MAPPING)
                .append("?").append(KieArtifactsDownloadServlet.FILE_PATH_PARAM)
                .append("=").append(path);
        return contextPath.toString();
    }
    
    public static String getSSHCommand(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) {
        final String host = settings.getPublicHost();
        final String user = settings.getUser();

        return TEMPLATES.sshSudoNsenterAddress(host, user, Integer.toString(details.getContainerPid())).asString();
    }
    
    
    public static SafeUri getImageUri(final KieImageType containerType) {
        if (containerType == null) return null;

        if (KieWorkbenchType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.kieIde().getSafeUri();
        } else if (KieDroolsWorkbenchType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.drools().getSafeUri();
        } else if (KieServerType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.kie().getSafeUri();
        } else if (UfDashbuilderType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.dashbuilderLogo().getSafeUri();
        } else if (WildflyType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.wildfly().getSafeUri();
        }else if (EAPType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.jbossEAP().getSafeUri();
        }else if (TomcatType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.tomcat().getSafeUri();
        }else if (H2Type.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.h2().getSafeUri();
        }else if (MySQLType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.mysql().getSafeUri();
        } else if (PostgreSQLType.INSTANCE.equals(containerType)) {
            return Images.INSTANCE.postgresql().getSafeUri();
        } 

        return Images.INSTANCE.dockerIcon().getSafeUri();
    }
    
    public static List<Map.Entry<String, String>> toMapEntries(final List<KieContainerPort> ports) {
        if (ports == null) return  null;

        final List<Map.Entry<String, String>> result = new LinkedList<Map.Entry<String, String>>();
        for (final KieContainerPort port : ports) {
            final Map.Entry<String, String> entry = new Map.Entry<String, String>() {

                @Override
                public String getKey() {
                    return Integer.toString(port.getPrivatePort());
                }

                @Override
                public String getValue() {
                    return Integer.toString(port.getPublicPort());
                }

                @Override
                public String setValue(String value) {
                    return null;
                }
            };
            result.add(entry);
        }
        return result;
    }

    public static List<Map.Entry<String, String>> toMapEntries(final String[] s, final String separator) {
        if (s == null) return  null;

        final List<Map.Entry<String, String>> result = new LinkedList<Map.Entry<String, String>>();
        for (final String _s : s) {
            final String[] split = _s.split(separator);
            final String[] values = split.length == 2 ? split : new String[] {_s , ""};  
            final Map.Entry<String, String> entry = new Map.Entry<String, String>() {

                @Override
                public String getKey() {
                    return values[0];
                }

                @Override
                public String getValue() {
                    return values[1];
                }

                @Override
                public String setValue(String value) {
                    return null;
                }
            };
            result.add(entry);
        }
        return result;
    }

    public static Map<String, String> toMap(final String[] s, final String separator) {
        if (s == null) return  null;

        final Map<String, String> result = new LinkedHashMap<String, String>();
        for (String _s : s) {
            // Split only for first ocurrence character. 
            final String[] split = _s.split(separator, 2);
            final String[] values = split.length == 2 ? split : new String[] {_s , ""};
            result.put(values[0], values[1]);
        }
        return result;
    }

    public static String getValue(final Map<String, String> map, final String key) {
        if (map == null) return null;
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String _key = entry.getKey();
            if (key.equals(_key)) return entry.getValue();
        }
        return null;
    }

    public static Date goBack(final Date date, final int days) {
        return new Date(date.getTime () - ( (days - 1) * MILLISECONDS_IN_DAY));
    }
    
    public static ImageResource getStatusImage(final KieAppStatus status) {
        ImageResource imageResource = Images.INSTANCE.circleGreyCloseIcon();
        if (status != null) {
            switch (status) {
                case OK:
                    imageResource = Images.INSTANCE.circleGreenIcon();
                    break;
                case FAILED:
                    imageResource = Images.INSTANCE.circleRedIcon();
                    break;
                case NOT_EVALUATED:
                    imageResource = Images.INSTANCE.circleGreyIcon();
                    break;
            }
        }
        return imageResource;
    }

    public static String getStatusText(final KieAppStatus status) {
        String iconTooltip = Constants.INSTANCE.statusNotApplicable();
        if (status != null) {
            switch (status) {
                case OK:
                    iconTooltip = Constants.INSTANCE.statusRunnable();
                    break;
                case FAILED:
                    iconTooltip = Constants.INSTANCE.statusNotRunnable();
                    break;
                case NOT_EVALUATED:
                    iconTooltip = Constants.INSTANCE.statusNotEvaluated();
                    break;
            }
        }
        return iconTooltip;
    }
    
    public static String getDbmsImageName(final KieImageType dbmsType, final Settings settings) {
        if (dbmsType != null) {
            if (dbmsType.equals(MySQLType.INSTANCE)) {
                return settings.getMysqlImage();
            } else if (dbmsType.equals(PostgreSQLType.INSTANCE)) {
                return settings.getPostgresImage();
            }
        }
        return null;
    }
    
    public static Date parseImageDateTag(final String dateTagged) {
        if (dateTagged == null || dateTagged.trim().length() == 0) return null;
        return IMAGE_TAG_DATE_FORMAT.parse(dateTagged);
    }
    
    public static String formatImageDateTag(final Date date) {
        if (date == null) return null;
        return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM).format(date);
    }

    public static String formatImageDateTag(final Date date, final String pattern) {
        if (date == null) return null;
        return DateTimeFormat.getFormat(pattern).format(date);
    }
}
