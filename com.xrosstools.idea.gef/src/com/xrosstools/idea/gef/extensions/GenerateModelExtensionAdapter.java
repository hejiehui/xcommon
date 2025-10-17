package com.xrosstools.idea.gef.extensions;

import java.util.function.Consumer;

public class GenerateModelExtensionAdapter implements GenerateModelExtension {
    @Override
    public boolean isGenerateModelSupported(String modelType) {
        return false;
    }

    @Override
    public void generateModel(String description, Consumer<String> callback) {}
}
