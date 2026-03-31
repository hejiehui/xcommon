package com.xrosstools.idea.gef.actions.codegen;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CodeGenOptionsDialog extends DialogWrapper {
    private final String message;
    private final String[] options;
    private final List<JCheckBox> checkBoxes = new ArrayList<>();

    public CodeGenOptionsDialog(String title, String message, String[] options) {
        super(true); // 设置为模态对话框
        this.message = message;
        this.options = options;
        setTitle(title);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (options == null || options.length == 0) {
            JPanel panel = new JPanel(new GridLayout(1, 1, 0, 0));
            JBLabel noOptionsLabel = new JBLabel("没有可用的选项");
            noOptionsLabel.setForeground(Color.GRAY);
            panel.add(noOptionsLabel);
            return panel;
        }

        JPanel panel = new JPanel(new GridLayout(options.length + 2, 1, 10, 0));

        // 添加消息标签
        JBLabel messageLabel = new JBLabel(message);
        messageLabel.setBorder(JBUI.Borders.emptyBottom(10));
        panel.add(messageLabel);

        // 添加分隔线
        JSeparator separator = new JSeparator();
        panel.add(separator);

        for (String option : options) {
            JBCheckBox checkBox = new JBCheckBox(option);
            checkBoxes.add(checkBox);
            panel.add(checkBox);
        }

//        panel.setPreferredSize(new Dimension(500, 300));
        return panel;
    }

    /**
     * 获取选中的选项
     * @return 选中的选项列表
     */
    public List<String> getSelectedOptions() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selected.add(options[i]);
            }
        }
        return selected;
    }

    /**
     * 获取选中的选项索引
     * @return 选中的选项索引列表
     */
    public List<Integer> getSelectedIndices() {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selected.add(i);
            }
        }
        return selected;
    }

    /**
     * 设置默认选中的选项
     * @param indices 要选中的索引列表
     */
    public void setDefaultSelections(List<Integer> indices) {
        if (indices != null) {
            for (int index : indices) {
                if (index >= 0 && index < checkBoxes.size()) {
                    checkBoxes.get(index).setSelected(true);
                }
            }
        }
    }

    /**
     * 设置全选/取消全选
     * @param selected 是否全选
     */
    public void setAllSelected(boolean selected) {
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setSelected(selected);
        }
    }

    @Override
    protected void doOKAction() {
        // 在关闭前可以添加一些验证逻辑
        super.doOKAction();
    }

    /**
     * 显示对话框并获取结果
     * @return 选中的选项列表，如果取消则为空列表
     */
    public static List<String> showDialog(String title, String message, String[] options) {
        if(options == null || options.length == 0)
            return new ArrayList<>();

        CodeGenOptionsDialog dialog = new CodeGenOptionsDialog(title, message, options);
        if (dialog.showAndGet()) {
            return dialog.getSelectedOptions();
        }
        return new ArrayList<>();
    }

    /**
     * 显示对话框并获取结果，可设置默认选中项
     * @return 选中的选项索引列表，如果取消则为空列表
     */
    public static List<Integer> showDialogWithDefaults(String title, String message, String[] options, List<Integer> defaultSelections) {
        CodeGenOptionsDialog dialog = new CodeGenOptionsDialog(title, message, options);
        dialog.setDefaultSelections(defaultSelections);
        if (dialog.showAndGet()) {
            return dialog.getSelectedIndices();
        }
        return new ArrayList<>();
    }
}