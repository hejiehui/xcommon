package com.xrosstools.idea.gef.actions.codegen;

public class SimpleImplGenerator implements ImplementationGenerator {
    public static final String[] EMPTY = new String[]{};
    private String interfaceName;
    private String[] imports = EMPTY;
    private String[] fields = EMPTY;
    private String[] methods = EMPTY;


    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public <T> String[] generateImports(T model) {
        return imports;
    }

    @Override
    public <T> String[] generateFields(T model) {
        return fields;
    }

    @Override
    public <T> String[] generateMethods(T model) {
        return methods;
    }

    public SimpleImplGenerator setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public SimpleImplGenerator setImports(String...imports) {
        this.imports = imports;
        return this;
    }

    public SimpleImplGenerator setFields(String...fields) {
        this.fields = fields;
        return this;
    }

    public SimpleImplGenerator setMethods(String...methods) {
        this.methods = methods;
        return this;
    }
}
