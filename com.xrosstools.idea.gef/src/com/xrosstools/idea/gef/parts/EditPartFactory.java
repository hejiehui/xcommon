package com.xrosstools.idea.gef.parts;

public interface EditPartFactory {
    EditPart createEditPart(EditPart parent, Object model);

    default EditPart createEditPart(EditContext context, EditPart parent, Object model) {
        EditPart childEditPart = createEditPart(parent, model);
        childEditPart.setEditPartFactory(this);
        childEditPart.setParent(parent);
        childEditPart.setContext(context);
        context.add(childEditPart, childEditPart.getModel());
        return childEditPart;
    }
}
