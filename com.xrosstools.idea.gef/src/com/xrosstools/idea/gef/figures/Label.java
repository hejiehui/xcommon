package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class Label extends Figure {
    private IconFigure icon;
    private Text text;
    private Color foreground;

    public Label(String text) {
        this();
        setText(text);
    }

    public Label() {
        setLayoutManager(new ToolbarLayout(true, ToolbarLayout.ALIGN_CENTER, 5));
    }

    public void setForegroundColor(Color foreground) {
        this.foreground = foreground;
        setTextColor();
    }

    private void setTextColor() {
        if(foreground != null && text != null)
            text.setForegroundColor(foreground);
    }

    public void setText(String textStr) {
        if (text == null) {
            text = new Text();
            add(text);
        }
        text.setText(textStr);
        setTextColor();
        repaint();
    }

    public void setIcon(String iconLoc) {
        if (icon == null) {
            icon = new IconFigure();
            add(icon);
        }
        icon.setSource(iconLoc);
    }

    public void setLabelAlignment(int position){
        //TODO
    }
}