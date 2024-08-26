package com.xrosstools.idea.gef.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertySourceXmlRegister<T> {
    private Map<T, PropertySourceXmlAccessor> accessorMap= new ConcurrentHashMap<>();

    public PropertySourceXmlAccessor register(T key) {
        PropertySourceXmlAccessor accessor = new PropertySourceXmlAccessor();
        accessorMap.put(key, accessor);
        return accessor;
    }

    /**
     * In case multiple source share same accessor
     */
    public PropertySourceXmlAccessor register(T key, PropertySourceXmlAccessor accessor) {
        accessorMap.put(key, accessor);
        return accessor;
    }

    public boolean contains(T key) {
        return accessorMap.containsKey(key);
    }

    public PropertySourceXmlAccessor get(T key) {
        return accessorMap.get(key);
    }

    public void readProperties(T key, Node node, IPropertySource source) {
        get(key).readProperties(node, source);
    }

    public void writeProperties(Document doc, T key, Element element, IPropertySource source) {
        get(key).writeProperties(doc, element, source);
    }
}
