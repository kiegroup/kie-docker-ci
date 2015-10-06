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

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Heading;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.widgets.container.navigator.item.CompositeNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.DateNavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.NavigationItem;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEvent;
import org.kie.dockerui.client.widgets.container.navigator.item.view.event.NavigationItemSelectedEventHandler;

import java.util.List;

public class CompositeNavigationItemView extends Composite {

    private static final int COLUMNS_BY_ROW = 6;
    interface CompositeNavigationItemViewBinder extends UiBinder<Widget, CompositeNavigationItemView> {}
    private static CompositeNavigationItemViewBinder uiBinder = GWT.create(CompositeNavigationItemViewBinder.class);

    

    @UiField
    AccordionGroup accordionGroup;
    
    @UiField
    Heading title;
    
    @UiConstructor
    public CompositeNavigationItemView() {
        initWidget(uiBinder.createAndBindUi(this));
        accordionGroup.getWidget(0).getElement().getStyle().setBackgroundColor("whitesmoke");
    }

    public void show(final CompositeNavigationItem navigationItem) {
        final String _title = navigationItem.getTitle();
        final List<NavigationItem> items = navigationItem.getItems();
        configureHeader(_title);
        if (items != null && !items.isEmpty()) {
            final int itemsCount = items.size();
            int colCount = COLUMNS_BY_ROW;
            int rowCount = 0;
            FluidRow row = new FluidRow();
            for (final NavigationItem item : items) {
                int offset = -1;
                if (colCount == COLUMNS_BY_ROW) {
                    row = new FluidRow();
                    accordionGroup.add(row);
                    colCount = 0;
                    
                    // Pre-calculate the first column offset in order to align items at row's center position.
                    offset = itemsCount - ( rowCount * COLUMNS_BY_ROW );
                    if (offset > 0 && offset < COLUMNS_BY_ROW) {
                        offset = ( COLUMNS_BY_ROW - offset ) / 2;
                    } else {
                        offset = -1;
                    }
                    rowCount++;
                }
                
                final Column column = new Column(2, offset > -1 ? offset * ( 12 / COLUMNS_BY_ROW) : 0);
                column.getElement().getStyle().setProperty("minWidth","150px");
                showItemView(column, item);
                row.add(column);
                colCount++;
            }
        } else {
            showEmptyView();
        }
    }

    private void showEmptyView() {
        accordionGroup.add(new Heading(5, Constants.INSTANCE.noData()))    ;    
    }

    private void configureHeader(final String _title) {
        title.setText(_title);
    }

    private void showItemView(final Panel parent, final NavigationItem item) {
        final Widget itemView = NavigationItemViewBuilder.build(item, true, navigationItemSelectedEventHandler);
        parent.add(itemView);
    }

    public HandlerRegistration addNavigationItemSelectedEventHandler(final NavigationItemSelectedEventHandler handler) {
        return addHandler(handler, NavigationItemSelectedEvent.TYPE);
    }

    private final NavigationItemSelectedEventHandler navigationItemSelectedEventHandler = new NavigationItemSelectedEventHandler() {
        @Override
        public void onNavigationItemSelected(NavigationItemSelectedEvent event) {
            CompositeNavigationItemView.this.fireEvent(event);
        }
    };
    
}
