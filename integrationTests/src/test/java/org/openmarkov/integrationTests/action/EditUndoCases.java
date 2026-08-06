/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkAndUpdatePotentialsEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.action.base.linkEdits.MultiAddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.OrientLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.action.core.AbsorbNodeEdit;
import org.openmarkov.core.action.core.AbsorbParentsEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.action.core.AddPotentialEdit;
import org.openmarkov.core.action.core.CRemoveNodeEdit;
import org.openmarkov.core.action.core.ChangeNetworkTypeEdit;
import org.openmarkov.core.action.core.CycleLengthEdit;
import org.openmarkov.core.action.core.DecisionCriteriaEdit;
import org.openmarkov.core.action.core.DecisionCriterionUnitEdit;
import org.openmarkov.core.action.core.MonteCarloOptionsEdit;
import org.openmarkov.core.action.core.MulticriteriaEdit;
import org.openmarkov.core.action.core.NetworkCommentEdit;
import org.openmarkov.core.action.core.NetworkDefaultStatesEdit;
import org.openmarkov.core.action.core.NodeAlwaysObservedEdit;
import org.openmarkov.core.action.core.NodeBaseNameEdit;
import org.openmarkov.core.action.core.NodeCommentEdit;
import org.openmarkov.core.action.core.NodeReplaceStatesEdit;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.action.core.PrecisionEdit;
import org.openmarkov.core.action.core.PurposeEdit;
import org.openmarkov.core.action.core.RelevanceEdit;
import org.openmarkov.core.action.core.RemoveConstraintEdit;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.action.core.RemovePolicyEdit;
import org.openmarkov.core.action.core.RevelationStateEdit;
import org.openmarkov.core.action.core.SetPotentialEdit;
import org.openmarkov.core.action.core.SetPotentialVariablesEdit;
import org.openmarkov.core.action.core.TemporalOptionsEdit;
import org.openmarkov.core.action.core.TimeSliceEdit;
import org.openmarkov.core.action.core.UnitEdit;
import org.openmarkov.core.action.core.VariableTypeConstraintEdit;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlyNumericVariables;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The cases of {@link UndoRestoresTheNetworkTest}: for each edit of the core module, a small
 * network and one edit built on it.
 *
 * <p>A case whose fourth field is not null names an edit that does not restore the network
 * today, with the identifier of the finding that describes it. The test checks that those
 * still fail; fixing one means removing its identifier here.
 *
 * @author Manuel Arias
 */
final class EditUndoCases {

    /**
     * Edits of the core module that have no case yet, each with the reason.
     */
    static final Map<String, String> WITHOUT_A_CASE = new LinkedHashMap<>();

    static {
        WITHOUT_A_CASE.put("RemovePotentialsEdit", "nobody calls it");
        WITHOUT_A_CASE.put("ListPNEdit", "a container of other edits, not an edit of its own");
        WITHOUT_A_CASE.put("COrientLinksEdit", "a container of OrientLinkEdit, covered by that one");
        WITHOUT_A_CASE.put("UncertainValuesEdit", "needs a table of uncertain values; pending");
        WITHOUT_A_CASE.put("UncertainValuesRemoveEdit", "needs a table of uncertain values; pending");
        WITHOUT_A_CASE.put("RemoveMarkovNetNodeEdit", "only reached from a Markov network projection; pending");
        WITHOUT_A_CASE.put("EventNodeAlwaysAppendEdit", "needs an event node of a simulation network; pending");
        WITHOUT_A_CASE.put("ICIPotentialEdit", "needs a canonical model in the network; pending");
        WITHOUT_A_CASE.put("AbsorbParentsEdit", "absorbs intermediate utility nodes; needs a network with a "
                                                + "super-value node, pending");
    }

    /**
     * Builds an edit together with the network it works on.
     */
    @FunctionalInterface interface EditBuilder {
        PNEdit create() throws Exception;
    }

