package org.openmarkov.inference.DES.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;

public class NodeMustBeEvent extends OpenMarkovException {
    
    //{node} must be event.
    public final Node node;
    
    public NodeMustBeEvent(Node node) {
        this.node = node;
    }
}
