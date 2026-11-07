package com.xrosstools.idea.gef.actions;

import com.google.gson.JsonObject;
import com.xrosstools.gef.lsp.LspEditorFacade;
import com.xrosstools.idea.gef.util.DataTypeEnum;

import java.util.Set;

public class AddPredefinedProeprtyAction extends Action {
    public static final String CONFIGURABLE_TYPES = "configurableTypes";
    public static final String CLASS_NAME = "className";
    public static final String DEFINED_KEYS = "definedKeys";
    private String className;
    private Set<String> definedKeys;

    public AddPredefinedProeprtyAction(String className, Set<String> definedKeys) {
        this.className = className;
        this.definedKeys = definedKeys;
    }

    public JsonObject getParams() {
        JsonObject params = new JsonObject();
        params.addProperty(CLASS_NAME, className);
        params.add(DEFINED_KEYS, LspEditorFacade.GSON.toJsonTree(definedKeys));
        params.add(CONFIGURABLE_TYPES, LspEditorFacade.GSON.toJsonTree(DataTypeEnum.CONFIGURABLE_NAMES));
        return params;
    }
}
