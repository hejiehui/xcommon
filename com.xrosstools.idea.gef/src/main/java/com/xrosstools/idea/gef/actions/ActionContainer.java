package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

public class ActionContainer extends Action {
    private List<Action> subItems = new ArrayList<>();

    public void add(Action actionItem) {
        subItems.add(actionItem);
    }

    public List<Action> getSubItems() {
        return subItems;
    }

    @Override
    public Command createCommand() {
        return null;
    }
}
