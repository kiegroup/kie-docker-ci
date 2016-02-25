package org.kie.dockerui.client;

import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.Tooltip;
import com.github.gwtbootstrap.client.ui.constants.Placement;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.service.SettingsService;
import org.kie.dockerui.client.service.SettingsServiceAsync;
import org.kie.dockerui.client.views.ArtifactsView;
import org.kie.dockerui.client.views.ContainersView;
import org.kie.dockerui.client.views.ImagesView;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.settings.Settings;

import java.util.List;

public class DockerUIEntryPoint implements EntryPoint {

    private ImagesView imagesView = null;
    private ContainersView containersView = null;
    private ArtifactsView artifactsView = null;
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);
    
    private final ImagesView.ShowContainersEventHandler showContainersEventHandler = new ImagesView.ShowContainersEventHandler() {
        @Override
        public void onShowContainers(final ImagesView.ShowContainersEvent event) {
            showContainersView(event.getContainers());   
        }
    };
    
    @Override
    public void onModuleLoad() {
        
        // Build initial image and containers cache.
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        kieClientManager.reload(new KieClientManager.KieClientManagerCallback() {
            @Override
            public void onFailure(Throwable caught) {
                showError("Error when loading images and containers from remote docker API (DockerUIEntryPoint)");
            }

            @Override
            public void onSuccess() {
                // show views.
                initSettingsCache(new Runnable() {
                    @Override
                    public void run() {
                        final Panel mainPanel = createView();
                        RootPanel.get().add(mainPanel);

                        // By default, show containers view.
                        showContainersView();
                        hideLoadingPopup();
                    }
                });
            }
        });
    }
    
    private static final ClickHandler goToJenkinsClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            final SettingsClientHolder settingsClientHolder = SettingsClientHolder.getInstance();
            final String jenkinsJobURL = settingsClientHolder.getSettings().getJenkinsJobURL();
            if (jenkinsJobURL != null) Window.open(jenkinsJobURL, "_blank", "");
        }
    };
    
    private Panel createView() {
        imagesView = new ImagesView();
        imagesView.addShowContainersEventHandler(showContainersEventHandler);
        containersView = new ContainersView();
        artifactsView = new ArtifactsView();

        final VerticalPanel mainPanel = new VerticalPanel();
        setWidthAt100pct(mainPanel.getElement());
        
        // Title.
        final HorizontalPanel  titlePanel = new HorizontalPanel();
        setWidthAt100pct(titlePanel.getElement());
        final com.github.gwtbootstrap.client.ui.Image jenkinsLogo = new Image(Images.INSTANCE.jenkins().getSafeUri());
        jenkinsLogo.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        jenkinsLogo.getElement().getStyle().setMarginBottom(10, Style.Unit.PX);
        jenkinsLogo.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
        jenkinsLogo.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        jenkinsLogo.setSize("20px", "30px");
        jenkinsLogo.addClickHandler(goToJenkinsClickHandler);
        final Tooltip jenkinsTooltip = new Tooltip(Constants.INSTANCE.goToJenkins());
        jenkinsTooltip.setPlacement(Placement.BOTTOM);
        jenkinsTooltip.setWidget(jenkinsLogo);
        final HTML header = new HTML();
        header.setHTML(new SafeHtmlBuilder().appendEscaped(Constants.INSTANCE.dockerContinuousIntegration()).toSafeHtml().asString());
        header.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        header.getElement().getStyle().setFontSize(30, Style.Unit.PX);
        header.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        final HorizontalPanel jenkinsPanel = new HorizontalPanel();
        jenkinsPanel.setWidth("20%");
        jenkinsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        jenkinsPanel.add(jenkinsTooltip);
        jenkinsPanel.add(jenkinsLogo);
        titlePanel.add(jenkinsPanel);
        final HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setWidth("80%");
        headerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        headerPanel.add(header);
        titlePanel.add(headerPanel);
        mainPanel.add(titlePanel);
        
        // Top Menu.
        final MenuBar topMenu = new MenuBar(false);
        topMenu.setAnimationEnabled(true);
        topMenu.addItem(Constants.INSTANCE.containers(), new Command() {
            @Override
            public void execute() {
                showContainersView();
            }
        });
        topMenu.addItem(Constants.INSTANCE.images(), new Command() {
            @Override
            public void execute() {
                showImagesView();
            }
        });
        if (!isEmpty(SettingsClientHolder.getInstance().getSettings().getArtifactsPath())) {
            topMenu.addItem(Constants.INSTANCE.artifacts(), new Command() {
                @Override
                public void execute() {
                    showArtifactsView();
                }
            });
        }
        mainPanel.add(topMenu);
        
        // Widgets area.
        final FlowPanel widgetsPanel = new FlowPanel();
        widgetsPanel.getElement().getStyle().setMarginTop(20, Style.Unit.PX);
        widgetsPanel.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
        widgetsPanel.getElement().getStyle().setMarginRight(20, Style.Unit.PX);
        setWidthAt100pct(widgetsPanel.getElement());
        // widgetsPanel.add(homeView);
        widgetsPanel.add(imagesView);
        widgetsPanel.add(containersView);
        widgetsPanel.add(artifactsView);
        mainPanel.add(widgetsPanel);

        imagesView.setVisible(false);
        containersView.setVisible(false);
        return mainPanel;
    }

    private void showImagesView() {
        hideViews();
        imagesView.setVisible(true);
        imagesView.show();
    }

    private void showImagesView(final List<KieImage> images) {
        hideViews();
        imagesView.setVisible(true);
        imagesView.show(images);
    }

    private void showContainersView() {
        hideViews();
        containersView.setVisible(true);
        containersView.show();
    }

    private void showContainersView(final List<KieContainer> containers) {
        hideViews();
        containersView.setVisible(true);
        containersView.show(containers);
    }

    private void showArtifactsView() {
        hideViews();
        artifactsView.setVisible(true);
        artifactsView.show();
    }

    private void hideViews() {
        imagesView.setVisible(false);
        containersView.setVisible(false);
        artifactsView.setVisible(false);
    }
    
    private void showError(final Throwable throwable) {
        showError("ERROR on KieCalendar. Exception: " + throwable.getMessage());
    }

    private void showError(final String message) {
        Log.log(message);
    }
    
    private void setWidthAt100pct(final Element element) {
        element.getStyle().setWidth(100, Style.Unit.PCT);
    }
    
    private void initSettingsCache(final Runnable callback) {
        settingsService.getSettings(new AsyncCallback<Settings>() {
            @Override
            public void onFailure(Throwable throwable) {
                hideLoadingPopup();
                Log.log("Error getting settings from backend. Message: " + throwable.getMessage());
            }

            @Override
            public void onSuccess(final Settings settings) {
                final SettingsClientHolder settingsClientHolder = SettingsClientHolder.getInstance();
                settingsClientHolder.setSettings(settings);
                callback.run();
            }
        });
    }
    
    // Fade out the "Loading application" pop-up.
    private void hideLoadingPopup() {
        final Element e = RootPanel.get("loading").getElement();

        new Animation() {

            @Override
            protected void onUpdate( double progress ) {
                e.getStyle().setOpacity( 1.0 - progress );
            }

            @Override
            protected void onComplete() {
                e.getStyle().setDisplay(Style.Display.NONE);
            }
        }.run(500);
    }
    
    private static boolean isEmpty(final String s) {
        return s == null || s.trim().length() == 0;
    }
    
}
