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

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.util.LogHtmlBuilder;
import org.kie.dockerui.shared.model.KieContainer;

public class KieContainerLogs extends Composite {

    private final static String BUTTON_SIZE = "32px";
    private final static int REFRESH_TIME = 10000;
    
    interface KieContainerLogsBinder extends UiBinder<Widget, KieContainerLogs> {}
    private static KieContainerLogsBinder uiBinder = GWT.create(KieContainerLogsBinder.class);
    
    interface KieContainerLogsStyle extends CssResource {
        String mainPanel();
        String loadingPanel();
        String logsPanel();
        String accordion();
        String refreshPanel();
        String detailsActionsPanel();
        String playButton();
        String stopButton();
        String logText();
    }

    @UiField
    FlowPanel mainPanel;
    
    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    AccordionGroup infoAccordionGroup;
    
    @UiField
    HTML infoContainerId;
    
    @UiField
    HTML infoContainerImage;
    
    @UiField
    HTML infoContainerName;
    
    @UiField
    Image playButton;
    
    @UiField
    Image stopButton;

    @UiField
    ScrollPanel logsPanel;

    @UiField
    HTML logText;

    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);

    private Timer timer = null;
    private String id = null;
    private int refreshMillis = REFRESH_TIME;
    
    private final ClickHandler playClickHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
            play();
        }
    };

    private final ClickHandler stopClickHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
            stop();
        }
    };
    
    @UiConstructor
    public KieContainerLogs() {
        initWidget(uiBinder.createAndBindUi(this));
        playButton.setUrl(Images.INSTANCE.playIconData().getSafeUri());
        playButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
        playButton.addClickHandler(playClickHandler);
        stopButton.setUrl(Images.INSTANCE.stopIconData().getSafeUri());
        stopButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
        stopButton.addClickHandler(stopClickHandler);
    }

    public void show(final String id) {
        show(id, true);
    }
    
    public void show(final String id, final boolean autoRefresh) {
        clear();
        showLoadingView();
        this.id = id;
        
        dockerService.getContainer(id, new AsyncCallback<KieContainer>() {
            @Override
            public void onFailure(final Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final KieContainer container) {
                infoAccordionGroup.setHeading(Constants.INSTANCE.logsForContainer() + " " + id);
                infoAccordionGroup.setVisible(true);
                infoContainerId.setText(id);
                infoContainerImage.setText(container.getImage());
                infoContainerName.setText(container.getName());

                if (autoRefresh && refreshMillis > 0) {
                    play();
                } else {
                    stop();
                }

                doShow();
            }
        });
    }

    private void doShow() {
        dockerService.logs(id, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final String logs) {
                hideLoadingView();
                if (logs != null) {
                    final LogHtmlBuilder logHtmlBuilder = new LogHtmlBuilder();
                    logText.setHTML(logHtmlBuilder.append(logs).toSafeHtml());
                    logText.setVisible(true);
                    logsPanel.scrollToBottom();
                }
            }
        });
    }
    
    public void play() {
        timer = new Timer() {
            @Override
            public void run() {
                doShow();
                timer.schedule(refreshMillis);
            }
        };

        timer.schedule(refreshMillis);
        disableButton(playButton);
        enableButton(stopButton);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        enableButton(playButton);
        disableButton(stopButton);
    }
    
    private void enableButton(final Image button) {
        applyAlpha(button, 1d);
    }

    private void disableButton(final Image button) {
        applyAlpha(button, 0.2d);
    }

    private void applyAlpha(final Image image, final double alpha) {
        image.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
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

    public void setRefreshMillis(int refreshMillis) {
        this.refreshMillis = refreshMillis;
    }

    private void showError(final Throwable throwable) {
        showError("ERROR on KieContainerLogs. Exception: " + throwable.getMessage());
    }
    
    private void showError(final String message) {
        hideLoadingView();
        stop();
        Log.log(message);
    }
    
    private void clearView() {
        loadingPanel.setVisible(false);
        logText.setVisible(false);
        infoAccordionGroup.setVisible(false);
    }
    
    public void clear() {
        clearView();
        infoAccordionGroup.setHeading("");
        stop();
        this.id = null;
    }
}
