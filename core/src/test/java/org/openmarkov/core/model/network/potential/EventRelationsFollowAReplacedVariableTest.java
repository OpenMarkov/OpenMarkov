/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pasting a node copies its relation and replaces its variables, one by one, by those of the pasted
 * nodes. The relations of the discrete-event simulation keep a table or some trees inside, and
 * those kept naming the variables of the original nodes: the pasted relation read always the first
 * state of each parent, or could not be sampled at all (error 21 of the pending list).
 *
 * <p>For each of them, what is checked is that after replacing every variable of a copy nothing in
 * it refers to a variable of the original, and that the copy answers what the original answers.
 *
 * @author Manuel Arias
 */
public class EventRelationsFollowAReplacedVariableTest {

    private static final double[] RANDOM = {0.5};

    private Variable ill;
    private Variable sex;
    private Variable onset;
    private Variable checkup;
    private Variable death;
    private Variable age;
    /** The variable of each pasted node, by the variable of the node it was copied from. */
    private Map<Variable, Variable> pasted;

    private static Variable event(String name) {
        Variable variable = new Variable(name, new State[]{new State("no"), new State("yes")});
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    @BeforeEach
    public void setUp() {
        ill = new Variable("Ill", new State[]{new State("no"), new State("yes")});
        sex = new Variable("Sex", new State[]{new State("male"), new State("female")});
        onset = event("Onset");
        checkup = event("Checkup");
        death = event("Death");
        age = new Variable("Age");
        pasted = new LinkedHashMap<>();
        for (Variable variable : List.of(ill, sex, onset, checkup, death, age)) {
            Variable copy = new Variable(variable);
            copy.setName(variable.getName() + "'");
            pasted.put(variable, copy);
        }
    }

    /** What pasting does to the relation of a node: a copy with every variable replaced. */
    private Potential paste(Potential original) {
        Potential copy = original.copy();
        for (int i = 0; i < copy.getNumVariables(); i++) {
            copy.replaceVariable(i, pasted.get(copy.getVariable(i)));
        }
        return copy;
    }

    private static EvidenceCase evidence(Finding... findings) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        for (Finding finding : findings) {
            evidence.addFinding(finding);
        }
        return evidence;
    }

    /** The same findings, on the variables of the pasted nodes. */
    private Finding[] onThePasted(Finding... findings) {
        Finding[] result = new Finding[findings.length];
        for (int i = 0; i < findings.length; i++) {
            Variable variable = pasted.get(findings[i].getVariable());
            result[i] = variable.getVariableType() == VariableType.NUMERIC
                    ? new Finding(variable, findings[i].getNumericalValue())
                    : new Finding(variable, findings[i].getStateIndex());
        }
        return result;
    }

    private void assertThePastedAnswersLikeTheOriginal(double expected, Potential original, Finding... findings)
            throws Exception {
        Potential copy = paste(original);
        assertEquals(expected, ((DESSimulablePotential) original).sampleConditionedVariable(RANDOM, evidence(findings)),
                1e-12, "the original");
        assertEquals(expected, assertInstanceOf(DESSimulablePotential.class, copy)
                .sampleConditionedVariable(RANDOM, evidence(onThePasted(findings))), 1e-12, "the pasted copy");
        assertNothingRefersToTheOriginalVariables(copy);
    }

    // -----------------------------------------------------------------------
    // No reference left to the variables of the original, wherever it is kept
    // -----------------------------------------------------------------------

    private void assertNothingRefersToTheOriginalVariables(Potential copy) throws Exception {
        List<String> found = new ArrayList<>();
        collectOriginalVariables(copy, copy.getClass().getSimpleName(), found,
                java.util.Collections.newSetFromMap(new IdentityHashMap<>()));
        assertTrue(found.isEmpty(), "the pasted relation still refers to variables of the original: " + found);
    }

