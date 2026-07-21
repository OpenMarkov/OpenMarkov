/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.model.network.potential.canonical.TuningPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the canonical name (first element of {@code @PotentialType.names()}) emitted
 * by {@link PotentialUtils#getPotentialName(Class)} for each registered potential.
 * Acts as a guard-rail against accidental reordering of the {@code names} array.
 * See docs/rediseno-potenciales/nombres-canonicos.md for the authoritative table.
 */
class PotentialCanonicalNameTest {

    @Test void tablePotentialCanonicalIsProbTable() {
        assertThat(PotentialUtils.getPotentialName(TablePotential.class)).isEqualTo("ProbTable");
    }

    @Test void uniformPotentialCanonicalIsUniform() {
        assertThat(PotentialUtils.getPotentialName(UniformPotential.class)).isEqualTo("Uniform");
    }

    @Test void deltaPotentialCanonicalIsDelta() {
        assertThat(PotentialUtils.getPotentialName(DeltaPotential.class)).isEqualTo("Delta");
    }

    @Test void productPotentialCanonicalIsProduct() {
        assertThat(PotentialUtils.getPotentialName(ProductPotential.class)).isEqualTo("Product");
    }

    @Test void sumPotentialCanonicalIsSum() {
        assertThat(PotentialUtils.getPotentialName(SumPotential.class)).isEqualTo("Sum");
    }

    @Test void univariateDistrPotentialCanonicalIsUnivariateDistr() {
        assertThat(PotentialUtils.getPotentialName(UnivariateDistrPotential.class)).isEqualTo("UnivariateDistr");
    }

    @Test void exactDistrPotentialCanonicalIsExact() {
        assertThat(PotentialUtils.getPotentialName(ExactDistrPotential.class)).isEqualTo("Exact");
    }

    @Test void augmentedProbTablePotentialCanonicalIsAugmentedProbTable() {
        assertThat(PotentialUtils.getPotentialName(AugmentedProbTablePotential.class)).isEqualTo("AugmentedProbTable");
    }

    @Test void linearCombinationCanonicalIsLinearCombination() {
        assertThat(PotentialUtils.getPotentialName(LinearCombinationPotential.class)).isEqualTo("Linear combination");
    }

    @Test void treeADDCanonicalIsTreeSlashADD() {
        assertThat(PotentialUtils.getPotentialName(TreeADDPotential.class)).isEqualTo("Tree/ADD");
    }

    @Test void iciPotentialCanonicalIsICIModel() {
        assertThat(PotentialUtils.getPotentialName(ICIPotential.class)).isEqualTo("ICIModel");
    }

    @Test void maxPotentialCanonicalIsORMAX() {
        assertThat(PotentialUtils.getPotentialName(MaxPotential.class)).isEqualTo("OR / MAX");
    }

    @Test void minPotentialCanonicalIsANDMIN() {
        assertThat(PotentialUtils.getPotentialName(MinPotential.class)).isEqualTo("AND / MIN");
    }

    @Test void tuningPotentialCanonicalIsTuning() {
        assertThat(PotentialUtils.getPotentialName(TuningPotential.class)).isEqualTo("Tuning");
    }

    @Test void tableAliasStillResolvesToTablePotential() {
        assertThat(PotentialUtils.getClassByName("Table")).isEqualTo(TablePotential.class);
    }

    @Test void probTableCanonicalResolvesToTablePotential() {
        assertThat(PotentialUtils.getClassByName("ProbTable")).isEqualTo(TablePotential.class);
    }
}
