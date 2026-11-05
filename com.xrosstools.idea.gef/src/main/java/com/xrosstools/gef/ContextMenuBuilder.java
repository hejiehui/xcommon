package com.xrosstools.gef;

import com.xrosstools.idea.gef.ContextMenuProvider;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.CommandAction;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ContextMenuBuilder<T extends IPropertySource> implements ContentChangeListener<T> {
    public List<Action> buildContextMenu(Object selected) {
        return new ArrayList<>();
    }

    @Override
    public void contentChanged(T content) {
    }

    public static void addSeparator(List<Action> menu) {
        menu.add(Action.SEPARATOR);
    }

    public static Action createItem(String text, boolean checked, Command command) {
        return new CommandAction(text, checked, command);
    }
}
