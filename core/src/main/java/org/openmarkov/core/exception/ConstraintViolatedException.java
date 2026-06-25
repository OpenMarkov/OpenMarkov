package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.stringformat.LocalizationFormatter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ConstraintViolatedException extends OpenMarkovException {
    public final PNConstraint constraint;
    
    public ConstraintViolatedException(PNConstraint constraint) {
        super();
        this.constraint = constraint;
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        String constraintExceptionMessage = Localizable.localize(this, formatter, ConstraintViolatedException.class.getName());
        String concreteExceptionMessage = Localizable.localize(this, formatter, this.getClass().getName());
        return constraintExceptionMessage + System.lineSeparator() + concreteExceptionMessage;
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
    public static class MultipleConstraintsViolateds extends ConstraintViolatedException {
        
        private final TreeMap<PNConstraint, TreeSet<ConstraintViolatedException>> exceptionsMap;
        
        public MultipleConstraintsViolateds(Collection<ConstraintViolatedException> exceptions) {
            super(null);
            exceptionsMap = new TreeMap<>(Comparator.comparing(constraint -> constraint.getClass().getSimpleName()));
            for (ConstraintViolatedException exception : exceptions) {
                exceptionsMap
                        .computeIfAbsent(exception.constraint,
                                         k -> new TreeSet<>(Comparator.comparing(Object::toString)))
                        .add(exception);
            }
        }
        
        @Override public @Nullable String getExceptionTitle() {
            if (thereIsOnlyOneKindOfConstraintViolated()) {
                return getFirstExceptionFound().getExceptionTitle();
            }
            return super.getExceptionTitle();
        }
        
        @Override public @NotNull String localize(LocalizationFormatter formatter) {
            var exceptionsMapAsStrings = this.exceptionsMap.values().stream().map(exceptions -> {
                var firstExceptionFound = exceptions.iterator().next();
                String constraintExceptionMessage = Localizable.localize(firstExceptionFound, formatter, ConstraintViolatedException.class.getName());
                var concreteExceptionMessages = exceptions
                        .stream()
                        .map(e -> Localizable.localize(e, formatter, e.getClass().getName()))
                        .map(MultipleConstraintsViolateds::tabList)
                        .collect(Collectors.joining(System.lineSeparator()));
                return constraintExceptionMessage + System.lineSeparator() + concreteExceptionMessages;
            });
            if (thereIsOnlyOneKindOfConstraintViolated()) {
                return exceptionsMapAsStrings.findFirst().get();
            }
            String constraintExceptionMessage = Localizable.localize(this, formatter, MultipleConstraintsViolateds.class.getName());
            String individualConstraintViolationReasons = exceptionsMapAsStrings
                    .map(MultipleConstraintsViolateds::tabList)
                    .collect(Collectors.joining(System.lineSeparator()));
            return constraintExceptionMessage + System.lineSeparator() + individualConstraintViolationReasons;
        }
        
        private static String tabList(String originalText) {
            String finalText = "";
            Iterator<String> lines = originalText.lines().iterator();
            if (lines.hasNext()) {
                finalText += "- " + lines.next();
            }
            while (lines.hasNext()) {
                finalText += System.lineSeparator() + "  " + lines.next();
            }
            return finalText;
        }
        
        private @Nullable ConstraintViolatedException getFirstExceptionFound() {
            TreeSet<ConstraintViolatedException> firstExceptionTree = getFirstExceptionTree();
            if (firstExceptionTree == null) return null;
            return firstExceptionTree.stream().findFirst().orElse(null);
        }
        
        private @Nullable TreeSet<ConstraintViolatedException> getFirstExceptionTree() {
            Collection<TreeSet<ConstraintViolatedException>> exceptionsTrees = exceptionsMap.values();
            if (exceptionsTrees.isEmpty()) {
                return null;
            }
            TreeSet<ConstraintViolatedException> firstExceptionTree = exceptionsTrees.stream().findFirst().orElse(null);
            if (firstExceptionTree == null || firstExceptionTree.isEmpty()) {
                return null;
            }
            return firstExceptionTree;
        }
        
        private boolean thereIsOnlyOneKindOfConstraintViolated() {
            return exceptionsMap.keySet().size() == 1 && !exceptionsMap.values().stream().findFirst().get().isEmpty();
        }
        
        
    }
    
    public static class LinkAlreadyExists extends ConstraintViolatedException {
        public LinkAlreadyExists(DistinctLinks constraint, Node from, Node to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Node from;
        private final Node to;
    }
    
    public static class VariableNameIsAlreadyPresent extends ConstraintViolatedException {
        
        public VariableNameIsAlreadyPresent(DistinctVariableNames constraint, String name) {
            super(constraint);
            this.name = name;
        }
        
        private final String name;
    }
    
    public static class NodeCannotHaveMoreParents extends ConstraintViolatedException {
        
        public NodeCannotHaveMoreParents(MaxNumParents constraint, Node node, int parentsSize) {
            super(constraint);
            this.node = node;
            this.parentsSize = parentsSize;
        }
        
        private final Node node;
        private final int parentsSize;
    }
    
    public static class ModelDoesNotAllowAddingLink extends ConstraintViolatedException {
        
        public ModelDoesNotAllowAddingLink(ModelNetworkConstraint constraint, Variable from, Variable to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Variable from;
        private final Variable to;
    }
    
    public static class ModelDoesNotAllowInvertingLink extends ConstraintViolatedException {
        
        public ModelDoesNotAllowInvertingLink(ModelNetworkConstraint constraint, Variable from, Variable to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Variable from;
        private final Variable to;
    }
    
    public static class ModelDoesNotAllowRemovingLink extends ConstraintViolatedException {
        
        public ModelDoesNotAllowRemovingLink(ModelNetworkConstraint constraint, Variable from, Variable to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Variable from;
        private final Variable to;
    }
    
    public static class VariableHasNoPotentials extends ConstraintViolatedException {
        
        public VariableHasNoPotentials(AllChanceVariablesHaveChancePotentials constraint, Variable variable) {
            super(constraint);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class NodeHasRepeatedLinks extends ConstraintViolatedException {
        
        public NodeHasRepeatedLinks(DistinctLinks distinctLinks, Node node) {
            super(distinctLinks);
            this.node = node;
        }
        
        private final Node node;
    }
    
    public static class NodeHasMoreParentsThanAllowed extends ConstraintViolatedException {
        
        public NodeHasMoreParentsThanAllowed(MaxNumParents maxNumParents, Node child, int numParents, int maxNumOfParentsAllowed) {
            super(maxNumParents);
            this.child = child;
            this.numParents = numParents;
            this.maxNumOfParentsAllowed = maxNumOfParentsAllowed;
        }
        
        private final Node child;
        private final int numParents;
        private final int maxNumOfParentsAllowed;
    }
    
    public static class AlwaysObservedVariableIsDescendantOfDecisionNode extends ConstraintViolatedException {
        
        public AlwaysObservedVariableIsDescendantOfDecisionNode(NoAlwaysObservedDescendantOfDecision constraint, Node alwaysObservedNode, Node decisionNode) {
            super(constraint);
            this.alwaysObservedNode = alwaysObservedNode;
            this.decisionNode = decisionNode;
        }
        
        private final Node alwaysObservedNode;
        private final Node decisionNode;
    }
    
    
    public static class CannotAddLinkToAPreviousTimeSlice extends ConstraintViolatedException {
        
        public CannotAddLinkToAPreviousTimeSlice(NoBackwardLink constraint, Variable parentVariable, Variable childVariable) {
            super(constraint);
            this.parentVariable = parentVariable;
            this.childVariable = childVariable;
        }
        
        private final Variable parentVariable;
        private final Variable childVariable;
    }
    
    public static class ThereIsACycle extends ConstraintViolatedException {
        
        public ThereIsACycle(NoCycle noCycle, Node parent, Node child) {
            super(noCycle);
            this.parent = parent;
            this.child = child;
        }
        
        private final Node parent;
        private final Node child;
    }
    
    public static class NameOfVariableCannotBeEmpty extends ConstraintViolatedException {
        
        public NameOfVariableCannotBeEmpty(PNConstraint constraint, Variable variable) {
            super(constraint);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class NameOfVariableIsAlreadyPresent extends ConstraintViolatedException {
        
        public NameOfVariableIsAlreadyPresent(PNConstraint constraint, String variableName) {
            super(constraint);
            this.variableName = variableName;
        }
        
        private final String variableName;
    }
    
    public static class ThereIsALoop extends ConstraintViolatedException {
        
        public ThereIsALoop(NoLoops constraint, Node node1, Node node2) {
            super(constraint);
            this.node1 = node1;
            this.node2 = node2;
        }
        
        private final Node node1;
        private final Node node2;
    }
    
    public static class MixedParentDoesntAllowThisNodeType extends ConstraintViolatedException {
        
        public MixedParentDoesntAllowThisNodeType(NoMixedParents constraint, Node child, Node parent) {
            super(constraint);
            this.child = child;
            this.parent = parent;
        }
        
        public final Node child;
        public final Node parent;
    }
    
    public static class MixedParentContainsMoreThanOneSet extends ConstraintViolatedException {
        
        public MixedParentContainsMoreThanOneSet(NoMixedParents constraint, Node child, List<Node> chanceOrDecisionNodes, List<Node> utilityNodes) {
            super(constraint);
            this.child = child;
            this.chanceOrDecisionNodes = chanceOrDecisionNodes;
            this.utilityNodes = utilityNodes;
        }
        
        public final Node child;
        public final List<Node> chanceOrDecisionNodes;
        public final List<Node> utilityNodes;
    }
    
    
    public static class DirectedLinkCannotMatchAnUndirectedLink extends ConstraintViolatedException {
        
        public DirectedLinkCannotMatchAnUndirectedLink(NoMultipleLinks constraint, Node node1, Node node2) {
            super(constraint);
            this.node1 = node1;
            this.node2 = node2;
        }
        
        private final Node node1;
        private final Node node2;
    }
    
    public static class CannotSelfLink extends ConstraintViolatedException {
        
        public CannotSelfLink(NoSelfLoop constraint, Node node) {
            super(constraint);
            this.node = node;
        }
        
        private final Node node;
    }
    
    public static class NoSuperValueNodeAllowed extends ConstraintViolatedException {
        
        public NoSuperValueNodeAllowed(NoSuperValueNode constraint, Node node) {
            super(constraint);
            this.node = node;
        }
        
        private final Node node;
    }
    
    public static class CannotHaveUtilityParent extends ConstraintViolatedException {
        
        public CannotHaveUtilityParent(NoUtilityParent constraint, Node child, Node utilityNode) {
            super(constraint);
            this.child = child;
            this.utilityNode = utilityNode;
        }
        
        private final Node child;
        private final Node utilityNode;
    }
    
    public static class OnlyAtemporalVariablesAllowed extends ConstraintViolatedException {
        
        public OnlyAtemporalVariablesAllowed(OnlyAtemporalVariables onlyAtemporalVariables, Variable variable) {
            super(onlyAtemporalVariables);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyChanceNodesAllowed extends ConstraintViolatedException {
        
        public OnlyChanceNodesAllowed(OnlyChanceNodes onlyChanceNodes, Variable variable) {
            super(onlyChanceNodes);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    //{eventNode} is of type event.
    public static class CannotHaveEventNodeException extends ConstraintViolatedException {
        
        public final Node eventNode;
        
        public CannotHaveEventNodeException(NoEventNodes constraint, Node eventNode) {
            super(constraint);
            this.eventNode = eventNode;
        }
        
    }
    
    //There can be only one orphan event node which the purpose of initial, but multiple have it:\n{orphanEventNodes}
    public static class    OnlyOneOrphanInitialEventException extends ConstraintViolatedException {
        
        public final List<Node> orphanEventNodes;
        
        public OnlyOneOrphanInitialEventException(OnlyOneOrphanInitialEvent constraint, List<Node> orphanEventNodes) {
            super(constraint);
            this.orphanEventNodes = orphanEventNodes;
        }
    }
    
    
    //Self loop can only be used if the node is event or chance, which is not applicable to {node}.
    public static class    OnlySelfLoopsWithEventAndChanceNodesException extends ConstraintViolatedException {
        
        public final Node node;
        
        public OnlySelfLoopsWithEventAndChanceNodesException(OnlySelfLoopsWithEventAndChanceNodes constraint, Node node) {
            super(constraint);
            this.node = node;
        }
    }
    
    
    public static class OnlyContinuousVariablesAllowed extends ConstraintViolatedException {
        
        public OnlyContinuousVariablesAllowed(OnlyContinuousVariables onlyContinuousVariables, Variable variable) {
            super(onlyContinuousVariables);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyDirectedLinksAllowed extends ConstraintViolatedException {
        
        public OnlyDirectedLinksAllowed(OnlyDirectedLinks onlyDirectedLinks, Node node, List<Node> siblings) {
            super(onlyDirectedLinks);
            this.node = node;
            this.siblings = siblings;
        }
        
        
        private final Node node;
        private final List<Node> siblings;
    }
    
    public static class OnlyDiscreteVariablesAllowed extends ConstraintViolatedException {
        
        public OnlyDiscreteVariablesAllowed(OnlyDiscreteVariables onlyDiscreteVariables, Variable variable) {
            super(onlyDiscreteVariables);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyFiniteStatesAllowed extends ConstraintViolatedException {
        
        public OnlyFiniteStatesAllowed(OnlyFiniteStatesVariables constraint, Variable variable) {
            super(constraint);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyNumericVariablesAllowed extends ConstraintViolatedException {
        
        public OnlyNumericVariablesAllowed(OnlyNumericVariables constraint, Variable variable) {
            super(constraint);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyOneAgentAllowed extends ConstraintViolatedException {
        
        public OnlyOneAgentAllowed(OnlyOneAgent constraint, Variable variable) {
            super(constraint);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyTemporalVariablesAllowed extends ConstraintViolatedException {
        
        public OnlyTemporalVariablesAllowed(OnlyTemporalVariables constraint, Variable variable) {
            super(constraint);
            this.variable = variable;
        }
        
        private final Variable variable;
    }
    
    public static class OnlyOneAgentAllowedInNetwork extends ConstraintViolatedException {
        
        public OnlyOneAgentAllowedInNetwork(OnlyOneAgent onlyOneAgent, ProbNet probNet) {
            super(onlyOneAgent);
            this.probNet = probNet;
        }
        
        private final ProbNet probNet;
    }
    
    public static class OnlyUndirectedLinksCannotHaveChildren extends ConstraintViolatedException {
        
        public OnlyUndirectedLinksCannotHaveChildren(OnlyUndirectedLinks onlyUndirectedLinks, Node node, List<Node> children) {
            super(onlyUndirectedLinks);
            this.node = node;
            this.children = children;
        }
        
        private final Node node;
        private final List<Node> children;
    }
    
    public static class OnlyUndirectedLinksCannotHaveParents extends ConstraintViolatedException {
        
        public OnlyUndirectedLinksCannotHaveParents(OnlyUndirectedLinks onlyUndirectedLinks, Node node, List<Node> parents) {
            super(onlyUndirectedLinks);
            this.node = node;
            this.parents = parents;
        }
        
        private final Node node;
        private final List<Node> parents;
    }
    
    public static class NetworkHasNoUtilityNodes extends ConstraintViolatedException {
        
        public NetworkHasNoUtilityNodes(PNConstraint properUtilityPotentials, ProbNet probNet) {
            super(properUtilityPotentials);
            this.probNet = probNet;
        }
        
        private final ProbNet probNet;
    }
    
    public static class UtilityNodeHasNoPotentials extends ConstraintViolatedException {
        
        public UtilityNodeHasNoPotentials(ProperUtilityPotentials constraint, Node utilityNode) {
            super(constraint);
            this.utilityNode = utilityNode;
        }
        
        private final Node utilityNode;
    }
    
    public static class NumOfPotentialsMismatchesNumOfUtilities extends ConstraintViolatedException {
        
        public NumOfPotentialsMismatchesNumOfUtilities(UtilityNodes utilityNodes, int numUtilityNodes, int numUtilityPotentials) {
            super(utilityNodes);
            this.numUtilityNodes = numUtilityNodes;
            this.numUtilityPotentials = numUtilityPotentials;
        }
        
        private final int numUtilityNodes;
        private final int numUtilityPotentials;
    }
    
    public static class CriterionNameIsEmpty extends ConstraintViolatedException {
        
        public CriterionNameIsEmpty(ValidCriterionName constraint, Criterion criterion) {
            super(constraint);
            this.criterion = criterion;
        }
        
        private final Criterion criterion;
    }
    
    public static class CriterionNameIsAlreadyPresent extends ConstraintViolatedException {
        
        public CriterionNameIsAlreadyPresent(ValidCriterionName constraint, Criterion criterion, String name) {
            super(constraint);
            this.criterion = criterion;
            this.name = name;
        }
        
        private final Criterion criterion;
        private final String name;
    }
    
    public static class NameOfStateCannotBeEmpty extends ConstraintViolatedException {
        
        public NameOfStateCannotBeEmpty(ValidState constraint, Node node) {
            super(constraint);
            this.node = node;
        }
        
        private final Node node;
    }
    
    public static class StateAlreadyExists extends ConstraintViolatedException {
        
        public StateAlreadyExists(ValidState validState, Node node, String newState) {
            super(validState);
            this.node = node;
            this.newState = newState;
        }
        
        private final Node node;
        private final String newState;
    }
    
    public static class StateDuplicated extends ConstraintViolatedException {
        
        public StateDuplicated(ValidState validState, Node node, String stateName) {
            super(validState);
            this.node = node;
            this.stateName = stateName;
        }
        
        private final Node node;
        private final String stateName;
    }
}
