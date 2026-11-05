package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.util.IPropertySource;

import java.util.ArrayList;
import java.util.List;

public class ImplementationMenuBuilder implements ImplementationMessages {
    public static final String SEPARATOR = "::";
    public static final String DEFAULT_METHOD = "#default";
    public static final String PROPERTY_KEY_PREFIX = "PROP_KEY";

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String getClassName(String implementation) {
        return implementation.contains(SEPARATOR) ? implementation.split(SEPARATOR)[0] : implementation;
    }
    public static String getMethodName(String implementation) {
        return implementation.contains(SEPARATOR) ? implementation.split(SEPARATOR)[1] : DEFAULT_METHOD;
    }

    public static String replaceMethodName(String implementation, String methodName) {
        if(DEFAULT_METHOD.equals(methodName) || methodName == null || methodName.trim().isEmpty())
            return implementation.split(SEPARATOR)[0];
        else
            return implementation.split(SEPARATOR)[0] + SEPARATOR + methodName;
    }

    public static void buildImplementationMenu(List<Action> menu, IPropertySource source, String propertyName, boolean allowMethod) {
        String implementation = (String)source.getPropertyValue(propertyName);
        if(isEmpty(implementation))
            menu.add(new ChangeImplementationAction(ASSIGN_ACTION_MSG, source, propertyName));
        else{
            menu.add(new ChangeImplementationAction(CHANGE_ACTION_MSG, source, propertyName));
            menu.add(new RemoveImplementationAction(source, propertyName));
            menu.add(new OpenImplementationAction(propertyName, implementation));

            if(allowMethod)
                buildReferenceMethodMenu(menu, source, propertyName, implementation);
        }
    }

    public static void buildReferenceMethodMenu(List<Action> menu, IPropertySource source, String propertyName, String implementation) {
        String methodName = getMethodName(implementation);

        ActionContainer methodMenu = new ActionContainer();
        methodMenu.setText(REFERENCE_METHOD_MSG + methodName);
        methodMenu.add(new ChangeMethodAction(source, propertyName, DEFAULT_METHOD, false));
        methodMenu.add(new ChangeMethodAction(source, propertyName));

        menu.add(methodMenu);
    }
}
