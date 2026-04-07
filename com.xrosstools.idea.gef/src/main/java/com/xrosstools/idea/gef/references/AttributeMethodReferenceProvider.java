package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;

public class AttributeMethodReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
        XmlAttributeValue attrValue = (XmlAttributeValue) element;
        String valueText = attrValue.getValue();
        String className = ImplementationUtil.getClassName(valueText);
        String methodName = ImplementationUtil.getMethodName(valueText);

        //We only support rename non default method
        if(ImplementationUtil.DEFAULT_METHOD.equals(methodName) ||
                methodName == null || methodName.trim().isEmpty() ||
                ImplementationUtil.findMethod(element.getProject(), className, methodName) == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        // +1 because of the initial " in attribute value
        int start = className.length() + ImplementationUtil.SEPARATOR.length() + 1;
        TextRange range  = new TextRange(start, start + methodName.length());
        PsiReference methodRef = new AttributeMethodReference(attrValue, className, methodName, range );

        return new PsiReference[]{methodRef};
    }
}
