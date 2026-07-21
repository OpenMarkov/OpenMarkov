package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;


import org.apache.commons.collections4.map.MultiKeyMap;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionWithKnownInverseCDF;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory of ParametrizedFunctions.
 * Returns parametrized probability functions subclasses of ProbDensFunction annotated with ParametrizedFunctionType
 *
 * @author cmyago
 * @version 1 20/10/2020
 */
public class ParametrizedFunctionManager {

    /**
     * Singleton
     */
    private static final ParametrizedFunctionManager instance = new ParametrizedFunctionManager();
    
    /**
     * Map associating a parametrized function with its class
     */
    private final MultiKeyMap parametrizedClasses;

    /**
     * Map associating parametrized functions with its parameters
     */

    private final MultiKeyMap parametrizationParameters;

    /**
     * Map associating a ProbDensFunction with its parametrized functions
     */
    private final Map<String, List<String>> distributionsMap;


    /**
     * Constructor for ProbDensFunctionManager.
     */
    private ParametrizedFunctionManager() {
        super();
        //Classes
        this.parametrizedClasses = new MultiKeyMap();
        //Mapping distribution-parametrizations
        this.distributionsMap = new HashMap<>();

        //Mapping parametrization-parameter
        this.parametrizationParameters = new MultiKeyMap();

        List<Class<?>> plugins = findAllParametrizedFunctions();


        for (Class<?> plugin : plugins) {
            //TODO Exceptions managing
            ParametrizedFunctionType annotation = plugin.getAnnotation(ParametrizedFunctionType.class);
            //Add class
            parametrizedClasses.put(annotation.distributionName(), annotation.parametrizationName(), plugin);
            //Add parametrizaion
            parametrizationParameters.put(annotation.distributionName(), annotation.parametrizationName(), Arrays.asList(annotation.parameters()));
            //Add to distribution-parametrization mapping
            //If there is the first parametrization of a distirbution
            if (!distributionsMap.containsKey(annotation.distributionName())) {
                distributionsMap.put(annotation.distributionName(), new ArrayList<String>());
            }
            distributionsMap.get(annotation.distributionName()).add(annotation.parametrizationName());

        }
    }

    // Methods

    /**
     * Singleton pattern.
     *
     * @return The unique instance.
     */
    public static ParametrizedFunctionManager getUniqueInstance() {
        return instance;
    }


    /**
     * 14/08/2022 --> Changed from ProbDensFunction to ProbDensFunctionWithKnownInverseCDF for using getInverseCumulativeDistributionFunction
     * Returns the class correspondent which these distributionName and parametrizationName
     *
     * @param distributionName    -name of the distribution
     * @param parametrizationName - name of the parametrized function
     * @return the class correspondent which these distributionName and parametrizationName
     */
    public Class<ProbDensFunctionWithKnownInverseCDF> getParametrizedClass(String distributionName, String parametrizationName) {
        return (Class<ProbDensFunctionWithKnownInverseCDF>) parametrizedClasses.get(distributionName, parametrizationName);
    }

    /**
     * Returns the parameters of parametrizedDistributionName
     *
     * @param distributionName    distribution parametrization whose parameters are needed
     * @param parametrizationName - name of the parametrized function
     * @return an array with the names of the parameters
     */
    public List<String> getParameters(String distributionName, String parametrizationName) {

        return (List<String>) parametrizationParameters.get(distributionName, parametrizationName);

    }


    /**
     * Returns an instance of the ProbDensFunction given by its distributionName an parametrizationName
     *
     * @param distributionName
     * @param parametrizationName
     * @return an instance of the ProbDensFunction given by its distributionName an parametrizationName
     */

    public ProbDensFunction newInstance(String distributionName, String parametrizationName) {
        Class<?> probDensFunctionClass = (Class<?>) parametrizedClasses.get(distributionName, parametrizationName);
        ProbDensFunction newInstance = null;
        try {
            newInstance = (ProbDensFunction) probDensFunctionClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return newInstance;
    }

    /**
     * Returns all the classes annotated with ParametrizedFunctionType class
     *
     * @return all the classes annotated with ParametrizedFunctionType class
     */
    private List<Class<?>> findAllParametrizedFunctions() {
        return  PluginSearch.init().annotatedWith(ParametrizedFunctionType.class).stream().toList();
    }


    /**
     * Returns the map associating ProbDensFunction with its parametrizations
     *
     * @return Map associating ProbDensFunction with its parametrizations
     */
    public Map<String, List<String>> getDistributionsMap() {
        return distributionsMap;
    }





}
