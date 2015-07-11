package org.kie.dockerui.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonCell;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import org.kie.dockerui.client.Log;
import org.kie.dockerui.client.resources.i18n.Constants;
import org.kie.dockerui.client.service.ArtifactsService;
import org.kie.dockerui.client.service.ArtifactsServiceAsync;
import org.kie.dockerui.client.service.SettingsClientHolder;
import org.kie.dockerui.client.util.ClientUtils;
import org.kie.dockerui.client.widgets.TimeoutPopupPanel;
import org.kie.dockerui.shared.model.KieArtifact;
import org.kie.dockerui.shared.model.KieImage;
import org.kie.dockerui.shared.settings.Settings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ArtifactsView extends Composite {

    interface ArtifactsViewBinder extends UiBinder<Widget, ArtifactsView> {}
    private static ArtifactsViewBinder uiBinder = GWT.create(ArtifactsViewBinder.class);

    interface ArtifactsViewStyle extends CssResource {
        String mainPanel();
        String loadingPanel();
        String artifactsList();
        String artifactsPanel();
        String searchPanel();
    }

    @UiField
    ArtifactsViewStyle style;
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    TimeoutPopupPanel loadingPanel;
    
    @UiField
    TextBox searchBox;
    
    @UiField
    Button searchButton;
    
    @UiField
    Button cleanSearchButton;
    
    @UiField
    HTMLPanel artifactsPanel;
    
    @UiField(provided = true)
    CellTable artifactsGrid;
    
    @UiField(provided = true)
    SimplePager pager;


    private Settings settings = null;
    private final ArtifactsServiceAsync artifactsService = GWT.create(ArtifactsService.class);
    private List<KieArtifact> artifacts = null;

    /**
     * The provider that holds the list of artifacts.
     */
    private final ListDataProvider<KieArtifact> gridProvider = new ListDataProvider<KieArtifact>();
    
    private static final ProvidesKey<KieArtifact> KEY_PROVIDER = new ProvidesKey<KieArtifact>() {
        @Override
        public Object getKey(final KieArtifact item) {
            return item == null ? null : item.getFileName();
        }
    };
    
    @UiConstructor
    public ArtifactsView() {

        // Init the artifacts grid.
        initArtifactsGrid();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        searchBox.addKeyPressHandler(searchBoxKeyPressHandler);
    }
    
    private final KeyPressHandler searchBoxKeyPressHandler = new KeyPressHandler() {
        @Override
        public void onKeyPress(final KeyPressEvent keyPressEvent) {
            if (keyPressEvent.getCharCode() == KeyCodes.KEY_ENTER) {
                doSearch();
            }
        }
    };

    private void initArtifactsGrid() {
        artifactsGrid = new CellTable<KieArtifact>(KEY_PROVIDER);
        artifactsGrid.setWidth("100%", true);

        // Do not refresh the headers and footers every time the data is updated.
        artifactsGrid.setAutoHeaderRefreshDisabled(true);
        artifactsGrid.setAutoFooterRefreshDisabled(true);

        // Attach a column sort handler to the ListDataProvider to sort the list.
        ColumnSortEvent.ListHandler<KieArtifact> sortHandler = new ColumnSortEvent.ListHandler<KieArtifact>(gridProvider.getList());
        artifactsGrid.addColumnSortHandler(sortHandler);

        // Create a Pager to control the table.
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(artifactsGrid);

        // Add a selection model so we can select cells.
        final SelectionModel<KieArtifact> selectionModel = new MultiSelectionModel<KieArtifact>(KEY_PROVIDER);
        artifactsGrid.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<KieArtifact>createCheckboxManager());

        // Initialize the columns.
        initTableColumns(selectionModel, sortHandler);

        // Add the CellList to the adapter in the database.
        addDataDisplay(artifactsGrid);
    }

    private void initTableColumns(SelectionModel<KieArtifact> selectionModel, ColumnSortEvent.ListHandler<KieArtifact> sortHandler) {

        // Artifact Filename.
        final Column<KieArtifact, String> fileNameColumn = new Column<KieArtifact, String>(
                new TextCell()) {
            @Override
            public String getValue(KieArtifact object) {
                return object.getFileName();
            }
        };
        fileNameColumn.setSortable(true);
        sortHandler.setComparator(fileNameColumn, new Comparator<KieArtifact>() {
            @Override
            public int compare(KieArtifact o1, KieArtifact o2) {
                return o1.getFileName().compareTo(o2.getFileName());
            }
        });
        artifactsGrid.addColumn(fileNameColumn, Constants.INSTANCE.fileName());
        artifactsGrid.setColumnWidth(fileNameColumn, 5, Style.Unit.PCT);

        // Artifact type.
        final Column<KieArtifact, String> artifactTypeColumn = new Column<KieArtifact, String>(
                new TextCell()) {
            @Override
            public String getValue(KieArtifact object) {
                return object.getExtension();
            }
        };
        artifactTypeColumn.setSortable(true);
        sortHandler.setComparator(artifactTypeColumn, new Comparator<KieArtifact>() {
            @Override
            public int compare(KieArtifact o1, KieArtifact o2) {
                return o1.getExtension().compareTo(o2.getExtension());
            }
        });
        artifactsGrid.addColumn(artifactTypeColumn, Constants.INSTANCE.artifactType());
        artifactsGrid.setColumnWidth(artifactTypeColumn, 5, Style.Unit.PCT);

        
        // Artifact timestamp.
        final Column<KieArtifact, String> timestampColumn = new Column<KieArtifact, String>(
                new TextCell()) {
            @Override
            public String getValue(KieArtifact object) {
                return object.getTimestamp();
            }
        };
        timestampColumn.setSortable(true);
        sortHandler.setComparator(timestampColumn, new Comparator<KieArtifact>() {
            @Override
            public int compare(KieArtifact o1, KieArtifact o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
        artifactsGrid.addColumn(timestampColumn, Constants.INSTANCE.timestamp());
        artifactsGrid.setColumnWidth(timestampColumn, 5, Style.Unit.PCT);

        // Download artifact button.
        // Pull image.
        final Column<KieArtifact, String> downloadColumn = new Column<KieArtifact, String>(
                new ButtonCell()) {
            @Override
            public String getValue(final KieArtifact object) {
                return Constants.INSTANCE.download();
            }
        };
        downloadColumn.setFieldUpdater(new FieldUpdater<KieArtifact, String>() {
            @Override
            public void update(final int index, final KieArtifact artifact, final String value) {
                fireDownload(artifact);
            }
        });
        downloadColumn.setSortable(false);
        artifactsGrid.addColumn(downloadColumn, Constants.INSTANCE.download());
        artifactsGrid.setColumnWidth(downloadColumn, 2, Style.Unit.PCT);
    }
    
    private void fireDownload(final KieArtifact artifact) {
        if (artifact == null || artifact.getAbsoluteFilePath() == null) return;
        final String downloadURL = ClientUtils.getDownloadURL(artifact, getSettings());
        if (downloadURL != null) {
            GWT.log("Downloading artifact using URL = '" + downloadURL + "'");
            Window.open(downloadURL,"_blank","");
        }
    }

    /**
     * Add a new image into the grid provider's list.
     */
    public void addArtifact(KieArtifact artifact) {
        List<KieArtifact> contacts = gridProvider.getList();
        contacts.remove(artifact);
        contacts.add(artifact);
    }
    
    private void redrawTable() {
        hideLoadingView();
        artifactsGrid.redraw();
    }

    private void addDataDisplay(HasData<KieArtifact> display) {
        gridProvider.addDataDisplay(display);
    }


    public void show() {
        clear();
        showLoadingView();
        
        artifactsService.list(new AsyncCallback<List<KieArtifact>>() {
            @Override
            public void onFailure(final Throwable throwable) {
                showError(throwable);
            }

            @Override
            public void onSuccess(final List<KieArtifact> kieArtifacts) {
                if (kieArtifacts != null) {
                    for (final KieArtifact kieArtifact : kieArtifacts) {
                        addArtifact(kieArtifact);
                    }
                    artifacts = new ArrayList<KieArtifact>(gridProvider.getList());
                    redrawTable();
                    hideLoadingView();
                }

                mainPanel.setVisible(true);
            }
        });
    }

    @UiHandler( "searchButton" )
    public void onSearch( final ClickEvent event ) {
        doSearch();
    }

    @UiHandler( "cleanSearchButton" )
    public void onCleanSearch( final ClickEvent event ) {
        cleanSearch();
    }

    private void doSearch() {
        final String searchPattern = searchBox.getText();
        if (searchPattern == null || searchPattern.trim().length() == 0) {
            cleanSearch();
        } else {
            if (artifacts != null && !artifacts.isEmpty()) {
                final List<KieArtifact> filtered = new ArrayList<KieArtifact>();
                for (final KieArtifact artifact : artifacts) {
                    if (artifact.getFileName().contains(searchPattern)) filtered.add(artifact);
                }
                cleanSearchButton.setVisible(true);
                gridProvider.setList(filtered);
                redrawTable();
            }
        }
    }
    
    private void cleanSearch() {
        searchBox.setText("");
        cleanSearchButton.setVisible(false);
        gridProvider.setList(artifacts);
        redrawTable();
    }
    
    private Settings getSettings() {
        if (settings == null) {
            settings = SettingsClientHolder.getInstance().getSettings();
        }
        return settings;
    }
    
    public void clear() {
        hideLoadingView();
        mainPanel.setVisible(false);
        artifacts = null;
        searchBox.setText("");
        gridProvider.getList().clear();
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

}
