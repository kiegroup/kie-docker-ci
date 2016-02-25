package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class UfDashbuilderType {

    public static final KieImageType INSTANCE =  new KieImageType("jboss-kie/uf-dashbuilder", "UF Dashbuilder", KieImageCategory.KIEAPP, "dashbuilder")
            .setSiteContextPath("uf-dashbuilder")
            .setArtifactId("dashbuilder")
            .setSupportedCategories(new KieImageCategory[] {KieImageCategory.KIEAPP, KieImageCategory.APPSERVER, KieImageCategory.DBMS}); 
}
