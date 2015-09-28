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
package org.kie.dockerui.client.widgets.container.navigator.item;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.resources.i18n.Constants;

public class NavigationItemView extends Composite {

    interface NavigationItemViewBinder extends UiBinder<Widget, NavigationItemView> {}
    private static NavigationItemViewBinder uiBinder = GWT.create(NavigationItemViewBinder.class);
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    VerticalPanel itemPanel;
    
    @UiField
    HTML title;
    
    @UiField
    HTML countText;
    
    @UiField
    FocusPanel itemDetailPanel;
    
    @UiField
    Heading text;
    
    @UiField
    Image image;
    
    private int size = 50;
    private String emptyText = "0";
    private String emptyTextTitle = Constants.INSTANCE.runningContainersCount();

    public static NavigationItemView build(final NavigationItemSelectedEventHandler handler) {
        final NavigationItemView navigationItemView = GWT.create(NavigationItemView.class);
        navigationItemView.setEmptyText(Constants.INSTANCE.newLiteral(), Constants.INSTANCE.createNewForThisType());
        final Style style = navigationItemView.getElement().getStyle();
        style.setWidth(200, Style.Unit.PX);
        style.setHeight(200, Style.Unit.PX);
        navigationItemView.setImageSizePx(150);
        navigationItemView.addNavigationItemSelectedEventHandler(handler);
        return navigationItemView;
    }
    
    @UiConstructor
    public NavigationItemView() {
        initWidget(uiBinder.createAndBindUi(this));
        mainPanel.setVisible(false);
    }
    
    public void setEmptyText(final String text, final String title) {
        this.emptyText = text;
        this.emptyTextTitle = title;
    }

    public void show(final NavigationItem navigationItem) {
        final String id = navigationItem.getId();
        final String _title = navigationItem.getTitle();
        final int  cCount = navigationItem.getContainersCount();
        final String _text = navigationItem.getText();
        final SafeUri imageUri = navigationItem.getImageUri();
        show(id, _title, _text, imageUri, cCount);
        mainPanel.setVisible(true);
    }

    private void show(final String id, final String _title, final String _text, final SafeUri imageUri, final int containersRunningCount) {
        showTitle(_title);
        showRunningContainers(id, containersRunningCount);
        
        // Show the navigation item image.
        final boolean isImage = imageUri != null;
        if (isImage) {
            image.setUrl(imageUri);
            image.setSize(getSizeInPx(), getSizeInPx());
        }
        image.setVisible(isImage);

        // Show the navigation item text.
        final boolean isText = _text != null;
        if (isText) {
            text.setText(new SafeHtmlBuilder().appendEscaped(_text).toSafeHtml().asString());
        }
        
        // Styles for placing the text over the image.
        final Style textStyle = text.getElement().getStyle();
        if (isImage && isText) {
            textStyle.setPosition(Style.Position.ABSOLUTE);
            textStyle.setMarginTop(size / 2, Style.Unit.PX);
            final int tSize = (int) (_text.length() * 5);
            textStyle.setMarginLeft( ( size / 2 ) - tSize, Style.Unit.PX);
        } else if (isText) {
            textStyle.setPosition(Style.Position.RELATIVE);
            textStyle.clearMarginTop();
            textStyle.clearMarginLeft();
        }
        text.setVisible(isText);

        // Add the click handler.
        addItemDetailClickHandler(id);
    }
    
    private String getSizeInPx() {
        return size + "px";
    }

    private void showTitle(final String _title) {
        title.setText(_title);
    }
    
    private void addItemDetailClickHandler(final String id) {
        itemDetailPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                NavigationItemView.this.fireEvent(new NavigationItemSelectedEvent(id));
            }
        });
    }
    private void showRunningContainers(final String id, final int containersRunningCount) {
        
        // Display containers count text, if applies.
        if (containersRunningCount < 0) {
            countText.setVisible(false);
        } else if (containersRunningCount == 0) {
            countText.setText(emptyText);
            countText.setTitle(emptyTextTitle);
            countText.setVisible(true);
        } else {
            countText.setText(Integer.toString(containersRunningCount));
            countText.setTitle(Constants.INSTANCE.runningContainersCount());
            countText.setVisible(true);
        } 
    }
    
    public void setImageSizePx(int size) {
        this.size = size;
    }

    public interface NavigationItemSelectedEventHandler extends EventHandler
    {
        void onNavigationItemSelected(NavigationItemSelectedEvent event);
    }

    public static class NavigationItemSelectedEvent extends GwtEvent<NavigationItemSelectedEventHandler> {

        public static Type<NavigationItemSelectedEventHandler> TYPE = new Type<NavigationItemSelectedEventHandler>();

        private String id;

        public NavigationItemSelectedEvent(final String id) {
            super();
            this.id = id;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(NavigationItemSelectedEventHandler handler) {
            handler.onNavigationItemSelected(this);
        }

        public String getId() {
            return id;
        }
    }

    public HandlerRegistration addNavigationItemSelectedEventHandler(final NavigationItemSelectedEventHandler handler) {
        return addHandler(handler, NavigationItemSelectedEvent.TYPE);
    }
    
}
