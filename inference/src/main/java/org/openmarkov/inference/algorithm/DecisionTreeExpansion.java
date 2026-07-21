/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.decisiontree.CENodeEvaluation;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.DecisionTreeNodeEvaluation;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.decisiontree.UnicritNodeEvaluation;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AbstractIndexedPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANConditionalSymmetricInference;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANDecisionTreeInference;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANOperations;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.FactoryDecisionTree;
import org.openmarkov.inference.algorithm.variableElimination.ChanceVariableElimination;
import org.openmarkov.inference.algorithm.variableElimination.DecisionVariableElimination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Expansion and evaluation of the decision tree of a decision model — an
 * influence diagram or a DAN.
 * <p>
 * This class implements the algorithm agreed with F. J. Díez on 2026-05-27
 * (see {@code DecisionTreeNode.pptx}). Its shape is deliberately literal:
 *
 * <pre>
 * evaluate(probNet, evidence) -&gt; decisionModelEvaluation
 *     symmetric model  -&gt; variableElimination
 *     asymmetric model -&gt; expandTree(probNet, evidence)
 *
 * expandTree(probNet, evidence) -&gt; decisionTreeNode
 *     node     = getNextNode(probNet, evidence)
 *     branches = expandForNode(probNet, evidence, node)
 *     evaluateEachBranch
 *     merge branches [average or optimal]
 *
 * expandTree(probNet, evidence, depth) -&gt; decisionTreeNode
 *     nodeList = getNextNode(probNet, evidence)
 *     switch nodeList.size()
 *       case 0   // only utility nodes remaining in the model
 *         variableElimination
 *       case 1
 *         node = nodeList.get(0)
 *         if depth = 0
 *           evaluate(probNet, evidence)
 *         else
 *           branches = expandForNode(probNet, evidence, node)
 *           evaluateEachBranch with depth = depth - 1
 *           merge branches
 *       otherwise  // several decisions can be the first one
 *         branches = prioritizeDecisions()
 *         evaluateEachBranch
 *         merge branches
 * </pre>
 *
 * Two purposes are kept apart, which is the point of the design:
 * {@link #expandTree(ProbNet, EvidenceCase, int)} builds a tree down to a
 * requested depth (a full tree may not fit in memory), whereas
 * {@link #evaluate(ProbNet, EvidenceCase)} evaluates. Symmetry is irrelevant
 * while expanding and decisive while evaluating, so the test for it lives in
 * {@code evaluate} alone.
 * <p>
 * The numerical work is delegated to the primitives already used by
 * {@link DANDecisionTreeInference}: {@link DANOperations#instantiate},
 * {@link ChanceVariableElimination} and {@link DecisionVariableElimination}.
 * Results are therefore expected to agree with that class; the difference is
 * structural — expansion is separated from evaluation, the tree is always
 * returned instead of being produced as a side effect behind a flag, and the
 * recursion lives in methods rather than in a constructor.
 */
public class DecisionTreeExpansion {

	/** Whether the model is evaluated as cost-effectiveness rather than unicriterion. */
	private final boolean isCEA;

	/**
	 * @param evaluationType {@link EvaluationType#UNICRITERION} or {@link EvaluationType#CE}.
	 */
	public DecisionTreeExpansion(EvaluationType evaluationType) {
		this.isCEA = evaluationType == EvaluationType.CE;
	}

	/**
	 * The probability and the utility of a subproblem, together with the tree
	 * node that represents it. The potentials — rather than the plain numbers
	 * carried by {@link DecisionTreeNodeEvaluation} — travel through the
	 * recursion because merging branches is a variable-elimination step, and in
	 * cost-effectiveness analysis the utility potential holds CEPs.
	 */
	private record Expansion(DecisionTreeNode node, TablePotential probability, Potential utility) {
	}

	// ---------------------------------------------------------------------
	// evaluate
	// ---------------------------------------------------------------------

	/**
	 * Evaluates a decision model. A symmetric model is evaluated by variable
	 * elimination; an asymmetric one is evaluated by expanding its tree.
	 *
	 * @param probNet  the decision model.
	 * @param evidence the evidence of the branch this model belongs to; may be {@code null}.
	 * @return the evaluation of the model.
	 */
	public DecisionTreeNodeEvaluation evaluate(ProbNet probNet, EvidenceCase evidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		return evaluateAsExpansion(probNet, evidence).node().getEvaluation();
	}

	private Expansion evaluateAsExpansion(ProbNet probNet, EvidenceCase evidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		if (DANOperations.isSymmetric(probNet, evidence != null ? evidence : new EvidenceCase())) {
			return variableElimination(probNet, evidence);
		}
		return expand(probNet, evidence, Integer.MAX_VALUE);
	}

	// ---------------------------------------------------------------------
	// expandTree
	// ---------------------------------------------------------------------

	/**
	 * Expands the whole decision tree, with no depth limit.
	 * <p>
	 * The pseudocode gives this overload a body of its own, without the
	 * {@code switch}; it is implemented here as an unbounded-depth expansion so
	 * that the empty and the several-decisions cases are handled too. This
	 * mirrors {@link DANDecisionTreeInference}, whose convenience constructors
	 * likewise default the maximum depth to {@link Integer#MAX_VALUE}.
	 *
	 * @param probNet  the decision model.
	 * @param evidence the evidence of the branch; may be {@code null}.
	 * @return the root of the decision tree.
	 */
	public DecisionTreeNode expandTree(ProbNet probNet, EvidenceCase evidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		return expandTree(probNet, evidence, Integer.MAX_VALUE);
	}

	/**
	 * Expands the decision tree down to the requested depth. Expansion stops
	 * when either the depth is exhausted or the model can no longer be expanded
	 * — the latter may happen first.
	 *
	 * @param probNet  the decision model.
	 * @param evidence the evidence of the branch; may be {@code null}.
	 * @param depth    how many further levels to expand; must not be negative.
	 * @return the root of the decision tree.
	 */
	public DecisionTreeNode expandTree(ProbNet probNet, EvidenceCase evidence, int depth)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		if (depth < 0) {
			throw new IllegalArgumentException("The depth of the expansion cannot be negative: " + depth);
		}
		return expand(probNet, evidence, depth).node();
	}

	private Expansion expand(ProbNet probNet, EvidenceCase evidence, int depth)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		List<Node> nodeList = getNextNode(probNet, evidence);
		if (nodeList.isEmpty()) {
			// Only utility nodes remain in the model, so it is symmetric by construction.
			return variableElimination(probNet, evidence);
		}
		if (depth == 0) {
			// The requested depth is exhausted: evaluate the rest of the model in one go
			// and collapse it into a leaf. Evaluating an asymmetric model expands its
			// tree, but that tree is a means to the evaluation, not part of the tree that
			// was asked for, so only its probability and utility are kept.
			//
			// The pseudocode tests the depth inside its `case 1` alone, leaving the
			// several-decisions case without a stopping condition; the test is hoisted
			// here so that it governs every case, as it does in DANDecisionTreeInference.
			// Otherwise a tree whose depth ran out on a node with several eligible
			// decisions would expand without limit.
			Expansion evaluated = evaluateAsExpansion(probNet, evidence);
			return leaf(probNet, evaluated.probability(), evaluated.utility());
		}
		if (nodeList.size() == 1) {
			return expandForNodeAndMerge(probNet, evidence, nodeList.get(0), childDepth(depth));
		}
		// Several decisions can be the first one.
		return prioritizeDecisions(probNet, evidence, nodeList, childDepth(depth));
	}

	/** Guards against {@link Integer#MAX_VALUE} — the unbounded case — decaying into a finite depth. */
	private static int childDepth(int depth) {
		return depth == Integer.MAX_VALUE ? Integer.MAX_VALUE : depth - 1;
	}

	// ---------------------------------------------------------------------
	// getNextNode
	// ---------------------------------------------------------------------

	/**
	 * Returns the nodes the tree could branch on next. The size of the result
	 * drives the algorithm:
	 * <ul>
	 * <li>empty — nothing left to branch on, only utility nodes remain;</li>
	 * <li>one — branch on it, be it an observed chance variable or the single
	 * decision that can be made first;</li>
	 * <li>several — several decisions can be made first, so the order itself has
	 * to be decided.</li>
	 * </ul>
	 *
	 * @param probNet  the decision model.
	 * @param evidence the evidence of the branch; may be {@code null}.
	 * @return the candidate nodes, never {@code null}.
	 */
	List<Node> getNextNode(ProbNet probNet, EvidenceCase evidence) {
		boolean thereAreDecisions = !probNet.getNodes(NodeType.DECISION).isEmpty();
		List<Variable> observedVariables = thereAreDecisions ?
				DANOperations.getVariablesObservedFromTheBegginning(probNet, null, evidence,
						!isInfluenceDiagram(probNet)) :
				DANOperations.getChanceVariablesNotInEvidence(probNet, evidence);
		if (!observedVariables.isEmpty()) {
			Variable variable = selectVariableWithoutAncestorsIn(observedVariables, probNet);
			return List.of(probNet.getNode(variable));
		}
		if (thereAreDecisions) {
			return DANOperations.getNextDecisions(probNet, evidence);
		}
		return List.of();
	}

	/**
	 * Selects a variable that has no ancestor among the given variables, so that
	 * the tree branches on causes before effects.
	 * <p>
	 * This repeats the intent of
	 * {@code DANOperations.selectVariableWithoutAncestorsInVariables}, which
	 * cannot be reused: it is package-private, and its inner loop advances the
	 * outer index instead of its own, so it always returns the first variable
	 * and never inspects any ancestor.
	 *
	 * @param variables the candidates; must not be empty.
	 * @param probNet   the decision model.
	 * @return a variable with no ancestor among {@code variables}.
	 */
	private static Variable selectVariableWithoutAncestorsIn(List<Variable> variables, ProbNet probNet) {
		for (Variable candidate : variables) {
			Set<Variable> ancestors = new HashSet<>();
			for (Node ancestor : ProbNetOperations.getNodeAncestors(probNet.getNode(candidate))) {
				ancestors.add(ancestor.getVariable());
			}
			boolean hasAncestorAmongCandidates = false;
			for (Variable other : variables) {
				if (other != candidate && ancestors.contains(other)) {
					hasAncestorAmongCandidates = true;
					break;
				}
			}
			if (!hasAncestorAmongCandidates) {
				return candidate;
			}
		}
		// Unreachable for an acyclic model; a cycle would make every candidate its own ancestor.
		return variables.get(0);
	}

	private static boolean isInfluenceDiagram(ProbNet probNet) {
		return probNet.getNetworkType() instanceof InfluenceDiagramType;
	}

	// ---------------------------------------------------------------------
	// expandForNode / evaluateEachBranch / mergeBranches
	// ---------------------------------------------------------------------

	/**
	 * Creates one branch per state of the variable of the given node. The
	 * branches carry no child yet; evaluating each branch fills them in.
	 *
	 * @param probNet  the decision model.
	 * @param evidence the evidence of the branch; unused, kept to match the algorithm's signature.
	 * @param node     the node to branch on.
	 * @return one branch per state.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	List<DecisionTreeBranch> expandForNode(ProbNet probNet, EvidenceCase evidence, Node node) {
		List<DecisionTreeBranch> branches = new ArrayList<>();
		for (State state : node.getVariable().getStates()) {
			branches.add(new DecisionTreeBranch(probNet, node.getVariable(), state));
		}
		return branches;
	}

	/**
	 * Branches on a single node: one child per state, each obtained by
	 * instantiating the model, and then merges the children back.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Expansion expandForNodeAndMerge(ProbNet probNet, EvidenceCase evidence, Node node, int childDepth)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		Variable variable = node.getVariable();
		boolean isChance = node.getNodeType() == NodeType.CHANCE;

		DecisionTreeNode treeNode = FactoryDecisionTree.createInstanceDecisionTreeNode(isCEA, node, probNet);
		List<DecisionTreeBranch> branches = expandForNode(probNet, evidence, node);
		List<TablePotential> childrenProbability = new ArrayList<>();
		List<Potential> childrenUtility = new ArrayList<>();

		for (DecisionTreeBranch branch : branches) {
			State state = branch.getBranchState();
			ProbNet instantiatedNet = DANOperations.instantiate(probNet, variable, state);
			// Only an observation extends the evidence; taking a decision does not.
			EvidenceCase childEvidence = isChance ? extendEvidence(evidence, variable, state) : evidence;
			Expansion child = expand(instantiatedNet, childEvidence, childDepth);
			branch.setChild(child.node());
			treeNode.addChild(branch);
			childrenProbability.add(child.probability());
			childrenUtility.add(child.utility());
		}

		return mergeBranches(probNet, treeNode, variable, isChance, childrenProbability, childrenUtility);
	}

	/**
	 * Branches on the order of the decisions that could be made first: one child
	 * per candidate decision, each obtained by prioritizing that decision over
	 * the others. The branching variable is a dummy one whose states are the
	 * names of the candidate decisions.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Expansion prioritizeDecisions(ProbNet probNet, EvidenceCase evidence, List<Node> nextDecisions,
			int childDepth) throws NotEvaluableNetworkException, IncompatibleEvidenceException,
			NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		Variable orderVariable = DANOperations.createDummyVariableOfOrder(nextDecisions);
		Node orderNode = new Node(probNet, orderVariable, NodeType.DECISION);

		DecisionTreeNode treeNode = FactoryDecisionTree.createInstanceDecisionTreeNode(isCEA, orderNode, probNet);
		List<TablePotential> childrenProbability = new ArrayList<>();
		List<Potential> childrenUtility = new ArrayList<>();

		for (Node decision : nextDecisions) {
			State state = orderVariable.getState(decision.getName());
			ProbNet prioritizedNet = DANOperations.prioritize(probNet, decision);
			// Deciding the order is a decision, so it does not extend the evidence either.
			Expansion child = expand(prioritizedNet, evidence, childDepth);
			DecisionTreeBranch branch = new DecisionTreeBranch(probNet, orderVariable, state);
			branch.setChild(child.node());
			treeNode.addChild(branch);
			childrenProbability.add(child.probability());
			childrenUtility.add(child.utility());
		}

		return mergeBranches(probNet, treeNode, orderVariable, false, childrenProbability, childrenUtility);
	}

	/**
	 * Merges the branches of a node — the {@code merge branches [average or
	 * optimal]} step. The branches of a chance node are averaged, weighted by
	 * their probability; those of a decision node are maximized over, keeping
	 * the best one.
	 */
	@SuppressWarnings("unchecked")
	private Expansion mergeBranches(ProbNet probNet, DecisionTreeNode treeNode, Variable variable, boolean isChance,
			List<TablePotential> childrenProbability, List<Potential> childrenUtility)
			throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException,
			PotentialOperationException.DifferentSizesInPotentialsAndStates {
		TablePotential probability;
		Potential utility;
		Potential conditionedUtility = (Potential) DiscretePotentialOperations.merge(variable,
				(List<? extends AbstractIndexedPotential>) (List<?>) childrenUtility);

		if (isChance) {
			TablePotential conditionedProbability = (TablePotential) DiscretePotentialOperations.merge(variable,
					(List<? extends AbstractIndexedPotential>) (List<?>) childrenProbability);
			ChanceVariableElimination elimination = new ChanceVariableElimination(variable,
					Arrays.asList(conditionedProbability), Arrays.asList(conditionedUtility));
			probability = elimination.getMarginalProbability();
			utility = sumOfUtilities(elimination.getUtilityPotentials());
		} else {
			// The children of a decision all share the same probability, so any of them will do.
			DecisionVariableElimination elimination = new DecisionVariableElimination(variable,
					Arrays.asList(childrenProbability.get(0)), Arrays.asList(conditionedUtility));
			Potential maximizedUtility = elimination.getUtility();
			utility = maximizedUtility != null ?
					maximizedUtility :
					(Potential) DiscretePotentialOperations.createZeroUtilityPotential(probNet);
			probability = elimination.getProjectedProbability();
		}
		return attachEvaluation(treeNode, probability, utility);
	}

	@SuppressWarnings("unchecked")
	private static Potential sumOfUtilities(List<Potential> utilityPotentials) {
		if (utilityPotentials.size() == 1 && utilityPotentials.get(0) instanceof GTablePotential) {
			return utilityPotentials.get(0);
		}
		return DiscretePotentialOperations.sum((List<TablePotential>) (List<?>) utilityPotentials);
	}

	// ---------------------------------------------------------------------
	// variableElimination — the leaves
	// ---------------------------------------------------------------------

	/**
	 * Evaluates a symmetric model by variable elimination and represents it as a
	 * leaf of the tree.
	 */
	private Expansion variableElimination(ProbNet probNet, EvidenceCase evidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException {
		DANConditionalSymmetricInference inference = new DANConditionalSymmetricInference(probNet, null,
				evidence != null ? evidence : new EvidenceCase(), isCEA);
		return leaf(probNet, inference.getProbability(), inference.getUtility());
	}

	/**
	 * Builds a leaf of the tree, hanging from the super-value node of the model
	 * and carrying an already computed probability and utility.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Expansion leaf(ProbNet probNet, TablePotential probability, Potential utility) {
		Node superValueNode = DANDecisionTreeInference.getSuperValueNode(probNet, isCEA);
		DecisionTreeNode treeNode = FactoryDecisionTree.createInstanceDecisionTreeNode(isCEA, superValueNode, probNet);
		return attachEvaluation(treeNode, probability, utility);
	}

	// ---------------------------------------------------------------------
	// Plumbing
	// ---------------------------------------------------------------------

	/**
	 * Reads the plain numbers out of the potentials and hangs them on the tree
	 * node as its evaluation.
	 * <p>
	 * In cost-effectiveness analysis the node's CEP is normalized (adjacent
	 * partitions of equal cost and effectiveness are merged, see
	 * {@link #normalizeCEP}). Only what the node stores and shows is normalized;
	 * the {@code utility} potential returned in the {@link Expansion} — the one
	 * that feeds the parent's merge — is left untouched, so the arithmetic of the
	 * recursion is unchanged.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Expansion attachEvaluation(DecisionTreeNode treeNode, TablePotential probability, Potential utility) {
		double probabilityValue = DANOperations.getOnlyValuePotential(probability);
		Potential nodeUtility = utility;
		DecisionTreeNodeEvaluation evaluation;
		if (isCEA) {
			CEP cep = DANOperations.getOnlyValuePotentialCEP((GTablePotential) utility);
			CEP normalized = normalizeCEP(cep);
			CENodeEvaluation ceEvaluation = new CENodeEvaluation();
			ceEvaluation.setCep(normalized);
			evaluation = ceEvaluation;
			if (normalized != cep) {
				// Replace the CEP inside a private copy of the potential, so the node's
				// stored utility matches the normalized CEP without mutating the
				// potential that flows up the recursion.
				GTablePotential copy = (GTablePotential) ((Potential) utility).copy();
				copy.elementTable.set(0, normalized);
				nodeUtility = copy;
			}
		} else {
			UnicritNodeEvaluation unicritEvaluation = new UnicritNodeEvaluation();
			unicritEvaluation.setUtility(DANOperations.getOnlyValuePotential((TablePotential) utility));
			// The strategy is left unset: variable elimination computes it, but the DAN
			// layer discards it. Wiring it through is a separate, unresolved question.
			evaluation = unicritEvaluation;
		}
		evaluation.setProb(probabilityValue);
		treeNode.setEvaluation(evaluation);
		treeNode.setScenarioProbability(probabilityValue);
		treeNode.setOnlyValueForUtility(nodeUtility);
		return new Expansion(treeNode, probability, utility);
	}

	/**
	 * Returns a CEP equal to the given one but with adjacent partitions of equal
	 * cost and effectiveness merged into a single partition. The piecewise
	 * cost-effectiveness function is unchanged; only its representation is made
	 * minimal, matching the form produced by {@link DANDecisionTreeInference}.
	 * The original is returned unchanged when there is nothing to merge.
	 * <p>
	 * Package-private rather than private so that {@code NormalizeCEPTest} can
	 * exercise it directly with hand-built CEPs.
	 */
	static CEP normalizeCEP(CEP cep) {
		double[] costs = cep.getCosts();
		double[] effectiveness = cep.getEffectivities();
		double[] thresholds = cep.getThresholds();
		StrategyTree[] interventions = cep.getStrategyTrees();
		int n = costs.length;
		if (n <= 1) {
			return cep;
		}

		List<Double> keptCost = new ArrayList<>();
		List<Double> keptEffectiveness = new ArrayList<>();
		List<StrategyTree> keptIntervention = new ArrayList<>();
		// The upper threshold of each kept partition; the last one is unbounded.
		List<Double> keptUpper = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			double upper = (i < n - 1) ? thresholds[i] : Double.POSITIVE_INFINITY;
			int last = keptCost.size() - 1;
			if (last >= 0 && costs[i] == keptCost.get(last) && effectiveness[i] == keptEffectiveness.get(last)) {
				// Merge into the previous kept partition by extending its upper boundary,
				// which drops the threshold that separated them.
				keptUpper.set(last, upper);
			} else {
				keptCost.add(costs[i]);
				keptEffectiveness.add(effectiveness[i]);
				keptIntervention.add(interventions[i]);
				keptUpper.add(upper);
			}
		}
		if (keptCost.size() == n) {
			return cep;
		}

		CEP.CEPBuilder builder = new CEP.CEPBuilder().thresholdBounds(cep.getMinThreshold(), cep.getMaxThreshold());
		int m = keptCost.size();
		for (int k = 0; k < m - 1; k++) {
			builder.addRow(keptIntervention.get(k), keptCost.get(k), keptEffectiveness.get(k), keptUpper.get(k));
		}
		return builder.build(keptIntervention.get(m - 1), keptCost.get(m - 1), keptEffectiveness.get(m - 1));
	}

	/**
	 * Returns a copy of the evidence extended with the observation of a state,
	 * leaving the original untouched.
	 */
	private static EvidenceCase extendEvidence(EvidenceCase evidence, Variable variable, State state)
			throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		EvidenceCase extended = evidence != null ? new EvidenceCase(evidence) : new EvidenceCase();
		extended.addFinding(new Finding(variable, state));
		return extended;
	}
}
