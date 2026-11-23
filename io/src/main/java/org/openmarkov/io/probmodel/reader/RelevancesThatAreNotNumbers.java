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
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The nodes to which a ProbModelXML file gives a relevance that is not a number. The reader opens them with the
 * relevance by default.
 *
 * @author Manuel Arias
 */
public final class RelevancesThatAreNotNumbers {

    private RelevancesThatAreNotNumbers() {
    }

    /** @return one line for each such node, with the relevance the file gives it; empty for another format */
    public static List<String> in(URL url) throws IOException {
        Element root;
        try (InputStream file = url.openStream()) {
            root = XMLParsers.saxBuilder().build(file).getRootElement();
        } catch (JDOMException notXML) {
            return List.of();
        }
        List<String> found = new ArrayList<>();
        if (root.getName().equals("ProbModelXML")) {
            for (Element xmlVariable : root.getDescendants(Filters.element(XMLTags.VARIABLE.toString()))) {
                String relevance = relevanceOf(xmlVariable);
                if (relevance != null && !isANumber(relevance)) {
                    found.add(PGMXReader_0_2.nodeNameOf(xmlVariable) + ": " + relevance);
                }
            }
        }
        return found;
    }

    /** @return the relevance that {@code xmlVariable} gives among its properties, or null if it gives none */
    private static String relevanceOf(Element xmlVariable) {
        Element xmlProperties = xmlVariable.getChild(XMLTags.ADDITIONAL_PROPERTIES.toString());
        if (xmlProperties == null) {
            xmlProperties = xmlVariable.getChild(XMLTags.PROPERTIES.toString());
        }
        if (xmlProperties == null) {
            return null;
        }
        String relevance = null;
        for (Element xmlProperty : xmlProperties.getChildren()) {
            if (XMLTags.RELEVANCE.toString().equals(xmlProperty.getAttributeValue(XMLAttributes.NAME.toString()))) {
                relevance = xmlProperty.getAttributeValue(XMLAttributes.VALUE.toString());
            }
        }
        return relevance;
    }

    static boolean isANumber(String written) {
        try {
            Double.parseDouble(written);
            return true;
        } catch (NumberFormatException notANumber) {
            return false;
        }
    }
}
