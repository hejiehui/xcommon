package com.xrosstools.idea.gef.control;

/**
 * 跨平台鼠标事件数据，用于替代 AWT MouseEvent。
 */
public class MouseEventData {
    // 事件类型常量（对应 AWT MouseEvent 的 id）
    public static final int MOUSE_CLICKED = 500;
    public static final int MOUSE_PRESSED = 501;
    public static final int MOUSE_RELEASED = 502;
    public static final int MOUSE_MOVED = 503;
    public static final int MOUSE_ENTERED = 504;
    public static final int MOUSE_EXITED = 505;
    public static final int MOUSE_DRAGGED = 506;
    public static final int MOUSE_WHEEL = 507;

    // 按钮常量
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;

    private int id;
    private int x;
    private int y;
    private int clickCount;
    private int button;
    private int modifiersEx;      // 修饰键掩码（如 Shift、Ctrl）
    private boolean popupTrigger;
    private long when;

    // 构造函数、getter/setter
    public MouseEventData() {}

    public MouseEventData(int id, int x, int y, int clickCount, int button, int modifiersEx, boolean popupTrigger, long when) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.clickCount = clickCount;
        this.button = button;
        this.modifiersEx = modifiersEx;
        this.popupTrigger = popupTrigger;
        this.when = when;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getClickCount() { return clickCount; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
    public int getButton() { return button; }
    public void setButton(int button) { this.button = button; }
    public int getModifiersEx() { return modifiersEx; }
    public void setModifiersEx(int modifiersEx) { this.modifiersEx = modifiersEx; }
    public boolean isPopupTrigger() { return popupTrigger; }
    public void setPopupTrigger(boolean popupTrigger) { this.popupTrigger = popupTrigger; }
    public long getWhen() { return when; }
    public void setWhen(long when) { this.when = when; }

    // 辅助方法
    public java.awt.Point getPoint() {
        return new java.awt.Point(x, y);
    }
}