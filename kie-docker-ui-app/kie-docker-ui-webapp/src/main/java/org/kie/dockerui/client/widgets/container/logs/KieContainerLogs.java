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
package org.kie.dockerui.client.widgets.container.logs;

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.util.LogHtmlBuilder;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.shared.model.KieContainer;

public class KieContainerLogs extends Composite {

    private final static String BUTTON_SIZE = "32px";
    public final static int REFRESH_TIME = 10000;
    
    interface KieContainerLogsBinder extends UiBinder<Widget, KieContainerLogs> {}
    private static KieContainerLogsBinder uiBinder = GWT.create(KieContainerLogsBinder.class);
    
    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    FlowPanel timeOutPopupCounterPanel;
    
    @UiField
    HTML timeOutPopupCounterText;
    
    @UiField
    KieLogsActions popupLogActions;
    
    @UiField
    AccordionGroup infoAccordionGroup;
    
    @UiField
    HTML infoContainerId;
    
    @UiField
    HTML infoContainerImage;
    
    @UiField
    HTML infoContainerName;
    
    @UiField
    KieLogsActions topLogActions;
    
    @UiField
    ScrollPanel logsPanel;

    @UiField
    HTML logText;

    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);

    private Timer timer = null;
    private int currentTimerSec = 0;
    private HandlerRegistration windowScrollHandlerRegistration;
    private String id = null;
    private int refreshMillis = REFRESH_TIME;
    
    private final KieLogsActions.ContainerLogActionEventHandler logActionEventHandler = new KieLogsActions.ContainerLogActionEventHandler() {
        @Override
        public void onLogAction(KieLogsActions.ContainerLogActionEvent event) {
            if (KieLogsActions.LogAction.REFRESH.equals(event.getAction())) {
                currentTimerSec = 0;
                doShow();
            } else if (KieLogsActions.LogAction.PLAY.equals(event.getAction())) {
                play();
            } else if (KieLogsActions.LogAction.STOP.equals(event.getAction())) {
                stop();
            }
        }
    };
    
    @UiConstructor
    public KieContainerLogs() {
        initWidget(uiBinder.createAndBindUi(this));
        topLogActions.addContainerLogActionEventHandler(logActionEventHandler);
        popupLogActions.addContainerLogActionEventHandler(logActionEventHandler);
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
                if (container == null) {
                    showError(Constants.INSTANCE.notAvailable());
                }
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
                currentTimerSec++;
                if ( (currentTimerSec * 1000) > refreshMillis) {
                    doShow();
                    currentTimerSec = 0;                    
                }
                updateTimerMillis();
                timer.schedule(1000);
            }
        };

        updateTimerMillis();
        disableAction(KieLogsActions.LogAction.PLAY);
        enableAction(KieLogsActions.LogAction.STOP);
        timer.schedule(1000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            currentTimerSec = 0;
        }
        
        disableTimerMillis();
        enableAction(KieLogsActions.LogAction.PLAY);
        disableAction(KieLogsActions.LogAction.STOP);
    }
    
    private void disableAction(KieLogsActions.LogAction action) {
        topLogActions.disableAction(action);
        popupLogActions.disableAction(action);
    }

    private void enableAction(KieLogsActions.LogAction action) {
        topLogActions.enableAction(action);
        popupLogActions.enableAction(action);
    }
    
    private void updateTimerMillis() {
        final int secRemaining = (refreshMillis / 1000) - currentTimerSec;
        timeOutPopupCounterText.setText(Constants.INSTANCE.nextRefresh() + " " + secRemaining + "s");
        timeOutPopupCounterPanel.setVisible(true);

        if (windowScrollHandlerRegistration == null) {
            windowScrollHandlerRegistration  = Window.addWindowScrollHandler(windowScrollHandler);
            updateTimerMillisPosition(Window.getScrollTop());
        }
    }

    private final Window.ScrollHandler windowScrollHandler = new Window.ScrollHandler() {
        @Override
        public void onWindowScroll(Window.ScrollEvent event) {
            final int scrollTop = event.getScrollTop();
            updateTimerMillisPosition(scrollTop);
        }
    };
    
    private void updateTimerMillisPosition(final int scrollTop) {
        final int wWidth = Window.getClientWidth();
        final int pWidth = timeOutPopupCounterPanel.getOffsetWidth() + 50;
        timeOutPopupCounterPanel.getElement().getStyle().setTop(scrollTop, Style.Unit.PX);
        timeOutPopupCounterPanel.getElement().getStyle().setLeft(wWidth - pWidth, Style.Unit.PX);
    }
    
    private void disableTimerMillis() {
        if (windowScrollHandlerRegistration != null) {
            windowScrollHandlerRegistration.removeHandler();
        }
        timeOutPopupCounterText.setText("");
        timeOutPopupCounterPanel.setVisible(false);
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
