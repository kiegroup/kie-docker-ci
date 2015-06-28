package org.kie.dockerui.shared;

import org.kie.dockerui.shared.model.KieContainerDetails;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

import java.util.LinkedList;
import java.util.List;

public class KieImageTypeManager {

    public static final String TYPE_KIE_WB_ID = "jboss-kie/kie-wb";
    public static final String TYPE_KIE_DROOLS_WB_ID = "jboss-kie/kie-drools-wb";
    public static final String TYPE_KIE_SERVER_ID = "jboss-kie/kie-server";
    public static final String TYPE_KIE_UF_DASHBUILDER_ID = "jboss-kie/uf-dashbuilder";
    public static final String TYPE_WILDFLY_ID = "jboss/wildfly";
    public static final String TYPE_EAP_ID = "redhat/eap";
    public static final String TYPE_TOMCAT_ID = "tomcat";
    public static final String TYPE_IN_MEMORY_DB_ID = "--InMemoryDB--";
    public static final String TYPE_H2_ID = "h2";
    public static final String TYPE_MYSQL_ID = "mysql";
    public static final String TYPE_POSTGRESQL_ID = "postgresql";
    public static final String TYPE_OTHER_ID = "other";

    private static final List<KieImageCategory> appServerAndDBMSCategories = new LinkedList<KieImageCategory>();
    static {
        appServerAndDBMSCategories.add(KieImageCategory.KIEAPP);
        appServerAndDBMSCategories.add(KieImageCategory.APPSERVER);
        appServerAndDBMSCategories.add(KieImageCategory.DBMS);
    }
    private static final List<KieImageCategory> appServerCategory = new LinkedList<KieImageCategory>();
    static {
        appServerCategory.add(KieImageCategory.KIEAPP);
        appServerCategory.add(KieImageCategory.APPSERVER);
    }
    public static final KieImageType KIE_WB = new KieImageType(TYPE_KIE_WB_ID, "KIE Workbench", KieImageCategory.KIEAPP, "kie-wb").setSiteContextPath("kie-wb").setSupportedCategories(appServerAndDBMSCategories);
    public static final KieImageType KIE_DROOLS_WB = new KieImageType(TYPE_KIE_DROOLS_WB_ID, "KIE Drools Workbench", KieImageCategory.KIEAPP, "kie-drools-wb").setSiteContextPath("kie-drools-wb").setSupportedCategories(appServerAndDBMSCategories);
    public static final KieImageType KIE_SERVER = new KieImageType(TYPE_KIE_SERVER_ID, "KIE Execution Server", KieImageCategory.KIEAPP, "kie-server/services/rest/server").setSiteContextPath("kie-server").setSupportedCategories(appServerCategory);
    public static final KieImageType KIE_UF_DASHBUILDER = new KieImageType(TYPE_KIE_UF_DASHBUILDER_ID, "UF Dashbuilder", KieImageCategory.KIEAPP, "dashbuilder").setSiteContextPath("uf-dashbuilder").setSupportedCategories(appServerAndDBMSCategories);
    public static final KieImageType KIE_WILDFLY = new KieImageType(TYPE_WILDFLY_ID, "Wildfly", KieImageCategory.APPSERVER);
    public static final KieImageType KIE_EAP = new KieImageType(TYPE_EAP_ID, "JBoss EAP", KieImageCategory.APPSERVER);
    public static final KieImageType KIE_TOMCAT = new KieImageType(TYPE_TOMCAT_ID, "Tomcat", KieImageCategory.APPSERVER);
    public static final KieImageType KIE_H2_IN_MEMORY = new KieImageType(TYPE_H2_ID, "H2", KieImageCategory.DBMS).setScope(KieImageType.Scope.RUNTIME);
    public static final KieImageType KIE_MYSQL = new KieImageType(TYPE_MYSQL_ID, "MySQL", KieImageCategory.DBMS).setScope(KieImageType.Scope.RUNTIME);
    public static final KieImageType KIE_POSTGRESQL = new KieImageType(TYPE_POSTGRESQL_ID, "PostgreSQL", KieImageCategory.DBMS).setScope(KieImageType.Scope.RUNTIME);
    public static final KieImageType KIE_OTHER = new KieImageType(TYPE_OTHER_ID, "Others", KieImageCategory.OTHERS);

    public static final KieImageType[] ALL_TYPES = new KieImageType[] {
            // KIEAPP Category.
            KIE_WB, KIE_DROOLS_WB, KIE_SERVER, KIE_UF_DASHBUILDER,
            // APPSERVER Category.
            KIE_WILDFLY, KIE_EAP, KIE_TOMCAT,
            // DBMS Category.
            KIE_H2_IN_MEMORY, KIE_MYSQL, KIE_POSTGRESQL,
            // OTHERS Category.
            KIE_OTHER
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

    public static KieImageType getAppServerType(final String repository) {
        final List<KieImageType> kieAppTypes = KieImageTypeManager.getTypes(KieImageCategory.APPSERVER);
        for (final KieImageType kieAppType: kieAppTypes) {
            if (kieAppType.getId().equals(repository)) return kieAppType;
        }

        // Check the app server used in kie apps containers.
        if (isKIEAppType(repository)) {
            if (repository.endsWith("wildfly8")) return KieImageTypeManager.KIE_WILDFLY;
            if (repository.endsWith("tomcat7")) return KieImageTypeManager.KIE_TOMCAT;
            if (repository.endsWith("eap64")) return KieImageTypeManager.KIE_EAP;
        }
        return null;
    }

    public static KieImageType getDBMSType(final String id, final String repository, final String[] tags, final KieContainerDetails details) {
        if (KieImageTypeManager.KIE_MYSQL.getId().equals(repository)) return KieImageTypeManager.KIE_MYSQL;
        if (KieImageTypeManager.KIE_POSTGRESQL.getId().equals(repository)) return KieImageTypeManager.KIE_POSTGRESQL;

        // Check container env vars to extract db usage information.
        if (isKIEAppType(repository)) {
            for (final String tag : tags) {
                if (details != null) {
                    String[] envs = details.getEnvVars();
                    if (envs != null && envs.length > 0) {
                        for (String env : envs) {
                            if (env.startsWith("KIE_CONNECTION_DRIVER")) {
                                String[] _s = extractEnvVariable(env);
                                if ("mysql".equals(_s[1])) return KieImageTypeManager.KIE_MYSQL;
                                if ("postgres".equals(_s[1])) return KieImageTypeManager.KIE_POSTGRESQL;
                            }
                        }
                    }
                }
                // If any env variable found, it should use H2, as default.
                return KieImageTypeManager.KIE_H2_IN_MEMORY;
            }
        }

        return null;
    }

    public static String[] extractEnvVariable(String env) {
        if (env == null) return null;
        if (env.length() == 1) return new String[] {env, ""};
        return env.split("=");
    }
    
}
