package org.kie.dockerui.client.views;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonCell;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import org.kie.dockerui.client.KieClientManager;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.DockerService;
import org.kie.dockerui.client.service.DockerServiceAsync;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.client.widgets.cell.ClickableImageResourceCell;
import org.kie.dockerui.client.widgets.cell.ImageTypesCell;
import org.kie.dockerui.client.widgets.util.KieCalendar;
import org.kie.dockerui.shared.model.KieAppStatus;
import org.kie.dockerui.shared.model.KieContainer;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.settings.Settings;
import org.kie.dockerui.shared.util.SharedUtils;

import java.util.*;


public class ImagesView extends Composite {

    interface ImagesViewBinder extends UiBinder<Widget, ImagesView> {}
    private static ImagesViewBinder uiBinder = GWT.create(ImagesViewBinder.class);

    @UiField
    FlowPanel mainPanel;
    
    @UiField
    Button refreshButton;

    @UiField
    TimeoutPopupPanel loadingPanel;

    @UiField
    TimeoutPopupPanel pullPanel;
    
    @UiField
    HTML pullText;
    
    @UiField
    Button allImagesButton;

    @UiField
    Button calendarButton;
    
    @UiField
    HTMLPanel imagesPanel;

    @UiField(provided = true)
    CellTable imagesGrid;
    
    @UiField(provided = true)
    SimplePager pager;

    @UiField
    FlowPanel calendarPanel;
    
    private final DockerServiceAsync dockerService = GWT.create(DockerService.class);
    private Settings settings = null;
    private final KieCalendar calendar = new KieCalendar();

    /**
     * The provider that holds the list of containers.
     */
    private final ListDataProvider<KieImage> imagesProvider = new ListDataProvider<KieImage>();
    
    @UiConstructor
    public ImagesView() {
        // Init the image list grid.
        initImagesGrid();
                
        initWidget(uiBinder.createAndBindUi(this));
        
        // Calendar.
        calendar.setWidth("1800px");
        calendar.setHeight("900px");
        calendar.setDays(7);
        calendarPanel.add(calendar);
        
        // Buttons handler.
        allImagesButton.addClickHandler(allImagesButtonClickHandler);
        calendarButton.addClickHandler(calendarButtonClickHandler);
        refreshButton.addClickHandler(refreshButtonClickHandler);
        refreshButton.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);
        
