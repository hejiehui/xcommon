package com.xrosstools.idea.gef.actions.codegen;

import com.intellij.openapi.project.Project;
import com.xrosstools.idea.gef.actions.AbstractCodeGenerator;
import com.xrosstools.idea.gef.actions.CodeGenHelper;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.apache.commons.lang.text.StrBuilder;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.xrosstools.idea.gef.actions.CodeGenHelper.getValue;
import static com.xrosstools.idea.gef.actions.CodeGenHelper.replace;

public abstract class AbstractOptionalGeneratorAction extends AbstractCodeGenerator {

    public static final String PACKAGE_PLACEHOLDER = "!PACKAGE!";
    public static final String IMPORTS_PLACEHOLDER = "!IMPORTS!";
    public static final String CLASS_NAME_PLACEHOLDER = "!CLASS_NAME!";
    public static final String INTERFACES_PLACEHOLDER = "!INTERFACES!";
    public static final String FIELDS_PLACEHOLDER = "!FIELDS!";
    public static final String METHODS_PLACEHOLDER = "!METHODS!";

    public static final String INTERFACE_SEPARATOR = ", ";
    public static final String IMPORT_TEMPLATE = "import %s;\n";
    public static final String INTERFACE_TEMPLATE = "%s" + INTERFACE_SEPARATOR;
    public static final String FIELD_TEMPLATE = "    %s\n";
    public static final String METHOD_TEMPLATE = "%s\n\n";

    private IPropertySource source;
    private String classType;
    private List<String> selectedOptions;

    public abstract String getDefaultClassName(IPropertySource source, String classType);

    public abstract String getOptionMessage(IPropertySource source, String classType);

    public abstract String[] getOptions(IPropertySource source, String classType);

    public abstract List<ImplementationGenerator> getGenerators(IPropertySource source, String classType, List<String> selectedOptions);

    public abstract Command createCommand(IPropertySource source, String classType, String fullClassName, List<String> selectedOptions);

    @Override
    public Command createCommand(String fullClassName) {
        return createCommand(source, classType, fullClassName, selectedOptions);
    }

    public AbstractOptionalGeneratorAction(Project project, IPropertySource source, String classType){
        super(project, SimpleCodeGenAction.getTitle(classType));
        setText(SimpleCodeGenAction.getTitle(classType));
        this.source = source;
        this.classType = classType;
    }

    public StringBuffer getTemplate() {
        return CodeGenHelper.getTemplate("/template/CodeTemplate.txt", AbstractOptionalGeneratorAction.class);
    }

    public String getDefaultFileName() {
        return getDefaultClassName(source, classType);
    }

    @Override
    public String getContent(String packageName, String fileName) {
        StringBuffer codeBuf = getTemplate();

        selectedOptions = CodeGenOptionsDialog.showDialog(getText(), getOptionMessage(source, classType), getOptions(source, classType));

        Set<String> imports = new LinkedHashSet<>();
        Set<String> interfaces = new LinkedHashSet<>();
        Set<String> fields = new LinkedHashSet<>();
        Set<String> methods = new LinkedHashSet<>();

        List<ImplementationGenerator> generators = getGenerators(source, classType, selectedOptions);
        for(ImplementationGenerator generator: generators) {
            generate(generator, imports, interfaces, fields, methods);
        }

        replace(codeBuf, PACKAGE_PLACEHOLDER, getValue(packageName));
        replace(codeBuf, IMPORTS_PLACEHOLDER, generateBy(IMPORT_TEMPLATE, imports));
        replace(codeBuf, CLASS_NAME_PLACEHOLDER, fileName);
        replace(codeBuf, INTERFACES_PLACEHOLDER, generateInterfaces(interfaces));
        replace(codeBuf, FIELDS_PLACEHOLDER, generateBy(FIELD_TEMPLATE, fields));
        replace(codeBuf, METHODS_PLACEHOLDER, generateBy(METHOD_TEMPLATE, methods));

        return codeBuf.toString();
    }

    protected String generateInterfaces(Set<String> interfaces) {
        String interfaceDeclare = generateBy(INTERFACE_TEMPLATE, interfaces);
        return interfaceDeclare.endsWith(INTERFACE_SEPARATOR) ? interfaceDeclare.substring(0, interfaceDeclare.length() - INTERFACE_SEPARATOR.length()) : interfaceDeclare;
    }

    protected String generateBy(String template, Set<String> contents) {
        StrBuilder sb = new StrBuilder();
        for(String content: contents)
            sb.append(String.format(template, content));
        return sb.toString();
    }

    protected void generate(ImplementationGenerator generator, Set<String> imports, Set<String> interfaces, Set<String> fields, Set<String> methods) {
        addToSet(imports, generator.generateImports(source));
        interfaces.add(generator.getInterfaceName());
        addToSet(fields, generator.generateFields(source));
        addToSet(methods, generator.generateMethods(source));
    }

    protected void addToSet(Set<String> sList, String[] sArray) {
        sList.addAll(Arrays.asList(sArray));
    }
}
