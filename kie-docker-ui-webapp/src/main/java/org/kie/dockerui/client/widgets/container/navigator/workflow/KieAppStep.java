package org.kie.dockerui.client.widgets.container.navigator.workflow;

import com.google.gwt.safehtml.shared.SafeUri;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.container.navigator.NavigationContext;
import org.kie.dockerui.client.widgets.container.navigator.NavigationWorkflowStep;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.model.impl.*;

import java.util.*;

public class KieAppStep extends AbstractStep {
    
    public static final String CONTEXT_KIE_APP = "kieApp";
    public static final KieAppStep INSTANCE = new KieAppStep();

    @Override
    public String getTitle(NavigationContext context) {
        return Constants.INSTANCE.allCategories();
    }

    @Override
    public Collection<NavigationItem> items(final NavigationContext context) {

        // Get all image types for KIE_APP and OTHERS categories.
        final List<KieImageType> categoryTypes = new ArrayList<>();
        categoryTypes.addAll(KieImageTypeManager.getTypes(KieImageCategory.KIEAPP));
        categoryTypes.addAll(KieImageTypeManager.getTypes(KieImageCategory.OTHERS));

        // Show all type for the current category defined by the current workflow step.
        final List<NavigationItem> result = new LinkedList<>();
        for (final KieImageType categoryType : categoryTypes) {
            final NavigationItem item = createNavigationItem(context, categoryType);
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
            final int i1 = getKieAppComparisonIndex(o1.getId());
            final int i2 = getKieAppComparisonIndex(o2.getId());
            return i1 - i2;
        }
    };

    private static int getKieAppComparisonIndex(final String kieAppTypeId) {
        if (kieAppTypeId == null) return -1;

        if (kieAppTypeId.equals(KieWorkbenchType.INSTANCE.getId())) {
            return 0;
        } else if (kieAppTypeId.equals(KieDroolsWorkbenchType.INSTANCE.getId())) {
            return 1;
        } else if (kieAppTypeId.equals(KieServerType.INSTANCE.getId())) {
            return 2;
        } else if (kieAppTypeId.equals(UfDashbuilderType.INSTANCE.getId())) {
            return 3;
        } else if (kieAppTypeId.equals(OthersType.INSTANCE.getId())) {
            return 4;
        }

        return -1;
    }
    
    @Override
    public boolean accepts(NavigationContext context, KieContainer container) {
        // All containers has a given KIE type.
        return true;
    }

    @Override
    public boolean accepts(NavigationContext context, KieImage image) {
        // All images has a given KIE type.
        return true;
    }

    private NavigationItem createNavigationItem(final NavigationContext context, final KieImageType type) {
        NavigationItem result = null;
        if (type!= null) {
            final String id = type.getId();
            final String title = type.getName();
            final SafeUri imageUri = ClientUtils.getImageUri(type);
            final int cCount = countContainers(context, type);
            result = createDefaultNavigationItem(id, title, null, imageUri, cCount);
        }
        
        return result;
    }
    
    private int countContainers(final NavigationContext context, final KieImageType type) {
        int result = 0;
        
        final List<KieContainer> containers = context.getContainers();
        if (containers != null && !containers.isEmpty()) {
            for (final KieContainer container : containers) {
                if (container.getType().equals(type)) {
                    result++;
                }
            }
        }
        
        return result;
    }

    

    @Override
    public NavigationWorkflowStep navigate(final String navigationItemId, final NavigationContext context) {
        context.getContext().put(CONTEXT_KIE_APP, navigationItemId);
        final KieImageType selectedType = KieImageTypeManager.getImageTypeById(navigationItemId);
        final boolean isOthersType = selectedType.getCategory().equals(KieImageCategory.OTHERS);
        return isOthersType ? KieLastStep.INSTANCE : KieAppVersionStep.INSTANCE;
    }

    @Override
    public void reset(final NavigationContext context) {
        context.getContext().remove(CONTEXT_KIE_APP);
    }
}
