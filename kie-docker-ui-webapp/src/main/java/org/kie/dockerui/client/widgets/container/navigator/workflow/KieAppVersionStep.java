package org.kie.dockerui.client.widgets.container.navigator.workflow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.container.navigator.NavigationContext;
import org.kie.dockerui.client.widgets.container.navigator.NavigationWorkflowStep;
import org.kie.dockerui.client.widgets.container.navigator.item.CompositeNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.DateNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.*;

public class KieAppVersionStep extends AbstractStep {
    
    public static final String CONTEXT_KIE_APP_VERSION = "kieAppVersion";
    public static final KieAppVersionStep INSTANCE = GWT.create(KieAppVersionStep.class);

    @Override
    public String getTitle(NavigationContext context) {
        final KieImageType type = getKieAppType(context);
        return new SafeHtmlBuilder().appendEscaped(type.getName()).toSafeHtml().asString();
    }

    @Override
    public int getItemsPerRow() {
        return 6;
    }

    @Override
    public Collection<NavigationItem> items(final NavigationContext context) {

        final KieImageType kieAppType = getKieAppType(context);
        final String kieAppId = kieAppType.getId();

        final List<NavigationItem> result = new ArrayList<>();
        final List<KieImage> allImages = context.getImages();

        if (allImages != null && !allImages.isEmpty()) {
            final Map<String, List<NavigationItem>> itemsPerVersion = new HashMap<>();
            final Set<String> tagsConsumed = new HashSet<>();
            for (final KieImage image : allImages) {
                final KieImageType type = image.getType();
                if (type.getId().equals(kieAppId)) {
                    final Set<String> tags = image.getTags();
                    for (final String tag : tags) {
                        if (!tagsConsumed.contains(tag)) {
                            final int cCount = countContainers(context, type, tag);
                            final String[] tagInfo = SharedUtils.parseTag(tag);
                            String version;
                            Date tagDate;
                            // Use image tag to obtain the timestamp informacion about it.
                            if (tagInfo != null && tagInfo.length == 3) {
                                version = tagInfo[1];
                                tagDate = ClientUtils.parseImageDateTag(tagInfo[2]);
                            // No timestamp info on image tag, use the image creation date.
                            } else {
                                version = tag;
                                tagDate = image.getCreated();
                            }
                            
                            String itemTitle;
                            String itemText = null;
                            int _month = 0;
                            int _day = 0;
                            if (tagDate != null) {
                                final String tagDayNumber = ClientUtils.formatImageDateTag(tagDate, "d");
                                final String tagMonthNumber = ClientUtils.formatImageDateTag(tagDate, "M");
                                _month = Integer.decode(tagMonthNumber);
                                _day = Integer.decode(tagDayNumber);
                                
                            } else {
                                itemText = Constants.INSTANCE.noDateTagInfo();
                            }

                            itemTitle = new SafeHtmlBuilder().appendEscaped(version)
                                    .toSafeHtml().asString();

                            List<NavigationItem> items = itemsPerVersion.get(version);
                            if (items == null) {
                                items = new ArrayList<>();
                            }

                            NavigationItem item = null;
                            if (itemText != null) {
                                item = createDefaultNavigationItem(tag, itemTitle, itemText, Images.INSTANCE.calendarEmptyIcon().getSafeUri(), cCount);
                            } else {
                                item = createDateNavigationItem(tag, itemTitle, _month, _day, cCount);
                            }
                            
                            if (item != null) {
                                GWT.log("Added navigation item for tag " + tag);
                                items.add(item);
                            }

                            itemsPerVersion.put(version, items);
                            tagsConsumed.add(tag);
                        }

                    }
                }
            }

            tagsConsumed.clear();

            if (!itemsPerVersion.isEmpty()) {
                for (final Map.Entry<String, List<NavigationItem>> entry : itemsPerVersion.entrySet()) {
                    final String version = entry.getKey();
                    final List<NavigationItem> items = entry.getValue();
                    Collections.sort(items, ITEM_COMPARATOR);
                    result.add(new CompositeNavigationItem() {
                        @Override
                        public List<NavigationItem> getItems() {
                            return items;
                        }

                        @Override
                        public String getId() {
                            return version;
                        }

                        @Override
                        public String getTitle() {
                            return new SafeHtmlBuilder().appendEscaped(kieAppType.getName())
                                    .appendEscaped(" - ").appendEscaped(version).toSafeHtml().asString();
                        }

                        @Override
                        public int getContainersCount() {
                            return -1;
                        }

                    });
                }
            }
        }

        Collections.sort(result, ITEM_COMPARATOR);
        return result;
    }
    
    private static final Comparator<NavigationItem> ITEM_COMPARATOR = new Comparator<NavigationItem>() {
        @Override
        public int compare(NavigationItem o1, NavigationItem o2) {
            final String id1 = o1.getId();
            final String id2 = o2.getId();
            return id2.compareTo(id1);
        }
    };

    @Override
    public boolean accepts(NavigationContext context, KieContainer container) {
        final String kieAppId = getKieAppId(context);
        return container.getType().getId().equals(kieAppId);
    }

    @Override
    public boolean accepts(NavigationContext context, KieImage image) {
        final String kieAppId = getKieAppId(context);
        return image.getType().getId().equals(kieAppId);
    }

    private int countContainers(final NavigationContext context, final KieImageType kieApp, String tag) {
        int result = 0;
        
        final List<KieContainer> containers = context.getContainers();
        if (containers != null && !containers.isEmpty()) {
            for (final KieContainer container : containers) {
                if (container.getType().equals(kieApp)
                        && container.getTag().equals(tag)) {
                    result++;
                }
            }
        }
        
        return result;
    }

    private String getKieAppId(NavigationContext context) {
        return context.getContext().get(KieAppStep.CONTEXT_KIE_APP);
    }

    private KieImageType getKieAppType(NavigationContext context) {
        final String kieAppId = getKieAppId(context);
        return KieImageTypeManager.getImageTypeById(kieAppId);
    }

    @Override
    public NavigationWorkflowStep navigate(final String navigationItemId, final NavigationContext context) {
        context.getContext().put(CONTEXT_KIE_APP_VERSION, navigationItemId);
        return KieAppServerStep.INSTANCE;
    }

    @Override
    public void reset(final NavigationContext context) {
        context.getContext().remove(CONTEXT_KIE_APP_VERSION);
    }
    
    public static String getTag(final NavigationContext context) {
        return context.getContext().get(CONTEXT_KIE_APP_VERSION);
    }
}
