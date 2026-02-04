package com.xrosstools.idea.gef.actions.codegen;

public interface ImplementationGenerator {
    String getInterfaceName();
    <T> String[] generateImports(T model);
    <T> String[] generateFields(T model);
    <T> String[] generateMethods(T model);
}
