package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommonStyleRouter implements ConnectionRouter {
    private Map<RouterStyle, ConnectionRouter> routers = new HashMap<>();
    private RouterStyle style;
    private ConnectionRouter curRouter;

    public CommonStyleRouter(RouterStyle style) {
        this.style = style;
    }

    @Override
    public boolean contains(Endpoint endpoint) {
        return getInternalRouter(endpoint.getParentConnection()).contains(endpoint);
    }

    @Override
    public void route(Connection conn) {
        ConnectionRouter router = getInternalRouter(conn);
        router.route(conn);
    }

    @NotNull
    public ConnectionRouter getInternalRouter(Connection conn) {
        ConnectionRouter router = routers.get(style);
        if(router == null) {
            router = style.create();
            routers.put(style, router);
            router.activate(conn);
        }

        if(curRouter != null && curRouter != router) {
            curRouter.deactivate(conn);
        }

        curRouter = router;
        return curRouter;
    }

    public RouterStyle getStyle() {
        return style;
    }

    public void setStyle(RouterStyle style) {
        this.style = style;
    }
}
