package org.kie.dockerui.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.dockerui.client.KieClientManager;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.client.widgets.container.KieContainerDetails;
import org.kie.dockerui.client.widgets.container.KieContainerLogs;
import org.kie.dockerui.client.widgets.container.KieContainerStart;
import org.kie.dockerui.client.widgets.container.explorer.KieContainersExplorer;
import org.kie.dockerui.client.widgets.container.navigator.KieContainersNavigator;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieContainerStartArguments;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.List;

public class ContainersView extends Composite {

    interface ContainersViewBinder extends UiBinder<Widget, ContainersView> {}
    private static ContainersViewBinder uiBinder = GWT.create(ContainersViewBinder.class);

    interface ContainersViewStyle extends CssResource {
        String mainPanel();
        String viewModeButtonsPanel();
        String mainPanels();
        String detailsWidget();
        String logsPanel();
        String logsWidget();
        String startContainerPanel();
        String kieContainersNavigatorPanel();
    }

    @UiField
    ContainersViewStyle style;

    @UiField
    FlowPanel mainPanel;

    @UiField
    TimeoutPopupPanel loadingPanel;
    
    @UiField
    FlowPanel viewModeButtonsPanel;

    @UiField
    Button backButton;

    @UiField
    Button refreshButton;

    @UiField
    FlowPanel mainPanels;
    
    @UiField
    FlowPanel containersPanel;
    
    @UiField
    KieContainersNavigator kieContainersNavigator;
    
    @UiField
    KieContainersExplorer kieContainersExplorer;

    @UiField
    FlowPanel logsPanel;
    
    @UiField
    KieContainerLogs logsWidget;
    
    @UiField
    FlowPanel startContainerPanel;
    
    @UiField
    KieContainerStart startContainerWidget;
    
    @UiField
    FlowPanel kieContainersListPanel;

    final KieContainerDetails detailsWidget;
    
