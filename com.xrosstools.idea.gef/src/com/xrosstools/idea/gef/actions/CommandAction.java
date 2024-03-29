package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;

public class CommandAction extends Action {
    private Command command;
    public CommandAction(String text, boolean checked, Command command){
        setText(text);
        setChecked(checked);
        this.command = command;
    }

    public Command createCommand() {
        return command;
    }
}
