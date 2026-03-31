package com.xrosstools.idea.gef.model;

public interface ModelProperties {
    //Indicates there no count limit in connection
    int NO_LIMIT = -1;

    String PROP_SOURCE = "source";
    String PROP_TARGET = "target";

    String PROP_CHILDREN = "children";
    String PROP_CHILD = "child";

    String PROP_INPUTS = "inputs";
    String PROP_OUTPUTS = "outputs";

    String PROP_INPUT = "input";
    String PROP_OUTPUT = "output";

    String PROP_INPUT_LIMIT = "inputLimit";
    String PROP_OUTPUT_LIMIT = "outputLimit";
    String PROP_CONNECT_SELF_ALLOWED = "connectSelfAllowed";


    String PROP_LOCATION = "location";
    String PROP_SIZE = "size";

    String PROP_LAYOUT = "layout";
}
