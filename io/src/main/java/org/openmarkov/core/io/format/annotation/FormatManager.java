/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.format.annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.plugin.PluginSearch;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class is the manager of the format annotations. Detects the plugins with FormatType
 * annotations.
 *
 * @author mpalacios
 * @author carmenyago -adapted the manager to different versions of ProbModelXML
 * @see FormatType
 */
public class FormatManager {
    
    private static final FormatManager INSTANCE = new FormatManager();
    
    private final List<? extends ProbNetReader> readerInstances;
    private final List<? extends ProbNetWriter> writerInstances;
    
    private final List<Class<? extends ProbNetReader>> readerClassesList;
    private final List<Class<? extends ProbNetWriter>> writerClassesList;
    
    /**
     * Gets a FormatManager instance
     */
    private FormatManager() {
        super();
        this.readerClassesList = FormatManager.findAllProbNetReaderPlugins().toList();
        this.writerClassesList = FormatManager.findAllProbNetWriterPlugins().toList();
        this.writerInstances = this.writerClassesList.stream().map(writerClass -> {
            try {
                return writerClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new UnreachableException("Cannot instantiate writer plugin: " + writerClass.getName(), e);
            }
        }).toList();
        this.readerInstances = this.readerClassesList.stream().map(readerClass -> {
            try {
                return readerClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new UnreachableException("Cannot instantiate reader plugin: " + readerClass.getName(), e);
            }
        }).toList();
    }
    
    public static FormatType info(Class<?> plugin) {
        return plugin.getAnnotation(FormatType.class);
    }
    
    public static FormatType info(ProbNetReader reader) {
        return info(reader.getClass());
    }
    
    public static FormatType info(ProbNetWriter writer) {
        return info(writer.getClass());
    }
    
    /**
     * Gets a FormatManager instance
     *
     * @return FormatManager instance
     */
    public static FormatManager getInstance() {
        return INSTANCE;
    }
    
    private static Stream<Class<? extends ProbNetReader>> findAllProbNetReaderPlugins() {
        return PluginSearch.init().annotatedWith(FormatType.class).extending(ProbNetReader.class).stream();
    }
    
    private static Stream<Class<? extends ProbNetWriter>> findAllProbNetWriterPlugins() {
        return PluginSearch.init().annotatedWith(FormatType.class).extending(ProbNetWriter.class).stream();
    }
    
    /**
     * Gets the plugin corresponding to the "Reader" role, given the URL of network
     *
     * @param url URL of the resource
     *
     * @return a ProbNetReader object
     */
    public ProbNetReader getProbNetReader(URL url) throws NoReaderForFileException, ParserException.BadlyStructuredFile {
        //checkVersion(url);
        String fileName = url.getFile();
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!fileExtension.equals("elv")) {
            checkStructure(url);
        }
        String fileVersion = getFileVersion(url, fileExtension);
        ProbNetReader reader = getProbNetReaderInstanceFor(fileExtension);
        if (reader == null) {
            throw new NoReaderForFileException(fileExtension, fileVersion, url);
        }
        return reader;
    }
    
    private static @NotNull String getFileVersion(URL url, String fileExtension) throws ParserException.BadlyStructuredFile {
        String fileVersion;
        if (fileExtension.equals("elv")) {
            fileVersion = "";
        } else {
            DocumentBuilder docBuilder;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new UnreachableException(e);
            }
            Document doc;
            try {
                doc = docBuilder.parse(url.openStream());
            } catch (SAXParseException e) {
                throw new ParserException.BadlyStructuredFile(url, e);
            } catch (SAXException e) {
                throw new UnreachableException("Unexpected SAX error reading " + url, e);
            } catch (IOException e) {
                throw new ParserException.BadlyStructuredFile(url, e);
            }
            fileVersion = doc.getDocumentElement().getAttribute("formatVersion");
            //Removing the last index of the version
            fileVersion = fileVersion.substring(0, fileVersion.lastIndexOf('.'));
        }
        return fileVersion;
    }
    
    /**
     * Gets the plugin corresponding to the "Reader" role, the extension and the version
     *
     * @param extension - the extension required
     *
     * @return a probNetReader object
     */
    public @Nullable ProbNetReader getProbNetReaderInstanceFor(String extension) {
        return this.readerInstances
                .stream()
                .filter(plugin -> Arrays.asList(FormatManager.info(plugin).extensions()).contains(extension))
                .findFirst().orElse(null);
    }
    
    
    public static Stream<Class<? extends ProbNetReader>> readersClasses() {
        return FormatManager.INSTANCE.readerClassesList.stream();
    }
    
    public static Stream<Class<? extends ProbNetWriter>> writersClasses() {
        return FormatManager.INSTANCE.writerClassesList.stream();
    }
    
    public static Stream<? extends ProbNetReader> readersInstances() {
        return FormatManager.INSTANCE.readerInstances.stream();
    }
    
    public static Stream<? extends ProbNetWriter> writersInstances() {
        return FormatManager.INSTANCE.writerInstances.stream();
    }
    
    public void checkVersion(InputStream input) throws SAXException, IOException, ParserConfigurationException {
        
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("version.xsd");
        
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(input);
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        Source schemaFile = new StreamSource(xsd);
        Schema schema = factory.newSchema(schemaFile);
        
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
    }
    
    public void checkVersion(URL url) throws SAXException, IOException {
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("version.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsd);
        Schema schema = factory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnreachableException(e);
        }
        Document document = db.parse(url.openStream());
        validator.validate(new DOMSource(document));
    }
    
    
    public void checkStructure(String name, InputStream inputStream) throws SAXException, IOException, ParserException.BadlyStructuredFile {
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("val_v4.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsd);
        Schema schema = factory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        try {
            validator.validate(new StreamSource(inputStream));
        } catch (SAXParseException e) {
            URL url = new File(name).toURI().toURL();
            throw new ParserException.BadlyStructuredFile(url, e);
        }
    }
    
    public void checkStructure(URL url) throws ParserException.BadlyStructuredFile {
        /*
        InputStream xsd = getClass().getClassLoader().getResourceAsStream("val_v4.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;
        try {
            schema = factory.newSchema(new StreamSource(xsd));
        } catch (SAXException e) {
            throw new UnreachableException("Cannot load XML schema val_v4.xsd", e);
        }
        try {
            schema.newValidator().validate(new StreamSource(url.openStream()));
        } catch (SAXException e) {
            throw new UnrecoverableException(new ParserException.CannotParseFile(e, url));
        } catch (IOException e) {
            throw new ParserException.BadlyStructuredFile(url, e);
        }
        */
    }
    
    public static boolean formatEquals(FormatType format1, FormatType format2) {
        return Arrays.equals(format1.extensions(), format2.extensions())
                && format1.description().equals(format2.description());
    }
}























