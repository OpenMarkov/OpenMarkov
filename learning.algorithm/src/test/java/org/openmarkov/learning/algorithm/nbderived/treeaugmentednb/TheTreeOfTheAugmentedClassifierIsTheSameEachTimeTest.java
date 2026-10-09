/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.nbderived.treeaugmentednb;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.cmi.mutualInformation.MutualInformationMetric;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The links between the features that the tree augmented naive Bayes classifier ends with.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTreeOfTheAugmentedClassifierIsTheSameEachTimeTest {

    /** A metric for which every pair of features weighs the same. */
    private static class SameWeightForEveryPair extends Metric {
        @Override public double getScore(PNEdit edit) {
            return 1;
        }

        @Override public double score(TablePotential nodePotential) {
            return 0;
        }
    }

    private static TreeSet<String> linksBetweenFeatures(TreeAugmentedNBAlgorithm algorithm) {
        TreeSet<String> links = new TreeSet<>();
        algorithm.mwst.getDirectedEdges().forEach(
                link -> links.add(link.getVariableFrom().getName() + "->" + link.getVariableTo().getName()));
        return links;
    }

    /** The class is the second variable, so the first feature is F1. */
    private static TreeSet<String> treeWhenEveryPairWeighsTheSame() {
        ProbNet probNet = new ProbNet();
        for (String name : List.of("F1", "Class", "F2", "F3", "F4")) {
            probNet.addNode(new Variable(name, 2), NodeType.CHANCE);
        }
        TreeAugmentedNBAlgorithm algorithm = new TreeAugmentedNBAlgorithm(probNet, null, new SameWeightForEveryPair(),
                                                                         1.0);
        algorithm.setClassVariableName("Class");
        algorithm.init(null);
        return linksBetweenFeatures(algorithm);
    }

    @Test void pairsWithTheSameWeightAreTakenInTheOrderOfTheFeatures() {
        for (int time = 0; time < 50; time++) {
            assertEquals(new TreeSet<>(List.of("F1->F2", "F1->F3", "F1->F4")), treeWhenEveryPairWeighsTheSame());
        }
    }

    /** The cases of the Asia network, with «Bronchitis» as the class. Its first column is «X-ray». */
    private static TreeSet<String> treeLearnedFromAsia() throws Exception {
        List<String[]> rows = new ArrayList<>();
        String[] names;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                TheTreeOfTheAugmentedClassifierIsTheSameEachTimeTest.class.getResourceAsStream(
                        "/network/BN-asia10k.csv"), StandardCharsets.UTF_8))) {
            names = reader.readLine().trim().split(",");
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (!line.isBlank()) {
                    rows.add(line.trim().split(","));
                }
            }
        }
        List<Map<String, Integer>> states = new ArrayList<>();
        for (int column = 0; column < names.length; column++) {
            states.add(new LinkedHashMap<>());
        }
        int[][] cases = new int[rows.size()][names.length];
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < names.length; column++) {
                Map<String, Integer> ofTheColumn = states.get(column);
                ofTheColumn.putIfAbsent(rows.get(row)[column].trim(), ofTheColumn.size());
                cases[row][column] = ofTheColumn.get(rows.get(row)[column].trim());
            }
        }
        List<Variable> variables = new ArrayList<>();
        ProbNet probNet = new ProbNet();
        for (int column = 0; column < names.length; column++) {
            variables.add(new Variable(names[column].trim(), states.get(column).size()));
            probNet.addNode(variables.get(column), NodeType.CHANCE);
        }
        CaseDatabase caseDatabase = new CaseDatabase(variables, cases);
        Metric metric = new MutualInformationMetric();
        metric.init(probNet, caseDatabase);
        TreeAugmentedNBAlgorithm algorithm = new TreeAugmentedNBAlgorithm(probNet, caseDatabase, metric, 1.0);
        algorithm.setClassVariableName("Bronchitis");
        algorithm.init(null);
        return linksBetweenFeatures(algorithm);
    }

    @Test void theSameCasesGiveTheSameTree() throws Exception {
        TreeSet<String> first = treeLearnedFromAsia();

        assertEquals(first, treeLearnedFromAsia());
        assertEquals(first, treeLearnedFromAsia());
    }

    @Test void noLinkOfTheTreeEntersTheFirstFeature() throws Exception {
        assertEquals(0, treeLearnedFromAsia().stream().filter(link -> link.endsWith("->X-ray")).count());
    }
}
