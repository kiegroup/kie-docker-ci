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
package org.kie.dockerui.client.widgets.container.navigator.item.view;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarModel;
import org.kie.dockerui.client.resources.bundles.Images;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.widgets.container.navigator.item.DateNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEvent;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEventHandler;

public class NavigationDateItemView extends Composite {

    private final int ICON_SIZE = 150;
    private final CalendarModel calendarModel = new CalendarModel();
    interface NavigationDateItemViewBinder extends UiBinder<Widget, NavigationDateItemView> {}
    private static NavigationDateItemViewBinder uiBinder = GWT.create(NavigationDateItemViewBinder.class);
    
    @UiField
    FocusPanel mainPanel;
    
    @UiField
    Heading month;

    @UiField
    HTML countText;
    
    @UiField
    Image image;

    @UiField
    Heading day;

    private String emptyText = "0";
    private String emptyTextTitle = Constants.INSTANCE.runningContainersCount();
    
    @UiConstructor
    public NavigationDateItemView() {
        initWidget(uiBinder.createAndBindUi(this));
        mainPanel.setVisible(false);
        image.setUrl(Images.INSTANCE.calendarEmptyIcon().getSafeUri());
        image.setSize(ICON_SIZE + "px", ICON_SIZE + "px");
    }

    public void setEmptyText(final String text, final String title) {
        this.emptyText = text;
        this.emptyTextTitle = title;
    }
    
    public void show(final DateNavigationItem dateNavigationItem) {
        final String id = dateNavigationItem.getId();
        final String _title = dateNavigationItem.getTitle();
        final int  cCount = dateNavigationItem.getContainersCount();
        final int _day = dateNavigationItem.getDay();
        final int _month = dateNavigationItem.getMonth();
        show(id, _day, _month, cCount);
        mainPanel.setVisible(true);
    }

    private void show(final String id, final int _day, final int _month, final int containersRunningCount) {
        showRunningContainers(id, containersRunningCount);
        day.setText(Integer.toString(_day));
        final String _m = calendarModel.formatMonth(_month - 1);
        month.setText(_m);

        // Positioning.
        positionItems();
        
        // Add the click handler.
        addItemDetailClickHandler(id);
    }
    
    private void positionItems() {
        
        // Day text.
        final Style dayStyle = day.getElement().getStyle();
        dayStyle.setPosition(Style.Position.ABSOLUTE);
        dayStyle.setMarginTop( (ICON_SIZE / 2) * -1, Style.Unit.PX);
        dayStyle.setMarginLeft( ( ICON_SIZE / 2 ) + 10, Style.Unit.PX);

        // Month text.
        final Style monthStyle = month.getElement().getStyle();
        monthStyle.setPosition(Style.Position.ABSOLUTE);
        monthStyle.setMarginTop(25, Style.Unit.PX);
        monthStyle.setMarginLeft( ( ICON_SIZE / 2 ) + (month.getOffsetWidth() / 2), Style.Unit.PX);

        // Count number.
        final Style countStyle = countText.getElement().getStyle();
        countStyle.setPosition(Style.Position.ABSOLUTE);
        countStyle.setMarginTop(20, Style.Unit.PX);
        countStyle.setMarginLeft( ( ICON_SIZE - 20 ), Style.Unit.PX);
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


    private void addItemDetailClickHandler(final String id) {
        mainPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                NavigationDateItemView.this.fireEvent(new NavigationItemSelectedEvent(id));
            }
        });
    }

    public HandlerRegistration addNavigationItemSelectedEventHandler(final NavigationItemSelectedEventHandler handler) {
        return addHandler(handler, NavigationItemSelectedEvent.TYPE);
    }
    
}
