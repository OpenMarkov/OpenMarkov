/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A formula keeps the variables it is written on. Pasting a node replaces the variables of its
 * relation by those of the pasted nodes, but the formulas kept the original ones: pasted in the same
 * network, where the pasted parent gets a new name, the node could not be computed; pasted in another
 * network it worked only because the name was the same (error 234 of the pending list).
 *
 * <p>Each relation is pasted both ways - with a new name and with the same one - and has to answer
 * what the original answers.
 *
 * @author Manuel Arias
 */
public class FormulasFollowAReplacedVariableTest {

    private static final double[] RANDOM = {0.5};

    private Variable value;
    private Variable age;
    private Variable sex;
    private Variable death;
    /** The variable of each pasted node, by the variable of the node it was copied from. */
    private Map<Variable, Variable> pasted;

    @BeforeEach
    public void setUp() {
        value = new Variable("Value");
        age = new Variable("Age");
        sex = new Variable("Sex", new State[]{new State("male"), new State("female")});
        death = new Variable("Death", new State[]{new State("no"), new State("yes")});
        death.setVariableType(VariableType.EVENT);
    }

    private void pasteWith(String suffix) {
        pasted = new LinkedHashMap<>();
        for (Variable variable : List.of(value, age, sex, death)) {
            Variable copy = new Variable(variable);
            copy.setName(variable.getName() + suffix);
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

    /** A woman of 40, on the given variables. */
    private static EvidenceCase aWomanOf40(Variable sex, Variable age) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(sex, 1));
        evidence.addFinding(new Finding(age, 40.0));
        return evidence;
    }

    private interface Answer {
        double[] of(Potential potential, Variable sex, Variable age) throws Exception;
    }

    /** The copy pasted with a new name and the one pasted with the same name answer like the original. */
    private void assertThePastedAnswerLikeTheOriginal(Potential original, Answer answer, double... expected)
            throws Exception {
        assertArrayEquals(expected, answer.of(original, sex, age), 1e-9, "the original");
        for (String suffix : List.of("'", "")) {
            pasteWith(suffix);
            assertArrayEquals(expected, answer.of(paste(original), pasted.get(sex), pasted.get(age)), 1e-9,
                    "the copy pasted as Age" + suffix);
        }
        assertArrayEquals(expected, answer.of(original, sex, age), 1e-9, "the original, after pasting it");
    }

    private static final Answer SAMPLE = (potential, sex, age) -> new double[]{
            ((DESSimulablePotential) potential).sampleConditionedVariable(RANDOM, aWomanOf40(sex, age))};

    private static final Answer TABLE = (potential, sex, age) ->
            potential.tableProject(aWomanOf40(sex, age), null, new ArrayList<>()).getValues();

    @Test
    public void anExpressionIsWrittenOnTheNewVariable() {
        pasteWith("'");
        VariableExpression expression = new VariableExpression(List.of(age, value), "{Age}*3+{Value}");

        VariableExpression replaced = expression.replacing(age, pasted.get(age));

        assertEquals("{Age'}*3+{Value}", replaced.asStringExpression());
        assertSame(pasted.get(age), replaced.references().get(0));
        assertSame(value, replaced.references().get(1));
        assertSame(expression, expression.replacing(sex, pasted.get(sex)), "it does not mention the sex");
    }

    @Test
    public void aFormula() throws Exception {
        List<Variable> variables = new ArrayList<>(List.of(value, sex, age));
        FunctionPotential original = new FunctionPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                new VariableExpression(variables, "{Age}*3"));

