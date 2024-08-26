package com.xrosstools.idea.gef.util;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class ListPropertyEntry<T> extends PropertyEntry<List<T>> {
    private String childName;
    public ListPropertyEntry(String name, String childName, PropertyChangeSupport listeners) {
        super(name, new ArrayList<>(), listeners);
        this.childName = childName;
    }

    public T getElement(int index) {
        return get().get(index);
    }

    public void setElement(int index, T element) {
        Object oldValue = getElement(index);
        get().set(index, element);
        firePropertyChange(childName, oldValue, element);
    }

    public void add(T element) {
        get().add(element);
        firePropertyChange(childName, null, element);
    }

    public void add(int index, T element) {
        get().add(index, element);
        firePropertyChange(childName, null, element);
    }

    public T remove(int index) {
        T oldValue = get().remove(index);
        firePropertyChange(childName, oldValue, null);
        return oldValue;
    }

    public void remove(T element) {
        get().remove(element);
        firePropertyChange(childName, element, null);
    }

    public int size() {
        return get().size();
    }

    public boolean isEmpty() {
        return get().isEmpty();
    }

    public boolean contains(T element) {
        return get().contains(element);
    }
}
