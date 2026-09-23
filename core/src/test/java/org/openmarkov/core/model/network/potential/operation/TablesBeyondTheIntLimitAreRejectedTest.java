/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An operation whose table would exceed 2^31 - 1 cells, or whose number of eliminated
 * configurations would, must throw InvalidArgumentException before reserving memory.
 *
 * @author Manuel Arias
 */
public class TablesBeyondTheIntLimitAreRejectedTest {

    private static List<Variable> binaries(String prefix, int howMany) {
        List<Variable> variables = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            variables.add(new Variable(prefix + i, 2));
        }
        return variables;
    }

    private static TablePotential ones(List<Variable> variables) {
        double[] values = new double[TablePotential.computeTableSize(variables)];
        Arrays.fill(values, 1.0);
        return new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    @Tag(TestSpeed.FAST)
    @Test public void multiplyingTo2To32Cells() {
        List<TablePotential> potentials = List.of(ones(binaries("A", 16)), ones(binaries("B", 16)));
        assertThrows(InvalidArgumentException.class, () -> DiscretePotentialOperations.multiply(potentials));
    }

    @Tag(TestSpeed.FAST)
    @Test public void multiplyingTo2To31Cells() {
        List<TablePotential> potentials = List.of(ones(binaries("A", 16)), ones(binaries("B", 15)));
        assertThrows(InvalidArgumentException.class, () -> DiscretePotentialOperations.multiply(potentials));
    }

    @Tag(TestSpeed.FAST)
    @Test public void summingTo2To31Cells() {
        List<TablePotential> potentials = List.of(ones(binaries("A", 16)), ones(binaries("B", 15)));
        assertThrows(InvalidArgumentException.class, () -> DiscretePotentialOperations.sum(potentials));
    }

    @Tag(TestSpeed.FAST)
    @Test public void multiplyingToFiveTimes2To30Cells() {
        List<Variable> second = binaries("B", 15);
        second.add(new Variable("F", 5));
        List<TablePotential> potentials = List.of(ones(binaries("A", 15)), ones(second));
        assertThrows(InvalidArgumentException.class, () -> DiscretePotentialOperations.multiply(potentials));
    }

    @Tag(TestSpeed.FAST)
    @Test public void mergingTo2To32Cells() {
        Variable decision = new Variable("D", 2);
        List<TablePotential> potentials = List.of(ones(binaries("A", 16)), ones(binaries("B", 15)));
        assertThrows(InvalidArgumentException.class, () -> DiscretePotentialOperations.merge(decision, potentials));
    }

    @Tag(TestSpeed.FAST)
    @Test public void eliminating31Variables() {
        List<Variable> first = binaries("A", 16);
        List<TablePotential> potentials = List.of(ones(first), ones(binaries("B", 16)));
        assertThrows(InvalidArgumentException.class,
                () -> DiscretePotentialOperations.multiplyAndMarginalize(potentials, List.of(first.getFirst())));
    }

    @Tag(TestSpeed.FAST)
    @Test public void eliminating32Variables() {
        List<Variable> first = binaries("A", 16);
        List<TablePotential> potentials = List.of(ones(first), ones(binaries("B", 17)));
        assertThrows(InvalidArgumentException.class,
                () -> DiscretePotentialOperations.multiplyAndMarginalize(potentials, List.of(first.getFirst())));
    }

    @Tag(TestSpeed.FAST)
    @Test public void offsetsBeyondTheLimit() {
        int[] dimensions = new int[32];
        Arrays.fill(dimensions, 2);
        assertThrows(InvalidArgumentException.class, () -> TablePotential.calculateOffsets(dimensions));
    }
}
