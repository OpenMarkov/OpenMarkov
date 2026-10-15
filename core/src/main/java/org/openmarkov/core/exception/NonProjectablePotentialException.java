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

import java.math.BigDecimal;
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
    /** A numeric variable with a uniform distribution has no value to compute with until it gets a finding. */
    public static final class NumericVariableNeedsAFinding extends NonProjectablePotentialException {
        public NumericVariableNeedsAFinding(Variable variable) {
            this.variable = variable;
        }

        public final Variable variable;
    }

    /**
     * A finding on a numeric node whose relation is a distribution that cannot give its density at the observed
     * value: the finding could not tell anything about the parents of the node.
     */
    public static final class DensityCannotBeCalculated extends NonProjectablePotentialException {
        public DensityCannotBeCalculated(Variable variable, String distribution) {
            this.variable = variable;
            this.distribution = distribution;
        }

        public final Variable variable;
        public final String distribution;
    }

    /**
     * A finding on a numeric node at a value where the density of its distribution is infinite, in some
     * configuration of its parents, such as a gamma of shape below 1 at 0: how much each configuration weighs
     * cannot be told.
     */
    public static final class DensityIsInfinite extends NonProjectablePotentialException {
        public DensityIsInfinite(Variable variable, String distribution, double value) {
            this.variable = variable;
            this.distribution = distribution;
            this.value = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        }

        public final Variable variable;
        public final String distribution;
        /** The observed value, as the message writes it. */
        public final String value;
    }

    /**
     * A finding on a numeric node whose distribution, in some configuration of its parents, has parameters that do
     * not make a distribution, such as a normal of deviation 0.
     */
    public static final class ParametersAreNotADistribution extends NonProjectablePotentialException {
        /**
         * @param where      the configuration of the parents, such as "with Disease = absent, ", or "" if there are
         *                   no parents
         * @param parameters the parameters, such as "mu = 5 and sigma = 0"
         */
        public ParametersAreNotADistribution(Variable variable, String distribution, String where, String parameters) {
            this.variable = variable;
            this.distribution = distribution;
            this.where = where;
            this.parameters = parameters;
        }

        public final Variable variable;
        public final String distribution;
        public final String where;
        public final String parameters;
    }

    /** A utility node has no numbers: its relation is the uniform one it was created with. */
    public static final class UtilityIsNotDefined extends NonProjectablePotentialException {
        public UtilityIsNotDefined(Variable utilityVariable) {
            this.utilityVariable = utilityVariable;
        }

        public final Variable utilityVariable;
    }

    public static final class MissingMandatoryCovariate extends NonProjectablePotentialException {
        public MissingMandatoryCovariate(Potential potential, String covariate) {
            this.potential = potential;
            this.covariate = covariate;
        }

        public final Potential potential;
        public final String covariate;
    }

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
