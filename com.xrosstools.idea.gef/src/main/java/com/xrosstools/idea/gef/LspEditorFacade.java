package com.xrosstools.idea.gef;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.*;
import com.xrosstools.idea.gef.control.EditorInteraction;
import com.xrosstools.idea.gef.control.KeyEventData;
import com.xrosstools.idea.gef.control.MouseEventData;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.tools.RedoAction;
import com.xrosstools.idea.gef.tools.SearchModelAction;
import com.xrosstools.idea.gef.tools.UndoAction;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// For client like VS Code, Eclipse, etc
public class LspEditorFacade<T extends IPropertySource> implements EditorFacade<T> {
    public static final String PRE_PALETTE = "palette_";
    public static final String PRE_TOOL = "tool_";
    public static final String ID = "id";
    public static final String CATEGORY = "category";
    public static final String VALUE = "value";
    public static final String LABEL = "label";
    public static final String TOOLTIP = "tooltip";


    private EditorInteraction<T> editorInteraction;

    private PanelContentProvider<T> provider;

    private final String uri;

    private Map<String, ActionListener> paletteItems = new HashMap<>();
    private Map<String, AnAction> toolbarItems = new HashMap<>();
    private Map<String, ActionListener> menuItems = new HashMap<>();

    // 暂存数据
    private Figure rootFigure;
    private Figure selectedFigure;
    private Object selectedModel;
    private Figure feedbackFigure;
    private String tooltipText;
    private String modelToSave;
    private JPopupMenu popupMenu;
    private int popupX, popupY;

    // 响应数据（每次 refreshVisual 后更新）
    private JsonObject lastResponse;

    public LspEditorFacade(String uri, String content, PanelContentProvider<T> provider) throws Exception {
        this.uri = uri;
        this.provider = provider;
        editorInteraction = new EditorInteraction<T>(this, provider));
        editorInteraction.setModel(provider.convert(content));
    }

    public void register(ContentChangeListener listener) {
        editorInteraction.register(listener);
    }

    public JsonObject contentChanged(String content) {
        try {
            editorInteraction.setModel(provider.convert(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getLastResponse();
    }

    public List<ControlItem> initPalette() {
        List<ControlItem> palette = new ArrayList<>();
        JPanel palettePanel = new JPanel();
        provider.buildPalette(palettePanel);
        for(Component c: palettePanel.getComponents()){
            if(c instanceof JButton) {
                JButton jb = (JButton) c;
                String id = PRE_PALETTE + jb.getText();
                palette.add(new ControlItem(id, jb.getText(), jb.getToolTipText(), id));
                paletteItems.put(id, jb.getAction());
            }
        }
        return palette;
    }

    public List<ControlItem> initToolbar() {
        List<ControlItem> toolbar = new ArrayList<>();

        DefaultActionGroup actionGroup = (DefaultActionGroup) provider.createToolbar();

        AnAction[] actions = actionGroup.getChildActionsOrStubs();

        for (AnAction action : actions) {
            toolbar.add(convert(action));
        }

        toolbar.add(convert(new UndoAction(editorInteraction)));
        toolbar.add(convert(new RedoAction(editorInteraction)));

        toolbar.add(convert(new SearchModelAction(editorInteraction)));
//        toolbar.add(convert(new ExportPngAction(this)));

        return toolbar;
    }

    private ControlItem convert(AnAction action) {
        ControlItem controlItem = null;
        String text = action.getTemplatePresentation().getText();
        String tooltipText = action.getTemplatePresentation().getDescription();
        String id = PRE_TOOL + text;
        controlItem = new ControlItem(id, text, tooltipText, id);

        toolbarItems.put(id, action);
        return controlItem;
    }

    public JsonObject execute(String command, JsonObject parameters) {
        switch (command) {
            case "selectModel":
                return selectModel(parameters.get(ID).getAsString());
            case "selectTreeNode":
                return selectTreeNode(parameters.get(ID).getAsString());
            case "selectPalette":
                return selectPalette(parameters.get(ID).getAsString());
            case "selectTool":
                return selectTool(parameters.get(ID).getAsString());
            case "selectContextMenu":
                return selectContextMenu(parameters.get(ID).getAsString());
            case "getTreeNodeContextMenu":
                return  getTreeNodeContextMenu(parameters.get(ID).getAsString());
            case "submitInputs":

            case "updateProperty":
                return updateProperty(
                        parameters.get(CATEGORY).getAsString(),
                        parameters.get(ID).getAsString(),
                        parameters.get(VALUE).getAsString(),);
            case "getXml":
//                return getXml(params).thenApply(r -> r);
            case "inputEvent":
                return handleInputEvent("", parameters);
            default:
                throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private JsonObject selectModel(String id) {
        editorInteraction.getRoot().findEditPart(id);
        // focus main window
        return getLastResponse();
    }

    private JsonObject selectTreeNode(String id) {
        editorInteraction.getRoot().findEditPart(id);
        return getLastResponse();
    }

    private JsonObject selectPalette(String id) {
        paletteItems.get(id).actionPerformed(null);
        return getLastResponse();
    }

    private JsonObject selectTool(String id) {
        toolbarItems.get(id).actionPerformed(null);
        return getLastResponse();
    }

    private JsonObject selectContextMenu(String id) {
        menuItems.get(id).actionPerformed(null);
        return getLastResponse();
    }

    private JsonObject getTreeNodeContextMenu(String id) {
        Object treePart = editorInteraction.getTreeRoot().findEditPart(id);
        showContextMenu(-1, -1, editorInteraction.getTreePopupMenu(treePart));

        return getLastResponse();
    }

    private JsonObject updateProperty(String cat, String id, Object value) {
        IPropertySource model = (IPropertySource) editorInteraction.getRoot().findEditPart(id).getModel();
        model.setPropertyValue(cat, id, value);
        return getLastResponse();
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

        // Context Menu
        if(popupMenu != null) {
            response.add("popupMenu", convertContextMenu("0", popupMenu));
        }

        // 保存最后响应
        this.lastResponse = response;

        // 清空暂存（避免重复发送旧数据）
        clearCache();
    }

    @Override
    public synchronized void save(T model) {
        try {
            modelToSave = provider.convert(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void showContextMenu(int x, int y, JPopupMenu menu) {
        this.popupX = x;
        this.popupY = y;
        this.popupMenu = menu;
        menuItems.clear();
        // 对于 LSP 后端，上下文菜单由前端实现，所以这里只记录位置，不实际显示
    }

    private JsonObject convertContextMenu(String id, MenuElement menuElement) {
        JsonObject menuItem = new JsonObject();
        menuItem.addProperty(ID, id);
        if(menuElement.getSubElements().length > 0) {
            menuItem.addProperty(LABEL, ((JMenu)menuElement).getText());
            menuItem.addProperty(TOOLTIP, ((JMenu)menuElement).getToolTipText());
            JsonArray subMenu = new JsonArray();
            int i = 0;
            for(MenuElement item: menuElement.getSubElements()){
                subMenu.add(convertContextMenu(id + "-" + i++, item));
            }
            menuItem.add("submenu", subMenu);
        }else {
            if(menuElement instanceof JPopupMenu)
                return menuItem;//TODO check this

            if(((JMenuItem) menuElement).getActionListeners().length != 0)
                menuItems.put(id, ((JMenuItem) menuElement).getActionListeners()[0]);
        }
        return menuItem;
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