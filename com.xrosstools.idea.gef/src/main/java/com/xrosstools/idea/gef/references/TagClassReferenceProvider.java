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
        if (!(element instanceof XmlTag)) {
            return PsiReference.EMPTY_ARRAY;
        }

        XmlTag tag = (XmlTag) element;
        String text = tag.getValue().getText();
        String className = ImplementationUtil.getClassName(text);

        if (className == null || className.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (ImplementationUtil.findClass(element.getProject(), className) == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        // 直接返回附着在 XmlTag 上的引用，范围是整个标签体文本
        return new PsiReference[]{new TagClassReference(tag, className)};
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
