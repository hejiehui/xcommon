package com.xrosstools.idea.gef.figures;

import java.awt.*;

public class Text extends Figure {
    private String text;
    private int defaultHeight = 12;
    private Font font = new Font(null, Font.ROMAN_BASELINE, defaultHeight);

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if(text == null)
            return;

        Font oldFont = graphics.getFont();
        Color oldColor = graphics.getColor();

        // paint background
        if(getBackgroundColor() != null) {
            graphics.setColor(getBackgroundColor());
            graphics.fillRect(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
        }

        graphics.setColor(getForegroundColor());

        graphics.setFont(font);

        graphics.drawString(text, getInnerX(), getY() + getHeight() - getInsets().bottom);

        graphics.setFont(oldFont);
        graphics.setColor(oldColor);
    }

    @Override
    public Dimension getPreferredSize() {
        int height = getMarginHeight();
        int width = getMarginWidth();

        height += font != null ? font.getSize() : defaultHeight;

        if(text == null)
            return new Dimension(width, height);

        // 使用 FontMetrics 计算文本宽度
        if (font != null) {
            // 创建临时画布获取 FontMetrics
            Canvas canvas = new Canvas();
            FontMetrics fontMetrics = canvas.getFontMetrics(font);
            width += fontMetrics.stringWidth(text);
        } else {
            width += defaultHeight * text.length();
        }

        return new Dimension(width, height);
    }
}
