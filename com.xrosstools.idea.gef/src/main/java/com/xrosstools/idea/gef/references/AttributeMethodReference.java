package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.Nullable;

public class AttributeMethodReference extends PsiReferenceBase implements PsiReference{
    private String className;
    private String methodName;

    public AttributeMethodReference(PsiElement element, String className, String methodName, TextRange range) {
        super(element, range);
        this.className = className;
        this.methodName = methodName;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ImplementationUtil.findMethod(getElement().getProject(), className, methodName);
    }

    public Object[] getVariants() {
        return new Object[0];
    }
}
