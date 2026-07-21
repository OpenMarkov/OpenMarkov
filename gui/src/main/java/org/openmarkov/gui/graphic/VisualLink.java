/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.configuration.GUIColors;

import java.awt.*;

/**
 * This class is the visual representation of a link.
 *
 * @author jmendoza
 * @version 1.0
 */
public non-sealed class VisualLink extends VisualArrow implements ClassLocalizable {
    
    /**
     * Object that has the information (included visual information) of the
     * destination node.
     */
    private VisualNode destination;
    
    /**
     * Object that has the information (included visual information) of the
     * source node.
     */
    private final VisualNode source;
    
    /**
     * Object that has the link information.
     */
    private final Link<Node> link;
    
    /**
     * Creates a new visual link from a link.
     *
     * @param newLink        object that has the information of the link.
     * @param newSource      source node.
     * @param newDestination destination node.
     */
    public VisualLink(Link<Node> newLink, VisualNode newSource, VisualNode newDestination) {
        super(newSource.getPosition(), newDestination.getPosition(), newLink.isDirected());
        
        this.link = newLink;
        this.source = newSource;
        this.destination = newDestination;
    }
    
    /**
     * Returns the source node of the link.
     *
     * @return the source node of the link.
     */
    public VisualNode getSourceNode() {
        
        return this.source;
        
    }
    
    /**
     * Returns the destination node of the link.
     *
     * @return the destination node of the link.
     */
    public VisualNode getDestinationNode() {
        
        return this.destination;
        
    }
    
    /**
     * Sets the destination node of the link.
     *
     * @param node the destination node of the link.
     */
    public void setDestinationNode(VisualNode node) {
        this.destination = node;
    }
    
    /**
     * Returns the link associated with the visual link.
     *
     * @return information of the link.
     */
    public Link<Node> getLink() {
        
        return this.link;
        
    }
    
    @Override public Shape getShape(Graphics2D g) {
        Shape sourceShape = this.source.getShape(g);
        Shape destinationShape = this.destination.getShape(g);
        Segment line = new Segment(
                new Point2D.Double(sourceShape.getBounds2D().getCenterX(), sourceShape.getBounds2D().getCenterY()),
                new Point2D.Double(destinationShape.getBounds2D().getCenterX(), destinationShape.getBounds2D()
                                                                                                .getCenterY()));
        this.setStartPoint(this.source.getCutPoint(line, g));
        this.setEndPoint(this.destination.getCutPoint(line, g));
        // 29/12/2019 When having a loop in event nodes source = destination and startPoint and endPoint are the center of the arc
        //02/02/2020 loops also in Cnance nodes so I have put an abstract method in VisualNode and overriden it in ChanceVisualNode and EventVisualNode
        if (this.source == this.destination) {
            this.setStartPoint(this.source.getSelfLoopPosition(g));
            this.setEndPoint(this.source.getSelfLoopPosition(g));
        }
        
        return super.getShape(g);
    }
    
    /**
     * Paints the visual link into the graphics object.
     *
     * @param g graphics object where paint the link.
     */
    @Override public void paint(Graphics2D g) {
        Tuple2Record<Point2D.Double, Point2D.Double> startAndEndPoint = null;
        this.isSelfLoop = this.source != null && this.source == this.destination;
        try {
            startAndEndPoint = VisualLink.shortenedPoints(g, this.source, this.destination, null);
        } catch (LinkCannotBePaintedException e) {
            return;
        }
        this.setStartPoint(startAndEndPoint.v0());
        this.setEndPoint(startAndEndPoint.v1());
        boolean hasAbsoluteLinkRestriction = this.link.hasTotalRestriction();
        this.setDoubleStriped(hasAbsoluteLinkRestriction);
        this.setSingleStriped(this.link.hasRestrictions() && !hasAbsoluteLinkRestriction);
        this.setLinkColor(this.link.hasRevealingConditions() ? GUIColors.Network.REVELATION_ARC_VARIABLE : GUIColors.Network.Link.FOREGOUND);
        super.paint(g);
    }
    
    //Cannot paint node from {source} to {destination}.
    static class LinkCannotBePaintedException extends OpenMarkovException {
        public final VisualNode source;
        public final VisualNode destination;
        
        LinkCannotBePaintedException(VisualNode source, VisualNode destination) {
            this.source = source;
            this.destination = destination;
        }
    }
    
    public static final @NotNull Tuple2Record<Point2D.Double, Point2D.Double> shortenedPoints(Graphics2D g,
                                                                                              @NotNull VisualNode source,
                                                                                              @Nullable VisualNode destination,
                                                                                              @Nullable Point2D.Double cursorPosition) throws LinkCannotBePaintedException {
        if (source == destination && source != null) {
            // 28/12/2019 allowed self lopps for Event nodes- 02/04/2020 allowed self-loops for chance nodes
            //Before adding this block, this catch was empty only has a return.
            //Now it checks if the link is a self-loop in an event node. If  it is the case the circular arrow is painted
            return Tuples.record(source.getSelfLoopPosition(g), source.getSelfLoopPosition(g));
        }
        Shape sourceShape = source.getShape(g);
        if (destination == null) {
            Segment line = new Segment(
                    new Point2D.Double(sourceShape.getBounds2D().getCenterX(), sourceShape.getBounds2D().getCenterY()),
                    new Point2D.Double(cursorPosition.getX(), cursorPosition.getY()));
            Point2D.Double sourceCutPoint = source.getCutPoint(line, g);
            if (sourceCutPoint == null) {
                return Tuples.record(
                        new Point2D.Double(sourceShape.getBounds2D().getCenterX(),
                                           sourceShape.getBounds2D().getCenterY()),
                        cursorPosition);
            }
            return Tuples.record(sourceCutPoint, cursorPosition);
        }
        Shape destinationShape = destination.getShape(g);
        Segment line = new Segment(
                new Point2D.Double(sourceShape.getBounds2D().getCenterX(), sourceShape.getBounds2D().getCenterY()),
                new Point2D.Double(destinationShape.getBounds2D().getCenterX(), destinationShape.getBounds2D()
                                                                                                .getCenterY()));
        Point2D.Double sourceCutPoint = source.getCutPoint(line, g);
        Point2D.Double cutPoint = destination.getCutPoint(line, g);
        if (sourceCutPoint == null || cutPoint == null) {
            throw new LinkCannotBePaintedException(source, destination);
        }
        return Tuples.record(sourceCutPoint, cutPoint);
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
}
