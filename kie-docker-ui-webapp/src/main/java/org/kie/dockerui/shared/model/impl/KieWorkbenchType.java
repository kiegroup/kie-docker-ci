package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class KieWorkbenchType {

    public static final KieImageType INSTANCE =  new KieImageType("jboss-kie/kie-wb", "KIE Workbench", KieImageCategory.KIEAPP, "kie-wb")
            .setSiteContextPath("kie-wb")
            .setArtifactId("kie-wb-distribution-wars")
            .setSupportedCategories(new KieImageCategory[] {KieImageCategory.KIEAPP, KieImageCategory.APPSERVER, KieImageCategory.DBMS}); 
}
