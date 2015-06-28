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
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
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
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.client.service.SettingsServiceAsync;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainerStartArguments;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.*;

/**
 * <p>Displays a wizard for creating KIE based containers.</p>
 */
public class KieContainerCreateWizard extends Composite {

    private static final String WORKFLOW_IMAGE_SIZE = "50px";
    private static final String IMAGE_TYPE_IMAGE_SIZE = "250px";
    
    interface KieContainerCreateWizardBinder extends UiBinder<Widget, KieContainerCreateWizard> {}
    private static KieContainerCreateWizardBinder uiBinder = GWT.create(KieContainerCreateWizardBinder.class);
    
    interface KieContainerCreateWizardStyle extends CssResource {
        String mainPanel();
        String loadingPanel();
        String emptyPanel();
        String typesPanel();
        String typeViewWidget();
        String workflowPanel();
        String workflowStepText();
        String workflowStepImage();
        String tagAndEnvsPanel();
        String envsMapWidget();
    }

    @UiField
    KieContainerCreateWizardStyle style;

    @UiField
    FlowPanel mainPanel;

    
    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    FlowPanel emptyPanel;
    
    @UiField
    PageHeader header;

    @UiField
    HorizontalPanel workflowPanel;
    
    @UiField
    Image workflowStepImage;
    
    @UiField
    Heading workflowStepText;
    
    @UiField
    FlowPanel typesPanel;
    
    @UiField
    FlowPanel tagAndEnvsPanel;
    
    @UiField
    HTML imageSelectedText;

    @UiField
    HTML dbImageSelectedText;

    @UiField
    TextBox containerName;
    
    @UiField
    TextBox dbContainerName;

    @UiField
    SplitDropdownButton tagsDropDown;

    @UiField
    MapEditor<String, String> envsMapWidget;
    
    @UiField
    HorizontalPanel buttonsPanel;
    
    @UiField
    Button cancelButton;
    
