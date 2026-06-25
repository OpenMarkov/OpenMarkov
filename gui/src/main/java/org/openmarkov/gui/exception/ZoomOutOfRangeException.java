package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

public class ZoomOutOfRangeException extends OpenMarkovException {
    
    public ZoomOutOfRangeException(String requestedZoom, int minZoom, int maxZoom) {
        this.requestedZoom = requestedZoom;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }
    
    public final String requestedZoom;
    public final int minZoom;
    public final int maxZoom;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
