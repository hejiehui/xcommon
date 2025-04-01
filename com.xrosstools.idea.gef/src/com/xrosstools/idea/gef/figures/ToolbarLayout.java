package com.xrosstools.idea.gef.figures;

import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;

import java.awt.*;
import java.util.List;

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

            if(count == 0)
                return new Dimension(parent.getMarginWidth(), parent.getMarginHeight());

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
        int insertionX = 0;
        int insertionY = 0;
        int width = 0;
        int height = 0;
        boolean vertical = !isHorizontal();

        List<AbstractGraphicalEditPart> parts = parent.getPart().getChildren();
        if(parts.size() == 0) {
            if (vertical) {
                insertionY = parent.getInnerHeight() / 2;
                width = parent.getInnerWidth();
            } else {
                insertionX = parent.getInnerWidth() / 2;
                height = parent.getInnerHeight();
            }
        }else {
            // Except the last one, all the insertion line refer to the next child
            Figure child = insertionIndex < parts.size() ?
                    getContainer(parent, parts.get(insertionIndex)) :
                    getContainer(parent, parts.get(insertionIndex - 1));

            insertionX = child.getX();
            insertionY = child.getY();

            int delta = 0;
            if(insertionIndex == 0) // The insertion line before first child
                delta = Figure.SELECTION_GAP + (vertical ? parent.getInsets().top : parent.getInsets().left);
            else if(insertionIndex < parts.size()) // The insertion line in the middle
                delta = gap/2;
            else { // The insertion line after last child
                delta = -Figure.SELECTION_GAP -(vertical ? parent.getInsets().bottom : parent.getInsets().right);
                if(vertical)
                    insertionY += child.getHeight();
                else
                    insertionX += child.getWidth();
            }

            if(vertical) {
                insertionY -= delta;
                width = child.getWidth();
            }else {
                insertionX -= delta;
                height = child.getHeight();
            }
        }

        insertionX += parent.getInnerX();
        insertionY += parent.getInnerY();

        Stroke s = Figure.setLineWidth(gef, 2);
        Color oldColor = gef.getColor();
        gef.setColor(Figure.SELECTION_LINE_COLOR);

        gef.drawLine(insertionX, insertionY, insertionX + width, insertionY + height);

        gef.setColor(oldColor);
        Figure.restore(gef, s);
    }

    public int getInsertionIndex(Figure parent, Point insertionPoint) {
        if(insertionPoint == null)
            return -1;

        int insertionIndex = 0;

        /* for two element, t
          | 0 X | 1 X | 2
           */
        for (AbstractGraphicalEditPart part : parent.getPart().getChildren()) {
            Point referLocation = getContainer(parent, part).getCenter();
            if (isHorizontal()) {
                if (referLocation.getX() > insertionPoint.x)
                    break;
            } else {
                if (referLocation.getY() > insertionPoint.y)
                    break;
            }
            insertionIndex++;
        }

        return insertionIndex;
    }

    private Figure getContainer(Figure parent, AbstractGraphicalEditPart part) {
        Figure figure = part.getFigure();
        while(figure.getParent() != parent)
            figure = figure.getParent();

        return figure;
    }

}
