package com.xrosstools.idea.gef.extensions;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.ExtensionAdapter;

public class ExtensionManager {
    private static final String TOOLBAR_EXT = "com.xrosstools.idea.gef.toolbarExtension";
    private static final String GENERATE_MODEL_EXT = "com.xrosstools.idea.gef.generateModelExtension";

    private static final String OLD_EXTENSION_EXT = "com.xrosstools.idea.gef.xrossExtension";

    public static ToolbarExtension createToolbarExtension(EditorPanel panel) {
        ToolbarExtension extension = hasExtension(TOOLBAR_EXT) ?
                createInstance(TOOLBAR_EXT, new ToolbarExtensionAdapter()) :
                createInstance(OLD_EXTENSION_EXT, new ExtensionAdapter());
        extension.setEditPanel(panel);
        return extension;
    }

    public static GenerateModelExtension createNewModelFileExtension() {
        return createInstance(GENERATE_MODEL_EXT, new GenerateModelExtensionAdapter());
    }

    private static <T> T createInstance(String extId, T defaultImpl) {
        ExtensionPointName<T> ep = ExtensionPointName.create(extId);
        T extension = ep.getExtensionList().size() == 1 ? ep.getExtensionList().get(0) : defaultImpl;

        return extension;
    }

    private static boolean hasExtension(String extId) {
        ExtensionPointName<?> ep = ExtensionPointName.create(extId);
        return ep.getExtensionList().size() == 1;
    }
}
