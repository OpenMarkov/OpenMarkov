/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

//TODO: Exceptions of this class are caught for just shown a message from the StringDatabase like
// 'Network is not evaluable', hindering the real reason behind the exception.
// This might be turned into a RuntimeException.
public abstract sealed class NotEvaluableNetworkException extends OpenMarkovException {

    public static final class NotApplicableNetwork extends NotEvaluableNetworkException {
		public NotApplicableNetwork(ProbNet probNet, List<NetworkType> possibleNetworkTypes) {
            this.probNet = probNet;
            this.possibleNetworkTypes = possibleNetworkTypes;
        }
        
        public final ProbNet probNet;
        public final List<NetworkType> possibleNetworkTypes;
    }
	
	public static final class UnsatisfiedConstraints extends NotEvaluableNetworkException {
		public UnsatisfiedConstraints(ProbNet probNet, List<PNConstraint> unsatisfiedConstraints) {
            this.probNet = probNet;
            this.unsatisfiedConstraints = unsatisfiedConstraints;
        }
        
        public final ProbNet probNet;
        public final List<PNConstraint> unsatisfiedConstraints;
    }
	
	public static final class VariableIsNotTemporal extends NotEvaluableNetworkException {
		public VariableIsNotTemporal(Variable variable) {
            this.variable = variable;
        }
        
        public final Variable variable;
    }
}
