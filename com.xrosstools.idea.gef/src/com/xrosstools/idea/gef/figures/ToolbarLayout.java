package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class ToolbarLayout implements LayoutManager {
    /** Constant to specify components to be aligned in the center */
    public static final int ALIGN_CENTER = 0;
    /** Constant to specify components to be aligned on the left/top */
    public static final int ALIGN_TOPLEFT = 1;
    /** Constant to specify components to be aligned on the right/bottom */
    public static final int ALIGN_BOTTOMRIGHT = 2;

    private boolean horizontal;
    private int alignment;
    private int gap;

    public void setGap(int gap) {
        this.gap = gap;
    }

    public ToolbarLayout() {}

    public ToolbarLayout(boolean horizontal, int alignment, int gap) {
        this.horizontal = horizontal;
        this.alignment = alignment;
        this.gap = gap;
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public void setSpacing(int gap) {
        this.gap = gap;
    }

    public void setMinorAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setStretchMinorAxis(boolean stretchMinorAxis) {
        //TODO
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void addLayoutComponent(String name, Figure comp) {
    }

    public void removeLayoutComponent(Figure comp) {
    }

    @Override
    public void setConstraint(Figure figure, Object constraint) {

    }

    @Override
    public Object getConstraint(Figure figure) {
        return null;
    }

    @Override
    public Dimension preferredLayoutSize(Figure parent) {
        synchronized (parent) {
            int count = parent.getComponentCount();
            int width = 0;
            int height = 0;
            for(Figure c: parent.getComponents()) {
                Dimension size = c.getPreferredSize();
                if (horizontal) {
                    width += size.width;
                    height = Math.max(height, size.height);
                }else {
                    height += size.height;
                    width = Math.max(width, size.width);
                }
            }

            if (horizontal)
                width += gap * (count - 1);
            else
                height += gap * (count - 1);

            width += parent.getMarginWidth();
            height += parent.getMarginHeight();
            return new Dimension(width, height);
        }
    }

    public Dimension minimumLayoutSize(Figure parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Figure parent) {
        synchronized (parent) {
            Rectangle area = parent.getClientArea();
            Dimension innerSize = area.getSize();
            int px = 0;
            int py = 0;

            int middle = horizontal ? innerSize.height/2 : innerSize.width/2;
            int nextPos = 0;

            for (Figure c :parent.getComponents()) {
                Dimension size = c.getPreferredSize();

                size.width = !horizontal && size.width == -1 ? innerSize.width : size.width;
                size.height = horizontal && size.height == -1 ? innerSize.height: size.height;
                c.setSize(size);

                int minorPos = horizontal ? size.height : size.width;

                switch (alignment) {
                    case ALIGN_CENTER:
                        minorPos = (middle - minorPos/2);
                        break;
                    case ALIGN_TOPLEFT:
                        minorPos = 0;
                        break;
                    case ALIGN_BOTTOMRIGHT:
                        minorPos = horizontal ? size.height : size.width;
                        break;
                    default:
                        throw new IllegalArgumentException("Alignment is not supported: " + alignment);
                }
                if (horizontal) {
                    c.setLocation(px + nextPos, py + minorPos);
                    nextPos += gap + size.width;
                } else {
                    c.setLocation(px + minorPos, py + nextPos);
                    nextPos += gap + size.height;
                }

                // Make sure child is layout as early as possible to avoid link screwed up
                c.layout();
            }
        }
    }

    @Override
    public void paintInsertionFeedback(Figure parent, Point insertionPoint, Graphics gef) {
        if(insertionPoint == null)
            return;

        int insertionIndex = getInsertionIndex(parent, insertionPoint);
        int insertionX = insertionPoint.x;
        int insertionY = insertionPoint.y;
        boolean vertical = !isHorizontal();

        if(parent.getComponentCount() == 0)
            return;

        if(insertionIndex == 0) {
            Figure child = parent.getComponents().get(0);
            if(vertical)
                insertionY = parent.getInsets().top/2;
            else
                insertionX = parent.getInsets().left/2;;
        } else if (insertionIndex < parent.getComponentCount()) {
            Figure child = parent.getComponents().get(insertionIndex);
            if(vertical)
                insertionY = parent.getInnerY() + child.getY() - gap / 2;
            else
                insertionX = parent.getInnerX() + child.getX() - gap / 2;
        } else {
            Figure child = parent.getComponents().get(insertionIndex -1);
            if (vertical)
                insertionY = parent.getHeight() - parent.getInsets().bottom/2;
            else
                insertionX = parent.getWidth() - parent.getInsets().right/2;;
        }

        if(vertical)
            gef.drawLine(parent.getInnerX(), insertionY, parent.getInnerX() + parent.getInnerWidth(), insertionY);
        else
            gef.drawLine(insertionX, parent.getInnerY(), insertionX, parent.getInnerY() + parent.getInnerrHeight());
    }

    public int getInsertionIndex(Figure parent, Point insertionPoint) {
        if(insertionPoint == null)
            return 0;

        int insertionIndex = 0;

        /* for two element, t
          | 0 X | 1 X | 2
           */
        for (Figure child : parent.getComponents()) {
            if (isHorizontal()) {
                if (child.getX() > insertionPoint.x)
                    break;
            } else {
                if (child.getY() > insertionPoint.y)
                    break;
            }
            insertionIndex++;
        }

        return insertionIndex;
    }

}
