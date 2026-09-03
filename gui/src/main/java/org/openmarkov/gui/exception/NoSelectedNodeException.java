package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.gui.graphic.VisualNetwork;

public class NoSelectedNodeException extends OpenMarkovException {
    
    public final VisualNetwork visualNetwork;
    
    public NoSelectedNodeException(VisualNetwork visualNetwork) {
        this.visualNetwork = visualNetwork;
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
}
