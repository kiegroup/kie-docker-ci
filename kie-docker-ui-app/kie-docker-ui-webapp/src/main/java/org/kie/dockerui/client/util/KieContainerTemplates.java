package org.kie.dockerui.client.util;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface KieContainerTemplates extends SafeHtmlTemplates {

    @Template("docker pull {0}:{1}/{2}")
    SafeHtml pullAddress(final String host, final int port, final String image);

    @Template("{0}://{1}:{2}/{3}")
    SafeHtml webAddress(final String protocol, final String host, final int port, final String contextPath);

    @Template("ssh -t {1}@{0} \"nsenter -t {2} -m -u -i -n -p -w\"")
    SafeHtml sshNsenterAddress(final String host, final String user, final String containerPid);

    @Template("ssh -t {1}@{0} \"sudo nsenter -t {2} -m -u -i -n -p -w\"")
    SafeHtml sshSudoNsenterAddress(final String host, final String user, final String containerPid);
    
}
