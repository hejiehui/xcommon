package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CreatePropertyCommand;
import com.xrosstools.idea.gef.commands.RemovePropertyCommand;
import com.xrosstools.idea.gef.commands.RenamePropertyCommand;
import com.xrosstools.idea.gef.util.DataTypeEnum;
import com.xrosstools.idea.gef.util.PropertyEntrySource;

import java.util.List;

public class PropertyMenuBuilder {
    public static void buildPropertyMenu(List<Action> menu, String category, String propertyDisplayName, PropertyEntrySource properties) {
        ActionContainer createElementMenu = new ActionContainer(String.format("Create %s", propertyDisplayName));

        for(String typeName: DataTypeEnum.CONFIGURABLE_NAMES) {
            createElementMenu.add(new InputTextCommandAction(typeName, String.format("Set %s name", propertyDisplayName), "", new CreatePropertyCommand(category, properties, DataTypeEnum.findByDisplayName(typeName))));
        }

        menu.add(createElementMenu);

        ActionContainer subRemove = new ActionContainer(String.format("Remove %s", propertyDisplayName));
        for(String name: properties.keySet(category)){
            subRemove.add(createAction(name, false, new RemovePropertyCommand(category, properties, name)));
        }
        menu.add(subRemove);

        ActionContainer subRename = new ActionContainer(String.format("Rename %s", propertyDisplayName));
        for(String name: properties.keySet(category)){
            subRename.add(new InputTextCommandAction(name, String.format("Rename %s", name), name, new RenamePropertyCommand(category, properties, name)));
        }

        menu.add(subRename);
    }

    public static void addPredefinedPropertiesMenu(List<Action> menu, PropertyEntrySource source, String propertiesCategory, String implementation) {
        ActionContainer subSetValue = new ActionContainer("Predefined properties");
        subSetValue.add(new AddPredefinedProeprtyAction(source.keySet(propertiesCategory)));
        menu.add(subSetValue);
    }

    public static Action createAction(String text, boolean checked, Command command) {
        return new CommandAction(text, checked, command);
    }
}
