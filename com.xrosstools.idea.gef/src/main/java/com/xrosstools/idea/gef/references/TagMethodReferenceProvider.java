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
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
        if (!(element instanceof XmlTag)) {
            return PsiReference.EMPTY_ARRAY;
        }

        XmlTag tag = (XmlTag) element;
        String bodyText = tag.getValue().getText(); // 原始标签体文本（含空白）
        String methodName = ImplementationUtil.getMethodName(bodyText);
        String className = ImplementationUtil.getClassName(bodyText);

        //We only support rename non default method
        if(ImplementationUtil.DEFAULT_METHOD.equals(methodName) ||
                methodName == null || methodName.trim().isEmpty() ||
                ImplementationUtil.findMethod(element.getProject(), className, methodName) == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        // 计算方法名在标签体中的起始位置
        int methodStartInBody = bodyText.lastIndexOf(methodName);

        // 计算标签体在整个标签文本中的起始偏移
        int bodyStartInTag = tag.getText().indexOf(bodyText);
        int methodStartInTag = bodyStartInTag + methodStartInBody;

        TextRange range = new TextRange(methodStartInTag, methodStartInTag + methodName.length());

        return new PsiReference[]{new TagMethodReference(tag, className, methodName, range)};
    }
}
