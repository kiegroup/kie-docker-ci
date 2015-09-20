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

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.shared.model.KieImageCategory;
import org.kie.dockerui.shared.model.KieImageType;

public class KieImageTypeView extends Composite {

    interface KieImageTypeViewBinder extends UiBinder<Widget, KieImageTypeView> {}
    private static KieImageTypeViewBinder uiBinder = GWT.create(KieImageTypeViewBinder.class);
    
    interface KieImageTypeViewStyle extends CssResource {
        String mainPanel();
        String typeImage();
        String titlePanel();
        String countText();
        String typeNameText();
        String createNewButton();

        String countTextPanel();
    }

    @UiField
    KieImageTypeViewStyle style;
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    VerticalPanel typePanel;
    
    @UiField
    HTML typeNameText;
    
    @UiField
    HTML countText;
    
    @UiField
    Image typeImage;
    
    private String size = "50px";
    private boolean showCreateButton = false;

    @UiConstructor
    public KieImageTypeView() {
        initWidget(uiBinder.createAndBindUi(this));
        mainPanel.setVisible(false);
    }

    public void setShowCreateButton(boolean showCreateButton) {
        this.showCreateButton = showCreateButton;
    }

    public void init(final KieImageType imageType) {
        init(imageType, -1);
    }
    
    public void init(final KieImageType imageType, final int containersRunningCount) {
        if (imageType == null) return;
        
        final SafeUri imageUri = ClientUtils.getImageUri(imageType);
        typeNameText.setText(imageType.getName());
        if (containersRunningCount < 0) {
            countText.setVisible(false);
        } else if (containersRunningCount == 0 && showCreateButton && !KieImageCategory.OTHERS.equals(imageType.getCategory())) {
            countText.setText(Constants.INSTANCE.createNew());
            countText.setTitle(Constants.INSTANCE.createNewForThisType());
            countText.addStyleName(style.createNewButton());
            countText.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    KieImageTypeView.this.fireEvent(new CreateContainerEvent(imageType));
                }
            });
            countText.setVisible(true);
        } else {
            countText.setText(Integer.toString(containersRunningCount));
            countText.removeStyleName(style.createNewButton());
            countText.setVisible(true);
        } 
        
        typeImage.setUrl(imageUri);
        typeImage.setSize(size, size);
        typeImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                KieImageTypeView.this.fireEvent(new ImageTypeSelectedEvent(imageType));
            }
        });
        mainPanel.setVisible(true);
    }
    
    public void setImageSize(String size) {
        this.size = size;
    }

    public interface ImageTypeSelectedEventHandler extends EventHandler
    {
        void onContainerTypeSelected(ImageTypeSelectedEvent event);
    }

    public static class ImageTypeSelectedEvent extends GwtEvent<ImageTypeSelectedEventHandler> {

        public static Type<ImageTypeSelectedEventHandler> TYPE = new Type<ImageTypeSelectedEventHandler>();

        private KieImageType type;

        public ImageTypeSelectedEvent(KieImageType type) {
            super();
            this.type = type;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ImageTypeSelectedEventHandler handler) {
            handler.onContainerTypeSelected(this);
        }

        public KieImageType getType() {
            return type;
        }
    }

    public HandlerRegistration addImageTypeSelectedEventHandler(final ImageTypeSelectedEventHandler handler) {
        return addHandler(handler, ImageTypeSelectedEvent.TYPE);
    }

    public interface CreateContainerEventHandler extends EventHandler
    {
        void onCreateContainer(CreateContainerEvent event);
    }

    public static class CreateContainerEvent extends GwtEvent<CreateContainerEventHandler> {

        public static Type<CreateContainerEventHandler> TYPE = new Type<CreateContainerEventHandler>();

        private KieImageType type;

        public CreateContainerEvent(KieImageType type) {
            super();
            this.type = type;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(CreateContainerEventHandler handler) {
            handler.onCreateContainer(this);
        }

        public KieImageType getType() {
            return type;
        }
    }

    public HandlerRegistration addCreateContainerEventHandler(final CreateContainerEventHandler handler) {
        return addHandler(handler, CreateContainerEvent.TYPE);
    }
}
