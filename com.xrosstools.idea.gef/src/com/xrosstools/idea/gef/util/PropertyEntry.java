package com.xrosstools.idea.gef.util;

import java.beans.PropertyChangeSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class PropertyEntry<T> {
    private PropertyChangeSupport listeners;

    private String category;
    private String name;
    private T value;
    private DataTypeEnum type;

    private IPropertyDescriptor descriptor;
    private BooleanSupplier condition;

    public PropertyEntry(String name) {
        this.name = name.trim();
    }

    public PropertyEntry(String name, T value) {
        this(name);
        this.value = value;
        this.type = DataTypeEnum.typeOf(value);
    }

    public PropertyEntry(String name, PropertyChangeSupport listeners) {
        this(name, null, listeners);
    }

    public PropertyEntry(String name, T value, PropertyChangeSupport listeners) {
        this(name, value);
        this.listeners = listeners;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        Object oldValue = this.value;
        this.value = newValue;
        firePropertyChange(oldValue, newValue);
    }

    public void firePropertyChange(String propName, Object oldValue, Object newValue) {
        listeners.firePropertyChange(propName, oldValue, newValue);
    }

    public void firePropertyChange(Object oldValue, Object newValue) {
        listeners.firePropertyChange(name, oldValue, newValue);
    }

    public IPropertyDescriptor getDescriptor() {
        if(descriptor != null)
            return descriptor;
        descriptor = type.createDescriptor();
        descriptor.setCategory(category);
        return descriptor;
    }

    public PropertyEntry<T> setDescriptor(IPropertyDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public PropertyEntry<T> withOptions(Supplier<Object[]> optionSupplier) {
        descriptor = new ListPropertyDescriptor(optionSupplier);
        return this;
    }

    public DataTypeEnum getType() {
        return type;
    }

    public PropertyEntry<T> setType(DataTypeEnum type) {
        this.type = type;
        return this;
    }

    public BooleanSupplier getCondition() {
        return condition;
    }

    public void setCondition(BooleanSupplier condition) {
        this.condition = condition;
    }

    public PropertyChangeSupport getListeners() {
        return listeners;
    }

    public void setListeners(PropertyChangeSupport listeners) {
        this.listeners = listeners;
    }

    public String getCategory() {
        return category;
    }

    public PropertyEntry setCategory(String category) {
        this.category = category;
        return this;
    }
}
