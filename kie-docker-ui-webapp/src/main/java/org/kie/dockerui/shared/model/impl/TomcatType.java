package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class TomcatType {

    public static final KieImageType INSTANCE =  new KieImageType("tomcat", "Tomcat", KieImageCategory.APPSERVER);
}
