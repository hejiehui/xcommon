package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class RoundedRectangle extends Figure {

    private int arcWidth = 5;
    private int arcHeight = 5;

    public int getArcWidth() {
        return arcWidth;
    }

    public void setArcWidth(int arcWidth) {
        this.arcWidth = arcWidth;
    }

    public int getArcHeight() {
        return arcHeight;
    }

    public void setArcHeight(int arcHeight) {
        this.arcHeight = arcHeight;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Color oldColor = graphics.getColor();

        if(getBackgroundColor() != null) {
            graphics.setColor(getBackgroundColor());
            graphics.fillRoundRect(getX(), getY(), getWidth(),getHeight(), arcWidth, arcHeight);
        }

        if(getForegroundColor() != null) {
            graphics.setColor(getForegroundColor());
        }
        graphics.drawRoundRect(getX(), getY(), getWidth(),getHeight(), arcWidth, arcHeight);
        graphics.setColor(oldColor);
    }
}
