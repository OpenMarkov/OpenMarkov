/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.potential.Potential;

public class CannotNormalizePotentialException extends OpenMarkovException {
    
    private final Potential potential;
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.

    
    public CannotNormalizePotentialException(Potential potential) {
        this.potential = potential;
    }
}
