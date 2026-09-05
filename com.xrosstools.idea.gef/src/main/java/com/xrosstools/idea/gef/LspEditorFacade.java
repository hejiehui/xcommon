package com.xrosstools.idea.gef;

import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.*;
import com.xrosstools.idea.gef.control.EditorInteraction;
import com.xrosstools.idea.gef.control.KeyEventData;
import com.xrosstools.idea.gef.control.MouseEventData;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.tools.ExportPngAction;
import com.xrosstools.idea.gef.tools.RedoAction;
import com.xrosstools.idea.gef.tools.SearchModelAction;
import com.xrosstools.idea.gef.tools.UndoAction;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// For client like VS Code, Eclipse, etc
public class LspEditorFacade<T extends IPropertySource> implements EditorFacade<T> {
    public static final String PRE_PALETTE = "palette_";
    public static final String PRE_TOOL = "tool_";

    private EditorInteraction<T> editorInteraction;
    private ContextMenuProvider outlineContextMenuProvider;

    private PanelContentProvider<T> provider;
    private final String uri;

    private Map<String, ActionListener> controls = new HashMap<>();
    // 暂存数据
    private Figure rootFigure;
    private Figure selectedFigure;
    private Object selectedModel;
    private Figure feedbackFigure;
    private String tooltipText;
    private Object modelToSave;
    private JPopupMenu popupMenu;
    private int popupX, popupY;

    // 响应数据（每次 refreshVisual 后更新）
    private JsonObject lastResponse;

    public LspEditorFacade(String uri, String content, PanelContentProvider<T> provider) throws Exception {
        this.uri = uri;
        this.provider = provider;
        editorInteraction = new EditorInteraction<T>(this, provider.getContextMenuProvider());
        editorInteraction.setModel(provider.convert(content));
        outlineContextMenuProvider = provider.getOutlineContextMenuProvider();
        outlineContextMenuProvider.setExecutor(editorInteraction);

        registerListener();
    }
    private void registerListener() {
    }

    public List<ControlItem> getPaletteItems() {
        List<ControlItem> paletteItems = new ArrayList<>();
        JPanel palettePanel = new JPanel();
        provider.buildPalette(palettePanel);
        for(Component c: palettePanel.getComponents()){
            if(c instanceof JButton) {
                JButton jb = (JButton) c;
                String id = PRE_PALETTE + jb.getText();
                paletteItems.add(new ControlItem(id, jb.getText(), jb.getToolTipText(), id));
                controls.put(id, jb.getAction());
            }
        }
        return paletteItems;
    }

    public List<ControlItem> getToolbarItems() {
        List<ControlItem> toolbarItems = new ArrayList<>();

        DefaultActionGroup actionGroup = (DefaultActionGroup) provider.createToolbar();

        AnAction[] actions = actionGroup.getChildActionsOrStubs();

        for (AnAction action : actions) {
            toolbarItems.add(convert(action));
        }

        toolbarItems.add(convert(new UndoAction(editorInteraction)));
        toolbarItems.add(convert(new RedoAction(editorInteraction)));

        toolbarItems.add(convert(new SearchModelAction(editorInteraction)));
//        toolbarItems.add(convert(new ExportPngAction(this)));

        return toolbarItems;
    }

    private ControlItem convert(AnAction action) {
        ControlItem controlItem = null;
        String text = action.getTemplatePresentation().getText();
        String tooltipText = action.getTemplatePresentation().getDescription();
        String id = PRE_TOOL + text;
        controlItem = new ControlItem(id, text, tooltipText, id);

        controls.put(id, e -> action.actionPerformed(null));
        return controlItem;
    }

    public JsonObject execute(String command, JsonObject parameters) {
        return null;
    }

    @Override
    public synchronized void setToolTipText(String tooltip) {
        this.tooltipText = tooltip;
    }

    @Override
    public synchronized void updateRootFigure(Figure rootFigure) {
        this.rootFigure = rootFigure;
    }

