package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class HomolateralAnchor extends AbstractAnchor {
    private boolean isSource;
    private boolean vertical;
    public HomolateralAnchor(Figure owner, boolean isSource, boolean vertical) {
        setOwner(owner);
        this.isSource = isSource;
        this.vertical = vertical;
    }

    @Override
    public Point getLocation(Point ref) {
        Rectangle r = getOwner().getBounds();
        Figure f = getOwner();

        Point pos;

        if(vertical) {
            if (isSource) {
                pos = ref.x < r.x ? f.getTop() : f.getBottom();
            } else {
                pos = ref.x > r.x ? f.getTop() : f.getBottom();
            }
        }else {
            if (isSource) {
                pos = ref.y < r.y ? f.getRight() : f.getLeft();
            } else {
                pos = ref.y > r.y ? f.getRight() : f.getLeft();
            }
        }

        return pos;
    }
}
