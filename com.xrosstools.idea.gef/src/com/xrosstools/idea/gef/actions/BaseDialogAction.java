package com.xrosstools.idea.gef.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.xrosstools.idea.gef.commands.Command;

import javax.swing.*;

public abstract class BaseDialogAction extends Action {
	private Project project;
	private String dialogTitle;
	private String dialogMessage; 
	private String initialValue;
    private Icon icon;
 
	public BaseDialogAction(
			Project project,
			String dialogTitle,
            String dialogMessage, 
            String initialValue){
		this(project, dialogTitle,dialogMessage,initialValue, AllIcons.Actions.EditSource);
	}

	public BaseDialogAction(
			Project project,
			String dialogTitle,
			String dialogMessage,
			String initialValue,
			Icon icon){
		this.project = project;
		this.dialogTitle = dialogTitle;
		this.dialogMessage = dialogMessage;
		this.initialValue = initialValue;
		this.icon = icon;
		setText(dialogTitle);
	}
	public Project getProject() {
		return project;
	}

	abstract protected Command createCommand(String value);

	@Override
    public Command createCommand() {
		Messages.InputDialog dialog = new Messages.InputDialog(dialogTitle, dialogMessage, icon, initialValue, new InputValidator() {
			@Override
			public boolean checkInput(String s) {
				return true;
			}

			@Override
			public boolean canClose(String s) {
				return true;
			}
		});
		dialog.show();

		return dialog.getExitCode() == 0 ? createCommand(dialog.getInputString()) : null ;
	}
}
