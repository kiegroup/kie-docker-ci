package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class H2Type {

    public static final KieImageType INSTANCE =  new KieImageType("h2", "H2", KieImageCategory.DBMS)
            .setScope(KieImageType.Scope.RUNTIME);
}
