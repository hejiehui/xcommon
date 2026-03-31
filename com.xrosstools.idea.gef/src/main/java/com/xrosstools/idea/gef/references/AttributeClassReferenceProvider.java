package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;

public class AttributeClassReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
        String text = ((XmlAttributeValue)element).getValue();
        String className = ImplementationUtil.getClassName(text);

        if(ImplementationUtil.findClass(element.getProject(), className) == null)
            return PsiReference.EMPTY_ARRAY;

        int start = 1;
        TextRange property = new TextRange(start, start + className.length()).shiftRight(element.getStartOffsetInParent());
        PsiReference classRef = new AttributeClassReference(element.getParent(), property);

        return new PsiReference[]{classRef};
    }
}
