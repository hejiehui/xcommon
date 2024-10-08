package com.xrosstools.idea.gef.figures;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

//Used as XYLayout
public class FreeformLayout implements LayoutManager {
    private static final int DEFAULT_MARGIN = 200;
    private int margin = 200;
    private Map constraints = new HashMap();
    private static final int CROSS_WIDTH = 15;

    public FreeformLayout() {
        this(DEFAULT_MARGIN);
    }

    public FreeformLayout(int margin) {
        this.margin = margin;
    }
    @Override
    public void setConstraint(Figure figure, Object constraint) {
        if (constraint == null)
            return;

        Rectangle bounds = (Rectangle)constraint;
        figure.setBounds(bounds);
        constraints.put(figure, constraint);
    }

    public Object getConstraint(Figure figure) {
        return constraints.get(figure);
    }

    public Point getOrigin(Figure parent) {
        return parent.getClientArea().getLocation();
    }

    @Override
    public Dimension preferredLayoutSize(Figure parent) {
        Rectangle rect = new Rectangle();
        for (Figure c: parent.getComponents()) {
            Rectangle r = (Rectangle) constraints.get(c);
            if (r != null) {
                if (r.width == -1 || r.height == -1) {
                    Dimension preferredSize = c.getPreferredSize();
                    r = new Rectangle(r);
                    if (r.width == -1)
                        r.width = preferredSize.width;
                    if (r.height == -1)
                        r.height = preferredSize.height;
                }
                rect = rect.union(r);
            }
        }

        return (new Dimension(rect.width + parent.getMarginWidth() + margin, rect.height + parent.getMarginHeight() + margin));
    }

    @Override
    public void layoutContainer(Figure parent) {
        for (Figure c : parent.getComponents()) {
            Rectangle bounds = (Rectangle) getConstraint(c);
            if (bounds != null) {
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension preferredSize = c.getPreferredSize();
                    bounds = new Rectangle(bounds);
                    if (bounds.width == -1)
                        bounds.width = preferredSize.width;
                    if (bounds.height == -1)
                        bounds.height = preferredSize.height;
                }
                c.setBounds(bounds);
            }
        }
    }

    @Override
    public int getInsertionIndex(Figure parent, Point insertionPoint) {
        return 0;
    }

    @Override
    public void paintInsertionFeedback(Figure parent, Point insertionPoint, Graphics gef) {
        int centerX = parent.getInnerX() + insertionPoint.x;
        int centerY = parent.getInnerY() + insertionPoint.y;
        gef.drawLine(centerX - CROSS_WIDTH, centerY, centerX + CROSS_WIDTH, centerY);
        gef.drawLine(centerX, centerY - CROSS_WIDTH, centerX, centerY + CROSS_WIDTH);
    }
}
