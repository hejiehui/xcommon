package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.util.IPropertySource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

public abstract class AbstractEditPart implements EditPart {
    private EditPartFactory factory;
    private EditPart parent;
    private Object model;
    private EditContext editContext;

    //TODO fix the name
    abstract protected void addChildPartVisual(EditPart childEditPart, int index);

    abstract protected void removeChildVisual(EditPart childEditPart);

    abstract protected EditPart findEditPart(Object model);

    public void addNotify(){
        refresh();
    }

    public void activate(){
        if(!(getModel() instanceof IPropertySource))
            return;

        PropertyChangeSupport support = ((IPropertySource)getModel()).getListeners();
        for(PropertyChangeListener listener: support.getPropertyChangeListeners())
            if(listener == this)
                return;

        support.addPropertyChangeListener(this);
    }

    public void deactivate(){
        if(getModel() instanceof IPropertySource)
            ((IPropertySource)getModel()).getListeners().removePropertyChangeListener(this);
    }

    @Override
    public final Object getModel() {
        return model;
    }

    @Override
    public final void setModel(Object model) {
        if(this.model == model)
            return;

        this.model = model;
    }

    public List getModelChildren() {
        return Collections.emptyList();
    }

    public List getChildren() {
        return Collections.emptyList();
    }

    public final void setParent(EditPart parent) {
        this.parent = parent;
    }

    public final EditPart getParent() {
        return parent;
    }

    public final void setEditPartFactory(EditPartFactory factory) {
        this.factory = factory;
    }

    public final EditPartFactory getEditPartFactory() {
        return factory;
    }

    public final void setContext(EditContext editContext) {
        this.editContext = editContext;
    }

    public EditContext getContext() {
        return editContext;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
        repaint();
    }

    public void execute(Command cmd) {
        getContext().getContentPane().execute(cmd);
    }

    public void repaint() {
        ((EditorPanel<IPropertySource>)getContext().getContentPane()).refreshVisual();
    }

    public void refresh() {
        refreshVisuals();
        refreshChildren();
    }

    protected void refreshVisuals() {}

    protected void refreshChildren() {
        refreshModelPart(getChildren(), getModelChildren(), nodeHandler);
        for(Object obj: getChildren()) {
            EditPart childPart = (EditPart)obj;
            childPart.refresh();
        }
    }


    public final void refreshModelPart(List parts, List models, EditPartHandler handler) {
        int size = parts.size();
        Map modelToEditPart = Collections.emptyMap();
        int i;
        if(size > 0) {
            modelToEditPart = new HashMap(size);
            for(i = 0; i < size; i++) {
                EditPart editPart = (EditPart)parts.get(i);
                modelToEditPart.put(editPart.getModel(), editPart);
            }
        }

        for(i = 0; i < models.size(); i++) {
            Object model = models.get(i);
            if(i >= parts.size() || ((EditPart)parts.get(i)).getModel() != model) {
                EditPart editPart = (EditPart)modelToEditPart.get(model);
                if(editPart != null) {
                    handler.reorderChild(parts, editPart, i);
                } else {
                    handler.addChildModel(parts, model, i);
                }
            }
        }

        size = parts.size();
        if(i < size) {
            List trash = new ArrayList(size - i);
            for(; i < size; i++)
                trash.add(parts.get(i));
            for(i = 0; i < trash.size(); i++)            {
                EditPart ep = (EditPart)trash.get(i);
                handler.removeChild(parts, ep);
                getContext().remove(ep.getModel());
            }
        }
    }

    protected void defaultReorder(List parts, EditPart childPart, int index) {
        parts.remove(childPart);
        parts.add(index, childPart);
    }

    protected EditPart createOrFindPart(Object model) {
        EditPart childEditPart = findEditPart(model);
        if(childEditPart != null)
            return childEditPart;

        childEditPart = getEditPartFactory().createEditPart(getContext(), this, model);
        return childEditPart;
    }

    private EditPartHandler nodeHandler = new EditPartHandler() {
        public void reorderChild(List parts, EditPart editPart, int index) {
            removeChildVisual(editPart);
            parts.remove(editPart);
            parts.add(index, editPart);
            addChildPartVisual(editPart, index);
        }

        public void addChildModel(List parts, Object child, int index) {
            EditPart childEditPart = getEditPartFactory().createEditPart(getContext(), AbstractEditPart.this, child);
            parts.add(index, childEditPart);
            addChildPartVisual(childEditPart, index);
            childEditPart.addNotify();
            childEditPart.activate();
        }

        public void removeChild(List parts, EditPart childEditPart) {
            childEditPart.deactivate();
            removeChildVisual(childEditPart);
            childEditPart.setParent(null);
            parts.remove(childEditPart);
        }
    };
}
