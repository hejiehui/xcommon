package com.xrosstools.idea.gef.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xrosstools.idea.gef.util.XmlHelper.*;

public class ConfigXmlAccessor {
    public static final String PROPERTIES = "properties";
    public static final String PROPERTY = "property";

    private String category;
    private String propertyNodeName;
    private Map<DataTypeEnum, IPropertyDescriptor> descriptors = new HashMap<>();

    public String getCategory() {
        return category;
    }

    public static class ConfigPartAccessor<T> {
        public String name;
        public boolean byAttribute = true;

        public ConfigPartAccessor(String name) {
            this.name = name;
       }

        public String read(Node node) {
            return byAttribute ? getAttribute(node, name) : getChildNodeText(node, name);
        }

        public void write(Document doc, Element element, String value) {
            if(byAttribute)
                element.setAttribute(name, value);
            else
                createTextNode(doc, element, name, value);
        }
    }

    public ConfigXmlAccessor(String category, String propertyNodeName) {
        this.category = category;
        this.propertyNodeName = propertyNodeName;
    }

    public ConfigPartAccessor<String> keyAccessor = new ConfigPartAccessor("key");
    public ConfigPartAccessor<DataTypeEnum> typeAccessor = new ConfigPartAccessor("type");
    public ConfigPartAccessor<?> valueAccessor = new ConfigPartAccessor("value");

    public void readProperties(Node parentNode, PropertyEntrySource source) {
        List<Node> propertyNodes = getValidChildNodes(parentNode, PROPERTY);
        for(Node node: propertyNodes) {
            String name = keyAccessor.read(node);
            DataTypeEnum type = DataTypeEnum.findByDisplayName(typeAccessor.read(node));
            String strValue = valueAccessor.read(node);
            Object value = strValue == null ? null : getDescriptor(type).convertToProperty(strValue);
            source.register(category, new PropertyEntry(name, value, null).setType(type));
        }
    }

    public void writeProperties(Document doc, Element parentNode, PropertyEntrySource source) {
        for(String key: source.keySet(category)) {
            Element entryNode = createNode(doc, parentNode, propertyNodeName);
            PropertyEntry entry = source.get(category, key);
            keyAccessor.write(doc, entryNode, entry.getName());
            typeAccessor.write(doc, entryNode, entry.getType().getDisplayName());
            if (entry.get() != null)
                valueAccessor.write(doc, entryNode, String.valueOf(entry.get()));
        }
    }

    private IPropertyDescriptor getDescriptor(DataTypeEnum type) {
        IPropertyDescriptor descriptor = descriptors.get(type);
        if(descriptor == null) {
            descriptor = type.createDescriptor();
            descriptors.put(type, descriptor);
        }
        return descriptor;
    }
}
