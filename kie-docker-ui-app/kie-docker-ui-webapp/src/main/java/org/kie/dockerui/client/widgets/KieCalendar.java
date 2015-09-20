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

import com.bradrydzewski.gwt.calendar.client.*;
import com.bradrydzewski.gwt.calendar.client.event.MouseOverEvent;
import com.bradrydzewski.gwt.calendar.client.event.MouseOverHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.kie.dockerui.client.KieClientManager;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.shared.model.KieAppStatus;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.model.KieImageType;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.Date;
import java.util.List;

public class KieCalendar extends Composite {

    interface KieCalendarBinder extends UiBinder<Widget, KieCalendar> {}
    private static KieCalendarBinder uiBinder = GWT.create(KieCalendarBinder.class);
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    FlowPanel calendarPanel;
    
    private final Calendar calendar = new Calendar();
    final DecoratedPopupPanel appointmentPopup = new DecoratedPopupPanel(true);
    private String width = "1600px";
    private String height = "400px";
    private int days = 7;
    
    @UiConstructor
    public KieCalendar() {
        initWidget(uiBinder.createAndBindUi(this));
        initCalendar();
    }

    private void initCalendar() {
        final CalendarSettings settings = new CalendarSettings();
        settings.setEnableDragDrop(false);
        settings.setEnableDragDropCreation(false);
        settings.setOffsetHourLabels(false);
        // Try to not show hours...we are only interested in whole days.
        settings.setIntervalsPerHour(1);
        settings.setPixelsPerInterval(1);
        final Date startDate = ClientUtils.goBack(new Date(), days);
        calendar.setDate(startDate);
        calendar.setView(CalendarViews.DAY, days);
        calendar.setWidth(width);
        calendar.setHeight(height);
        calendar.setSettings(settings);
        calendar.addMouseOverHandler(appointmentMouseOverHandler);
        appointmentPopup.setAutoHideEnabled(true);

        calendarPanel.add(calendar);
    }

    public HandlerRegistration addOpenHandler(final OpenHandler<Appointment> appointmentOpenHandler) {
        return calendar.addOpenHandler(appointmentOpenHandler);
    }
    
    private final MouseOverHandler<Appointment> appointmentMouseOverHandler = new MouseOverHandler<Appointment>() {
        @Override
        public void onMouseOver(MouseOverEvent<Appointment> event) {
            final Element element = (Element)event.getElement();
            final Appointment appt = event.getTarget();
            final StringBuilder t = new StringBuilder("<p><b>").append(appt.getTitle()).append("</b></p>");
            if (appt.getDescription() != null) {
                t.append(appt.getDescription());
            }
            t.append("<p><i>").append(Constants.INSTANCE.dblClickToExplore()).append("</i></p>");
            appointmentPopup.setWidget(new HTML(t.toString()));

            int left = element.getAbsoluteLeft() + 10;
            int top = element.getAbsoluteTop() + 10;
            appointmentPopup.setPopupPosition(left, top);
            appointmentPopup.show();
        }
    };
    
    public void show() {
        clear();
        showLoadingView();
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        final List<KieImage> result = kieClientManager.getImages();
        hideLoadingView();
        if (result != null && !result.isEmpty()) {
            for (final KieImage image : result) {
                if (SharedUtils.isKieApp(image)) {
                    addAppointment(image);
                }
            }
        } else {
            showPopup(Constants.INSTANCE.noImages());
        }
        calendarPanel.setVisible(true);
    }

    private void addAppointment(final KieImage image) {
        if (image != null) {
            final String i = SharedUtils.getImage(image.getRegistry(), image.getRepository(), image.getTags().iterator().next());

            // Appointment.
            final Appointment appt = new Appointment();
            appt.setId(image.getId());
            appt.setReadOnly(true);
            appt.setAllDay(true);

            // Title.
            final StringBuilder title = new StringBuilder(image.getType().getName());
            final List<KieImageType> subTypes = image.getSubTypes();
            if (subTypes != null && !subTypes.isEmpty()) title.append(" - " + subTypes.get(0).getName());
            title.append(" (").append(image.getTags().iterator().next()).append(")");
            appt.setTitle(title.toString());

            // Status.
            final KieAppStatus status = image.getAppStatus();
            final String statusText = ClientUtils.getStatusText(status);
            String color = "grey";
            if (status != null) {
                AppointmentStyle style = AppointmentStyle.GREY;
                switch (status) {
                    case FAILED:
                        style = AppointmentStyle.RED;
                        color = "red";
                        break;
                    case OK:
                        style = AppointmentStyle.GREEN;
                        color = "green";
                        break;
                }
                appt.setStyle(style);
            }

            // Description.
            final String createdText = DateTimeFormat.getMediumDateTimeFormat().format(image.getCreated());
            final StringBuilder description = new StringBuilder("<p><b>")
                    .append(Constants.INSTANCE.image())
                    .append("</b>: ")
                    .append("<i>")
                    .append(i)
                    .append("</i></p>")
                    .append("<p><b>")
                    .append(Constants.INSTANCE.created())
                    .append("</b>: ")
                    .append("<i>")
                    .append(createdText)
                    .append("</i></p>");
            if (statusText != null)  {
                description.append("<p style=\"color: ").append(color).append("\"><b>")
                        .append(Constants.INSTANCE.status())
                        .append("</b>: ")
                        .append("<i>")
                        .append(statusText)
                        .append("</i></p>");
            }
            appt.setDescription(description.toString());

            // Date.
            final Date created = image.getCreated();
            appt.setStart(created);
            appt.setEnd(created);
            
            // Add the appointment.
            calendar.addAppointment(appt);
        }
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public void setWidth(String width) {
        this.width = width;
        calendar.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        calendar.setHeight(height);
        this.height = height;
    }

    private void showError(final Throwable throwable) {
        showError("ERROR on KieCalendar. Exception: " + throwable.getMessage());
    }

    private void showError(final String message) {
        hideLoadingView();
        Log.log(message);
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

    private void showPopup(final String message) {
        Window.alert(message);
    }

    public void clear() {
        hideLoadingView();
        calendar.clearAppointments();
        calendarPanel.setVisible(false);
    }
}
