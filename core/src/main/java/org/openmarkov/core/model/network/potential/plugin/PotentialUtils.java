/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;
import org.openmarkov.plugin.ExtensionTree;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Manuel Arias
 */
public class PotentialUtils {
    
    public static String getPotentialName(Class<?> clazz) {
        // Walk up the class hierarchy to find the @PotentialType annotation.
        // Subclasses like UncertainTablePotential and StrategicTablePotential
        // don't have their own annotation and should inherit their parent's name.
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            PotentialType annotation = c.getAnnotation(PotentialType.class);
            if (annotation != null) {
                String[] names = annotation.names();
                return names[0] == null ? "" : names[0];
            }
        }
        return "";
    }
    
    public static List<String> getNames(Class<?> clazz) {
        PotentialType annotation = clazz.getAnnotation(PotentialType.class);
        if (annotation == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(annotation.names());
    }
    
    public static final HashMap<String, Class<? extends Potential>> POTENTIALS_BY_NAME;
    
    static {
        HashMap<String, Class<? extends Potential>> potentialsByName = new HashMap<>();
        var potentialsByExceptionLevel = PluginSearch.init()
                                                     .extending(Potential.class).extensionTree()
                                                     .breathFirstLevelOrderQueue()
                                                     .stream().map(ExtensionTree::getCurrentClass).toList();
        potentialsByExceptionLevel.forEach(potentialClass -> {
            PotentialUtils.getNames(potentialClass).forEach(potentialName -> {
                potentialsByName.put(potentialName, potentialClass);
            });
        });
        potentialsByExceptionLevel.forEach(potentialClass -> {
            String potentialName = PotentialUtils.getPotentialName(potentialClass);
            if (potentialName == null || potentialName.isBlank()) return;
            potentialsByName.putIfAbsent(potentialName, potentialClass);
        });
        POTENTIALS_BY_NAME = potentialsByName;
    }
    
    public static Class<? extends Potential> getClassByName(String name) {
        return PotentialUtils.POTENTIALS_BY_NAME.get(name);
    }
    
    /**
     * Returns a potential by name.
     *
     * @param variables   List of variables
     * @param role        Potential role
     * @param cycleLength Cycle lenghts
     *
     * @return a new Potential instance given the parameters.
     */
    public static Potential instanciateSafely(Class<? extends Potential> potentialClass, List<Variable> variables, PotentialRole role, CycleLength... cycleLength) {
        try {
            Constructor<? extends Potential> constructor;
            try {
                if (cycleLength != null && cycleLength.length != 0) {
                    constructor = potentialClass.getConstructor(List.class, CycleLength.class);
                    return constructor.newInstance(variables, cycleLength[0]);
                }
                constructor = potentialClass.getConstructor(List.class, PotentialRole.class);
                return constructor.newInstance(variables, role);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                constructor = potentialClass.getConstructor(List.class);
                return constructor.newInstance(variables);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Returns all potentials' names applicable to the given variable list and potential role.
     *
     * @param node Node
     *
     * @return a list of potentials' names.
     */
    public static List<Class<? extends Potential>> getFilteredPotentialClasses(Node node) {
        List<Class<? extends Potential>> filteredPotentials = new ArrayList<>();
        
        Potential potential = node.getPotential();
        List<Variable> variables = potential != null ? potential.getVariables() : PotentialOperations.variableAndParents(node.getProbNet(), node.getVariable());
        PotentialRole potentialRole = potential != null ? potential.getPotentialRole() : PotentialRole.CONDITIONAL_PROBABILITY;
        for (Class<? extends Potential> potentialClass : PotentialUtils.findAllPotentials().toList()) {
            try {
                Method validateMethod = potentialClass.getMethod("validate", Node.class, List.class, PotentialRole.class);
                if ((Boolean) validateMethod.invoke(null, node, variables, potentialRole)) {
                    filteredPotentials.add(potentialClass);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new UnreachableException(e);
            }
        }
        return filteredPotentials;
    }
    
    /**
     * Finds all learning algorithms.
     *
     * @return a list of learning algorithms.
     */
    private static @NotNull Stream<Class<? extends Potential>> findAllPotentials() {
        return PluginSearch.init()
                           .annotatedWith(PotentialType.class)
                           .childrenOf(Potential.class)
                           .stream();
    }
    
    @Nullable public static Potential generateDefaultPotential(ProbNet probNet, Variable variable, NodeType nodeType) {
        final ArrayList<Variable> variableAndParents = PotentialOperations.variableAndParents(probNet, variable);
        return switch (nodeType) {
            case DECISION, SV_PRODUCT, SV_SUM -> null;
            case CHANCE -> new TablePotential(variableAndParents, PotentialRole.CONDITIONAL_PROBABILITY);
            case UTILITY -> new ExactDistrPotential(variableAndParents, PotentialRole.CONDITIONAL_PROBABILITY);
            case EVENT -> new DeltaPotential(variableAndParents, PotentialRole.CONDITIONAL_PROBABILITY, 0.02);
        };
    }
    
    @Nullable public static Potential generateDefaultPotential(Node node) {
        return PotentialUtils.generateDefaultPotential(node.getProbNet(), node.getVariable(), node.getNodeType());
    }
    
}

