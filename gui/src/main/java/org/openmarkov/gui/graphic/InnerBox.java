/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import org.openmarkov.gui.configuration.GUIFonts;
import javax.swing.*;
import java.awt.*;

/**
 * This abstract class specifies the methods that all inner boxes of the
 * visual nodes have to implement.
 *
 * @author asaez
 * @version 1.0
 */
public abstract sealed class InnerBox extends VisualElement permits FSVariableBox, NumericVariableBox {

	/**
	 * Font type Helvetica, plain, size 11.
	 */
	protected static final Font INNERBOX_FONT = GUIFonts.of(Font.PLAIN, 11);
    
    /**
	 * Internal margin around the Box.
	 */
	protected static final double INTERNAL_MARGIN = 4;

	/**
	 * Width of the Box.
	 */
	protected static final double BOX_WIDTH = VisualNode.NODE_EXPANDED_WIDTH - (2 * INTERNAL_MARGIN) + 1;

	/**
	 * Indentation of states.
	 */
	protected static final double STATES_INDENT = 5;

	/**
	 * Vertical separation between states.
	 */
	protected static final double STATES_VERTICAL_SEPARATION = 12;

	/**
	 * Height of the bar.
	 */
	protected static final double BAR_HEIGHT = 5;

	/**
     * Object used to measure foreground in a specific font.
	 */
	private static final FontMetrics fontMeter = new JPanel().getFontMetrics(INNERBOX_FONT);

	/**
	 * Width of the narrowest column that shows a value: what a probability takes in the font of the box.
	 */
	protected static final double VALUE_COLUMN_WIDTH = fontMeter.stringWidth(VisualState.formatValue(0));

	/**
	 * The height of this InnerBox.
	 */
	protected double height;

	/**
	 * The VisualNode this InnerBox is associated to.
	 */
	protected VisualNode visualNode;

	/**
     * Returns the height of the foreground used in the innerBox.
	 *
     * @param text foreground that appears in the innerBox.
	 * @param g    graphics object where to paint the element.
     * @return the height of the foreground used in the innerBox.
	 */
	protected static double getInnerBoxTextHeight(String text, Graphics2D g) {
		return fontMeter.getStringBounds(text, g).getHeight();
	}

	/**
     * Returns the width of the foreground used in the innerBox.
	 *
     * @param text foreground that appears in the innerBox.
	 * @param g    graphics object where to paint the element.
     * @return the width of the foreground used in the innerBox.
	 */
	protected static double getInnerBoxTextWidth(String text, Graphics2D g) {
		return fontMeter.getStringBounds(text, g).getWidth();
	}

	/**
	 * Returns the width of a text in the font of the innerBox.
	 */
	protected static double getTextWidth(String text) {
		return fontMeter.stringWidth(text);
	}

	/**
	 * Width of the column on the left of the bars, where the names of the states or the label go.
	 */
	protected abstract double getLabelColumnWidth();

	/**
	 * Width of the column on the right of the bars, where the values go. Wide enough for any value
	 * this box can show.
	 */
	protected abstract double getValueColumnWidth();

	/**
	 * Horizontal position where the bars start, measured from the left border of the box.
	 */
	protected double getBarX() {
		return STATES_INDENT + getLabelColumnWidth();
	}

	/**
	 * Horizontal position where the values start, measured from the left border of the box.
	 */
	protected double getValueX() {
		return BOX_WIDTH - STATES_INDENT - getValueColumnWidth();
	}

	/**
	 * Length of a full bar: what is left between the label column and the value column.
	 */
	protected double getBarFullLength() {
		return getValueX() - STATES_INDENT - getBarX();
	}

	/**
	 * Returns the visualNode associated with the innerBox.
	 *
	 * @return visualNode associated with the innerBox.
	 */
	public VisualNode getVisualNode() {
		return visualNode;
	}

	/**
	 * Returns the height of the innerBox. It's calculated depending on the
	 * font, the number of states and the cases in memory
	 *
	 * @return the height of the innerBox.
	 */
	public abstract double getInnerBoxHeight(Graphics2D g);

	/**
	 * Returns the number of visual states of this inner box.
	 *
	 * @return the number of visual states of this inner box.
	 */
	public abstract int getNumStates();

	/**
	 * This method recreates the visual state of the inner box.
	 *
	 * @param numCases Number of evidence cases in memory.
	 */
	public abstract void updateNumCases(int numCases);

}
