package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;

import java.awt.event.ActionEvent;

public abstract class AbstractCodeGenerator extends Action {
    private static final String JAVA = ".java";
    private final String title;
    private String packageName;
    private String className;

    public AbstractCodeGenerator(String title) {
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

    public Command createCommand() {
        return createCommand(String.format("%s.%s", packageName, className));
    }

    public Command createCommand(String fullClassName) {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // ----- 第1步：显示包选择对话框（只选包，不调用 getDirectories）-----
        String selectedPackage = getPlatformFacade().selectPackage(getDefaultPackage());
        if (selectedPackage == null) return;  // 用户取消

        // ----- 第2步：显示文件名输入对话框 -----
        String inputFileName = getPlatformFacade().choseFileName(title, getDefaultFileName());

        if (inputFileName == null || inputFileName.isEmpty()) return;
        final String fileName = inputFileName.endsWith(JAVA) ? inputFileName : inputFileName + JAVA;

        // ----- 第3步：显示其他需选项的对话框-----
        if(!additionalOptions()) return;

        // ----- 第4步：生成内容、检查文件等）-----
        generateFile(title, selectedPackage, fileName);
    }

    private void generateFile(String title, String packageName, String fileName) {
        className = fileName.replace(JAVA, "");
        String fileContent = getContent(packageName, className);
        getPlatformFacade().saveJavaFile(packageName, fileName, fileContent);
    }

    public boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
