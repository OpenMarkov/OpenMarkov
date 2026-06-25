package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.Collection;
import java.util.List;

public abstract sealed class InvalidNetworkTypeException extends OpenMarkovException {
    

    
    //TODO: It is always wrapped in UnreachableException or another exception, this might be turned into a
    // RuntimeException.
    public static final class UnmetConstraints extends InvalidNetworkTypeException {
        public final ProbNet probNet;
        public final NetworkType newNetworkType;
        public final List<PNConstraint> unsatisfiedConstraints;
        
        // Constructor
        public UnmetConstraints(ProbNet probNet, NetworkType newNetworkType, List<PNConstraint> unsatisfiedConstraints) {
            this.probNet = probNet;
            this.newNetworkType = newNetworkType;
            this.unsatisfiedConstraints = unsatisfiedConstraints;
        }
    }
    
    public static final class NotAllowedType extends InvalidNetworkTypeException {
    
        public NotAllowedType(ProbNet probNet, Collection<NetworkType> allowedTypes) {
            this.probNet = probNet;
            this.allowedTypes = allowedTypes;
        }
        
        public final ProbNet probNet;
        public final Collection<NetworkType> allowedTypes;
    }
    
}
