package com.xrosstools.idea.gef.figures;

import com.intellij.openapi.util.IconLoader;
import com.xrosstools.idea.gef.Activator;

import javax.swing.*;
import java.awt.*;

public class IconFigure extends Figure {
    private Icon image;

    public IconFigure() {
        setSource(null);
    }

    public IconFigure(String source) {
        setSource(source);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if(image == null)
            return;

        image.paintIcon(getRootPane(), graphics, getInnerX(), getInnerY());
    }

    // Do not show selection frame
    public void paintSelection(Graphics graphics) {}

    public void setSource(String source) {
        image = source == null ? null: IconLoader.findIcon(Activator.getIconPath(source));
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
