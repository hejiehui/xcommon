package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;

public class TagClassReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
        XmlTagValue value = ((XmlTag)element).getValue();
        String text = value.getText();

        PsiElement target = findTextElement(element);
        String className = ImplementationUtil.getClassName(text);

        if(ImplementationUtil.findClass(element.getProject(), className) == null)
            return PsiReference.EMPTY_ARRAY;

        int start = 0;
        TextRange property = new TextRange(start, start + className.length());
        PsiReference classRef = new TagClassReference(target, className, property);

        return new PsiReference[]{classRef};
    }

    public static PsiElement findTextElement(PsiElement element) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof XmlText) {
                return child;
            }
        }
        return null;
    }
}
