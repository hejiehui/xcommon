package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;

public class TagMethodReference extends PsiReferenceBase implements PsiReference {
    private String className;
    private String methodName;
    public TagMethodReference(PsiElement element, String className, String methodName, TextRange range) {
        super(element, range);
        this.className = className;
        this.methodName = methodName;
    }

    @NotNull
    @Override
    public PsiElement resolve() {
        return ImplementationUtil.findMethod(getElement().getProject(), className, methodName);
    }

    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        String newValue = getElement().getText().replace(methodName, newElementName);
        XmlTag newTag = XmlElementFactory.getInstance(getElement().getProject()).createTagFromText(newValue);
        getElement().replace(newTag);
        return newTag;
    }

    public Object[] getVariants() {
        return new Object[0];
    }
}
