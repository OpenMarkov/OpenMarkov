package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Node;

public class ThereIsNoPotentialInNodeException extends OpenMarkovException {
    
    public ThereIsNoPotentialInNodeException(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public final String nodeName;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
