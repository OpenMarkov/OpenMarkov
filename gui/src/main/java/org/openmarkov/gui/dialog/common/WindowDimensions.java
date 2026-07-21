package org.openmarkov.gui.dialog.common;

import java.awt.*;
import java.io.Serializable;

/**
 * Immutable snapshot of a window's location, size, and extended state (e.g. maximized),
 * used for persisting and restoring window geometry.
 */
public record WindowDimensions(Point location, Dimension size, int extendedState) implements Serializable {
    
    public static WindowDimensions of(Window window){
        var isMaximized = window instanceof Frame frame && frame.getExtendedState() == Frame.MAXIMIZED_BOTH;
        Point location = isMaximized ? new Point(0,0) : window.getLocation();
        Dimension size = window.getSize();
        return new WindowDimensions(location, size, window instanceof Frame frame? frame.getExtendedState() : Frame.NORMAL);
    }
    
    public static WindowDimensions of(Window window, WindowDimensions previousDimensions){
        if(previousDimensions == null){
            return WindowDimensions.of(window);
        }
        var isMaximized = window instanceof Frame frame && frame.getExtendedState() == Frame.MAXIMIZED_BOTH;
        Point location = isMaximized ? previousDimensions.location() : window.getLocation();
        Dimension size = isMaximized ? previousDimensions.size() : window.getSize();
        return new WindowDimensions(location, size, window instanceof Frame frame? frame.getExtendedState() : Frame.NORMAL);
    }
    
    public void set(Window window){
        window.setLocation(this.location());
        window.setSize(this.size());
        if(window instanceof Frame frame){
            frame.setExtendedState(this.extendedState());
        }
    }
    
}
