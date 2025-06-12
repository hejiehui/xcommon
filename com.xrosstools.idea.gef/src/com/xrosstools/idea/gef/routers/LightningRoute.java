package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;

//Use LightningRouter instead
@Deprecated
public class LightningRoute implements ConnectionRouter {
    private LightningRouter impl;

    public LightningRoute(boolean vertical) {
        impl = new LightningRouter(vertical);
    }

    @Override
    public void route(Connection conn) {
        impl.route(conn);
    }
}