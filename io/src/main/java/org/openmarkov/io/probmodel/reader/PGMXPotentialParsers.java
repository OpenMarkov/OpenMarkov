/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import org.jdom2.Element;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.model.network.potential.canonical.TuningPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.strings.XMLTags;

import java.util.Arrays;
import java.util.List;

/**
 * Static parser methods for all potential types supported by PGMX format version 0.2.
 * <p>
 * Each method is a {@link PotentialParser} implementation registered in
 * {@link PGMXReader_0_2#buildPotentialParsers()}. Methods that need access to the
 * reader's dispatch mechanism (e.g., {@code TreeADDPotential}) remain as instance
 * methods in the reader itself.
 *
 * @author Manuel Arias
 * @see PGMXReader_0_2
 * @see PotentialParser
 */
public class PGMXPotentialParsers {

    private PGMXPotentialParsers() {}

    // -------------------------------------------------------------------------
    // Simple structural potentials
    // -------------------------------------------------------------------------

    public static UniformPotential getUniformPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                        List<Variable> variables) {
        return new UniformPotential(variables, xmlRole);
    }

    public static ProductPotential getProductPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                        List<Variable> variables) {
        return new ProductPotential(variables, xmlRole);
    }

    public static Potential getSumPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                             List<Variable> variables) {
        return new SumPotential(variables, xmlRole);
    }

    public static SameAsPrevious getSameAsPrevious(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                    List<Variable> variables)
            throws PGMXParserException.CannotAssignPotentialToStaticVariable {
        if (!variables.getFirst().isTemporal()) {
            throw new PGMXParserException.CannotAssignPotentialToStaticVariable("SameAsPrevious", xmlPotential);
        }
        return new SameAsPrevious(variables);
    }

    public static CycleLengthShift getCycleLengthShiftPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                                  List<Variable> variables)
            throws PGMXParserException.CannotAssignPotentialToStaticVariable {
        Variable variable = variables.getFirst();
        if (!variable.isTemporal()) {
            throw new PGMXParserException.CannotAssignPotentialToStaticVariable("CycleLengthShift", xmlPotential);
        }
        return new CycleLengthShift(variables, probNet.getCycleLength());
    }

    // -------------------------------------------------------------------------
    // Table-based potentials
    // -------------------------------------------------------------------------

    public static TablePotential getTablePotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                    List<Variable> variables)
            throws PGMXParserException.WrongNumberOfValues {
        Element xmlRootTable = xmlPotential.getChild(XMLTags.VALUES.toString());
        Element xmlRootUncertainValues = xmlPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString());
        Element xmlRootUncertainParameters = xmlPotential.getChild(XMLTags.UNCERTAIN_PARAMETERS.toString());
        boolean hasUncertainty = xmlRootUncertainValues != null || xmlRootUncertainParameters != null;
        TablePotential tablePotential;
        if (xmlRootTable != null) {
            double[] table = PGMXReader_0_2.parseDoubles(xmlRootTable.getTextNormalize());
            PGMXReader_0_2.checkNumberOfValues(table.length, AbstractIndexedPotential.computeTableSize(variables),
                                               PGMXReader_0_2.relationOf(variables), xmlRootTable);
            tablePotential = hasUncertainty
                    ? new UncertainTablePotential(variables, xmlRole, table)
                    : new TablePotential(variables, xmlRole, table);
        } else {
            tablePotential = hasUncertainty
                    ? new UncertainTablePotential(variables, xmlRole)
                    : new TablePotential(variables, xmlRole);
        }
        if (xmlRootUncertainValues != null) {
            tablePotential.setUncertainValues(PGMXReader_0_2.getUncertainValues(xmlRootUncertainValues));
        } else if (xmlRootUncertainParameters != null) {
            tablePotential.setUncertainValues(PGMXReader_0_2.getUncertainValues(xmlRootUncertainParameters));
        }
        return tablePotential;
    }

    public static ExactDistrPotential getExactDistrPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                              List<Variable> variables)
            throws PGMXParserException.WrongNumberOfValues {
        Element xmlRootTable = xmlPotential.getChild(XMLTags.VALUES.toString());
        ExactDistrPotential exactDistrPotential = new ExactDistrPotential(variables, xmlRole);
        // A table without numbers is left with its zeros; TablesAFileLeavesOut tells of it
        if (xmlRootTable != null) {
            double[] table = PGMXReader_0_2.parseDoubles(xmlRootTable.getTextNormalize());
            PGMXReader_0_2.checkNumberOfValues(table.length, exactDistrPotential.getTablePotential().getValues().length,
                                               PGMXReader_0_2.relationOf(variables), xmlRootTable);
            exactDistrPotential.getTablePotential().setValues(table);
        }
        Element xmlRootUncertainParameters = xmlPotential.getChild(XMLTags.UNCERTAIN_PARAMETERS.toString());
        Element xmlRootUncertainValues = xmlPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString());
        if (xmlRootUncertainValues != null) {
            exactDistrPotential.setUncertainValues(PGMXReader_0_2.getUncertainValues(xmlRootUncertainValues));
        } else if (xmlRootUncertainParameters != null) {
            exactDistrPotential.setUncertainValues(PGMXReader_0_2.getUncertainValues(xmlRootUncertainParameters));
        }
        return exactDistrPotential;
    }

    // -------------------------------------------------------------------------
    // ICI (canonical form) potentials
    // -------------------------------------------------------------------------

    public static Potential getICIPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                             List<Variable> variables)
            throws PGMXParserException.DataMissing, PGMXParserException.UnknownVariable,
                   PGMXParserException.WrongNumberOfValues {
        Element xmlModel = xmlPotential.getChild(XMLTags.MODEL.toString());
        ICIPotential iciPotential = null;
        if (xmlModel.getText().equals(PotentialUtils.getPotentialName(MaxPotential.class))
                || xmlModel.getText().equals("GeneralizedMax")) {
            iciPotential = new MaxPotential(variables);
        } else if (xmlModel.getText().equals(PotentialUtils.getPotentialName(MinPotential.class))
                || xmlModel.getText().equals("GeneralizedMin")) {
            iciPotential = new MinPotential(variables);
        } else if (xmlModel.getText().equals(PotentialUtils.getPotentialName(TuningPotential.class))) {
            iciPotential = new TuningPotential(variables);
        }
        for (Element subpotential : xmlPotential.getChild(XMLTags.SUBPOTENTIALS.toString()).getChildren()) {
            List<Element> subpotentialVariables = subpotential.getChild(XMLTags.VARIABLES.toString()).getChildren();
            Element xmlValues = subpotential.getChild(XMLTags.VALUES.toString());
            double[] values = PGMXReader_0_2.parseDoubles(xmlValues.getTextNormalize());
            String relation = PGMXReader_0_2.relationOf(variables);
            if (subpotentialVariables.size() > 1) {
                Variable variable = PGMXReader_0_2.getVariable(subpotentialVariables.get(1), probNet, relation);
                PGMXReader_0_2.checkNumberOfValues(values.length, iciPotential.noisyParametersSizes(variable),
                                                   relation, xmlValues);
                iciPotential.setNoisyParameters(variable, values);
            } else {
                PGMXReader_0_2.getVariable(subpotentialVariables.getFirst(), probNet, relation);
                PGMXReader_0_2.checkNumberOfValues(values.length, iciPotential.leakyParametersSize(), relation,
                                                   xmlValues);
                iciPotential.setLeakyParameters(values);
            }
        }
        return iciPotential;
    }

    // -------------------------------------------------------------------------
    // Hazard / GLM potentials
    // -------------------------------------------------------------------------

    public static Potential getWeibullPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                 List<Variable> variables)
            throws PGMXParserException.DataMissing, PGMXParserException.UnknownVariable,
                   PGMXParserException.WrongNumberOfValues {
        WeibullHazardPotential potential = new WeibullHazardPotential(variables, xmlRole);
        Element xmlTimeVariable = xmlPotential.getChild(XMLTags.TIME_VARIABLE.toString());
        if (xmlTimeVariable != null) {
            Variable timeVariable = PGMXReader_0_2.getVariable(xmlTimeVariable, probNet,
                                                               PGMXReader_0_2.relationOf(variables));
            potential.setTimeVariable(timeVariable);
        }
        Element xmlLog = xmlPotential.getChild(XMLTags.LOG.toString());
        potential.setLog(xmlLog == null || Boolean.parseBoolean(xmlLog.getValue()));
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    public static Potential getExponentialHazardPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                           List<Variable> variables)
            throws PGMXParserException.WrongNumberOfValues {
        ExponentialHazardPotential potential = new ExponentialHazardPotential(variables, xmlRole);
        Element xmlLog = xmlPotential.getChild(XMLTags.LOG.toString());
        potential.setLog(xmlLog == null || Boolean.parseBoolean(xmlLog.getValue()));
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    public static Potential getExponentialPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                     List<Variable> variables)
            throws PGMXParserException.WrongNumberOfValues {
        ExponentialPotential potential = new ExponentialPotential(variables, xmlRole);
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    public static Potential getLinearRegressionPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                          List<Variable> variables)
            throws PGMXParserException.WrongNumberOfValues {
        LinearCombinationPotential potential = new LinearCombinationPotential(variables, xmlRole);
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    static void getRegressionPotential(Element xmlPotential, GLMPotential potential, List<Variable> variables)
            throws PGMXParserException.WrongNumberOfValues {
        String relation = PGMXReader_0_2.relationOf(variables);
        Element xmlCovariates = xmlPotential.getChild(XMLTags.COVARIATES.toString());
        if (xmlCovariates != null) {
            potential.setCovariates(PGMXReader_0_2.getCovariates(xmlCovariates, variables));
        }
        // One coefficient per covariate
        Element xmlCoefficients = xmlPotential.getChild(XMLTags.COEFFICIENTS.toString());
        double[] coefficients = PGMXReader_0_2.parseDoubles(xmlCoefficients.getText());
        PGMXReader_0_2.checkNumberOfValues(coefficients.length, potential.getCovariates().length, relation,
                                           xmlCoefficients);
        potential.setCoefficients(coefficients);
        // The lower triangle of a square matrix with a row per coefficient
        int triangle = coefficients.length * (coefficients.length + 1) / 2;
        Element xmlCovarianceMatrix = xmlPotential.getChild(XMLTags.COVARIANCE_MATRIX.toString());
        Element xmlCholeskyDecomposition = xmlPotential.getChild(XMLTags.CHOLESKY_DECOMPOSITION.toString());
        if (xmlCovarianceMatrix != null) {
            double[] matrix = PGMXReader_0_2.parseDoubles(xmlCovarianceMatrix.getText());
            PGMXReader_0_2.checkNumberOfValues(matrix.length, triangle, relation, xmlCovarianceMatrix);
            potential.setCovarianceMatrix(matrix);
        } else if (xmlCholeskyDecomposition != null) {
            double[] matrix = PGMXReader_0_2.parseDoubles(xmlCholeskyDecomposition.getText());
            PGMXReader_0_2.checkNumberOfValues(matrix.length, triangle, relation, xmlCholeskyDecomposition);
            potential.setCholeskyDecomposition(matrix);
        }
    }

    // -------------------------------------------------------------------------
    // Function / expression potentials
    // -------------------------------------------------------------------------

    public static Potential getFunctionPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                  List<Variable> variables) {
        FunctionPotential potential = new FunctionPotential(variables, xmlRole);
        Element xmlFunction = xmlPotential.getChild(XMLTags.FUNCTION.toString());
        potential.setFunction(new VariableExpression(variables, xmlFunction.getText()));
        return potential;
    }

    // -------------------------------------------------------------------------
    // Discrete-value potentials
    // -------------------------------------------------------------------------

    public static Potential getDeltaPotential(Element xmlPotential, ProbNet probNet, PotentialRole role,
                                               List<Variable> variables)
            throws PGMXParserException.DeltaPotentialWithoutState, PGMXParserException.FoundUnknownState {
        Element xmlNumericValue = xmlPotential.getChild(XMLTags.NUMERIC_VALUE.toString());
        Element xmlState = xmlPotential.getChild(XMLTags.STATE.toString());
        Element xmlStateIndex = xmlPotential.getChild(XMLTags.STATE_INDEX.toString());
        if (xmlNumericValue != null) {
            double value = PGMXReader_0_2.finiteNumber(xmlNumericValue.getText());
            return new DeltaPotential(variables, role, value);
        } else if (xmlState != null) {
            return new DeltaPotential(variables, role, stateNamed(variables.getFirst(), xmlState));
        } else if (xmlStateIndex != null) {
            int stateIndex = PGMXReader_0_2.parseInteger(xmlStateIndex.getText());
            State state = variables.getFirst().getStates()[stateIndex];
            return new DeltaPotential(variables, role, state);
        }
        throw new PGMXParserException.DeltaPotentialWithoutState(role, xmlPotential);
    }

    /**
     * @return the state of {@code variable} that {@code xmlState} names; the writer formats the file trimming the
     * blanks at the ends of a text, so a state whose name has them is also found without them
     */
    private static State stateNamed(Variable variable, Element xmlState) throws PGMXParserException.FoundUnknownState {
        String name = xmlState.getText();
        State state = variable.getState(name);
        if (state == null) {
            state = Arrays.stream(variable.getStates()).filter(s -> s.getName().trim().equals(name.trim()))
                          .findFirst().orElse(null);
        }
        if (state == null) {
            throw new PGMXParserException.FoundUnknownState(variable, name, xmlState);
        }
        return state;
    }

    /** @author carmenyago */
    public static Potential getBinomialPotential(Element xmlPotential, ProbNet probNet, PotentialRole role,
                                                  List<Variable> variables)
            throws PGMXParserException.BinomialPotentialMissingCasesAndProbabilities {
        Element xmlNumberOfCases = xmlPotential.getChild(XMLTags.NUMBER_OF_CASES.toString());
        Element xmlTheta = xmlPotential.getChild(XMLTags.THETA.toString());
        if ((xmlNumberOfCases != null) && (xmlTheta != null)) {
            int N = PGMXReader_0_2.parseInteger(xmlNumberOfCases.getText());
            double theta = PGMXReader_0_2.finiteNumber(xmlTheta.getText());
            return new BinomialPotential(variables, role, N, theta);
        }
        throw new PGMXParserException.BinomialPotentialMissingCasesAndProbabilities(role, xmlPotential);
    }
}
