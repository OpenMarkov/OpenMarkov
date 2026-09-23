/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.AbsorbParentsEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Absorbing the parents of a node defined by a formula gives each operand the value of the parent
 * with its name, whatever the names and the order of the links.
 *
 * @author Manuel Arias
 */
public class AFormulaTakesEachParentByItsNameTest {

    private static final String NETWORK = "/networks/dan/DAN-two-utility-and-chance-function-sv.pgmx";
    private static final String LINK_U1 = "<Link directed=\"true\">\n        <Variable name=\"U1\" />\n        <Variable name=\"U0\" />\n      </Link>";
    private static final String LINK_U2 = "<Link directed=\"true\">\n        <Variable name=\"U2\" />\n        <Variable name=\"U0\" />\n      </Link>";

    private ProbNet read(UnaryOperator<String> change) throws Exception {
        String text;
        try (InputStream in = getClass().getResourceAsStream(NETWORK)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(text.contains(LINK_U1 + "\n      " + LINK_U2), "the network changed");
        Path file = Files.createTempFile("formula", ".pgmx");
        Files.writeString(file, change.apply(text));
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    private static double absorbWithTheMenu(ProbNet net) throws Exception {
        new AbsorbParentsEdit(net, net.getNode("U0")).executeEdit();
        return valueOf(net.getNode("U0"));
    }

    private static double absorbBeforeInference(ProbNet net) throws Exception {
        BasicOperations.absorbParents(net, net.getNode("U0"), null);
        return valueOf(net.getNode("U0"));
    }

    private static double valueOf(Node node) {
        return ((ExactDistrPotential) node.getPotential()).getTablePotential().getValues()[0];
    }

    private static String renamed(String text) {
        return text.replace("\"U1\"", "\"Coste\"").replace("\"U2\"", "\"Beneficio\"")
                   .replace("abs({U1})*{U2}", "abs({Coste})*{Beneficio}").replace("<Values>-3.0</Values>", "<Values>5.0</Values>");
    }

    /** U1 = 5 and U2 = -2, with the link from U2 first. */
    private static String swapped(String text) {
        return text.replace(LINK_U1 + "\n      " + LINK_U2, LINK_U2 + "\n      " + LINK_U1)
                   .replace("<Values>-2.0</Values>", "<Values>5.0</Values>")
                   .replace("<Values>-3.0</Values>", "<Values>-2.0</Values>");
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void parentsWithOtherNames() throws Exception {
        assertEquals(10, absorbWithTheMenu(read(AFormulaTakesEachParentByItsNameTest::renamed)), 1E-12);
        assertEquals(10, absorbBeforeInference(read(AFormulaTakesEachParentByItsNameTest::renamed)), 1E-12);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void parentsLinkedInAnotherOrder() throws Exception {
        assertEquals(-10, absorbWithTheMenu(read(AFormulaTakesEachParentByItsNameTest::swapped)), 1E-12);
        assertEquals(-10, absorbBeforeInference(read(AFormulaTakesEachParentByItsNameTest::swapped)), 1E-12);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aVariableThatIsNotAParentIsNamed() throws Exception {
        ProbNet net = read(text -> text.replace("abs({U1})*{U2}", "abs({U1})*{Missing}"));
        UnrecoverableException e = assertThrows(UnrecoverableException.class,
                () -> BasicOperations.absorbParents(net, net.getNode("U0"), null));
        assertInstanceOf(NonProjectablePotentialException.class, e.getCause());
        assertTrue(e.getCause().toString().contains("Missing"), e.getCause().toString());
    }
}
