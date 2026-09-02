/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TreeADDTableProjectTest {
    
    private Variable variableA;
    private Variable variableB;
    private Variable variableC;
    private State absent;
    private State present;
    private State mild;
    private State moderate;
    private State severe;
    private TreeADDPotential treeADD;
    
    @BeforeEach public void setUp() {
        // create variables
        variableA = new Variable("A", 4);
        variableB = new Variable("B", 2);
        variableC = new Variable("C", 2);
        
        // set variable states
        absent = new State("absent");
        present = new State("present");
        State[] states = {absent, present};
        
        mild = new State("mild");
        moderate = new State("moderate");
        severe = new State("severe");
        
        State[] statesA = {absent, mild, moderate, severe};
        
        variableC.setStates(states);
        variableB.setStates(states);
        variableA.setStates(statesA);
        
        List<Variable> variablesC = Arrays.asList(variableC);
        double[] tableBAbsent = {0.7, 0.3};
        TablePotential bAbsentPotential = new TablePotential(variablesC, PotentialRole.CONDITIONAL_PROBABILITY,
                                                             tableBAbsent);
        
        double[] tableBPresent = {0.8, 0.2};
        TablePotential bPresentPotential = new TablePotential(variablesC, PotentialRole.CONDITIONAL_PROBABILITY,
                                                              tableBPresent);
        
        double[] tableASevere = {0.6, 0.4};
        TablePotential aSeverePotential = new TablePotential(variablesC, PotentialRole.CONDITIONAL_PROBABILITY,
                                                             tableASevere);
        
        List<Variable> variablesCB = Arrays.asList(variableC, variableB);
        double[] tableAmoderate = {0.7, 0.3, 0.1, 0.9};
        TablePotential aModeratePotential = new TablePotential(variablesCB, PotentialRole.CONDITIONAL_PROBABILITY,
                                                               tableAmoderate);
        
        //Branches
        List<State> branchModerateStates = new ArrayList<>();
        branchModerateStates.add(moderate);
        List<Variable> parentVariables = Arrays.asList(variableC, variableA, variableB);
        TreeADDBranch branchModerate = new TreeADDBranch(branchModerateStates, variableA, aModeratePotential,
                                                         parentVariables);
        
        List<State> branchSevereStates = new ArrayList<>();
        branchSevereStates.add(severe);
        TreeADDBranch branchSevere = new TreeADDBranch(branchSevereStates, variableA, aSeverePotential,
                                                       parentVariables);
        
        //Subtree
        List<Variable> subVariables = Arrays.asList(variableC, variableB);
        TreeADDBranch branchAbsent = new TreeADDBranch(Arrays.asList(absent), variableB, bAbsentPotential,
                                                       subVariables);
        TreeADDBranch branchPresent = new TreeADDBranch(Arrays.asList(present), variableB, bPresentPotential,
                                                        subVariables);
        List<TreeADDBranch> subBranches = Arrays.asList(branchAbsent, branchPresent);
        TreeADDPotential aAbsentMildPotential = new TreeADDPotential(subVariables, variableB,
                                                                     PotentialRole.CONDITIONAL_PROBABILITY, subBranches);
        
        TreeADDBranch branchAbsentMild = new TreeADDBranch(Arrays.asList(absent, mild), variableA, aAbsentMildPotential,
                                                           parentVariables);
        
        List<TreeADDBranch> branches = new ArrayList<>();
        branches.add(branchAbsentMild);
        branches.add(branchModerate);
        branches.add(branchSevere);
        
        treeADD = new TreeADDPotential(parentVariables, variableA, PotentialRole.CONDITIONAL_PROBABILITY, branches);
    }
    
    /**
     * The variables of the projection come out in one order and only one. The expected table used
     * to be reordered to whatever order the projection had answered before the values were
     * compared, so the order was never checked — and it was not fixed: the branches were walked
     * through a {@code HashMap} keyed by the branches, which have no {@code hashCode} of their own,
     * so the walk followed the addresses in memory and changed from one run to the next.
     */
    @Test public void testTableProject() throws NumberFormatException, NonProjectablePotentialException {
        TablePotential tablePotential = treeADD.tableProject(null, null);

        Assertions.assertEquals(Arrays.asList(variableC, variableA, variableB), tablePotential.getVariables());
        Assertions.assertEquals(16, tablePotential.getValues().length);
        Assertions.assertArrayEquals(new double[]{0.7, 0.3, 0.7, 0.3, 0.7, 0.3, 0.6, 0.4, 0.8, 0.2, 0.8, 0.2, 0.1,
                0.9, 0.6, 0.4}, tablePotential.getValues(), 0.001);
    }

    /** The same tree, projected again, answers the same table: same order, same values. */
    @Test public void projectingTheSameTreeTwiceAnswersTheSameTable()
            throws NumberFormatException, NonProjectablePotentialException {
        TablePotential once = treeADD.tableProject(null, null);
        TablePotential again = treeADD.tableProject(null, null);

        Assertions.assertEquals(once.getVariables(), again.getVariables());
        Assertions.assertArrayEquals(once.getValues(), again.getValues(), 0.0);
    }

    /**
     * Two branches whose potentials rest on different variables, so that the union of the variables
     * of the result depends on the order the branches are walked in. Each tree is built afresh,
     * because the walk followed the addresses in memory and those belong to the objects, not to the
     * run: building the same tree twice was enough for the answer to change.
     */
    @Test public void theOrderOfTheVariablesIsTheSameEveryTime() throws NonProjectablePotentialException {
        Set<List<String>> orders = new HashSet<>();
        for (int attempt = 0; attempt < 200; attempt++) {
            orders.add(treeOverDifferentVariables().tableProject(null, null)
                                                   .getVariables()
                                                   .stream()
                                                   .map(Variable::getName)
                                                   .toList());
        }

        Assertions.assertEquals(Set.of(List.of("C", "A", "D", "E")), orders,
                "every projection must order the variables the same way");
    }

    /**
     * When several branches are additive, the criterion of the result is the one of the first of
     * them. It used to be the one of whichever additive branch the walk happened to reach last, so
     * the same tree could answer a cost one time and an effectiveness the next.
     */
    @Test public void theCriterionIsTheOneOfTheFirstAdditiveBranch() throws NonProjectablePotentialException {
        for (int attempt = 0; attempt < 200; attempt++) {
            TablePotential projected = treeWithTwoCriteria().tableProject(null, null);

            Assertions.assertEquals("cost", projected.getCriterion().getCriterionName());
        }
    }

    /** A tree whose two branches rest on different variables: C and D under one, C and E under the other. */
    private static TreeADDPotential treeOverDifferentVariables() {
        Variable top = binaryVariable("A");
        Variable conditioned = binaryVariable("C");
        Variable underFirst = binaryVariable("D");
        Variable underSecond = binaryVariable("E");
        List<Variable> variables = Arrays.asList(conditioned, underFirst, underSecond, top);

        TablePotential first = new TablePotential(Arrays.asList(conditioned, underFirst),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.7, 0.3, 0.6, 0.4});
        TablePotential second = new TablePotential(Arrays.asList(conditioned, underSecond),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.8, 0.2, 0.5, 0.5});
        return treeOf(top, variables, first, second);
    }

    /** A tree whose first branch is a cost and whose second one is an effectiveness. */
    private static TreeADDPotential treeWithTwoCriteria() {
        Variable top = binaryVariable("A");
        Variable conditioned = binaryVariable("C");
        List<Variable> variables = Arrays.asList(conditioned, top);

        TablePotential cost = new TablePotential(Arrays.asList(conditioned), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.7, 0.3});
        cost.setCriterion(new Criterion("cost"));
        TablePotential effectiveness = new TablePotential(Arrays.asList(conditioned),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.8, 0.2});
        effectiveness.setCriterion(new Criterion("effectiveness"));
        return treeOf(top, variables, cost, effectiveness);
    }

    /** A tree of two branches, one per state of its top variable. */
    private static TreeADDPotential treeOf(Variable top, List<Variable> variables, TablePotential first,
                                           TablePotential second) {
        State[] states = top.getStates();
        List<TreeADDBranch> branches = new ArrayList<>();
        branches.add(new TreeADDBranch(Arrays.asList(states[0]), top, first, variables));
        branches.add(new TreeADDBranch(Arrays.asList(states[1]), top, second, variables));
        return new TreeADDPotential(variables, top, PotentialRole.CONDITIONAL_PROBABILITY, branches);
    }

    private static Variable binaryVariable(String name) {
        Variable variable = new Variable(name, 2);
        variable.setStates(new State[]{new State("s0"), new State("s1")});
        return variable;
    }
}