    /**
     * @param edit        the class of the edit, so that the census can find it
     * @param what        what this case does, to name the test
     * @param build       builds the network and the edit on it
     * @param knownDefect identifier of the finding, or null when undo must work
     */
    record Case(Class<? extends PNEdit> edit, String what, EditBuilder build, String knownDefect) {
        @Override public String toString() {
            return edit.getSimpleName() + " — " + what;
        }
    }

    private static Case passes(Class<? extends PNEdit> edit, String what, EditBuilder build) {
        return new Case(edit, what, build, null);
    }

    private static Case fails(Class<? extends PNEdit> edit, String what, String finding, EditBuilder build) {
        return new Case(edit, what, build, finding);
    }

    static List<Case> all() {
        List<Case> cases = new ArrayList<>();

        // Nodes and links
        cases.add(passes(AddNodeEdit.class, "adding a chance node",
                () -> new AddNodeEdit(bayesianNetwork(), new Variable("D", 2), NodeType.CHANCE, null)));
        cases.add(fails(RemoveNodeEdit.class, "removing a node with parents",
                "undo() puts the node back without its links; the only caller, CRemoveNodeEdit, "
                + "removes and restores them in separate steps", () -> {
            ProbNet net = bayesianNetwork();
            return new RemoveNodeEdit(net, net.getNode("C"));
        }));
        cases.add(passes(CRemoveNodeEdit.class, "removing a node and its links", () -> {
            ProbNet net = bayesianNetwork();
            return new CRemoveNodeEdit(net, net.getNode("C"));
        }));
        cases.add(passes(AddLinkEdit.class, "adding a directed link", () -> {
            ProbNet net = bayesianNetwork();
            return new AddLinkEdit(net, net.getVariable("A"), net.getVariable("B"), true);
        }));
        cases.add(passes(RemoveLinkEdit.class, "removing a directed link", () -> {
            ProbNet net = bayesianNetwork();
            return new RemoveLinkEdit(net, net.getVariable("A"), net.getVariable("C"), true);
        }));
        cases.add(passes(InvertLinkEdit.class, "inverting a directed link", () -> {
            ProbNet net = bayesianNetwork();
            return new InvertLinkEdit(net, net.getVariable("A"), net.getVariable("C"), true);
        }));
        cases.add(passes(InvertLinkAndUpdatePotentialsEdit.class, "inverting a link and its potentials", () -> {
            ProbNet net = bayesianNetwork();
            return new InvertLinkAndUpdatePotentialsEdit(net, net.getVariable("A"), net.getVariable("C"));
        }));
        cases.add(passes(OrientLinkEdit.class, "orienting an undirected link", () -> {
            ProbNet net = bayesianNetwork();
            net.addLink(net.getVariable("A"), net.getVariable("B"), false);
            return new OrientLinkEdit(net, net.getVariable("A"), net.getVariable("B"), true);
        }));
        cases.add(passes(MultiAddLinkEdit.class, "adding two links at once", () -> {
            ProbNet net = bayesianNetwork();
            return new MultiAddLinkEdit(net, List.of(net.getNode("A")), List.of(net.getNode("B")), true);
        }));
        cases.add(fails(AbsorbNodeEdit.class, "absorbing a parent into its child", "G2", () -> {
            ProbNet net = bayesianNetwork();
            return new AbsorbNodeEdit(net, net.getVariable("A"));
        }));

        // Properties of a node
        cases.add(passes(NodeCommentEdit.class, "changing the comment",
                () -> new NodeCommentEdit(bayesianNetwork().getNode("A"), "a comment")));
        cases.add(passes(NodeBaseNameEdit.class, "renaming the variable",
                () -> new NodeBaseNameEdit(bayesianNetwork().getNode("A"), "Renamed")));
        cases.add(passes(PurposeEdit.class, "changing the purpose",
                () -> new PurposeEdit(bayesianNetwork().getNode("A"), "Symptom")));
        cases.add(passes(RelevanceEdit.class, "changing the relevance",
                () -> new RelevanceEdit(bayesianNetwork().getNode("A"), 7.0)));
        cases.add(passes(UnitEdit.class, "changing the unit",
                () -> new UnitEdit(bayesianNetwork().getNode("A"), "kilogram")));
        cases.add(passes(PrecisionEdit.class, "changing the precision",
                () -> new PrecisionEdit(bayesianNetwork().getNode("A"), 0.5)));
        cases.add(passes(TimeSliceEdit.class, "putting the node in a time slice",
                () -> new TimeSliceEdit(bayesianNetwork().getNode("A"), 1)));
        cases.add(passes(NodeAlwaysObservedEdit.class, "marking the node as always observed",
                () -> new NodeAlwaysObservedEdit(bayesianNetwork().getNode("A"), true)));
        cases.add(passes(NodeReplaceStatesEdit.class, "replacing the states",
                () -> new NodeReplaceStatesEdit(bayesianNetwork().getNode("A"),
                        new State[] { new State("low"), new State("high") })));
        cases.add(fails(NodeStateEdit.class, "adding a state",
                "G4, and undo() also hands the node the potentials of its neighbours, because it saves "
                + "what getPotentials(variable) returns", () -> new NodeStateEdit(
                        bayesianNetwork().getNode("A"), StateAction.ADD, 0, "another")));
        cases.add(passes(NodeStateEdit.class, "renaming a state",
                () -> new NodeStateEdit(bayesianNetwork().getNode("A"), StateAction.RENAME, 0, "renamed")));
        cases.add(fails(NodeStateEdit.class, "removing a state",
                "G4, same as adding a state", () -> new NodeStateEdit(
                        bayesianNetwork().getNode("A"), StateAction.REMOVE, 0, "absent")));
        cases.add(passes(VariableTypeEdit.class, "turning the variable into a numeric one",
                () -> new VariableTypeEdit(bayesianNetwork().getNode("A"), VariableType.NUMERIC, true)));

        // Potentials
        cases.add(passes(PotentialChangeEdit.class, "changing the potential of a node", () -> {
            ProbNet net = bayesianNetwork();
            Node node = net.getNode("A");
            return new PotentialChangeEdit(node, node.getPotentials().getFirst(), otherPotentialOf(node));
        }));
        cases.add(passes(SetPotentialVariablesEdit.class, "reordering the variables of a potential", () -> {
            ProbNet net = bayesianNetwork();
            Node node = net.getNode("C");
            List<Variable> variables = node.getPotentials().getFirst().getVariables();
            return new SetPotentialVariablesEdit(node,
                    List.of(variables.get(0), variables.get(2), variables.get(1)));
        }));
        cases.add(passes(SetPotentialEdit.class, "replacing the potential of a node", () -> {
            ProbNet net = bayesianNetwork();
            Node node = net.getNode("A");
            return new SetPotentialEdit(node, otherPotentialOf(node));
        }));
        cases.add(passes(AddPotentialEdit.class, "adding a potential", () -> {
            ProbNet net = bayesianNetwork();
            return new AddPotentialEdit(net, otherPotentialOf(net.getNode("A")));
        }));
        cases.add(passes(RemovePolicyEdit.class, "removing the policy of a decision", () -> {
            ProbNet net = influenceDiagram();
            return new RemovePolicyEdit(net.getNode("D"));
        }));
        cases.add(passes(RevelationStateEdit.class, "declaring a state that reveals a link", () -> {
            ProbNet net = bayesianNetwork();
            Node from = net.getNode("A");
            Node to = net.getNode("C");
            return new RevelationStateEdit(net.getLink(from, to, true),
                    from.getVariable().getStates()[0], true);
        }));

        // Properties of the network
        cases.add(passes(NetworkCommentEdit.class, "changing the comment of the network",
                () -> new NetworkCommentEdit(bayesianNetwork(), "a comment", false)));
        cases.add(passes(NetworkDefaultStatesEdit.class, "changing the default states",
                () -> new NetworkDefaultStatesEdit(bayesianNetwork(),
                        new State[] { new State("yes"), new State("no") })));
        cases.add(passes(ChangeNetworkTypeEdit.class, "turning a Bayesian network into an influence diagram",
                () -> new ChangeNetworkTypeEdit(bayesianNetwork(), InfluenceDiagramType.getUniqueInstance())));
        cases.add(passes(CycleLengthEdit.class, "changing the length of the cycle",
                () -> new CycleLengthEdit(bayesianNetwork(), new CycleLength(CycleLength.Unit.MONTH, 3))));
        cases.add(passes(TemporalOptionsEdit.class, "changing the temporal options",
                () -> new TemporalOptionsEdit(bayesianNetwork(), new TemporalOptions())));
        cases.add(passes(MonteCarloOptionsEdit.class, "changing the simulation options",
                () -> new MonteCarloOptionsEdit(bayesianNetwork(), new MonteCarloOptions())));
        cases.add(passes(MulticriteriaEdit.class, "changing the criteria of the network",
                () -> new MulticriteriaEdit(influenceDiagram(), List.of(new Criterion("cost")),
                        new MulticriteriaOptions())));
        cases.add(passes(DecisionCriteriaEdit.class, "adding a decision criterion",
                () -> new DecisionCriteriaEdit(influenceDiagram(), StateAction.ADD, new Criterion("cost"), "cost")));
        cases.add(passes(DecisionCriterionUnitEdit.class, "changing the unit of a criterion", () -> {
            ProbNet net = influenceDiagram();
            return new DecisionCriterionUnitEdit(net, net.getDecisionCriteria().getFirst().getCriterionName(), "euro");
        }));

        // Constraints of the network
        cases.add(passes(RemoveConstraintEdit.class, "removing a constraint", () -> {
            ProbNet net = bayesianNetwork();
            net.addConstraint(new OnlyDiscreteVariables());
            return new RemoveConstraintEdit(net, net.getConstraints().getLast());
        }));
        cases.add(passes(VariableTypeConstraintEdit.class, "changing the constraint on the type of variable",
                () -> new VariableTypeConstraintEdit(bayesianNetwork(), new OnlyNumericVariables())));

        return cases;
    }

