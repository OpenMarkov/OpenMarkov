/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * This class is the manager of the inference annotations. Detects the plugins
 * with InferenceAnnotation annotations.
 * <p>
 * Whether an algorithm can evaluate a network is decided by <em>building</em> it: the
 * {@link InferenceAlgorithm} constructor checks the network type and the constraints, and
 * refuses the network by throwing {@link NotEvaluableNetworkException} or
 * {@link ConstraintViolatedException}. There is no separate check to call.
 *
 * @author mpalacios
 * @author myebra
 * @author ibermejo
 * @see InferenceAnnotation
 */
public class InferenceManager {
    /**
     * Name under which variable elimination registers itself. It is preferred over the
     * sampling algorithms whenever it applies, because it is exact rather than approximate.
     */
    private static final String PREFERRED_ALGORITHM_NAME = "VariableElimination";
    /**
     * Name of the algorithm handed out by {@link #getDefaultApproximateAlgorithm}.
     */
    private static final String APPROXIMATE_ALGORITHM_NAME = "LikelihoodWeighting";
    /**
     * The plugins detected in the project, by their registered name. Sorted, so that
     * "any algorithm that accepts this network" is always the same one.
     */
    private final Map<String, Class<? extends InferenceAlgorithm>> inferenceAlgorithms;

    /**
     * Constructor for InferenceManager.
     */
    public InferenceManager() {
        this.inferenceAlgorithms = new TreeMap<>();
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
            if (buildIfItAccepts(algorithmName, probNet) != null) {
                inferenceAlgorithmNames.add(algorithmName);
            }
        }
        return inferenceAlgorithmNames;
    }

    /**
     * Returns an instance of every algorithm that can evaluate the given instance of ProbNet
     *
     * @param probNet Network
     *
     * @return the algorithms that can evaluate the network, one instance each
     */
    public List<InferenceAlgorithm> getInferenceAlgorithms(ProbNet probNet) {
        List<InferenceAlgorithm> applicableAlgorithms = new ArrayList<>();
        for (String algorithmName : inferenceAlgorithms.keySet()) {
            InferenceAlgorithm algorithm = buildIfItAccepts(algorithmName, probNet);
            if (algorithm != null) {
                applicableAlgorithms.add(algorithm);
            }
        }
        return applicableAlgorithms;
    }

    /**
     * Returns an instance of the algorithm whose names we receive as a
     * parameter, given the ProbNet
     *
     * @param algorithmName Algorithm name
     * @param probNet       Network
     *
     * @return an instance of the algorithm of that name, or {@code null} if no algorithm
     * is registered under it
     *
     * @throws NotEvaluableNetworkException if the algorithm exists but refuses the network
     * @throws ConstraintViolatedException  if the network breaks a constraint the algorithm imposes
     */
    public @Nullable InferenceAlgorithm getInferenceAlgorithmByName(String algorithmName, ProbNet probNet)
            throws NotEvaluableNetworkException, ConstraintViolatedException {
        Class<? extends InferenceAlgorithm> inferenceAlgorithmClass = inferenceAlgorithms.get(algorithmName);
        return (inferenceAlgorithmClass == null) ? null : build(inferenceAlgorithmClass, probNet);
    }

    /**
     * Returns an instance of the default algorithm given the ProbNet: variable elimination
     * when it accepts the network, and otherwise any registered algorithm that does.
     *
     * @param probNet Network
     *
     * @return an instance of the default algorithm, or {@code null} if no registered
     * algorithm can evaluate the network
     */
    public @Nullable InferenceAlgorithm getDefaultInferenceAlgorithm(ProbNet probNet) {
        InferenceAlgorithm exactAlgorithm = buildIfItAccepts(PREFERRED_ALGORITHM_NAME, probNet);
        if (exactAlgorithm != null) {
            return exactAlgorithm;
        }
        for (String algorithmName : inferenceAlgorithms.keySet()) {
            InferenceAlgorithm algorithm = buildIfItAccepts(algorithmName, probNet);
            if (algorithm != null) {
                return algorithm;
            }
        }
        return null;
    }

    /**
     * Returns an instance of the default approximate algorithm given the
     * ProbNet
     *
     * @param probNet Network
     *
     * @return an instance of the default approximate algorithm, or {@code null} if it
     * is not registered or does not accept the network
     */
    public @Nullable InferenceAlgorithm getDefaultApproximateAlgorithm(ProbNet probNet) {
        return buildIfItAccepts(APPROXIMATE_ALGORITHM_NAME, probNet);
    }

    /**
     * Builds the named algorithm for this network, or returns {@code null} if it is not
     * registered or does not accept the network. Refusing a network is an answer, not an
     * error, which is why this is the shape the selection methods above are written on.
     */
    private @Nullable InferenceAlgorithm buildIfItAccepts(String algorithmName, ProbNet probNet) {
        try {
            return getInferenceAlgorithmByName(algorithmName, probNet);
        } catch (NotEvaluableNetworkException | ConstraintViolatedException e) {
            return null;
        }
    }

    /**
     * Builds an algorithm through its {@code (ProbNet)} constructor, which is where the
     * network is accepted or refused.
     * <p>
     * An algorithm that cannot be built at all — no public {@code (ProbNet)} constructor, say —
     * is a different matter from one that refuses this particular network: it is a defect in
     * that algorithm. It is logged and reported as {@code null} rather than thrown, so that one
     * broken plugin cannot take every other algorithm down with it; but it is logged, so that it
     * does not disappear without a trace either.
     */
    private static @Nullable InferenceAlgorithm build(Class<? extends InferenceAlgorithm> algorithmClass,
                                                      ProbNet probNet)
            throws NotEvaluableNetworkException, ConstraintViolatedException {
        try {
            return algorithmClass.getConstructor(ProbNet.class).newInstance(probNet);
        } catch (InvocationTargetException e) {
            switch (e.getTargetException()) {
                case NotEvaluableNetworkException notEvaluable -> throw notEvaluable;
                case ConstraintViolatedException constraintViolated -> throw constraintViolated;
                default -> throw new UnreachableException(e);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            OpenMarkovLogger.LOGGER.warn("Registered inference algorithm " + algorithmClass.getName()
                                                 + " cannot be built: it needs a public constructor taking a ProbNet.",
                                         e);
            return null;
        }
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
