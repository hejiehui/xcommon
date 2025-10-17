package com.xrosstools.idea.gef.extensions;

import java.util.function.Consumer;

public interface GenerateModelExtension {
    boolean isGenerateModelSupported(String modelType);
    void generateModel(String description, Consumer<String> callback);
}
