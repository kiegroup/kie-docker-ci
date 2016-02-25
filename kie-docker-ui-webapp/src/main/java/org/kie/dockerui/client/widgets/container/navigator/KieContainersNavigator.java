/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.dockerui.client.widgets.container.navigator;

import com.github.gwtbootstrap.client.ui.Accordion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.KieClientManager;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.widgets.container.navigator.item.CompositeNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.view.NavigationItemViewBuilder;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEvent;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEventHandler;
import org.kie.dockerui.client.widgets.container.navigator.workflow.KieAppStep;
import org.kie.dockerui.client.widgets.container.navigator.workflow.KieAppVersionStep;
import org.kie.dockerui.client.widgets.container.navigator.workflow.KieLastStep;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;

import java.util.*;

public class KieContainersNavigator extends Composite {

    interface KieContainersNavigatorBinder extends UiBinder<Widget, KieContainersNavigator> {}
    private static KieContainersNavigatorBinder uiBinder = GWT.create(KieContainersNavigatorBinder.class);
    
    interface KieContainersNavigatorStyle extends CssResource {
        String mainPanel();
        String typesPanel();
        String emptyPanel();
        String loadingPanel();
    }
    
    @UiField
    KieContainersNavigatorStyle style;
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    FlowPanel newContainerPanel;
    
    @UiField
    com.github.gwtbootstrap.client.ui.Button newContainerButton;
    
    @UiField
    FlowPanel typesPanel;
    
    @UiField
    FlowPanel emptyPanel;

    @UiField
    NavigationBreadCrumbView breadcrumb;
    
    @UiField
    FlowPanel loadingPanel;
    
    private final List<KieContainer> containers = new LinkedList<KieContainer>();
    private final List<KieImage> images = new LinkedList<KieImage>();
    private final Map<String, String> navigationContext = new LinkedHashMap<>(5);
    private final NavigationContext context = new NavigationContext() {
        @Override
        public Map<String, String> getContext() {
            return navigationContext;
        }

        @Override
        public List<KieContainer> getContainers() {
            return containers;
        }

        @Override
        public List<KieImage> getImages() {
            return images;
        }
    };
    
    private final Stack<NavigationWorkflowStep> steps = new Stack<>();


