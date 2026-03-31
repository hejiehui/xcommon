package com.xrosstools.idea.gef.references;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlFile;

public abstract class AbstractReferenceContributor extends PsiReferenceContributor {
    private String extension;
    public AbstractReferenceContributor(String extension) {
        this.extension = extension;
    }

    public void registerTag(PsiReferenceRegistrar registrar, String tagName, boolean supportMethod) {
        XmlElementPattern pattern = XmlPatterns.xmlTag()
                .withName(tagName)
                .inFile(xmlFile().withName(string().endsWith("." + extension)));

        registrar.registerReferenceProvider(pattern, new TagClassReferenceProvider());
        if(supportMethod)
            registrar.registerReferenceProvider(pattern, new TagMethodReferenceProvider());

    }

    public void registerAttr(PsiReferenceRegistrar registrar, String parentTagName, String attribute, boolean supportMethod) {
        XmlAttributeValuePattern pattern = XmlPatterns.xmlAttributeValue()
                .withParent(
                        XmlPatterns.xmlAttribute().withName(attribute)
                                .withParent(XmlPatterns.xmlTag().withName(parentTagName)))
                .inFile(xmlFile().withName(string().endsWith("." + extension)));

        registrar.registerReferenceProvider(pattern, new AttributeClassReferenceProvider());
        if(supportMethod)
            registrar.registerReferenceProvider(pattern, new AttributeMethodReferenceProvider());
    }
}
