package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class BusinessCentralType {

    public static final KieImageType INSTANCE =  new KieImageType("jboss-kie/business-central", "Business Central", KieImageCategory.KIEAPP, "business-central")
            .setSiteContextPath("business-central")
            .setArtifactId("business-central")
            .setSupportedCategories(new KieImageCategory[] {KieImageCategory.KIEAPP, KieImageCategory.APPSERVER, KieImageCategory.DBMS}); 
}
