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
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.*;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.shared.KieImageTypeManager;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieContainerStartArguments;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.DatabaseUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>Creates and start KIE based containers.</p>
 * <p>Take cares about if a database container must be run too.</p>
 */
public class KieContainerStart extends Composite {

    interface KieContainerStartBinder extends UiBinder<Widget, KieContainerStart> {}
    private static KieContainerStartBinder uiBinder = GWT.create(KieContainerStartBinder.class);
    
    interface KieContainerStartStyle extends CssResource {
        String mainPanel();
        String loadingPanel();
        String mainPanels();
        String logsWidget();
        String logsPanel();
    }

    @UiField
    KieContainerStartStyle style;

    @UiField
    FlowPanel mainPanel;

    @UiField
    Alert alert;

    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    Heading header;

    @UiField
    FlowPanel slidingPanelWrapper;
    
    @UiField
    FlowPanel argumentsPreviewPanel;
    
    @UiField
    HTML imageSelectedText;
    
    @UiField
    TextBox containerName;
    
    @UiField
    VerticalPanel dbImageArgumentsPanel;
    
    @UiField
    HTML dbImageSelectedText;
    
    @UiField
    TextBox dbContainerName;
    
    @UiField
    FlowPanel logsPanel;

    @UiField
    KieContainerLogs logsWidget;
    
    @UiField
    FlowPanel buttonsPanel;
    
    @UiField
    Button cancelButton;
    
    @UiField
    Button nextButton;
    
    @UiField
    FlowPanel databasePanel;
    @UiField
    
    Image databaseCreatedImage;
    @UiField
    
    HTML databaseJdbcUrlText;
    @UiField
    
    HTML databaseUserText;
    @UiField
    
    HTML databasePasswordText;
    @UiField
    
