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
package org.kie.dockerui.client.widgets;

import com.github.gwtbootstrap.client.ui.*;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.KieClientManager;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.client.service.SettingsServiceAsync;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.*;

public class KieContainersNavigator extends Composite {

    private static final String IMAGE_TYPE_IMAGE_SIZE = "150px";
    private  static final int CONTAINERS_PER_ROW = 3;

    interface KieContainersNavigatorBinder extends UiBinder<Widget, KieContainersNavigator> {}
    private static KieContainersNavigatorBinder uiBinder = GWT.create(KieContainersNavigatorBinder.class);
    
    interface KieContainersNavigatorStyle extends CssResource {
        String mainPanel();
        String typesPanel();
        String emptyPanel();
        String loadingPanel();
        String typeViewWidget();
        String createContainerPanel();
        String rowTypesPanel();
    }

    
    @UiField
    KieContainersNavigatorStyle style;
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    FlowPanel typesPanel;
    
    @UiField
    FlowPanel emptyPanel;
    
    @UiField
    Breadcrumbs breadcrumb;
    
    @UiField
    FlowPanel loadingPanel;
    
    @UiField
    PopupPanel createContainerPanel;
    
    @UiField
    SplitDropdownButton createImageDropDown;
    
    private static final KieImageCategory[] WORKFLOW = new KieImageCategory[] {
            KieImageCategory.KIEAPP,
            KieImageCategory.APPSERVER,
            KieImageCategory.DBMS
    } ;
    
    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);
    private final List<KieContainer> containers = new LinkedList<KieContainer>();
    private final List<KieImage> images = new LinkedList<KieImage>();
    private int workflowStep = 0;
    private final List<KieImageType> selectedTypes = new LinkedList<KieImageType>();;
    private final Set<KieImageType> typesAvailable = new LinkedHashSet<KieImageType>();;
    private final Map<KieImageType, List<KieContainer>> _typeContainers = new HashMap<KieImageType, List<KieContainer>>();
    private final Map<KieImageType, Set<KieImage>> _typeImages = new HashMap<KieImageType, Set<KieImage>>();
    
    
    @UiConstructor
    public KieContainersNavigator() {
        initWidget(uiBinder.createAndBindUi(this));
        
    }
    
    public List<KieImageType> getSelectedTypes() {
        if (selectedTypes == null) return null;
        return new LinkedList<KieImageType>(selectedTypes);
    }
    
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

    private List<KieContainer> getContainers(final KieImageType type, final Collection<KieImageType> subTypes) {
        final List<KieContainer> result = new LinkedList<KieContainer>();
        if (type != null) {
            for (final KieContainer container : containers) {
                boolean match = container.getType().equals(type);
                if (match) {
                    if (subTypes != null ) {
                        for (KieImageType subType : subTypes) {
                            if (container.getSubTypes() != null && !container.getSubTypes().contains(subType)) {
                                match = false;
                                break;
                            }
                        }
                    }
                }
                if (match) result.add(container);
            }
        }
        return result;
    }

    public void show() {
        clear();
        showLoadingView();
        
        // First category in workflow.
        workflowStep = 0;
        
        loadImages(new Runnable() {
            @Override
            public void run() {
                loadContainers(new Runnable() {
                    @Override
                    public void run() {
                        showWorkflowCategory();
                    }
                });
            }
        });
    }

    private void showWorkflowCategory() {
        clearView();
        showLoadingView();

        if (images == null || images.size() == 0) {
            showEmptyView();
            return;
        }
        
        final boolean isCategorySupported = workflowStep == 0 || supportsCurrentCategory(selectedTypes.get(0));
        
        if (isCategorySupported && workflowStep < WORKFLOW.length) {

            typesAvailable.clear();
            final KieImageType lastSelectedType = getLastSelectedType();
            
            if ( lastSelectedType == null || !KieImageCategory.OTHERS.equals(lastSelectedType.getCategory()) ) {
                final KieImageCategory currentCategory = WORKFLOW[workflowStep];
                final List<KieImageType> categoryTypes = KieImageTypeManager.getTypes(currentCategory);
                // Show other container types different than the KIE ones on first workflow step.
                if (workflowStep == 0) {
                    categoryTypes.addAll(KieImageTypeManager.getTypes(KieImageCategory.OTHERS));
                }

                if (categoryTypes != null) {
                    // Show all type for the current category defined by the current workflow step.
                    for (final KieImageType categoryType : categoryTypes) {

                        if (KieImageType.Scope.NAMING_CONVENTION.equals(categoryType.getScope())) {

                            for (final KieImage image : images) {

                                Boolean addImage = selectedTypes.isEmpty() ? image.getType().equals(categoryType) : image.getType().equals(selectedTypes.get(0));

                                Boolean addImageSubType = true;
                                if (workflowStep > 0) {
                                    addImageSubType = hasSubType(image, categoryType);
                                } 
                                
                                if (addImage && addImageSubType) {
                                    _addTypeContainer(categoryType, image);
                                }

                            }

                        } else if (KieImageType.Scope.RUNTIME.equals(categoryType.getScope())) {

                            _addTypeContainer(categoryType, null);

                        }

                    }
                }
            }

            showTypesViews();            
        }

        updateBreadCrumb();
        hideLoadingView();
    }
    
    private boolean supportsCurrentCategory(final KieImageType type) {
        return type != null && type.getSupportedCategories() != null &&
                type.getSupportedCategories().contains(WORKFLOW[workflowStep]);
    }
    
    private boolean hasSubType(final KieImage image, final KieImageType subType) {
        if (image == null || subType == null) return false;
        if (image.getSubTypes() != null) {
            for (final KieImageType imageSubType : image.getSubTypes()) {
                if (subType.equals(imageSubType)) return true;
            }
        }
        return false;
    }
    
    private void _addTypeContainer(final KieImageType type, final KieImage image) {
        KieImageType kieType = null;
        List<KieImageType> subTypes = null;
        if (selectedTypes == null || selectedTypes.isEmpty()) {
            kieType = type;
        } else {
            kieType = selectedTypes.get(0);
            subTypes = new ArrayList<KieImageType>();
            if (selectedTypes.size() > 1) subTypes.addAll(selectedTypes.subList(1, selectedTypes.size()));
            subTypes.add(type);
        }

        final List<KieContainer> containers = getContainers(kieType, subTypes);
        typesAvailable.add(type);
        _typeContainers.put(type, containers);
        if (image != null) {
            Set<KieImage> _i = _typeImages.get(type);
            if (_i == null) {
                _i = new HashSet<KieImage>();
                _typeImages.put(type, _i);
            }
            _i.add(image);
        }
    }

    private void _removeTypeContainer(final KieImageType type) {
        selectedTypes.remove(type);
        typesAvailable.remove(type);
        _typeContainers.remove(type);
        _typeImages.remove(type);
    }

    private void showTypesViews() {
        typesPanel.clear();
        if (typesAvailable.size() == 0) {
            showEmptyView();
            return;
        } else {
            HorizontalPanel hPanel = null;
            int c = 0;
            for (final KieImageType  _type : typesAvailable) {
                final List<KieContainer> _containers = _typeContainers.get(_type);
                if (c == 0) {
                    hPanel = new HorizontalPanel();
                    hPanel.addStyleName(style.rowTypesPanel());
                    typesPanel.add(hPanel);
                }
                if (c == CONTAINERS_PER_ROW) c = -1;

                final KieImageTypeView imageTypeView = new KieImageTypeView();
                hPanel.add(imageTypeView);
                imageTypeView.setShowCreateButton(true);
                imageTypeView.addStyleName(style.typeViewWidget());
                imageTypeView.setImageSize(IMAGE_TYPE_IMAGE_SIZE);
                imageTypeView.addImageTypeSelectedEventHandler(typeSelectedEventHandler);
                imageTypeView.addCreateContainerEventHandler(createContainerEventHandler);
                imageTypeView.init(_type, _containers.size());
                c++;
            }
            typesPanel.setVisible(true);
        }
    }

    private final KieImageTypeView.ImageTypeSelectedEventHandler typeSelectedEventHandler = new KieImageTypeView.ImageTypeSelectedEventHandler() {
        @Override
        public void onContainerTypeSelected(KieImageTypeView.ImageTypeSelectedEvent event) {
            final KieImageType selectedType = event.getType();
            selectedTypes.add(selectedType);
            workflowStep++;
            onSelectedTypesUpdated();
        }
    };

    private final KieImageTypeView.CreateContainerEventHandler createContainerEventHandler = new KieImageTypeView.CreateContainerEventHandler() {
        @Override
        public void onCreateContainer(final KieImageTypeView.CreateContainerEvent event) {
            final KieImageType selectedType = event.getType();
            if (isLastWorkflowStep()) {
                selectedTypes.add(selectedType);
                workflowStep++;
                showCreateContainerView();
            } else {
                // Force to continue selecting categories until last workflow step.
                selectedTypes.add(selectedType);
                workflowStep++;
                onSelectedTypesUpdated();    
            }
        }
    };
    
    private boolean isLastWorkflowStep() {
        final KieImageType kieType = selectedTypes.size() > 0 ? selectedTypes.get(0) : null;
        if (kieType != null) {
            final List<KieImageCategory> supportedCategories = kieType.getSupportedCategories();
            return supportedCategories != null && workflowStep == supportedCategories.size() - 1;
        }
        return false;
    }
    
    private void showCreateContainerView() {

        createImageDropDown.clear();
        
        // If all the workflow steps have been completed, show the create container event request.
        final KieImageType kieType = selectedTypes.get(0);
        final List<KieImageCategory> supportedCategories = kieType.getSupportedCategories();
        
        if (selectedTypes.size() == supportedCategories.size()) {
            final boolean supportsDb = SharedUtils.supportsDatabase(kieType);
            if (!supportsDb) {
                doCreateContainer(null);
            } else {
                // Obtain the additional image to run, for the selected database.
                settingsService.getDefaultImageForType(getLastSelectedType(), new AsyncCallback<String>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        showError(throwable);
                    }

                    @Override
                    public void onSuccess(final String dbImageName) {
                        doCreateContainer(dbImageName);
                    }
                });
            }
        } else {
            showError("Strange error... cannot create container. Not all categories selected! This error should never fire..");
        }
        
    }

    private void doCreateContainer(final String dbImageName) {
        Set<KieImage> selectedImages = getSelectedImages();
        if (selectedImages != null && !selectedImages.isEmpty()) {
            final Set<String> imgs = new LinkedHashSet<String>();
            for (final KieImage selectedImage : selectedImages) {
                final StringBuilder fullName = new StringBuilder();
                final String registry = selectedImage.getRegistry();
                if (registry != null && registry.trim().length() > 0) {
                    fullName.append(registry).append("/");
                }
                final String repository = selectedImage.getRepository();
                fullName.append(repository);
                final Set<String> tags = selectedImage.getTags();

                if (tags != null && !tags.isEmpty()) {
                    final String imageName = fullName.toString();
                    for (final String tag : tags) {
                        imgs.add(imageName + ":" + tag);
                    }
                } else {
                    imgs.add(fullName.toString());
                }
            }

            for (final String i : imgs) {
                final NavLink tagLink = new NavLink(i);
                tagLink.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        hidePopup(createContainerPanel);
                        KieContainersNavigator.this.fireEvent(new CreateContainerEvent(i, dbImageName));
                    }
                });
                createImageDropDown.add(tagLink);
            }

            showPopup(createContainerPanel);

        } else {
            showError(Constants.INSTANCE.noImagesForCreateContainer());
        }
    }
    
    private void showPopup(final PopupPanel popupPanel) {
        popupPanel.getElement().getStyle().setDisplay(Style.Display.INLINE);
        popupPanel.setVisible(true);
        popupPanel.center();
        popupPanel.show();
    }
    
    private void hidePopup(final PopupPanel popupPanel) {
        popupPanel.getElement().getStyle().setDisplay(Style.Display.NONE);
        popupPanel.setVisible(false);
        popupPanel.hide();
    }
    
    private Set<KieImage> getSelectedImages() {
        int p = selectedTypes.size();
        KieImageType lastType = selectedTypes.get(p - 1);
        Set<KieImage> images = _typeImages.get(lastType);
        while ( lastType != null && (images == null || images.isEmpty()) ) {
            p--;
            lastType = selectedTypes.get(p - 1);
            images = _typeImages.get(lastType);
        }
        
        return images;
    }
    
    private void onSelectedTypesUpdated() {

        // Fire the event for the containers of the selected types.
        if (workflowStep == 0) {
            fireEvent(new ShowContainersEvent(true));
        } else {
            final List<KieContainer> containers = getContainer(getLastSelectedType());
            fireEvent(new ShowContainersEvent(containers));
        }

        showWorkflowCategory();
    }
    
    private List<KieContainer> getContainer(final KieImageType type) {
        if (type == null) return null;
        for (final Map.Entry<KieImageType, List<KieContainer>> entry : _typeContainers.entrySet()) {
            final KieImageType _type = entry.getKey();
            if (type.equals(_type)) return entry.getValue();
        }
        return null;
    }

    private KieImageType getLastSelectedType() {
        if (selectedTypes.isEmpty()) return null;
        return selectedTypes.get(selectedTypes.size() - 1);
    }

    private void showEmptyView() {
        clearView();
        emptyPanel.setVisible(true);
        updateBreadCrumb();
    }
    
    private void updateBreadCrumb() {
        breadcrumb.clear();
        final NavLink rootLink = new NavLink(Constants.INSTANCE.allCategories());
        rootLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                show();
                // Force firing show containers event with the new list.
                onSelectedTypesUpdated();
            }
        });
        breadcrumb.add(rootLink);
        if (selectedTypes != null && !selectedTypes.isEmpty()) {
            for (final KieImageType type : selectedTypes) {
                final NavLink link = new NavLink(type.getName());
                link.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        final String typeIdClicked = type.getId();
                        final List<KieImageType> _copy = new LinkedList<KieImageType>(selectedTypes);
                        for (int x = _copy.size() - 1; x >= 0; x--) {
                            final KieImageType _type = _copy.get(x);
                            if (!typeIdClicked.equals(_type.getId())) {
                                _removeTypeContainer(_type);
                                workflowStep--;
                            } else {
                                break;
                            }
                        }
                        onSelectedTypesUpdated();
                        showWorkflowCategory();
                    }
                });
                breadcrumb.add(link);
            }
        }
        breadcrumb.setVisible(true);
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
    
    private void clearView() {
        typesPanel.setVisible(false);
        emptyPanel.setVisible(false);
        loadingPanel.setVisible(false);
        breadcrumb.setVisible(false);
        hidePopup(createContainerPanel);
        
    }
    public void clear() {
        clearView();
        typesPanel.clear();
        breadcrumb.clear();
        this.selectedTypes.clear();
        this.containers.clear();
        this.workflowStep = 0;
        typesAvailable.clear();
        _typeContainers.clear();
        _typeImages.clear();
        createImageDropDown.clear();
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

        private String image;
        private String dbImage;

        public CreateContainerEvent(final String image) {
            super();
            this.image = image;
        }

        public CreateContainerEvent(String image, String dbImage) {
            this.image = image;
            this.dbImage = dbImage;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(CreateContainerEventHandler handler) {
            handler.onCreateContainer(this);
        }

        public String getImage() {
            return image;
        }

        public String getDbImage() {
            return dbImage;
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
