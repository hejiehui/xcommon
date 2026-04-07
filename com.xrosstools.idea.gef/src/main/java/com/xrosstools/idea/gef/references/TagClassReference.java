package com.xrosstools.idea.gef.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.xrosstools.idea.gef.actions.ImplementationUtil;
import org.jetbrains.annotations.NotNull;

public class TagClassReference extends PsiReferenceBase<XmlTag> implements PsiReference {
    private String className;
    private String packageName;
    public TagClassReference(XmlTag tag, String className) {
        // 宿主是 XmlTag，引用范围设为整个标签体文本
        super(tag, getBodyTextRange(tag));
        this.className = className;
        int lastDot = className.lastIndexOf(".");
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        } else {
            packageName = "";  // 没有包名的情况
        }
    }

    // 返回标签体文本在 XmlTag 中的精确范围（简单可靠）
    private static TextRange getBodyTextRange(XmlTag tag) {
        String tagText = tag.getText();
        String bodyText = tag.getValue().getText();
        int start = tagText.indexOf(bodyText);
        if (start < 0) start = 0;
        return new TextRange(start, start + bodyText.length());
    }

    @Override
    public @NotNull PsiElement resolve() {
        return ImplementationUtil.findClass(myElement.getProject(), className);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        // 只修改标签体文本中的类名部分（简单替换）
        String oldBody = myElement.getValue().getText();
        String newQualifiedName = packageName.isEmpty() ? newElementName : packageName + '.' + newElementName;

        String newBody = oldBody.replace(className, newQualifiedName);
        myElement.getValue().setText(newBody);
        // 更新内部类名，以便后续 resolve 正确
        this.className = newQualifiedName;
        return myElement;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
