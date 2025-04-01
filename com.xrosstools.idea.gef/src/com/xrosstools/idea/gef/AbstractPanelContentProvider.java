package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class AbstractPanelContentProvider<T extends IPropertySource> implements PanelContentProvider<T>, PropertyChangeListener {
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
    public ContextMenuProvider getContextMenuProvider() {
        return ContextMenuProvider.DEFAULT_PROVIDER;
    }

    @Override
    public ContextMenuProvider getOutlineContextMenuProvider() {
        return ContextMenuProvider.DEFAULT_PROVIDER;
    }

    @Override
    public void preBuildRoot(){}

    @Override
    public void postBuildRoot(){}

    //TODO to be removed i future
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    public void createConnection(Object connModel){
        editorPanel.createConnection(connModel);
    }

    public void createModel(Object model){
        editorPanel.createModel(model);
    }

    /**
     * This is just an adapter method for backward compatible of xdecision and xstate editor.
     * It will be removed from future release
     */
    @Override
    public ActionGroup createToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        JToolBar  toolbar = new JToolBar ();
        toolbar.setFloatable(false);
        buildToolbar(toolbar);

        for(Component c: toolbar.getComponents()){
            if(c instanceof JButton) {
                JButton b = (JButton)c;
                actionGroup.add(createToolbarAction(b.getActionListeners()[0], b.getIcon(), b.getToolTipText()));
            }

            if(c instanceof JToolBar.Separator) {
                actionGroup.addSeparator();
            }
        }
        return actionGroup;
    }

    /**
     * This method is moved up from old PanelContentProvider for backward compatible of xdecision and xstate editor.
     * It will be removed from future release
     */
    @Deprecated
    public void buildToolbar(JToolBar toolbar) {}

    public AnAction createToolbarAction(ActionListener action, Icon icon, String tooltip) {
        return new AnActionAdapter(tooltip, tooltip, icon, attachExecutor(action));
    }

    public static class AnActionAdapter extends AnAction {
        private ActionListener listener;
        public AnActionAdapter(@Nullable String text, @Nullable String description, @Nullable Icon icon, @Nullable ActionListener listener) {
            super(text, description, icon);
            this.listener = listener;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            //TODO in the future, we need to construct action event from anActionEvent.
            listener.actionPerformed(null);
        }

        public ActionListener getListener() {
            return listener;
        }
    }

    @Deprecated
    public JButton createToolbarButton(Action action, Icon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.addActionListener(attachExecutor(action));
        btn.setSize(new Dimension(32, 32));
        btn.setPreferredSize(new Dimension(32, 32));
        return btn;
    }

    public JButton createPaletteButton(ActionListener action, Icon icon, String tooltip) {
        JButton btn = new JButton(tooltip, icon);
        btn.setContentAreaFilled(false);
        btn.addActionListener(attachExecutor(action));
        return btn;
    }

    public ActionListener attachExecutor(ActionListener action) {
        if(action instanceof Action)
            ((Action)action).setExecutor(editorPanel);
        return action;
    }
}
