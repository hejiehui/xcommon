package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class RectangleFigure extends Figure {

    @Override
    public void paintComponent(Graphics graphics) {
        Color oldColor = graphics.getColor();

        if(getBackgroundColor() != null)
            graphics.setColor(getBackgroundColor());
        graphics.fillRect(getX(), getY(), getWidth(),getHeight());

        if(getForegroundColor() != null)
            graphics.setColor(getForegroundColor());

        graphics.drawRect(getX(), getY(), getWidth(),getHeight());
        graphics.setColor(oldColor);
    }
}
