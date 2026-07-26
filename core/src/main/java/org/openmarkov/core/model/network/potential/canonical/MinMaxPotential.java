/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base for MIN and MAX families of ICI canonical potentials.
 * Provides the factorization into delta and accrued potentials as described
 * by Diez and Galan (2003). Subclasses implement the specific delta and
 * accrued potential computations for MAX (OR) or MIN (AND) semantics.
 */
public abstract class MinMaxPotential extends ICIPotential {
    
    // Constants
    /**
     * Constant defined to manipulate sub-potential variables
     */
    protected static final int CONDITIONED_VAR_POSITION = 0;
    
    /**
     * Constant defined to manipulate sub-potential variables
     */
    protected static final int CONDITIONING_VAR_POSITION = 1;
    
    // Attributes
    /**
     * The pseudoVariable is used in the factorization
     * of the noisy MAX/MIN proposed by D&iacute;ez and
     * Gal&aacute;n (2003).
     */
    protected Variable pseudoVariable;
    
    // Constructor
    /**
     * Creates a MinMax potential with the given model type and variables.
     * Initializes the pseudo-variable used in the factorization.
     *
     * @param model     the ICI model type (e.g., GENERAL_MAX, GENERAL_MIN)
     * @param variables the list of variables (conditioned variable first)
     */
    public MinMaxPotential(ICIModelType model, List<Variable> variables) {
        // In principle, role will be "conditional probability"
        super(model, variables);
        Variable conditionedVariable = getConditionedVariable();
        String pseudoVariableName = "pseudo-" + conditionedVariable.getName();
        // TODO Check no other variable exists with the same name
        pseudoVariable = new Variable(pseudoVariableName, conditionedVariable.getNumStates());
    }
    
    /**
     * Copy constructor. Recreates the pseudo-variable from the conditioned variable.
     *
     * @param potential the MinMax potential to copy
     */
    public MinMaxPotential(MinMaxPotential potential) {
        super(potential);
        Variable conditionedVariable = getConditionedVariable();
        String pseudoVariableName = "pseudo-" + conditionedVariable.getName();
        // TODO Check no other variable exists with the same name
        pseudoVariable = new Variable(pseudoVariableName, conditionedVariable.getNumStates());
    }
    
    // Methods
    
    /**
     * @return Delta<sub>Y</sub> potential. {@code TablePotential}
     */
    protected abstract TablePotential getDeltaPotential();
    
    /**
     * @param potential Potential
     *
     * @return C<sub>y</sub><sup>x<sub>i</sub></sup> potential.
     * {@code TablePotential}
     */
    protected abstract TablePotential getAccruedPotential(TablePotential potential);
    
    public List<TablePotential> getAccruedPotentials(List<TablePotential> subpotentials) {
        List<TablePotential> accruedPotentials = new ArrayList<>(subpotentials.size());
        for (TablePotential subpotential : subpotentials) {
            accruedPotentials.add(getAccruedPotential(subpotential));
        }
        return accruedPotentials;
    }
    
    /**
     * The factorization of this model: one accrued potential per parent, one for the leak, and the
     * delta. Given a model in which A-&#62;D and B-&#62;D, this returns C<sub>D'</sub><sup>A</sup>,
     * C<sub>D'</sub><sup>B</sup>, C<sub>D'</sub><sup>*</sup> and delta<sub>D,D'</sub>.
     *
     * <p>It used to accrue what {@link #buildFactorization} handed it, and that list was already
     * accrued: every parent's contribution came out summed twice, the delta came out summed as though
     * it were a distribution, and a second delta was put in front of the lot. Five factors instead of
     * four, two of them mentioning the child. Nothing called this method, so nothing noticed. The
     * method it called was named buildSubpotentialList, which is what invited the mistake: it does
     * not return subpotentials, and it is called buildFactorization now.
     *
     * @return the factors this model breaks into
     */
    public List<TablePotential> getTablePotentials() {
        return buildFactorization();
    }
    
    /**
     * @return The accrued potentials plus the Delta potential,
     * all of them projected onto the evidence, multiplied and marginalized back into a single table
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidence, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException {
        List<TablePotential> potentials = new ArrayList<>();
        List<TablePotential> subPotentials = buildFactorization();
        for (TablePotential subPotential : subPotentials) {
            potentials.add(subPotential.tableProject(evidence, null, projectedPotentials));
        }
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variables);
    }
    
    /**
     * @return The conditional probability table given by this potential
     */
    @Override public TablePotential getCPT() {
        List<Variable> variablesToEliminate = Arrays.asList(pseudoVariable);
        return DiscretePotentialOperations
                .multiplyAndMarginalize(buildFactorization(), variables, variablesToEliminate);
    }
    
    public Variable getPseudoVariable() {
        return pseudoVariable;
    }
    
    /**
     * Builds the factors this model breaks into: the accrued potential of each link, the accrued
     * potential of the leak, and the delta. Note what it returns - the FINISHED factorization, not the
     * raw per-link tables it starts from. It was called buildSubpotentialList, and that name cost a
     * defect: {@link #getTablePotentials} accrued its result a second time.
     *
     * @return {@code List} of {@code TablePotential}, one per link plus the leak plus the delta
     */
    protected List<TablePotential> buildFactorization() {
        List<TablePotential> subpotentials = new ArrayList<>();
        
        //Noisy parents
        for (int i = 1; i < variables.size(); ++i) {
            List<Variable> linkVariables = new ArrayList<>();
            linkVariables.add(variables.get(0)); // conditioned variable
            linkVariables.add(variables.get(i)); // parent i
            
            subpotentials.add(new TablePotential(linkVariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                 getNoisyParameters(variables.get(i))));
        }
        
        // Leak parent
        if (getLeakyParameters() != null) {
            List<Variable> leakVariables = new ArrayList<>();
            leakVariables.add(variables.get(0)); // conditioned variable
            subpotentials.add(new TablePotential(leakVariables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                 getLeakyParameters()));
        }
        
        List<TablePotential> accruedPotentials = getAccruedPotentials(subpotentials);
        accruedPotentials.add(getDeltaPotential());
        
        return accruedPotentials;
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        MinMaxPotential potential = (MinMaxPotential) super.deepCopy(copyNet);
        // The pseudo variable is not a node of the network - it exists only inside the
        // factorization - so it is built, never looked up. Built here from the copy's own
        // conditioned variable, as both constructors build it. Taking the original's instead, as
        // this line used to, handed the two potentials the same object and undid the fresh one the
        // copy constructor had already made.
        Variable copiedChild = potential.getConditionedVariable();
        potential.pseudoVariable = new Variable("pseudo-" + copiedChild.getName(),
                                                copiedChild.getNumStates());
        return potential;
    }
    
}
