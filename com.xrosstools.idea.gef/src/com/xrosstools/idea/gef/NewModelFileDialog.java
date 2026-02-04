package com.xrosstools.idea.gef;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;

public class NewModelFileDialog extends DialogWrapper {
    private final Project project;
    private final String title;
    private final String message;
    private final Icon icon;
    private final String initialName;
    private final boolean showDescription;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JBCheckBox modeCheckBox;

    public NewModelFileDialog(Project project,
                              String title,
                              String message,
                              Icon icon,
                              String initialName,
                              boolean showDescription) {
        super(project, true);
        this.project = project;
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.initialName = initialName;
        this.showDescription = showDescription;

        init();
        setTitle(title);
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建消息标签
        JLabel messageLabel = new JLabel(message);
        if (icon != null) {
            messageLabel.setIcon(icon);
        }
        mainPanel.add(messageLabel, BorderLayout.NORTH);

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 名字输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputPanel.add(new JLabel(showDescription ? "Name:" : ""), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JBTextField(initialName);
        nameField.setPreferredSize(new Dimension(300, 30));
        inputPanel.add(nameField, gbc);

        // 描述部分（根据showDescription决定是否显示）
        if (showDescription) {
            // 描述标签
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            inputPanel.add(new JLabel("Description:"), gbc);

            // 描述输入框
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            descriptionArea = new JBTextArea(5, 30);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JBScrollPane(descriptionArea);
            scrollPane.setPreferredSize(new Dimension(300, 100));
            inputPanel.add(scrollPane, gbc);
        }

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        modeCheckBox = new JBCheckBox("Stream mode");
        mainPanel.add(modeCheckBox, BorderLayout.SOUTH);

        return mainPanel;
    }

    public String getName() {
        return nameField.getText();
    }

    public String getDescription() {
        return descriptionArea == null ? null : descriptionArea.getText();
    }

    public boolean isStreamMode() {
        return modeCheckBox.isSelected();
    }
}
