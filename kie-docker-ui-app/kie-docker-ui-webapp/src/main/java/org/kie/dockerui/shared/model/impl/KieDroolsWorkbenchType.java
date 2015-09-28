package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class KieDroolsWorkbenchType {

    public static final KieImageType INSTANCE =  new KieImageType("jboss-kie/kie-drools-wb", "KIE Drools Workbench", KieImageCategory.KIEAPP, "kie-drools-wb")
            .setSiteContextPath("kie-drools-wb")
            .setArtifactId("kie-drools-wb-distribution-wars")
            .setSupportedCategories(new KieImageCategory[] {KieImageCategory.KIEAPP, KieImageCategory.APPSERVER, KieImageCategory.DBMS}); 
}
