package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class MySQLType {

    public static final KieImageType INSTANCE =  new KieImageType("mysql", "MySQL", KieImageCategory.DBMS)
            .setScope(KieImageType.Scope.RUNTIME);
}
