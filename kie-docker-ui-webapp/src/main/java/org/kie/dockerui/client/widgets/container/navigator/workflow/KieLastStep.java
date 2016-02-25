package org.kie.dockerui.client.widgets.container.navigator.workflow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.kie.dockerui.client.widgets.container.navigator.NavigationContext;
import org.kie.dockerui.client.widgets.container.navigator.NavigationWorkflowStep;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;

import java.util.Collection;
import java.util.List;

public class KieLastStep extends AbstractStep {
    
    public static final KieLastStep INSTANCE = GWT.create(KieLastStep.class);

    public static String getDbms(NavigationContext context) {
        return context.getContext().get(KieDbmsStep.CONTEXT_DBMS);
    }
    
    @Override
    public String getTitle(NavigationContext context) {
        final String kieAppId = context.getContext().get(KieAppStep.CONTEXT_KIE_APP);
        final String dbmsId = getDbms(context);
        final KieImageType type = dbmsId != null ? KieImageTypeManager.getImageTypeById(dbmsId) : KieImageTypeManager.getImageTypeById(kieAppId); 
        return new SafeHtmlBuilder().appendEscaped(type.getName()).toSafeHtml().asString();
    }

    @Override
    public Collection<NavigationItem> items(final NavigationContext context) {
        // This is the last step. No more items to navigate.
        return null;
    }

    @Override
    public boolean accepts(NavigationContext context, KieContainer container) {
        final String dbmsId = getDbms(context);
        return acceptsDbmsId(container, dbmsId);
    }

    @Override
    public boolean accepts(NavigationContext context, KieImage image) {
        // Images do not care about the dbms to use.
        return true;
    }

    private boolean acceptsDbmsId(final KieContainer container, final String dbmsId) {
        if (container != null) {
            if (dbmsId == null) return true;
            final List<KieImageType> subTypes = container.getSubTypes();
            return subTypes != null && subTypes.size() == 2 && subTypes.get(1).getId().equalsIgnoreCase(dbmsId);
        }
        return false;
    }

    @Override
    public NavigationWorkflowStep navigate(final String navigationItemId, final NavigationContext context) {
        // This is the last step. No more items to navigate.
        return null;
    }
    
    @Override
    public void reset(final NavigationContext context) {
        // Last step do not navigates, do not modified the navigation context. Do nothing here.
    }
}
