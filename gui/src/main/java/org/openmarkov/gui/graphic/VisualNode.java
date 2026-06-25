/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.PurposeType;
import org.openmarkov.gui.configuration.GUIColor;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;


/**
 * This abstract class specifies the methods that all visual nodes have to
 * implement.
 *
 * @author jmendoza 1.0
 * @author jlgozalo 1.1
 * @version 1.2 asaez - The class is defined as abstract
 * Some new constants, attributes and methods are defined
 */
public final class VisualNode extends VisualElement implements ClassLocalizable {
    
    /**
     * Font type Helvetica, bold, size 15.
     */
    private static final Font TEXT_FONT = new Font("Helvetica", Font.BOLD, 15);
    
    /**
     * Default width of a node when it is contracted. It is the width that it
     * has if the length of its name is shorter enough; otherwise, its width is
     * adjusted to fit the length of the name.
     */
    private static final double DEFAULT_NODE_CONTRACTED_WIDTH = 40;
    
    /**
     * Width of a node when it is expanded.
     */
    static final double NODE_EXPANDED_WIDTH = 205;
    
    /**
     * Vertical margin of a node when it is expanded.
     */
    private static final double NODE_EXPANDED_HEIGHT_MARGIN = 5;
    
    /**
     * Space from the left border of the node to the foreground.
     */
    private static final double HORIZONTAL_SPACE_TO_TEXT = 15;
    
    /**
     * Space from the top border of the node to the foreground.
     */
    private static final double VERTICAL_SPACE_TO_TEXT = 4;
    
    /**
     * Object used to measure foreground in a specific font.
     */
    private static final FontMetrics FONT_METRICS = new JPanel().getFontMetrics(VisualNode.TEXT_FONT);
    
    /**
     * Visual Network to which this visual node is associated.
     */
    private final VisualNetwork visualNetwork;
    
    /**
     * Object that has the node information.
     */
    final Node node;
    
    /**
     * Object that manages the internal representation of the node when
     * it is expanded.
     */
    InnerBox innerBox;
    
    /**
     * This variable determines if the node is going to be painted
     * expanded (true) or contracted (false).
     */
    boolean expanded;
    
    /**
     * This variable indicates if the node has a pre-Resolution finding
     * established (true) or not (false).
     */
    boolean preResolutionFinding;
    
    /**
     * This variable indicates if the node has a post-Resolution finding
     * established (true) or not (false).
     */
    boolean postResolutionFinding;
    //TODO Debería ser un array de booleanos, con un valor por cada caso de evidencia
    //     (esto no pasa en el caso del preResol.)
    
    /**
     * This variable influences the width of the node.
     */
    private boolean byTitle = false;
    
    /**
     * Value of the X coordinate in temporal position of the node.
     */
    private double temporalCoordinateX;
    
    /**
     * Value of the Y coordinate in temporal position of the node.
     */
    private double temporalCoordinateY;
    
    public VisualNode(Node node, VisualNetwork visualNetwork) {
        this.node = node;
        this.visualNetwork = visualNetwork;
        this.setTemporalPosition(new Point2D.Double(node.getCoordinateX(), node.getCoordinateY()));
        this.expanded = false;
        this.preResolutionFinding = false;
        this.postResolutionFinding = false;
        this.innerBox = switch (this.node.getNodeType()) {
            case CHANCE -> switch (node.getVariable().getVariableType()) {
                case FINITE_STATES -> new FSVariableBox(this);
                case DISCRETIZED -> new DiscretizedVariableBox(this);
                case NUMERIC -> new NumericVariableBox(this);
                case EVENT -> null; //TODO
            };
            case DECISION -> new FSVariableBox(this);
            case UTILITY -> new NumericVariableBox(this, "  EU");
            case EVENT, SV_PRODUCT, SV_SUM -> null;
        };
    }
    
