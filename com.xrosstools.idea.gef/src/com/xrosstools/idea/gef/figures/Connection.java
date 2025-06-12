package com.xrosstools.idea.gef.figures;

import com.xrosstools.idea.gef.parts.AbstractConnectionEditPart;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.routers.ConnectionLocator;
import com.xrosstools.idea.gef.routers.ConnectionRouter;
import com.xrosstools.idea.gef.routers.PointList;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Connection extends Figure {
    private AbstractGraphicalEditPart sourcePart;
    private boolean showSourceFeedback;
    private Object feedbackTarget;

    private Endpoint sourceEndpoint;
    private Endpoint targetEndpoint;
    private PointList points = new PointList();
    private ConnectionRouter router;
    private RotatableDecoration sourceDecoration;
    private RotatableDecoration targetDecoration;
    private Map<Figure, ConnectionLocator> children = new LinkedHashMap<>();

    public Connection() {
        sourceEndpoint = new Endpoint();
        targetEndpoint = new Endpoint();
        add(sourceEndpoint, new ConnectionEndpointLocator(true));
        add(targetEndpoint, new ConnectionEndpointLocator(false));

        targetDecoration = new ArrowDecoration();
        add(targetDecoration, new ConnectionEndpointLocator(false));
    }

    public AbstractConnectionEditPart getConnectionPart() {
        return (AbstractConnectionEditPart)getPart();
    }

    public void add(Figure child, ConnectionLocator locator) {
        add(child);
        children.put(child, locator);
    }

    private boolean isValid() {
        if(sourcePart != null)
            return true;

        AbstractConnectionEditPart part = getConnectionPart();
        return part.getSource() != null && part.getTarget() != null;
    }

    @Override
    public void layout() {
        if(!isValid())
            return;

        layoutEndpoints();
        layout(points);
    }

    /**
     * Layout start and end point of the connection
     */
    public void layoutEndpoints() {
        points.removeAllPoints();

        Object source = getSource();
        Object target = getTarget();

        Point endRef = getReferencePoint(source);
        Point startRef = getReferencePoint(target);

        points.addPoint(getLinkPoint(source, startRef, true));
        points.addPoint(getLinkPoint(target, endRef, false));
    }

    public void layout(PointList pointList) {
        if(router != null)
            router.route(this);

        Figure parent = getParent() == null ? sourcePart.getFigure() :getParent();
        translateToRelative(parent, points);
        for(Map.Entry<Figure, ConnectionLocator> childEntry: children.entrySet()) {
            Figure figure = childEntry.getKey();
            figure.setLocation(childEntry.getValue().getLocation(pointList));
            figure.setSize(figure.getPreferredSize());
            figure.layout();
        }

        translateToAbsolute(parent, points);
    }

    private Point getReferencePoint(Object target) {
        if(target instanceof Point)
            return new Point((Point)target);

        Figure figure = (Figure)target;
        Point center = figure.getCenter();
        figure.translateToAbsolute(center);
        return center;
    }

    private Point getLinkPoint(Object target, Point refPoint, boolean isSource) {
        if(target instanceof Point)
            return new Point((Point)target);

        Figure figure = (Figure)target;
        AbstractAnchor anchor;

        if(sourcePart == null)
            anchor = isSource ?
                    figure.getPart().getSourceConnectionAnchor(getConnectionPart()) :
                    figure.getPart().getTargetConnectionAnchor(getConnectionPart());
        else
            //Because there is no connection party yet
            anchor = new ChopboxAnchor(figure);

        figure.translateToRelative(refPoint);
        Point anchorPoint = anchor.getLocation(refPoint);
        figure.translateToAbsolute(anchorPoint);
        return anchorPoint;
    }

    public void setSourcePart(AbstractGraphicalEditPart sourcePart) {
        this.sourcePart = sourcePart;
    }

    public Figure findFigureAt(int x, int y) {
        if(hasFeedback())
            return null;

        for(Figure child: children.keySet()) {
            Figure found = child.findFigureAt(x, y);
            if(found == null)
                continue;

            return found;
        }

        if(points.containsPoint(x, y))
            return this;

        return null;
    }

    @Override
    public final void painLink(Graphics graphics) {}

    public void paint(Graphics graphics) {
        if(!isValid())
            return;

        super.paint(graphics);
    }

        @Override
    public void paintComponent(Graphics graphics) {
        Stroke s = setLineWidth(graphics, getLineWidth());

        Figure parent = getParent() == null ? sourcePart.getFigure() :getParent();
        translateToRelative(parent, points);

        graphics.drawPolyline(points.getXPoints(), points.getYPoints(), points.getPoints());

        restore(graphics, s);
    }

    public void paintCreationFeedback(Graphics graphics) {
        Figure sourceFigure = sourcePart.getFigure();

        if(sourceFigure == feedbackTarget || feedbackTarget == null)
            return;

        Point zero = sourceFigure.getParent().getLocation();
        sourceFigure.getParent().translateToAbsolute(zero);
        graphics.translate(zero.x, zero.y);

        layout();

        paintComponent(graphics);
        paintChildren(graphics);

        graphics.translate(-zero.x, -zero.y);
    }

    public void clearFeedback() {
        feedbackTarget = null;
    }

    public boolean hasFeedback() {
        return feedbackTarget != null;
    }

    public boolean isConnectToSameNode() {
        if(feedbackTarget == null) {
            return getConnectionPart().getSource() == getConnectionPart().getTarget();
        }

        if(feedbackTarget instanceof Point)
            return false;

        if(showSourceFeedback == true && feedbackTarget == getConnectionPart().getTargetFigure())
            return true;

        return showSourceFeedback == false && feedbackTarget == getConnectionPart().getSourceFigure();
    }

    public void relocateSourceFeedback(Object lastHoverlocation) {
        showSourceFeedback = true;
        setFeedback(lastHoverlocation);
    }

    public void relocateTargetFeedback(Object lastHoverlocation) {
        showSourceFeedback = false;
        setFeedback(lastHoverlocation);
    }

    public Object getFeedback() {
        return feedbackTarget;
    }

    private void setFeedback(Object feedbackTarget) {
        if (feedbackTarget == null) {
            clearFeedback();
            return;
        }

        this.feedbackTarget = feedbackTarget;
    }

    @Override
    public void paintSelection(Graphics graphics) {}

    public void setRouter(ConnectionRouter router) {
        this.router = router;
    }

    public ConnectionRouter getRouter() {
        return router;
    }

    public PointList getPoints() {
        return points;
    }

    public void setSourceDecoration(RotatableDecoration sourceDecoration) {
        this.sourceDecoration = sourceDecoration;
    }

    public void setTargetDecoration(RotatableDecoration targetDecoration) {
        this.targetDecoration = targetDecoration;
    }

    public Endpoint getSourceEndpoint() {
        return sourceEndpoint;
    }

    public Endpoint getTargetEndpoint() {
        return targetEndpoint;
    }

    private class ConnectionEndpointLocator implements ConnectionLocator {
        private boolean source;
        public ConnectionEndpointLocator(boolean source) {
            this.source = source;
        }

        @Override
        public Point getLocation(PointList points) {
            Point p = source ? points.getFirst() : points.getLast();
            p = new Point(p);
            p.translate(-Endpoint.SIZE/2, -Endpoint.SIZE/2);
            return p;
        }
    }

    public Object getSource() {
        if(hasFeedback() && showSourceFeedback)
            return feedbackTarget;
        else
            return sourcePart == null ? getConnectionPart().getSourceFigure() : sourcePart.getFigure();
    }

    public Object getTarget() {
        if(hasFeedback() && !showSourceFeedback)
            return feedbackTarget;
        else
            return getConnectionPart().getTargetFigure();
    }
}