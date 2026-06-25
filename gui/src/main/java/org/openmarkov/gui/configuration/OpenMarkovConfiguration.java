/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Singleton that manages the serialized configuration for OpenMarkov.
 * Reads configuration from disk on startup and provides per-component property access.
 */
@SuppressWarnings("serial")
public class OpenMarkovConfiguration implements DefaultConfiguration, Serializable {

	// Attributes
	/**
	 * Singleton pattern
	 */
    private static OpenMarkovConfiguration OPEN_MARKOV_CONFIGURATION = new OpenMarkovConfiguration();
    
    private static final String CONFIGURATION_FILE_NAME = "OpenMarkov.conf";

	private HashMap<String, Configuration> configurations;
    
    // Constructor

	/**
	 * Singleton pattern (private constructor).<p>
	 * Reads configuration from disk or generates default configuration.
	 */
	private OpenMarkovConfiguration() {
        readConfiguration();
	}

	// Methods

	/**
	 * Singleton pattern.
	 *
     * @return {@code OpenMarkovConfiguration}
	 */
	public static OpenMarkovConfiguration getUniqueInstance() {
        return OPEN_MARKOV_CONFIGURATION;
	}

	/**
	 * Returns a property value for the given plugin and property name.
	 *
	 * @param pluginName   the name of the plugin/component
	 * @param propertyName the name of the property
	 * @return the property value, or {@code null} if the plugin or property does not exist
	 */
	public static Object getProperty(String pluginName, String propertyName) {
		Configuration componentConfiguration = getUniqueInstance().getComponentConfiguration(pluginName);
		if (componentConfiguration != null) {
			return componentConfiguration.getProperty(propertyName);
		}
		return null;
	}

	/**
	 * Write configuration to disk in serialized format.
	 */
    public static void writeConfiguration() {
		try {
            FileOutputStream fos = new FileOutputStream(CONFIGURATION_FILE_NAME);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(OPEN_MARKOV_CONFIGURATION);
			oos.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Generates a default configuration for each component.
	 */
	@Override public void generateDefaultConfiguration() {
		configurations = createConfigurationObjects();
	}

	/**
	 * Returns the configuration for the component with the given name.
	 *
	 * @param name the component name
	 * @return the configuration, or {@code null} if no component with that name exists
	 */
	public Configuration getComponentConfiguration(String name) {
		return configurations.get(name);
	}

	/**
	 * Creates configurations for each component.<p>
	 * To extend this method for each component:<ol>
     * <li>Create a class that implements {@code ComponentConfiguration}.
	 * <li>Create an object of that class.
	 * <li>Put that object in the HashMap.
	 * </ol>
	 *
     * @return {@code HashMap} with key = {@code String} (component
     * name) and value = an {@code object} that implements
     * {@code ComponentConfiguration}.
	 */
    private static HashMap<String, Configuration> createConfigurationObjects() {
        HashMap<String, Configuration> configurations = new HashMap<String, Configuration>();
		// Create a class that implements Configuration
		String kernelComponenteName = "kernel";
		ComponentConfiguration kernelConfiguration = new ComponentConfiguration(kernelComponenteName); // component name
		// Put that object in the HashMap
		configurations.put(kernelComponenteName, kernelConfiguration);
		return configurations;
	}

	private void readConfiguration() {
		HashMap<String, Configuration> configurationsCollection = createConfigurationObjects();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CONFIGURATION_FILE_NAME))) {
            OPEN_MARKOV_CONFIGURATION = (OpenMarkovConfiguration) ois.readObject();
			if (configurations == null) {
				generateDefaultConfiguration(configurationsCollection);
			} else {
				generateDefaultConfiguration(configurationsCollection, configurations);
			}
		} catch (FileNotFoundException f) {
			generateDefaultConfiguration(configurationsCollection);
        } catch (IOException | ClassNotFoundException e) {
		}
    }

	/**
     * @param configurationsCollection {@code HashMap} with {@code key =
     *                                 String} and {@code value = ComponentConfiguration}
	 */
	private void generateDefaultConfiguration(HashMap<String, Configuration> configurationsCollection) {
		ArrayList<Configuration> configurationsArray = new ArrayList<Configuration>(configurationsCollection.values());
		for (Configuration configuration : configurationsArray) {
			configuration.generateDefaultConfiguration();
		}
		configurations = configurationsCollection;
		writeConfiguration();
	}

	private void generateDefaultConfiguration(HashMap<String, Configuration> configurationsCollection,
			HashMap<String, Configuration> configurationsReaded) {
		ArrayList<Configuration> configsCollectionArray = new ArrayList<Configuration>(
				configurationsCollection.values());
		boolean newConfiguration = false;
		for (Configuration configuration : configsCollectionArray) {
			if (configurationsReaded.get(configuration) == null) {
				configuration.generateDefaultConfiguration();
				configurationsReaded.put(configuration.getComponentName(), configuration);
				newConfiguration = true;
			}
		}
		configurations = configurationsReaded;
		if (newConfiguration) {
			writeConfiguration();
		}
	}

}
