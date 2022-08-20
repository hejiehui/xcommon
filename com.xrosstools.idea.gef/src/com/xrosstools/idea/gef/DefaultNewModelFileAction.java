package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

public class DefaultNewModelFileAction extends AnAction {
    private String modelTypeName;
    private String modelTypeExt;
    private String iconName;
    private String newFileName;
    private String templatePath;

    public DefaultNewModelFileAction(
            String modelTypeName,
            String modelTypeExt,
            String iconName,
            String newFileName,
            String templatePath) {
        this.modelTypeName = modelTypeName;
        this.modelTypeExt = modelTypeExt;
        this.iconName = iconName;
        this.newFileName = newFileName;
        this.templatePath = templatePath;
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

        Messages.InputDialog dialog = new Messages.InputDialog(project, "New " + modelTypeName + " Model", "Name: ", IconLoader.findIcon(Activator.getIconPath(iconName)), newFileName, new InputValidator() {
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

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    VirtualFile newFile = dir.createChildData(project, name + "." + modelTypeExt);

                    BufferedInputStream in = new BufferedInputStream(getClass().getResourceAsStream(templatePath));
                    int buf_size = 1024;
                    byte[] buffer = new byte[buf_size];
                    int len;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while (-1 != (len = in.read(buffer, 0, buf_size))) {
                        bos.write(buffer, 0, len);
                    }

                    newFile.setBinaryContent(bos.toByteArray());
                    FileEditorManager.getInstance(project).openFile(newFile, true);
                } catch (Throwable e) {
                    throw new IllegalStateException("Can not save document " + name, e);
                }
            }
        });
    }
}
