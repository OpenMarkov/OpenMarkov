package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Potential that computes TTE from a piecewise exponential distribution.
 * Life tables are also sampled as piecewise exponential distributions.
 *
 * It is define complete piecewise distribution but considering that when computing its time-to-event it starts at a certain value.
 * For example, the table contains a full Life Table but each patient enter the model at a certain age
 *
 * Probabilities may be in probability of a time unit format or in rate format.
 *
 * The starting value is defined by initTimeFunction.
 *
 * The table has the following format:
 *
 * The table is organised in the following format:
 * time_0, probabilityPerTimeUnit in the interval [0, time_1)
 * time_1, probabilityPerTimeUnit  in the interval [time_1, time_2)
 * ...
 * time_i, probabilityPerTimeUnit  in the interval [time_i, time_i+1)
 * ...
 * time_n, probabilityPerTimeUnit  in the interval [time_n, infinite)
 *
 * The cycle units are the same of the rest of the model and are those in which the discount is expressed.
 * 0 > time_1 >...> time_i>...time_n
 *
 * When time_0 = 0, time unit is year, and every time_i - time_i+1 =1 we have the traditional life table
 *
 * FIXME The table can be introduced in the GUI graph or read from a file ??
 * @author cmyago
 * @version 1.0 23/10/2023
 *
 */
@PotentialType(names = "Piecewise Exponential")
public class PiecewiseExponentialPotential extends Potential implements DESSimulablePotential {
	private final List<Variable> numericVariables;
//	/**
//	 * True if the initial value is the first interval
//	 */
//	private boolean useFirstInterval= true;
	/**
	 * True if the distribution is defined by rates instead of unit of time probabilities (such as annual probability, monthly probability)
	 */
	private boolean useRates;
	/**
	 * Piecewise table; intervals; probabilities
	 * <time_i, prob_i>
	 */
	private TreeMap<Double, Double> piecewiseTable= new TreeMap<>();

	/**
	 * a_i = (1-p_i)^(t_i+1-t_i)
	 * Product(a_i) = a_0*a_1*...*a_i
	 * <t_i, a_i-1*...*a_0>
	 */
	private TreeMap<Double, Double> aProduct;

	/**
	 * < a_i*a_i-1*...*a_0, t_i>
	 */
	private TreeMap<Double,Double> inverseAProduct;

	/**
	 * log(1-p_0), ..., log(1-p_i), ..., log(1- p_n)
	 */
	private double[] logProbs;


	/**
	 * a_i = (1-p_i)^(t_i+1-t_i)
	 * Product(a_i) = a_0*a_1*...*a_i
	 * Log(Product(a_i)) = log(a_0*a_1*...*a_i)
	 */
	private double[] aLogProduct;


//	/**
//	 * Represent the product needed for computing the survival function in the interval [time_i, time_i+1)
//	 * TreeMap with pairs <time_i,
//	 * (1-prob_i-1)^(time_i - time_i-1)*...*(1-prob_0)^(time_1 - time_0),
//	 *
//	 */
//	 private final TreeMap<Double, Double> survivalProduct = new TreeMap<>();

	/**
	 * TreeMap with pairs <key=(1-prob_i)^(time_i+1 -time_i)*(1-prob_i-1)^(time_i - time_i-1)*...*(1-prob_0)^(time_1 - time_0),log(key)>
	 * from time_0 to time_n-1
	 */
	 private TreeMap<Double,Double> survivalProductLog;

	/**
	 * Function
	 */
	private FunctionPotential initTimeFunction;




	/**
	 * Creates a new PiecewiseExponentialPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of PiecewiseExponentialPotential
	 * @param role      <code>PotentialRole</code> of PiecewiseExponentialPotential
	 */
//	public PiecewiseExponentialPotential(List<Variable> variables, PotentialRole role, TreeMap piecewiseTable) {
//		this(variables,role);
//		setPiecewiseTable(piecewiseTable);
//
//	}


