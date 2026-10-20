package com.xrosstools.idea.gef.core;

/**
 * 跨平台键盘事件数据，用于替代 AWT KeyEvent。
 */
public class KeyEventData {
    // 事件类型常量
    public static final int KEY_TYPED = 400;
    public static final int KEY_PRESSED = 401;
    public static final int KEY_RELEASED = 402;

    public static final String KEY_DELETE = "delete";
    public static final String KEY_ESCAPE = "escape";
    public static final String KEY_UP = "up";
    public static final String KEY_DOWN = "down";
    public static final String KEY_LEFT = "left";
    public static final String KEY_RIGHT = "right";

    private int id;
    private String key;
    private int keyCode;
    private char keyChar;
    private int modifiersEx;
    private long when;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
    public char getKeyChar() { return keyChar; }
    public void setKeyChar(char keyChar) { this.keyChar = keyChar; }
    public int getModifiersEx() { return modifiersEx; }
    public void setModifiersEx(int modifiersEx) { this.modifiersEx = modifiersEx; }
    public long getWhen() { return when; }
    public void setWhen(long when) { this.when = when; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}