package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.actions.tools.SearchModelAction;

import javax.swing.*;
import java.util.List;

public interface PlatformFacade {
    void showErrorDialog(String dialogTitle, String dialogMessage);
    String showInputDialog(String dialogTitle, String dialogMessage, Icon icon, String initialValue);
    void showFoundDialog(List<SearchModelAction.Entry> found);

    String assignImpl(String currentImpl);
    void openImpl(String implementation);

    String selectPackage(String packageName);
    String choseFileName(String dialogTitle, String defaultName);

    void saveJavaFile(String packageName, String fileName, String fileContent);

    void saveAsPNG();
}
