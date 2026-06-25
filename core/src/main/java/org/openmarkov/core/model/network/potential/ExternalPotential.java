package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.List;

/**
 * Potential which represents when value is taken from an external source such a file.
 * @author cmyago
 * @version 1.0 28/08/2023 - Adapted from IncrementPotential
 *
 */
@PotentialType(names = "External")
public class ExternalPotential extends Potential implements DESSimulablePotential {



	/**
	 * Creates a new ExternalPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of ExternalPotential
	 * @param role      <code>PotentialRole</code> of ExternalPotential
	 */
	public ExternalPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
	}


	/**
	 * Creates a new ExternalPotential with variables as its list of Variable and role as PotentialRole
	 * @param role      <code>PotentialRole</code> of ExternalPotential
	 * @param variables  Group of <code>Variable</code> which are the variables of the new ExternalPotential
	 */
	public ExternalPotential(PotentialRole role, Variable... variables) {
		this(toList(variables), role);
	}

	/**
	 * Creates a new ExternalPotential equal to potential
	 * Copy constructor for IncrementPotential
	 *
	 * @param potential ExternalPotential from which the new ExternalPotential is created
	 */
	public ExternalPotential(ExternalPotential potential) {
		super(potential);
	}

	// Methods

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role
	 *
	 * @param node      <code>Node</code>
	 * @param variables <code>ArrayList</code> of <code>Variable</code>
	 * @param role      <code>PotentialRole</code>
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		//28/08/2023 FIXME Provisional; only for DESnets
		return (node.getProbNet().getNetworkType() instanceof DESNetworkType) && (node.getNodeType() == NodeType.CHANCE);
	}

//	@Override
//	public double sampleConditionedVariable(double randomNumber, EvidenceCase parents) {
//		//14/08/2023 FIXME; currently it needs a different treatment
//		return 0;
//	}

//    @Override
//	public void resetSimulation(){
//
//		incrementedValue =0;
//	}
	// Methods
	@Override
	public @NotNull TablePotential tableProject(
			EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	
	@Override public Potential copy() {
		return new ExternalPotential(this);
	}

	@Override public boolean isUncertain() {
		return false;
	}

	@Override public String toString() {
		return super.toString() + " = Increment";
	}

	@Override public void scalePotential(double scale) {

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		ExternalPotential potential = (ExternalPotential) super.deepCopy(copyNet);
		return potential;

	}
	/**
	 * Not implemented: returns {@code null} instead of throwing
	 * {@code UnsupportedOperationException} as the base implementation does.
	 */
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		return null;
	}
	/**
	 * Not implemented: returns {@code null} instead of throwing
	 * {@code UnsupportedOperationException} as the base implementation does.
	 */
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		return null;
	}

	/**
	 * Always returns {@code Double.MAX_VALUE}, the sentinel indicating no sample is
	 * produced, since the value is meant to be supplied from an external source.
	 */
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
		return Double.MAX_VALUE;
	}
}
