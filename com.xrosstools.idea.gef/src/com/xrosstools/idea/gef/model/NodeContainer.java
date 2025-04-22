package com.xrosstools.idea.gef.model;

import java.util.List;

public interface NodeContainer<T> extends ModelProperties {
    List<T> getChildren();

    void setChildren(List<T> children);

    boolean isEmpty();

    boolean contains(T child);

    void addChild(T child);

    void addChild(int index, T child);

    void move(int newIndex, T child);

    void removeChild(int index);

    void removeChild(T child);

    T get(int index);

    default int inexOf(T child) {
        return getChildren().indexOf(child);
    }
}