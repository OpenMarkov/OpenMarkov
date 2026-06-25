package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import java.util.List;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
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
            IncompatibleEvidenceException, NonProjectablePotentialException {
        super(dan, isCEA);
        VariableElimination ver = null;
        TablePotential probability = null;
        Potential utility = null;
        boolean callInference = true;
        @ToCheck(reasonKind = ToCheck.ReasonKind.USER_EXPERIENCE,
                reasonDescription = "Is this try catch intended to work like this?")
        var toCheck = false;
        try {
            ver = (!isCEAnalysis ? new VEEvaluation(dan) : new VECEAnalysis(dan));
            ver.setPreResolutionEvidence(DANOperations.translateEvidenceTo(dan, evidenceCase));
            ver.setConditioningVariables(conditioningVariables);
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther |
                 IncompatibleEvidenceException.FindingVariableIsMissingAState | ConstraintViolatedException e) {
            probability = DiscretePotentialOperations.createZeroProbabilityPotential();
            utility = DiscretePotentialOperations.createZeroUtilityPotential(dan);
            callInference = false;
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
