package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CommonStyleLocator implements ConnectionLocator {
    private Map<RouterStyle, ConnectionLocator> locators = new HashMap<>();
    private Figure figure;
    private RouterStyle style;
    public CommonStyleLocator(Figure figure) {
        this.figure = figure;
    }

    @Override
    public Point getLocation(PointList pl) {
        ConnectionLocator locator = locators.get(style);
        if(locator == null) {
            locator = style.createLocator(figure);
            locators.put(style, locator);
        }
        return locator.getLocation(pl);
    }

    public RouterStyle getStyle() {
        return style;
    }

    public void setStyle(RouterStyle style) {
        this.style = style;
    }
}
