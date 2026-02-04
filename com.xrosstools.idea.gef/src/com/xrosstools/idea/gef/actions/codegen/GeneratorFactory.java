package com.xrosstools.idea.gef.actions.codegen;

import java.util.HashMap;
import java.util.Map;

public class GeneratorFactory<T> {
    private Map<T, ImplementationGenerator> generatorRegistry = new HashMap<>();

    public void register(T type, ImplementationGenerator implementationlGenerator) {
        generatorRegistry.put(type, implementationlGenerator);
    }

    public ImplementationGenerator getGenerator(T type) {
        return generatorRegistry.get(type);
    }
}
