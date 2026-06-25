package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Fluent builder for checking multiple {@link PNConstraint}s against a {@link ProbNet}
 * and collecting any violations before throwing a single aggregated exception.
 */
public class ConstraintChecker {
    
    private final HashSet<ConstraintViolatedException> exceptions;
    private final ProbNet probNet;
    
    public ConstraintChecker(ProbNet probNet) {
        this.probNet = probNet;
        this.exceptions = new HashSet<>();
    }
    
    /**
     * Records a constraint violation to be thrown later by {@link #buildAndThrow()}.
     *
     * @param exception the violation to record
     * @return this checker for fluent chaining
     */
    public ConstraintChecker addException(ConstraintViolatedException exception) {
        this.exceptions.add(exception);
        return this;
    }
    
    /**
     * Runs the given check against every constraint of the specified type present in the network.
     *
     * @param constraintClass the constraint type to look up
     * @param checker         the check logic to apply to each constraint instance
     * @param <Constraint>    the constraint type
     * @return this checker for fluent chaining
     */
    public <Constraint extends PNConstraint> ConstraintChecker checkConstraint(Class<Constraint> constraintClass, ConstraintCheck<? super Constraint> checker) {
        Iterator<Constraint> constraints = probNet.getConstraintsOfClass(constraintClass).iterator();
        while (constraints.hasNext()) {
            checker.verify(constraints.next());
        }
        return this;
    }
    
    /**
     * Invokes a custom check that may add violations to the provided list.
     *
     * @param checker consumer that receives a mutable list to add violations to
     * @return this checker for fluent chaining
     */
    public ConstraintChecker check(Consumer<? super ArrayList<ConstraintViolatedException>> checker) {
        ArrayList<ConstraintViolatedException> exceptions = new ArrayList<>();
        checker.accept(exceptions);
        this.exceptions.addAll(exceptions);
        return this;
    }
    
    /**
     * Throws a {@link ConstraintViolatedException} if any violations were recorded.
     * If multiple violations exist, they are wrapped in a
     * {@link ConstraintViolatedException.MultipleConstraintsViolateds}.
     *
     * @throws ConstraintViolatedException if one or more violations were recorded
     */
    public void buildAndThrow() throws ConstraintViolatedException {
        switch (this.exceptions.size()) {
            case 0 -> {
            }
            case 1 -> throw this.exceptions.stream().findFirst().get();
            default -> throw new ConstraintViolatedException.MultipleConstraintsViolateds(this.exceptions);
        }
    }
    
    /**
     * Functional interface for a single constraint verification step.
     *
     * @param <Constraint> the constraint type being checked
     */
    @FunctionalInterface
    public interface ConstraintCheck<Constraint extends PNConstraint> {
        /**
         * Verifies the given constraint instance.
         *
         * @param constraint the constraint to verify
         */
        void verify(Constraint constraint);
    }
    
    
}
