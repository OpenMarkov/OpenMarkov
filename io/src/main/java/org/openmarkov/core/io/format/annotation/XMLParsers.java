/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.format.annotation;

import org.jdom2.input.SAXBuilder;
import org.openmarkov.core.exception.UnreachableException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

/**
 * Parsers for the XML of a network file, which cannot make OpenMarkov read other files or connect to the addresses
 * that the file names.
 *
 * @author Manuel Arias
 */
public final class XMLParsers {

    private XMLParsers() {
    }

    /** Refuses to load the DTDs and the entities that the file names outside itself. */
    public static DocumentBuilder documentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnreachableException(e);
        }
    }

    /** Refuses to load the DTDs and the entities that the file names outside itself. */
    public static SAXBuilder saxBuilder() {
        SAXBuilder builder = new SAXBuilder();
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return builder;
    }

    /** Refuses to load the DTDs and the entities that the file names outside itself. */
    public static Validator validator(Schema schema) {
        Validator validator = schema.newValidator();
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new UnreachableException(e);
        }
        return validator;
    }
}
