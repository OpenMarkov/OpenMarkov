/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import net.sourceforge.jeval.EvaluationException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import java.util.List;

/**
 * Thrown when the {@code Potential} cannot be projected into a set of
 * {@code TablePotential}s given the evidence supplied.
 */
//TODO: Almost every exception of this class is wrapped into an UnrecheableException,
// or shown with JOptionPanel, leading to further bugs. This might be a RuntimeException.
public abstract sealed class NonProjectablePotentialException extends OpenMarkovException {

    public static final class SuperValueMustBeSumOrProduct extends NonProjectablePotentialException {
        public SuperValueMustBeSumOrProduct(Potential potential) {
            this.potential = potential;
        }
        
        public final Potential potential;
    }
    
    public static final class PotentialCannotBeConvertedToATable extends NonProjectablePotentialException {
        public PotentialCannotBeConvertedToATable(Potential potential) {
            this.potential = potential;
        }
        
        public final Potential potential;
    }
    
    /**
     * A generalized linear model needs exactly one coefficient per covariate. Nothing stops the
     * two from being set separately and out of step — the file readers set the coefficients first
     * and the covariates only if the file carries them — so the mismatch is caught here, when the
     * potential is asked to project itself, and reported with both counts.
     */
    public static final class CoefficientsDoNotMatchCovariates extends NonProjectablePotentialException {
        public CoefficientsDoNotMatchCovariates(Potential potential, int coefficientCount, int covariateCount) {
            this.potential = potential;
            this.coefficientCount = coefficientCount;
            this.covariateCount = covariateCount;
        }

        public final Potential potential;
        public final int coefficientCount;
        public final int covariateCount;
    }

    public static final class PotentialCannotBeConvertedToATableDueToVariable extends NonProjectablePotentialException {
        public PotentialCannotBeConvertedToATableDueToVariable(Potential potential, Variable variable) {
            this.potential = potential;
            this.variable = variable;
        }
        
        public final Potential potential;
        public final Variable variable;
    }
    
    public static final class MissingVariableInEvidence extends NonProjectablePotentialException {
        public MissingVariableInEvidence(Variable variable, EvidenceCase evidenceCase) {
            this.variable = variable;
            this.evidenceCase = evidenceCase;
        }
        
        public final Variable variable;
        public final EvidenceCase evidenceCase;
    }
    
    public static final class MissingEvidenceInVariable extends NonProjectablePotentialException {
        public MissingEvidenceInVariable(Potential potential, Variable timeVariable) {
            this.potential = potential;
            this.timeVariable = timeVariable;
        }
        
        public final Potential potential;
        public final Variable timeVariable;
    }
    
    public static final class TopVariableNotInDomain extends NonProjectablePotentialException {
        public TopVariableNotInDomain(TreeADDPotential treeADDPotential, List<TreeADDBranch> branches) {
            this.treeADDPotential = treeADDPotential;
            this.branches = branches;
        }
        
        public final TreeADDPotential treeADDPotential;
        public final List<TreeADDBranch> branches;
    }
    
    //TODO: Only used once and wrapped into an UnrecheableException, this might be a RuntimeException.
    public static final class CannotEvaluate extends NonProjectablePotentialException {
        public CannotEvaluate(String elementToEvaluate, EvaluationException evaluationException) {
            this.elementToEvaluate = elementToEvaluate;
            this.evaluationException = evaluationException;
        }
        
        public CannotEvaluate(VariableExpression elementToEvaluate, EvaluationException evaluationException) {
            this.elementToEvaluate = elementToEvaluate.toString();
            this.evaluationException = evaluationException;
        }
        
        public final String elementToEvaluate;
        public final EvaluationException evaluationException;
    }
    
    public static final class CannotResolveVariable extends NonProjectablePotentialException {
        public CannotResolveVariable(String reference) {
            this.reference = reference;
        }
        
        public final String reference;
    }
}
