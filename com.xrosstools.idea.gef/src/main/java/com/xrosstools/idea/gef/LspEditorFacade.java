package com.xrosstools.idea.gef;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.ActionContainer;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;
import com.xrosstools.idea.gef.core.*;
import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.tools.ExportPngAction;
import com.xrosstools.idea.gef.tools.RedoAction;
import com.xrosstools.idea.gef.tools.SearchModelAction;
import com.xrosstools.idea.gef.tools.UndoAction;
import com.xrosstools.idea.gef.util.IPropertyDescriptor;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

// For client like VS Code, Eclipse, etc
public class LspEditorFacade<T extends IPropertySource> implements EditorFacade<T> {
    // Command list
    public static final String INITIALIZE = "initialize";
    public static final String HANDLE_INPUT_EVENT = "handleInputEvent";
    public static final String SELECT_MODEL = "selectModel";
    public static final String SELECT_TREE_NODE = "selectTreeNode";
    public static final String SELECT_PALETTE = "selectPalette";
    public static final String SELECT_TOOL = "selectTool";
    public static final String SELECT_CONTEXT_MENU = "selectContextMenu";
    public static final String GET_TREE_NODE_CONTEXT_MENU = "getTreeNodeContextMenu";
    public static final String SUBMIT_INPUT = "submitInputs";
    public static final String UPDATE_PROPERTY = "updateProperty";
    public static final String GET_XML = "getXml";

    public static final String[] COMMANDS = new String[] {
            INITIALIZE,
            HANDLE_INPUT_EVENT,
            SELECT_MODEL, SELECT_TREE_NODE,
            SELECT_PALETTE, SELECT_TOOL, SELECT_CONTEXT_MENU, GET_TREE_NODE_CONTEXT_MENU,
            SUBMIT_INPUT,UPDATE_PROPERTY,GET_XML};

    // Event list
    public static final String MOUSE_PRESSED = "MOUSE_PRESSED";
    public static final String MOUSE_RELEASED = "MOUSE_RELEASED";
    public static final String MOUSE_CLICKED = "MOUSE_CLICKED";
    public static final String MOUSE_MOVED = "MOUSE_MOVED";
    public static final String MOUSE_DRAGGED = "MOUSE_DRAGGED";
    public static final String MOUSE_ENTERED = "MOUSE_ENTERED";
    public static final String MOUSE_EXITED = "MOUSE_EXITED";
    public static final String MOUSE_WHEEL = "MOUSE_WHEEL";
    public static final String KEY_PRESSED = "KEY_PRESSED";
    public static final String KEY_RELEASED = "KEY_RELEASED";
    public static final String KEY_TYPED = "KEY_TYPED";


    public static final String PALETTE = "palette";
    public static final String TOOLBAR = "toolbar";
    public static final String ID = "id";
    public static final String CATEGORY = "category";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String LABEL = "label";
    public static final String TOOLTIP = "tooltip";
    public static final String ICON_ID = "iconId";
    public static final String CHECKED = "checked";
    public static final String SUB_ITEMS = "subItems";
    public static final String IS_SEPARATOR = "isSeparator";

    private EditorInteraction<T> editorInteraction;
    private ContentProvider<T> provider;

    private final String uri;

    private static Gson gson = new Gson();
    private Map<String, ActionListener> paletteItems = new HashMap<>();
    private Map<String, ActionListener> toolbarItems = new HashMap<>();
    private Map<String, ActionListener> menuItems = new HashMap<>();

    private Map<Icon, String> iconMap;
    private Map<String, String> iconDataCache = new HashMap<>();

    // 暂存数据
    private Figure rootFigure;
    private Object selectedModel;

    private Figure insertionTarget;

    private String tooltipText;
    private String modelToSave;
    private Action[] popupMenu;

    private String curCommand;
    private String curEventType;

    public LspEditorFacade(String uri, String content, ContentProvider<T> provider) throws Exception {
        this.uri = uri;
        this.provider = provider;
        iconMap = initIconMap();
        editorInteraction = new EditorInteraction<T>(this, provider, iconMap);
        editorInteraction.setModel(provider.convert(content));
    }

