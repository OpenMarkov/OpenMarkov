/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard-rail tests for {@link PotentialRole}. One test per active enum value so that
 * removing any of them (or renaming) triggers an explicit failure in this file.
 */
class PotentialRoleTest {

    @Test void conditionalProbabilityIsDeclared() {
        assertThat(PotentialRole.valueOf("CONDITIONAL_PROBABILITY")).isNotNull();
    }

    @Test void jointProbabilityIsDeclared() {
        assertThat(PotentialRole.valueOf("JOINT_PROBABILITY")).isNotNull();
    }

    @Test void policyIsDeclared() {
        assertThat(PotentialRole.valueOf("POLICY")).isNotNull();
    }

    @Test void linkRestrictionIsDeclared() {
        assertThat(PotentialRole.valueOf("LINK_RESTRICTION")).isNotNull();
    }

    @Test void unspecifiedIsDeclared() {
        assertThat(PotentialRole.valueOf("UNSPECIFIED")).isNotNull();
    }

    @Test void onlyFiveActiveValues() {
        assertThat(PotentialRole.values()).hasSize(5);
    }
}
