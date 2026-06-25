/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.configuration.GUIColors;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class implements the graphic representation of each state that a node
 * has.
 *
 * @author asaez
 * @version 1.0
 */
public final class VisualState extends VisualElement {
    /**
     * Number of decimals
     */
    public static final int NUMBER_OF_DECIMALS = 4;
    /**
     * Font type Helvetica, plain, size 11.
     */
    protected static final Font STATES_FONT = new Font("Helvetica", Font.PLAIN, 11);
    
    /** Factor to multiply a number in the range of (0,1) to draw a bar to obtain its size in pixels. */
    private static final double lengthRelationInBars = 10000.0;
    
    /**
     * The VisualNode this State is associated to.
     */
    private VisualNode visualNode;
    /**
     * The order number assigned to this State. Determines in which position
     * will be painted this state.
     */
    private int stateIndex;
    /**
     * The name assigned to this State.
     */
    private String stateName;
    /**
     * Array of values assigned to the state. There is one value for each
     * evidence case in memory.
     */
    private final List<Double> stateValues;
    /**
     * This variable indicates which is the position of the arrayList currently
     * selected (corresponding with the current evidence case).
     */
    private int currentStateValue;
    /**
     * Array of booleans that determine whether the state has evidence or not
     */
    private final List<Boolean> evidence;
    
    /**
     * Formatting string for values shown in the visual state
     */
    private String formattingString = "0.";
    
    /**
     * Creates a new State.
     *
     * @param visualNode visualNode to which this State is associated.
     * @param number     order number to be assigned to this State inside the inner
     *                   box.
     * @param name       name of this state.
     * @param numValues  Number of values that has to have each visual state.
     */
    public VisualState(VisualNode visualNode, int number, String name, int numValues) {
        this.visualNode = visualNode;
        this.stateIndex = number;
        this.stateName = name;
        this.stateValues = new ArrayList<Double>(numValues);
        for (int i = 0; i < numValues; i++) {
            this.stateValues.add(0.0);
        }
        this.evidence = new ArrayList<>();
        this.evidence.add(false);
        this.currentStateValue = 0;
        this.formattingString = this.formattingString + "0".repeat(VisualState.NUMBER_OF_DECIMALS);
    }
    
    /**
     * Creates a new State.
     *
     * @param visualNode visualNode to which this State is associated.
     * @param number     order number to be assigned to this State inside the inner
     *                   box.
     * @param name       name of this state.
     */
    public VisualState(VisualNode visualNode, int number, String name) {
        this(visualNode, number, name, 1);
    }
    
    /**
     * Returns the visualNode to which this sate is associated.
     *
     * @return visualNode to which this sate is associated.
     */
    public VisualNode getVisualNode() {
        return this.visualNode;
    }
    
    /**
     * Sets the visualNode to which this sate is associated.
     *
     * @param visualNode the visualNode to which this sate is associated.
     */
    public void setVisualNode(VisualNode visualNode) {
        this.visualNode = visualNode;
    }
    
    /**
     * Returns the order number assigned to this state.
     *
     * @return order number assigned to this state.
     */
    public int getStateIndex() {
        return this.stateIndex;
    }
    
    /**
     * Sets the order number of this state.
     *
     * @param stateIndex the order number of this state.
     */
    public void setStateIndex(int stateIndex) {
        this.stateIndex = stateIndex;
    }
    
    /**
     * Returns the name assigned to this state.
     *
     * @return name assigned to this state.
     */
    public String getStateName() {
        return this.stateName;
    }
    
    /**
     * Sets the name of this state.
     *
     * @param stateName the name of this state.
     */
    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
    
    /**
     * Sets which is the position of the array of values that is selected.
     *
     * @param currentStateValue the position of the array of values to be set.
     */
    public void setCurrentStateValue(int currentStateValue) {
        this.currentStateValue = currentStateValue;
    }
    
    /**
     * Creates a new position in the array of values of the visual state It is
     * initially assigned 0.0 to this new position
     */
    public void createNewStateValue() {
        this.stateValues.add(0.0);
        this.evidence.add(false);
    }
    
    /**
     * Clears all the positions in the array of values of the visual state and
     * creates again the initial position assigning 0.0 to it
     */
    public void clearAllStateValues() {
        this.stateValues.clear();
        this.stateValues.add(0, 0.0);
        this.evidence.clear();
        this.evidence.add(false);
    }
    
    /**
     * Sets the value of this state for the given position of the array (this
     * position matches the evidence case number). The value is truncated so it
     * only has NUMBER_OF_DECIMALS decimals
     *
     * @param caseNumber the position in the array to be established
     * @param value      the value to be set
     */
    public void setStateValue(int caseNumber, double value) {
        // Value is currently formatted fixely with 4 decimals
        double truncatedValue = (Math.rint(value * Math.pow(10, NUMBER_OF_DECIMALS))) / Math
                .pow(10, NUMBER_OF_DECIMALS);
        this.stateValues.set(caseNumber, truncatedValue);
    }
    