	/**
	 * Creates a new PiecewiseExponentialPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of PiecewiseExponentialPotential
	 * @param role      <code>PotentialRole</code> of PiecewiseExponentialPotential
	 */
	public PiecewiseExponentialPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
		this.numericVariables =  variables.subList(1,variables.size()).stream().
				filter(variable -> (variable.getVariableType() == VariableType.NUMERIC)
						|| (variable.getVariableType() == VariableType.EVENT)).collect(Collectors.toList());
		useRates = false;
		piecewiseTable.put(0.0,1.0);
		setPiecewiseTable(piecewiseTable);
		this.initTimeFunction = new FunctionPotential(variables, role, new VariableExpression(variables, piecewiseTable.firstKey().toString()));
	}

	/**
	 * Creates a new PiecewiseExponentialPotential with variables as its list of Variable and role as PotentialRole
	 *
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of PiecewiseExponentialPotential
	 * @param role      <code>PotentialRole</code> of PiecewiseExponentialPotential
	 */
	public PiecewiseExponentialPotential(List<Variable> variables, PotentialRole role, TreeMap piecewiseTable, FunctionPotential initTimeFunction, boolean useRates) {
		this(variables,role);
		setPiecewiseTable(piecewiseTable);
		this.initTimeFunction = initTimeFunction;
		this.useRates = useRates;
	}



	/**
	 * Creates a new PiecewiseExponentialPotential with variables as its list of Variable and role as PotentialRole
	 * @param role      <code>PotentialRole</code> of PiecewiseExponentialPotential
	 * @param variables  Group of <code>Variable</code> which are the variables of the new PiecewiseExponentialPotential
	 */
	public PiecewiseExponentialPotential(PotentialRole role, Variable... variables) {
		this(toList(variables), role);
	}

	/**
	 * Creates a new PiecewiseExponentialPotential equal to potential
	 * Copy constructor for IncrementPotential
	 *
	 * @param potential PiecewiseExponentialPotential from which the new PiecewiseExponentialPotential is created
	 */
	public PiecewiseExponentialPotential(PiecewiseExponentialPotential potential) {
		this(potential.getVariables(),potential.getPotentialRole(),
				potential.getPiecewiseTable(),
				potential.initTimeFunction,
				potential.useRates);
	}

	// Methods
	public void setPiecewiseTable(TreeMap<Double, Double> piecewiseTable) {
		this.piecewiseTable = piecewiseTable;
		aProduct = new TreeMap<>();
		inverseAProduct = new TreeMap<>();
		List<Double> timeI = new ArrayList<>(piecewiseTable.keySet());
		List<Double> probI = new ArrayList<>(piecewiseTable.values());
		double a =1;
		for (int i = 0; i < timeI.size(); i++) {
			aProduct.put(timeI.get(i),a);
			if (i<timeI.size() -1) {
				a *= Math.pow(1 - probI.get(i), timeI.get(i + 1) - timeI.get(i));
			} else {
				a = 0;
			}
			inverseAProduct.put(a, timeI.get(i));
		}

	}
	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role
	 *
	 * @param node      <code>Node</code>
	 * @param variables <code>ArrayList</code> of <code>Variable</code>
	 * @param role      <code>PotentialRole</code>
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		return (node.getProbNet().getNetworkType() instanceof DESNetworkType) &&
				((node.getNodeType() == NodeType.EVENT)
				|| (variables.get(0).getVariableType()==VariableType.NUMERIC));
	}

	/**
	 * Returns the number of random numbers needed to draw a sample, which equals the number
	 * of intervals (entries) in the piecewise table.
	 *
	 * @return the size of the piecewise table
	 */
	@Override
	public int numRandomNumbersNeeded(){
		return piecewiseTable.size();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Draws a time-to-event from the piecewise exponential distribution by inverse-CDF sampling,
	 * conditioned on the entry time computed by {@link #getInitTimeFunction()}. The returned value
	 * is the sampled time relative to that entry time (i.e. {@code sample - initValue}).
	 *
	 * @throws InvalidArgumentException if the entry time is lower than
	 *         the first time in the piecewise table
	 */
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
		//initValue should be >= than time_0
		double initValue = initTimeFunction.sampleConditionedVariable(new double[]{0},parents);
		if (initValue < piecewiseTable.firstKey()){
			throw new InvalidArgumentException("Piecewise Exponential: initValue lower than first value in timetable");
		}

		//Get survival value of initValue; floorEntry has complexity O(log(n))
		double initValueFloor = piecewiseTable.floorEntry(initValue).getKey().doubleValue(); //time_i
		double initValueProb = piecewiseTable.floorEntry(initValue).getValue().doubleValue(); //p_i
 		double initValueProduct = aProduct.floorEntry(initValue).getValue().doubleValue() ; //a_i
		double initValueSurv = Math.pow(1- initValueProb, initValue - initValueFloor)*initValueProduct;
		//Get logarithm of conditioned random number:
		double u =randomNumbers[0]*initValueSurv;

		//Get the quantile interval
		double tI = inverseAProduct.get(inverseAProduct.floorKey(u));
		double prodAi = aProduct.get(tI);
		double pI = piecewiseTable.get(tI);
		double sample = tI + (Math.log(u) - Math.log(prodAi))/Math.log(1 - pI);
		return sample - initValue;


	}


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
		//TODO
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	
	@Override public Potential copy() {
		return new PiecewiseExponentialPotential(this);
	}

	@Override public boolean isUncertain() {
		return false;
	}

	@Override public String toString() {
		return super.toString() + " = Piecewise exponential";
	}

	@Override public void scalePotential(double scale) {

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		PiecewiseExponentialPotential potential = (PiecewiseExponentialPotential) super.deepCopy(copyNet);
		return potential;

	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	/** Reordering is not supported for this potential; always returns {@code null}. */
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		return null;
	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	/** Reordering is not supported for this potential; always returns {@code null}. */
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		return null;
	}


	public List<Variable> getNumericVariables() {
		return numericVariables;	}

	/**
	 * Sorts the entries of the piecewise table by increasing time. Currently a no-op, since the
	 * underlying {@code TreeMap} already keeps the intervals ordered by their lower bound.
	 */
	public void sort(){

	}

	/**
	 * TreeMap with pairs {@code <lower_bound_interval, probability_per_time_unit>}
	 */
	public TreeMap<Double, Double> getPiecewiseTable() {
		return piecewiseTable;
	}


	/**
	 * Function
	 */
	public FunctionPotential getInitTimeFunction() {
		return initTimeFunction;
	}

	public void setInitTimeFunction(FunctionPotential initTimeFunction) {
		this.initTimeFunction = initTimeFunction;
	}

	/**
	 * True if the distribution is defined by rates instead of unit of time probabilities (such as annual probability, monthly probability)
	 */
	public boolean isUseRates() {
		return useRates;
	}

	public void setUseRates(boolean useRates) {
		this.useRates = useRates;
	}


}
