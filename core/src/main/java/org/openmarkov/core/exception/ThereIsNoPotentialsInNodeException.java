package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import org.openmarkov.core.model.network.Node;

public class ThereIsNoPotentialsInNodeException extends OpenMarkovException {
    
    public ThereIsNoPotentialsInNodeException(Node node) {
        this.node = node;
    }
    
    public final Node node;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