    HTML databaseNameText;

    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);
    private final DatabaseServiceAsync databaseService = GWT.create(DatabaseService.class);

    private HandlerRegistration logsNextButtonHandlerRegistration;
    
    @UiConstructor
    public KieContainerStart() {
        initWidget(uiBinder.createAndBindUi(this));
        
        // Configure button handlers.
        cancelButton.addClickHandler(cancelButtonHandler);
    }
    
    private final ClickHandler cancelButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            clear();
        }
    };

    public void show(final KieContainerStartArguments arguments) {
        clear();
        showInputArguments(arguments);
    }
    
    private boolean checkArguments(final KieContainerStartArguments arguments) {

        if (arguments == null) {
            showError("No arguments about the image to start");
            return false;
        }
        
        final String image = arguments.getImage();
        final String containerName = arguments.getContainerName();
        final String dbImage = arguments.getDbImage();
        final String dbContainerName = arguments.getDbContainerName();
        final Map<String, String> envs = arguments.getEnvs();

        if (isEmpty(image)) {
            showError("No image specified to start");
            return false;
        }

        if (isEmpty(containerName)) {
            showError("No container name specified");
            return false;
        }

        if (dbImage != null && isEmpty(dbContainerName)) {
            showError("No database container name specified");
            return false;
        }
        
        return true;
    }
    
    private void showInputArguments(final KieContainerStartArguments arguments) {
        showLoadingView();

        if (checkArguments(arguments)) {
            final String image = arguments.getImage();
            final String name = arguments.getContainerName();
            String dbImage = arguments.getDbImage();
            final String dbName = arguments.getDbContainerName();
            final Map<String, String> envs = arguments.getEnvs();

            header.setText(Constants.INSTANCE.verifyContainerArguments());
            imageSelectedText.setText(image);
            containerName.setText(name);
            
            
            if (dbImage != null && dbImage.equals(KieImageTypeManager.TYPE_IN_MEMORY_DB_ID)) {
                dbImageSelectedText.setText(Constants.INSTANCE.inMemoryDB());
                dbContainerName.setText("");
                dbImage = null;
            } else if (dbImage != null) {
                dbImageSelectedText.setText(dbImage);
                dbContainerName.setText(dbName);
            } else {
                dbImageSelectedText.setText(Constants.INSTANCE.noImageRequiredForDatabase());
                dbContainerName.setText("");
            }
            // TODO: Env vars.

            final String _dbImage = dbImage;
            addNextButtonHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    if (_dbImage != null) {
                        showNextButton(Constants.INSTANCE.createAndStartDatabaseContainer());
                        runDatabase(arguments);
                    } else {
                        showNextButton(Constants.INSTANCE.createAndStartKieContainer());
                        runImage(arguments, null, null);
                    }
                }
            });
            
            databasePanel.setVisible(false);
            logsPanel.setVisible(false);
            argumentsPreviewPanel.setVisible(true);
            hideLoadingView();
        }
        
    }
    
    private void addNextButtonHandler(final ClickHandler handler) {
        if (logsNextButtonHandlerRegistration != null) logsNextButtonHandlerRegistration.removeHandler();
        if (handler != null) {
            logsNextButtonHandlerRegistration = nextButton.addClickHandler(handler);
        }
    }
    
    private void showNextButton(final String text) {
        nextButton.setText(text);
        nextButton.setVisible(true);
    }

    private void runDatabase(final KieContainerStartArguments arguments) {
        final String image = arguments.getImage();
        final String name = arguments.getContainerName();
        final String dbImage = arguments.getDbImage();
        final String dbName = arguments.getDbContainerName();
        final KieImageType dbImageType = arguments.getDbImageType();

        // Specify the password for the dbms container via environemtn variables.
        Map<String, String> dbContainerEnvs = getDbContainerEnvs(dbImageType);
        
        // Create the database container..
        create(dbImage, dbName, dbContainerEnvs, null, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final String id) {
                hideLoadingView();
                showAlert(Constants.INSTANCE.containerCreatedWithId() + " " + id);
                
                // Run the database container
                showLoadingView();
                start(id, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        showError(throwable);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        hideLoadingView();
                        showAlert(Constants.INSTANCE.containerStartedWithId() + " " + id);
                        
                        header.setText(Constants.INSTANCE.runningDbContainer() + " " + dbImageType.getName());
                        showNextButton(Constants.INSTANCE.createKieDatabase());
                        showLogs(id, new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent clickEvent) {
                                showDatabaseDetails(arguments, id);
                            }
                        });
                    }
                });
            }
        });
    }
    
    private void createKieDatabase(final String containerId, final KieImageType type, final AsyncCallback<Void> callback) {
        final String dbName = DatabaseUtils.getDefaultDatabaseName();
        try {
            databaseService.createDatabase(containerId, type, dbName, callback);
        } catch (Exception e) {
            showError(e);
        }
    }
    
    private Map<String, String> getDbContainerEnvs(final KieImageType type) {
        if (type == null) return null;
        final String[] passwordEnv = DatabaseUtils.buildPasswordEnvVar(type);
        Map<String, String> dbContainerEnvs = null;
        if (passwordEnv != null) {
            dbContainerEnvs = new LinkedHashMap<String, String>();
            dbContainerEnvs.put(passwordEnv[0], passwordEnv[1]);
        }
        return dbContainerEnvs;
    }
    
    private void showDatabaseDetails(final KieContainerStartArguments arguments, final String dbContainerId) {
        clearView();
        showLoadingView();

        final String dbName = arguments.getDbContainerName();
        final KieImageType dbImageType = arguments.getDbImageType();

        createKieDatabase(dbContainerId, dbImageType, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(Void aVoid) {

                // Calculate the environment variables for the connection and the link value.
                final String linkName = dbName;
                final String linkAlias = DatabaseUtils.getAlias(dbImageType);
                final String[] alias = new String[]{linkName, linkAlias};


                dockerService.getContainer(dbContainerId, new AsyncCallback<KieContainer>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        showError(throwable);
                    }

                    @Override
                    public void onSuccess(final KieContainer dbContainer) {
                        if (dbContainer == null) {
                            showError("Cannot obtain the previous database container running with id " + dbContainerId);
                            return;
                        }

                        // Obtain current settings from client cache.
                        final Settings settings = SettingsClientHolder.getInstance().getSettings();
                        final String dbName = DatabaseUtils.getDefaultDatabaseName();

                        // Specify the password for the dbms container via environment variables.
                        final Map<String, String> dbContainerEnvs = DatabaseUtils.buildEnvsForDbConnection(dbImageType != null ? dbImageType : KieImageTypeManager.KIE_H2_IN_MEMORY,
                                settings.getPublicHost(), DatabaseUtils.getPublicPort(dbContainer), dbName);

                        databaseCreatedImage.setUrl(Images.INSTANCE.greenTick().getSafeUri());
                        databaseJdbcUrlText.setText(ClientUtils.getValue(dbContainerEnvs, DatabaseUtils.KIE_CONNECTION_URL));
                        databaseUserText.setText(ClientUtils.getValue(dbContainerEnvs, DatabaseUtils.KIE_CONNECTION_USER));
                        databasePasswordText.setText(ClientUtils.getValue(dbContainerEnvs, DatabaseUtils.KIE_CONNECTION_PASSWORD));
                        databaseNameText.setText(dbName);

                        logsPanel.setVisible(false);
                        argumentsPreviewPanel.setVisible(false);
                        databasePanel.setVisible(true);


                        showNextButton(Constants.INSTANCE.createAndStartKieContainer());
                        hideLoadingView();
                        addNextButtonHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent clickEvent) {
                                // Create and run the kie image.
                                runImage(arguments, dbContainerEnvs, alias);
                            }
                        });
                    }
                });
            }
        });

    }
    
    private void showLogs(final String id, final ClickHandler nextButtonHandler) {
        
        // Enable widget display.
        logsWidget.show(id);
        addNextButtonHandler(nextButtonHandler);
        databasePanel.setVisible(false);
        argumentsPreviewPanel.setVisible(false);
        logsPanel.setVisible(true);
    }
    
    private void runImage(final KieContainerStartArguments arguments, final Map<String, String> dbContainerEnvs, final String[] alias) {
        showLoadingView();
        
        final String image = arguments.getImage();
        final String name = arguments.getContainerName();
        final KieImageType imageType = arguments.getImageType();
        Map<String, String> envs = arguments.getEnvs();

        // Check if container must be linked with the database one.
        if (dbContainerEnvs != null) {
            if (envs != null) envs.putAll(dbContainerEnvs);
            else envs = dbContainerEnvs;
        }
        
        // Create the kie container from the specified image, and use container linking if necessary.
        create(image, name, envs, alias, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final String id) {
                hideLoadingView();
                showAlert(Constants.INSTANCE.containerCreatedWithId() + " " + id);

                // Run the kie container.
                // If linking, set KIE_CONNECTION_DATABASE!
                showLoadingView();
                start(id, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        showError(throwable);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        hideLoadingView();
                        showAlert(Constants.INSTANCE.containerStartedWithId() + " " + id);

                        header.setText(Constants.INSTANCE.runningKieContainer() + " " + imageType.getName());
                        showNextButton(Constants.INSTANCE.finished());
                        cancelButton.setVisible(false);
                        showLogs(id, new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent clickEvent) {
                                clear();
                                fireEvent(new ContainerStartedEvent(id));
                            }
                        });
                    }
                });
                
            }
        });
        
    }
    
    
    
    private void create(final String image, final String name, final Map<String, String> arguments, String[] linking, final AsyncCallback<String> callback) {
        final String[] args = envsToString(arguments);
        dockerService.create(image, name, args, linking, callback);
    }

    private void start(final String id, final AsyncCallback<Void> callback) {
        dockerService.start(id, callback);
    }

    private static String[] envsToString(Map<String, String> envsMap) {
        if (envsMap != null && envsMap.size() > 0) {
            final List<String> result = new LinkedList<String>();
            for(final  Map.Entry<String, String> entry : envsMap.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                result.add(key + "=" + value);
            }
            return result.toArray(new String[result.size()]);
        }
        return null;
    }
    
    private boolean isEmpty(final String str) {
        return str == null || str.trim().length() == 0;
    }
    
    private void showAlert(final String message) {
        alert.setText(message);
        alert.setVisible(true);
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
        showError("ERROR on KieContainerDetails. Exception: " + throwable.getMessage());
    }
    
    private void showError(final String message) {
        hideLoadingView();
        Log.log(message);
        clear();
        nextButton.setVisible(false);
    }
    
    private void clearView() {
        loadingPanel.setVisible(false);
        alert.setVisible(false);
    }

    public void clear() {
        clearView();
        header.setText("");
        imageSelectedText.setText("");
        dbImageSelectedText.setText("");
        containerName.setText("");
        dbContainerName.setText("");
        logsWidget.clear();
        databaseJdbcUrlText.setText("");
        databaseUserText.setText("");
        databasePasswordText.setText("");
        databaseNameText.setText("");
    }


    // ****************************************************
    //         CONTAINER STARTED EVENT
    // ****************************************************

    public interface ContainerStartedEventHandler extends EventHandler
    {
        void onContainerStarted(ContainerStartedEvent event);
    }

    public static class ContainerStartedEvent extends GwtEvent<ContainerStartedEventHandler> {

        public static Type<ContainerStartedEventHandler> TYPE = new Type<ContainerStartedEventHandler>();

        private String id;

        public ContainerStartedEvent(final String id) {
            super();
            this.id = id;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ContainerStartedEventHandler handler) {
            handler.onContainerStarted(this);
        }

        public String getId() {
            return id;
        }
    }

    public HandlerRegistration addContainerStartedEventHandler(final ContainerStartedEventHandler handler) {
        return addHandler(handler, ContainerStartedEvent.TYPE);
    }
}
