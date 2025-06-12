package com.xrosstools.idea.gef.util;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class PropertyEntrySource extends PropertySource {
    private Map<String, PropertyEntry> defaultEntryMap = new LinkedHashMap<>();
    private Map<String, Map<String, PropertyEntry>> categoryEntryMap = new LinkedHashMap<>();

    public void register(String category, PropertyEntry entry) {
        register(entry.setCategory(category));
    }

    public void register(PropertyEntry entry) {
        register(entry, entry.getDescriptor(), ()->true);
    }

    public void register(PropertyEntry entry, BooleanSupplier condition) {
        register(entry, entry.getDescriptor(), condition);
    }

    public void register(PropertyEntry entry, IPropertyDescriptor descriptor) {
         register(entry, descriptor, ()->true);
    }

    public void register(PropertyEntry entry, IPropertyDescriptor descriptor, BooleanSupplier condition) {
        entry.setDescriptor(descriptor);
        entry.setCondition(condition);

        String category = entry.getCategory();
        if(category == null)
            defaultEntryMap.put(entry.getName(), entry);
        else {
            Map<String, PropertyEntry> entryMap = categoryEntryMap.get(category);
            if(entryMap == null) {
                entryMap = new HashMap<>();
                categoryEntryMap.put(category, entryMap);
            }
            entryMap.put(entry.getName(), entry);
        }
        descriptor.setId(entry.getName());
        entry.setListeners(getListeners());
    }

    public PropertyEntry unregister(String key) {
        return remove(defaultEntryMap, key);
    }

    public PropertyEntry unregister(String category, String key) {
        Map<String, PropertyEntry> entryMap = categoryEntryMap.get(category);
        if(entryMap == null)
            return null;

        return remove(entryMap, key);
    }

    private PropertyEntry remove(Map<String, PropertyEntry> entryMap, String key) {
        if(!entryMap.containsKey(key))
            return null;

        PropertyEntry entry = entryMap.remove(key);

        entry.setListeners(null);
        return entry;
    }

    public boolean containsKey(String key) {
        return defaultEntryMap.containsKey(key);
    }

    public Set<String> keySet() {
        return defaultEntryMap.keySet();
    }

    public boolean containsKey(String category, String key) {
        return categoryEntryMap.containsKey(category) && categoryEntryMap.get(category).containsKey(key);
    }

    public Set<String> keySet(String category) {
        if(!categoryEntryMap.containsKey(category))
            return new HashSet<>();

        return categoryEntryMap.get(category).keySet();
    }

    public Collection<PropertyEntry> values() {
        return defaultEntryMap.values();
    }

    public Collection<PropertyEntry> values(String category) {
        if(!categoryEntryMap.containsKey(category))
            return new HashSet<>();
        return categoryEntryMap.get(category).values();
    }

    public PropertyEntry get(String key) {
        return defaultEntryMap.get(key);
    }

    public PropertyEntry get(String category, String key) {
        return containsKey(category, key) ? categoryEntryMap.get(category).get(key) : null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptors = getPropertyDescriptors(defaultEntryMap);
        for(String category: categoryEntryMap.keySet()) {
            descriptors = combine(descriptors, getPropertyDescriptors(category));
        }
        return descriptors;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors(String category) {
        if(category == null)
            return getPropertyDescriptors(defaultEntryMap);

        return categoryEntryMap.containsKey(category) ? getPropertyDescriptors(categoryEntryMap.get(category)) : new IPropertyDescriptor[0];
    }

    private IPropertyDescriptor[] getPropertyDescriptors(Map<String, PropertyEntry> entryMap) {
        List<IPropertyDescriptor> descriptors = new ArrayList<>();
        for(PropertyEntry entry: entryMap.values()) {
            entry.getDescriptor().setVisible(entry.getCondition().getAsBoolean());
            descriptors.add(entry.getDescriptor());
        }
        return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
         if (defaultEntryMap.containsKey(id))
             return defaultEntryMap.get(id).get();

        return null;
    }

    @Override
    public Object getPropertyValue(String category, Object id) {
        if(!containsKey(category, (String)id)) return null;

        return categoryEntryMap.get(category).get(id).get();
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (defaultEntryMap.containsKey(id)) {
            defaultEntryMap.get(id).set(value);
        }
    }

    @Override
    public void setPropertyValue(String category, Object id, Object value) {
        if(containsKey(category, (String)id))
            categoryEntryMap.get(category).get(id).set(value);
    }

    protected <T> PropertyEntry<Boolean> booleanProperty(String propName) {
        return booleanProperty(propName, Boolean.FALSE);
    }

    protected <T> PropertyEntry<Boolean> booleanProperty(String propName, Boolean value) {
        return property(propName, value);
    }

    protected PropertyEntry<String> stringProperty(String propName) {
        return stringProperty(propName, null);
    }

    protected PropertyEntry<String> stringProperty(String propName, String value) {
        return property(propName, value);
    }

    protected PropertyEntry<Integer> intProperty(String propName) {
        return intProperty(propName, 0);
    }

    protected PropertyEntry<Integer> intProperty(String propName, int value) {
        return property(propName, value);
    }

    protected PropertyEntry<Long> longProperty(String propName) {
        return longProperty(propName, 0);
    }

    protected PropertyEntry<Long> longProperty(String propName, long value) {
        return property(propName, value);
    }

    protected PropertyEntry<Float> floatProperty(String propName) {
        return floatProperty(propName, 0);
    }

    protected PropertyEntry<Float> floatProperty(String propName, float value) {
        return property(propName, value);
    }

    protected PropertyEntry<Double> doubleProperty(String propName) {
        return doubleProperty(propName, 0);
    }

    protected PropertyEntry<Double> doubleProperty(String propName, double value) {
        return property(propName, value);
    }

    protected PropertyEntry<Integer> indexProperty(String propName, int value, Supplier<String[]> optionSupplier) {
        return property(propName, value).setDescriptor(new ComboBoxPropertyDescriptor(optionSupplier));
    }

    protected <T> PropertyEntry<T> property(String propName) {
        return new PropertyEntry<>(propName);
    }

    protected <T> PropertyEntry<T> property(String propName, T value) {
        return new PropertyEntry<>(propName, value);
    }

    protected <T> PropertyEntry<T> enumProperty(String propName, T value, T[] values) {
        return new PropertyEntry<T>(propName, value).setDescriptor(new ListPropertyDescriptor(values));
    }

    protected <T> PropertyEntry<T> enumProperty(String propName, T value, Supplier<Object[]> optionSupplier) {
        return new PropertyEntry<T>(propName, value).setDescriptor(new ListPropertyDescriptor(optionSupplier));
    }
}
