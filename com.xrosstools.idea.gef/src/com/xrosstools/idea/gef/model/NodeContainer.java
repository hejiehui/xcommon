package com.xrosstools.idea.gef.model;

import java.util.List;

public interface NodeContainer<T extends Node> extends ModelProperties {
    List<T> getChildren();

    void setChildren(List<T> children);

    boolean isEmpty();

    boolean contains(T child);

    /**
     * Check if child can be added
     */
    default boolean isChildAcceptable(T child){return true;}

    void addChild(T child);

    void addChild(int index, T child);

    void removeChild(int index);

    void removeChild(T child);

    T get(int index);
}