    /**
     * Returns the height of the visual node. It's calculated depending on the
     * font of the node and the foreground that appears in it.
     *
     * @param text foreground that appears in the visual node.
     * @param g    graphics object where to paint the element.
     *
     * @return the height of the visual node.
     */
    private static double getHeight(String text, Graphics2D g) {
        return VisualNode.FONT_METRICS.getStringBounds(text, g).getHeight();
    }
    
    /**
     * Returns the width of the visual node. It's calculated depending on the
     * font of the node and the foreground that appears in it.
     *
     * @param text foreground that appears in the visual node.
     * @param g    graphics object where to paint the element.
     *
     * @return the height of the visual node.
     */
    private static double getWidth(String text, Graphics2D g) {
        return VisualNode.FONT_METRICS.getStringBounds(text, g).getWidth();
    }
    
    /**
     * Returns the point which will be the center for a circular arrow
     * 05/04/2020 - At this time only Event and Chance Nodes may have circular arrows (self-loops)
     *
     * @param g graphics object where to paint the element.
     *
     * @return the point which will be the center for a circular arrow
     */
    public Point2D.Double getSelfLoopPosition(Graphics2D g) {
        return switch (this.node.getNodeType()) {
            case CHANCE, EVENT -> {
                RoundRectangle2D.Double shape = (RoundRectangle2D.Double) this.getShape(g);
                yield new Point2D.Double(shape.getMaxX(), shape.getMaxY());
            }
            case DECISION, UTILITY -> new Point2D.Double(this.getShape(g).getBounds2D().getMaxX(), this.getShape(g)
                                                                                                       .getBounds2D()
                                                                                                       .getMaxY());
            case SV_SUM, SV_PRODUCT -> null;
        };
    }
    
    /**
     * Returns the real position of the node.
     *
     * @return position of the node in the screen.
     */
    @Override public Point2D.Double getPosition() {
        return new Point2D.Double(this.node.getCoordinateX(), this.node.getCoordinateY());
    }
    
    /**
     * Sets the position of the node.
     *
     * @param value new position.
     */
    public void setPosition(Point2D.Double value) {
        this.node.setCoordinateX((int) value.getX());
        this.node.setCoordinateY((int) value.getY());
    }
    
    /**
     * Returns the temporal position of the node in the screen.
     *
     * @return position of the node in the screen.
     */
    public Point2D.Double getTemporalPosition() {
        
        return new Point2D.Double(this.temporalCoordinateX, this.temporalCoordinateY);
    }
    
    /**
     * Sets the temporal position of the node.
     *
     * @param value new position.
     */
    public void setTemporalPosition(Point2D.Double value) {
        this.temporalCoordinateX = value.getX();
        this.temporalCoordinateY = value.getY();
    }
    
    public void setTemporalCoordinateX(double temporalCoordinateX) {
        this.temporalCoordinateX = temporalCoordinateX;
    }
    
    public void setTemporalCoordinateY(double temporalCoordinateY) {
        this.temporalCoordinateY = temporalCoordinateY;
    }
    
    public double getTemporalCoordinateX() {
        return this.temporalCoordinateX;
    }
    
    public double getTemporalCoordinateY() {
        return this.temporalCoordinateY;
    }
    
    /**
     * Returns the string that must appear into the node. The variable 'byTitle'
     * influences this string. If 'byTitle' is true and the node hasn't a title,
     * then the name is used as title.
     *
     * @return the string that must appear into the node.
     */
    private String getNodeName() {
        return this.node.getName();
    }
    
    /**
     * Returns the node associated with the visual node.
     *
     * @return information of the node.
     */
    public Node getNode() {
        return this.node;
    }
    
    /**
     * Returns the InnerBox associated with the visual node.
     *
     * @return innerBox associated with the visual node.
     */
    public InnerBox getInnerBox() {
        return this.innerBox;
    }
    
    /**
     * Sets the inner box associated to the node.
     *
     * @param innerBox new inner box associated to the node.
     */
    public void setInnerBox(InnerBox innerBox) {
        this.innerBox = innerBox;
    }
    
