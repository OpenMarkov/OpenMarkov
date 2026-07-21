/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import org.openmarkov.core.model.network.Point2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * This is an abstract class where common elements are defined. This elements
 * are used by visual objects such as nodes or links.
 *
 * @author jmendoza 1.0
 * @author jlgozalo 1.1
 * @version 1.2 asaez - add method for adjusting the foreground that should be
 * shown in a limited space
 */
public abstract sealed class VisualElement permits InnerBox, VisualArrow, VisualNode, VisualState {

	/**
	 * Used to paint normal lines.
	 */
	protected static final BasicStroke NORMAL_STROKE = new BasicStroke(1.0f);

	/**
	 * Used to paint wide lines.
	 */
	protected static final BasicStroke WIDE_STROKE = new BasicStroke(2.0f);

	/**
	 * Used to paint normal lines.
	 */
	protected static final BasicStroke NORMAL_DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, new float[] { 3.0f, 5.0f }, 0.0f);

	/**
	 * Used to paint wide lines.
	 */
	protected static final BasicStroke WIDE_DASHED_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, new float[] { 3.0f, 5.0f }, 0.0f);

	/**
	 * This variable determines if the element is selected.
	 */
	private boolean selected = false;

	/**
	 * This variable determines if the element is visible.
	 */
	private boolean visible = true;

	/**
	 * Returns if the element is selected of the screen.
	 *
	 * @return selection state of the element.
	 */
	public boolean isSelected() {

		return selected;
	}

	/**
	 * Sets the selection state of the element.
	 *
	 * @param value new selection state of the element.
	 */
	public void setSelected(boolean value) {

		selected = value;
	}

	/**
	 * Returns if the element is visible.
	 *
	 * @return the visible.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visibility of the element.
	 *
	 * @param visible the visible to set.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Determines if the point is inside the shape.
	 *
	 * @param point point to check.
	 * @param g     graphic object where the shape can be painted.
	 * @return true if the point is inside the shape; otherwise, false.
	 */
	public boolean pointIsInsideShape(Point2D.Double point, Graphics2D g) {
		Shape shape = this.drawnBounds==null ? getShape(g):this.drawnBounds;
		boolean res = shape != null && shape.contains(new java.awt.geom.Point2D.Double(point.x, point.y));
		return res;
    }

	/**
     * Adjusts the foreground that should be shown in a limited space.
     * Used when foreground's length is greater than the assigned space
	 *
     * @param text         original foreground that should be adjusted.
     * @param maxWidth     maximum space that should be occupied by the foreground.
     * @param endingLength number of characters of the end of the foreground that should be shown.
     * @param font         the Font in which the foreground must be written.
     * @param g            graphics object where to paint the foreground.
	 */
    protected static String adjustText(String text, double maxWidth, int endingLength, Font font, Graphics2D g) {
		g.setFont(font);
		FontMetrics fontMeter = new JPanel().getFontMetrics(font);
        String endText;
		int textLenght = text.length();
		if ((fontMeter.getStringBounds(text, g).getWidth()) >= maxWidth) {
			endText = "..." + text.substring(textLenght - endingLength, textLenght);
			text = text + endText;
			while ((fontMeter.getStringBounds(text, g).getWidth()) >= maxWidth && textLenght > 0) {
				textLenght--;
				text = text.substring(0, textLenght) + endText;
			}
		}
		return text;
	}
	
	/**
	 * Returns the shape of the node.
	 *
	 * @param g graphic object where the shape can be painted.
	 * @return shape of the node.
	 */
	public abstract Shape getShape(Graphics2D g);

	/**
	 * Paints the visual element into the graphics object.
	 *
	 * @param g graphics object where to paint the element.
	 */
	public abstract void paint(Graphics2D g);
	
	/**
	 * Returns the point where the segment cuts with the border of the visual element.
	 *
	 * @param segment segment that cuts the border of the element.
	 * @return the point where the segments cuts the border or null if it
	 * doesn't.
	 */
	public Point2D.Double getCutPoint(Segment segment, Graphics2D g) {
		return new Point2D.Double();
	}

	public Point2D.Double getCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	public Point2D.Double getPosition() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected Rectangle2D drawnBounds;
    
    protected static Rectangle2D boundsWithTranslate(Rectangle2D bounds2D, Graphics2D g) {
        return VisualElement.boundsAddingXandY(bounds2D, g.getTransform().getTranslateX(), g.getTransform().getTranslateY());
    }
	
	protected static Rectangle2D boundsAddingXandY(Rectangle2D bounds2D, double plusX,  double plusY) {
		return switch (bounds2D){
			case Rectangle2D.Float floatRect ->
					new Rectangle2D.Float((float) (floatRect.x+plusX), (float) (floatRect.y+plusY), floatRect.width, floatRect.height);
			case Rectangle2D.Double doubleRect ->
					new Rectangle2D.Double(doubleRect.x+plusX, doubleRect.y+plusY, doubleRect.width, doubleRect.height);
			default -> new Rectangle2D.Double(bounds2D.getX()+plusX, bounds2D.getY()+plusY, bounds2D.getWidth(), bounds2D.getHeight());
		};
	}
}
