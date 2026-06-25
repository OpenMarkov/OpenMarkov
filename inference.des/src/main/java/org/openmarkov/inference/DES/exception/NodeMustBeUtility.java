package org.openmarkov.inference.DES.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;

public class NodeMustBeUtility extends OpenMarkovException {
    
    //{node} must be utility.
    public final Node node;
    
    public NodeMustBeUtility(Node node) {
        this.node = node;
    }
}
