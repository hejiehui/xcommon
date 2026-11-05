package com.xrosstools.idea.gef;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.xrosstools.gef.EditorInteraction;
import com.xrosstools.idea.gef.actions.tools.SearchModelAction;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.List;

public class ListDialog extends DialogWrapper {
    private JBList<SearchModelAction.Entry> list;
    private final List<SearchModelAction.Entry> items;
    private final EditorInteraction editorInteraction;

    public ListDialog(Project project, List<SearchModelAction.Entry> items, EditorInteraction editorInteraction) {
        super(project);
        this.items = items;
        this.editorInteraction = editorInteraction;
        init();
        setTitle(String.format("Found %d occurrences", items.size()));
    }

    @Override
    protected JComponent createCenterPanel() {
        list = new JBList<>(items);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // 确保只触发一次
                    SearchModelAction.Entry selected = list.getSelectedValue();
                    editorInteraction.selectModel(selected.model);
                }
            }
        });

        return new JBScrollPane(list);
    }
}
