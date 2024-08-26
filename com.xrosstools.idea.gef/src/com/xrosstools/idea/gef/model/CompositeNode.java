package com.xrosstools.idea.gef.model;

import java.util.ArrayList;
import java.util.List;

public class CompositeNode<T extends Node> extends Node implements NodeContainer<T> {
    private List<T> children = new ArrayList<>();

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        Object oldValue = this.children;
        this.children = children;
        firePropertyChange(PROP_CHILDREN, oldValue, children);
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public boolean contains(T child) {
        return children.contains(child);
    }

    public void addChild(T child) {
        children.add(child);
        firePropertyChange(PROP_CHILD, null, child);
    }

    public void addChild(int index, T child) {
        children.add(index, child);
        firePropertyChange(PROP_CHILD, null, child);
    }

    public void removeChild(int index) {
        Object oldValue = children.remove(index);
        firePropertyChange(PROP_CHILD, oldValue, null);
    }

    public void removeChild(T child) {
        if(children.remove(child))
            firePropertyChange(PROP_CHILD, child, null);
    }

    public T get(int index) {
        return children.get(index);
    }
}