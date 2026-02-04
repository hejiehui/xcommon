package com.xrosstools.idea.gef.actions.codegen;

import com.intellij.openapi.project.Project;
import com.xrosstools.idea.gef.actions.AbstractCodeGenerator;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;
import com.xrosstools.idea.gef.util.IPropertySource;

import static com.xrosstools.idea.gef.actions.CodeGenHelper.*;

public class SimpleCodeGenAction extends AbstractCodeGenerator {
    public static final String PACKAGE_PLACEHOLDER = "!PACKAGE!";
    public static final String CLASS_NAME_PLACEHOLDER = "!CLASS_NAME!";

    private String template;
    private IPropertySource source;
    private String property;
    private String defaultName;

    public SimpleCodeGenAction(Project project, String template, IPropertySource source, String property, String defaultName){
        super(project, getTitle(property));
        setText(getTitle(property));
        this.template = template;
        this.source = source;
        this.property = property;
        this.defaultName = defaultName;
    }

    public String getDefaultFileName() {
        return defaultName;
    }

    public Command createCommand(String fullClassName) {
        return new PropertyChangeCommand(source, property, fullClassName);
    }

    @Override
    public String getContent(String packageName, String fileName) {
        StringBuffer codeBuf = new StringBuffer(template);

        replace(codeBuf, PACKAGE_PLACEHOLDER, getValue(packageName));
        replace(codeBuf, CLASS_NAME_PLACEHOLDER, fileName);

        return codeBuf.toString();
    }

    public static String getTitle(String property) {
        return "Generate " + property;
    }
}
