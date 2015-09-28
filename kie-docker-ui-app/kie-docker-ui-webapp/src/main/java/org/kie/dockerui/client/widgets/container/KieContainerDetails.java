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
package org.kie.dockerui.client.widgets.container;

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.DatabaseUtils;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KieContainerDetails extends Composite {

    
    interface KieContainerDetailsBinder extends UiBinder<Widget, KieContainerDetails> {}
    private static KieContainerDetailsBinder uiBinder = GWT.create(KieContainerDetailsBinder.class);
    
    interface KieContainerDetailsStyle extends CssResource {
        String mainPanel();
        String loadingPanel();
        String detailsFormPanel();
        String portsGrid();
        String envVarsGrid();
        String containerSSHCommand();
        String clear();
        String artifactVersions();
        String pullAddressNotePanel();
    }

    @UiField
    KieContainerDetailsStyle style;

    @UiField
    FlowPanel mainPanel;
    
    @UiField
    TimeoutPopupPanel loadingPanel;
    
    @UiField
    FlowPanel detailsFormPanel;
    
    @UiField
    TextBox containerId;
    
    @UiField
    TextBox containerName;
    
    @UiField
    TextBox containerImage;
    
    @UiField
    TextBox containerStatus;
    
    @UiField
    TextBox containerCommand;
    
    @UiField
    TextBox containerIP;

    @UiField
    HTML containerPullAddress;
    
    @UiField
    HTML containerSSHCommand;
    
    @UiField
    Hyperlink containerWebAddress;
    
    @UiField
    DataGrid<Map.Entry<String, String>> portsGrid;
    
    @UiField
    DataGrid<Map.Entry<String, String>> envVarsGrid;
    
    @UiField
    AccordionGroup pullAddressGroup;
    
    @UiField
    AccordionGroup sshCommandGroup;

    @UiField
    AccordionGroup artifactVersionsGroup;

    @UiField
    AccordionGroup webAddressGroup;

    @UiField
    AccordionGroup jdbcGroup;
    
    @UiField
    HTML jdbcUrl;
    
    @UiField
    HTML jdbcUser;
    
    @UiField
    HTML jdbcPassword;
    
    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);

    @UiConstructor
    public KieContainerDetails() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void show(final KieContainer container) {
        clear();
        showLoadingView();

        // Obtain current settings from client cache.
        final Settings settings = SettingsClientHolder.getInstance().getSettings();
        
        // Inspect the container.
        dockerService.inspect(container.getId(), new AsyncCallback<org.kie.dockerui.shared.model.KieContainerDetails>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final org.kie.dockerui.shared.model.KieContainerDetails kieContainerDetails) {
                hideLoadingView();
                load(container, kieContainerDetails, settings);
                detailsFormPanel.setVisible(true);
            }
        });
        
    }
    
    private void load(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) {
        containerId.setValue(container.getId());
        containerName.setValue(container.getName());
        containerImage.setValue(container.getImage());
        containerStatus.setValue(container.getStatus());
        containerIP.setValue(details.getIpAddress());
        final KieImageType type = container.getType();
        artifactVersionsGroup.clear();
        if (type != null && KieImageCategory.KIEAPP.equals(type.getCategory())) {
            final String sshCommand = ClientUtils.getSSHCommand(container, details, settings);
            final String webAddress  = ClientUtils.getWebAddress(container, details, settings);
            final String siteAddress  = ClientUtils.getSiteAddress(container, details, settings);
            if (settings.isRegistryEnabled()) {
                final String pullAddress = ClientUtils.getPullAddress(container, details, settings);
                containerPullAddress.setText(pullAddress);
                pullAddressGroup.setVisible(true);
            } else {
                containerPullAddress.setText("");
                pullAddressGroup.setVisible(false);
            }
            containerSSHCommand.setText(sshCommand);
            containerWebAddress.setText(webAddress);
            containerWebAddress.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    Window.open(webAddress, "_blank", "");
                }
            });
            sshCommandGroup.setVisible(true);
            webAddressGroup.setVisible(true);
            final VerticalPanel p = new VerticalPanel();
            p.addStyleName(style.artifactVersions());
            final Frame f = new Frame(siteAddress);
            f.addStyleName(style.artifactVersions());
            final Button b = new Button(Constants.INSTANCE.navigate());
            b.setType(ButtonType.INFO);
            b.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    try {
                        Window.open(siteAddress,"_blank","");
                    } catch (IllegalStateException e) {
                        showError(Constants.INSTANCE.noPublicPortsAvailable());
                    }
                }
            });
            p.add(f);
            p.add(b);
            artifactVersionsGroup.add(p);
            artifactVersionsGroup.setVisible(true);

            // Specify the password for the dbms container via environment variables.
            jdbcGroup.setVisible(false);
            if (SharedUtils.supportsDatabase(container)) {
                final Map<String, String> dbContainerEnvs = ClientUtils.toMap(details.getEnvVars(), "=");
                if (dbContainerEnvs != null) {
                    final String _jdbcUrl = ClientUtils.getValue(dbContainerEnvs, DatabaseUtils.KIE_CONNECTION_URL);
                    if (_jdbcUrl != null && _jdbcUrl.trim().length() > 0) {
                        jdbcUrl.setText(_jdbcUrl);
                        jdbcUser.setText(ClientUtils.getValue(dbContainerEnvs, DatabaseUtils.KIE_CONNECTION_USER));
                        jdbcPassword.setText(ClientUtils.getValue(dbContainerEnvs, DatabaseUtils.KIE_CONNECTION_PASSWORD));
                        jdbcGroup.setVisible(true);
                    }
                }
            }
        } else {
            containerPullAddress.setText("");
            containerSSHCommand.setText("");
            containerWebAddress.setText("");
            pullAddressGroup.setVisible(false);
            sshCommandGroup.setVisible(false);
            webAddressGroup.setVisible(false);
            artifactVersionsGroup.setVisible(false);
            jdbcGroup.setVisible(false);
        }
        
        configurePortsGrid(container, details, settings);
        configureEnvVarsGrid(container, details, settings);
    }
    
    private void configurePortsGrid(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) {

        // NOTE: If not removing and re-adding columns, grid tow data refresh is not well done. 
        if (portsGrid.getColumnCount() > 0) {
            portsGrid.removeColumn(0);
            portsGrid.removeColumn(0);
        }
        
        // Set emtpy message.
        portsGrid.setEmptyTableWidget(new Label(Constants.INSTANCE.noPorts()));
        
        // Create Private port column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String> privatePortColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<String, String> object) {
                        return object.getKey().toString();
                    }
                };
        privatePortColumn.setSortable(false);
        privatePortColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        portsGrid.addColumn(privatePortColumn, Constants.INSTANCE.privatePort());
        portsGrid.setColumnWidth(privatePortColumn, 40, Style.Unit.PCT);

        // Create Public port column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String> publicPortColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<String, String> object) {
                        return object.getValue().toString();
                    }
                };
        publicPortColumn.setSortable(false);
        publicPortColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        portsGrid.addColumn(publicPortColumn, Constants.INSTANCE.publicPort());
        portsGrid.setColumnWidth(publicPortColumn, 40, Style.Unit.PCT);


        final List<Map.Entry<String, String>> list = ClientUtils.toMapEntries(container.getPorts());
        if (list != null) {
            portsGrid.setRowCount(list.size());
            portsGrid.setRowData(0, list);
        }
        
    }

    private void configureEnvVarsGrid(final KieContainer container, final org.kie.dockerui.shared.model.KieContainerDetails details, final Settings settings) {

        // NOTE: If not removing and re-adding columns, grid tow data refresh is not well done. 
        if (envVarsGrid.getColumnCount() > 0) {
            envVarsGrid.removeColumn(0);
            envVarsGrid.removeColumn(0);
        }

        // Set emtpy message.
        envVarsGrid.setEmptyTableWidget(new Label(Constants.INSTANCE.noEnvVars()));

        // Create name column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String> nameColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<String, String> object) {
                        return object.getKey().toString();
                    }
                };
        nameColumn.setSortable(false);
        nameColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        envVarsGrid.addColumn(nameColumn, Constants.INSTANCE.name());
        envVarsGrid.setColumnWidth(nameColumn, 20, Style.Unit.PCT);

        // Create value column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String> valueColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<String, String>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<String, String> object) {
                        return object.getValue().toString();
                    }
                };
        valueColumn.setSortable(false);
        valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        envVarsGrid.addColumn(valueColumn, Constants.INSTANCE.value());
        envVarsGrid.setColumnWidth(valueColumn, 20, Style.Unit.PCT);


        final List<Map.Entry<String, String>> list = ClientUtils.toMapEntries(details.getEnvVars(), "=");
        if (list != null) {
            envVarsGrid.setRowCount(list.size());
            envVarsGrid.setRowData(0, list);
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
        showError("ERROR on KieContainerDetails. Exception: " + throwable.getMessage());
    }
    
    private void showError(final String message) {
        hideLoadingView();
        Log.log(message);
    }
    
    private void clearView() {
        loadingPanel.setVisible(false);
        detailsFormPanel.setVisible(false);
    }
    
    private void clearForm() {
        containerId.setValue("");
        containerName.setValue("");
        containerImage.setValue("");
        containerStatus.setValue("");
        containerIP.setValue("");
        containerPullAddress.setText("");
        containerSSHCommand.setText("");
        containerWebAddress.setText("");
        final List<Map.Entry<String, String>> emptyList = new ArrayList<Map.Entry<String, String>>();
        portsGrid.setRowCount(0);
        portsGrid.setRowData(0, emptyList);
        envVarsGrid.setRowCount(0);
        envVarsGrid.setRowData(0, emptyList);
    }
    
    
    public void clear() {
        clearView();
        clearForm();
    }
}
