package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class Rhombus extends Figure {
    @Override
    public void paintComponent(Graphics graphics) {
        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;
        int centerX = getX() + halfWidth;
        int centerY = getY() + halfHeight;

        int[] xPoints = {
                centerX,
                centerX + halfWidth,
                centerX,
                centerX - halfWidth
        };
        int[] yPoints = {
                centerY - halfHeight,
                centerY,
                centerY + halfHeight,
                centerY
        };

        Color oldColor = graphics.getColor();

        if(getBackgroundColor() != null) {
            graphics.setColor(getBackgroundColor());
            graphics.fillPolygon(xPoints, yPoints, 4);
        }

        if(getForegroundColor() != null) {
            graphics.setColor(getForegroundColor());
        }

        graphics.drawPolygon(xPoints, yPoints, 4);
        graphics.setColor(oldColor);
    }
}
