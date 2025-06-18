package com.xrosstools.idea.gef.figures;

import com.xrosstools.idea.gef.routers.RouterStyle;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CommonStyleAnchor extends AbstractAnchor {
    private RouterStyle style;
    private boolean isSource;
    private Map<RouterStyle, AbstractAnchor> anchors = new HashMap<>();

    public CommonStyleAnchor(Figure owner, boolean isSource) {
        setOwner(owner);
        this.isSource = isSource;
    }

    public RouterStyle getStyle() {
        return style;
    }

    public void setStyle(RouterStyle style) {
        this.style = style;
    }

    @Override
    public Point getLocation(Point loc) {
    	return isSource ? getSourceLocation(loc) : getTargetLocation(loc);
    }
    
    private Point getSourceLocation(Point ref) {
        AbstractAnchor anchor = anchors.get(style);
        if(anchor == null) {
            Figure owner = getOwner();
            switch (style) {
                case HORIZONTAL_LIGHTNING:
                case HORIZONTAL_RIGHT_ANGLE:
                    anchor = new MidpointAnchor(owner, false);
                    break;
                case VERTICAL_LIGHTNING:
                case VERTICAL_RIGHT_ANGLE:
                    anchor = new MidpointAnchor(owner, true);
                    break;
                case VERTICAL_HOMOLATERAL:
                    anchor = new HomolateralAnchor(owner, true, true);
                    break;
                case HORIZONTAL_HOMOLATERAL:
                    anchor = new HomolateralAnchor(owner, true, false);
                    break;
                default:
                    anchor = new ChopboxAnchor(owner);
            }
            anchors.put(style, anchor);
        }

        return anchor.getLocation(ref);
    }

    private Point getTargetLocation(Point ref) {
        AbstractAnchor anchor = anchors.get(style);
        if(anchor == null) {
            Figure owner = getOwner();
            switch (style) {
                case HORIZONTAL_LIGHTNING:
                case VERTICAL_RIGHT_ANGLE:
                    anchor = new MidpointAnchor(owner, false);
                    break;
                case HORIZONTAL_RIGHT_ANGLE:
                case VERTICAL_LIGHTNING:
                    anchor = new MidpointAnchor(owner, true);
                    break;
                case VERTICAL_HOMOLATERAL:
                    anchor = new HomolateralAnchor(owner, false, true);
                    break;
                case HORIZONTAL_HOMOLATERAL:
                    anchor = new HomolateralAnchor(owner, false, false);
                    break;
                default:
                    anchor = new ChopboxAnchor(owner);
            }
            anchors.put(style, anchor);
        }

        return anchor.getLocation(ref);
    }
}
