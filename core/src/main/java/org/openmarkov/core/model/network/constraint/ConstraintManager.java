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
     * The constraints every network of the given type has: those that are YES by default, plus those
     * the type declares YES, minus those it declares NO.
     */
    public ArrayList<PNConstraint> buildConstraintList(NetworkType type) {
        // Init the list with those constraints that have the default value set to YES
        ArrayList<PNConstraint> constraints = new ArrayList<>();
        for (Class<? extends PNConstraint> constraintClass : defaultConstraintBehaviors.keySet()) {
            if (getDefaultBehavior(constraintClass) == ConstraintBehavior.YES) {
                addIfInstantiable(constraints, constraintClass);
            }
        }

        // Overwrite the list with the constraints specified in the corresponding network type
        HashMap<Class<? extends PNConstraint>, ConstraintBehavior> overwrittenConstraints = type
                .getOverwrittenConstraints();
        for (Class<? extends PNConstraint> constraintClass : overwrittenConstraints.keySet()) {
            if (overwrittenConstraints.get(constraintClass) == ConstraintBehavior.YES) {
                // A network type may re-declare as YES a constraint that is already mandatory by
                // default (MDP does it with NoCycle, NoSelfLoop and OnlyDirectedLinks). Adding it
                // again would put the same constraint twice in the list.
                boolean alreadyPresent = constraints.stream()
                                                    .anyMatch(constraint -> constraint.getClass() == constraintClass);
                if (!alreadyPresent) {
                    addIfInstantiable(constraints, constraintClass);
                }
            } else if (overwrittenConstraints.get(constraintClass) == ConstraintBehavior.NO) {
                constraints.removeIf(constraint -> constraint.getClass() == constraintClass);
            }
        }
        return constraints;

    }

    /**
     * Adds a new instance of the constraint to the list, unless it has no no-argument constructor:
     * a constraint that keeps data inside is created by whoever knows the data.
     */
    private static void addIfInstantiable(ArrayList<PNConstraint> constraints,
            Class<? extends PNConstraint> constraintClass) {
        try {
            constraints.add(constraintClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException builtByItsCallerInstead) {
            // Nothing to add here.
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UnreachableException(e);
        }
    }

    public ConstraintBehavior getDefaultBehavior(Class<?> constraintClass) {
        return defaultConstraintBehaviors.get(constraintClass);
    }

    public static @NotNull Stream<Class<? extends PNConstraint>> findAllConstraints() {
        return PluginSearch.init().annotatedWith(Constraint.class).childrenOf(PNConstraint.class).stream();
    }
}
