package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TagMethodReference extends PsiReferenceBase<XmlTag> {
    private String className;
    private String methodName;

    public TagMethodReference(XmlTag tag, String className, String methodName, TextRange range) {
        super(tag, range);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public @Nullable PsiElement resolve() {
        return ImplementationUtil.findMethod(myElement.getProject(), className, methodName);
    }

    public PsiElement handleElementRename(@NotNull String newMethodName) throws IncorrectOperationException {
        String oldBody = myElement.getValue().getText();

        String newBody = oldBody.replace(ImplementationUtil.SEPARATOR + methodName,
        ImplementationUtil.SEPARATOR + newMethodName);
        myElement.getValue().setText(newBody);
        this.methodName = newMethodName;
        return myElement;
    }

    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
