package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class PostgreSQLType {

    public static final KieImageType INSTANCE = new KieImageType("postgresql", "PostgreSQL", KieImageCategory.DBMS)
            .setScope(KieImageType.Scope.RUNTIME);
}
