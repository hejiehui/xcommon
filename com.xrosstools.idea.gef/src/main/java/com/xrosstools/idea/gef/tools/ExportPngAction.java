package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.GefIcons;
import com.xrosstools.idea.gef.actions.AbstractCodeGenerator;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExportPngAction extends AnAction {
    public static final String NAME = "Export";
    private EditorPanel editorPanel;

    public ExportPngAction(EditorPanel editorPanel) {
        super(NAME, "Export diagram to PNG file", GefIcons.EXPORT_PDF);
        this.editorPanel = editorPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        savePanelAsPNG(anActionEvent.getProject(), editorPanel.getUnitPanel());
    }

    public void savePanelAsPNG(Project project, JPanel panel) {
        // 1. 获取面板尺寸（确保面板已显示在屏幕上）
        int width = panel.getWidth();
        int height = panel.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Panel has invalid dimensions");
        }

        // 2. 创建缓冲图像（支持透明度）
        BufferedImage image = UIUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // 3. 获取图像绘图上下文并绘制面板内容
        java.awt.Graphics2D g2d = image.createGraphics();
        panel.printAll(g2d);  // 关键：将整个面板内容绘制到图像上
        g2d.dispose();       // 释放资源

        saveImageWithDialog(project, image, editorPanel.getFile().getNameWithoutExtension());
    }

    private boolean saveImageWithDialog(Project project,BufferedImage image,String defaultName) {
        // 1. 创建文件选择描述符（仅选择目录）
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory
                .createSingleFolderDescriptor()
                .withTitle("Select location")
                .withDescription("Please select directory for saving PNG");

        // 2. 弹出目录选择对话框
        VirtualFile selectedDir = FileChooser.chooseFile(descriptor, project, null);
        if (selectedDir == null) return false; // 用户取消

        // 3. 弹出文件名输入对话框
        String fileName = AbstractCodeGenerator.getFileName(project, "PNG file name", defaultName);
        if (fileName == null || fileName.trim().isEmpty()) return false; // 用户取消

        // 4. 确保文件名以 .png 结尾
        if (!fileName.toLowerCase().endsWith(".png")) {
            fileName += ".png";
        }

        try {
            // 5. 构建目标文件路径
            File targetFile = new File(selectedDir.getPath(), fileName);

            // 6. 检查文件是否已存在
            if (targetFile.exists()) {
                int overwrite = Messages.showYesNoDialog(
                        "File already exists, overwrite?",
                        "Confirm overwrite",
                        Messages.getQuestionIcon()
                );
                if (overwrite != Messages.YES) return false;
            }

            // 7. 保存图像
            ImageIO.write(image, "PNG", targetFile);
            return true;
        } catch (IOException e) {
            Messages.showErrorDialog("Failed to save: " + e.getMessage(), "Error");
            return false;
        }
    }
}
