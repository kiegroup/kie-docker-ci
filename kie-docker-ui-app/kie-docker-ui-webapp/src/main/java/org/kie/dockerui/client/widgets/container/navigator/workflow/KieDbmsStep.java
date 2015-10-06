package org.kie.dockerui.client.widgets.container.navigator.workflow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.container.navigator.NavigationContext;
import org.kie.dockerui.client.widgets.container.navigator.NavigationWorkflowStep;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.model.impl.H2Type;
import org.kie.dockerui.shared.model.impl.MySQLType;
import org.kie.dockerui.shared.model.impl.PostgreSQLType;

import java.util.*;

public class KieDbmsStep extends AbstractStep {
    
    public static final String CONTEXT_DBMS = "dbms";
    public static final KieDbmsStep INSTANCE = GWT.create(KieDbmsStep.class);

    @Override
    public String getTitle(NavigationContext context) {
        final String appServerId = context.getContext().get(KieAppServerStep.CONTEXT_APP_SERVER);
        final KieImageType type = KieImageTypeManager.getImageTypeById(appServerId);
        return new SafeHtmlBuilder().appendEscaped(type.getName()).toSafeHtml().asString();
    }
    
    @Override
    public Collection<NavigationItem> items(final NavigationContext context) {
        final List<NavigationItem> result = new LinkedList<>();

        final String kieAppId = context.getContext().get(KieAppStep.CONTEXT_KIE_APP);
        final String tag = context.getContext().get(KieAppVersionStep.CONTEXT_KIE_APP_VERSION);
        final String appServerId = context.getContext().get(KieAppServerStep.CONTEXT_APP_SERVER);

        final List<KieImageType> dbmsTypes = KieImageTypeManager.getTypes(KieImageCategory.DBMS);
        final List<KieContainer> containers = context.getContainers();
        for (final KieImageType dbmsType : dbmsTypes) {
            int cCount = 0;

            if (containers != null && !containers.isEmpty()) {
                for (final KieContainer container  : containers) {
                    final List<KieImageType> subTypes = container.getSubTypes();
                    if (container.getType().getId().equalsIgnoreCase(kieAppId)
                            && container.getTag().equalsIgnoreCase(tag)
                            && subTypes != null && subTypes.size() == 2
                            && subTypes.get(0).getId().equals(appServerId)
                            && subTypes.get(1).getId().equals(dbmsType.getId())) {
                        cCount++;
                    }
                }
            }

            final SafeUri imageUri = ClientUtils.getImageUri(dbmsType);
            final NavigationItem item = createDefaultNavigationItem(dbmsType.getId(), dbmsType.getName(), null, imageUri, cCount);
            if (item != null) {
                result.add(item);
            }
        }

        Collections.sort(result, ITEM_COMPARATOR);
        return result;
    }

    private static final Comparator<NavigationItem> ITEM_COMPARATOR = new Comparator<NavigationItem>() {
        @Override
        public int compare(NavigationItem o1, NavigationItem o2) {
            final int i1 = getDmbsComparisonIndex(o1.getId());
            final int i2 = getDmbsComparisonIndex(o2.getId());
            return i1 - i2;
        }
    };

    private static int getDmbsComparisonIndex(final String appServerTypeId) {
        if (appServerTypeId == null) return -1;

        if (appServerTypeId.equals(H2Type.INSTANCE.getId())) {
            return 0;
        } else if (appServerTypeId.equals(MySQLType.INSTANCE.getId())) {
            return 1;
        } else if (appServerTypeId.equals(PostgreSQLType.INSTANCE.getId())) {
            return 2;
        }

        return -1;
    }

    @Override
    public boolean accepts(NavigationContext context, KieContainer container) {
        final String appServerId = context.getContext().get(KieAppServerStep.CONTEXT_APP_SERVER);
        final List<KieImageType> subTypes = container.getSubTypes();
        return subTypes != null && !subTypes.isEmpty()
                && subTypes.get(0).getId().equalsIgnoreCase(appServerId);
    }

    @Override
    public boolean accepts(NavigationContext context, KieImage image) {
        final String appServerId = context.getContext().get(KieAppServerStep.CONTEXT_APP_SERVER);
        final List<KieImageType> subTypes = image.getSubTypes();
        return subTypes != null && !subTypes.isEmpty()
                && subTypes.get(0).getId().equalsIgnoreCase(appServerId);
    }

    @Override
    public NavigationWorkflowStep navigate(final String navigationItemId, final NavigationContext context) {
        context.getContext().put(CONTEXT_DBMS, navigationItemId);
        return KieLastStep.INSTANCE;
    }

    @Override
    public void reset(final NavigationContext context) {
        context.getContext().remove(CONTEXT_DBMS);
    }
}
