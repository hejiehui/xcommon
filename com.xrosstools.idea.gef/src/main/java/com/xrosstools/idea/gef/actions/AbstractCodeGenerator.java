package com.xrosstools.idea.gef.actions;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.xrosstools.idea.gef.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;

public abstract class AbstractCodeGenerator extends Action {
    private static final String JAVA = ".java";
    private final Project project;
    private final String title;
    private String packageName;
    private String className;

    public AbstractCodeGenerator(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    public abstract String getContent(String packageName, String fileName);

    public String getDefaultPackage() {
        return null;
    }

    public String getDefaultFileName() {
        return null;
    }

    /**
     * Allow subclass shows and collect additional information
     * @return if continue processing
     */
    public boolean additionalOptions() {return true;}

    public Command createCommand(String fullClassName) {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // ----- 第1步：在 EDT 上显示包选择对话框（只选包，不调用 getDirectories）-----
        PsiPackage selectedPackage = selectPackage(project, getDefaultPackage());
        if (selectedPackage == null) return;  // 用户取消

        // ----- 第2步：在 EDT 上显示文件名输入对话框 -----
        String inputFileName = getFileName(project, title, getDefaultFileName());
        if (inputFileName == null || inputFileName.isEmpty()) return;
        final String fileName = inputFileName.endsWith(JAVA) ? inputFileName : inputFileName + JAVA;

        // ----- 第3步：显示其他需在EDT上显示的对话框-----
        if(!additionalOptions()) return;

        // ----- 第4步：启动后台任务，执行所有耗时操作（获取 VirtualFile、生成内容、检查文件等）-----
        generateFile(project, title, selectedPackage, fileName);
    }

    private PsiPackage selectPackage(Project project, String defaultPackage) {
        // 1. 准备默认包名（简单情况，直接读当前文件包名，这个不涉及索引，可以放在 EDT）
        if (defaultPackage == null) {
            FileEditorManager editorManager = FileEditorManager.getInstance(project);
            VirtualFile[] selected = editorManager.getSelectedFiles();
            if (selected.length > 0) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(selected[0]);
                if (psiFile instanceof PsiJavaFile) {
                    defaultPackage = ((PsiJavaFile) psiFile).getPackageName();
                }
            }
        }

        PackageChooserDialog chooser = new PackageChooserDialog("Select Package", project);
        if (defaultPackage != null) chooser.selectPackage(defaultPackage);
        return chooser.showAndGet() ? chooser.getSelectedPackage() : null;
    }

    private void generateFile(Project project, String title, PsiPackage selectedPackage, String fileName) {
        className = fileName.replace(JAVA, "");
        packageName = selectedPackage.getQualifiedName();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            // 耗时操作1：通过 PsiPackage 获取 VirtualFile（触发索引）
            VirtualFile targetDir = ApplicationManager.getApplication().runReadAction((Computable<VirtualFile>)() -> {
                PsiDirectory[] dirs = selectedPackage.getDirectories();
                return dirs.length > 0 ? dirs[0].getVirtualFile() : null;
            });
            if (targetDir == null) return;

            // 耗时操作2：获取包名、检查文件是否已存在（读操作）（纯计算，但放在 readAction 更安全）
            PsiFile existingFile = ApplicationManager.getApplication().runReadAction((Computable<PsiFile>)() -> {
                PsiDirectory directory = PsiManager.getInstance(project).findDirectory(targetDir);
                return directory != null ? directory.findFile(fileName) : null;
            });

            ApplicationManager.getApplication().invokeLater(() -> {
                //生成文件内容
                String fileContent = getContent(packageName, className);
                if (fileContent == null) return;
                if (existingFile != null) {
                    int result = Messages.showOkCancelDialog(project,
                            fileName + " already exists!",
                            "Replace File",
                            "Replace",
                            "Cancel",
                            Messages.getQuestionIcon());
                    if (result != Messages.OK) return;
                }

                writeFile(targetDir, fileName, fileContent, existingFile);
            });
        });
    }

    private void writeFile(VirtualFile targetDir, String fileName, String fileContent, PsiFile existingFile) {
        //5. execute command if any
        super.actionPerformed(null);

        //6. Write to file
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile file;
            if (existingFile != null) {
                Document document = PsiDocumentManager.getInstance(project).getDocument(existingFile);
                document.setText(fileContent);
                PsiDocumentManager.getInstance(project).commitDocument(document);
                file = existingFile;
                FileEditorManager.getInstance(project).openFile(file.getVirtualFile(), true);
            } else {
                try {
                    VirtualFile newFile = targetDir.createChildData(project, fileName);
                    newFile.setBinaryContent(fileContent.getBytes());
                    FileEditorManager.getInstance(project).openFile(newFile, true);
                } catch (Throwable ex) {
                    throw new IllegalStateException("Can not save document " + fileName, ex);
                }
            }
        });
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

    /**
     * 在 EDT 上直接运行
     */
    public PsiClass chooseClass(String title, @Nullable String defaultClassName) {
        final PsiClass[] initialSelection = new PsiClass[1];

        if (defaultClassName != null && !defaultClassName.isEmpty()) {
            // 1. 后台线程：查找默认类（索引查询）
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                initialSelection[0] = ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> {
                    PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
                    PsiClass[] classes = cache.getClassesByName(defaultClassName, GlobalSearchScope.allScope(project));
                    return classes.length > 0 ? classes[0] : null;
                });
            }, title, true, project);  // true = 可取消
        }

        final PsiClass finalInitialSelection = initialSelection[0];
        TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(project);
        TreeClassChooser chooser = chooserFactory.createWithInnerClassesScopeChooser(
                title,
                GlobalSearchScope.allScope(project),
                ClassFilter.ALL,
                finalInitialSelection
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

    public boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public Command createCommand() {
        return createCommand(String.format("%s.%s", packageName, className));
    }
}