    @UiField
    Button runButton;
    

    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);
    
    private final List<KieImageType> selectedTypes = new LinkedList<KieImageType>();
    private Workflow workflowStep;
    private KieImage image;
    private String tag;
    private String dbmsImage;
    private Map<String, String> envsMap = new LinkedHashMap<String, String>();
    
    private enum Workflow {
        KIEAPP_SELECTION,
        APPSERVER_SELECTION,
        DBMS_SELECTION,
        CONTAINER_ARGUMENTS;
    }
    
    @UiConstructor
    public KieContainerCreateWizard() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // Button click handlers.
        cancelButton.addClickHandler(cancelButtonHandler);
        runButton.addClickHandler(runButtonHandler);
        
        // Env variables editor event handlers.
        envsMapWidget.setValue(envsMap, true);
        envsMapWidget.addValueAddEventHandler(envsMapAddHandler);
        envsMapWidget.addKeyModifiedEventHandler(envsMapKeyModifiedHandler);
        envsMapWidget.addValueModifiedEventHandler(envsMapValueModifiedHandler);

    }

    private final MapEditor.ValueAddEventHandler envsMapAddHandler = new MapEditor.ValueAddEventHandler() {
        @Override
        public void onValueAdd(MapEditor.ValueAddEvent event) {
            final String key = event.getKey();
            final String value = event.getValue();
            envsMap.put(key, value);

            // Redraw the editor.
            redrawEnvsMapWidget();
        }
    };

    private final MapEditor.KeyModifiedEventHandler envsMapKeyModifiedHandler = new MapEditor.KeyModifiedEventHandler() {
        @Override
        public void onKeyModified(MapEditor.KeyModifiedEvent event) {
            final String last = event.getLast();
            final String value = event.getValue();

            // If key has changed, remove old value pair.
            String newValue = "";
            if (last != null) {
                newValue = envsMap.remove(last);
            }

            // Update the parameter map.
            envsMap.put(value, newValue);

            // Redraw the editor.
            redrawEnvsMapWidget();
        }
    };

    private final MapEditor.ValueModifiedEventHandler envsMapValueModifiedHandler = new MapEditor.ValueModifiedEventHandler() {
        @Override
        public void onValueModified(MapEditor.ValueModifiedEvent event) {
            final String value = event.getValue();
            final int index = event.getIndex();

            // Look for the key object.
            final String key = getKeyParameter(index);

            // Update the parameter map.
            envsMap.put(key, value);

            // Redraw the editor.
            redrawEnvsMapWidget();
        }
    };

    private String getKeyParameter(final int index) {
        if (!envsMap.isEmpty() && index > -1) {
            int x = 0;
            for (Map.Entry<String, String> entry : envsMap.entrySet()) {
                if (index == x) return entry.getKey();
                x++;
            }

        }
        return null;
    }
    
    private void redrawEnvsMapWidget() {
        // Redraw the widget.
        envsMapWidget.setValue(envsMap, true);
        KieContainerCreateWizard.this.envsMapWidget.redraw();
    }

    public void show() {
        clear();
        showLoadingView();
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        final List<KieImage> result = kieClientManager.getImages();
        showKieAppCategoryView(result);
    }
    
    private void showEmptyView() {
        clearView();
        hideLoadingView();
        emptyPanel.setVisible(true);
    }
    
    private void showKieAppCategoryView(final List<KieImage> images) {
        workflowStep = Workflow.KIEAPP_SELECTION;
        clearView();
        showLoadingView();
        
        if (images == null || images.size() == 0) {
            showEmptyView();
            return;
        }
        
        final Set<KieImageType> kieAppAvailableTypes = new LinkedHashSet<KieImageType>();
        for (final KieImage image : images) {
            final KieImageType type = image.getType();
            if (type != null && KieImageCategory.KIEAPP.equals(type.getCategory())) {
                kieAppAvailableTypes.add(type);
            }
        }
        
        showTypesViews(kieAppAvailableTypes, new KieImageTypeView.ImageTypeSelectedEventHandler() {
            @Override
            public void onContainerTypeSelected(KieImageTypeView.ImageTypeSelectedEvent event) {
                selectedTypes.add(event.getType());
                showAppServerCategoryView(images);
            }
        });

        showWorkflowPanel();
        hideLoadingView();
        
    }

    private void showAppServerCategoryView(final List<KieImage> images) {
        workflowStep = Workflow.APPSERVER_SELECTION;
        clearView();
        showLoadingView();

        if (images == null || images.size() == 0) {
            showEmptyView();
            return;
        }

        final Set<KieImageType> kieAppAvailableTypes = new LinkedHashSet<KieImageType>();
        for (final KieImage image : images) {
            final KieImageType type = image.getType();
            if (type != null && selectedTypes.contains(type)) {
                List<KieImageType> subTypes = image.getSubTypes();
                for (final KieImageType subType : subTypes) {
                    if (subType != null && KieImageCategory.APPSERVER.equals(subType.getCategory())) {
                        kieAppAvailableTypes.add(subType);
                    }
                }
            }
        }

        showTypesViews(kieAppAvailableTypes, new KieImageTypeView.ImageTypeSelectedEventHandler() {
            @Override
            public void onContainerTypeSelected(KieImageTypeView.ImageTypeSelectedEvent event) {
                selectedTypes.add(event.getType());
                showDBNMSCategoryView(images);
            }
        });

        showWorkflowPanel();
        hideLoadingView();
    }

    private void showDBNMSCategoryView(final List<KieImage> images) {
        workflowStep = Workflow.DBMS_SELECTION;
        clearView();
        showLoadingView();

        if (images == null || images.size() == 0) {
            showEmptyView();
            return;
        }

        final List<KieImageType> dbmsTypes = KieImageTypeManager.getTypes(KieImageCategory.DBMS);
        
        final Set<KieImage> selectedImages = new LinkedHashSet<KieImage>();
        for (final KieImage image : images) {
            final KieImageType type = image.getType();
            if (type != null && selectedTypes.get(0).equals(type)) {
                List<KieImageType> subTypes = image.getSubTypes();
                for (final KieImageType subType : subTypes) {
                    if (hasSubType(image, getLastSelectedType())) {
                        if (subType != null && KieImageCategory.APPSERVER.equals(subType.getCategory())) {
                            selectedImages.add(image);
                        }
                    }
                }
            }
        }
        
        if (selectedImages.size() > 1) {
            showError("Error - More than one image is selected from the specified categories, and the database selection does not depends on the existing build images, it just depends on runtime configuration. Please contact administrator.");
            show();
        }

        if (selectedImages.size() == 0) {
            showError("Error - No image resulting from the specified categories. Please start the wizard again or contact the administrator.");
            show();
        }

        showTypesViews(new HashSet<KieImageType>(dbmsTypes), new KieImageTypeView.ImageTypeSelectedEventHandler() {
            @Override
            public void onContainerTypeSelected(KieImageTypeView.ImageTypeSelectedEvent event) {
                final KieImageType selectedType = event.getType();
                selectedTypes.add(selectedType);
                KieContainerCreateWizard.this.image = selectedImages.iterator().next();
                showContainerArgumentsView();
            }
        });

        showWorkflowPanel();
        hideLoadingView();
    }
    
    private void showContainerArgumentsView() {
        workflowStep = Workflow.CONTAINER_ARGUMENTS;
        clearView();
        showLoadingView();
        
        if (image == null) {
            showEmptyView();
            return;
        }

        // Obtain the additional image to run, for the selected database.
        //  TODO: When this widget will be used again, check SharedUtils.supportsDatabase(kieType);
        settingsService.getDefaultImageForType(getLastSelectedType(), new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final String dbmsImageName) {
                updateTagSelectedView();

                KieContainerCreateWizard.this.dbmsImage = dbmsImageName;
                if (dbmsImageName != null) {
                    dbImageSelectedText.setHTML(dbmsImageName);
                    dbContainerName.setEnabled(true);
                } else {
                    dbImageSelectedText.setHTML(Constants.INSTANCE.noImageRequiredForDatabase());
                    dbContainerName.setEnabled(false);
                }
                if (image.getTags() != null) {
                    for (final String tag : image.getTags()) {
                        final NavLink tagLink = new NavLink(tag);
                        tagLink.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent clickEvent) {
                                KieContainerCreateWizard.this.tag = tag;
                                updateTagSelectedView();
                            }
                        });
                        tagsDropDown.add(tagLink);
                    }
                }
                tagAndEnvsPanel.setVisible(true);
                buttonsPanel.setVisible(true);
                hideLoadingView();

            }
        });

        showWorkflowPanel();
    }
    
    private KieImageType getLastSelectedType() {
        if (selectedTypes.size() == 0) return  null;
        return selectedTypes.get(selectedTypes.size() - 1);
    }
    
    private void updateTagSelectedView() {
        final String _image = SharedUtils.getImage(image.getRegistry(), image.getRepository(), tag);
        imageSelectedText.setText(_image);
        if (tag != null) {
            tagsDropDown.setIcon(IconType.OK);
            tagsDropDown.setText(tag);
        } else {
            tagsDropDown.setIcon(IconType.TAG);
            tagsDropDown.setText(Constants.INSTANCE.tag());
        }
    }
    
    private final ClickHandler cancelButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            // Reset views and go home.
            show();
        }
    };

    private final ClickHandler runButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            String _dbImage = null;
            String _dbName = null;
            KieImageType _dbType = null;
            if (checkSelectedTag() && checkContainerName()) {
                if (dbmsImage != null) {
                    if (!checkDbContainerName()) return;
                    _dbImage = dbmsImage;
                    _dbName = dbContainerName.getText();
                    _dbType = getLastSelectedType();
                }
                final String _image = SharedUtils.getImage(null, image.getRepository(), tag);
                final KieContainerStartArguments arguments = new KieContainerStartArguments(selectedTypes.get(0), _image, containerName.getText(), _dbType, _dbImage, _dbName, envsMap);
                fireEvent(new ContainerCreatedEvent(arguments));
            }
        }
    };
    
    private boolean checkSelectedTag() {
        if (tag == null) {
            showError(Constants.INSTANCE.selectTag());
            return false;
        }
        return true;
    }

    private boolean checkContainerName() {
        if (containerName.getText() == null || containerName.getText().trim().length() == 0) {
            showError(Constants.INSTANCE.selectContainerName());
            return false;
        }
        return true;
    }

    private boolean checkDbContainerName() {
        if (dbContainerName.getText() == null || dbContainerName.getText().trim().length() == 0) {
            showError(Constants.INSTANCE.selectContainerName());
            return false;
        }
        return true;
    }
    
    
    private void showTypesViews(final Set<KieImageType> kieAppAvailableTypes, final KieImageTypeView.ImageTypeSelectedEventHandler selectedEvent) {
        if (kieAppAvailableTypes.size() == 0) {
            showEmptyView();
            return;
        } else {
            HorizontalPanel hPanel = null;
            int c = 0;
            for (final KieImageType type : kieAppAvailableTypes) {
                if (c == 0) {
                    hPanel = new HorizontalPanel();
                    typesPanel.add(hPanel);
                }
                if (c == 2) c = -1;
                final KieImageTypeView imageTypeView = new KieImageTypeView();
                imageTypeView.addStyleName(style.typeViewWidget());
                imageTypeView.setImageSize(IMAGE_TYPE_IMAGE_SIZE);
                hPanel.add(imageTypeView);
                imageTypeView.addImageTypeSelectedEventHandler(selectedEvent);
                imageTypeView.init(type);
                c++;
            }
            typesPanel.setVisible(true);
        }
    }
    
    private boolean hasSubType(final KieImage image, final KieImageType type) {
        if (image.getSubTypes() != null) {
            for (final KieImageType subType : image.getSubTypes()) {
                if (subType != null && subType.equals(type)) return true;
            }
            
        }
        return false;
    }
    
    private void showWorkflowPanel() {
        if (workflowStep != null) {
            switch (workflowStep) {
                case KIEAPP_SELECTION:
                    workflowStepImage.setUrl(Images.INSTANCE.numberOne().getSafeUri());
                    workflowStepText.setText(Constants.INSTANCE.selectKieAppType());
                    break;
                case APPSERVER_SELECTION:
                    workflowStepImage.setUrl(Images.INSTANCE.numberTwo().getSafeUri());
                    workflowStepText.setText(Constants.INSTANCE.selectAppServerType());
                    break;
                case DBMS_SELECTION:
                    workflowStepImage.setUrl(Images.INSTANCE.numberThree().getSafeUri());
                    workflowStepText.setText(Constants.INSTANCE.selectDbmsType());
                    break;
                case CONTAINER_ARGUMENTS:
                    workflowStepImage.setUrl(Images.INSTANCE.numberFour().getSafeUri());
                    workflowStepText.setText(Constants.INSTANCE.selectTagAndEnvs());
                    break;
            }
            workflowStepImage.setSize(WORKFLOW_IMAGE_SIZE, WORKFLOW_IMAGE_SIZE);
            workflowPanel.setVisible(true);
        }
    }
    
    private void showLoadingView() {
        loadingPanel.center();
        loadingPanel.setVisible(true);
        loadingPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        loadingPanel.show();
    }

    private void hideLoadingView() {
        loadingPanel.setVisible(false);
        loadingPanel.getElement().getStyle().setDisplay(Style.Display.NONE);
        loadingPanel.hide();
    }
    
    private void showError(final Throwable throwable) {
        showError("ERROR on KieContainerCreate. Exception: " + throwable.getMessage());
    }
    
    private void showError(final String message) {
        hideLoadingView();
        Log.log(message);
    }
    
    private void clearView() {
        loadingPanel.setVisible(false);
        emptyPanel.setVisible(false);
        header.setVisible(false);
        typesPanel.setVisible(false);
        typesPanel.clear();
        workflowPanel.setVisible(false);
        tagAndEnvsPanel.setVisible(false);
        buttonsPanel.setVisible(false);
    }
    
    public void clear() {
        clearView();
        selectedTypes.clear();
        workflowStep = null;
        image = null;
        tag = null;
        dbmsImage = null;
        envsMap.clear();
        imageSelectedText.setText("");
        containerName.setText("");
        dbContainerName.setText("");
        tagsDropDown.clear();
        envsMapWidget.clear();
    }

    // ****************************************************
    //         CONTAINER CREATED EVENT
    // ****************************************************

    public interface ContainerCreatedEventHandler extends EventHandler
    {
        void onContainerCreated(ContainerCreatedEvent event);
    }

    public static class ContainerCreatedEvent extends GwtEvent<ContainerCreatedEventHandler> {

        public static Type<ContainerCreatedEventHandler> TYPE = new Type<ContainerCreatedEventHandler>();

        private KieContainerStartArguments arguments;

        public ContainerCreatedEvent(final KieContainerStartArguments arguments) {
            super();
            this.arguments = arguments;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ContainerCreatedEventHandler handler) {
            handler.onContainerCreated(this);
        }

        public KieContainerStartArguments getArguments() {
            return arguments;
        }
    }

    public HandlerRegistration addContainerCreatedEventHandler(final ContainerCreatedEventHandler handler) {
        return addHandler(handler, ContainerCreatedEvent.TYPE);
    }
}
