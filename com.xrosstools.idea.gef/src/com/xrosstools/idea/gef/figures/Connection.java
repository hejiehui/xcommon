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

    @Override
    public void layout() {
        layoutEndpoints();
        layout(points);
    }

    public void layout(PointList pointList) {
        if(router != null)
            router.route(this);
        for(Map.Entry<Figure, ConnectionLocator> childEntry: children.entrySet()) {
            childEntry.getKey().setLocation(childEntry.getValue().getLocation(pointList));
            childEntry.getKey().setSize(childEntry.getKey().getPreferredSize());
            childEntry.getKey().layout();
        }
    }

    /**
     * Layout start and end point of the connection
     */
    public void layoutEndpoints() {
        points.removeAllPoints();

        Point start = getSourceReferencePoint();
        Point end = getTargetReferencePoint();

        points.addPoint(getSourcePoint(end));
        points.addPoint(getTargetPoint(start));
    }

    private Point getSourceReferencePoint() {
        if(feedbackTarget == null || showSourceFeedback == false)
            return getConnectionPart().getSourceFigure().getCenter();

        return getReferencePoint();
    }

    private Point getTargetReferencePoint() {
        if(feedbackTarget == null || showSourceFeedback == true)
            return getConnectionPart().getTargetFigure().getCenter();

        return getReferencePoint();
    }

    private Point getReferencePoint() {
        return feedbackTarget instanceof Point ? (Point)feedbackTarget : ((Figure)feedbackTarget).getCenter();
    }

    private Point getSourcePoint(Point end) {
        if(feedbackTarget == null || (feedbackTarget != null && showSourceFeedback == false))
            return getConnectionPart().getSourceAnchor().getLocation(end);

        return getAnchorLocation(end);
    }

    private Point getTargetPoint(Point start) {
        if(feedbackTarget == null || (feedbackTarget != null && showSourceFeedback == true))
            return getConnectionPart().getTargetAnchor().getLocation(start);

        return getAnchorLocation(start);
    }

    private Point getAnchorLocation(Point referencePoint) {
        if(feedbackTarget instanceof Point)
            return (Point)feedbackTarget;

        Figure figure = (Figure)feedbackTarget;
        AbstractAnchor anchor = showSourceFeedback ?
                figure.getPart().getSourceConnectionAnchor(getConnectionPart()) :
                figure.getPart().getTargetConnectionAnchor(getConnectionPart());

        return anchor.getLocation(referencePoint);
    }

    public void setSourcePart(AbstractGraphicalEditPart sourcePart) {
        this.sourcePart = sourcePart;
    }

    public Figure findFigureAt(int x, int y) {
        if(feedbackTarget != null)
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

    @Override
    public void paintComponent(Graphics graphics) {
        Stroke s = setLineWidth(graphics, getLineWidth());

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


        points.removeAllPoints();

        Point start, end;
        if(feedbackTarget instanceof Point) {
            start = new ChopboxAnchor(sourceFigure).getLocation((Point) feedbackTarget);
            end = (Point)feedbackTarget;
        } else {
            start = new ChopboxAnchor(sourceFigure).getLocation(((Figure)feedbackTarget).getCenter());
            end = new ChopboxAnchor((Figure)feedbackTarget).getLocation(sourceFigure.getCenter());
        }
        points.addPoint(start);
        points.addPoint(end);

        layout(points);
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

        if(showSourceFeedback == true && feedbackTarget == getConnectionPart().getSourceFigure())
            return true;

        return showSourceFeedback == false && feedbackTarget == getConnectionPart().getTargetFigure();
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

        if (feedbackTarget instanceof Point) {
            Point feedbackLocation = new Point((Point)feedbackTarget);
            (getConnectionPart() == null ? sourcePart.getFigure() : getConnectionPart().getSourceFigure()).translateToRelative(feedbackLocation);
            this.feedbackTarget = feedbackLocation;
        }else
            this.feedbackTarget = feedbackTarget;
    }

    @Override
    public void paintSelection(Graphics graphics) {}

    public void setRouter(ConnectionRouter router) {
        this.router = router;
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
}