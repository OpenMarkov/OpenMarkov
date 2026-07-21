/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.util;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AbstractIndexedPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class has usefully static methods for testing but it is not a test
 * class.
 */
public class UtilTestMethods {
    
    /**
     * Looks for a variable with the received name.
     *
     * @param variables <code>ArrayList</code> of <code>Variable</code>s.
     * @param name      <code>String</code>.
     * @return First variable in <code>variables</code> with the given
     * <code>name</code> or <code>null</code> if not exist.
     */
    public static @Nullable Variable getVariableName(Collection<? extends Variable> variables, String name) {
        for (Variable variable : variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }
    
    /**
     * @param variablesChoice <code>ArrayList</code> of <code>Variable</code>.
     * @param name            <code>String</code>.
     * @return <code>true</code> if exists a <code>Variables</code> in
     * <code>variablesChoice</code> with the given name.
     */
    public static boolean existsVariableName(Collection<? extends Variable> variablesChoice, String name) {
        for (Variable variable : variablesChoice) {
            if (variable.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Translates the coordinate received in the first two parameters to the
     * variables ordination of the third parameter and returns the configuration
     * value.
     *
     * @param variables           <code>ArrayList</code> of <code>Variable</code>.
     * @param coordinateVariables <code>int[]</code>.
     * @param potential           <code>GTablePotential</code>.
     * @return A configuration of the potential received in the third parameter
     * (an <code>Object</code>).
     */
    public static Object getConfiguration(List<Variable> variables, int[] coordinateVariables,
                                          GTablePotential potential) {
        int position = getConfigurationPosition(variables, coordinateVariables, potential);
        return potential.elementTable.get(position);
    }
    
    /**
     * Translates the coordinate received in the first two parameters to the
     * variables ordination of the third parameter and returns the configuration
     * value.
     *
     * @param variables           <code>ArrayList</code> of <code>Variable</code>.
     * @param coordinateVariables <code>int[]</code>.
     * @param potential           <code>TablePotential</code>.
     * @return A configuration of the potential received in the third parameter
     * (a <code>double</code>).
     */
    public static double getConfiguration(ArrayList<Variable> variables, int[] coordinateVariables,
                                          TablePotential potential) {
        int position = getConfigurationPosition(variables, coordinateVariables, potential);
        return potential.getValues()[position];
    }
    
    /**
     * This method invokes a private method of a class.
     * Use this skeleton to call this method:<p>
     * <code>
     * MyClass instance = new MyClass();<p>
     * String expResult = "Expected Result";<p>
     * Object[] params = {"A String Value", "Another Value"};<p>
     * String result = (String) this.invokePrivateMethod(instance,
     * "myPrivateName", params);<p>
     * assertEquals(expResult, result);<p>
     * </code>
     *
     * @param test       Object of the class that will be checked. <code>Object</code>
     * @param methodName <code>String</code>
     * @param params     <code>Object[]</code>
     * @return The <code>Object</code> returned by the private method.
     * @throws Exception A generic exception because the invoked method can
     *                   fail in unpredictable ways.
     */
    public static Object invokePrivateMethod(Object test, String methodName, Object params[]) throws SecurityException, IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        Object ret = null;
        
        final Method[] methods = test.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                methods[i].setAccessible(true);
                ret = methods[i].invoke(test, params);
                break;
            }
        }
        return ret;
    }
    
    /**
     * Translates the coordinate received in the first two parameters to the
     * variables ordination of the third parameter and returns the position of
     * the configuration.
     *
     * @param variables           <code>ArrayList</code> of <code>Variable</code>.
     * @param coordinateVariables <code>int[]</code>.
     * @param potential           <code>TablePotential</code>.
     * @return The position of the potential received in the third parameter
     * (an <code>double</code>).
     */
    private static int getConfigurationPosition(List<Variable> variables, int[] coordinateVariables,
                                                AbstractIndexedPotential potential) {
        List<Variable> variablesPotential = potential.getVariables();
        int[] coordinate = new int[variablesPotential.size()];
        int i = 0;
        for (Variable variable : variables) {
            coordinate[variablesPotential.indexOf(variable)] = coordinateVariables[i++];
        }
        return potential.getPosition(coordinate);
    }
    
    /**
     * Create a <code>ProbNet</code> with the variables names received. All variables are binary.
     *
     * @param variablesNames <code>String[]</code>
     */
    public static ProbNet createProbNet(String... variablesNames) {
        int numVariables = variablesNames.length;
        ProbNet probNet = new ProbNet();
        Variable[] variables = new Variable[numVariables];
        for (int i = 0; i < numVariables; i++) {
            variables[i] = new Variable(variablesNames[i], "positive", "negative");
            probNet.addNode(variables[i], NodeType.CHANCE);
        }
        return probNet;
    }
    
    /**
     * Adds a link between two variables. The method assumes there are no problems.
     *
     * @param probNet
     * @param variableName1
     * @param variableName2
     * @param directed
     */
    public static void addLink(ProbNet probNet, String variableName1, String variableName2, boolean directed) {
        Variable variable1 = probNet.getVariable(variableName1);
        Variable variable2 = probNet.getVariable(variableName2);
        probNet.addLink(variable1, variable2, directed);
    }
    
    //    /**
    //     * @param variable. <code>Variable</code>
    //     * @param variables. <code>Array</code> of <code>Variable</code>
    //     * @return <code>Array</code> of <code>Variable</code>
    //     */
    public static List<Variable> getListOfVariables(Variable variable, List<Variable> variables) {
        List<Variable> listOfVariables = new ArrayList<Variable>(1 + variables.size());
        listOfVariables.add(variable);
        listOfVariables.addAll(variables);
        return listOfVariables;
    }
}
