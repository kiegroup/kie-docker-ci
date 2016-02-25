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
package org.kie.dockerui.client.widgets.container.navigator;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collection;

public class NavigationBreadCrumbView extends Composite {

    interface NavigationBreadCrumbViewBinder extends UiBinder<Widget, NavigationBreadCrumbView> {}
    private static NavigationBreadCrumbViewBinder uiBinder = GWT.create(NavigationBreadCrumbViewBinder.class);

    @UiField
    Breadcrumbs breadcrumb;
    
    @UiConstructor
    public NavigationBreadCrumbView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void show(final Collection<NavigationWorkflowStep> steps, final NavigationContext context) {
        clear();

        int x = 0;
        for (final NavigationWorkflowStep step : steps) {
            final int c = x;
            final NavLink navLink = new NavLink(step.getTitle(context));
            navLink.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent clickEvent) {
                    NavigationBreadCrumbView.this.fireEvent(new NavigationBreadCrumbEvent(c));
                }
            });
            breadcrumb.add(navLink);
            x++;
        }
    }
    
    public void clear() {
        breadcrumb.clear();
    }

    // ****************************************************
    //                  NAVIGATE EVENT
    // ****************************************************

    public interface NavigationBreadCrumbEventHandler extends EventHandler
    {
        void onNavigateTo(NavigationBreadCrumbEvent event);
    }

    public static class NavigationBreadCrumbEvent extends GwtEvent<NavigationBreadCrumbEventHandler> {

        public static Type<NavigationBreadCrumbEventHandler> TYPE = new Type<NavigationBreadCrumbEventHandler>();

        private int index;

        public NavigationBreadCrumbEvent(int index) {
            this.index = index;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(NavigationBreadCrumbEventHandler handler) {
            handler.onNavigateTo(this);
        }

        public int getIndex() {
            return index;
        }
    }

    public HandlerRegistration addNavigationBreadCrumbEventHandler(final NavigationBreadCrumbEventHandler handler) {
        return addHandler(handler, NavigationBreadCrumbEvent.TYPE);
    }

}
