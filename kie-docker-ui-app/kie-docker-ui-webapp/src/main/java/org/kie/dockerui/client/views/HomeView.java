package org.kie.dockerui.client.views;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.widgets.KieCalendar;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.shared.model.KieDockerSummary;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.settings.Settings;


public class HomeView extends Composite {

    interface HomeViewBinder extends UiBinder<Widget, HomeView> {}
    private static HomeViewBinder uiBinder = GWT.create(HomeViewBinder.class);

    interface HomeViewStyle extends CssResource {
        String mainPanel();
        String loadingPanel();
        String summaryPanel();
        String calendar();
        String calendarMainPanel();
        String title();
        String totals();
        String totalsContainerPanel();
        String totalsPanel();
    }

    @UiField
    HomeViewStyle style;
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    HTML imagesCountText;
    
    @UiField
    HTML containersCountText;

    @UiField
    HTML kieImagesCountText;

    @UiField
    HTML kieContainersCountText;
    
    @UiField
    FlowPanel calendarPanel;

    private Settings settings = null;
    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);
    private final KieCalendar calendar = new KieCalendar();

    @UiConstructor
    public HomeView() {
        
        initWidget(uiBinder.createAndBindUi(this));

        // Calendar.
        calendar.setWidth("1600px");
        calendar.setHeight("500px");
        calendar.setDays(7);
        calendar.addStyleName(style.calendar());
        calendarPanel.add(calendar);

        // Calendar appointment open handler.
        calendar.addOpenHandler(appointmentOpenHandler);
    }

    private final OpenHandler<Appointment> appointmentOpenHandler = new OpenHandler<Appointment>() {
        @Override
        public void onOpen(final OpenEvent<Appointment> event) {
            showLoadingView();
            final Appointment appt = event.getTarget();
            final String imageId = appt.getId();
            dockerService.getImage(imageId, new AsyncCallback<KieImage>() {
                @Override
                public void onFailure(final Throwable caught) {
                    showError(caught);
                }

                @Override
                public void onSuccess(final KieImage result) {
                    if (result != null) {
                        fireEvent(new ShowImageEvent(result));
                    } else {
                        showError(Constants.INSTANCE.notAvailable());
                    }
                    hideLoadingView();
                }
            });
        }
    };

    public void show() {
        clear();
        showLoadingView();

        if (settings == null) {
            settings = SettingsClientHolder.getInstance().getSettings();
        }
        
        dockerService.summary(new AsyncCallback<KieDockerSummary>() {
            @Override
            public void onFailure(final Throwable caught) {
                showError(caught);
            }

            @Override
            public void onSuccess(final KieDockerSummary result) {
                showSummary(result.getImagesCount(), result.getContainersCount(),
                        result.getKieImagesCount(), result.getKieContainersCount());
                showCalendar();
                mainPanel.setVisible(true);
                hideLoadingView();
            }
        });
        
    }
    
    private void showSummary(final int imagesCount, final int containersCount, final int KieImagesCount, final int kieContainersCount) {
        imagesCountText.setText(Integer.toString(imagesCount));
        containersCountText.setText(Integer.toString(containersCount));
        kieImagesCountText.setText(Integer.toString(KieImagesCount));
        kieContainersCountText.setText(Integer.toString(kieContainersCount));
    }
    
    private void showCalendar() {
        calendar.show();
        calendarPanel.setVisible(true);
    }
    
    public void clear() {
        hideLoadingView();
        imagesCountText.setText("");
        containersCountText.setText("");
        kieImagesCountText.setText("");
        kieContainersCountText.setText("");
        calendarPanel.setVisible(false);
        mainPanel.setVisible(false);
    }

    private void showError(final Throwable throwable) {
        showError("ERROR on HomeView. Exception: " + throwable.getMessage());
    }

    private void showPopup(final String message) {
        Window.alert(message);
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


    // ****************************************************
    //                  SHOW CONTAINERS EVENT
    // ****************************************************

    public interface ShowImageEventHandler extends EventHandler
    {
        void onShowImage(ShowImageEvent event);
    }

    public static class ShowImageEvent extends GwtEvent<ShowImageEventHandler> {

        public static Type<ShowImageEventHandler> TYPE = new Type<ShowImageEventHandler>();

        private KieImage image;

        public ShowImageEvent(KieImage image) {
            this.image = image;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ShowImageEventHandler handler) {
            handler.onShowImage(this);
        }

        public KieImage getImage() {
            return image;
        }
    }

    public HandlerRegistration addShowImageEventHandler(final ShowImageEventHandler handler) {
        return addHandler(handler, ShowImageEvent.TYPE);
    }

}
