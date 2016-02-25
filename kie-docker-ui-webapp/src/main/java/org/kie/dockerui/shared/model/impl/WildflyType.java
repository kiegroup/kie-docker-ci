package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class WildflyType {

    public static final KieImageType INSTANCE =  new KieImageType("jboss/wildfly", "Wildfly", KieImageCategory.APPSERVER);
}