    @Override
    public synchronized void updateSelectedFigure(Figure selectedFigure) {
        this.selectedFigure = selectedFigure;
    }

    @Override
    public synchronized void updateSelectedModel(Object model) {
        this.selectedModel = model;
    }

    @Override
    public synchronized void updateFeedbackFigure(Figure feedbackFigure) {
        this.feedbackFigure = feedbackFigure;
    }

    @Override
    public synchronized void refreshVisual() {
        // 构建完整响应 JSON
        JsonObject response = new JsonObject();
        response.addProperty("uri", uri);

        // 根图形（完整图形树）
        if (rootFigure != null) {
            JsonObject graphicModel = rootFigure.getGraphicModel();
            if (graphicModel != null) {
                response.add("graphicModel", graphicModel);
            }
        }

        // 选中的图形（可选）
        // TODO this is unnecessary
        if (selectedFigure != null) {
            JsonObject selected = new JsonObject();
            selected.addProperty("type", selectedFigure.getClass().getSimpleName());
            // 可以加入更多选中信息，如 id 等
            response.add("selectedFigure", selected);
        }

        // 选中的业务模型（用于属性面板）
        if (selectedModel != null) {
            // 如果 selectedModel 实现了 IPropertySource，可以提取属性
            // 这里简化，只传类名
            JsonObject modelInfo = new JsonObject();
            modelInfo.addProperty("className", selectedModel.getClass().getName());
            response.add("selectedModel", modelInfo);
        }

        // 反馈图形（如连接线预览）
        if (feedbackFigure != null) {
            JsonObject feedback = feedbackFigure.getGraphicModel();
            if (feedback != null) {
                response.add("feedbackFigure", feedback);
            }
        }

        // Tooltip（可选）
        if (tooltipText != null) {
            response.addProperty("tooltip", tooltipText);
        }

        // 保存最后响应
        this.lastResponse = response;

        // 清空暂存（避免重复发送旧数据）
        clearCache();
    }

    @Override
    public synchronized void save(T model) {
        this.modelToSave = model;
        // 这里可以立即保存，或延迟到 refreshVisual 后统一保存
        // 根据您的需求，可以选择立即保存或暂存后统一处理
        // 示例：立即保存（调用外部服务）
        // saveModel(uri, model);
    }

    @Override
    public synchronized void showContextMenu(int x, int y, JPopupMenu menu) {
        this.popupX = x;
        this.popupY = y;
        this.popupMenu = menu;
        // 对于 LSP 后端，上下文菜单由前端实现，所以这里只记录位置，不实际显示
    }

    /**
     * 获取最后一次 refreshVisual 构建的完整响应。
     * 调用后不会清除响应，可以多次获取。
     */
    public synchronized JsonObject getLastResponse() {
        return lastResponse;
    }

    /**
     * 清除暂存数据（在 refreshVisual 中调用，避免重复发送）
     */
    private void clearCache() {
        tooltipText = null;
        rootFigure =  null;
        selectedFigure = null;
        selectedModel  = null;
        feedbackFigure  = null;
        modelToSave = null;
        popupMenu = null;
    }

    // 如果需要保存模型，可以提供一个外部注入的保存函数
    // private void saveModel(String uri, Object model) { ... }

