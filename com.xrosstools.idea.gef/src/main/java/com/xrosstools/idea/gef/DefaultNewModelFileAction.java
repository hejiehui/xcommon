package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.actions.CodeGenHelper;
import com.xrosstools.idea.gef.extensions.ExtensionManager;
import com.xrosstools.idea.gef.extensions.GenerateModelExtension;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class DefaultNewModelFileAction extends AnAction {
    private String modelTypeName;
    private String modelTypeExt;
    private Icon icon;
    private String newFileName;
    private final String template;
    private GenerateModelExtension extension;

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
        extension = ExtensionManager.createNewModelFileExtension();
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
        if (selected == null) {
            Messages.showErrorDialog("Please select location first", "Error");
            return;
        }

        if (!selected.isDirectory()) {
            selected = selected.getParent();
        }

        final VirtualFile dir = selected;

        boolean supportGenerateModel = extension.isGenerateModelSupported(modelTypeExt);
        NewModelFileDialog nameDescriptionDialog = new NewModelFileDialog(project, "Name: ", "New " + modelTypeName + " Model", icon, newFileName, supportGenerateModel);
        if (!nameDescriptionDialog.showAndGet())
            return;

        String name = nameDescriptionDialog.getName();
        String description = nameDescriptionDialog.getDescription();
        boolean streamMode = nameDescriptionDialog.isStreamMode();

        if (!canClose(dir, name))
            return;

        if(supportGenerateModel && (description != null && description.trim().length() > 0))
            extension.generateModel(description, generated->createModelFile(project, dir, modelTypeExt, name, generated), streamMode);
        else
            createModelFile(project, dir, modelTypeExt, name, createTemplateFor(name));
    }

    private void createModelFile(Project project, VirtualFile dir, String modelTypeExt, String name, String templateStr) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    VirtualFile newFile = dir.createChildData(project, name + "." + modelTypeExt);
                    newFile.setBinaryContent(templateStr.getBytes(StandardCharsets.UTF_8));
                    FileEditorManager.getInstance(project).openFile(newFile, true);
                } catch (Throwable e) {
                    throw new IllegalArgumentException("Can not create document: " + name, e);
                }
            }
        });
    }

    private boolean canClose(VirtualFile dir, String s) {
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
}
