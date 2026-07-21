/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel.reader;

import org.jdom2.Element;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Manuel Arias
 */
public class PGMXReaderPotentialRoleTest {

    private final PGMXReader_0_2 reader = new PGMXReader_0_2();

    @Test public void potentialWithoutRoleAttributeRaisesAClearParseError() {
        Element xmlPotential = new Element(XMLTags.POTENTIAL.toString());
        // no role attribute set
        assertThrows(PGMXParserException.PotentialWithoutRole.class,
                () -> reader.getPotentialRole(xmlPotential));
    }

    @Test public void potentialWithRoleAttributeIsParsed() throws Exception {
        Element xmlPotential = new Element(XMLTags.POTENTIAL.toString());
        xmlPotential.setAttribute(XMLAttributes.ROLE.toString(),
                PotentialRole.CONDITIONAL_PROBABILITY.toString());
        assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, reader.getPotentialRole(xmlPotential));
    }
}
