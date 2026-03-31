package com.xrosstools.idea.gef.util;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.function.Supplier;

public class ListPropertyDescriptor extends PropertyDescriptor {
    private Supplier<Object[]> provider;
    private Object[] values;
    private ComboBox ctrl = new ComboBox();

    /**
     * For fixed options
     */
    public ListPropertyDescriptor(String label, Object[] values) {
        this(label, () -> values);
    }

    public ListPropertyDescriptor(Object[] values) {
        this(null, values);
    }

    /**
     * For non fixed options, you can change options by updating values.
     */
    public ListPropertyDescriptor(String label, List values) {
        this(label, ()->values.toArray());
    }

    public ListPropertyDescriptor(List values) {
        this(null, values);
    }

    public ListPropertyDescriptor(String label, Supplier<Object[]> provider) {
        this(provider);
        setLabel(label);
    }

    public ListPropertyDescriptor(Supplier<Object[]> provider) {
        this.provider = provider;
    }

    public JComponent getEditor(Object value) {
        ctrl.removeAllItems();
        values = provider.get();
        for(Object v: values)
            ctrl.addItem(getDisplayText(v));

        ctrl.setSelectedItem(getDisplayText(value));
        ctrl.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                ctrl.transferFocusUpCycle();
        });

        return ctrl;
    }

    public Object getCellEditorValue() {
        return convertToProperty((String) ctrl.getSelectedItem());
    }

    @Override
    public Object convertToProperty(String displayText) {
        values = provider.get();
        for(Object value: values)
            if(value.toString().equals(displayText))
                return value;
        return null;
    }
}