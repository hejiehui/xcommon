package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class FreeformLayout implements LayoutManager {
    private static final int DEFAULT_MARGIN = 200;
    private int margin = 200;
    private Point oldParentLocation;

    public FreeformLayout() {
        this(DEFAULT_MARGIN);
    }

    public FreeformLayout(int margin) {
        this.margin = margin;
    }
    @Override
    public void setConstraint(Figure figure, Object constraint) {

    }

    @Override
    public Dimension preferredLayoutSize(Figure parent) {
        checkParentLocaton(parent);
        int parentX = parent.getLocation().x;
        int parentY = parent.getLocation().y;
        int width=0;
        int height=0;

        for (Figure c: parent.getComponents()) {
            Dimension size = c.getPreferredSize();
            Point location = c.getLocation();

            int cWidth = c.getX() + (int)size.getWidth() - parentX;
            if(cWidth > width)
                width = cWidth;

            int cHeight = c.getY() + (int)size.getHeight() - parentY;
            if(cHeight > height)
                height = cHeight;

        }
        return new Dimension(width + margin, height + margin);
    }

    @Override
    public void layoutContainer(Figure parent) {

        for (Figure c : parent.getComponents()) {
            Dimension size = c.getPreferredSize();
            c.setSize(size);
        }
    }


    private void checkParentLocaton(Figure parent) {
        Point parentLocation = parent.getLocation();
        //This is the first time model file is opened, so we just init location
        if(oldParentLocation == null) {
            oldParentLocation = parentLocation;
            return;
        }

        if(oldParentLocation.x == parentLocation.x && oldParentLocation.y == parentLocation.y)
            return;

        int deltaX = parentLocation.x - oldParentLocation.x;
        int deltaY = parentLocation.y - oldParentLocation.y;

        for (Figure c : parent.getComponents()) {
            Point loc = c.getLocation();
            loc.translate(deltaX, deltaY);
            c.setLocation(loc);
        }

        oldParentLocation = parentLocation;
    }

    @Override
    public int getInsertionIndex(Figure parent, Point insertionPoint) {
        return 0;
    }

    @Override
    public void paintInsertionFeedback(Figure parent, Point insertionPoint, Graphics gef) {}
}
