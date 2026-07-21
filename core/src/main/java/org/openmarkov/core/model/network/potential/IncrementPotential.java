package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.List;

/**
 * Potential which represents an increment in the value taken by a numeric variable.
 * TODO It does not represent a probability distribucion --???
 * @author cmyago
 * @version 1.0 10/04/2020 - Adapted from Uniform
 * 04/01/2023 - FIXME merge with CycleLengthShift?
 */
@PotentialType(names = "Increment")
public class IncrementPotential extends Potential implements DESSimulablePotential {
	/**
	 * Number of increments
	 */
	private int incrementedValue=0;

	// Constructors

	/**
	 * Creates a new IncrementPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of IncrementPotential
	 * @param role      <code>PotentialRole</code> of IncrementPotential
	 */
	public IncrementPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);

	}


	/**
	 * Creates a new IncrementPotential with variables as its list of Variable and role as PotentialRole
	 * @param role      <code>PotentialRole</code> of IncrementPotential
	 * @param variables  Group of <code>Variable</code> which are the variables of the new IncrementPotential
	 */
	public IncrementPotential(PotentialRole role, Variable... variables) {
		this(toList(variables), role);
	}

	/**
	 * Creates a new IncrementPotential equal to potential
	 * Copy constructor for UniformPotential
	 *
	 * @param potential IncrementPotential from which the new IncrementPotential is created
	 */
	public IncrementPotential(IncrementPotential potential) {
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
		//Currently 10/04/2020 it makes sense when the node variable is numeric and there is a self-loop
		//10/01/2023 FIXME Provisional; only for DESnets
		return (node.getProbNet().getNetworkType() instanceof DESNetworkType) && (variables.get(0).getVariableType() == VariableType.NUMERIC);
//				&& (variables.stream().filter(v -> v.equals(node.getVariable())).count()==2);//Check this use of equals
	}

	/**
	 * {@inheritDoc}
	 * <p>This potential does not draw a random sample: it increments and returns an
	 * internal counter, so successive calls yield consecutive integer values.</p>
	 */
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
	//14/08/2022 refactored for nuisance variable. Changed for starting in 0;
		//14/08/2022 Check; currently value is stored here and in DES record
		return ++incrementedValue;
	}

	/**
	 * {@inheritDoc}
	 * <p>Resets the internal increment counter back to zero.</p>
	 */
    @Override
	public void resetSimulation(){

		incrementedValue =0;
	}
	// Methods
	@Override
	public @NotNull TablePotential tableProject(
			EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException {
		//TODO
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	
	@Override public Potential copy() {
		return new IncrementPotential(this);
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
		IncrementPotential potential = (IncrementPotential) super.deepCopy(copyNet);
		return potential;

	}
	/** Reordering is not implemented for this potential; returns {@code null}. */
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		return null;
	}
	/** Reordering is not implemented for this potential; returns {@code null}. */
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		return null;
	}
}
