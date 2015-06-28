package org.kie.dockerui.client.widgets;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonCell;
import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import org.kie.dockerui.client.resources.i18n.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapEditor<T, K> extends Composite implements
        HasValue<Map<T,K>> {

    interface MapEditorStyle extends CssResource {
        String errorPanelError();
        String mainPanel();
        String gridPanel();
        String grid();
        String addButtonPanel();
    }

    interface Binder extends UiBinder<Widget, MapEditor> {
        Binder BINDER = GWT.create(Binder.class);
    }
    
    private Map<T, K> value;

    @UiField
    MapEditorStyle style;
    
    @UiField
    HTMLPanel mainPanel;
    
    @UiField
    ScrollPanel gridPanel;
    
    @UiField
    DataGrid<Map.Entry<T, K>> grid;
    
    @UiField
    Button addButton;
    
    @UiField
    com.github.gwtbootstrap.client.ui.Label errorLabel;

    private final ListDataProvider<Map.Entry<T, K>> model = new ListDataProvider<Map.Entry<T, K>>(new LinkedList<Map.Entry<T, K>>());
    
    public MapEditor() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        createGrid();
        addButton.addClickHandler(addClickHandler);
    }
    
    private final ClickHandler addClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            fireEvent(new ValueAddEvent(Constants.INSTANCE.newValue(), Constants.INSTANCE.newValue()));
        }
    };
    
    private final FieldUpdater removeButtonHandler = new FieldUpdater<Map.Entry<T, K>, String>() {
        @Override
        public void update(int index, Map.Entry<T, K> object, String value) {
            removeEntry(object.getKey());
            redraw();
        }
    };

    private final FieldUpdater keyModifiedEventHandler = new FieldUpdater<Map.Entry<T, K>, String>() {
        @Override
        public void update(int index, Map.Entry<T, K> object, String value) {
            fireEvent(new KeyModifiedEvent(index, object.getKey().toString(), value));
        }
    };
    
    private final FieldUpdater valueModifiedEventHandler = new FieldUpdater<Map.Entry<T, K>, String>() {
        @Override
        public void update(int index, Map.Entry<T, K> object, String value) {
            fireEvent(new ValueModifiedEvent(index, object.getValue().toString(), value));
        }
    };

    @Override
    public Map<T, K> getValue() {
        return value;
    }

    @Override
    public void setValue(Map<T, K> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Map<T, K> value, boolean fireEvents) {
        /*if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }*/

        // Disable current error markers, if present.
        disableError();

        final Map<T, K> before = this.value;
        this.value = value;

        // Fill grid values.
        redraw();

        // Fire events, if necessary.
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Map<T, K>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    private void createGrid() {
        
        grid.setEmptyTableWidget(new Label(Constants.INSTANCE.noData()));
        
        // Create KEY column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> keyColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        return object.getKey().toString();
                    }
                };
        keyColumn.setSortable(false);
        keyColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        grid.addColumn(keyColumn, Constants.INSTANCE.key());
        grid.setColumnWidth(keyColumn, 20, Style.Unit.PCT);
        keyColumn.setFieldUpdater(keyModifiedEventHandler);

        // Create Value column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> valueColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        return object.getValue().toString();
                    }
                };
        valueColumn.setSortable(false);
        valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        grid.addColumn(valueColumn, Constants.INSTANCE.value());
        grid.setColumnWidth(valueColumn, 20, Style.Unit.PCT);
        valueColumn.setFieldUpdater(valueModifiedEventHandler);

        // Create remove button column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> removeColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new ButtonCell(IconType.MINUS, ButtonSize.MINI)) {

                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        // return Constants.INSTANCE.remove();
                        return null;
                    }
                };
        
        removeColumn.setFieldUpdater(removeButtonHandler);
        removeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        grid.addColumn(removeColumn, Constants.INSTANCE.actions());
        grid.setColumnWidth(removeColumn, 20, Style.Unit.PCT);

        // Link model to grid.
        // model.addDataDisplay(grid);

    }
    
    private K removeEntry(T key) {
        return this.value.remove(key);
    }
    
    public void redraw() {
        // NOTE: If not removing and re-adding columns, grid tow data refresh is not well done. 
        grid.removeColumn(0);
        grid.removeColumn(0);
        grid.removeColumn(0);
        createGrid();
        
        final int count = value != null ? value.size() : 0;
        final List<Map.Entry<T, K>> list = value != null ? new LinkedList<Map.Entry<T, K>>(value.entrySet()) : new LinkedList<Map.Entry<T, K>>(); 
        grid.setRowCount(count);
        grid.setRowData(0, list);
    }
    
    private void enableError(String text) {
        setLabelText(text);
        errorLabel.setVisible(true);
    }

    private void disableError() {
        setLabelText(null);
        errorLabel.setVisible(false);
    }
    
    public void clear() {
        setValue(null);
        disableError();
    }

    private void setLabelText(final String text) {
        if (text == null || text.trim().length() == 0) {
            errorLabel.setText("");
        } else {
            errorLabel.setText(text);
        }
    }

    /*
        EVENTS
     */
    
    public HandlerRegistration addValueAddEventHandler(ValueAddEventHandler handler)
    {
        return this.addHandler(handler, ValueAddEvent.TYPE);
    }

    public HandlerRegistration addValueModifiedEventHandler(ValueModifiedEventHandler handler)
    {
        return this.addHandler(handler, ValueModifiedEvent.TYPE);
    }

    public HandlerRegistration addKeyModifiedEventHandler(KeyModifiedEventHandler handler)
    {
        return this.addHandler(handler, KeyModifiedEvent.TYPE);
    }
    
    public static class ValueAddEvent extends GwtEvent<ValueAddEventHandler> {

        public static Type<ValueAddEventHandler> TYPE = new Type<ValueAddEventHandler>();
        
        private String key;
        private String value;

        public ValueAddEvent(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }


        @Override
        public Type<ValueAddEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ValueAddEventHandler handler) {
            handler.onValueAdd(this);
        }
    }

    public interface ValueAddEventHandler extends EventHandler
    {
        void onValueAdd(ValueAddEvent event);
    }

    public abstract static class AbstractValueModifiedEvent<T extends EventHandler> extends GwtEvent<T> {

        private int index;
        private String last;
        private String value;

        public AbstractValueModifiedEvent(int index, String last, String value) {
            this.index = index;
            this.last = last;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public String getLast() {
            return last;
        }

        public String getValue() {
            return value;
        }
    }
    
    public static class ValueModifiedEvent extends AbstractValueModifiedEvent<ValueModifiedEventHandler> {

        public static Type<ValueModifiedEventHandler> TYPE = new Type<ValueModifiedEventHandler>();

        public ValueModifiedEvent(int index, String last, String value) {
            super(index, last, value);
        }


        @Override
        public Type<ValueModifiedEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ValueModifiedEventHandler handler) {
            handler.onValueModified(this);
        }
    }

    public static class KeyModifiedEvent extends AbstractValueModifiedEvent<KeyModifiedEventHandler> {

        public static Type<KeyModifiedEventHandler> TYPE = new Type<KeyModifiedEventHandler>();

        public KeyModifiedEvent(int index, String last, String value) {
            super(index, last, value);
        }


        @Override
        public Type<KeyModifiedEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(KeyModifiedEventHandler handler) {
            handler.onKeyModified(this);
        }
    }

    public interface KeyModifiedEventHandler extends EventHandler
    {
        void onKeyModified(KeyModifiedEvent event);
    }

    public interface ValueModifiedEventHandler extends EventHandler
    {
        void onValueModified(ValueModifiedEvent event);
    }
}