    private void collectOriginalVariables(Object object, String path, List<String> found, Set<Object> visited)
            throws Exception {
        if (object == null || !visited.add(object)) {
            return;
        }
        if (object instanceof Variable variable) {
            if (pasted.containsKey(variable)) {
                found.add(path + " -> " + variable.getName());
            }
        } else if (object instanceof Collection<?> collection) {
            for (Object element : collection) {
                collectOriginalVariables(element, path + "[]", found, visited);
            }
        } else if (object instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                collectOriginalVariables(entry.getKey(), path + "{key}", found, visited);
                collectOriginalVariables(entry.getValue(), path + "{value}", found, visited);
            }
        } else if (object.getClass().isArray()) {
            if (!object.getClass().getComponentType().isPrimitive()) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    collectOriginalVariables(Array.get(object, i), path + "[]", found, visited);
                }
            }
        } else if (!(object instanceof Enum<?>)
                && object.getClass().getName().startsWith("org.openmarkov.core.model.network.potential")) {
            for (Class<?> type = object.getClass(); type.getName().startsWith("org.openmarkov"); type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
                        field.setAccessible(true);
                        collectOriginalVariables(field.get(object), path + "." + field.getName(), found, visited);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Each relation
    // -----------------------------------------------------------------------

    /** Exact time to the onset: 2 for men and 3 for women. */
    @Test
    public void aTimeToEventOverAChanceParent() throws Exception {
        DistributionTablePotential original = new DistributionTablePotential(
                new ArrayList<>(List.of(onset, sex)), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        original.getTableWithEvents().setValues(new double[]{2, 3});

        assertThePastedAnswersLikeTheOriginal(3, original, new Finding(sex, 1));
    }

    /** Exact time to death: 8 or 3 after the onset, 5 or 1 after the checkup, depending on being ill. */
    @Test
    public void aTimeToEventOverTwoEventsAndAChanceParent() throws Exception {
        DistributionTablePotential original = new DistributionTablePotential(
                new ArrayList<>(List.of(death, onset, checkup, ill)), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        original.getTableWithEvents().setValues(new double[]{8, 3, 5, 1});

        assertThePastedAnswersLikeTheOriginal(3, original, new Finding(onset, 1), new Finding(ill, 1));
        assertThePastedAnswersLikeTheOriginal(1, original, new Finding(checkup, 1), new Finding(ill, 1));
        assertThePastedAnswersLikeTheOriginal(5, original, new Finding(checkup, 1), new Finding(ill, 0));
    }

    @Test
    public void aTimeToEventWithANumericParent() throws Exception {
        DistributionTablePotential original = new DistributionTablePotential(
                new ArrayList<>(List.of(death, sex, age)), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        String[] functions = original.getTableWithEvents().getTableWithFunctions().getFunctionValues();
        functions[0] = "4";
        functions[1] = "7";

        assertThePastedAnswersLikeTheOriginal(7, original, new Finding(sex, 1), new Finding(age, 40.0));
    }

    /** The node becomes ill at the onset; at the checkup, only women do. */
    @Test
    public void aTransitionTable() throws Exception {
        TransitionTablePotential original = new TransitionTablePotential(
                new ArrayList<>(List.of(ill, onset, checkup, sex)), PotentialRole.CONDITIONAL_PROBABILITY);
        // Ill, then Sex, then the events
        original.setValues(new double[]{0, 1, 0, 1, 1, 0, 0, 1});

        assertThePastedAnswersLikeTheOriginal(1, original, new Finding(onset, 1), new Finding(sex, 0));
        assertThePastedAnswersLikeTheOriginal(0, original, new Finding(checkup, 1), new Finding(sex, 0));
        assertThePastedAnswersLikeTheOriginal(1, original, new Finding(checkup, 1), new Finding(sex, 1));
    }

    /** A cost of 100 when healthy and 2000 when ill. */
    @Test
    public void anExactValue() throws Exception {
        Variable cost = new Variable("Cost");
        pasted.put(cost, new Variable("Cost'"));
        ExactDistrPotential original = new ExactDistrPotential(
                new ArrayList<>(List.of(cost, ill)), PotentialRole.UNSPECIFIED, new double[]{100, 2000});

        assertThePastedAnswersLikeTheOriginal(2000, original, new Finding(ill, 1));
    }

    private TreeADDBranch branch(Variable top, int state, Potential potential, List<Variable> parents) {
        return new TreeADDBranch(new ArrayList<>(List.of(top.getStates()[state])), top, potential, new ArrayList<>(parents));
    }

    /** A tree on the sex: the first leaf for men, the second one for women. */
    private TreeADDPotential treeOnSex(Variable conditioned, Potential forMen, Potential forWomen) {
        return new TreeADDPotential(new ArrayList<>(List.of(conditioned, sex)), sex, PotentialRole.CONDITIONAL_PROBABILITY,
                new ArrayList<>(List.of(branch(sex, 0, forMen, List.of(sex)), branch(sex, 1, forWomen, List.of(sex)))));
    }

    /** After the onset, men stay healthy and women become ill. */
    @Test
    public void aTreeWithEvents() throws Exception {
        TreeWithEventsPotential original = new TreeWithEventsPotential(
                new ArrayList<>(List.of(ill, onset, sex)), PotentialRole.CONDITIONAL_PROBABILITY);
        TreeADDPotential onSex = treeOnSex(ill,
                new TablePotential(new ArrayList<>(List.of(ill)), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{1, 0}),
                new TablePotential(new ArrayList<>(List.of(ill)), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0, 1}));
        original.setTree(onset, new TreeADDPotential(new ArrayList<>(List.of(ill, onset, sex)), onset,
                PotentialRole.CONDITIONAL_PROBABILITY,
                new ArrayList<>(List.of(branch(onset, 0, onSex, List.of(onset, sex))))));

        assertThePastedAnswersLikeTheOriginal(1, original, new Finding(onset, 0), new Finding(sex, 1));
        assertThePastedAnswersLikeTheOriginal(0, original, new Finding(onset, 0), new Finding(sex, 0));
    }

    /** Death comes 8 after the onset for men and 3 for women. */
    @Test
    public void aTreeWithExcludedEvents() throws Exception {
        TreeWithExcludedEventsPotential original = new TreeWithExcludedEventsPotential(
                new ArrayList<>(List.of(death, onset, sex)), PotentialRole.CONDITIONAL_PROBABILITY);
        original.setNoEventTree(treeOnSex(death,
                new ExactDistrPotential(new ArrayList<>(List.of(death)), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{8}),
                new ExactDistrPotential(new ArrayList<>(List.of(death)), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{3})));

        assertThePastedAnswersLikeTheOriginal(3, original, new Finding(onset, 1), new Finding(sex, 1));
        assertThePastedAnswersLikeTheOriginal(8, original, new Finding(onset, 1), new Finding(sex, 0));
    }

    /** Replacing a variable in the copy must leave the original as it was. */
    @Test
    public void theOriginalKeepsItsOwnVariables() throws Exception {
        DistributionTablePotential original = new DistributionTablePotential(
                new ArrayList<>(List.of(death, onset, checkup, ill)), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        original.getTableWithEvents().setValues(new double[]{8, 3, 5, 1});

        paste(original);

        assertEquals(List.of(death, onset, checkup, ill), original.getVariables());
        assertEquals(1, original.sampleConditionedVariable(RANDOM, evidence(new Finding(checkup, 1), new Finding(ill, 1))));
    }
}
