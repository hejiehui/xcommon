package com.xrosstools.idea.gef.actions.tools;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.gef.GefIcons;
import com.xrosstools.gef.EditorInteraction;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.util.IPropertyDescriptor;
import com.xrosstools.idea.gef.util.IPropertySource;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SearchModelAction extends Action {
    public static final String ID = "search";
    public static final String NAME = "Search";
    private static final String MESSAGE = "Text";
    private static final String NODE_SEPARATOR = "/";

    private EditorInteraction editorInteraction;
    public SearchModelAction(EditorInteraction editorInteraction) {
        setText(NAME);
        setTooltip("Search in model");
        setIcon(GefIcons.SEARCH);
        this.editorInteraction = editorInteraction;
    }

    @Override
    public void actionPerformed(ActionEvent anActionEvent) {
        String keyword = getPlatformFacade().showInputDialog(NAME, MESSAGE, GefIcons.SEARCH, "");

        if (keyword == null) return;

        List<Entry> found = new ArrayList<>();
        search(keyword, found, "", editorInteraction.getTreeRoot());
        if(found.isEmpty())
            getPlatformFacade().showErrorDialog("Error", "No model found");
        else
            getPlatformFacade().showFoundDialog(found);
    }

    private void search(String text, List<Entry> found, String parent, AbstractTreeEditPart part) {
        if(part == null)
            return;

        parent += part.getText();
        Object model = part.getModel();
        if(model instanceof IPropertySource) {
            String props = matchProperties(text, (IPropertySource)model);
            if(props != null) {
                Entry entry = new Entry();
                entry.model = model;
                entry.name = parent + props;
                found.add(entry);
            }
        }

        parent += NODE_SEPARATOR;
        for(Object child: part.getChildren()) {
            if(child != null && child instanceof AbstractTreeEditPart)
                search(text, found, parent, (AbstractTreeEditPart)child);
        }
    }

    private String matchProperties(String text, IPropertySource model) {
        List<String> props = new ArrayList<>();
        for(IPropertyDescriptor descriptor: model.getPropertyDescriptors()){
            String category = descriptor.getCategory();
            Object value = category == null ? model.getPropertyValue(descriptor.getId()) : model.getPropertyValue(category, descriptor.getId());
            if(value != null && value.toString().contains(text)) {
                String id = descriptor.getId().toString();
                if(category == null)
                    props.add(id);
                else
                    props.add(String.format("%s.%s", category, id));
            }
        }

        return props.size() == 0 ? null : props.toString();
    }

    public static class Entry {
        public String name;
        public Object model;
        public String toString() {
            return name;
        }
    }
}
