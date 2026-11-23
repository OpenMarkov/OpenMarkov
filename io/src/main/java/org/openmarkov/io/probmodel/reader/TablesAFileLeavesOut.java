/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel.reader;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.openmarkov.core.io.format.annotation.XMLParsers;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The tables that a ProbModelXML file leaves out and the reader fills in: a table without numbers and, in a canonical
 * model, the table of a parent or of the leak.
 *
 * @author Manuel Arias
 */
public final class TablesAFileLeavesOut {

    private TablesAFileLeavesOut() {
    }

    /** @return one line for each table left out, with its node and how it is opened; empty for another format */
    public static List<String> in(URL url) throws IOException {
        Element root;
        try (InputStream file = url.openStream()) {
            root = XMLParsers.saxBuilder().build(file).getRootElement();
        } catch (JDOMException notXML) {
            return List.of();
        }
        List<String> leftOut = new ArrayList<>();
        if (root.getName().equals("ProbModelXML")) {
            for (Element xmlPotential : root.getDescendants(Filters.element(XMLTags.POTENTIAL.toString()))) {
                String type = xmlPotential.getAttributeValue(XMLAttributes.TYPE.toString());
                if (PotentialUtils.getNames(ICIPotential.class).contains(type)) {
                    addTablesOfACanonicalModel(xmlPotential, leftOut);
                } else if (xmlPotential.getChild(XMLTags.VALUES.toString()) == null) {
                    if (isAUtilityTable(xmlPotential, type)) {
                        leftOut.add(PGMXReader_0_2.nodeOfTheRelation(xmlPotential)
                                            + ": its table has no numbers. It is opened with zeros.");
                    } else if (PotentialUtils.getNames(TablePotential.class).contains(type)) {
                        leftOut.add(PGMXReader_0_2.nodeOfTheRelation(xmlPotential)
                                            + ": its table has no numbers. It is opened as uniform.");
                    }
                }
            }
        }
        return leftOut;
    }

    /** @return whether the reader takes {@code xmlPotential} for a table of utilities */
    static boolean isAUtilityTable(Element xmlPotential, String type) {
        return PotentialUtils.getNames(ExactDistrPotential.class).contains(type)
                || PotentialUtils.getNames(TablePotential.class).contains(type)
                && xmlPotential.getChild(XMLTags.UTILITY_VARIABLE.toString()) != null;
    }

    private static void addTablesOfACanonicalModel(Element xmlPotential, List<String> leftOut) {
        Element xmlVariables = xmlPotential.getChild(XMLTags.VARIABLES.toString());
        if (xmlVariables == null || xmlVariables.getChildren().isEmpty()) {
            return;
        }
        List<String> parents = new ArrayList<>(xmlVariables.getChildren().stream().map(PGMXReader_0_2::nodeNameOf).toList());
        String node = parents.removeFirst();
        boolean leak = false;
        Element xmlSubpotentials = xmlPotential.getChild(XMLTags.SUBPOTENTIALS.toString());
        for (Element subpotential : xmlSubpotentials == null ? List.<Element>of() : xmlSubpotentials.getChildren()) {
            Element variables = subpotential.getChild(XMLTags.VARIABLES.toString());
            if (variables == null || variables.getChildren().isEmpty()) {
                continue;
            }
            if (variables.getChildren().size() > 1) {
                parents.remove(PGMXReader_0_2.nodeNameOf(variables.getChildren().get(1)));
            } else {
                leak = true;
            }
        }
        for (String parent : parents) {
            leftOut.add(node + ": its canonical model has no table for the parent " + parent
                                + ". It is opened with the table by default, in which " + parent + " alone determines "
                                + node + ".");
        }
        if (!leak) {
            leftOut.add(node + ": its canonical model has no table for the leak. It is opened with the leak by default.");
        }
    }
}
