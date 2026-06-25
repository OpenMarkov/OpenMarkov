/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.java.enumUtils.EnumUtils;

/**
 * Enumerates the roles a potential can play in a probabilistic graphical model.
 *
 * <ul>
 *   <li>{@link #CONDITIONAL_PROBABILITY} — conditional probability distribution {@code P(X | parents)}.</li>
 *   <li>{@link #JOINT_PROBABILITY} — joint distribution over several variables.</li>
 *   <li>{@link #POLICY} — decision policy (optimal action table).</li>
 *   <li>{@link #LINK_RESTRICTION} — constraint attached to a link.</li>
 *   <li>{@link #UNSPECIFIED} — role not fixed by this enum. Currently used as the internal
 *       marker for utility potentials: the PGMX readers map {@code role="utility"} to
 *       {@code UNSPECIFIED}, and {@code PGMXWriter_0_2} writes {@code UNSPECIFIED} as
 *       {@code role="utility"}. See {@code org.openmarkov.io.probmodel.reader.PGMXReader_0_2}.</li>
 * </ul>
 *
 * @author Manuel Arias
 * @version 1.0
 */
public enum PotentialRole implements Localizable {

    CONDITIONAL_PROBABILITY,
    JOINT_PROBABILITY,
    POLICY,
    LINK_RESTRICTION,
    UNSPECIFIED
	;
    
    public String toString() {
        return EnumUtils.toCamelCase(this);
	}
    
    
    @Override public @NotNull String path() {
        return "";
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        return this.toString();
    }
}
