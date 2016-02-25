package org.kie.dockerui.client.widgets.container.navigator.workflow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.container.navigator.NavigationContext;
import org.kie.dockerui.client.widgets.container.navigator.NavigationWorkflowStep;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.model.impl.EAPType;
import org.kie.dockerui.shared.model.impl.TomcatType;
import org.kie.dockerui.shared.model.impl.WildflyType;

import java.util.*;

public class KieAppServerStep extends AbstractStep {
    
    public static final String CONTEXT_APP_SERVER = "appServer";
    public static final KieAppServerStep INSTANCE = GWT.create(KieAppServerStep.class);

    @Override
    public String getTitle(NavigationContext context) {
        final String tag = context.getContext().get(KieAppVersionStep.CONTEXT_KIE_APP_VERSION);
        return new SafeHtmlBuilder().appendEscaped(tag).toSafeHtml().asString();
    }
    
    @Override
    public Collection<NavigationItem> items(final NavigationContext context) {

        final String kieAppId = context.getContext().get(KieAppStep.CONTEXT_KIE_APP);
        final String tag = KieAppVersionStep.getTag(context);

        final List<NavigationItem> result = new LinkedList<>();
        final Set<KieImageType> _availableAppServerImages = new HashSet<>();
        final List<KieImage> images = context.getImages();
        if (images != null && !images.isEmpty()) {
            for (final KieImage image : images) {
                final List<KieImageType> subTypes = image.getSubTypes();
                if (subTypes != null && !subTypes.isEmpty()) {
                    final KieImageType appServer = subTypes.get(0);
                    _availableAppServerImages.add(appServer);
                }
            }
        }
        
        if (!_availableAppServerImages.isEmpty()) {
            final List<KieContainer> containers = context.getContainers();
            for (final KieImageType appServer : _availableAppServerImages) {
                int cCount = 0;

                if (containers != null && !containers.isEmpty()) {
                    for (final KieContainer container  : containers) {
                        final List<KieImageType> subTypes = container.getSubTypes();
                        if (container.getType().getId().equalsIgnoreCase(kieAppId)
                                && container.getTag().equalsIgnoreCase(tag)
                                && subTypes != null && !subTypes.isEmpty()
                                && subTypes.get(0).equals(appServer)) {
                            cCount++;
                        }
                    }
                }

                final SafeUri imageUri = ClientUtils.getImageUri(appServer);
                final NavigationItem item = createDefaultNavigationItem(appServer.getId(), appServer.getName(), null, imageUri, cCount);
                if (item != null) {
                    result.add(item);
                }
            }
            
        }

        Collections.sort(result, ITEM_COMPARATOR);        
        return result;
    }

    private static final Comparator<NavigationItem> ITEM_COMPARATOR = new Comparator<NavigationItem>() {
        @Override
        public int compare(NavigationItem o1, NavigationItem o2) {
            final int i1 = getAppServerComparisonIndex(o1.getId());
            final int i2 = getAppServerComparisonIndex(o2.getId());
            return i1 - i2;
        }
    };

    private static int getAppServerComparisonIndex(final String appServerTypeId) {
        if (appServerTypeId == null) return -1;

        if (appServerTypeId.equals(WildflyType.INSTANCE.getId())) {
            return 0;
        } else if (appServerTypeId.equals(EAPType.INSTANCE.getId())) {
            return 1;
        } else if (appServerTypeId.equals(TomcatType.INSTANCE.getId())) {
            return 2;
        }

        return -1;
    }

    @Override
    public boolean accepts(NavigationContext context, KieContainer container) {
        final String tag = KieAppVersionStep.getTag(context);
        return container.getTag().equalsIgnoreCase(tag);
    }

    @Override
    public boolean accepts(NavigationContext context, KieImage image) {
        final String tag = KieAppVersionStep.getTag(context);
        return image.getTags().contains(tag);
    }

    @Override
    public NavigationWorkflowStep navigate(final String navigationItemId, final NavigationContext context) {
        context.getContext().put(CONTEXT_APP_SERVER, navigationItemId);
        return KieDbmsStep.INSTANCE;
    }

    @Override
    public void reset(final NavigationContext context) {
        context.getContext().remove(CONTEXT_APP_SERVER);
    }
}
