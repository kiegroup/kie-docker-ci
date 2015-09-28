package org.kie.dockerui.shared.model.impl;

import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class EAPType {

    public static final KieImageType INSTANCE =  new KieImageType("redhat/eap", "JBoss EAP", KieImageCategory.APPSERVER);
}
