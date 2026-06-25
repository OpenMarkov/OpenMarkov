package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.gui.graphic.VisualNetwork;

//TODO: This should probably be a RuntimeException instead of wraping it on UnrecheableException
// when used.
public class NoSelectedNodeException extends RuntimeException implements IBundledOpenMarkovException {
    public NoSelectedNodeException(VisualNetwork visualNetwork) {
        this.visualNetwork = visualNetwork;
    }
    
    public final VisualNetwork visualNetwork;
    
    @Override public String toString() {
        return this.localize();
    }
    
}
