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
package org.kie.dockerui.client.widgets.container.explorer;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import org.kie.dockerui.client.KieClientManager;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.client.widgets.cell.ClickableImageResourceCell;
import org.kie.dockerui.client.widgets.cell.ContainerActionsCell;
import org.kie.dockerui.client.widgets.cell.ContainerTypesCell;
import org.kie.dockerui.shared.model.KieAppStatus;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.Comparator;
import java.util.List;

public class KieContainersExplorer extends Composite {

    private static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_FULL);

    interface KieContainersExplorerBinder extends UiBinder<Widget, KieContainersExplorer> {}
    private static KieContainersExplorerBinder uiBinder = GWT.create(KieContainersExplorerBinder.class);
    
    interface KieContainersExplorerStyle extends CssResource {
        String mainPanel();
        String containerList();
        String loadingPanel();
        String emptyPanel();
        String containersPanel();
    }

    @UiField
    KieContainersExplorerStyle style;
    
    @UiField
    HTMLPanel mainPanel;

    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    FlowPanel emptyPanel;

    @UiField
    HTMLPanel containersPanel;
    
    @UiField(provided = true)
    CellTable containerList;

    @UiField(provided = true)
    SimplePager pager;

    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);


    /**
     * The provider that holds the list of containers.
     */
    private final ListDataProvider<KieContainer> containersProvider = new ListDataProvider<KieContainer>();
    /**
     * Add a new container.
     *
     * @param container the container to add.
     */
    public void addContainer(KieContainer container) {
        List<KieContainer> contacts = containersProvider.getList();
        // Remove the contact first so we don't add a duplicate.
        contacts.remove(container);
        contacts.add(container);
    }
    
    private static final ProvidesKey<KieContainer> KEY_PROVIDER = new ProvidesKey<KieContainer>() {
        @Override
        public Object getKey(KieContainer item) {
            return item == null ? null : item.getId();
        }
    };
    
    @UiConstructor
    public KieContainersExplorer() {

        containerList = new CellTable<KieContainer>(KEY_PROVIDER);
        containerList.setWidth("100%", true);

        // Do not refresh the headers and footers every time the data is updated.
        containerList.setAutoHeaderRefreshDisabled(true);
        containerList.setAutoFooterRefreshDisabled(true);

        // Attach a column sort handler to the ListDataProvider to sort the list.
        ColumnSortEvent.ListHandler<KieContainer> sortHandler = new ColumnSortEvent.ListHandler<KieContainer>(containersProvider.getList());
        containerList.addColumnSortHandler(sortHandler);

        // Create a Pager to control the table.
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(containerList);

        // Add a selection model so we can select cells.
        final SelectionModel<KieContainer> selectionModel = new MultiSelectionModel<KieContainer>(KEY_PROVIDER);
        containerList.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<KieContainer>createCheckboxManager());

        // Initialize the columns.
        initTableColumns(selectionModel, sortHandler);

        // Add the CellList to the adapter in the database.
        addDataDisplay(containerList);
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    private void loadContainers() {
        showLoadingView();
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        final List<KieContainer> containers = kieClientManager.getContainers();
        show(containers);
    }
    
    public void show() {
        loadContainers();
    }

    public void show(final List<KieContainer> containers) {
        clear();
        showLoadingView();
        
        if (containers == null || containers.isEmpty()) {
            showEmptyView();
        } else {
            for (final KieContainer container : containers) {
                addContainer(container);
            }
            containersPanel.setVisible(true);
        }
        
        hideLoadingView();
    }

    private void showEmptyView() {
        clearView();
        emptyPanel.setVisible(true);
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
    
    /**
     * Add the columns to the table.
     */
    private void initTableColumns(
            final SelectionModel<KieContainer> selectionModel,
            ColumnSortEvent.ListHandler<KieContainer> sortHandler) {
       
        // Container status.
        final ClickableImageResourceCell statusCell = new ClickableImageResourceCell();
        final Column<KieContainer, ImageResource> statusColumn = new Column<KieContainer, ImageResource>(statusCell) {

            @Override
            public ImageResource getValue(final KieContainer container) {
                final boolean isUp = SharedUtils.getContainerStatus(container);
                ImageResource imageResource = null;
                String tooltipText = null;
                if (!isUp) {
                    imageResource = Images.INSTANCE.circleGreyCloseIcon();
                    String statusText = Constants.INSTANCE.containerIsDown();
                    tooltipText = new SafeHtmlBuilder().appendEscaped(statusText).toSafeHtml().asString();
                } else {
                    final KieAppStatus status = container.getAppStatus();
                    imageResource = ClientUtils.getStatusImage(status);
                    String statusText = ClientUtils.getStatusText(status);
                    tooltipText = new SafeHtmlBuilder().appendEscaped(statusText + " (" + Constants.INSTANCE.clickForUpdate() + ")").toSafeHtml().asString();
                }
                statusCell.setTooltip(tooltipText);
                return imageResource;
            }
        };

        statusColumn.setFieldUpdater(new FieldUpdater<KieContainer, ImageResource>() {
            @Override
            public void update(int index, KieContainer object, ImageResource value) {
                updateStatus(object);
            }
        });
        containerList.addColumn(statusColumn, Constants.INSTANCE.containerStatus());
        containerList.setColumnWidth(statusColumn, 2, Style.Unit.PCT);

        // Container type cells.
        final Column<KieContainer, KieContainer> typeColumn = new Column<KieContainer, KieContainer>(new ContainerTypesCell()) {

            @Override
            public KieContainer getValue(KieContainer container) {
                return container;
            }
        };
        containerList.addColumn(typeColumn, Constants.INSTANCE.categories());
        containerList.setColumnWidth(typeColumn, 5, Style.Unit.PCT);

        // Container id.
        final Column<KieContainer, String> idColumn = new Column<KieContainer, String>(
                new EditTextCell()) {
            @Override
            public String getValue(KieContainer object) {
                return object.getTruncId();
            }
        };
        idColumn.setSortable(true);
        sortHandler.setComparator(idColumn, new Comparator<KieContainer>() {
            @Override
            public int compare(KieContainer o1, KieContainer o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        containerList.addColumn(idColumn, Constants.INSTANCE.containerId());
        containerList.setColumnWidth(idColumn, 5, Style.Unit.PCT);

        // Container repository name.
        final Column<KieContainer, String> imageColumn = new Column<KieContainer, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieContainer object) {
                return object.getRepository();
            }
        };
        imageColumn.setSortable(true);
        sortHandler.setComparator(imageColumn, new Comparator<KieContainer>() {
            @Override
            public int compare(KieContainer o1, KieContainer o2) {
                return o1.getRepository().compareTo(o2.getRepository());
            }
        });
        containerList.addColumn(imageColumn, Constants.INSTANCE.containerImage());
        containerList.setColumnWidth(imageColumn, 5, Style.Unit.PCT);

        // Container tag.
        final Column<KieContainer, String> tagColumn = new Column<KieContainer, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieContainer object) {
                return object.getTag();
            }
        };
        tagColumn.setSortable(true);
        sortHandler.setComparator(tagColumn, new Comparator<KieContainer>() {
            @Override
            public int compare(KieContainer o1, KieContainer o2) {
                return o1.getTag().compareTo(o2.getTag());
            }
        });
        containerList.addColumn(tagColumn, Constants.INSTANCE.tag());
        containerList.setColumnWidth(tagColumn, 10, Style.Unit.PCT);
        
        // Container name.
        final Column<KieContainer, String> nameColumn = new Column<KieContainer, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieContainer object) {
                return object.getName();
            }
        };
        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<KieContainer>() {
            @Override
            public int compare(KieContainer o1, KieContainer o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        containerList.addColumn(nameColumn, Constants.INSTANCE.containerName());
        containerList.setColumnWidth(nameColumn, 10, Style.Unit.PCT);

        // Container creation date.
        final Column<KieContainer, String> creationDateColumn = new Column<KieContainer, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieContainer object) {
                return ClientUtils.formatImageDateTag(object.getCreated());
            }
        };
        creationDateColumn.setSortable(false);
        containerList.addColumn(creationDateColumn, Constants.INSTANCE.containerCreationDate());
        containerList.setColumnWidth(creationDateColumn, 5, Style.Unit.PCT);

        // Container user actions - Button cells.
        final Column<KieContainer, String> actionsColumn = new Column<KieContainer, String>(actionsCell) {
            @Override
            public String getValue(KieContainer container) {
                return container.getId();
            }
        };
        containerList.addColumn(actionsColumn, Constants.INSTANCE.actions());
        containerList.setColumnWidth(actionsColumn, 10, Style.Unit.PCT);
        
    }
    
    private void updateStatus(final KieContainer container) {
        showLoadingView();
        if ( !SharedUtils.isKieApp(container) || !SharedUtils.getContainerStatus(container) ) {
            hideLoadingView();
            showPopup(Constants.INSTANCE.notAvailable());
        } else {
            dockerService.updateStatus(container.getId(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(final Throwable caught) {
                    showError(caught);
                }

                @Override
                public void onSuccess(final Void result) {
                    hideLoadingView();
                    showPopup(Constants.INSTANCE.reloadStatusCompleted());
                    fireEvent(new ReloadEvent(container));
                }
            });
        }
    }
    
    private final ContainerActionsCell.ContainersProvider actionContainersProvider = new ContainerActionsCell.ContainersProvider() {
        @Override
        public KieContainer getContainer(final String id) {
            if (containersProvider.getList() != null) {
                for (final KieContainer _c : containersProvider.getList()) {
                    if (_c.getId().equals(id)) return _c;
                }
            }
            return null;
        }
        
    };
    
    private void fireReload(final KieContainer container) {
        fireEvent(new ReloadEvent(container));
    }
    
    private final ContainerActionsCell.DoContainerActionCallback containerActionCallback = new ContainerActionsCell.DoContainerActionCallback() {
        @Override
        public void onStart(final KieContainer container) {
            start(container);
        }

        @Override
        public void onStop(final KieContainer container) {
            stop(container);
        }

        @Override
        public void onRestart(final KieContainer container) {
            restart(container);
        }

        @Override
        public void onRemove(final KieContainer container) {
            remove(container);
        }

        @Override
        public void onViewLogs(final KieContainer container) {
            viewLogs(container);
        }

        @Override
        public void onViewDetails(final KieContainer container) {
            viewDetails(container);
        }

        @Override
        public void onNavigate(final KieContainer container) {
            navigate(container);
        }

        @Override
        public void onDownloadArtifact(KieContainer container) {
            downloadArtifact(container);
        }
    };

    private final ContainerActionsCell actionsCell = new ContainerActionsCell(actionContainersProvider, containerActionCallback);

    private void downloadArtifact(final KieContainer container) {

        // Obtain current settings from client cache.
        final Settings settings = SettingsClientHolder.getInstance().getSettings();
        final String downloadURL = ClientUtils.getDownloadURL(settings, container.getType(), 
                container.getSubTypes() != null && !container.getSubTypes().isEmpty() ? container.getSubTypes().get(0) : null,
                container.getTag());
        GWT.log("Downloading artifact using URL = '" + downloadURL + "'");
        Window.open(downloadURL,"_blank","");
    }
    
    private void navigate(final KieContainer container) {

        // Obtain current settings from client cache.
        final Settings settings = SettingsClientHolder.getInstance().getSettings();
        
        // Obtain the URL by inspecting the container.
        dockerService.inspect(container.getId(), new AsyncCallback<org.kie.dockerui.shared.model.KieContainerDetails>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final org.kie.dockerui.shared.model.KieContainerDetails kieContainerDetails) {
                String addr = null;
                try {
                    addr = ClientUtils.getWebAddress(container, kieContainerDetails, settings);
                    Window.open(addr,"_blank","");
                } catch (IllegalStateException e) {
                    showError(Constants.INSTANCE.noPublicPortsAvailable());
                }
            }
        });
        
    }

    private void start(final KieContainer container) {
        showLoadingView();
        dockerService.start(container.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(Void aVoid) {
                showPopup(Constants.INSTANCE.containerStarted() + " " + container.getId());
                fireReload(container);
            }
        });
    }

    private void stop(final KieContainer container) {
        showLoadingView();
        dockerService.stop(container.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(Void aVoid) {
                showPopup(Constants.INSTANCE.containerStopped() + " " + container.getId());
                fireReload(container);
            }
        });
    }

    private void restart(final KieContainer container) {
        showLoadingView();
        dockerService.restart(container.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(Void aVoid) {
                showPopup(Constants.INSTANCE.containerRestarted() + " " + container.getId());
                fireReload(container);
            }
        });
    }

    private void remove(final KieContainer container) {
        showLoadingView();
        dockerService.remove(container.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(Void aVoid) {
                showPopup(Constants.INSTANCE.containerRemoved() + " " + container.getId());
                containersProvider.getList().remove(container);
                fireReload(container);
            }
        });
    }

    private void viewDetails(final KieContainer container) {
        fireEvent(new ShowContainerDetailsEvent(container));
    }
    
    private void viewLogs(final KieContainer container) {
        fireEvent(new ShowContainerLogsEvent(container));
    }

    private void showPopup(final String message) {
        Window.alert(message);
    }

    private void showError(final Throwable throwable) {
        showError("ERROR on KieContainersExplorer. Exception: " + throwable.getMessage());
    }
    
    private void showError(final String message) {
        hideLoadingView();
        Log.log(message);
    }
    
    private void addDataDisplay(HasData<KieContainer> display) {
        containersProvider.addDataDisplay(display);
    }

    private void clearView() {
        loadingPanel.setVisible(false);
        emptyPanel.setVisible(false);
        containersPanel.setVisible(false);
    }
    
    public void clear() {
        clearView();
        containersProvider.getList().clear();
    }

    // ****************************************************
    //                  SHOW LOG FOR CONTAINER EVENT
    // ****************************************************

    public interface ShowContainerLogsEventHandler extends EventHandler
    {
        void onShowContainerLogs(ShowContainerLogsEvent event);
    }

    public static class ShowContainerLogsEvent extends GwtEvent<ShowContainerLogsEventHandler> {

        public static Type<ShowContainerLogsEventHandler> TYPE = new Type<ShowContainerLogsEventHandler>();

        private KieContainer container;

        public ShowContainerLogsEvent(KieContainer container) {
            super();
            this.container = container;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ShowContainerLogsEventHandler handler) {
            handler.onShowContainerLogs(this);
        }

        public KieContainer getContainer() {
            return container;
        }
    }

    public HandlerRegistration addShowContainerLogsEventHandler(final ShowContainerLogsEventHandler handler) {
        return addHandler(handler, ShowContainerLogsEvent.TYPE);
    }

    // ****************************************************
    //                  SHOW DETAILS FOR CONTAINER EVENT
    // ****************************************************

    public interface ShowContainerDetailsEventHandler extends EventHandler
    {
        void onShowContainerDetails(ShowContainerDetailsEvent event);
    }

    public static class ShowContainerDetailsEvent extends GwtEvent<ShowContainerDetailsEventHandler> {

        public static Type<ShowContainerDetailsEventHandler> TYPE = new Type<ShowContainerDetailsEventHandler>();

        private KieContainer container;

        public ShowContainerDetailsEvent(KieContainer container) {
            super();
            this.container = container;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ShowContainerDetailsEventHandler handler) {
            handler.onShowContainerDetails(this);
        }

        public KieContainer getContainer() {
            return container;
        }
    }

    public HandlerRegistration addShowContainerDetailsEventHandler(final ShowContainerDetailsEventHandler handler) {
        return addHandler(handler, ShowContainerDetailsEvent.TYPE);
    }

    // ****************************************************
    //                  RELOAD REQUEST EVENT
    // ****************************************************

    public interface ReloadEventHandler extends EventHandler
    {
        void onReload(ReloadEvent event);
    }

    public static class ReloadEvent extends GwtEvent<ReloadEventHandler> {

        public static Type<ReloadEventHandler> TYPE = new Type<ReloadEventHandler>();

        private KieContainer container;

        public ReloadEvent(final KieContainer container) {
            super();
            this.container = container;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ReloadEventHandler handler) {
            handler.onReload(this);
        }

        public KieContainer getContainer() {
            return container;
        }
    }

    public HandlerRegistration addReloadEventHandler(final ReloadEventHandler handler) {
        return addHandler(handler, ReloadEvent.TYPE);
    }
}
