package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xrosstools.idea.gef.actions.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionListener;

public class AnActionAdapter extends AnAction {
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

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if(!(listener instanceof com.xrosstools.idea.gef.actions.Action))
            return;

        com.xrosstools.idea.gef.actions.Action action = (Action)listener;
        boolean enabled =action.calculateEnabled();

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        if(presentation.isEnabled())
            presentation.setText(action.getText());
    }

    public ActionListener getListener() {
        return listener;
    }
}
