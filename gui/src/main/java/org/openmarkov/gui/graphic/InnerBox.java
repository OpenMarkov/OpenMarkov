/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

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
	protected static final Font INNERBOX_FONT = new Font("Helvetica", Font.PLAIN, 11);
    
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
	 * Horizontal starting position of bars in Chance and Decision Nodes.
	 */
	protected static final double BAR_HORIZONTAL_POSITION = 52;

	/**
	 * Horizontal starting position of bars in Utility Nodes.
	 */
	protected static final double BAR_HORIZONTAL_POSITION_UTILITY = 32;

	/**
	 * Maximum length of the bar.
	 */
	protected static final double BAR_FULL_LENGTH = 100;

	/**
	 * Height of the bar.
	 */
	protected static final double BAR_HEIGHT = 5;

	/**
	 * Horizontal position for the value to be shown on the right
	 * of the bar in Chance and Decision Nodes.
	 */
	protected static final double VALUE_HORIZONTAL_POSITION = BAR_HORIZONTAL_POSITION + BAR_FULL_LENGTH + STATES_INDENT;

	/**
	 * Horizontal position for the value to be shown on the right
	 * of the bar in Utility Nodes.
	 */
	protected static final double VALUE_HORIZONTAL_POSITION_UTILITY = BAR_HORIZONTAL_POSITION_UTILITY + BAR_FULL_LENGTH
			+ STATES_INDENT;

	/**
     * Object used to measure foreground in a specific font.
	 */
	private static final FontMetrics fontMeter = new JPanel().getFontMetrics(INNERBOX_FONT);

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
