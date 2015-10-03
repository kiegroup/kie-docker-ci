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
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
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

public class KieLogsActions extends Composite {

    private final static String BUTTON_SIZE = "32px";
    
    interface KieLogsActionsBinder extends UiBinder<Widget, KieLogsActions> {}
    private static KieLogsActionsBinder uiBinder = GWT.create(KieLogsActionsBinder.class);

    public enum LogAction {
        REFRESH, PLAY, STOP
    }
    
    @UiField
    Image refreshButton;

    @UiField
    Image playButton;

    @UiField
    Image stopButton;

    private String buttonSize = BUTTON_SIZE;
    
    private final ClickHandler refreshClickHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
            fireEvent(new ContainerLogActionEvent(LogAction.REFRESH));
        }
    };
    
    private final ClickHandler playClickHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
            fireEvent(new ContainerLogActionEvent(LogAction.PLAY));
        }
    };

    private final ClickHandler stopClickHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
            fireEvent(new ContainerLogActionEvent(LogAction.STOP));
        }
    };
    
    @UiConstructor
    public KieLogsActions() {
        initWidget(uiBinder.createAndBindUi(this));
        refreshButton.setUrl(Images.INSTANCE.reloadIconBlue().getSafeUri());
        refreshButton.addClickHandler(refreshClickHandler);
        playButton.setUrl(Images.INSTANCE.playIconData().getSafeUri());
        playButton.addClickHandler(playClickHandler);
        stopButton.setUrl(Images.INSTANCE.stopIconData().getSafeUri());
        stopButton.addClickHandler(stopClickHandler);
        updateButtonsSize();
    }
    
    public void enableAction(final LogAction action) {
        Image button = getImageForAction(action); 
        applyAlpha(button, 1d);
    }

    public void disableAction(final LogAction action) {
        Image button = getImageForAction(action);
        applyAlpha(button, 0.2d);
    }

    public void setButtonSize(String buttonSize) {
        this.buttonSize = buttonSize;
        updateButtonsSize();
    }
    
    private void updateButtonsSize() {
        refreshButton.setSize(buttonSize, buttonSize);
        playButton.setSize(buttonSize, buttonSize);
        stopButton.setSize(buttonSize, buttonSize);
    }

    private void applyAlpha(final Image image, final double alpha) {
        image.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
    }
    
    private Image getImageForAction(final LogAction action) {
        return LogAction.REFRESH.equals(action) ? refreshButton : ( LogAction.PLAY.equals(action) ? playButton : stopButton);
    }
    
    
    public interface ContainerLogActionEventHandler extends EventHandler
    {
        void onLogAction(ContainerLogActionEvent event);
    }

    public static class ContainerLogActionEvent extends GwtEvent<ContainerLogActionEventHandler> {

        public static Type<ContainerLogActionEventHandler> TYPE = new Type<ContainerLogActionEventHandler>();

        private LogAction action;

        public ContainerLogActionEvent(final LogAction action) {
            super();
            this.action = action;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ContainerLogActionEventHandler handler) {
            handler.onLogAction(this);
        }

        public LogAction getAction() {
            return action;
        }
    }

    public HandlerRegistration addContainerLogActionEventHandler(final ContainerLogActionEventHandler handler) {
        return addHandler(handler, ContainerLogActionEvent.TYPE);
    }
    
}
