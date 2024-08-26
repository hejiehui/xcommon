package com.xrosstools.idea.gef.util;

import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class PropertyTableModel extends AbstractTableModel {
    private static final String DEFAULT = "Misc";
    private IPropertySource source;
    private CommandExecutor executor;
    private class TableRow {
        boolean isCategory;
        String categoryName;
        Object propertyName;
        Object label;
        IPropertyDescriptor propertyDescriptor;
    }

    private boolean showCatName;
    private List<TableRow> internalRows = new ArrayList<>();

    public PropertyTableModel(IPropertySource source, CommandExecutor executor) {
        this.source = source;
        this.executor = executor;

        Map<String, List<TableRow>> rowByCategory = new HashMap<>();

        for (IPropertyDescriptor d: source.getPropertyDescriptors()) {
            if(d.isVisible() == false) continue;

            String catName = d.getCategory();
            if(catName == null)
                catName = DEFAULT;
            List<TableRow> rows = rowByCategory.get(catName);
            if(rows == null) {
                rows = new ArrayList<>();
                TableRow header = new TableRow();
                header.isCategory = true;
                header.categoryName = catName;
                rows.add(header);
                rowByCategory.put(catName, rows);
            }

            TableRow row = new TableRow();
            row.propertyName = d.getId();
            row.propertyDescriptor = d;
            row.label = d.getLabel() == null ? d.getId() : d.getLabel();
            rows.add(row);
        }

        if(rowByCategory.size() == 1 && rowByCategory.containsKey(DEFAULT)) {
            internalRows.addAll(rowByCategory.get(DEFAULT));
            // Remove header
            internalRows.remove(0);
        }
        else {
            Set<String> categories = new TreeSet<>(rowByCategory.keySet());
            for (String catName : categories) {
                internalRows.addAll(rowByCategory.get(catName));
            }
        }

        showCatName = rowByCategory.size() > 1;
    }

    public IPropertyDescriptor getDescriptor(int row) {
        return internalRows.get(row).propertyDescriptor;
    }

    public String getDisplayText(int rowIndex, int columnIndex, Object value) {
        if(columnIndex == 0)
            return (String)value;

        IPropertyDescriptor descriptor = getDescriptor(rowIndex);

        return descriptor.getDisplayText(value);
    }

    public boolean isSame(IPropertySource anotherSource) {
        return source == anotherSource;
    }

    @Override
    public int getRowCount() {
        return source == null ? 0 : internalRows.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "Property" : "Value";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return IPropertyDescriptor.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !internalRows.get(rowIndex).isCategory  && columnIndex == 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TableRow row = internalRows.get(rowIndex);

        if(columnIndex == 0) {
            return row.isCategory ? (showCatName ? row.categoryName : "") : ("        " + row.label);
        } else {
            return isCellEditable(rowIndex, columnIndex) ?
                    source.getPropertyValue(internalRows.get(rowIndex).propertyName) :
                    null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String propertyName = (internalRows.get(rowIndex).propertyName).toString();
        Object oldValue = source.getPropertyValue(propertyName);

        if(aValue != oldValue)
            executor.execute(new PropertyChangeCommand(source, propertyName, oldValue, aValue));
    }
}
