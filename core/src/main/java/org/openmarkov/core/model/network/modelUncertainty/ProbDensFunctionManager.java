/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Singleton registry of {@link ProbDensFunction} implementations. At startup
 * it scans the classpath for classes annotated with
 * {@link ProbDensFunctionType} and indexes them by name (and by univariate
 * name) so that uncertain values can be created from the textual descriptors
 * stored in network files.
 */
public class ProbDensFunctionManager {
    
    private static final ProbDensFunctionManager INSTANCE = new ProbDensFunctionManager();
    private final Map<String, Class<? extends ProbDensFunction>> probDensFunctions;
    
    //For Univariate
    private final Map<String, List<String[]>> probDensParametrizations;
    
    /**
     * Constructor for ProbDensFunctionManager.
     */
    private ProbDensFunctionManager() {
        this.probDensFunctions = new HashMap<>();
        this.probDensParametrizations = new HashMap<>();
        findAllProbDensFunctions().forEach(plugin->{
            ProbDensFunctionType annotation = plugin.getAnnotation(ProbDensFunctionType.class);
            probDensFunctions.put(annotation.name(), plugin);
            //For Univariate
            String univariateName = annotation.univariateName();
            String name = annotation.name();
            if (univariateName.equals("default"))
                univariateName = name;
            String[] parametersNames = annotation.parameters();
            String parametersNamesConcat = parametersNames[0];
            for (int i = 1; i < parametersNames.length; i++) {
                parametersNamesConcat += ", " + parametersNames[i];
            }
            String[] parametrizationData = new String[]{parametersNamesConcat, name};
            if (probDensParametrizations.containsKey(univariateName)) {
                List<String[]> parametersList = probDensParametrizations.get(univariateName);
                parametersList.add(parametrizationData);
            } else {
                List<String[]> parametersList = new ArrayList<String[]>();
                parametersList.add(parametrizationData);
                probDensParametrizations.put(univariateName, parametersList);
            }
        });
    }
    
    // Methods
    
    /**
     * Singleton pattern.
     *
     * @return The unique instance.
     */
    public static ProbDensFunctionManager getUniqueInstance() {
        return INSTANCE;
    }
    
    //For Univariate
    public List<String> getValidProbDensFunctions() {
        List<String> validFunctions = new ArrayList<>(probDensFunctions.keySet());
        return validFunctions;
    }
    
    public List<String> getDistributions() {
        List<String> distributions = new ArrayList<>(probDensParametrizations.keySet());
        return distributions;
        
    }
    
    public List<String[]> getParametrizations(String univariateName) {
        return probDensParametrizations.get(univariateName);
    }
    
    public String getDistributionName(String univariateName, String parametrization) {
        String name = "";
        List<String[]> parametrizationList = probDensParametrizations.get(univariateName);
        for (String[] p : parametrizationList) {
            if (p[0].equals(parametrization)) {
                name = p[1];
                break;
            }
        }
        return name;
    }
    
    /**
     * @param functionName Name of the function
     * @return The class for that function name
     */
    @SuppressWarnings("unchecked") public Class<? extends ProbDensFunction> getProbDensFunctionClass(
            String functionName) {
        return probDensFunctions.get(functionName);
    }
    
    public Class<? extends ProbDensFunction> getProbDensFunctionClass(String univariateName, String parametrization) {
        
        return getProbDensFunctionClass(getDistributionName(univariateName, parametrization));
    }
    
    
    public List<String> getValidProbDensFunctions(boolean isChance) {
        List<String> validFunctions = new ArrayList<>();
        for (String functionName : probDensFunctions.keySet()) {
            Class<?> functionClass = probDensFunctions.get(functionName);
            ProbDensFunctionType annotation = functionClass.getAnnotation(ProbDensFunctionType.class);
            if ((annotation.isValidForNumeric() && !isChance) || (annotation.isValidForProbabilities() && isChance)) {
                validFunctions.add(functionName);
            }
        }
        return validFunctions;
    }
    
    public List<String> getDESValidProbDensFunctions() {
        List<String> validFunctions = new ArrayList<>();
        for (String functionName : probDensFunctions.keySet()) {
            Class<?> functionClass = probDensFunctions.get(functionName);
            ProbDensFunctionType annotation = functionClass.getAnnotation(ProbDensFunctionType.class);
            if ((annotation.isValidForNumeric()) || (annotation.isValidForProbabilities())) {
                validFunctions.add(functionName);
            }
        }
        return validFunctions;
    }
    
    public String[] getParameters(String functionName) {
        Class<? extends ProbDensFunction> functionClass = probDensFunctions.get(functionName);
        ProbDensFunctionType annotation = functionClass.getAnnotation(ProbDensFunctionType.class);
        return annotation.parameters();
    }
    
    public ProbDensFunction newInstance(String functionName, double[] parameters) {
        Class<? extends ProbDensFunction> probDensFunctionClass = probDensFunctions.get(functionName);
        try {
            ProbDensFunction newInstance = probDensFunctionClass.getDeclaredConstructor().newInstance();
            newInstance.setParameters(parameters);
            return newInstance;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    private static @NotNull Stream<Class<? extends ProbDensFunction>> findAllProbDensFunctions() {
        return PluginSearch.init()
                           .annotatedWith(ProbDensFunctionType.class)
                           .childrenOf(ProbDensFunction.class)
                           .stream();
    }
    
}
