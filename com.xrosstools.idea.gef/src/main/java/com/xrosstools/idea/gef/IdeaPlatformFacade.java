package com.xrosstools.idea.gef;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.UIUtil;
import com.xrosstools.idea.gef.actions.PlatformFacade;
import com.xrosstools.idea.gef.actions.tools.SearchModelAction;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.xrosstools.idea.gef.actions.ImplementationMenuBuilder.*;

public class IdeaPlatformFacade implements PlatformFacade {
    private Project project;
    private EditorPanel editorPanel;

    public IdeaPlatformFacade(Project project, EditorPanel editorPanel) {
        this.project = project;
        this.editorPanel = editorPanel;
    }

    @Override
    public void showErrorDialog(String dialogTitle, String dialogMessage) {
        Messages.showErrorDialog(dialogMessage, dialogTitle);
    }

    @Override
    public String showInputDialog(String dialogTitle, String dialogMessage, Icon icon, String initialValue) {
        Messages.InputDialog dialog = new Messages.InputDialog(dialogMessage, dialogTitle, icon, initialValue, new InputValidator() {
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

        return dialog.getExitCode() == 0 ? dialog.getInputString() : null;
    }

    public void showFoundDialog(List<SearchModelAction.Entry> found) {
        new ListDialog(project, found, editorPanel.getEditorInteraction()).show();
    }

    @Override
    public String assignImpl(String currentImpl) {
        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser(currentImpl);
        PsiClass selected = null;
        chooser.showDialog();
        selected = chooser.getSelected();

        if(selected == null)
            return currentImpl;

        return selected.getQualifiedName();
    }

    @Override
    public void openImpl(String implementation) {
        String className = getClassName(implementation);
        String methodName = getMethodName(implementation);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            PsiClass psiClass = findClass(project, className);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (null == psiClass) {
                    Messages.showErrorDialog("Can not open " + className, "Error");
                }else {
                    if(DEFAULT_METHOD.equals(methodName))
                        OpenSourceUtil.navigate(psiClass);
                    else {
                        PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);
                        if(methods.length != 0)
                            OpenSourceUtil.navigate(methods[0]);
                        else
                            Messages.showErrorDialog(String.format("Can't find \"%s\" for class %s", methodName, className), "Method not found");
                    }
                }
            });
        });
    }

    @Override
    public String selectPackage(String defaultPackage) {
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
        return chooser.showAndGet() ? chooser.getSelectedPackage().getQualifiedName() : null;
    }

    @Override
    public String choseFileName(String dialogTitle, String defaultName) {
        return Messages.showInputDialog(
                project,
                "Enter file name:",
                dialogTitle,
                Messages.getQuestionIcon(),
                defaultName,
                new FileNameInputValidator());
    }

    @Override
    public void saveJavaFile(String packageName, String fileName, String fileContent) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            // 耗时操作1：通过 PsiPackage 获取 VirtualFile（触发索引）
            VirtualFile targetDir = ApplicationManager.getApplication().runReadAction((Computable<VirtualFile>)() -> {
                //Get dir from packageName
                PsiPackage selectedPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
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

    public static PsiClass findClass(Project project, String className) {
        return ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> {
            return JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
        });
    }

    private void writeFile(VirtualFile targetDir, String fileName, String fileContent, PsiFile existingFile) {
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

    public void saveAsPNG() {
        BufferedImage image = getImage();
        File location = selectLocation();

        if(location == null)
            return;

        String defaultName = editorPanel.getFile().getNameWithoutExtension();
        String fileName = choseFileName("PNG file name", defaultName);
        saveAsPNG(location, image, fileName);
    }

    private BufferedImage getImage() {
        JPanel panel = editorPanel.getUnitPanel();
        // 1. 获取面板尺寸（确保面板已显示在屏幕上）
        int width = panel.getWidth();
        int height = panel.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Panel has invalid dimensions");
        }

        // 2. 创建缓冲图像（支持透明度）
        BufferedImage image = UIUtil.createImage(panel, width, height, BufferedImage.TYPE_INT_ARGB);

        // 3. 获取图像绘图上下文并绘制面板内容
        java.awt.Graphics2D g2d = image.createGraphics();
        panel.printAll(g2d);  // 关键：将整个面板内容绘制到图像上
        g2d.dispose();       // 释放资源

        return image;
    }

    private File selectLocation() {
        // 1. 创建文件选择描述符（仅选择目录）
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory
                .createSingleFolderDescriptor()
                .withTitle("Select Location")
                .withDescription("Please select directory for saving PNG");

        // 2. 弹出目录选择对话框
        VirtualFile selectedDir = FileChooser.chooseFile(descriptor, project, null);
        return selectedDir == null ? null : VfsUtilCore.virtualToIoFile(selectedDir);
    }

    private void saveAsPNG(File location, BufferedImage image,String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return; // 用户取消

        // 4. 确保文件名以 .png 结尾
        if (!fileName.toLowerCase().endsWith(".png")) {
            fileName += ".png";
        }

        try {
            // 5. 构建目标文件路径
            File targetFile = new File(location, fileName);

            // 6. 检查文件是否已存在
            if (targetFile.exists()) {
                int overwrite = Messages.showYesNoDialog(
                        "File already exists, overwrite?",
                        "Confirm Overwrite",
                        Messages.getQuestionIcon()
                );
                if (overwrite != Messages.YES) return;
            }

            // 7. 保存图像
            ImageIO.write(image, "PNG", targetFile);
            openImage(targetFile);
        } catch (IOException e) {
            Messages.showErrorDialog("Failed to save: " + e.getMessage(), "Error");
        }
    }

    private void openImage(File targetFile) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(targetFile);
            } catch (IOException e) {
                // 打开失败时仅记录日志或提示，不影响主流程
                Messages.showInfoMessage("Cannot open file: " + e.getMessage(), "Open Failed");
            }
        } else {
            Messages.showInfoMessage("Desktop not supported, cannot open file automatically.", "Open Unavailable");
        }
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
}