    @UiConstructor
    public KieContainersNavigator() {
        initWidget(uiBinder.createAndBindUi(this));
        breadcrumb.addNavigationBreadCrumbEventHandler(navigationBreadCrumbEventHandler);
        newContainerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final List<KieImage> acceptedImages = KieContainersNavigator.this.getAcceptedImages();
                final KieImage image = acceptedImages != null && !acceptedImages.isEmpty() ? acceptedImages.get(0) : null;
                final String dbmsImage = KieLastStep.getDbms(context);
                final KieImageType dbmsImageType = dbmsImage != null ? KieImageTypeManager.getImageTypeById(dbmsImage) : null;
                final String tag = KieAppVersionStep.getTag(context);
                KieContainersNavigator.this.fireEvent(new CreateContainerEvent(image, tag, dbmsImageType));
            }
        });
    }

    public void show() {
        clear();
        showLoadingView();
        
        loadImages(new Runnable() {
            @Override
            public void run() {
                loadContainers(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingView();
                        showStepView();
                    }
                });
            }
        });
    }

    private void showStepView() {
        clearView();
        showLoadingView();
        
        // Show the navigation items, if any.
        if (getCurrentStep() != null) {
            final Collection<NavigationItem> navigationItems = getCurrentStep().items(context);
            final int itemsPerRow = getCurrentStep().getItemsPerRow();
            final List<KieContainer> containers = getAcceptedContainers();

            final boolean existItems = navigationItems != null && !navigationItems.isEmpty(); 
            if (existItems) {
                showNavigationItems(navigationItems, itemsPerRow);
            } else if (isLastStep() && ( containers == null || containers.isEmpty() ) ){
                showCreateNewContainerView();
            } else {
                showEmptyView();
            }

            // Show containers.
            fireEvent(new ShowContainersEvent(containers));
            
        } else {
            showEmptyView();
        }

        // Update the breadcrumb at current navigation point.
        updateBreadCrumb();
        
        hideLoadingView();
    }
    
    private List<KieImage> getAcceptedImages() {
        if (images != null && !images.isEmpty()) {
            final List<KieImage> result = new ArrayList<>(images.size());
            for (final KieImage image : images) {
                if (isImageAccepted(image)) {
                    result.add(image);
                }
            }
            return result;
        }
        return null;
    }

    private boolean isImageAccepted(final KieImage image) {
        for (final NavigationWorkflowStep step : steps) {
            if (!step.accepts(context, image)) return false;
        }
        return true;
    }
    
    private List<KieContainer> getAcceptedContainers() {
        if (containers != null && !containers.isEmpty()) {
            final List<KieContainer> result = new ArrayList<>(containers.size());
            for (final KieContainer container : containers) {
                if (isContainerAccepted(container)) {
                    result.add(container);
                }
            }
            return result;
        }
        return null;
    }
    
    private boolean isContainerAccepted(final KieContainer container) {
        for (final NavigationWorkflowStep step : steps) {
            if (!step.accepts(context, container)) return false;
        }
        return true;
    }
    
    private boolean isLastStep() {
        return getCurrentStep().equals(KieLastStep.INSTANCE);
    }
    
    private NavigationWorkflowStep getCurrentStep() {
        return steps.peek();
    }

    private void showEmptyView() {
        clearView();
        emptyPanel.setVisible(true);
    }
    
    private void showCreateNewContainerView() {
        clearView();
        newContainerButton.setEnabled(true);
        newContainerPanel.setVisible(true);
    }
    
    private void showNavigationItems(final Collection<NavigationItem> navigationItems, final int itemsPerRow) {
        typesPanel.clear();
        Panel hPanel = null;
        int c = 0;
        for (final NavigationItem item : navigationItems) {
            Widget view = null;
            
            // Composited items view.
            boolean isActive = false;
            if (item instanceof CompositeNavigationItem) {

                if (hPanel == null) {
                    hPanel = new Accordion();
                    isActive = true;
                    typesPanel.add(hPanel);
                }

            // Single item view grid.
            } else {
                
                if (c == 0) {
                    hPanel = new HorizontalPanel();
                    hPanel.setWidth("100%");
                    typesPanel.add(hPanel);
                }
                
                if ( c == ( itemsPerRow - 1) ) c = 0;
                else c++;
                
            }

            view = NavigationItemViewBuilder.build(item, isActive, navigationItemSelectedEventHandler);
            

            // Add the item/composite into the parent.
            hPanel.add(view);
        }
        typesPanel.setVisible(true);
    }

    private final NavigationBreadCrumbView.NavigationBreadCrumbEventHandler navigationBreadCrumbEventHandler = new NavigationBreadCrumbView.NavigationBreadCrumbEventHandler() {
        @Override
        public void onNavigateTo(final NavigationBreadCrumbView.NavigationBreadCrumbEvent event) {
            navigateToStepIndex(event.getIndex());
        }
    };
    
    private void updateBreadCrumb() {
        breadcrumb.show(steps, context);
    }
    
    private void navigateNext(final String navigationItemId) {
        if (navigationItemId != null) {
            final NavigationWorkflowStep next = getCurrentStep().navigate(navigationItemId, context);
            if (next != null) {
                steps.push(next);
            }
        }
        showStepView();
    }

    private void navigateToStepIndex(final int index) {
        GWT.log("Navigate to step with index " + index + ".");
        int size = steps.size();
        while ( size > ( index + 1 ) ) {
            final NavigationWorkflowStep step = steps.pop();
            getCurrentStep().reset(context);
            size = steps.size();
        }
        showStepView();
    }
    
    public void clear() {
        clearView();
        navigationContext.clear();
        containers.clear();
        images.clear();
        clearSteps();
    }
    
    private void clearSteps() {
        steps.clear();;
        // Initial workflow step.
        steps.push(KieAppStep.INSTANCE);
    }

    private void clearView() {
        hideLoadingView();
        typesPanel.clear();
        typesPanel.setVisible(false);
        emptyPanel.setVisible(false);
        newContainerPanel.setVisible(false);
        newContainerButton.setEnabled(false);
        breadcrumb.clear();
    }

    private final NavigationItemSelectedEventHandler navigationItemSelectedEventHandler = new NavigationItemSelectedEventHandler() {
        @Override
        public void onNavigationItemSelected(NavigationItemSelectedEvent event) {
            final String navigationItemId = event.getId();
            navigateNext(navigationItemId);
        }
    };

    private void loadContainers(final Runnable callback) {

        // Obtain running containers by querying docker remote API.
        showLoadingView();
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        final List<KieContainer> containers = kieClientManager.getContainers();
        hideLoadingView();
        KieContainersNavigator.this.containers.clear();
        if (containers != null) {
            KieContainersNavigator.this.containers.addAll(containers);
        }

        // Run the callback as the backend request has been completed.
        callback.run();
    }

    private void loadImages(final Runnable callback) {
        final KieClientManager kieClientManager = KieClientManager.getInstance();

        // Obtain running containers by querying docker remote API.
        showLoadingView();
        final List<KieImage> images = kieClientManager.getImages();
        hideLoadingView();
        KieContainersNavigator.this.images.clear();
        if (containers != null) {
            KieContainersNavigator.this.images.addAll(images);
        }

        // Run the callback as the backend request has been completed.
        callback.run();
    }

    private void showError(final Throwable throwable) {
        showError("ERROR on KieContainersNavigator. Exception: " + throwable.getMessage());
    }

    private void showLoadingView() {
        clearView();
        loadingPanel.setVisible(true);
    }

    private void hideLoadingView() {
        loadingPanel.setVisible(false);
    }


    private void showError(final String message) {
        hideLoadingView();
        Log.log(message);
    }
    
    // ****************************************************
    //                  CREATE CONTAINER EVENT
    // ****************************************************

    public interface CreateContainerEventHandler extends EventHandler
    {
        void onCreateContainer(CreateContainerEvent event);
    }

    public static class CreateContainerEvent extends GwtEvent<CreateContainerEventHandler> {

        public static Type<CreateContainerEventHandler> TYPE = new Type<CreateContainerEventHandler>();

        private KieImage image;
        private String tag;
        private KieImageType dbImageType;

        public CreateContainerEvent(KieImage image, String tag, KieImageType dbImageType) {
            this.image = image;
            this.tag = tag;
            this.dbImageType = dbImageType;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(CreateContainerEventHandler handler) {
            handler.onCreateContainer(this);
        }

        public KieImageType getDbImageType() {
            return dbImageType;
        }

        public KieImage getImage() {
            return image;
        }

        public String getTag() {
            return tag;
        }
    }

    public HandlerRegistration addCreateContainerEventHandler(final CreateContainerEventHandler handler) {
        return addHandler(handler, CreateContainerEvent.TYPE);
    }
    
    // ****************************************************
    //                  SHOW DETAILS FOR CONTAINER EVENT
    // ****************************************************

    public interface ShowContainersEventHandler extends EventHandler
    {
        void onShowContainers(ShowContainersEvent event);
    }

    public static class ShowContainersEvent extends GwtEvent<ShowContainersEventHandler> {

        public static Type<ShowContainersEventHandler> TYPE = new Type<ShowContainersEventHandler>();

        private List<KieContainer> containers;
        private boolean all = false;

        public ShowContainersEvent(final List<KieContainer> containers) {
            super();
            this.containers = containers;
        }

        public ShowContainersEvent(final boolean isAll) {
            super();
            this.all = isAll;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ShowContainersEventHandler handler) {
            handler.onShowContainers(this);
        }

        public List<KieContainer> getContainers() {
            return containers;
        }

        public boolean isAll() {
            return all;
        }
    }

    public HandlerRegistration addShowContainerEventHandler(final ShowContainersEventHandler handler) {
        return addHandler(handler, ShowContainersEvent.TYPE);
    }
    
}