        assertThePastedAnswerLikeTheOriginal(original, SAMPLE, 120);
    }

    @Test
    public void aLinearCombination() throws Exception {
        List<Variable> variables = new ArrayList<>(List.of(value, age));
        LinearCombinationPotential original = new LinearCombinationPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                new VariableExpression[]{VariableExpression.Common.CONSTANT, new VariableExpression(variables, "{Age}")},
                new double[]{10, 2});

        assertThePastedAnswerLikeTheOriginal(original, TABLE, 90);
    }

    @Test
    public void aTimeToEventGivenByFormulas() throws Exception {
        DistributionTablePotential original = new DistributionTablePotential(
                new ArrayList<>(List.of(death, sex, age)), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        String[] functions = original.getTableWithEvents().getTableWithFunctions().getFunctionValues();
        functions[0] = "{Age}*2";
        functions[1] = "Age*3";

        assertThePastedAnswerLikeTheOriginal(original, SAMPLE, 120);
    }

    /** These two cannot be computed here without a network, so what is checked is what they keep inside. */
    private void assertWrittenOnThePasted(AugmentedProbTable table, List<Variable> expectedVariables, String... formulas) {
        assertEquals(expectedVariables, table.getVariables(), "the table inside must be on the variables of the pasted nodes");
        for (int i = 0; i < formulas.length; i++) {
            VariableExpression function = table.getFunctionValues()[i];
            assertEquals(formulas[i], function.asStringExpression());
            for (Variable reference : function.references()) {
                assertSame(pasted.get(age), reference, "the formula must be written on the pasted parent");
            }
        }
    }

    @Test
    public void aTableOfFormulas() {
        Variable smoker = new Variable("Smoker", new State[]{new State("no"), new State("yes")});
        List<Variable> variables = new ArrayList<>(List.of(smoker, sex, age));
        AugmentedProbTablePotential original = new AugmentedProbTablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        VariableExpression[] cells = original.getAugmentedProbTable().getFunctionValues();
        cells[2] = new VariableExpression(variables, "1-{Age}/100");
        cells[3] = new VariableExpression(variables, "{Age}/100");

        for (String suffix : List.of("'", "")) {
            pasteWith(suffix);
            pasted.put(smoker, new Variable(smoker));
            AugmentedProbTablePotential copy = (AugmentedProbTablePotential) paste(original);

            assertWrittenOnThePasted(copy.getAugmentedProbTable(), List.of(pasted.get(smoker), pasted.get(sex)),
                    "0.5", "0.5", "1-{Age" + suffix + "}/100", "{Age" + suffix + "}/100");
            assertEquals(List.of(pasted.get(smoker), pasted.get(sex)), copy.getFiniteStatesVariables());
            assertEquals(List.of(pasted.get(age)), copy.getParameterVariables());
        }
        assertEquals("{Age}/100", cells[3].asStringExpression(), "the original keeps its own");
        assertSame(age, original.getAugmentedProbTable().getFunctionValues()[3].references().get(0));
    }

    @Test
    public void aDistributionWhoseParametersAreFormulas() {
        List<Variable> variables = new ArrayList<>(List.of(value, sex, age));
        UnivariateDistrPotential original = new UnivariateDistrPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        original.setParameter(0, new VariableExpression(variables, "{Age}*2"));
        original.setParameter(1, new VariableExpression(variables, "{Age}*3"));

        for (String suffix : List.of("'", "")) {
            pasteWith(suffix);
            UnivariateDistrPotential copy = (UnivariateDistrPotential) paste(original);

            assertWrittenOnThePasted(copy.getDistributionTable(),
                    List.of(copy.getPseudoVariableDistribution(), pasted.get(sex)),
                    "{Age" + suffix + "}*2", "{Age" + suffix + "}*3");
            assertEquals(List.of(pasted.get(sex)), copy.getFiniteStatesVariables());
            assertEquals(List.of(pasted.get(age)), copy.getParameterVariables());
        }
        assertSame(age, original.getDistributionTable().getFunctionValues()[1].references().get(0), "the original keeps its own");
    }

    /** An exact value of 100 for men and 2000 for women is projected as a table: on the pasted nodes too. */
    @Test
    public void aDistributionWhoseParametersAreNumbers() throws Exception {
        UnivariateDistrPotential original = new UnivariateDistrPotential(
                new ArrayList<>(List.of(value, sex)), PotentialRole.CONDITIONAL_PROBABILITY);
        original.setParameters(new double[]{100, 2000});

        for (String suffix : List.of("'", "")) {
            pasteWith(suffix);
            Potential copy = paste(original);
            EvidenceCase aWoman = new EvidenceCase();
            aWoman.addFinding(new Finding(pasted.get(sex), 1));

            assertArrayEquals(new double[]{2000}, copy.tableProject(aWoman, null, new ArrayList<>()).getValues(), 1e-9,
                    "the copy pasted as Sex" + suffix);
        }
    }

    @Test
    public void aPiecewiseExponentialThatStartsAtTheAge() throws Exception {
        List<Variable> variables = new ArrayList<>(List.of(death, age));
        TreeMap<Double, Double> pieces = new TreeMap<>(Map.of(0.0, 0.01, 50.0, 0.1));
        PiecewiseExponentialPotential original = new PiecewiseExponentialPotential(variables,
                PotentialRole.CONDITIONAL_PROBABILITY, pieces,
                new FunctionPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, new VariableExpression(variables, "{Age}")),
                false);
        double expected = original.sampleConditionedVariable(RANDOM, aWomanOf40(sex, age));

        assertThePastedAnswerLikeTheOriginal(original, SAMPLE, expected);
    }
}
