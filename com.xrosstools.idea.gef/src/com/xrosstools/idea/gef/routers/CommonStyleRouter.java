package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommonStyleRouter implements ConnectionRouter {
    private Map<RouterStyle, ConnectionRouter> routers = new HashMap<>();
    private RouterStyle style;

    public CommonStyleRouter(RouterStyle style) {
        this.style = style;
    }

    @Override
    public boolean contains(Endpoint endpoint) {
        return getConnectionRouter().contains(endpoint);
    }

    @Override
    public void route(Connection conn) {
        ConnectionRouter router = getConnectionRouter();
        router.route(conn);
    }

    @NotNull
    private ConnectionRouter getConnectionRouter() {
        ConnectionRouter router = routers.get(style);
        if(router == null) {
            router = style.create();
            routers.put(style, router);
        }
        return router;
    }

    public RouterStyle getStyle() {
        return style;
    }

    public void setStyle(RouterStyle style) {
        this.style = style;
    }
}
