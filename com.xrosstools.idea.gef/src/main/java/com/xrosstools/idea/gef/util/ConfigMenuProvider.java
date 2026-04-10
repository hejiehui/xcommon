package com.xrosstools.idea.gef.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import com.xrosstools.idea.gef.actions.InputTextCommandAction;
import com.xrosstools.idea.gef.commands.CreatePropertyCommand;
import com.xrosstools.idea.gef.commands.RemovePropertyCommand;
import com.xrosstools.idea.gef.commands.RenamePropertyCommand;

import javax.swing.*;

import java.util.List;
import java.util.Set;

import static com.xrosstools.idea.gef.ContextMenuProvider.createItem;

public class ConfigMenuProvider {
    public static void buildPropertyMenu(Project project, JPopupMenu menu, String category, String propertyDisplayName, PropertyEntrySource properties) {
        JMenu createElementMenu = new JMenu(String.format("Create %s", propertyDisplayName));

        for(String typeName: DataTypeEnum.CONFIGURABLE_NAMES) {
            createElementMenu.add(createItem(new InputTextCommandAction(project, typeName, String.format("Set %s name", propertyDisplayName), "", new CreatePropertyCommand(category, properties, DataTypeEnum.findByDisplayName(typeName)))));
        }

        menu.add(createElementMenu);

        JMenu subRemove = new JMenu(String.format("Remove %s", propertyDisplayName));
        for(String name: properties.keySet(category)){
            subRemove.add(createItem(name, false, new RemovePropertyCommand(category, properties, name)));
        }
        menu.add(subRemove);

        JMenu subRename = new JMenu(String.format("Rename %s", propertyDisplayName));
        for(String name: properties.keySet(category)){
            subRename.add(createItem(new InputTextCommandAction(project, name, String.format("Rename %s", name), name, new RenamePropertyCommand(category, properties, name))));
        }

        menu.add(subRename);
    }

    public static void addPredefinedPropertiesMenu(Project project, JPopupMenu menu, PropertyEntrySource source, String propertiesCategory, String implementation) {
        List<String> propKeys = ImplementationUtil.getPropertyKeysInEDT(project, implementation);
        if (propKeys.isEmpty())
            return;

        Set<String> keys = source.keySet(propertiesCategory);

        JMenu subSetValue = new JMenu("Predefined properties");
        for (String key : propKeys) {
            if(keys.contains(key))
                continue;

            JMenu createProperty = new JMenu(key);

            for (String typeName : DataTypeEnum.CONFIGURABLE_NAMES) {
                CreatePropertyCommand cmd = new CreatePropertyCommand(propertiesCategory, source, DataTypeEnum.findByDisplayName(typeName));
                cmd.setInputText(key);
                createProperty.add(createItem(typeName, false, cmd));
            }
            subSetValue.add(createProperty);
        }
        menu.add(subSetValue);
    }
}
