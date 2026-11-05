package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.InputTextCommand;

public class InputTextCommandAction extends BaseDialogAction {
    private InputTextCommand command;
    public InputTextCommandAction(String dialogTitle, String dialogMessage, String initialValue, InputTextCommand command) {
        super(dialogTitle, dialogMessage, initialValue);
        this.command = command;
    }

    @Override
    protected Command createCommand(String value) {
        command.setInputText(value);
        return command;
    } 
}