        // Calendar appointment open handler.
        calendar.addOpenHandler(appointmentOpenHandler);
    }

    private final ClickHandler allImagesButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            showAllImages();            
        }
    };

    private final ClickHandler calendarButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            showCalendar();
        }
    };
    
    private final ClickHandler refreshButtonClickHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
            showLoadingView();
            KieClientManager.getInstance().reload(new KieClientManager.KieClientManagerCallback() {
                @Override
                public void onFailure(final Throwable caught) {
                    showError(caught);
                }

                @Override
                public void onSuccess() {
                    hideLoadingView();
                    showAllImages();
                }
            });
        }
    };
    
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
                        final List<KieImage> list = new ArrayList<KieImage>(1);
                        list.add(result);
                        imagesProvider.setList(list);
                        redrawTable();
                        showAllImagesPanel();
                    } else {
                        showError(Constants.INSTANCE.notAvailable());
                    }
                    hideLoadingView();
                }
            });
        }
    };

    private static final ProvidesKey<KieImage> KEY_PROVIDER = new ProvidesKey<KieImage>() {
        @Override
        public Object getKey(KieImage item) {
            return item == null ? null : item.getId();
        }
    };

    /**
     * Add a new image into the grid provider's list.
     *
     * @param image the image to add.
     */
    public void addImage(KieImage image) {
        List<KieImage> contacts = imagesProvider.getList();
        // Remove the contact first so we don't add a duplicate.
        contacts.remove(image);
        contacts.add(image);
    }
    
    public void show() {
        clear();
        showCalendar();
        mainPanel.setVisible(true);
    }

    public void show(final List<KieImage> images) {
        clear();
        showLoadingView();

        if (images != null) {
            for (final KieImage kieImage : images) {
                addImage(kieImage);
            }
            redrawTable();
        }

        hideLoadingView();
        showAllImagesPanel();
        mainPanel.setVisible(true);
    }

    private void showAllImages() {
        showLoadingView();

        // All images Images table.
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        final List<KieImage> kieImages = kieClientManager.getImages();
        if (kieImages != null) {
            for (final KieImage kieImage : kieImages) {
                addImage(kieImage);
            }
            redrawTable();
        }

        hideLoadingView();
        showAllImagesPanel();
    }
    
    private void showCalendar() {
        allImagesButton.setActive(false);
        calendarButton.setActive(true);
        imagesPanel.setVisible(false);
        calendarPanel.setVisible(true);
        showCalendarPanel();
        
    }

    private void showAllImagesPanel() {
        allImagesButton.setActive(true);
        calendarButton.setActive(false);
        imagesPanel.setVisible(true);
        calendarPanel.setVisible(false);
    }
    
    private void showCalendarPanel() {
        calendar.show();
    }
    
    private void initImagesGrid() {
        imagesGrid = new CellTable<KieImage>(KEY_PROVIDER);
        imagesGrid.setWidth("100%", true);

        // Do not refresh the headers and footers every time the data is updated.
        imagesGrid.setAutoHeaderRefreshDisabled(true);
        imagesGrid.setAutoFooterRefreshDisabled(true);

        // Attach a column sort handler to the ListDataProvider to sort the list.
        ColumnSortEvent.ListHandler<KieImage> sortHandler = new ColumnSortEvent.ListHandler<KieImage>(imagesProvider.getList());
        imagesGrid.addColumnSortHandler(sortHandler);

        // Create a Pager to control the table.
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(imagesGrid);

        // Add a selection model so we can select cells.
        final SelectionModel<KieImage> selectionModel = new MultiSelectionModel<KieImage>(KEY_PROVIDER);
        imagesGrid.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<KieContainer>createCheckboxManager());

        // Initialize the columns.
        initTableColumns(selectionModel, sortHandler);

        // Add the CellList to the adapter in the database.
        addDataDisplay(imagesGrid);
    }

    private void initTableColumns(SelectionModel<KieImage> selectionModel, ColumnSortEvent.ListHandler<KieImage> sortHandler) {

        // Image status.
        final ClickableImageResourceCell statusCell = new ClickableImageResourceCell();
        final Column<KieImage, ImageResource> statusColumn = new Column<KieImage, ImageResource>(statusCell) {

            @Override
            public ImageResource getValue(final KieImage image) {
                final KieAppStatus status = image.getAppStatus();
                final ImageResource imageResource = ClientUtils.getStatusImage(status);
                final String iconTooltip = ClientUtils.getStatusText(status);
                statusCell.setTooltip(new SafeHtmlBuilder().appendEscaped(iconTooltip).toSafeHtml().asString());
                return imageResource;
            }
        };
        imagesGrid.addColumn(statusColumn, Constants.INSTANCE.containerStatus());
        imagesGrid.setColumnWidth(statusColumn, 2, Style.Unit.PCT);

        // Image id.
        final Column<KieImage, String> idColumn = new Column<KieImage, String>(
                new EditTextCell()) {
            @Override
            public String getValue(KieImage object) {
                return object.getTruncId();
            }
        };
        idColumn.setSortable(true);
        sortHandler.setComparator(idColumn, new Comparator<KieImage>() {
            @Override
            public int compare(KieImage o1, KieImage o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        imagesGrid.addColumn(idColumn, Constants.INSTANCE.imageId());
        imagesGrid.setColumnWidth(idColumn, 5, Style.Unit.PCT);
        
        // Image type cells.
        final Column<KieImage, KieImage> typeColumn = new Column<KieImage, KieImage>(new ImageTypesCell()) {

            @Override
            public KieImage getValue(KieImage container) {
                return container;
            }
        };
        imagesGrid.addColumn(typeColumn, Constants.INSTANCE.categories());
        imagesGrid.setColumnWidth(typeColumn, 5, Style.Unit.PCT);
        
        // Repository.
        final Column<KieImage, String> imageColumn = new Column<KieImage, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieImage object) {
                return object.getRepository();
            }
        };
        imageColumn.setSortable(true);
        sortHandler.setComparator(imageColumn, new Comparator<KieImage>() {
            @Override
            public int compare(KieImage o1, KieImage o2) {
                return o1.getRepository().compareTo(o2.getRepository());
            }
        });
        imagesGrid.addColumn(imageColumn, Constants.INSTANCE.imageRepository());
        imagesGrid.setColumnWidth(imageColumn, 5, Style.Unit.PCT);

        // Tag.
        final Column<KieImage, String> tagColumn = new Column<KieImage, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieImage object) {
                return object.getTags().toString();
            }
        };
        tagColumn.setSortable(false);
        imagesGrid.addColumn(tagColumn, Constants.INSTANCE.tags());
        imagesGrid.setColumnWidth(tagColumn, 10, Style.Unit.PCT);

        // Image creation date.
        final Column<KieImage, String> creationDateColumn = new Column<KieImage, String>(
                new TextCell()) {
            @Override
            public String getValue(final KieImage object) {
                return ClientUtils.formatImageDateTag(object.getCreated());
            }
        };
        creationDateColumn.setSortable(false);
        imagesGrid.addColumn(creationDateColumn, Constants.INSTANCE.imageCreationDate());
        imagesGrid.setColumnWidth(creationDateColumn, 5, Style.Unit.PCT);

        // Download artifact.
        final Column<KieImage, String> artifactDownloadColumn = new Column<KieImage, String>(
                new ButtonCell()) {
            @Override
            public String getValue(final KieImage object) {
                return Constants.INSTANCE.download();
            }
        };
        artifactDownloadColumn.setFieldUpdater(new FieldUpdater<KieImage, String>() {
            @Override
            public void update(final int index, final KieImage image, final String value) {
                downloadArtifact(image);
            }
        });
        artifactDownloadColumn.setSortable(false);
        imagesGrid.addColumn(artifactDownloadColumn, Constants.INSTANCE.downloadWAR());
        imagesGrid.setColumnWidth(artifactDownloadColumn, 2, Style.Unit.PCT);
        
        // Explore containers.
        final Column<KieImage, String> containersColumn = new Column<KieImage, String>(
                new ButtonCell()) {
            @Override
            public String getValue(final KieImage object) {
                return Constants.INSTANCE.view();
            }
        };
        containersColumn.setFieldUpdater(new FieldUpdater<KieImage, String>() {
            @Override
            public void update(final int index, final KieImage image, final String value) {
                showContainers(image);               
            }
        });
        containersColumn.setSortable(false);
        imagesGrid.addColumn(containersColumn, Constants.INSTANCE.containers());
        imagesGrid.setColumnWidth(containersColumn, 2, Style.Unit.PCT);

        if (getSettings().isRegistryEnabled()) {
            // Pull image.
            final Column<KieImage, String> pullColumn = new Column<KieImage, String>(
                    new ButtonCell()) {
                @Override
                public String getValue(final KieImage object) {
                    return Constants.INSTANCE.pull();
                }
            };
            pullColumn.setFieldUpdater(new FieldUpdater<KieImage, String>() {
                @Override
                public void update(final int index, final KieImage image, final String value) {
                    pullImage(image);
                }
            });
            pullColumn.setSortable(false);
            imagesGrid.addColumn(pullColumn, Constants.INSTANCE.pull());
            imagesGrid.setColumnWidth(pullColumn, 2, Style.Unit.PCT);
        }
    }

    private void pullImage(final KieImage image) {
        if (image == null) {
            showPopup(Constants.INSTANCE.notAvailable());
        } else {
            final String pull = SharedUtils.getPullAddress(image, getSettings());
            showPullView(pull);
        }
    }
    
    private void showContainers(final KieImage image) {
        showLoadingView();
        
        final KieClientManager kieClientManager = KieClientManager.getInstance();
        final List<KieContainer> containers = kieClientManager.getContainers();
        if (containers != null) {
            final String i = SharedUtils.getImage(image.getRegistry(), image.getRepository(), image.getTags().iterator().next());
            final List<KieContainer> result = new LinkedList<KieContainer>();
            for (final KieContainer container : containers) {
                final String _image = container.getImage();
                if (i.equalsIgnoreCase(_image)) result.add(container);
            }

            hideLoadingView();
            fireEvent(new ShowContainersEvent(result));
        }
        
    }

    private void downloadArtifact(final KieImage image) {
        // Obtain current settings from client cache.
        final Settings settings = SettingsClientHolder.getInstance().getSettings();
        final Set<String> tags = image.getTags();
        if (tags != null && !tags.isEmpty()) {
            final String downloadURL = ClientUtils.getDownloadURL(settings, image);
            GWT.log("Downloading artifact using URL = '" + downloadURL + "'");
            Window.open(downloadURL,"_blank","");
        }
    }
    
    private void redrawTable() {
        hideLoadingView();
        imagesGrid.redraw();
    }

    private void addDataDisplay(HasData<KieImage> display) {
        imagesProvider.addDataDisplay(display);
    }
    
    public void clear() {
        hideLoadingView();
        hidePullView();
        mainPanel.setVisible(false);
        imagesProvider.getList().clear();
    }

    private void showError(final Throwable throwable) {
        showError("ERROR on ImagesView. Exception: " + throwable.getMessage());
    }

    private void showPopup(final String message) {
        Window.alert(message);
    }

    
    private void showError(final String message) {
        hidePullView();
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

    private void showPullView(final String pull) {
        pullText.setText(pull);
        pullPanel.center();
        pullPanel.setVisible(true);
        pullPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        pullPanel.show();
    }

    private void hidePullView() {
        pullPanel.setVisible(false);
        pullPanel.getElement().getStyle().setDisplay(Style.Display.NONE);
        pullPanel.hide();
    }
    
    private Settings getSettings() {
        if (settings == null) {
            settings = SettingsClientHolder.getInstance().getSettings();
        }
        return settings;
    }

    // ****************************************************
    //                  SHOW CONTAINERS EVENT
    // ****************************************************

    public interface ShowContainersEventHandler extends EventHandler
    {
        void onShowContainers(ShowContainersEvent event);
    }

    public static class ShowContainersEvent extends GwtEvent<ShowContainersEventHandler> {

        public static Type<ShowContainersEventHandler> TYPE = new Type<ShowContainersEventHandler>();

        private List<KieContainer> containers;

        public ShowContainersEvent(final List<KieContainer> containers) {
            super();
            this.containers = containers;
        }

        @Override
        public Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ShowContainersEventHandler handler) {
            handler.onShowContainers(this);
        }

        public List<KieContainer> getContainers() {
            return containers;
        }
    }

    public HandlerRegistration addShowContainersEventHandler(final ShowContainersEventHandler handler) {
        return addHandler(handler, ShowContainersEvent.TYPE);
    }
    
}
