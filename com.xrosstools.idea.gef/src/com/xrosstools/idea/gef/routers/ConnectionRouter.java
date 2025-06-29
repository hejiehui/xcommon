package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;

public interface ConnectionRouter {
    void route(Connection conn);
    default void activate(Connection conn) {}
    default void deactivate(Connection conn) {}
    default boolean contains(Endpoint endpoint) {return false;}
}
