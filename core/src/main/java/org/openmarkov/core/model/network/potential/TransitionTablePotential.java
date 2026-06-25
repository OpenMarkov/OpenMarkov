package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.List;

/**
 * A <code>TransitionTablePotential</code> is a type of relation with a list of
 * probabilistic nodes.
 *  Transition class to be merged with the new structure of tables
 * There have to be at least one Event variable
 * @author cmyago
 * @version 1.0 -24/03/2019- -cmyago -
 * @version 1.1 -24/08/2019 - renamed to TransitionTable and added the possibility of incompatible combinations
 * @version 1.2 -25/04/2020 - changed sampling method
*/


@PotentialType(names = "TransitionTable")
public class TransitionTablePotential extends TableWithEvents implements DESSimulablePotential {



    /**
     *
     * @param variables
     * @param role
     */
    public TransitionTablePotential(List<Variable> variables, PotentialRole role) {

        super(variables, role);

    }


    //TODO
    public TransitionTablePotential(TransitionTablePotential potential) {
        super(potential);
    }

    public TransitionTablePotential(List<Variable> variables) {
        this(variables,PotentialRole.CONDITIONAL_PROBABILITY);
    }


    /**
     * Returns true  if certain Potential type makes sense given the
     * variables and the potential role.
     * This potential makes sense when at least one of the parents
     * is an event and no event parents have Finite States or Discretized variables
     *
     * @param node      . <code>Node</code> where the potential is set
     * @param variables . <code>List</code> of <code>Variable</code>.
     * @param role      . <code>PotentialRole</code>.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
//For using it with utility nodes
//        boolean variableSuitable= variables.get(0).getVariableType()==VariableType.FINITE_STATES;
        //10/01/2023 FIXME Provisional; Potential for DESnets
        if (!(node.getProbNet().getNetworkType() instanceof DESNetworkType)) return false;
        //TransitionTablePotential is only for CHANCE nodes
        boolean variableSuitable= ( (node.getNodeType() == NodeType.CHANCE) && (variables.get(0).getVariableType() == VariableType.FINITE_STATES)
        &&  variables.subList(1,variables.size()).stream().anyMatch(variable -> variable.getVariableType() ==VariableType.EVENT));
        //return (variableSuitable && eventSuitable);
        return variableSuitable;
    }


    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
        return tablePotential.sampleConditionedVariable(randomNumbers,convert(parents));
    }

    @Override
    public Potential deepCopy(ProbNet copyNet) {
        TransitionTablePotential potential = (TransitionTablePotential) super.deepCopy(copyNet);

        potential.setTablePotential(this.tablePotential);
        potential.setUseTableWithFunctions(this.useTableWithFunctions);
        potential.setTableWithFunctions(this.tableWithFunctions);
        potential.setEvents(this.events);
        potential.setTableVariables(this.tableVariables);
        potential.setHasImpossibleConfigurations(this.hasImpossibleConfigurations);
        potential.setImpossibleConfigurations(this.impossibleConfigurations);
        potential.properties =  this.properties;

        return potential;

    }
}

