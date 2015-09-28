package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class KieServerType {

    public static final KieImageType INSTANCE =  new KieImageType("jboss-kie/kie-server", "KIE Execution Server", KieImageCategory.KIEAPP, "kie-server/services/rest/server")
            .setSiteContextPath("kie-server")
            .setArtifactId("kie-server")
            .setSupportedCategories(new KieImageCategory[] {KieImageCategory.KIEAPP, KieImageCategory.APPSERVER, KieImageCategory.DBMS}); 
}
