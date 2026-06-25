/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.potential.TablePotential;

//TODO: Catches of this exceptions are always wrapped with UnrecheableException, perhaps this is a
// RuntimeException.
public abstract sealed class UnexpectedInferenceException extends OpenMarkovException {
	
	public static final class ThereIsMoreThanOneConditioningVariable extends UnexpectedInferenceException {
		public ThereIsMoreThanOneConditioningVariable(TablePotential tablePotential) {
            this.tablePotential = tablePotential;
        }
        
        public final TablePotential tablePotential;
    }
    

    
}
