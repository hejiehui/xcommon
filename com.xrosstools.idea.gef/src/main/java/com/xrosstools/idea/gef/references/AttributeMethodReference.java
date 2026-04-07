package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.IncorrectOperationException;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttributeMethodReference extends PsiReferenceBase<XmlAttributeValue> {
    private String className;
    private String methodName;

    public AttributeMethodReference(XmlAttributeValue attrValue, String className, String methodName, TextRange range) {
        super(attrValue, range);
        this.className = className;
        this.methodName = methodName;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ImplementationUtil.findMethod(getElement().getProject(), className, methodName);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newMethodName) throws IncorrectOperationException {
        XmlAttribute attribute = (XmlAttribute)myElement.getParent();

        String oldValue = myElement.getValue();
        String newValue = oldValue.replace(ImplementationUtil.SEPARATOR + methodName,
                ImplementationUtil.SEPARATOR + newMethodName);

        this.methodName = newMethodName;
        attribute.setValue(newValue);
        return myElement;
    }

    public Object[] getVariants() {
        return new Object[0];
    }
}
