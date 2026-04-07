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
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
        if (!(element instanceof XmlAttributeValue)) {
            return PsiReference.EMPTY_ARRAY;
        }
        XmlAttributeValue attrValue = (XmlAttributeValue) element;
        String valueText = attrValue.getValue();  // 不带引号的属性值
        String className = ImplementationUtil.getClassName(valueText);

        if(ImplementationUtil.findClass(element.getProject(), className) == null)
            return PsiReference.EMPTY_ARRAY;

        int start = 1;
        TextRange range  = new TextRange(start, start + className.length());

        return new PsiReference[]{new AttributeClassReference(attrValue, className, range )};
    }
}
