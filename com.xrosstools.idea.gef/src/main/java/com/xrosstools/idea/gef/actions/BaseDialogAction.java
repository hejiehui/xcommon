package com.xrosstools.idea.gef.actions;

import com.xrosstools.gef.GefIcons;
import com.xrosstools.idea.gef.commands.Command;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class BaseDialogAction extends Action {
	private String dialogTitle;
	private String dialogMessage; 
	private String initialValue;
    private Icon icon;
	private String inputValue;
 
	public BaseDialogAction(
			String dialogTitle,
            String dialogMessage, 
            String initialValue){
		this(dialogTitle,dialogMessage,initialValue, GefIcons.TEXT_PNG);
	}

	public BaseDialogAction(
			String dialogTitle,
			String dialogMessage,
			String initialValue,
			Icon icon){
		this.dialogTitle = dialogTitle;
		this.dialogMessage = dialogMessage;
		this.initialValue = initialValue;
		this.icon = icon;
		setText(dialogTitle);
	}

	abstract protected Command createCommand(String value);

	@Override
	public void actionPerformed(ActionEvent e) {
		inputValue = getPlatformFacade().showInputDialog(dialogTitle, dialogMessage, icon, initialValue);
		super.actionPerformed(e);
	}

	@Override
    public Command createCommand() {
		return inputValue == null ? null : createCommand(inputValue);
	}
}
