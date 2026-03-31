package com.xrosstools.idea.gef.figures;

import javax.swing.*;
import java.awt.*;

public class IconFigure extends Figure {
    private Icon image;

    public IconFigure() {
        setSource(null);
    }

    public IconFigure(Icon icon) {
        setSource(icon);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if(image == null)
            return;

        image.paintIcon(getRootPane(), graphics, getInnerX(), getInnerY());
    }

    public void setSource(Icon icon) {
        image = icon;
        if (image == null)
            setSize(0,16);
        else {
            setSize(image.getIconWidth(), image.getIconHeight());
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return  image == null ?
            new Dimension(getMarginWidth(), getMarginHeight()) :
            new Dimension(image.getIconWidth() + getMarginWidth(), image.getIconHeight() + getMarginHeight());
    }
}