    /**
     * Changes the type of the foreground (name or title) that appears inside the
     * node.
     *
     * @param newByTitle true if the title of the node will be shown; false if the name
     *                   will be shown.
     */
    public void setByTitle(boolean newByTitle) {
        this.byTitle = newByTitle;
    }
    
    /**
     * Returns true if the node will be painted expanded; false if contracted.
     *
     * @return true if the node will be painted expanded.
     */
    public boolean isExpanded() {
        return this.expanded;
    }
    
    /**
     * Establishes the way in which the node will be painted: expanded if true;
     * contracted if false.
     *
     * @param expanded true if the node has to be represented expanded; false if contracted.
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    /**
     * Returns true if the node has a pre-Resolution finding established.
     *
     * @return true if the node has a pre-Resolution finding established.
     */
    public boolean isPreResolutionFinding() {
        return this.preResolutionFinding;
    }
    
    /**
     * Sets if the node has a pre-Resolution finding established or not.
     *
     * @param findingInNode true if the node has a pre-Resolution finding established.
     */
    public void setPreResolutionFinding(boolean findingInNode) {
        this.preResolutionFinding = findingInNode;
    }
    
    /**
     * Returns true if the node has a post-Resolution finding established.
     *
     * @return true if the node has a post-Resolution finding established.
     */
    public boolean isPostResolutionFinding() {
        return this.postResolutionFinding;
    }
    
    /**
     * Sets if the node has a post-Resolution finding established or not.
     *
     * @param findingInNode true if the node has a post-Resolution finding established.
     */
    public void setPostResolutionFinding(boolean findingInNode) {
        this.postResolutionFinding = findingInNode;
    }
    
    /**
     * Returns true if the node has a finding established (pre or post-Resolution).
     *
     * @return true if the node has a finding established (pre or post-Resolution).
     */
    public boolean hasAnyFinding() {
        return this.preResolutionFinding || this.postResolutionFinding;
    }
    
