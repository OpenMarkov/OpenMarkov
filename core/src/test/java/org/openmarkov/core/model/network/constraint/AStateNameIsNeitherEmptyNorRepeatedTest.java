/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.NodeReplaceStatesEdit;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A state of a variable of finite states has a name, and no two states share one, whatever their capitals and
 * surrounding spaces (error 38 of the pending list). The edits refuse to break the rule and a whole network is checked
 * against it; the only state of a numeric variable has no name.
 *
 * @author Manuel Arias
 */
class AStateNameIsNeitherEmptyNorRepeatedTest {

    private ProbNet net;
    private Variable variable;
    private Node node;

    @BeforeEach void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        variable = new Variable("X", "yes", "no");
        node = net.addNode(variable, NodeType.CHANCE);
    }

    private String[] stateNames() {
        return Arrays.stream(variable.getStates()).map(State::getName).toArray(String[]::new);
    }

    /** The constraint the edit breaks when it is refused. */
    private static ConstraintViolatedException refusal(NodeStateEdit edit) {
        var refused = assertThrows(DoEditException.CannotDoEditException.class, edit::executeEdit);
        return assertInstanceOf(ConstraintViolatedException.class, refused.originException);
    }

    static Stream<NetworkType> networkTypes() {
        return Stream.of(BayesianNetworkType.getUniqueInstance(), InfluenceDiagramType.getUniqueInstance(),
                         MIDType.getUniqueInstance(), DecisionAnalysisNetworkType.getUniqueInstance(),
                         DESNetworkType.getUniqueInstance());
    }

    @ParameterizedTest @MethodSource("networkTypes")
    void everyTypeOfNetworkHasTheRule(NetworkType type) {
        assertNotNull(new ProbNet(type).getConstraintOfClass(ValidState.class));
    }

    @ParameterizedTest @ValueSource(strings = { "Yes", "yes", " YES " })
    void renamingToTheNameOfAnotherStateIsRefused(String name) {
        // stateIndex is inverted: 0 → the last state, "no"
        assertInstanceOf(ConstraintViolatedException.StateAlreadyExists.class,
                         refusal(new NodeStateEdit(node, StateAction.RENAME, 0, name)));
        assertArrayEquals(new String[]{"yes", "no"}, stateNames());
    }

    @ParameterizedTest @ValueSource(strings = { "", "   " })
    void renamingToAnEmptyNameIsRefused(String name) {
        assertInstanceOf(ConstraintViolatedException.NameOfStateCannotBeEmpty.class,
                         refusal(new NodeStateEdit(node, StateAction.RENAME, 0, name)));
        assertArrayEquals(new String[]{"yes", "no"}, stateNames());
    }

    @ParameterizedTest @ValueSource(strings = { "no", "NO", "maybe" })
    void renamingToItsOwnNameOrToAFreeOneIsAccepted(String name) {
        assertDoesNotThrow(() -> new NodeStateEdit(node, StateAction.RENAME, 0, name).executeEdit());
        assertArrayEquals(new String[]{"yes", name}, stateNames());
    }

    @Test void addingAStateWithARepeatedOrEmptyNameIsRefused() {
        assertInstanceOf(ConstraintViolatedException.StateAlreadyExists.class,
                         refusal(new NodeStateEdit(node, StateAction.ADD, 2, "No")));
        assertInstanceOf(ConstraintViolatedException.NameOfStateCannotBeEmpty.class,
                         refusal(new NodeStateEdit(node, StateAction.ADD, 2, "")));
        assertArrayEquals(new String[]{"yes", "no"}, stateNames());
    }

    @Test void replacingTheStatesWithRepeatedNamesIsRefused() {
        var edit = new NodeReplaceStatesEdit(node, new State[]{new State("low"), new State("Low")});
        assertThrows(DoEditException.CannotDoEditException.class, edit::executeEdit);
        assertArrayEquals(new String[]{"yes", "no"}, stateNames());
    }

    @Test void aWholeNetworkWithRepeatedOrEmptyNamesBreaksTheRule() {
        net.addNode(new Variable("Y", "a", "A"), NodeType.CHANCE);
        net.addNode(new Variable("Z", "", "b"), NodeType.CHANCE);
        ConstraintChecker checker = new ConstraintChecker(net);
        net.getConstraintOfClass(ValidState.class).checkProbNet(net, checker);
        List<Class<?>> broken = checker.getViolations().stream().<Class<?>>map(Object::getClass).toList();
        assertEquals(List.of(ConstraintViolatedException.StateDuplicated.class,
                             ConstraintViolatedException.NameOfStateCannotBeEmpty.class), broken);
    }

    @Test void theUnnamedStateOfANumericVariableDoesNotBreakTheRule() {
        ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable cost = new Variable("Cost", true, 0, 100, true, 0.01);
        influenceDiagram.addNode(cost, NodeType.UTILITY);
        assertEquals(List.of(""), Arrays.stream(cost.getStates()).map(State::getName).toList());
        ConstraintChecker checker = new ConstraintChecker(influenceDiagram);
        influenceDiagram.getConstraintOfClass(ValidState.class).checkProbNet(influenceDiagram, checker);
        assertTrue(checker.getViolations().isEmpty(), checker.getViolations().toString());
    }
}
