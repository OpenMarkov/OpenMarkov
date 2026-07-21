package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.gui.graphic.VisualNode;

public class PreResolutionNodeInInferenceException extends OpenMarkovException {
    
    public PreResolutionNodeInInferenceException(VisualNode node) {
        this.node = node;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public final VisualNode node;
}
