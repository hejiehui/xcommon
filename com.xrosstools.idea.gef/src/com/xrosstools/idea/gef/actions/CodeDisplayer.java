package com.xrosstools.idea.gef.actions;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;

public class CodeDisplayer extends DialogWrapper {
    private String text;

    public CodeDisplayer(String title, String text) {
        super(true);
        setTitle(title);
        this.text = text;
        init();
        setSize(700, 500);
    }

    @Override
    protected JComponent createCenterPanel() {
        JBTextArea textArea = new JBTextArea();
        textArea.setText(text);

        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setViewPosition(new Point(0, 0));

        textArea.setCaretPosition(0);
        return scrollPane;
    }
}
