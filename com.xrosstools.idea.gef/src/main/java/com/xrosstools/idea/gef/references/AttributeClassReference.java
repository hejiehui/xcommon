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

public class AttributeClassReference extends PsiReferenceBase<XmlAttributeValue> {
    private String className;

    public AttributeClassReference(XmlAttributeValue attrValue, String className, TextRange range) {
        super(attrValue, range);
        this.className = className;
    }

    @NotNull
    @Override
    public PsiElement resolve() {
        return ImplementationUtil.findClass(myElement.getProject(), className);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newClassName) throws IncorrectOperationException {
        XmlAttribute attribute = (XmlAttribute)myElement.getParent();

        String oldValue = myElement.getValue();
        String newValue = oldValue.replace(className, newClassName);
        attribute.setValue(newValue);
        this.className = newClassName;
        return myElement;
    }

    public Object[] getVariants() {
        return new Object[0];
    }
}
