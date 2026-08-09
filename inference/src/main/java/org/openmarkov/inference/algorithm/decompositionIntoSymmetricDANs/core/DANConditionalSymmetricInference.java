package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import java.util.List;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VariableElimination;

public class DANConditionalSymmetricInference extends DANInference {
    
    public DANConditionalSymmetricInference(ProbNet network, boolean isCEA) {
        super(network, isCEA);
        // TODO Auto-generated constructor stub
    }
    
    public DANConditionalSymmetricInference(ProbNet dan, List<Variable> conditioningVariables,
                                            EvidenceCase evidenceCase, boolean isCEA)
            throws NotEvaluableNetworkException.NotApplicableNetwork,
            NotEvaluableNetworkException.UnsatisfiedConstraints,
            IncompatibleEvidenceException, NonProjectablePotentialException {
        super(dan, isCEA);
        VariableElimination ver = null;
        TablePotential probability = null;
        Potential utility = null;
        boolean callInference = true;
        try {
            ver = (!isCEAnalysis ? new VEEvaluation(dan) : new VECEAnalysis(dan));
            ver.setPreResolutionEvidence(DANOperations.translateEvidenceTo(dan, evidenceCase));
            ver.setConditioningVariables(conditioningVariables);
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther |
                 IncompatibleEvidenceException.FindingVariableIsMissingAState e) {
            // The evidence of this branch cannot happen, so the branch weighs zero.
            probability = DiscretePotentialOperations.createZeroProbabilityPotential();
            utility = DiscretePotentialOperations.createZeroUtilityPotential(dan);
            callInference = false;
        } catch (ConstraintViolatedException e) {
            // The network does not meet what the algorithm requires: that is not a branch worth
            // zero, it is a network that cannot be evaluated.
            throw new NotEvaluableNetworkException.UnsatisfiedConstraints(dan, List.of(e.constraint));
        }
        if (callInference) {
            if (!isCEAnalysis) {
                VEEvaluation auxVer = (VEEvaluation) ver;
                probability = auxVer.getProbability();
                utility = auxVer.getUtility();
            } else {
                VECEAnalysis auxVer = (VECEAnalysis) ver;
                probability = auxVer.getProbability();
                utility = auxVer.getUtility();
            }
        }
        setProbability(probability);
        setUtility(utility);
    }
    
}
