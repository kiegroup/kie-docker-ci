package org.kie.dockerui.client;

import com.google.gwt.user.client.Window;

public class Log {
    
    public static void log(final String message) {
        Window.alert(message);
    }
}
