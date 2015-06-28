package org.kie.dockerui.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum KieImageCategory implements IsSerializable {
    KIEAPP,
    APPSERVER,
    DBMS,
    OTHERS;
}