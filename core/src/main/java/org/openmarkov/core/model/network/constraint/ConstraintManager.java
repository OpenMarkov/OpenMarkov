/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class ConstraintManager {
    private static final ConstraintManager INSTANCE = new ConstraintManager();
    private final HashMap<Class<? extends PNConstraint>, ConstraintBehavior> defaultConstraintBehaviors;
    
    /**
     * Constructor for ConstraintManager.
     */
    @SuppressWarnings("unchecked") private ConstraintManager() {
        this.defaultConstraintBehaviors = new HashMap<>();
        ConstraintManager.findAllConstraints().forEach(plugin -> {
            Constraint lAnnotation = plugin.getAnnotation(Constraint.class);
            this.defaultConstraintBehaviors.put(plugin, lAnnotation.defaultBehavior());
        });
    }
    
    // Methods
    
    /**
     * Singleton pattern.
     *
     * @return The unique instance.
     */
    public static ConstraintManager getUniqueInstance() {
        return INSTANCE;
    }
    
    /**
     * Generates the minimal (i.e. not including optional constraints)
     * constraint list given the network type and the Constraints annotated as
     * such.
     *
     * @param includeOptionals If include optional constraints
     * @param type             of the network the list is being generated for.
     *
     * @return a minimal list of constraint.
     */
    public ArrayList<PNConstraint> buildConstraintList(NetworkType type, boolean includeOptionals) {
        // Init the list with those constraints that have the default value set to YES
        ArrayList<PNConstraint> constraints = new ArrayList<>();
        for (Class<? extends PNConstraint> constraintClass : defaultConstraintBehaviors.keySet()) {
            boolean isMandatoryConstraint = getDefaultBehavior(constraintClass) == ConstraintBehavior.YES;
            boolean isOptionalConstraintToUse = includeOptionals && getDefaultBehavior(constraintClass) == ConstraintBehavior.OPTIONAL;
            if (isMandatoryConstraint || isOptionalConstraintToUse) {
                try {
                    constraints.add(constraintClass.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new UnreachableException(e);
                }
            }
        }
        
        // Overwrite the list with the constraints specified in the corresponding network type
        HashMap<Class<? extends PNConstraint>, ConstraintBehavior> overwrittenConstraints = type
                .getOverwrittenConstraints();
        for (Class<? extends PNConstraint> constraintClass : overwrittenConstraints.keySet()) {
            if (overwrittenConstraints.get(constraintClass) == ConstraintBehavior.YES) {
                try {
                    constraints.add(constraintClass.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new UnreachableException(e);
                }
            } else if (overwrittenConstraints.get(constraintClass) == ConstraintBehavior.NO) {
                for (int i = 0; i < constraints.size(); ++i) {
                    if (constraints.get(i).getClass() == constraintClass) {
                        constraints.remove(i);
                    }
                }
            }
        }
        return constraints;
        
    }
    
    public final ArrayList<PNConstraint> buildConstraintList(NetworkType type) {
        return buildConstraintList(type, false);
    }
    
    public ConstraintBehavior getDefaultBehavior(Class<?> constraintClass) {
        return defaultConstraintBehaviors.get(constraintClass);
    }
    
    public static @NotNull Stream<Class<? extends PNConstraint>> findAllConstraints() {
        return PluginSearch.init().annotatedWith(Constraint.class).childrenOf(PNConstraint.class).stream();
    }
}