    public JsonObject handleInputEvent(String eventType, JsonObject data) {
        switch (eventType) {
            case "MOUSE_PRESSED":
                editorInteraction.mousePressed(toMouseEventData(data));
                break;
            case "MOUSE_RELEASED":
                editorInteraction.mouseReleased(toMouseEventData(data));
                break;
            case "MOUSE_CLICKED":
                editorInteraction.mouseClicked(toMouseEventData(data));
                break;
            case "MOUSE_MOVED":
                editorInteraction.mouseMoved(toMouseEventData(data));
                break;
            case "MOUSE_DRAGGED":
                editorInteraction.mouseDragged(toMouseEventData(data));
                break;
            case "MOUSE_ENTERED":
                editorInteraction.mouseEntered(toMouseEventData(data));
                break;
            case "MOUSE_EXITED":
                editorInteraction.mouseExited(toMouseEventData(data));
                break;
            case "MOUSE_WHEEL":
                editorInteraction.mouseWheelMoved(toMouseEventData(data));
                break;
            case "KEY_PRESSED":
                editorInteraction.keyPressed(toKeyEventData(data));
                break;
            case "KEY_RELEASED":
                editorInteraction.keyReleased(toKeyEventData(data));
                break;
            case "KEY_TYPED":
                editorInteraction.keyTyped(toKeyEventData(data));
                break;
        }

        return getLastResponse();
    }

    // TODO simplify the data mapping
    private MouseEventData toMouseEventData(JsonObject data) {
        MouseEventData e = new MouseEventData();
        e.setId(getEventId(data.get("type").getAsString()));
        e.setX(data.get("x").getAsInt());
        e.setY(data.get("y").getAsInt());
        e.setClickCount(data.get("clickCount").getAsInt());
        e.setButton(data.get("button").getAsInt());
        e.setModifiersEx(data.get("modifiersEx").getAsInt());
        e.setPopupTrigger(data.get("popupTrigger").getAsBoolean());
        e.setWhen(data.get("when").getAsLong());
        return e;
    }

    private KeyEventData toKeyEventData(JsonObject data) {
        KeyEventData e = new KeyEventData();
        e.setId(getEventId(data.get("type").getAsString()));
        e.setKeyCode(data.get("keyCode").getAsInt());
        e.setKeyChar(data.get("keyChar").getAsString().charAt(0));
        e.setModifiersEx(data.get("modifiersEx").getAsInt());
        e.setWhen(data.get("when").getAsLong());
        return e;
    }

    private int getEventId(String type) {
        switch (type) {
            case "MOUSE_PRESSED": return MouseEventData.MOUSE_PRESSED;
            case "MOUSE_RELEASED": return MouseEventData.MOUSE_RELEASED;
            case "MOUSE_CLICKED": return MouseEventData.MOUSE_CLICKED;
            case "MOUSE_MOVED": return MouseEventData.MOUSE_MOVED;
            case "MOUSE_DRAGGED": return MouseEventData.MOUSE_DRAGGED;
            case "MOUSE_ENTERED": return MouseEventData.MOUSE_ENTERED;
            case "MOUSE_EXITED": return MouseEventData.MOUSE_EXITED;
            case "MOUSE_WHEEL": return MouseEventData.MOUSE_WHEEL;
            case "KEY_PRESSED": return KeyEventData.KEY_PRESSED;
            case "KEY_RELEASED": return KeyEventData.KEY_RELEASED;
            case "KEY_TYPED": return KeyEventData.KEY_TYPED;
            default: return -1;
        }
    }

    static class ControlItem {
        String id;
        String label;
        String tooltip;
        String icon;
        ControlItem(String id, String label, String tooltip, String icon) {
            this.id = id;
            this.tooltip = tooltip;
            this.label = label;
            this.icon = icon;
        }
    }

    static class PropertyItem {
        String category;
        String id;
        String label;
        Object value;
        String[] options;
        PropertyItem(String id, String label, Object value, String[] options) {
            this(null, id, label, value, options);
        }

        PropertyItem(String category, String id, String label, Object value, String[] options) {
            this.category = category;
            this.id = id;
            this.label = label;
            this.value = value;
            this.options = options;
        }
    }

    static class TreeItem {
        String id;
        String label;
        String tooltip;
        String icon;
        List<TreeItem> children;
        TreeItem(String id, String label, String tooltip, String icon) {
            this.id = id;
            this.label = label;
            this.tooltip = tooltip;
            this.icon = icon;
        }
    }
}