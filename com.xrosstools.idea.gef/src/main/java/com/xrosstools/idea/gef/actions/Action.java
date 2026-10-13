package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CommandListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public abstract class Action implements ActionListener {
    public static final Action SEPARATOR = new Action() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    private String text;
    private String tooltip;
    private boolean checked;
    private Icon icon;
    private CommandExecutor executor;

    @Deprecated
    public void setListener(PropertyChangeListener listener) {}

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean calculateEnabled() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Command c = createCommand();
        if(c == null)
            return;

        executor.execute(c);
    }

    public Command createCommand() {return null;}

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }
}
