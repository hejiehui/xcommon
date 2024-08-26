package com.xrosstools.idea.gef.model;

import com.xrosstools.idea.gef.util.ListPropertyEntry;
import com.xrosstools.idea.gef.util.PropertyEntry;
import com.xrosstools.idea.gef.util.PropertyEntrySource;

import java.awt.*;
import java.util.List;

public class Node<C extends NodeConnection> extends PropertyEntrySource implements ModelProperties {
    private ListPropertyEntry<C> inputs = new ListPropertyEntry<>(PROP_INPUTS, PROP_INPUT, getListeners());
    private PropertyEntry<Integer> inputLimit = new PropertyEntry<>(PROP_INPUT_LIMIT, NO_LIMIT, getListeners());

    private ListPropertyEntry<C> outputs = new ListPropertyEntry<>(PROP_OUTPUTS, PROP_OUTPUT, getListeners());
    private PropertyEntry<Integer> outputLimit = new PropertyEntry<>(PROP_OUTPUT_LIMIT, NO_LIMIT, getListeners());

    private PropertyEntry<Boolean> connectSelfAllowed = new PropertyEntry<>(PROP_CONNECT_SELF_ALLOWED, true, getListeners());

    private PropertyEntry<Point> location = new PropertyEntry<>(PROP_LOCATION, getListeners());
    private PropertyEntry<Dimension> size = new PropertyEntry<>(PROP_SIZE, new Dimension(100, 50), getListeners());

    public List<C> getInputs() {
        return inputs.get();
    }

    public void setInputs(List<C> _inputs) {
        inputs.set(_inputs);
    }

    public void addInput(C input) {
        inputs.add(input);
    }

    public void removeInput(C input) {
        inputs.remove(input);
    }

    public List<C> getOutputs() {
        return outputs.get();
    }

    public void setOutputs(List<C> _outputs) {
        outputs.set(_outputs);
    }

    public void addOutput(C output) {
        outputs.add(output);
    }

    public void removeOutput(C output) {
        outputs.remove(output);
    }

    public Point getLocation() {
        return location.get();
    }

    /**
     * To avoid trigger property chang event, you can use getLocation().setLocation(x, y) instead of setLocation();
     */
    public void setLocation(Point _location) {
        location.set(_location);
    }

    public Dimension getSize() {
        return size.get();
    }

    /**
     * To avoid trigger property chang event, you can use getSize().setSize(x, y) instead of setSize();
     */
    public void setSize(Dimension _size) {
        size.set(_size);
    }

    public int getInputLimit() {
        return inputLimit.get();
    }

    public void setInputLimit(int _inputLimit) {
        inputLimit.set(_inputLimit);
    }

    public boolean isSourceAcceptable(Node source) {
        return !(this == source && isConnectSelfAllowed() == false);
    }

    public boolean checkInputLimit() {
        return getInputLimit() < 0 ||  getInputLimit() > inputs.size();
    }

    public int getOutputLimit() {
        return outputLimit.get();
    }

    public void setOutputLimit(int _outputLimit) {
        outputLimit.set(_outputLimit);
    }

    public boolean checkOutputLimit() {
        return getOutputLimit() < 0 ||  getOutputLimit() > outputs.size();
    }

    public boolean isTargetAcceptable(Node target) {
        return !(this == target && isConnectSelfAllowed() == false);
    }

    public boolean isConnectSelfAllowed() {
        return connectSelfAllowed.get();
    }

    public void setConnectSelfAllowed(boolean _connectSelfAllowed) {
        connectSelfAllowed.set(_connectSelfAllowed);
    }
}
