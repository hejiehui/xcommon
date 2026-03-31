package com.xrosstools.idea.gef.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xrosstools.idea.gef.util.XmlHelper.*;

public class PropertySourceXmlAccessor {
    private List<String> attributes = new ArrayList<>();
    private List<String> nodes = new ArrayList<>();

    public PropertySourceXmlAccessor attributes(String...propNames) {
        for(String name: propNames)
            attributes.add(name);
        return this;
    }

    public PropertySourceXmlAccessor nodes(String...propNames) {
        for(String name: propNames)
            nodes.add(name);
        return this;
    }

    public void readProperties(Node node, IPropertySource source) {
        Map<String, IPropertyDescriptor> descriptorMap = getDescriptors(source);

        for(String propName: attributes) {
            String attributeValue = getAttribute(node, toAttrbuteName(propName));
            if (attributeValue == null) continue;

            //If it is String, we use value directly
            if(source.getPropertyValue(propName) instanceof String)
                source.setPropertyValue(propName,attributeValue);
            else
                source.setPropertyValue(propName, descriptorMap.get(propName).convertToProperty(attributeValue));
        }

        for(String propName: nodes) {
            String text = getChildNodeText(node, toAttrbuteName(propName));
            if (text == null) continue;

            source.setPropertyValue(propName, descriptorMap.get(propName).convertToProperty(text));
        }
    }

    public void writeProperties(Document doc, Element element, IPropertySource source) {
        for(IPropertyDescriptor descriptor: source.getPropertyDescriptors()) {
            if(descriptor.isVisible() == false) continue;

            String propName = (String)descriptor.getId();
            Object value = source.getPropertyValue(propName);

            if(value == null) continue;

            String elementName = toAttrbuteName(propName);
            if(attributes.contains(propName))
                element.setAttribute(elementName, String.valueOf(value));
            else if (nodes.contains(propName))
                createTextNode(doc, element, elementName, String.valueOf(value));
        }
    }

    private String toAttrbuteName(String propName) {
        return propName.toLowerCase().replace(" ", "_");
    }

    private Map<String, IPropertyDescriptor> getDescriptors(IPropertySource source) {
        Map<String, IPropertyDescriptor> descriptorMap = new HashMap<>();
        for(IPropertyDescriptor descriptor: source.getPropertyDescriptors()) {
            descriptorMap.put(descriptor.getId().toString(), descriptor);
        }
        return descriptorMap;
    }
}