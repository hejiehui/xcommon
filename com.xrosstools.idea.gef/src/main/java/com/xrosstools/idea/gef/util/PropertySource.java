package com.xrosstools.idea.gef.util;

import java.beans.PropertyChangeSupport;

public class PropertySource implements IPropertySource {
    public static final IPropertyDescriptor[] EMPTY = new IPropertyDescriptor[0];

    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    @Override
    public PropertyChangeSupport getListeners() {
        return listeners;
    }

    public void firePropertyChange(String propName, Object oldValue, Object newValue) {
        listeners.firePropertyChange(propName, oldValue, newValue);
    }

    public void firePropertyChange(String propName) {
        listeners.firePropertyChange(propName, null, null);
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return EMPTY;
    }

    @Override
    public Object getPropertyValue(Object name) {
        return null;
    }

    @Override
    public void setPropertyValue(Object name, Object value) {
    }

    public IPropertyDescriptor[] combine(IPropertyDescriptor[] p1, IPropertyDescriptor[] p2) {
        IPropertyDescriptor[] descriptors = new IPropertyDescriptor[p1.length + p2.length];
        System.arraycopy(p1, 0, descriptors, 0, p1.length);
        System.arraycopy(p2, 0, descriptors, p1.length, p2.length);
        return descriptors;
    }
}