    @UiConstructor
    public ContainersView() {
        initWidget(uiBinder.createAndBindUi(this));

        // Create the widget that will be displayed using a popup panel.
        detailsWidget = new KieContainerDetails();
        detailsWidget.addStyleName(style.detailsWidget());
        
        // Configure view mode buttons.
        backButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                goBack();
            }
        });
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent clickEvent) {
                refresh();
            }
        });

        // Catch children widgets events.
        kieContainersNavigator.addShowContainerEventHandler(new KieContainersNavigator.ShowContainersEventHandler() {
            @Override
            public void onShowContainers(KieContainersNavigator.ShowContainersEvent event) {
                final boolean isAll = event.isAll();
                final List<KieContainer> containers = event.getContainers();
                
                if (!isAll && ( containers == null || containers.isEmpty()) ) {
                    hideExplorer();
                } else {
                    filterExplorer(isAll, containers);
                }
            }
        });
        kieContainersNavigator.addCreateContainerEventHandler(new KieContainersNavigator.CreateContainerEventHandler() {
            @Override
            public void onCreateContainer(KieContainersNavigator.CreateContainerEvent event) {
                showStartContainer(event.getImage(), event.getTag(), event.getDbImageType());
            }
        });
        kieContainersExplorer.addShowContainerLogsEventHandler(new KieContainersExplorer.ShowContainerLogsEventHandler() {
            @Override
            public void onShowContainerLogs(final KieContainersExplorer.ShowContainerLogsEvent event) {
                showLogs(event.getContainer());
            }
        });
        kieContainersExplorer.addShowContainerDetailsEventHandler(new KieContainersExplorer.ShowContainerDetailsEventHandler() {
            @Override
            public void onShowContainerDetails(final KieContainersExplorer.ShowContainerDetailsEvent event) {
                showDetails(event.getContainer());
            }
        });
        kieContainersExplorer.addReloadEventHandler(new KieContainersExplorer.ReloadEventHandler() {
            @Override
            public void onReload(KieContainersExplorer.ReloadEvent event) {
                refresh();
            }
        });
        startContainerWidget.addContainerStartedEventHandler(new KieContainerStart.ContainerStartedEventHandler() {
            @Override
            public void onContainerStarted(KieContainerStart.ContainerStartedEvent event) {
                refresh();
            }
        });

    }
    
    private void refresh() {
        showLoadingView();
        KieClientManager.getInstance().reload(new KieClientManager.KieClientManagerCallback() {
            @Override
            public void onFailure(final Throwable caught) {
                showError(caught);
            }

            @Override
            public void onSuccess() {
                hideLoadingView();
                show();
            }
        });
    }
    
    public void show() {
        clear();
        kieContainersNavigator.show();
        kieContainersExplorer.show();
        logsPanel.setVisible(false);
        startContainerPanel.setVisible(false);
        containersPanel.setVisible(true);
    }

    public void show(final List<KieContainer> containers) {
        clear();
        kieContainersNavigator.show();
        kieContainersExplorer.show(containers);
        logsPanel.setVisible(false);
        startContainerPanel.setVisible(false);
        containersPanel.setVisible(true);
    }

    private void goBack() {
        logsWidget.clear();
        backButton.setVisible(false);
        logsPanel.setVisible(false);
        startContainerPanel.setVisible(false);
        containersPanel.setVisible(true);
    }

    private void filterExplorer(final boolean isAll, final List<KieContainer> containers) {
        if (!isAll) kieContainersExplorer.show(containers);
        else kieContainersExplorer.show();
        showExplorer();
    }
    
    private void hideExplorer() {
        kieContainersExplorer.setVisible(false);
    }

    private void showExplorer() {
        kieContainersExplorer.setVisible(true);
    }

    private void showLogs(final KieContainer container) {
        startContainerPanel.setVisible(false);
        containersPanel.setVisible(false);
        logsPanel.setVisible(true);
        // By default, do not refresh logs.
        logsWidget.show(container.getId(), false);
        backButton.setVisible(true);
    }

    private void showDetails(final KieContainer container) {
        final PopupPanel detailsPopup = new PopupPanel();
        detailsPopup.setTitle(Constants.INSTANCE.detailsForContainer() + " " + container.getId());
        detailsPopup.setAutoHideEnabled(true);
        detailsPopup.setModal(true);
        detailsPopup.setGlassEnabled(true);
        detailsPopup.add(detailsWidget);
        detailsPopup.center();
        detailsPopup.show();
        detailsWidget.show(container);
    }

    private void showStartContainer(final KieImage image, final String tag, final KieImageType dbImageType) {
        final String imageName = SharedUtils.getImage(image.getRegistry(), image.getRepository(), tag);
        String imageKieName = image.getRepository();
        final int _slashPos = imageKieName.lastIndexOf("/");
        if (_slashPos > -1) {
            imageKieName = imageKieName.substring(_slashPos + 1, imageKieName.length());
        }
        imageKieName = imageKieName + "-" + tag;
        final String containerName = dbImageType != null ? imageKieName + "-" + dbImageType.getId() : imageKieName;
        String dbContainerImage = null;
        String dbContainerName = null;
        if(dbImageType != null) {
            dbContainerImage = ClientUtils.getDbmsImageName(dbImageType, SettingsClientHolder.getInstance().getSettings());
            dbContainerName = dbImageType.getId() + "-" + imageKieName;
        }
        final KieContainerStartArguments arguments = new KieContainerStartArguments(image.getType(), imageName,
                containerName, dbImageType, dbContainerImage, dbContainerName, null);
        startContainerPanel.setTitle(Constants.INSTANCE.createAndStartKieContainer() + " " + containerName);
        logsPanel.setVisible(false);
        containersPanel.setVisible(false);
        startContainerPanel.setVisible(true);
        backButton.setVisible(true);
        startContainerPanel.setVisible(true);
        startContainerWidget.show(arguments);
    }
    
    public void clear() {
        hideLoadingView();
        kieContainersNavigator.clear();
        kieContainersExplorer.clear();
        showExplorer();
        backButton.setVisible(false);
        startContainerWidget.clear();
        startContainerPanel.setVisible(false);
        logsPanel.setVisible(false);
        startContainerPanel.setVisible(false);
        containersPanel.setVisible(true);
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
        showError("ERROR on ContainersView. Exception: " + throwable.getMessage());
    }

    private void showError(final String message) {
        Log.log(message);
    }

}
