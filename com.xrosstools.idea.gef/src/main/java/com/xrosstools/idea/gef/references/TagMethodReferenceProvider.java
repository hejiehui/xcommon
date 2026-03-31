package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;

public class TagMethodReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
        String text = ((XmlTag)element).getValue().getTrimmedText();

        PsiElement target = TagClassReferenceProvider.findTextElement(element);

        String methodName = ImplementationUtil.getMethodName(text);
        String className = ImplementationUtil.getClassName(text);

        //We only support rename non default method
        if(ImplementationUtil.DEFAULT_METHOD.equals(methodName) || methodName == null || methodName.trim().length() == 0 || ImplementationUtil.findMethod(element.getProject(), className, methodName) == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        int start = 0 + className.length() + ImplementationUtil.SEPARATOR.length();
        TextRange property = new TextRange(start, start + methodName.length()).shiftRight(target.getStartOffsetInParent());

        PsiReference methodRef = new TagMethodReference(element, className, methodName, property);

        return new PsiReference[]{methodRef};
    }
}
