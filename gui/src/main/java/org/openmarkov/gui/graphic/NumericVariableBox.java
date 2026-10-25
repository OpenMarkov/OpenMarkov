/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import org.openmarkov.gui.configuration.GUIFonts;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.gui.configuration.GUIColors;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * This class is the visual representation of the inner box associated
 * to a VisualNode that represents a numeric variable
 *
 * @author asaez 1.0
 */
public non-sealed class NumericVariableBox extends InnerBox {
    
    /**
     * Font type Helvetica, plain, size 9.
     */
    protected static final Font SCALE_FONT = GUIFonts.of(Font.PLAIN, 9);
    
    /**
     * Vertical separation between the line for value and
     * the line for the scale.
     */
    private static final double SCALE_VERTICAL_SEPARATION = 12;
    
    /**
     * Horizontal Offset for the position of the range values
     * of the scale.
     */
    private static final int SCALE_RANGE_HORIZONTAL_OFFSET = 8;
    
    /**
     * Vertical Offset for the position of the range values
     * of the scale.
     */
    private static final int SCALE_RANGE_VERTICAL_OFFSET = 4;
    
    /**
     * Minimum value that the numeric variable can take.
     */
    private double minValue = Double.NEGATIVE_INFINITY;
    
    /**
     * Maximum value that the numeric variable can take.
     */
    private double maxValue = Double.POSITIVE_INFINITY;
    
    /**
     * This variable contains the visual state that is part
     * of this inner box.
     */
    private VisualState visualState;
    
    /**
     * Creates a new numeric variable innerBox.
     *
     * @param vNode visualNode to which this numeric variable innerBox is associated.
     */
    public NumericVariableBox(VisualNode vNode) {
        this(vNode, "");
    }
    
    /**
     * Creates a new numeric variable innerBox.
     *
     * @param vNode visualNode to which this numeric variable innerBox is associated.
     */
    public NumericVariableBox(VisualNode vNode, String stateName) {
        visualNode = vNode;
        visualState = new VisualState(visualNode, 0, stateName);
        PartitionedInterval domain = vNode.getNode().getVariable().getPartitionedInterval();
        setMinValue(domain.getMin());
        setMaxValue(domain.getMax());
    }
    
    /**
     * Returns the minimum value that the numeric variable can take.
     *
     * @return minimum value that the numeric variable can take.
     */
    public double getMinValue() {
        return minValue;
    }
    
    /**
     * Sets the minimum value that the numeric variable can take.
     *
     * @param minValue minimum value that the numeric variable can take.
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }
    
    /**
     * Returns the maximum value that the numeric variable can take.
     *
     * @return maximum value that the numeric variable can take.
     */
    public double getMaxValue() {
        return maxValue;
    }
    
    /**
     * Sets the maximum value that the numeric variable can take.
     *
     * @param maxValue maximum value that the numeric variable can take.
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
    
    /**
     * The length of the bar of {@code value} in the range of the box, from 0 to {@code fullLength}.
     */
    double barLength(double value, double fullLength) {
        double length = (value - minValue) / (maxValue - minValue) * fullLength;
        return Double.isNaN(length) ? 0 : Math.clamp(length, 0, fullLength);
    }
    
    /**
     * An end of the range as the scale writes it, with two decimals at most.
     */
    private static String scaleLabel(double end) {
        return "" + Math.rint(end * 100) / 100;
    }
    
    /**
     * This method recreates the visual state of the inner box.
     *
     * @param numCases Number of evidence cases in memory.
     */
    @Override public void updateNumCases(int numCases) {
        PartitionedInterval domain = visualNode.getNode().getVariable().getPartitionedInterval();
        setMinValue(domain.getMin());
        setMaxValue(domain.getMax());
        String stateName = (visualState != null) ? visualState.getStateName() : "";
        visualState = new VisualState(visualNode, 0, stateName, numCases);
    }
    
    /**
     * Returns the visual state contained by this inner box.
     *
     * @return visual state contained by this inner box.
     */
    public VisualState getVisualState() {
        return visualState;
    }
    
    /**
     * Sets the visual state contained by this inner box.
     *
     * @param visualState visual state contained by this inner box.
     */
    public void setVisualState(VisualState visualState) {
        this.visualState = visualState;
    }
    
    /**
     * Returns the number of visual states of this inner box.
     *
     * @return the number of visual states of this inner box.
     */
    @Override public int getNumStates() {
        return 1;
    }
    
    @Override protected double getLabelColumnWidth() {
        return getTextWidth(visualState.getStateName()) + STATES_INDENT;
    }
    
    /**
     * Wide enough for the value at either end of the range, so for every value in between, and for the values the box
     * writes, which the range does not bound when it could not be computed and has infinite ends.
     */
    @Override protected double getValueColumnWidth() {
        double width = Math.max(VALUE_COLUMN_WIDTH, Math.max(getTextWidth(VisualState.formatValue(minValue)),
                                                             getTextWidth(VisualState.formatValue(maxValue))));
        // Those of every evidence case, so that the column does not change when another case is shown
        for (int caseNumber = 0; caseNumber < visualState.getNumberOfValues(); caseNumber++) {
            double value = visualState.getStateValue(caseNumber);
            if (!Double.isNaN(value)) {
                width = Math.max(width, getTextWidth(VisualState.formatValue(value)));
            }
        }
        return width;
    }
    
    @Override public Shape getShape(Graphics2D g) {
        double innerNodeHeight = getInnerBoxHeight(g);
        return new Rectangle2D.Double(0, 0, BOX_WIDTH, innerNodeHeight);
    }
    
    /**
     * Paints the inner part of the visual node into the graphics object.
     *
     * @param g graphics object where paint the node.
     */
    @Override public void paint(Graphics2D g) {
        Shape shape = getShape(g);
        this.drawnBounds=VisualElement.boundsWithTranslate(shape.getBounds2D(), g);
        g.setPaint(GUIColors.Inference.BOX_BACKGROUND.getColor());
        g.fill(shape);
        g.setStroke(NORMAL_STROKE);
        g.setPaint(GUIColors.Inference.BOX_FOREGROUND.getColor());
        g.draw(shape);
        g.setFont(INNERBOX_FONT);
        
        visualState.paint(g);
        
        //draw the scale in the bottom part
        double scaleXPostion = getBarX() - 1;
        double barFullLength = getBarFullLength();
        double scaleYPostion = INTERNAL_MARGIN
                + STATES_VERTICAL_SEPARATION + SCALE_VERTICAL_SEPARATION + (
                BAR_HEIGHT * (
                        visualState.getNumberOfValues() - 1
                )
        );
        
        g.draw(new Line2D.Double(scaleXPostion, scaleYPostion, scaleXPostion + barFullLength, scaleYPostion));
        g.draw(new Line2D.Double(scaleXPostion, scaleYPostion - (BAR_HEIGHT / 2), scaleXPostion,
                                 scaleYPostion + (BAR_HEIGHT / 2)));
        g.draw(new Line2D.Double(scaleXPostion + (barFullLength / 4), scaleYPostion - (BAR_HEIGHT / 2),
                                 scaleXPostion + (barFullLength / 4), scaleYPostion + (BAR_HEIGHT / 2)));
        g.draw(new Line2D.Double(scaleXPostion + (barFullLength / 2), scaleYPostion - (BAR_HEIGHT / 2),
                                 scaleXPostion + (barFullLength / 2), scaleYPostion + (BAR_HEIGHT / 2)));
        g.draw(new Line2D.Double(scaleXPostion + (barFullLength * 3 / 4), scaleYPostion - (BAR_HEIGHT / 2),
                                 scaleXPostion + (barFullLength * 3 / 4), scaleYPostion + (BAR_HEIGHT / 2)));
        g.draw(new Line2D.Double(scaleXPostion + barFullLength, scaleYPostion - (BAR_HEIGHT / 2),
                                 scaleXPostion + barFullLength, scaleYPostion + (BAR_HEIGHT / 2)));
        
        g.setFont(SCALE_FONT);
        g.drawString(scaleLabel(minValue), (int) scaleXPostion - SCALE_RANGE_HORIZONTAL_OFFSET,
                     (int) scaleYPostion + g.getFont().getSize() + SCALE_RANGE_VERTICAL_OFFSET);
        g.drawString(scaleLabel(maxValue), (int) ((int) scaleXPostion + barFullLength) - SCALE_RANGE_HORIZONTAL_OFFSET,
                     (int) scaleYPostion + g.getFont().getSize() + SCALE_RANGE_VERTICAL_OFFSET);
        g.setFont(INNERBOX_FONT);
    }
    
    /**
     * Returns the height of the innerBox.
     *
     * @param g graphics object.
     *
     * @return the height of the innerBox.
     */
    @Override public double getInnerBoxHeight(Graphics2D g) {
        double innerBoxHeight;
        if (visualNode.getVisualNetwork().isPropagationActive()) {
            innerBoxHeight = INTERNAL_MARGIN * 2 + STATES_VERTICAL_SEPARATION + SCALE_VERTICAL_SEPARATION
                    + (visualState.getNumberOfValues() - 1) * BAR_HEIGHT + BAR_HEIGHT / 2 + SCALE_FONT.getSize();
        } else {
            innerBoxHeight = INTERNAL_MARGIN * 2 + STATES_VERTICAL_SEPARATION + SCALE_VERTICAL_SEPARATION
                    + BAR_HEIGHT / 2 + SCALE_FONT.getSize();
        }
        return innerBoxHeight;
    }
    
}