    public JsonObject contentChanged(String content) {
        try {
            editorInteraction.setModel(provider.convert(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getLastResponse();
    }

    private Map<Icon, String> initIconMap() {
        Map<Icon, String> iconMap = getDefaultIconMap();

        loadIconAsDataUri(UndoAction.ID, this.getClass());
        loadIconAsDataUri(RedoAction.ID, this.getClass());
        loadIconAsDataUri(SearchModelAction.ID, this.getClass());
        loadIconAsDataUri(ExportPngAction.ID, this.getClass());

        for (String iconId : iconMap.values()) {
            loadIconAsDataUri(iconId, getClass());
        }

        Map<Icon, String> additionalIconMap = provider.getIconMap();
        if (additionalIconMap.isEmpty()) return iconMap;

        for (String iconId : additionalIconMap.values()) {
            loadIconAsDataUri(iconId, provider.getClass());
        }

        iconMap.putAll(additionalIconMap);
        return iconMap;
    }

    private JsonArray initPalette() {
        JsonArray palette = new JsonArray();
        for(Action action: provider.getPaletteItems(editorInteraction)){
            palette.add(convert(action));
            paletteItems.put(action.getText(), action);
        }

        return palette;
    }

    private JsonArray initToolbar() {
        JsonArray toolbar = new JsonArray();
        for(Action action: provider.getToolbarItems(editorInteraction))
            toolbar.add(convert(toolbarItems, action));

        toolbar.add(convert(toolbarItems, new UndoAction(editorInteraction)));
        toolbar.add(convert(toolbarItems, new RedoAction(editorInteraction)));

//        toolbar.add(ControlItem.convert(toolbarItems, new SearchModelAction(editorInteraction)));
//        toolbar.add(convert(new ExportPngAction(this)));

        return toolbar;
    }

    private JsonObject initialize() {
        updateSelectedModel(editorInteraction.getModel());
        updateRootFigure(editorInteraction.getRoot().getFigure());

        JsonObject response = getLastResponse();
        if (response == null) {
            response = new JsonObject();
        }
        response.addProperty("uri", uri);

        response.add(PALETTE, initPalette());
        response.add(TOOLBAR, initToolbar());
        response.add("icons", buildIconMap());

        return response;
    }

    /**
     * 构建 iconId -> data URI 的映射。
     */
    private JsonObject buildIconMap() {
        JsonObject icons = new JsonObject();

        for (String iconId : iconMap.values()) {
            icons.addProperty(iconId, iconDataCache.get(iconId));
        }

        return icons;
    }

    private Map<Icon, String> getDefaultIconMap() {
        Map<Icon, String> iconMap =new HashMap<>();

        iconMap.put(GefIcons.Undo, UndoAction.ID);
        iconMap.put(GefIcons.Redo, RedoAction.ID);
        iconMap.put(GefIcons.SEARCH, SearchModelAction.ID);
        iconMap.put(GefIcons.EXPORT_PNG, ExportPngAction.ID);

        return iconMap;
    }

    public synchronized JsonObject execute(String command, JsonObject parameters) {
        try {
            curCommand = command;
            switch (command) {
                case INITIALIZE:
                    return initialize();
                case SELECT_MODEL:
                    return selectModel(parameters.get(ID).getAsString());
                case SELECT_TREE_NODE:
                    return selectTreeNode(parameters.get(ID).getAsString());
                case SELECT_PALETTE:
                    return selectPalette(parameters.get(ID).getAsString());
                case SELECT_TOOL:
                    return selectTool(parameters.get(ID).getAsString());
                case SELECT_CONTEXT_MENU:
                    return selectContextMenu(parameters.get(ID).getAsString());
                case GET_TREE_NODE_CONTEXT_MENU:
                    return  getTreeNodeContextMenu(parameters.get(ID).getAsString());
                case SUBMIT_INPUT:
                    return submitInputs(parameters.get(ID).getAsString(),parameters.get(VALUES).getAsJsonObject());
                case UPDATE_PROPERTY:
                    return updateProperty(
                            parameters.get(CATEGORY) == null ? null: parameters.get(CATEGORY).getAsString(),
                            parameters.get(ID).getAsString(),
                            parameters.get(VALUE).getAsString());
                case GET_XML:
                    return getXml();
                case HANDLE_INPUT_EVENT:
                    return handleInputEvent(parameters.get("eventType").getAsString(), parameters.get("data").getAsJsonObject());
                default:
                    throw new IllegalArgumentException("Unknown command: " + command);
            }
        }catch (Throwable e){
            log(e.getMessage());
            e.printStackTrace(System.err);
            throw e;
        }
    }

    private JsonObject handleInputEvent(String eventType, JsonObject data) {
        curEventType = eventType;
        switch (eventType) {
            case MOUSE_PRESSED:
                editorInteraction.mousePressed(toMouseEventData(data));
                break;
            case MOUSE_RELEASED:
                editorInteraction.mouseReleased(toMouseEventData(data));
                break;
            case MOUSE_CLICKED:
                editorInteraction.mouseClicked(toMouseEventData(data));
                break;
            case MOUSE_MOVED:
                editorInteraction.mouseMoved(toMouseEventData(data));
                break;
            case MOUSE_DRAGGED:
                editorInteraction.mouseDragged(toMouseEventData(data));
                break;
            case MOUSE_ENTERED:
                editorInteraction.mouseEntered(toMouseEventData(data));
                break;
            case MOUSE_EXITED:
                editorInteraction.mouseExited(toMouseEventData(data));
                break;
            case MOUSE_WHEEL:
                editorInteraction.mouseWheelMoved(toMouseEventData(data));
                break;
            case KEY_PRESSED:
                editorInteraction.keyPressed(toKeyEventData(data));
                break;
            case KEY_RELEASED:
                editorInteraction.keyReleased(toKeyEventData(data));
                break;
            case KEY_TYPED:
                editorInteraction.keyTyped(toKeyEventData(data));
                break;
            default:
                log("eventType == null");
        }

        return getLastResponse();
    }

    private JsonObject selectModel(String id) {
        Object model = editorInteraction.getRoot().findEditPartById(id).getModel();
        editorInteraction.selectModel(model);
        // focus main window
        return getLastResponse();
    }

    private JsonObject selectTreeNode(String id) {
        log("selectTreeNode: " + id);
        Object model = editorInteraction.getTreeRoot().findEditPartById(id).getModel();
        editorInteraction.selectTreeModel(model);
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
        AbstractTreeEditPart treePart = (AbstractTreeEditPart)editorInteraction.getTreeRoot().findEditPartById(id);
        showContextMenu(-1, -1, editorInteraction.getOutlineContextMenuItems(treePart));

        return getLastResponse();
    }

    private JsonObject submitInputs(String id, JsonObject values) {
        return getLastResponse();
    }

    private JsonObject updateProperty(String cat, String id, Object value) {
        IPropertySource model = (IPropertySource) selectedModel;
        PropertyChangeCommand cmd = cat == null ?
                new PropertyChangeCommand((IPropertySource) selectedModel, id, value):
                new PropertyChangeCommand((IPropertySource) selectedModel, cat, id, value);
        editorInteraction.execute(cmd);
        return getLastResponse();
    }

    private JsonObject getXml() {
        log("getXml");
        save(editorInteraction.getModel());
        return getLastResponse();
    }

    @Override
    public synchronized void setToolTipText(String tooltip) {
//        log("tooltip" + tooltip);
        this.tooltipText = tooltip;
    }

    @Override
    public synchronized void updateRootFigure(Figure rootFigure) {
//        log("updateRootFigure: " + rootFigure.getId());
//        this.rootFigure = rootFigure;
    }

    @Override
    public synchronized void updateSelectedFigure(Figure selectedFigure) {
//        this.rootFigure = editorInteraction.getRoot().getFigure();
    }

    @Override
    public synchronized void updateSelectedModel(Object model) {
        this.selectedModel = model;
    }

    @Override
    public synchronized void updateFeedbackFigure(Figure feedbackFigure) {
    }

    public synchronized void refreshVisual() {
//        log("refreshVisual");
    }

    public synchronized JsonObject getLastResponse() {
        // 构建完整响应 JSON
        JsonObject response = new JsonObject();
        response.addProperty("uri", uri);

        // 根图形（完整图形树）
        if (showRootFigure()) {
            response.add("graphicModel", getRootFigure());
            log("Response rootFigure: " + rootFigure.getId());
        }

        // Tree outline
        response.add("outlineModel", gson.toJsonTree(convert(editorInteraction.getTreeRoot())));

        // 选中的图形（可选）
        response.add("selectedFigure", getSelected());

        // 选中的业务模型（用于属性面板）
        if (selectedModel instanceof IPropertySource) {
            response.add("selectedModel", gson.toJsonTree(PropertyItem.convert((IPropertySource)selectedModel)).getAsJsonArray());
        }

        getFeedback(response);

        // Tooltip（可选）
        if (tooltipText != null) {
            response.addProperty("tooltip", tooltipText);
        }

        // Context Menu
        if (popupMenu != null) {
            response.add("popupMenu", convertContextMenu(popupMenu));
        }

        // Model content
        if (modelToSave != null) {
            response.addProperty("modelContent", modelToSave);
        }

        // 清空暂存（避免重复发送旧数据）
        clearCache();

        return response;
    }

    private boolean showRootFigure() {
        if( curCommand != null && !curCommand.equalsIgnoreCase(HANDLE_INPUT_EVENT) )
            return true;

        boolean result = !(curEventType == null ||
                curEventType.equalsIgnoreCase(MOUSE_MOVED) ||
                curEventType.equalsIgnoreCase(MOUSE_ENTERED) ||
                curEventType.equalsIgnoreCase(MOUSE_EXITED) ||
                curEventType.equalsIgnoreCase(MOUSE_WHEEL));
        if (result) {
            log("curEventType: " + curEventType);
        }
        return result;
    }

    private JsonObject getRootFigure() {
        rootFigure = editorInteraction.getRoot().getFigure();
        Dimension size = rootFigure.getPreferredSize();
        rootFigure.setSize(size);
        size.height += 100;
        return rootFigure.getGraphicModel();
    }

    private void getFeedback(JsonObject response) {
        if(!isFeedback) return;

        response.addProperty("selectableSource", editorInteraction.getLastSelectableSource() == null ? null : editorInteraction.getLastSelectableSource().getId());
        response.addProperty("selectableTarget", editorInteraction.getLastSelectableTarget() == null ? null : editorInteraction.getLastSelectableTarget().getId());

        if (editorInteraction.getLastHover() != null) {
            insertionTarget = editorInteraction.getLastHover();
            response.addProperty("insertionTarget", insertionTarget == null ? null : insertionTarget.getId());
//            log("insertionTarget");
            if (insertionTarget != null && editorInteraction.getInsertionPoint() != null) {
                JsonObject location = new JsonObject();
                Point insertionPoint = editorInteraction.getInsertionPoint();
                location.addProperty("x", insertionPoint.getX());
                location.addProperty("y", insertionPoint.getY());
                response.add("insertionPoint", location);
//                log("insertionPoint");
            }
        }
    }

    private JsonObject getSelected() {
        Figure lastSelected = editorInteraction.getLastSelected();
        if (lastSelected == null)
            return null;

        if(lastSelected instanceof Connection) {
            log("Connection");
            return lastSelected.getGraphicModel();
        }

        if (lastSelected.getMoveFeedbackLocation() != null) {
            JsonObject selected = new JsonObject();
            selected.addProperty("id", lastSelected.getId());

            JsonObject feedbackLocation = new JsonObject();
            Point moveFeedbackLocation = lastSelected.getMoveFeedbackLocation();
            feedbackLocation.addProperty("x", moveFeedbackLocation.x);
            feedbackLocation.addProperty("y", moveFeedbackLocation.y);
            selected.add("moveFeedbackLocation", feedbackLocation);

            log("selected");
            return selected;
        }

        return null;
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
    public synchronized void showContextMenu(int x, int y, Action[] actions) {
        this.popupMenu = actions;
        menuItems.clear();
        // 对于 LSP 后端，上下文菜单由前端实现，所以这里只记录位置，不实际显示
    }

    private JsonArray convertContextMenu(Action[] actions) {
        JsonArray menu = new JsonArray();
        int i = 0;
        for (Action action : actions) {
            menu.add(convertContextMenu(String.valueOf(i++), action));
        }
        return menu;
    }

    private JsonObject convertContextMenu(String id, Action action) {
        JsonObject menuItem = new JsonObject();
        menuItem.addProperty(ID, id);
        if (action instanceof ActionContainer) {
            menuItem.addProperty(LABEL, action.getText());
            menuItem.addProperty(TOOLTIP, action.getTooltip());
            JsonArray subMenu = new JsonArray();
            int i = 0;
            for(Action item: ((ActionContainer)action).getSubItems()){
                subMenu.add(convertContextMenu(id + "-" + i++, item));
            }
            menuItem.add("submenu", subMenu);
        } else {
            menuItem.addProperty(IS_SEPARATOR, action == Action.SEPARATOR);
            menuItems.put(id, action);
        }
        return menuItem;
    }

    /**
     * 清除暂存数据（在 refreshVisual 中调用，避免重复发送）
     */
    private void clearCache() {
        curEventType = null;
        curCommand = null;
        tooltipText = null;
        rootFigure = null;
//        selectedModel  = null;
        modelToSave = null;
        popupMenu = null;
    }

    // 如果需要保存模型，可以提供一个外部注入的保存函数
    // private void saveModel(String uri, Object model) { ... }
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
        e.setKey(data.get("key").getAsString().toLowerCase());
        return e;
    }

    private int getEventId(String type) {
        switch (type) {
            case MOUSE_PRESSED: return MouseEventData.MOUSE_PRESSED;
            case MOUSE_RELEASED: return MouseEventData.MOUSE_RELEASED;
            case MOUSE_CLICKED: return MouseEventData.MOUSE_CLICKED;
            case MOUSE_MOVED: return MouseEventData.MOUSE_MOVED;
            case MOUSE_DRAGGED: return MouseEventData.MOUSE_DRAGGED;
            case MOUSE_ENTERED: return MouseEventData.MOUSE_ENTERED;
            case MOUSE_EXITED: return MouseEventData.MOUSE_EXITED;
            case MOUSE_WHEEL: return MouseEventData.MOUSE_WHEEL;
            case KEY_PRESSED: return KeyEventData.KEY_PRESSED;
            case KEY_RELEASED: return KeyEventData.KEY_RELEASED;
            case KEY_TYPED: return KeyEventData.KEY_TYPED;
            default: return -1;
        }
    }

    private JsonObject convert(Map<String, ActionListener> actionMap, Action action) {
        JsonObject jsonObject = convert(action);
        actionMap.put(action.getText(), action);
        return jsonObject;
    }

    private JsonObject convert(Action action) {
        JsonObject e = new JsonObject();
        e.addProperty(ID, action.getText());
        e.addProperty(TOOLTIP, action.getTooltip());
        e.addProperty(ICON_ID, iconMap.get(action.getIcon()));

        if(action instanceof ActionContainer) {
            JsonArray subItems = new JsonArray();
            for(Action child: ((ActionContainer)action).getSubItems()) {
                subItems.add(convert(child));
            }
            e.add(SUB_ITEMS, subItems);
        }

        return e;
    }

    private void loadIconAsDataUri(String iconId, Class<?> clazz) {
        if (iconDataCache.containsKey(iconId)) return;

        String dataUri = null;
        String path = "/icons/" + iconId + ".png";
        try (InputStream is = clazz.getResourceAsStream(path)) {
            byte[] bytes = readAllBytes(is);
            dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.err.println("=== icon load failed: " + path + " -> " + e.getMessage());
        }

        iconDataCache.put(iconId, dataUri);
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    static class PropertyItem {
        String category;
        String id;
        String label;
        Object value;
        String[] options;

        public static List<PropertyItem> convert(IPropertySource propertySource) {
            List<PropertyItem>  items = new ArrayList<>();
            for(IPropertyDescriptor descriptor: propertySource.getPropertyDescriptors()) {
                PropertyItem item = new PropertyItem();

                item.category = descriptor.getCategory();
                item.id = descriptor.getId().toString();
                item.label = descriptor.getLabel();
                item.value = item.category == null ? propertySource.getPropertyValue(item.id) : propertySource.getPropertyValue(item.category, item.id);
                //TODO options
                items.add(item);
            }

            return items;
        }
    }

    static class TreeItem {
        String id;
        String label;
        String iconId;
        List<TreeItem> children = new ArrayList<>();
    }

    private TreeItem convert(AbstractTreeEditPart treeNode) {
        TreeItem item = new TreeItem();
        item.id = treeNode.getId();
        item.label = treeNode.getText();
        item.iconId = iconMap.get(treeNode.getImage());

        for (Object child: treeNode.getChildren()) {
            item.children.add(convert((AbstractTreeEditPart)child));
        }
        return item;
    }

    private void log(String message) {
        if(message != null && !message.startsWith("setToolTipText"))
            System.err.println(message);
    }

    private String lastEvt;
    private void delog(String message) {
        if(message == null || lastEvt == null || lastEvt.equals(message)) {
            lastEvt = message;
            return;
        }

        log(message);
        lastEvt = message;
    }

    private final boolean isFeedback = false;
}