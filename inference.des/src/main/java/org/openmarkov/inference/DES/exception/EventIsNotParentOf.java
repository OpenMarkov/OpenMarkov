package org.openmarkov.inference.DES.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;

public class EventIsNotParentOf extends OpenMarkovException {
    
    public final Node eventNode;
    public final Node node;
    
    public EventIsNotParentOf(Node eventNode, Node node) {
        this.eventNode = eventNode;
        this.node = node;
    }
}
