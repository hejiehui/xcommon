package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.actions.CodeGenHelper;

import javax.swing.*;

public class DefaultNewModelFileAction extends AnAction {
    private String modelTypeName;
    private String modelTypeExt;
    private Icon icon;
    private String newFileName;
    private final String template;

    public DefaultNewModelFileAction(
            String modelTypeName,
            String modelTypeExt,
            Icon icon,
            String newFileName,
            String templatePath) {
        this.modelTypeName = modelTypeName;
        this.modelTypeExt = modelTypeExt;
        this.icon = icon;
        this.newFileName = newFileName;
        this.template = CodeGenHelper.getTemplate(templatePath, this.getClass()).toString();
    }

    public String getTemplate() {
        return template;
    }

    /**
     * Sub-class can use override this to modify default template
     * @param fileName
     * @return
     */
    public String createTemplateFor(String fileName) {
        return getTemplate();
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        VirtualFile selected = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if(selected == null) {
            Messages.showErrorDialog("Please select location first", "Error");
            return;
        }

        if(!selected.isDirectory()) {
            selected = selected.getParent();
        }

        final VirtualFile dir = selected;

        Messages.InputDialog dialog = new Messages.InputDialog(project, "New " + modelTypeName + " Model", "Name: ", icon, newFileName, new InputValidator() {
            @Override
            public boolean checkInput(String s) {
                return true;
            }

            @Override
            public boolean canClose(String s) {
                if(s== null || s.trim().length() == 0)
                    return false;

                s = s.trim();
                String name = s + "." + modelTypeExt;
                for(VirtualFile c: dir.getChildren()) {
                    if (c.getName().equalsIgnoreCase(name)) {
                        Messages.showErrorDialog("Name \"" + name + "\" is already used, please chose another one.", "Error");
                        return false;
                    }
                }
                return true;
            }
        });
        dialog.show();

        if (dialog.getExitCode() != 0)
            return;

        final String name = dialog.getInputString();
        final String templateStr = createTemplateFor(name);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    VirtualFile newFile = dir.createChildData(project, name + "." + modelTypeExt);
                    newFile.setBinaryContent(templateStr.getBytes());
                    FileEditorManager.getInstance(project).openFile(newFile, true);
                } catch (Throwable e) {
                    throw new IllegalStateException("Can not save document " + name, e);
                }
            }
        });
    }
}
