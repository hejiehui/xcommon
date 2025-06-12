package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;

import java.util.HashMap;
import java.util.Map;

public class CommonStyleRouter implements ConnectionRouter {
    private Map<RouterStyle, ConnectionRouter> routers = new HashMap<>();
    private RouterStyle style;

    public CommonStyleRouter(RouterStyle style) {
        this.style = style;
    }

    @Override
    public void route(Connection conn) {
        ConnectionRouter router = routers.get(style);
        if(router == null) {
            router = style.create();
            routers.put(style, router);
        }

        router.route(conn);
    }

    public RouterStyle getStyle() {
        return style;
    }

    public void setStyle(RouterStyle style) {
        this.style = style;
    }
}