    // Networks

    /**
     * A → C ← B, with the default potentials.
     */
    static ProbNet bayesianNetwork() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("undo");
        new AddNodeEdit(net, new Variable("A", 2), NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, new Variable("B", 3), NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, new Variable("C", 2), NodeType.CHANCE, null).executeEdit();
        new AddLinkEdit(net, net.getVariable("A"), net.getVariable("C"), true).executeEdit();
        new AddLinkEdit(net, net.getVariable("B"), net.getVariable("C"), true).executeEdit();
        return net;
    }

    /**
     * X → D → U ← X, with one decision and one utility node.
     */
    static ProbNet influenceDiagram() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.setName("undo");
        new AddNodeEdit(net, new Variable("X", 2), NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, new Variable("D", 2), NodeType.DECISION, null).executeEdit();
        new AddNodeEdit(net, new Variable("U"), NodeType.UTILITY, null).executeEdit();
        new AddLinkEdit(net, net.getVariable("X"), net.getVariable("D"), true).executeEdit();
        new AddLinkEdit(net, net.getVariable("D"), net.getVariable("U"), true).executeEdit();
        new AddLinkEdit(net, net.getVariable("X"), net.getVariable("U"), true).executeEdit();
        return net;
    }

    /**
     * A table potential over the same variables as the first potential of the node, with values
     * that differ from it.
     */
    private static TablePotential otherPotentialOf(Node node) {
        List<Variable> variables = node.getPotentials().getFirst().getVariables();
        TablePotential potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] values = potential.getValues();
        for (int position = 0; position < values.length; position++) {
            values[position] = (position % 2 == 0) ? 0.25 : 0.75;
        }
        return potential;
    }

    private EditUndoCases() {
    }
}
