/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.gui;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.plugin.PluginSearch;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Registry that discovers {@link AlgorithmParametersDialog} classes annotated with
 * {@link AlgorithmConfiguration} and provides factory methods to instantiate
 * the appropriate dialog for a given learning algorithm.
 */
public class AlgorithmConfigurationManager {
    private final List<Class<? extends AlgorithmParametersDialog>> algorithmConfigurations;
    private final JFrame parent;
    
    /**
     * Constructor for AlgorithmConfigurationManager.
     */
    @SuppressWarnings("unchecked") public AlgorithmConfigurationManager(JFrame parent) {
        this.parent = parent;
        algorithmConfigurations =        AlgorithmConfigurationManager.findAllAlgorithmsConfigurations().toList();
        
    }
    
    private static AlgorithmConfiguration info(Class<? extends AlgorithmParametersDialog> plugin) {
        return plugin.getAnnotation(AlgorithmConfiguration.class);
    }
    
    /**
     * Finds and instantiates the parameters dialog for the given learning algorithm class.
     *
     * @param algorithmClass the learning algorithm class
     * @return the corresponding parameters dialog, or {@code null} if none found
     */
    public final AlgorithmParametersDialog instanciateForAlgorithm(Class<? extends LearningAlgorithm> algorithmClass) {
        return instanciate(algorithmConfigurations.stream().filter(config-> info(config).algorithm()==algorithmClass )
                                                  .findFirst().orElse(null));
    }
    
    /**
     * Returns a learning algorithm by name.
     *
     * @return a learning algorithm.
     */
    public final AlgorithmParametersDialog instanciate(Class<? extends AlgorithmParametersDialog> algorithmOptionsGUIClass) {
        try {
            return algorithmOptionsGUIClass.getConstructor(JFrame.class, boolean.class)
                                           .newInstance(parent, true);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
                 InvocationTargetException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Finds all learning algorithms.
     *
     * @return a list of learning algorithms.
     */
    private static @NotNull Stream<Class<? extends AlgorithmParametersDialog>> findAllAlgorithmsConfigurations() {
        return PluginSearch.init()
                           .annotatedWith(AlgorithmConfiguration.class)
                           .childrenOf(AlgorithmParametersDialog.class)
                           .stream();
    }
    
}
