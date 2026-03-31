package com.xrosstools.idea.gef.util;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.function.Supplier;

/**
 * This class is used for accessing index property value
 */
public class ComboBoxPropertyDescriptor extends PropertyDescriptor {
    private ComboBox ctrl = new ComboBox();
    private Supplier<String[]> provider;

    public ComboBoxPropertyDescriptor(String propertyId, String label, String[] values) {
        this(label, () -> values);
        setId(propertyId);
    }

    public ComboBoxPropertyDescriptor(String label, String[] values) {
        this(label, () -> values);
    }

    public ComboBoxPropertyDescriptor(String[] values) {
        this(() -> values);
    }

    public ComboBoxPropertyDescriptor(String label, Supplier<String[]> provider) {
        this(provider);
        setLabel(label);
    }

    public ComboBoxPropertyDescriptor(Supplier<String[]> provider) {
        this.provider = provider;
    }

    public JComponent getEditor(Object value) {
        ctrl.removeAllItems();
        for (String v : provider.get())
            ctrl.addItem(v);

        ctrl.setSelectedIndex((Integer) value);
        ctrl.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                ctrl.transferFocusUpCycle();
        });

        return ctrl;
    }

    public Object getCellEditorValue() {
        return ctrl.getSelectedIndex();
    }

    @Override
    public Object convertToProperty(String displayText) {
        String[] values = provider.get();
        for(int i = 0; i < values.length; i++)
            if (values[i].equals(displayText))
                return i;
        return -1;
    }

    public String getDisplayText(Object editorValue) {
        int index = (Integer) editorValue;
        String[] values = provider.get();
        return index < 0 || index >= values.length ? "" : values[index];
    }
}