    /**
     * Returns the number of positions in the array. This number is the same
     * that the number of evidence cases in memory and the same that the number
     * of bars that should be painted
     *
     * @return the number of bars to be painted for that state.
     */
    public int getNumberOfValues() {
        return this.stateValues.size();
    }
    
    /**
     * Calculates the position that this state occupies inside the inner box.
     * This position is reserve
     *
     * @return the position that this state occupies inside the inner box
     */
    private int getStatePosition() {
        InnerBox innerBox = this.visualNode.getInnerBox();
        if (innerBox instanceof FSVariableBox) {
            return (innerBox.getNumStates() - this.stateIndex);
        }
        return 1;
    }
    
    /**
     * Sets the color in which to paint depending on which is the associated
     * evidence case
     *
     * @param caseNumber number of the evidence case
     * @param g          graphics object where paint the node.
     */
    private static void setColorCaseDependent(int caseNumber, Graphics2D g) {
        g.setPaint(GUIColors.Inference.EVIDENCE_CASES_COLORS.get(caseNumber % GUIColors.Inference.EVIDENCE_CASES_COLORS.size())
                                                            .background()
                                                            .getColor());
    }
    
    /**
     * Paint the representation of the state when it is its not compiled form
     *
     * @param x x coordinate reference for painting
     * @param y y coordinate reference for painting
     * @param g graphics object where paint the node.
     */
    private static void paintNotCompiled(Double x, Double y, Graphics2D g) {
        Double aux1 = x;
        int aux2 = Double.valueOf(InnerBox.BAR_FULL_LENGTH / 20).intValue();
        while (aux1 < (x + InnerBox.BAR_FULL_LENGTH)) {
            g.drawLine(aux1.intValue() + (aux2 / 2), Double.valueOf(y + InnerBox.BAR_HEIGHT / 2).intValue(),
                       aux1.intValue() + aux2 + (aux2 / 2), Double.valueOf(y + InnerBox.BAR_HEIGHT / 2).intValue());
            aux1 += (aux2 * 2);
        }
    }
    
    @Override public Shape getShape(Graphics2D g) {
        double x = 0;
        double w = InnerBox.BOX_WIDTH - (InnerBox.STATES_INDENT * 2) + 1;
        double y = 0;
        double h;
        if (this.visualNode.getVisualNetwork().isPropagationActive()) {
            h = (InnerBox.BAR_HEIGHT * this.stateValues.size()) + 4;
            return new Rectangle2D.Double(x, y, w, h);
        }
        h = InnerBox.BAR_HEIGHT + 4;
        return new Rectangle2D.Double(x, y, w, h);
    }
    
