/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.NodeBaseNameEdit;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The name of a node or of a state cannot have a character that a network file cannot keep, such as the manual line
 * break of Word (U+000B), because the network could not be saved.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANameWithACharacterAFileCannotKeepIsRefusedTest {

    private ProbNet net;
    private Node node;

    @BeforeEach void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        node = net.addNode(new Variable("X", "yes", "no"), NodeType.CHANCE);
    }

    private static ConstraintViolatedException refusal(Executable edit) {
        var refused = assertThrows(DoEditException.CannotDoEditException.class, edit);
        return assertInstanceOf(ConstraintViolatedException.class, refused.originException);
    }

    @ParameterizedTest @ValueSource(strings = {"Line\u000Bbreak", "Bell\u0007"})
    void renamingANodeIsRefused(String name) {
        ConstraintViolatedException refused = refusal(new NodeBaseNameEdit(node, name)::executeEdit);

        assertInstanceOf(ConstraintViolatedException.NameOfVariableHasACharacterAFileCannotKeep.class, refused);
        assertEquals("X", node.getName());
    }

    @Test void addingANodeIsRefused() {
        var edit = new org.openmarkov.core.action.core.AddNodeEdit(net, new Variable("Bell\u0007"), NodeType.CHANCE, null);

        assertInstanceOf(ConstraintViolatedException.NameOfVariableHasACharacterAFileCannotKeep.class,
                         refusal(edit::executeEdit));
        assertEquals(1, net.getNumNodes());
    }

    @Test void renamingAStateIsRefused() {
        assertInstanceOf(ConstraintViolatedException.NameOfStateHasACharacterAFileCannotKeep.class,
                         refusal(new NodeStateEdit(node, StateAction.RENAME, 0, "no\u000B")::executeEdit));
    }

    @Test void aTabIsStillAccepted() {
        assertDoesNotThrow(() -> new NodeBaseNameEdit(node, "with\ttab").executeEdit());
    }

    @Test void aWholeNetworkWithSuchNamesBreaksBothRules() {
        net.addNode(new Variable("Line\u000Bbreak", "a", "b\u0007"), NodeType.CHANCE);
        ConstraintChecker checker = new ConstraintChecker(net);

        net.getConstraintOfClass(NoEmptyName.class).checkProbNet(net, checker);
        net.getConstraintOfClass(ValidState.class).checkProbNet(net, checker);

        List<Class<?>> broken = checker.getViolations().stream().<Class<?>>map(Object::getClass).toList();
        assertThat(broken).containsExactlyInAnyOrderElementsOf(List.of(
                ConstraintViolatedException.NameOfVariableHasACharacterAFileCannotKeep.class,
                ConstraintViolatedException.NameOfStateHasACharacterAFileCannotKeep.class));
    }
}
