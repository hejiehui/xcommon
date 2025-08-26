package com.xrosstools.idea.gef.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.xrosstools.idea.gef.commands.Command;

import java.awt.event.ActionEvent;

public abstract class AbstractCodeGenerator extends Action {
    private static final String JAVA = ".java";
    private Project project;
    private String title;

    public String getDefaultPackage() {
        return null;
    }

    public String getDefaultFileName() {
        return null;
    }

    public abstract String getContent(String packageName, String fileName);

    public AbstractCodeGenerator(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //1. Get package dir
        VirtualFile targetDir = selectTargetDirectory(project, getDefaultPackage());
        if(targetDir == null)
            return;

        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(targetDir);
        if (directory == null)
            return;

        //2. Get file name
        String _fileName = getFileName(project, title, getDefaultFileName());
        if(_fileName == null || _fileName.isEmpty()) return;

        final String fileName = _fileName.endsWith(JAVA) ? _fileName : _fileName + JAVA;

        //3. Generate file
        String className = fileName.replace(JAVA, "");
        String packageName = getPackageName(project, targetDir);
        final String fileContent = getContent(packageName, className);
        if(fileContent == null) return;

        //4. Check existing file
        PsiFile existingFile = directory.findFile(fileName);
        if (existingFile != null) {
            int result = Messages.showOkCancelDialog(
                    project,
                    fileName + " is already exist!",
                    "Replace existing file?",
                    "Replace",
                    "Cancel",
                    Messages.getQuestionIcon());

            if(result == Messages.CANCEL)
                return;
        }

        //4. Write to file
        ApplicationManager.getApplication().invokeLater(() ->
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    PsiFile file;
                    if (existingFile != null) {
                        Document document = PsiDocumentManager.getInstance(project).getDocument(existingFile);
                        document.setText(fileContent);
                        PsiDocumentManager.getInstance(project).commitDocument(document);
                        file = existingFile;
                        FileEditorManager.getInstance(project).openFile(file.getVirtualFile(), true);
                    } else {
//                        file = PsiFileFactory.getInstance(project)
//                                .createFileFromText(fileName, JavaFileType.INSTANCE, fileContent);
//                        directory.add(file);

                        try {
                            VirtualFile newFile = targetDir.createChildData(project, fileName);
                            newFile.setBinaryContent(fileContent.getBytes());
                            FileEditorManager.getInstance(project).openFile(newFile, true);
                        } catch (Throwable ex) {
                            throw new IllegalStateException("Can not save document " + fileName, ex);
                        }
                    }
                })
        );
    }

    public static String getFileName(Project project, String title, String defaultName) {
        return Messages.showInputDialog(
                project,
                "Enter file name:",
                title,
                Messages.getQuestionIcon(),
                defaultName,
                new FileNameInputValidator());
    }

    public PsiClass chooseClass(String title, String className) {
        PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
        PsiClass[] classes = cache.getClassesByName(className, GlobalSearchScope.allScope(project));

        PsiClass initialSelection = classes.length > 0 ? classes[0] : null;

        TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(project);
        TreeClassChooser chooser = chooserFactory.createWithInnerClassesScopeChooser(
                title,
                GlobalSearchScope.allScope(project),
                ClassFilter.ALL,
                initialSelection
        );

        chooser.showDialog();
        return chooser.getSelected();
    }

    private static class FileNameInputValidator implements InputValidator {
        @Override
        public boolean checkInput(String input) {
            return input != null && !input.trim().isEmpty();
        }

        @Override
        public boolean canClose(String input) {
            return checkInput(input);
        }
    }

    public VirtualFile selectTargetDirectory(Project project, String defaultPackage) {
        if(defaultPackage == null) {
            ProjectView projectView = ProjectView.getInstance(project);
            Object[] selectedElements = projectView.getCurrentProjectViewPane().getSelectedElements();
            if (selectedElements != null && selectedElements.length > 0) {
                PsiElement element = (PsiElement) selectedElements[0];
                VirtualFile file;
                if (element instanceof PsiFile)
                    file = ((PsiFile)element).getVirtualFile();
                else if (element instanceof PsiClass)
                    file = ((PsiClass)element).getContainingFile().getVirtualFile();
                else
                    file = element instanceof PsiDirectory ? ((PsiDirectory)element).getVirtualFile() : null;

                return file.isDirectory() ? file : file.getParent();
            }
        }

        PackageChooserDialog chooser = new PackageChooserDialog("Select package", project);
        chooser.selectPackage(defaultPackage);

        if (chooser.showAndGet()) {
            PsiPackage selectedPackage = chooser.getSelectedPackage();
            return selectedPackage != null ?
                    selectedPackage.getDirectories()[0].getVirtualFile() : null;
        }

        return null;
    }

    private String getPackageName(Project project, VirtualFile file) {
        if(!ProjectRootManager.getInstance(project).getFileIndex().isInSourceContent(file))
            return null;

        VirtualFile dir = file.isDirectory() ? file : file.getParent();
        PsiDirectory psiDir = PsiManager.getInstance(project).findDirectory(dir);
        return psiDir == null ? "" : JavaDirectoryService.getInstance().getPackage(psiDir).getQualifiedName();
    }

    public boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    public Command createCommand() {
        return null;
    }
}
