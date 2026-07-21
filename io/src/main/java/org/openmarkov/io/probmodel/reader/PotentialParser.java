/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import org.jdom2.Element;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.util.List;

/**
 * Strategy for reading a single potential type from a PGMX XML element.
 * <p>
 * Implementations are registered in {@link PGMXReader_0_2#buildPotentialParsers()} and
 * dispatched by {@link PGMXReader_0_2#autoGetPotential}.
 *
 * @author Manuel Arias
 * @see PGMXPotentialParsers
 */
@FunctionalInterface
public interface PotentialParser {
    /**
     * Parses a potential from its XML element.
     *
     * @param xml      the {@code <Potential>} XML element
     * @param net      the network being built (used to look up variables and nodes)
     * @param role     the potential role declared in the XML attribute
     * @param vars     the variables referenced by this potential, in order
     * @return the parsed {@link Potential}
     * @throws PGMXParserException if the element is malformed or missing required data
     */
    Potential parse(Element xml, ProbNet net, PotentialRole role, List<Variable> vars)
            throws PGMXParserException;
}
