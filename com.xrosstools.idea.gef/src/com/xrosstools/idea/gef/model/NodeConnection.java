package com.xrosstools.idea.gef.model;

import com.xrosstools.idea.gef.util.PropertyEntry;
import com.xrosstools.idea.gef.util.PropertySource;

public class NodeConnection<S extends Node, T extends Node> extends PropertySource implements ModelProperties {
    private PropertyEntry<S> source = new PropertyEntry<>(PROP_SOURCE, getListeners());
    private PropertyEntry<T> target = new PropertyEntry<>(PROP_TARGET, getListeners());

    public NodeConnection(){}

    public NodeConnection(S source, T target) {
        setSource(source);
        setTarget(target);
        source.addOutput(this);
        target.addInput(this);
    }


    public S getSource() {
        return source.get();
    }

    public void setSource(S _source) {
        source.set(_source);
    }

    public T getTarget() {
        return target.get();
    }

    public void setTarget(T _target) {
        target.set(_target);
    }
}
