package com.xrosstools.idea.gef.extensions;

import java.util.function.Consumer;

public interface GenerateModelExtension {
    boolean isGenerateModelSupported(String modelType);
    void generateModel(String description, Consumer<String> callback);

    default void generateModel(String description, Consumer<String> callback, boolean stream) {
        generateModel(description, callback);
    }
}
