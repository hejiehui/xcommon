package com.xrosstools.idea.gef.util;

import java.beans.PropertyChangeSupport;
import java.util.function.Supplier;

public class PropertyEntry<T> {
    private PropertyChangeSupport listeners;

    private String name;
    private T value;

    private IPropertyDescriptor descriptor;

    public PropertyEntry(String name, PropertyChangeSupport listeners) {
        this(name, null, listeners);
    }

    public PropertyEntry(String name, T value, PropertyChangeSupport listeners) {
        this.name = name;
        this.value = value;
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
}
