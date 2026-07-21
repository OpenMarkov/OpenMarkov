package org.openmarkov.inference.DES.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;

public class NodeMustBeChance extends OpenMarkovException {
    
    public final Node node;
    
    public NodeMustBeChance(Node node) {
        this.node = node;
    }
}
