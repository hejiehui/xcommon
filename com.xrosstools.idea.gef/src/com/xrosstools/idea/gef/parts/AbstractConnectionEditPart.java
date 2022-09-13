package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.figures.AbstractAnchor;
import com.xrosstools.idea.gef.figures.Figure;

public abstract class AbstractConnectionEditPart extends AbstractGraphicalEditPart {
    private AbstractGraphicalEditPart source;
    private AbstractGraphicalEditPart target;

    public AbstractGraphicalEditPart getSource() {
        return source;
    }

    public AbstractGraphicalEditPart getTarget() {
        return target;
    }

    public void setSource(AbstractGraphicalEditPart source) {
        this.source = source;
    }

    public void setTarget(AbstractGraphicalEditPart target) {
        this.target = target;
    }

    public Figure getSourceFigure() {
        return getSource().getFigure();
    }

    public Figure getTargetFigure() {
        return getTarget().getFigure();
    }

    public AbstractAnchor getSourceAnchor() {
        return getSource().getSourceConnectionAnchor(this);
    }

    public AbstractAnchor getTargetAnchor() {
        return getTarget().getTargetConnectionAnchor(this);
    }
}