    @Override public Shape getShape(Graphics2D g) {
        return switch (this.node.getNodeType()) {
            case CHANCE, EVENT -> {
                String text = this.getNodeName();
                double textHeight = VisualNode.getHeight(text, g);
                double textWidth = VisualNode.getWidth(text, g);
                double height = textHeight + 2 * VisualNode.VERTICAL_SPACE_TO_TEXT + (this.isExpanded() ?
                        this.innerBox.getInnerBoxHeight(g) + VisualNode.NODE_EXPANDED_HEIGHT_MARGIN * 2 : 0);
                double width = this.isExpanded() ? VisualNode.NODE_EXPANDED_WIDTH
                        : textWidth < textHeight ?
                          VisualNode.DEFAULT_NODE_CONTRACTED_WIDTH
                          : textWidth + 2 * VisualNode.HORIZONTAL_SPACE_TO_TEXT;
                yield new RoundRectangle2D.Double(this.getTemporalPosition().getX(), this.getTemporalPosition().getY(),
                                                  width, height,
                                                  VisualNode.CHANCE_NODE_ARC_WIDTH,
                                                  VisualNode.CHANCE_NODE_ARC_HEIGHT);
            }
            case DECISION -> {
                String text = this.getNodeName();
                double textHeight = VisualNode.getHeight(text, g);
                double textWidth = VisualNode.getWidth(text, g);
                double rectangleWidth = this.isExpanded() ? VisualNode.NODE_EXPANDED_WIDTH :
                        textWidth < textHeight ?
                        VisualNode.DEFAULT_NODE_CONTRACTED_WIDTH
                        : textWidth + 2 * VisualNode.HORIZONTAL_SPACE_TO_TEXT;
                double rectangleHeight = textHeight + 2 * VisualNode.VERTICAL_SPACE_TO_TEXT + (this.isExpanded() ?
                        this.innerBox.getInnerBoxHeight(g) + VisualNode.NODE_EXPANDED_HEIGHT_MARGIN * 2
                        : 0);
                double rectanglePosX = this.getTemporalPosition().getX();
                double rectanglePosY = this.getTemporalPosition().getY();
                yield new Rectangle2D.Double(rectanglePosX, rectanglePosY, rectangleWidth, rectangleHeight);
            }
            case UTILITY -> {
                Point2D.Double[] points = this.getUtilityNodePoints(g);
                int length = points.length;
                GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD, length);
                polygon.moveTo(points[0].getX(), points[0].getY());
                for (int i = 1; i < length; i++) {
                    polygon.lineTo(points[i].getX(), points[i].getY());
                }
                polygon.closePath();
                yield polygon;
            }
            case SV_SUM, SV_PRODUCT -> null;
        };
    }
    
    @Override public void paint(Graphics2D g) {
        NodeType nodeType = this.node.getNodeType();
        Shape shape = this.getShape(g);
        this.drawnBounds=VisualElement.boundsWithTranslate(shape.getBounds2D(), g);
        switch (nodeType) {
            case CHANCE, EVENT -> {
                String text = this.getNodeName();
                double textHeight = VisualNode.getHeight(text, g);
                GUIColor fillColor = switch (nodeType) {
                    case CHANCE ->
                            this.preResolutionFinding ? GUIColors.Network.ChanceNode.BACKGROUND_ON_PRE_RESOLUTION_FINDING
                                    : this.postResolutionFinding && this.visualNetwork.getWorkingMode()== NetworkEditorPanel.WorkingMode.INFERENCE ?
                                      GUIColors.Network.ChanceNode.BACKGROUND_ON_POST_RESOLUTION_FINDING
                                      : GUIColors.Network.ChanceNode.BACKGROUND;
                    case EVENT -> this.node.getPurpose()
                                           .equals(PurposeType.TERMINAL_EVENT.getName()) ? GUIColors.Network.EventNode.BACKGROUND_TERMINAL
                            : this.node.getPurpose().equals(PurposeType.INITIAL_EVENT.getName()) ?
                              GUIColors.Network.EventNode.BACKGROUND_INITIAL
                              : GUIColors.Network.EventNode.BACKGROUND;
                    case DECISION, UTILITY, SV_PRODUCT, SV_SUM -> null;
                };
                g.setPaint(fillColor.getColor());
                g.fill(shape);
                g.setPaint((switch (nodeType) {
                    case CHANCE -> GUIColors.Network.ChanceNode.FOREGROUND;
                    case EVENT -> this.node.getPurpose().equals(PurposeType.TERMINAL_EVENT.getName()) ?
                            GUIColors.Network.EventNode.FOREGROUND_TERMINAL :
                            this.node.getPurpose().equals(PurposeType.INITIAL_EVENT.getName()) ?
                            GUIColors.Network.EventNode.FOREGROUND_INITIAL :
                            GUIColors.Network.EventNode.FOREGROUND;
                    case DECISION, SV_PRODUCT, SV_SUM, UTILITY -> null;
                }).getColor());
                if (this.node.isAlwaysObserved()) {
                    g.setPaint(GUIColors.Network.ALWAYS_OBSERVED.getColor());
                }
                if (this.node.isAlwaysObserved()) {
                    g.setStroke((this.isSelected()) ? VisualNode.CHANCE_NODE_STROKE_OBSERVED_WIDE : VisualNode.CHANCE_NODE_STROKE_OBSERVED_NORMAL);
                } else if (this.node.isInput()) {
                    g.setStroke((this.isSelected()) ? VisualElement.WIDE_DASHED_STROKE : VisualElement.NORMAL_DASHED_STROKE);
                } else {
                    g.setStroke((this.isSelected()) ? VisualElement.WIDE_STROKE : VisualElement.NORMAL_STROKE);
                }
                g.draw(shape);
                g.setFont(VisualNode.TEXT_FONT);
                g.setPaint((switch (nodeType) {
                    case CHANCE -> GUIColors.Network.ChanceNode.TEXT;
                    case EVENT -> GUIColors.Network.EventNode.TEXT;
                    case DECISION, SV_PRODUCT, SV_SUM, UTILITY -> null;
                }).getColor());
                if (this.isExpanded()) {
                    text = VisualElement.adjustText(text, shape.getBounds2D().getWidth(),
                                                    3, VisualNode.TEXT_FONT, g);
                }
                double textPosX = shape.getBounds2D().getCenterX() - FONT_METRICS.getStringBounds(text, g).getWidth() / 2;
                double textPosY = shape.getBounds2D().getY() + (textHeight);
                g.drawString(text, (float) textPosX, (float) textPosY);
                if (this.isExpanded()) {
                    var innerBoxGraphics = (Graphics2D) g.create();
                    innerBoxGraphics.translate(shape.getBounds2D().getX() + InnerBox.INTERNAL_MARGIN,
                                               shape.getBounds2D().getY()
                                                       + InnerBox.INTERNAL_MARGIN
                                                       + FONT_METRICS.getHeight());
                    this.innerBox.paint(innerBoxGraphics);
                }
            }
            case DECISION -> {
                String text = this.getNodeName();
                double textHeight = VisualNode.getHeight(text, g);
                if (this.preResolutionFinding) {
                    g.setPaint(GUIColors.Network.DecisionNode.BACKGROUND_ON_PRE_RESOLUTION_FINDING.getColor());
                } else if (this.postResolutionFinding && (this.visualNetwork.getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE)) {
                    g.setPaint(GUIColors.Network.DecisionNode.BACKGROUND_ON_POST_RESOLUTION_FINDING.getColor());
                } else {
                    if (!this.node.getPotentials().isEmpty()) {
                        g.setPaint(GUIColors.Network.DecisionNode.BACKGROUND_ON_POLICY.getColor());
                    } else {
                        g.setPaint(GUIColors.Network.DecisionNode.BACKGROUND.getColor());
                    }
                }
                g.fill(shape);
                g.setPaint(GUIColors.Network.DecisionNode.FOREGROUND.getColor());
                g.setStroke(this.getContourStroke());
                g.draw(shape);
                g.setFont(VisualNode.TEXT_FONT);
                g.setPaint(GUIColors.Network.DecisionNode.TEXT.getColor());
                if (this.isExpanded()) {
                    double rectangleWitdh = shape.getBounds2D().getWidth();
                    text = VisualElement.adjustText(text, rectangleWitdh, 3, VisualNode.TEXT_FONT, g);
                }
                double textPosX = shape.getBounds2D().getCenterX() - FONT_METRICS.getStringBounds(text, g).getWidth() / 2;
                double textPosY = shape.getBounds2D().getY() + (textHeight);
                g.drawString(text, (float) textPosX, (float) textPosY);
                if (this.isExpanded()) {
                    var innerBoxGraphics = (Graphics2D) g.create();
                    innerBoxGraphics.translate(shape.getBounds2D().getX() + InnerBox.INTERNAL_MARGIN,
                                               shape.getBounds2D().getY()
                                                       + InnerBox.INTERNAL_MARGIN
                                                       + FONT_METRICS.getHeight());
                    this.innerBox.paint(innerBoxGraphics);
                }
            }
            case UTILITY -> {
                String text = this.getNodeName();
                double textHeight = getHeight(text, g);
                Point2D.Double[] points = this.getUtilityNodePoints(g);
                
                boolean isChildOfEvent = this.node.getParents()
                                                  .stream()
                                                  .filter(parent -> parent.getNodeType() == NodeType.EVENT)
                                                  .count() > 0;
                
                g.setPaint((isChildOfEvent ? GUIColors.Network.UtilityNode.BACKGROUND_WITH_EVENT : GUIColors.Network.UtilityNode.BACKGROUND).getColor());
                g.fill(shape);
                g.setPaint(GUIColors.Network.UtilityNode.FOREGROUND.getColor());
                g.setStroke(this.getContourStroke());
                
                g.draw(shape);
                g.setFont(TEXT_FONT);
                g.setPaint(GUIColors.Network.UtilityNode.TEXT.getColor());
                
                if (this.isExpanded()) {
                    double interiorWitdh = points[2].getX() - points[1].getX();
                    text = adjustText(text, interiorWitdh, 3, TEXT_FONT, g);
                }
                
                double textPosX = shape.getBounds2D().getCenterX() - FONT_METRICS.getStringBounds(text, g).getWidth() / 2;
                double textPosY = shape.getBounds2D().getY() + (textHeight);
                
                g.drawString(text, (float) textPosX, (float) textPosY);
                if (this.isExpanded()) {
                    var innerBoxGraphics = (Graphics2D) g.create();
                    innerBoxGraphics.translate(shape.getBounds2D()
                                                    .getX() + UTILITY_NODE_EXPANDED_WIDTH_MARGIN + InnerBox.INTERNAL_MARGIN,
                                               shape.getBounds2D().getY()
                                                       + InnerBox.INTERNAL_MARGIN
                                                       + FONT_METRICS.getHeight());
                    this.innerBox.paint(innerBoxGraphics);
                }
            }
            case SV_SUM, SV_PRODUCT -> {
            }
        }
        ;
    }
    
    /**
     * Returns the six points of the hexagon that limits the node. The order is:
     * first the most left point,
     * second the left top point,
     * third the right top point,
     * fourth the most right point,
     * fifth the right bottom point,
     * sixth the left bottom points.
     *
     * @param g graphic object where the node can be painted.
     *
     * @return an array that contains the six (or four) points of the hexagon.
     */
    private Point2D.Double[] getUtilityNodePoints(Graphics2D g) {
        String text = this.getNodeName();
        double textHeight = VisualNode.getHeight(text, g);
        double textWidth = VisualNode.getWidth(text, g);
        double posX = this.getTemporalPosition().getX();
        double posY = this.getTemporalPosition().getY();
        
        double hexagonWidth;
        double hexagonHeight;
        
        if (this.isExpanded()) {
            hexagonHeight = this.innerBox.getInnerBoxHeight(g) + textHeight + 2 * VisualNode.VERTICAL_SPACE_TO_TEXT
                    + VisualNode.NODE_EXPANDED_HEIGHT_MARGIN * 2;
            hexagonWidth = VisualNode.NODE_EXPANDED_WIDTH + 8.0;
        } else {
            hexagonHeight = textHeight + 2 * VisualNode.VERTICAL_SPACE_TO_TEXT;
            if (textWidth < textHeight) {
                hexagonWidth = VisualNode.DEFAULT_NODE_CONTRACTED_WIDTH;
            } else {
                hexagonWidth = textWidth + 2 * VisualNode.HORIZONTAL_SPACE_TO_TEXT;
            }
        }
        
        double triangleWidth = 8.0;
        
        Point2D.Double[] points = new Point2D.Double[6];
        
        points[0] = new Point2D.Double(posX - hexagonWidth / 2, posY);
        points[3] = new Point2D.Double(posX + hexagonWidth / 2, posY);
        points[1] = new Point2D.Double(points[0].getX() + triangleWidth, posY - (hexagonHeight / 2));
        points[2] = new Point2D.Double(points[3].getX() - triangleWidth, points[1].getY());
        points[4] = new Point2D.Double(points[2].getX(), posY + (hexagonHeight / 2));
        points[5] = new Point2D.Double(points[1].getX(), points[4].getY());
        for (int i = 0; i < points.length; i++) {
            var centeredPoint = points[i];
            points[i] = new Point2D.Double(
                    centeredPoint.getX() + hexagonWidth / 2,
                    centeredPoint.getY() + hexagonHeight / 2);
        }
        return points;
        
    }
    
    /**
     * Returns the point where the segment cuts with the border of the node.
     *
     * @param segment segment that cuts the border of the node.
     * @param g       graphic object where the node can be painted.
     *
     * @return the point where the segments cuts the border or null if it
     * doesn't.
     */
    @Override public Point2D.@Nullable Double getCutPoint(Segment segment, Graphics2D g) {
        return switch (this.node.getNodeType()) {
            case CHANCE, EVENT -> {
                RoundRectangle2D.Double dimensions = (RoundRectangle2D.Double) this.getShape(g);
                double radius = dimensions.getArcWidth() / 2;
                double rectangleWidth = dimensions.getWidth() - dimensions.getArcWidth();
                double rectangleHeight = dimensions.getHeight() - dimensions.getArcHeight();
                Point2D.Double point1 = new Point2D.Double(dimensions.getX() + radius, dimensions.getY());
                Point2D.Double point2 = new Point2D.Double(point1.getX() + rectangleWidth, point1.getY());
                Point2D.Double point3 = new Point2D.Double(point2.getX() + radius, point2.getY() + radius);
                Point2D.Double point4 = new Point2D.Double(point3.getX(), point3.getY() + rectangleHeight);
                Point2D.Double point5 = new Point2D.Double(point2.getX(), point4.getY() + radius);
                Point2D.Double point6 = new Point2D.Double(point1.getX(), point5.getY());
                Point2D.Double point7 = new Point2D.Double(dimensions.getX(), point4.getY());
                Point2D.Double point8 = new Point2D.Double(dimensions.getX(), point3.getY());
                Point2D.Double circleULCenter = new Point2D.Double(point1.getX(), point8.getY());
                Point2D.Double circleURCenter = new Point2D.Double(point2.getX(), point3.getY());
                Point2D.Double circleDLCenter = new Point2D.Double(point6.getX(), point7.getY());
                Point2D.Double circleDRCenter = new Point2D.Double(point5.getX(), point4.getY());
                for (var pair : List.of(
                        // upper horizontal segment of the round rectangle
                        Tuples.record(point1, point2),
                        // right vertical segment
                        Tuples.record(point3, point4),
                        //lower horizontal segment
                        Tuples.record(point5, point6),
                        //left vertical segment
                        Tuples.record(point7, point8))) {
                    Point2D.Double point = segment.cutPoint(new Segment(pair.v0(), pair.v1()));
                    if (point != null) {
                        yield point;
                    }
                }
                
                // try to find the cut point in the upper left corner of the round
                // rectangle
                Point2D.Double[] points = segment.cutPoint(circleULCenter, radius);
                if (points != null) {
                    for (int i = 0; i < points.length; i++) {
                        if ((points[i].getX() < circleULCenter.getX()) && (points[i].getY() < circleULCenter.getY())) {
                            yield points[i];
                        }
                    }
                }
                // try to find the cut point in the upper right corner of the round
                // rectangle
                points = segment.cutPoint(circleURCenter, radius);
                if (points != null) {
                    for (int i = 0; i < points.length; i++) {
                        if ((points[i].getX() > circleURCenter.getX()) && (points[i].getY() < circleURCenter.getY())) {
                            yield points[i];
                        }
                    }
                }
                // try to find the cut point in the lower right corner of the round
                // rectangle
                points = segment.cutPoint(circleDRCenter, radius);
                if (points != null) {
                    for (int i = 0; i < points.length; i++) {
                        if ((points[i].getX() > circleDRCenter.getX()) && (points[i].getY() > circleDRCenter.getY())) {
                            yield points[i];
                        }
                    }
                }
                // try to find the cut point in the lower left corner of the round
                // rectangle
                points = segment.cutPoint(circleDLCenter, radius);
                if (points != null) {
                    for (int i = 0; i < points.length; i++) {
                        if ((points[i].getX() < circleDLCenter.getX()) && (points[i].getY() > circleDLCenter.getY())) {
                            yield points[i];
                        }
                    }
                }
                yield null;
            }
            case DECISION -> {
                Rectangle2D.Double shape = (Rectangle2D.Double) this.getShape(g);
                Point2D.Double[] points = new Point2D.Double[]{
                        new Point2D.Double(shape.getX(), shape.getY()),
                        new Point2D.Double(shape.getX() + shape.getWidth(), shape.getY()),
                        new Point2D.Double(shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight()),
                        new Point2D.Double(shape.getX(), shape.getY() + shape.getHeight())
                };
                
                int length = points.length;
                Point2D.Double result = null;
                int index1 = 0;
                int index2 = 1;
                int iteration = 0;
                
                while ((result == null) && (iteration < length)) {
                    result = segment.cutPoint(new Segment(points[index1], points[index2]));
                    index1 = (index1 + 1) % length;
                    index2 = (index2 + 1) % length;
                    iteration++;
                }
                yield result;
            }
            case UTILITY -> {
                Point2D.Double[] points = this.getUtilityNodePoints(g);
                int length = points.length;
                Point2D.Double result = null;
                int index1 = 0;
                int index2 = 1;
                int iteration = 0;
                
                while ((result == null) && (iteration < length)) {
                    result = segment.cutPoint(new Segment(points[index1], points[index2]));
                    index1 = (index1 + 1) % length;
                    index2 = (index2 + 1) % length;
                    iteration++;
                    
                }
                yield result;
            }
            case SV_SUM, SV_PRODUCT -> null;
        };
    }
    
    /**
     * Returns the visualNetwork.
     *
     * @return the visualNetwork.
     */
    public VisualNetwork getVisualNetwork() {
        return this.visualNetwork;
    }
    
    /**
     * Updates the visual node according to the changes in the node.
     *
     * @param numCases the number of evidence cases in memory
     */
    public void updateNumCases(int numCases) {
        switch (node.getNodeType()) {
            case CHANCE -> {
                innerBox = switch (node.getVariable().getVariableType()) {
                    case FINITE_STATES -> new FSVariableBox(this);
                    case DISCRETIZED -> new DiscretizedVariableBox(this);
                    case NUMERIC -> new NumericVariableBox(this);
                    case EVENT -> null;
                };
            }
            case DECISION, SV_PRODUCT, SV_SUM, EVENT, UTILITY -> {
            }
        }
        if (this.innerBox != null) {
            this.innerBox.updateNumCases(numCases);
        }
    }
    
    /**
     * Returns stroke to be used for the contour
     *
     * @return The contour stroke
     */
    private Stroke getContourStroke() {
        Stroke s;
        if (this.node.isInput()) {
            s = (this.isSelected()) ? VisualElement.WIDE_DASHED_STROKE : VisualElement.NORMAL_DASHED_STROKE;
        } else {
            s = (this.isSelected()) ? VisualElement.WIDE_STROKE : VisualElement.NORMAL_STROKE;
        }
        return s;
    }
    
    @Override public Point2D.Double getCenter() {
        return this.getTemporalPosition();
    }
    
    @Override public String toString() {
        return this.node.getName() + " - " + this.getPosition();
    }
    
    private static final BasicStroke CHANCE_NODE_STROKE_OBSERVED_WIDE = new BasicStroke(6.0f);
    private static final BasicStroke CHANCE_NODE_STROKE_OBSERVED_NORMAL = new BasicStroke(3.0f);
    /**
     * Width of a the arc of the rounded rectangle.
     */
    static final double CHANCE_NODE_ARC_WIDTH = 20;
    /**
     * Height of a the arc of the rounded rectangle.
     */
    static final double CHANCE_NODE_ARC_HEIGHT = 20;
    static final double UTILITY_NODE_EXPANDED_WIDTH_MARGIN = 4;
}
