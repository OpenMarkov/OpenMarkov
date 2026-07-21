/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author mluque
 * ResourceBundle based in XML properties files.
 */
public class XMLResourceBundle extends ResourceBundle {
	
	private final XMLProperties props;

	public XMLResourceBundle(InputStream stream) throws IOException {
		props = new XMLProperties();
		props.load(stream);
	}

	@Override protected Object handleGetObject(String key) {
		return props.getProperty(key);
	}
	
	@Override public @NotNull Enumeration<String> getKeys() {
		var keysIterator= this.props.stringPropertyNames().iterator();
		return new IteratorEnumeration<String>(keysIterator);
	}


	public Set<String> getStringKeys() {
		return this.props.stringPropertyNames();
	}
	
	
	// Helper class to convert Iterator to Enumeration
	static class IteratorEnumeration<T> implements Enumeration<T> {
		private final Iterator<T> iterator;
		
		public IteratorEnumeration(Iterator<T> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}
		
		@Override
		public T nextElement() {
			return iterator.next();
		}
	}
}
