package com.xrosstools.idea.gef.control;

import com.xrosstools.idea.gef.figures.Figure;

/**
 * 定义图形编辑器的交互状态机接口。
 * 每个状态（如 ready, figureSelected）都实现此接口。
 */
public interface InteractionHandle {
    /** 进入该状态时调用 */
    default void enter() {}

    /** 离开该状态时调用 */
    default void leave() {}

    /** 鼠标事件处理 */
    default void mousePressed(MouseEventData e) {}
    default void mouseReleased(MouseEventData e) {}
    default void mouseClicked(MouseEventData e) {}
    default void mouseMoved(MouseEventData e) {}
    default void mouseDragged(MouseEventData e) {}
    default void mouseEntered(MouseEventData e) {}
    default void mouseExited(MouseEventData e) {}
    default void mouseWheelMoved(MouseEventData e) {}

    /** 键盘事件处理 */
    default void keyTyped(KeyEventData e) {}
    default void keyPressed(KeyEventData e) {}
    default void keyReleased(KeyEventData e) {}

    /** 可选的反馈图形（用于临时绘制） */
    default Figure getFeedback() { return null; }

    /** 状态标识（用于调试） */
    default String getId() { return getClass().getSimpleName(); }
}