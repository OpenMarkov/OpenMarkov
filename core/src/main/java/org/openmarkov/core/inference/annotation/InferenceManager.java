/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.TuningNetworkType;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is the manager of the inference annotations. Detects the plugins
 * with InferenceAnnotation annotations.
 *
 * @author mpalacios
 * @author myebra
 * @author ibermejo
 * @see InferenceAnnotation
 */
public class InferenceManager {
    /**
     * The list of plugins detected in the project
     */
    private final HashMap<String, Class<? extends InferenceAlgorithm>> inferenceAlgorithms;
    
    /**
     * Constructor for InferenceManager.
     */
    @SuppressWarnings("unchecked") public InferenceManager() {
        this.inferenceAlgorithms = new HashMap<>();
        InferenceManager.findAllInferencePlugins().forEach(algorithmClass -> {
            InferenceAnnotation lAnnotation = algorithmClass.getAnnotation(InferenceAnnotation.class);
            this.inferenceAlgorithms.put(lAnnotation.name(), algorithmClass);
        });
    }
    
    /**
     * Returns the list of the names of the algorithms that can evaluate the
     * given instance of ProbNet
     *
     * @param probNet Network
     *
     * @return the list of the names of the algorithms that can evaluate the network
     */
    public List<String> getInferenceAlgorithmNames(ProbNet probNet) {
        List<String> inferenceAlgorithmNames = new ArrayList<>();
        for (String algorithmName : inferenceAlgorithms.keySet()) {
            try {
                Constructor<? extends InferenceAlgorithm> constructor = inferenceAlgorithms.get(algorithmName)
                                                                                           .getConstructor(ProbNet.class);
                constructor.newInstance(probNet);
                inferenceAlgorithmNames.add(algorithmName);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ignored) {
            }
            
        }
        return inferenceAlgorithmNames;
    }
    
    /**
     * Returns the list of the names of the algorithms that can evaluate the
     * given instance of ProbNet
     *
     * @param probNet Network
     *
     * @return the list of the names of the algorithms that can evaluate the network
     */
    public List<InferenceAlgorithm> getInferenceAlgorithms(ProbNet probNet) {
        List<InferenceAlgorithm> inferenceAlgorithms = new ArrayList<>();
        for (String algorithmName : this.inferenceAlgorithms.keySet()) {
            try {
                Class<? extends InferenceAlgorithm> inferenceAlgorithmClass = this.inferenceAlgorithms
                        .get(algorithmName);
                Constructor<? extends InferenceAlgorithm> constructor = this.inferenceAlgorithms.get(algorithmName)
                                                                                                .getConstructor(ProbNet.class);
                Method checkEval = inferenceAlgorithmClass.getMethod("checkEvaluability", ProbNet.class);
                @ToCheck(reasonKind = ToCheck.ReasonKind.CODE_QUALITY, reasonDescription = "isEvaluable is always true") boolean isEvaluable = true;
                try {
                    checkEval.invoke(inferenceAlgorithmClass, probNet);
                } catch (InvocationTargetException e) {
                    isEvaluable = e.getTargetException().getClass() != NotEvaluableNetworkException.class;
                }
                if (isEvaluable) {
                    InferenceAlgorithm inferenceAlgorithm = constructor.newInstance(probNet);
                    inferenceAlgorithms.add(inferenceAlgorithm);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     InstantiationException e) {
                throw new UnreachableException(e);
            }
        }
        return inferenceAlgorithms;
    }
    
    /**
     * Returns an instance of the algorithm whose names we receive as a
     * parameter, given the ProbNet
     *
     * @param algorithmName Algorithm name
     * @param probNet       Network
     *
     * @return an instance of the algorithm whose names we receive as a parameter
     *
     * @throws NotEvaluableNetworkException NotEvaluableNetworkException
     * @throws NoSuchMethodException        NoSuchMethodException
     */
    public @Nullable InferenceAlgorithm getInferenceAlgorithmByName(String algorithmName, ProbNet probNet)
            throws NotEvaluableNetworkException, NoSuchMethodException {
        Class<? extends InferenceAlgorithm> inferenceAlgorithmClass = inferenceAlgorithms.get(algorithmName);
        try {
            Constructor<? extends InferenceAlgorithm> constructor = inferenceAlgorithmClass.getConstructor(ProbNet.class);
            Method checkEval = inferenceAlgorithmClass.getMethod("checkEvaluability", ProbNet.class);
            checkEval.invoke(inferenceAlgorithms.get(algorithmName), probNet);
            return constructor.newInstance(probNet);
        } catch (InvocationTargetException e) {
            Throwable targetExcep = e.getTargetException();
            if (targetExcep instanceof NotEvaluableNetworkException notEvaluableNetworkException) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw notEvaluableNetworkException;
            }
            throw new UnreachableException(e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 SecurityException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Returns an instance of the default algorithm given the ProbNet
     *
     * @param probNet Network
     *
     * @return an instance of the default algorithm
     *
     * @throws NotEvaluableNetworkException NotEvaluableNetworkException
     */
    public InferenceAlgorithm getDefaultInferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException {
        InferenceAlgorithm defaultAlgorithm = null;
        try {
            if (probNet.getNetworkType().equals(BayesianNetworkType.getUniqueInstance())) {
                defaultAlgorithm = getInferenceAlgorithmByName("VariableElimination", probNet);
            } else if (probNet.getNetworkType().equals(InfluenceDiagramType.getUniqueInstance())) {
                defaultAlgorithm = getInferenceAlgorithmByName("VariableElimination", probNet);
            } else if (probNet.getNetworkType().equals(TuningNetworkType.getUniqueInstance())) {
                defaultAlgorithm = getInferenceAlgorithmByName("VariableElimination", probNet);
            } else if (probNet.getNetworkType().equals(DecisionAnalysisNetworkType.getUniqueInstance())) {
                defaultAlgorithm = getInferenceAlgorithmByName("DSD", probNet);
            } else {
                List<InferenceAlgorithm> possibleAlgorithms = getInferenceAlgorithms(probNet);
                if (!possibleAlgorithms.isEmpty()) {
                    defaultAlgorithm = possibleAlgorithms.get(0); // Get the first
                }
            }
        } catch (SecurityException | NoSuchMethodException e) {
            throw new UnreachableException(e);
        }
        return defaultAlgorithm;
    }
    
    /**
     * Returns an instance of the default approximate algorithm given the
     * ProbNet
     *
     * @param probNet Network
     *
     * @return An instance of the default approximate algorithm
     *
     * @throws NotEvaluableNetworkException NotEvaluableNetworkException
     */
    public InferenceAlgorithm getDefaultApproximateAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException {
        InferenceAlgorithm defaultAlgorithm = null;
        try {
            defaultAlgorithm = getInferenceAlgorithmByName("LikelihoodWeighting", probNet);
        } catch (SecurityException | NoSuchMethodException e) {
            // This should not be the case as we are hard coding to an algorithm
            // that should have a public constructor
            throw new UnreachableException(e);
        }
        return defaultAlgorithm;
    }
    
    /**
     * This method gets all the plugins with InferenceType annotations
     *
     * @return a list with the plugins detected with InferenceType annotations.
     */
    private static @NotNull Stream<Class<? extends InferenceAlgorithm>> findAllInferencePlugins() {
        return PluginSearch.init()
                           .annotatedWith(InferenceAnnotation.class)
                           .childrenOf(InferenceAlgorithm.class)
                           .stream();
    }
}
