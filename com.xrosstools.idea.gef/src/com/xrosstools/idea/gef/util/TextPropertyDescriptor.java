package com.xrosstools.idea.gef.util;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextPropertyDescriptor extends PropertyDescriptor{
    private JTextField editor;

    @Deprecated
    public TextPropertyDescriptor(Object propertyId, Object label) {
        setId(propertyId);
        setLabel(label.toString());
    }

    /**
     * This constructor uses propertyId instead of label at the beginning.
     * It now treat parameter as label
     * @param label used to be propertyId
     */
    public TextPropertyDescriptor(Object label) {
        setLabel(label.toString());
    }

    public TextPropertyDescriptor() {
        editor = new JTextField();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyCode() == e.VK_ENTER || e.getKeyChar() == '\n')
                    editor.transferFocusUpCycle();
            }
        });
    }

    public JComponent getEditor(Object value) {
        editor.setText(value.toString());
        editor.setBorder(null);
        return editor;
    }

    public Object convertToProperty(String text) {
        return text;
    }

    @Override
    public Object getCellEditorValue() {
        return convertToProperty(editor.getText());
    }
}
