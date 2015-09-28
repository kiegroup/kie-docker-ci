package org.kie.dockerui.client.widgets.container.navigator.workflow;

import com.google.gwt.safehtml.shared.SafeUri;
import org.kie.dockerui.client.widgets.container.navigator.NavigationWorkflowStep;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;

public abstract class AbstractStep implements NavigationWorkflowStep {

    private  static final int CONTAINERS_PER_ROW = 5;

    @Override
    public int getItemsPerRow() {
        return CONTAINERS_PER_ROW;
    }

    protected NavigationItem createNavigationItem(final String id, final String title, final String text,
                                                  final SafeUri imageUi, final int containersCount ) {
        return new NavigationItem() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getText() {
                return text;
            }

            @Override
            public SafeUri getImageUri() {
                return imageUi;
            }

            @Override
            public int getContainersCount() {
                return containersCount;
            }
        };
    }
    
}
