package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.CommandAction;
import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.Command;
import cucumber.api.java.hu.De;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.List;

public abstract class ContextMenuProvider {
    public static final ContextMenuProvider DEFAULT_PROVIDER = new ContextMenuProvider() {
        public JPopupMenu buildContextMenu(Object selected) {
            return new JPopupMenu();
        }
    };

    private static final JMenuItem SEPARATOR = new JMenuItem();

    private CommandExecutor executor;

    public abstract JPopupMenu buildContextMenu(Object selected);

    protected JPopupMenu buildDisplayMenu(Object selected) {
        JPopupMenu menu = buildContextMenu(selected);
        attachExecutor(menu);
        return menu;
    }
    public ContextMenuProvider(){}

    @Deprecated
    public ContextMenuProvider(PropertyChangeListener listener) {
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public static void addSeparator(JPopupMenu menu) {
        Object last = menu.getComponentCount() > 0 ? menu.getComponents()[menu.getComponentCount()-1] : null;
        if(last instanceof JPopupMenu.Separator)
            return;
        menu.addSeparator();
    }

    public static JMenuItem separator() {
        return SEPARATOR;
    }

    public static JMenuItem createItem(Action action) {
        JMenuItem item = new JMenuItem(action.getText());
        item.addActionListener(action);
        item.setSelected(action.isChecked());
        return item;
    }

    public static JMenuItem createItem(String text, boolean checked, Command command) {
        return createItem(new CommandAction(text, checked, command));
    }

    public static JMenuItem createItem(String text, List<JMenuItem> items) {
        JMenu menu = new JMenu(text);
        boolean allSeparator = true;
        for(JMenuItem item: items) {
            if (item != SEPARATOR)
                allSeparator = false;
            menu.add(item);
        }

        if(allSeparator)
            menu.removeAll();

        return menu;
    }

    public static void addAll(JPopupMenu menu, List<JMenuItem> items) {
        Object lastItem = menu.getComponentCount() > 0 ? menu.getComponents()[menu.getComponentCount()-1] : null;
        if(lastItem instanceof JPopupMenu.Separator)
            lastItem = SEPARATOR;

        for (int i = 0; i < items.size(); i++) {
            JMenuItem item = items.get(i);
            if (item == SEPARATOR && lastItem != SEPARATOR) {
                addSeparator(menu);
                lastItem = SEPARATOR;
            } else {
                if (isEmpty(item))
                    continue;;

                lastItem = item;
                menu.add(item);
            }
        }
    }

    private static boolean isEmpty(JMenuItem item) {
        return item instanceof JMenu ? ((JMenu)item).getItemCount() == 0 : false;
    }

    public void attachExecutor(MenuElement menuElement) {
        if(menuElement.getSubElements().length > 0) {
            for(MenuElement item: menuElement.getSubElements()){
                attachExecutor(item);
            }
        }else {
            if(menuElement instanceof JPopupMenu)
                return;

            if(((JMenuItem) menuElement).getActionListeners().length != 0)
                ((Action) ((JMenuItem) menuElement).getActionListeners()[0]).setExecutor(executor);
        }
    }
}
