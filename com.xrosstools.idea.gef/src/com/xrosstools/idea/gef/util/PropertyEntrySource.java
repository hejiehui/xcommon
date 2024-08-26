package com.xrosstools.idea.gef.util;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class PropertyEntrySource extends PropertySource {
    private class Triple {
        PropertyEntry entry;
        BooleanSupplier condition;
        IPropertyDescriptor descriptor;
    }

    private Map<String, Triple> entryMap = new LinkedHashMap<>();

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
        Triple triple = new Triple();
        triple.entry = entry;
        triple.descriptor = descriptor;
        triple.condition = condition;

        entryMap.put(entry.getName(), triple);
        descriptor.setId(entry.getName());
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> descriptors = new ArrayList<>();
         for(Triple entry: entryMap.values()) {
             entry.descriptor.setVisible(entry.condition.getAsBoolean());
             descriptors.add(entry.descriptor);
         }
        return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
         if (entryMap.containsKey(id))
             return entryMap.get(id).entry.get();

        return null;
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (entryMap.containsKey(id)) {
            entryMap.get(id).entry.set(value);
        }
    }

    protected <T> PropertyEntry<Boolean> booleanProperty(String propName) {
        return booleanProperty(propName, Boolean.FALSE);
    }

    protected <T> PropertyEntry<Boolean> booleanProperty(String propName, Boolean value) {
        return enumProperty(propName, value, new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    }

    protected PropertyEntry<String> stringProperty(String propName) {
        return stringProperty(propName, null);
    }

    protected PropertyEntry<String> stringProperty(String propName, String value) {
        return new PropertyEntry<>(propName, value, getListeners()).setDescriptor(new StringPropertyDescriptor());
    }

    protected PropertyEntry<Integer> intProperty(String propName) {
        return intProperty(propName, 0);
    }

    protected PropertyEntry<Integer> intProperty(String propName, int value) {
        return new PropertyEntry<>(propName, value, getListeners()).setDescriptor(new IntegerPropertyDescriptor());
    }

    protected PropertyEntry<Long> longProperty(String propName) {
        return longProperty(propName, 0);
    }

    protected PropertyEntry<Long> longProperty(String propName, long value) {
        return new PropertyEntry<>(propName, value, getListeners()).setDescriptor(new LongPropertyDescriptor());
    }

    protected PropertyEntry<Float> floatProperty(String propName) {
        return floatProperty(propName, 0);
    }

    protected PropertyEntry<Float> floatProperty(String propName, float value) {
        return new PropertyEntry<>(propName, value, getListeners()).setDescriptor(new FloatPropertyDescriptor());
    }

    protected PropertyEntry<Double> doubleProperty(String propName) {
        return doubleProperty(propName, 0);
    }

    protected PropertyEntry<Double> doubleProperty(String propName, double value) {
        return new PropertyEntry<>(propName, value, getListeners()).setDescriptor(new DoublePropertyDescriptor());
    }

    protected PropertyEntry<Integer> indexProperty(String propName, int value, Supplier<String[]> optionSupplier) {
        return intProperty(propName, value).setDescriptor(new ComboBoxPropertyDescriptor(optionSupplier));
    }

    protected <T> PropertyEntry<T> property(String propName) {
        return new PropertyEntry<>(propName, getListeners());
    }

    protected <T> PropertyEntry<T> property(String propName, T value) {
        return new PropertyEntry<>(propName, value, getListeners());
    }

    protected <T> PropertyEntry<T> enumProperty(String propName, T value, T[] values) {
        return new PropertyEntry<T>(propName, getListeners()).setDescriptor(new ListPropertyDescriptor(values));
    }

    protected <T> PropertyEntry<T> enumProperty(String propName, T value, Supplier<Object[]> optionSupplier) {
        return new PropertyEntry<T>(propName, value, getListeners()).setDescriptor(new ListPropertyDescriptor(optionSupplier));
    }
}
