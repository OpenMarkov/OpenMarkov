/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The messages of the network constraints name each node, variable and network by its name instead of describing it
 * whole, and break their lines where they mean to.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheMessagesOfTheConstraintsNameEachNodeTest {

    static Stream<Class<?>> exceptions() {
        return Arrays.stream(ConstraintViolatedException.class.getDeclaredClasses())
                     .filter(type -> ConstraintViolatedException.class.isAssignableFrom(type))
                     .filter(type -> !Modifier.isAbstract(type.getModifiers()))
                     .filter(type -> !type.getSimpleName().equals("MultipleConstraintsViolated"));
    }

    /** The exception of class {@code type} built with nodes C, a chance node, and D, a decision, of a network. */
    private static ConstraintViolatedException built(Class<?> type) throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.setName("net");
        Node c = net.addNode(new Variable("C", "no", "yes"), NodeType.CHANCE);
        Node d = net.addNode(new Variable("D", "no", "yes"), NodeType.DECISION);
        Constructor<?> constructor = type.getConstructors()[0];
        Class<?>[] parameters = constructor.getParameterTypes();
        Object[] arguments = new Object[parameters.length];
        int nodes = 0;
        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameter = parameters[i];
            if (parameter == Node.class) {
                arguments[i] = (nodes++ % 2 == 0) ? c : d;
            } else if (parameter == Variable.class) {
                arguments[i] = (nodes++ % 2 == 0) ? c.getVariable() : d.getVariable();
            } else if (parameter == List.class) {
                arguments[i] = List.of(c, d);
            } else if (parameter == String.class) {
                arguments[i] = "name";
            } else if (parameter == int.class) {
                arguments[i] = 3;
            } else if (parameter == ProbNet.class) {
                arguments[i] = net;
            } else if (parameter == Criterion.class) {
                arguments[i] = new Criterion("cost");
            }
        }
        return (ConstraintViolatedException) constructor.newInstance(arguments);
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void theMessageDoesNotDescribeANodeWhole(Class<?> type) throws Exception {
        String message = built(type).toString();

        assertFalse(message.contains("with states"), message);
        assertFalse(message.contains("node of"), message);
        assertFalse(message.contains("network named"), message);
        assertFalse(message.contains("\\n"), message);
    }

    @Test void aDecisionInABayesianNetworkIsRefusedNamingTheNode() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        AddNodeEdit edit = new AddNodeEdit(net, new Variable("C", "no", "yes"), NodeType.DECISION, new Point2D.Double(0, 0));

        var refused = assertThrows(ConstraintViolatedException.class, edit::tryConstraintsWillBeMet);

        assertTrue(refused.toString().contains("Node C must be a chance node."), refused.toString());
    }
}
