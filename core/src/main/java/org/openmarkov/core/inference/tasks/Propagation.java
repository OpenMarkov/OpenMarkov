/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;
import java.util.List;

/**
 * @author jorgepmartin
 * @author artasom
 */
public interface Propagation extends Task {

	HashMap<Variable, TablePotential> getPosteriorValues()
			throws IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork;

    
    void setPostResolutionEvidence(EvidenceCase postResolutionEvidence) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException;

	void setVariablesOfInterest(List<Variable> variablesOfInterest);

}