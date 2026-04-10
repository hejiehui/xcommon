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
    private String packageName;

    public AttributeClassReference(XmlAttributeValue attrValue, String className, TextRange range) {
        super(attrValue, range);
        this.className = className;
        int lastDot = className.lastIndexOf(".");
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        } else {
            packageName = "";  // 没有包名的情况
        }
    }

    @NotNull
    @Override
    public PsiElement resolve() {
        return ImplementationUtil.findClass(myElement.getProject(), className);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newClassName) throws IncorrectOperationException {
        XmlAttribute attribute = (XmlAttribute)myElement.getParent();

        String newQualifiedName = packageName.isEmpty() ? newClassName : packageName + '.' + newClassName;

        String oldValue = myElement.getValue();
        String newValue = oldValue.replace(className, newQualifiedName);
        attribute.setValue(newValue);
        this.className = newQualifiedName;
        return myElement;
    }

    public Object[] getVariants() {
        return new Object[0];
    }
}
