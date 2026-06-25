package org.openmarkov.inference.DES.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.ProbNet;

public class OnlyDESNetsAllowedException extends OpenMarkovException {
    
    //Network {probNet.name} must be a DESNet.
    public final ProbNet probNet;
    
    public OnlyDESNetsAllowedException(ProbNet probNet) {
        this.probNet = probNet;
    }
}
