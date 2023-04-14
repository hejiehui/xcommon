package com.xrosstools.idea.gef;

import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

public abstract class AbstractPanelContentProvider<T extends IPropertySource> implements PanelContentProvider<T> {
    private VirtualFile virtualFile;
    private EditorPanel editorPanel;
    public VirtualFile getFile() {
        return virtualFile;
    }

    public AbstractPanelContentProvider(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    @Override
    public void setEditorPanel(EditorPanel editorPanel) {
        this.editorPanel = editorPanel;
    }

    @Override
    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    @Override
    public void preBuildRoot(){}

    @Override
    public void postBuildRoot(){}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        editorPanel.refresh();
    }

    public void createConnection(Object connModel){
        editorPanel.createConnection(connModel);
    }

    public void createModel(Object model){
        editorPanel.createModel(model);
    }

    public JButton createToolbarButton(Action action, Icon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.addActionListener(action);
        btn.setSize(new Dimension(32, 32));
        btn.setPreferredSize(new Dimension(32, 32));
        return btn;
    }

    public JButton createPaletteButton(ActionListener action, Icon icon, String tooltip) {
        JButton btn = new JButton(tooltip, icon);
        btn.setContentAreaFilled(false);
        btn.addActionListener(action);
        return btn;
    }
}
