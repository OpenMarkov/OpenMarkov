/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openmarkov.core.exception.UnreachableException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <b>{@code XMLProperties}</b> extends Java's
 * {@code java.util.Properties} class, and provides
 * behavior similar to properties but that use XML as the
 * input and output format.
 */
@SuppressWarnings("serial") class XMLProperties extends Properties {

	/**
     * <p> This overrides the default {@code load()}
	 * behavior to read from an XML document. </p>
	 * @param reader the reader to read XML from
	 * @throws IOException	when errors occur reading.
	 */
	@Override public void load(Reader reader) throws IOException {
		try {
			// Load XML into JDOM Document
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(reader);
			// Turn into properties objects
            loadFromElements(doc.getRootElement().getChildren(), new StringBuilder());
		} catch (JDOMException e) {
			throw new IOException(e);
		}
	}

	/**
     * <p> This overrides the default {@code load()}
	 * behavior to read from an XML document. </p>
	 *
	 * @param inputStream the input stream
	 * @throws IOException	when errors occur reading.
	 */
	@Override public void load(InputStream inputStream) throws IOException {
		load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
	}

	/**
     * <p> This overrides the default {@code load()}
	 * behavior to read from an XML document. </p>
	 *
	 * @param xmlDocument the XML document to read
	 * @throws IOException	when errors occur reading.
	 */
	public void load(File xmlDocument) throws IOException {

		load(new FileReader(xmlDocument));
	}

	/**
	 * <p>This helper method loads the XML properties from a specific
	 * XML element, or set of elements.</p>
	 *
     * @param elements {@code List} of elements to load from.
	 * @param baseName the base name of this property.
	 */
	private void loadFromElements(List<Element> elements, StringBuilder baseName) {
		// Iterate through each element
		for (Element current : elements) {
			String name = current.getName();
			//String text = current.getTextTrim();
			String text = current.getAttributeValue("value");

			// Don't add "." if no baseName
            if (!baseName.isEmpty()) {
				baseName.append(".");
			}
			baseName.append(name);

			// See if we have an element value
            if ((text != null) && (!text.isEmpty())) {
				// If text, this is a property
				setProperty(baseName.toString(), text);
			}
			// Look for in the children
			List<Element> children = current.getChildren();
			if (children != null) {
				loadFromElements(children, baseName);
			}

			// On unwind from recursion, remove last name
			if (baseName.length() == name.length()) {
				baseName.setLength(0);
			} else {
				baseName.setLength(baseName.length() - (name.length() + 1));
			}
		}
	}

	/**
	 * @deprecated This method does not throw an IOException
	 * if an I/O error occurs while saving the property list.
	 * As of the Java 2 platform v1.2, the preferred way to save
	 * a properties list is via the
	 * method.
	 */
    @Deprecated @Override public void save(OutputStream out, String header) {
		try {
			store(out, header);
        } catch (IOException e) {
            throw new UnreachableException(e);
		}
	}

	/**
	 * <p> This will output the properties in this object
	 * as XML to the supplied output writer. </p>
	 *
	 * @param writer the writer to output XML to.
	 * @param header comment to add at top of file
	 * @throws IOException	when writing errors occur.
	 */
	@Override public void store(Writer writer, String header) throws IOException {

		// Create a new JDOM Document with a root element "properties"
		Element root = new Element("properties");
		Document doc = new Document(root);

		// Add in header information
		Comment comment = new Comment(header);
		doc.getContent().add(0, comment);

		// Get the property names
		Enumeration<?> propertyNames = propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = propertyNames.nextElement().toString();
			String propertyValue = getProperty(propertyName);
			createXMLRepresentation(root, propertyName, propertyValue);
		}

		// Output document to supplied filename
		//XMLOutputter outputter = new XMLOutputter("  ", true);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, writer);
		writer.flush();
	}

	/**
	 * <p> This will output the properties in this object
	 * as XML to the supplied output stream. </p>
	 *
	 * @param out    the output stream.
	 * @param header comment to add at top of file
	 * @throws IOException	when writing errors occur.
	 */
	@Override public void store(OutputStream out, String header) throws IOException {

		store(new OutputStreamWriter(out), header);
	}

	/**
	 * <p> This will output the properties in this object
	 * as XML to the supplied output file. </p>
	 *
	 * @param xmlDocument XML file to output to.
	 * @param header      comment to add at top of file
	 * @throws IOException	when writing errors occur.
	 */
	public void store(File xmlDocument, String header) throws IOException {

		store(new FileWriter(xmlDocument), header);
	}

	/**
	 * <p> This will convert a single property and its value to
	 * an XML element and textual value. </p>
	 *
     * @param root          JDOM root {@code Element} to add children to.
	 * @param propertyName  name to base element creation on.
	 * @param propertyValue value to use for property.
	 */
    private static void createXMLRepresentation(Element root, String propertyName, String propertyValue) {

		int split;
		String name = propertyName;
		Element current = root;
        Element test;
        
        while ((split = name.indexOf('.')) != -1) {
			String subName = name.substring(0, split);
			name = name.substring(split + 1);

			// Check for existing element
			if ((test = current.getChild(subName)) == null) {
				Element subElement = new Element(subName);
				current.addContent(subElement);
				current = subElement;
			} else {
				current = test;
			}
		}

		// When out of loop, what's left is the final element's name
		Element last = new Element(name);
		last.setText(propertyValue);
		/** Uncomment this for Attribute usage */
        /*
        last.setAttribute("value", propertyValue);
        */
		current.addContent(last);
	}
}