    /**
     * Paints the three parts of the visual representation of a state: - The
     * state's name. - Horizontal bars which lengths are proportional to the
     * values assigned to the state for each of the evidence cases in memory. -
     * The value assigned to the state for the current evidence case.
     *
     * @param g graphics object where paint the node.
     */
    @Override public void paint(Graphics2D g) {
        double xBar;
        double xValue;
        double xName = InnerBox.STATES_INDENT;
        boolean isNumeric = this.visualNode.getNode().getVariable().getVariableType() == VariableType.NUMERIC;
        if (isNumeric) {
            xBar = xName + InnerBox.BAR_HORIZONTAL_POSITION_UTILITY;
            xValue = xName + InnerBox.VALUE_HORIZONTAL_POSITION_UTILITY;
        } else {
            xBar = xName + InnerBox.BAR_HORIZONTAL_POSITION;
            xValue = xName + InnerBox.VALUE_HORIZONTAL_POSITION;
        }
        double yFirstBar;
        double yText;
        if (this.visualNode.getVisualNetwork().isPropagationActive()) {
            yText =   (
                    InnerBox.STATES_VERTICAL_SEPARATION * getStatePosition()
            ) + ((this.stateValues.size() - 1) * InnerBox.BAR_HEIGHT * (getStatePosition() - 1)) + (
                    ((this.stateValues.size() - 1) * InnerBox.BAR_HEIGHT) / 2
            );
            yFirstBar =  (
                    InnerBox.STATES_VERTICAL_SEPARATION * getStatePosition()
            ) + ((this.stateValues.size() - 1) * InnerBox.BAR_HEIGHT * (getStatePosition() - 1)) - InnerBox.BAR_HEIGHT - 1;
        } else {
            yText =  (
                    InnerBox.STATES_VERTICAL_SEPARATION * getStatePosition()
            );
            yFirstBar =  (
                    InnerBox.STATES_VERTICAL_SEPARATION * getStatePosition()
            ) - InnerBox.BAR_HEIGHT - 1;
        }
        this.drawnBounds= VisualElement.boundsAddingXandY(VisualElement.boundsWithTranslate(getShape(g).getBounds2D(), g), 0, yFirstBar);
        
        g.setColor(GUIColors.Inference.BOX_TEXT.getColor());
        g.setFont(STATES_FONT);
        this.stateName = adjustText(this.stateName, InnerBox.BAR_HORIZONTAL_POSITION, 2, STATES_FONT, g);
        g.drawString(this.stateName, (int) xName, (int) yText);
        if (getVisualNode().getVisualNetwork().isPropagationActive()) {
            for (int i = 0; i < this.stateValues.size(); i++) {
                g.setPaint(GUIColors.Inference.STATE_BAR_BORDER.getColor());
                g.drawLine(Double.valueOf(xBar - 1).intValue(),
                           Double.valueOf(yFirstBar + (i * InnerBox.BAR_HEIGHT) - 1).intValue(),
                           Double.valueOf(xBar - 1).intValue(),
                           Double.valueOf(yFirstBar + (i * InnerBox.BAR_HEIGHT) + InnerBox.BAR_HEIGHT).intValue());
                g.drawLine(Double.valueOf(xBar + InnerBox.BAR_FULL_LENGTH).intValue(),
                           Double.valueOf(yFirstBar + (i * InnerBox.BAR_HEIGHT) - 1).intValue(),
                           Double.valueOf(xBar + InnerBox.BAR_FULL_LENGTH).intValue(),
                           Double.valueOf(yFirstBar + (i * InnerBox.BAR_HEIGHT) + InnerBox.BAR_HEIGHT).intValue());
                setColorCaseDependent(i, g);
                double barLength;
                if (isNumeric) {
                    InnerBox innerBox = this.visualNode.getInnerBox();
                    Double minRange = ((NumericVariableBox) innerBox).getMinValue();
                    Double maxRange = ((NumericVariableBox) innerBox).getMaxValue();
                    double range = maxRange - minRange;
                    double value = this.stateValues.get(i) - minRange;
                    barLength = ((value * lengthRelationInBars) / range) / InnerBox.BAR_FULL_LENGTH;
                } else {
                    barLength = (this.stateValues.get(i) * lengthRelationInBars) / InnerBox.BAR_FULL_LENGTH;
                }
                g.fill(new Rectangle2D.Double(xBar, yFirstBar + (i * InnerBox.BAR_HEIGHT), barLength,
                                              InnerBox.BAR_HEIGHT));
                setColorCaseDependent(this.currentStateValue, g);
                
                if (!Double.isNaN(this.stateValues.get(this.currentStateValue))) {
                    // Value is currently formatted fixely with 4 decimals
                    DecimalFormat decimalFormat = new DecimalFormat(this.formattingString,
                                                                    new DecimalFormatSymbols(Locale.US));
                    String formattedValue = String.valueOf(decimalFormat.format(this.stateValues.get(this.currentStateValue)));
                    g.drawString(formattedValue, ((int) xValue), (int) yText);
                }
            }
        } else {
            g.setPaint(GUIColors.Inference.STATE_BAR_BORDER.getColor());
            g.drawLine(Double.valueOf(xBar - 1).intValue(), Double.valueOf(yFirstBar - 1).intValue(),
                       Double.valueOf(xBar - 1).intValue(), Double.valueOf(yFirstBar + InnerBox.BAR_HEIGHT).intValue());
            g.drawLine(Double.valueOf(xBar + InnerBox.BAR_FULL_LENGTH).intValue(), Double.valueOf(yFirstBar - 1)
                                                                                         .intValue(),
                       Double.valueOf(xBar + InnerBox.BAR_FULL_LENGTH).intValue(),
                       Double.valueOf(yFirstBar + InnerBox.BAR_HEIGHT).intValue());
            if (getVisualNode().hasAnyFinding()) {
                if (this.evidence.get(this.currentStateValue)) {
                    setColorCaseDependent(this.currentStateValue, g);
                    g.fill(new Rectangle2D.Double(xBar, yFirstBar, InnerBox.BAR_FULL_LENGTH, InnerBox.BAR_HEIGHT));
                    g.setPaint(GUIColors.Inference.STATE_BAR_BORDER.getColor());
                } else {
                    paintNotCompiled(xBar, yFirstBar, g);
                }
            } else {
                paintNotCompiled(xBar, yFirstBar, g);
            }
        }
        g.setPaint(GUIColors.Inference.BOX_TEXT.getColor());
    }
    
    public void removeFinding() {
        this.evidence.set(this.currentStateValue, false);
    }
    
    public void addFinding() {
        this.evidence.set(this.currentStateValue, true);
    }
}
