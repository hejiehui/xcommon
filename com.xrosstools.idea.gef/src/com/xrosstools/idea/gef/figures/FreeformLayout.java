package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class FreeformLayout implements LayoutManager {
    private static final int DEFAULT_MARGIN = 200;
    private int margin = 200;

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

    @Override
    public int getInsertionIndex(Figure parent, Point insertionPoint) {
        return 0;
    }

    @Override
    public void paintInsertionFeedback(Figure parent, Point insertionPoint, Graphics gef) {}
}
