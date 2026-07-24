/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * <b>{@code XMLProperties}</b> extends Java's
 * {@code java.util.Properties} class, and provides
 * behavior similar to properties but that use XML as the
 * input format.
 * <p>
 * <strong>Reading only.</strong> The class used to carry a whole XML writer as well —
 * {@code store}, {@code save}, {@code createXMLRepresentation} — which nothing in the repository ever
 * called and which wrote with the default charset of the platform, so it would have corrupted an
 * accent or an ñ on any machine that is not UTF-8. It was removed rather than repaired. What is
 * inherited from {@link Properties} still writes, but in the {@code .properties} format, not in XML.
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

}
