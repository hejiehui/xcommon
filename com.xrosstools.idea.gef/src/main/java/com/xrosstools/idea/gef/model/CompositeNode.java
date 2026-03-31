package com.xrosstools.idea.gef.model;

import com.xrosstools.idea.gef.util.ListPropertyEntry;

import java.util.ArrayList;
import java.util.List;

public class CompositeNode<T extends Node> extends Node implements NodeContainer<T> {
    private ListPropertyEntry<T> children = new ListPropertyEntry<>(PROP_CHILDREN, PROP_CHILD, getListeners());

    public List<T> getChildren() {
        return children.get();
    }

    public void setChildren(List<T> _children) {
        children.set(_children);
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public boolean contains(T child) {
        return children.contains(child);
    }

    public void addChild(T child) {
        children.add(child);
    }

    public void addChild(int index, T child) {
        children.add(index, child);
    }

    public void removeChild(int index) {
        children.remove(index);
    }

    public void removeChild(T child) {
        children.remove(child);
    }

    public T get(int index) {
        return children.getElement(index);
    }

    public void move(int newIndex, T child) {
        children.move(newIndex, child);
    }
}