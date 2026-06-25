/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * This class is used to translate the coordinates of the screen to the
 * coordinates of a panel, according to a zoomManager value.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - fix constants values adding final modifiers
 */
public class ZoomManager {
    
    public static final double MAX_VALUE = 5.0;
	public static final double MIN_VALUE = 0.1;
	public static final double DEFAULT_VALUE = 1.0;
	private double zoom;
    
    public ZoomManager() {
		zoom = DEFAULT_VALUE;
	}
    
    public ZoomManager(final double newZoom) {
        setZoom(newZoom <= 0 ? DEFAULT_VALUE : newZoom);
    }
 
	public double getZoom() {
		return zoom;
	}
    
    public void setZoom(double value) {
        this.zoom = Math.max(Math.min(value, MAX_VALUE), MIN_VALUE);
	}

	/**
	 * Converts a component of a coordinate of the screen to a component of a
	 * coordinate in the panel. The result must be rounded because if not, a
	 * little variation is added if the zoomManager isn't 1.0.
	 *
	 * @param value a component of a coordinate of the screen.
	 * @return a component of a coordinate of the panel.
	 */
	public double screenToPanel(double value) {
		return value / zoom;
	}

	/**
	 * Converts a component of a coordinate of the panel to a component of a
	 * coordinate in the screen. The result must be rounded because if not, a
	 * little variation is added if the zoomManager isn't 1.0.
	 *
	 * @param value a component of a coordinate of the panel.
	 * @return a component of a coordinate of the screen.
	 */
	public double panelToScreen(double value) {
		return value * zoom;
	}
	
	public MouseAdapter redelegatedMouseAdapter(MouseAdapter source) {
		return new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				source.mouseClicked(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mousePressed(MouseEvent e) {
				source.mousePressed(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mouseReleased(MouseEvent e) {
				source.mouseReleased(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mouseEntered(MouseEvent e) {
				source.mouseEntered(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mouseExited(MouseEvent e) {
				source.mouseExited(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mouseWheelMoved(MouseWheelEvent e) {
				source.mouseWheelMoved(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mouseDragged(MouseEvent e) {
				source.mouseDragged(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			@Override public void mouseMoved(MouseEvent e) {
				source.mouseMoved(mouseEventWithTranslatePos(e, ZoomManager.this.getZoom()));
			}
			
			private static <T extends MouseEvent> T mouseEventWithTranslatePos(T e, double zoom) {
				int newX = (int) (e.getX() / zoom);
				int newY = (int) (e.getY() / zoom);
				e.translatePoint(newX - e.getX(), newY - e.getY());
				return e;
			}
		};
	}
	
}
