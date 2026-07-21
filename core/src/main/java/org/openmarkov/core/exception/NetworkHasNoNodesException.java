package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import org.openmarkov.core.model.network.ProbNet;

public class NetworkHasNoNodesException extends OpenMarkovException {
    
    public NetworkHasNoNodesException(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    public final ProbNet probNet;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
