/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

import org.openmarkov.core.model.network.Util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class contains the OpenMarkov system variables and methods to change and
 * recover those variables.
 */
@SuppressWarnings("serial") public class ComponentConfiguration implements Configuration, Serializable {

	// Attributes
	private final HashMap<String, Object> componentVariables;

	/**
	 * frozen
	 */
	private final String componentName;

	// Constructor

	/**
     * @param componentName {@code String}
	 */
	public ComponentConfiguration(String componentName) {
		this.componentName = componentName;
		componentVariables = new HashMap<>();
	}

	// Methods
	@Override public void generateDefaultConfiguration() {
		String initialPath = System.getProperty("user.dir");
		componentVariables.put("initialPath", initialPath + "\\");

		Properties properties = System.getProperties();
		String osName = properties.getProperty("os.name");
		if (osName.toLowerCase().contains("windows")) {
			componentVariables.put("windows", Boolean.TRUE);
		} else {
			componentVariables.put("windows", Boolean.FALSE);
		}
		if (osName.toLowerCase().contains("linux") || osName.toLowerCase().contains("unix")) {
			componentVariables.put("unix", Boolean.TRUE);
		} else {
			componentVariables.put("unix", Boolean.FALSE);
		}

		String netsPath = initialPath + "openmarkov\\nets\\";
		String localizePath = "openmarkov\\io\\localize\\";
		String ceNetTest = "tests\\openmarkov\\ce\\";
		String netTest = "tests\\openmarkov\\nets\\";
		String ioTest = "tests\\openmarkov\\nets\\";

		if ((Boolean) componentVariables.get("unix") == true) {
			netsPath = Util.windows2unixPath(netsPath);
			localizePath = Util.windows2unixPath(localizePath);
			ceNetTest = Util.windows2unixPath(ceNetTest);
			netTest = Util.windows2unixPath(netTest);
			ioTest = Util.windows2unixPath(ioTest);
		}

		componentVariables.put("netsDirectory", netsPath);
		componentVariables.put("localizeDirectory", localizePath);
		componentVariables.put("ceTestDirectory", ceNetTest);
		componentVariables.put("netsTestDirectory", netTest);
		componentVariables.put("ioTestDirectory", ioTest);
	}

	@Override public void setProperty(String name, Object value) {
		componentVariables.put(name, value);
	}

	@Override public Object getProperty(String name) {
		return componentVariables.get(name);
	}

	@Override public String getComponentName() {
		return componentName;
	}

}
