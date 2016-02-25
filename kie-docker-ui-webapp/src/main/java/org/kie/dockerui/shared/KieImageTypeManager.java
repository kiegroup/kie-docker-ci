package org.kie.dockerui.shared;

import org.kie.dockerui.shared.model.KieContainerDetails;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.model.impl.*;

import java.util.LinkedList;
import java.util.List;

public class KieImageTypeManager {

    static final KieImageType[] KIE_APP_TYPES = new KieImageType[] {
            KieWorkbenchType.INSTANCE, KieDroolsWorkbenchType.INSTANCE, KieServerType.INSTANCE, UfDashbuilderType.INSTANCE
    };

    static final KieImageType[] APP_SERVER_TYPES = new KieImageType[] {
            WildflyType.INSTANCE, EAPType.INSTANCE, TomcatType.INSTANCE
    };

    static final KieImageType[] DBMS_TYPES = new KieImageType[] {
            H2Type.INSTANCE, MySQLType.INSTANCE, PostgreSQLType.INSTANCE
    };
    
    static final KieImageType[] ALL_TYPES = new KieImageType[] {
            KieWorkbenchType.INSTANCE, KieDroolsWorkbenchType.INSTANCE, KieServerType.INSTANCE, UfDashbuilderType.INSTANCE,
            WildflyType.INSTANCE, EAPType.INSTANCE, TomcatType.INSTANCE,
            H2Type.INSTANCE, MySQLType.INSTANCE, PostgreSQLType.INSTANCE,
            OthersType.INSTANCE
    };

    public static List<KieImageType> getTypes(final KieImageCategory category) {
        if (category == null) return null;

        final List<KieImageType> result = new LinkedList<KieImageType>();
        for (final KieImageType type : ALL_TYPES) {
            if (category.equals(type.getCategory())) result.add(type);
        }
        return result;
    }

    public static KieImageType getKIEAppType(final String repository) {
        final List<KieImageType> kieAppTypes = KieImageTypeManager.getTypes(KieImageCategory.KIEAPP);
        for (final KieImageType kieAppType: kieAppTypes) {
            if (repository.startsWith(kieAppType.getId())) return kieAppType;
        }
        return null;
    }

    public static boolean isKIEAppType(final String repository) {
        return getKIEAppType(repository) != null;
    }

    public static KieImageType getImageTypeById(final String id) {
        for (final KieImageType type : ALL_TYPES) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
    
    public static KieImageType getAppServerType(final String repository) {
        final List<KieImageType> kieAppTypes = KieImageTypeManager.getTypes(KieImageCategory.APPSERVER);
        for (final KieImageType kieAppType: kieAppTypes) {
            if (kieAppType.getId().equals(repository)) return kieAppType;
        }

        // Check the app server used in kie apps containers.
        if (isKIEAppType(repository)) {
            if (repository.matches(".+wildfly\\d*")) return WildflyType.INSTANCE;
            if (repository.matches(".+tomcat\\d*")) return TomcatType.INSTANCE;
            if (repository.matches(".+eap\\d*")) return EAPType.INSTANCE;
        }
        return null;
    }

    public static KieImageType getDBMSType(final String repository, final KieContainerDetails details) {
        if (MySQLType.INSTANCE.getId().equals(repository)) return MySQLType.INSTANCE;
        if (PostgreSQLType.INSTANCE.getId().equals(repository)) return PostgreSQLType.INSTANCE;

        // Check container env vars to extract db usage information.
        if (details != null && isKIEAppType(repository)) {
            String[] envs = details.getEnvVars();
            if (envs != null && envs.length > 0) {
                for (String env : envs) {
                    if (env.startsWith("KIE_CONNECTION_DRIVER")) {
                        String[] _s = extractEnvVariable(env);
                        if ("mysql".equals(_s[1])) return MySQLType.INSTANCE;
                        if ("postgres".equals(_s[1])) return PostgreSQLType.INSTANCE;
                    }
                }
            }
            // If any env variable found, it should use H2, as default.
            return H2Type.INSTANCE;
        }

        return null;
    }
    
    public static String getArtifactQualifier(final KieImageType type, final KieImageType subType) {
        if (subType != null && type.getCategory().equals(KieImageCategory.KIEAPP)) {
            if (KieServerType.INSTANCE.equals(type)) {
                if (WildflyType.INSTANCE.equals(subType)) {
                    return "ee7";
                } else if (EAPType.INSTANCE.equals(subType)) {
                    return "ee6";
                } else if (TomcatType.INSTANCE.equals(subType)) {
                    return "webc";
                }
            } else {
                return getDefaultArtifactQualifier(subType);
            }
        }
        return "";
    }
    
    private static String getDefaultArtifactQualifier(final KieImageType subType) {
        if (WildflyType.INSTANCE.equals(subType)) {
            return "wildfly8";
        } else if (EAPType.INSTANCE.equals(subType)) {
            return "eap6_4";
        } else if (TomcatType.INSTANCE.equals(subType)) {
            return "tomcat7";
        }
        return "";
    }

    public static String[] extractEnvVariable(String env) {
        if (env == null) return null;
        if (env.length() == 1) return new String[] {env, ""};
        return env.split("=");
    }
    
}
