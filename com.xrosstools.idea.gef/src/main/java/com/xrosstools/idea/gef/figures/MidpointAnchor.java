package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class MidpointAnchor extends AbstractAnchor {
    private boolean vertical;
    public MidpointAnchor(Figure owner, boolean vertical) {
        setOwner(owner);
        this.vertical = vertical;
    }

    @Override
    public Point getLocation(Point ref) {
        Rectangle r = getOwner().getBounds();
        Figure f = getOwner();

        Point pos;

        if(vertical) {
            if(ref.y < r.y)
                pos = f.getTop();
            else if(ref.y > r.y + r.height)
                pos = f.getBottom();
            else if(ref.x < r.x) {
                pos = new Point(r.x, ref.y);
            }else if(ref.x < r.x + r.width) {
                pos = new Point(ref.x < r.getCenterX() ? r.x :  r.x + r.width, ref.y);
            } else {
                pos = new Point(r.x + r.width, ref.y);
            }
        }else{
            if(ref.x < r.x)
                pos = f.getLeft();
            else  if(ref.x > r.x + r.width)
                pos = f.getRight();
            else if(ref.y < r.y) {
                pos = new Point(ref.x, r.y);
            }else if(ref.y >= r.y && ref.y <= r.y + r.height){
                pos = new Point(ref.x, ref.y < r.getCenterY() ? r.y :  r.y + r.height);
            }else {
                pos = new Point(ref.x, r.y + r.height);
            }
        }

        return pos;
    }
}
