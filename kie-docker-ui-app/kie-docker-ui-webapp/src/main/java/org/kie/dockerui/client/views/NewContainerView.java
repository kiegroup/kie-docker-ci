package org.kie.dockerui.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.dockerui.client.widgets.KieContainerCreateWizard;
import org.kie.dockerui.client.widgets.KieContainerStart;

public class NewContainerView extends Composite {

    interface NewContainerViewBinder extends UiBinder<Widget, NewContainerView> {}
    private static NewContainerViewBinder uiBinder = GWT.create(NewContainerViewBinder.class);

    interface NewContainerViewStyle extends CssResource {
        String mainPanel();
    }

    @UiField
    FlowPanel mainPanel;
    
    @UiField
    KieContainerCreateWizard createContainerWidget;
    
    @UiField
    KieContainerStart startContainerWidget;

    @UiConstructor
    public NewContainerView() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void show() {
        createContainerWidget.show();
        createContainerWidget.addContainerCreatedEventHandler(containerCreatedEventHandler);
        createContainerWidget.setVisible(true);
        startContainerWidget.setVisible(false);
    }
    
    private final KieContainerCreateWizard.ContainerCreatedEventHandler containerCreatedEventHandler = new KieContainerCreateWizard.ContainerCreatedEventHandler() {
        @Override
        public void onContainerCreated(KieContainerCreateWizard.ContainerCreatedEvent event) {
            createContainerWidget.setVisible(false);
            startContainerWidget.addContainerStartedEventHandler(new KieContainerStart.ContainerStartedEventHandler() {
                @Override
                public void onContainerStarted(KieContainerStart.ContainerStartedEvent event) {
                    // GO home.
                    show();
                }
            });
            startContainerWidget.show(event.getArguments());
            startContainerWidget.setVisible(true);
        }
    };

    public HandlerRegistration addContainerStartedEventHandler(final KieContainerStart.ContainerStartedEventHandler handler) {
        return startContainerWidget.addContainerStartedEventHandler(handler);
    }

    public void clear() {
        createContainerWidget.clear();
        createContainerWidget.clear();
        startContainerWidget.clear();
    }

}
