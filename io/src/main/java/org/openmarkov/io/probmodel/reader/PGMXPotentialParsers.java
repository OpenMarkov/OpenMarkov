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
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

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
    // Internal helpers (mirrors of PGMXReader_0_2 static helpers)
    // -------------------------------------------------------------------------

    private static Variable getVariable(Element element, ProbNet probNet) {
        String variableName = element.getAttributeValue(XMLAttributes.NAME.toString());
        String timeSlice = element.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
        variableName = variableName.replace(" [" + timeSlice + "]", "");
        return (timeSlice == null) ? probNet.getVariable(variableName)
                : probNet.getVariable(variableName, Integer.parseInt(timeSlice));
    }

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
                                                    List<Variable> variables) {
        Element xmlRootTable = xmlPotential.getChild(XMLTags.VALUES.toString());
        Element xmlRootUncertainValues = xmlPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString());
        Element xmlRootUncertainParameters = xmlPotential.getChild(XMLTags.UNCERTAIN_PARAMETERS.toString());
        boolean hasUncertainty = xmlRootUncertainValues != null || xmlRootUncertainParameters != null;
        TablePotential tablePotential;
        if (xmlRootTable != null) {
            double[] table = PGMXReader_0_2.parseDoubles(xmlRootTable.getTextNormalize());
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
                                                              List<Variable> variables) {
        Element xmlRootTable = xmlPotential.getChild(XMLTags.VALUES.toString());
        double[] table = PGMXReader_0_2.parseDoubles(xmlRootTable.getTextNormalize());
        ExactDistrPotential exactDistrPotential = new ExactDistrPotential(variables, xmlRole, table);
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
                                             List<Variable> variables) {
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
            double[] values = PGMXReader_0_2.parseDoubles(subpotential.getChild(XMLTags.VALUES.toString()).getTextNormalize());
            if (subpotentialVariables.size() > 1) {
                Variable variable = getVariable(subpotentialVariables.get(1), probNet);
                iciPotential.setNoisyParameters(variable, values);
            } else {
                getVariable(subpotentialVariables.getFirst(), probNet);
                iciPotential.setLeakyParameters(values);
            }
        }
        return iciPotential;
    }

    // -------------------------------------------------------------------------
    // Hazard / GLM potentials
    // -------------------------------------------------------------------------

    public static Potential getWeibullPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                 List<Variable> variables) {
        WeibullHazardPotential potential = new WeibullHazardPotential(variables, xmlRole);
        Element xmlTimeVariable = xmlPotential.getChild(XMLTags.TIME_VARIABLE.toString());
        if (xmlTimeVariable != null) {
            String variableName = xmlTimeVariable.getAttributeValue(XMLAttributes.NAME.toString());
            String timeSlice = xmlTimeVariable.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
            Variable timeVariable = probNet.getVariable(variableName, Integer.parseInt(timeSlice));
            potential.setTimeVariable(timeVariable);
        }
        Element xmlLog = xmlPotential.getChild(XMLTags.LOG.toString());
        potential.setLog(xmlLog == null || Boolean.parseBoolean(xmlLog.getValue()));
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    public static Potential getExponentialHazardPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                           List<Variable> variables) {
        ExponentialHazardPotential potential = new ExponentialHazardPotential(variables, xmlRole);
        Element xmlLog = xmlPotential.getChild(XMLTags.LOG.toString());
        potential.setLog(xmlLog == null || Boolean.parseBoolean(xmlLog.getValue()));
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    public static Potential getExponentialPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                     List<Variable> variables) {
        ExponentialPotential potential = new ExponentialPotential(variables, xmlRole);
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    public static Potential getLinearRegressionPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                          List<Variable> variables) {
        LinearCombinationPotential potential = new LinearCombinationPotential(variables, xmlRole);
        getRegressionPotential(xmlPotential, potential, variables);
        return potential;
    }

    static void getRegressionPotential(Element xmlPotential, GLMPotential potential, List<Variable> variables) {
        Element xmlCoefficients = xmlPotential.getChild(XMLTags.COEFFICIENTS.toString());
        potential.setCoefficients(PGMXReader_0_2.parseDoubles(xmlCoefficients.getText()));
        Element xmlCovariates = xmlPotential.getChild(XMLTags.COVARIATES.toString());
        if (xmlCovariates != null) {
            potential.setCovariates(PGMXReader_0_2.getCovariates(xmlCovariates, variables));
        }
        Element xmlCovarianceMatrix = xmlPotential.getChild(XMLTags.COVARIANCE_MATRIX.toString());
        Element xmlCholeskyDecomposition = xmlPotential.getChild(XMLTags.CHOLESKY_DECOMPOSITION.toString());
        if (xmlCovarianceMatrix != null) {
            potential.setCovarianceMatrix(PGMXReader_0_2.parseDoubles(xmlCovarianceMatrix.getText()));
        } else if (xmlCholeskyDecomposition != null) {
            potential.setCholeskyDecomposition(PGMXReader_0_2.parseDoubles(xmlCholeskyDecomposition.getText()));
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
            throws PGMXParserException.DeltaPotentialWithoutState {
        Element xmlNumericValue = xmlPotential.getChild(XMLTags.NUMERIC_VALUE.toString());
        Element xmlState = xmlPotential.getChild(XMLTags.STATE.toString());
        Element xmlStateIndex = xmlPotential.getChild(XMLTags.STATE_INDEX.toString());
        if (xmlNumericValue != null) {
            double value = Double.parseDouble(xmlNumericValue.getText());
            return new DeltaPotential(variables, role, value);
        } else if (xmlState != null) {
            State state = variables.getFirst().getState(xmlState.getText());
            return new DeltaPotential(variables, role, state);
        } else if (xmlStateIndex != null) {
            int stateIndex = Integer.parseInt(xmlStateIndex.getText());
            State state = variables.getFirst().getStates()[stateIndex];
            return new DeltaPotential(variables, role, state);
        }
        throw new PGMXParserException.DeltaPotentialWithoutState(role, xmlPotential);
    }

    /** @author carmenyago */
    public static Potential getBinomialPotential(Element xmlPotential, ProbNet probNet, PotentialRole role,
                                                  List<Variable> variables)
            throws PGMXParserException.BinomialPotentialMissingCasesAndProbabilities {
        Element xmlNumberOfCases = xmlPotential.getChild(XMLTags.NUMBER_OF_CASES.toString());
        Element xmlTheta = xmlPotential.getChild(XMLTags.THETA.toString());
        if ((xmlNumberOfCases != null) && (xmlTheta != null)) {
            int N = Integer.parseInt(xmlNumberOfCases.getText());
            double theta = Double.parseDouble(xmlTheta.getText());
            return new BinomialPotential(variables, role, N, theta);
        }
        throw new PGMXParserException.BinomialPotentialMissingCasesAndProbabilities(role, xmlPotential);
    }

    // -------------------------------------------------------------------------
    // Stubs (implementations pending)
    // -------------------------------------------------------------------------

    /**
     * Parses a {@code <Potential type="Conditional Gaussian">} element into a
     * {@link ConditionalGaussianPotential}.
     * <p>
     * Implemented as part of refactor A.1 so the paper describing continuous
     * extensions in OpenMarkov can be reproduced end-to-end: the writer side
     * already assumes the reader recovers a CGP with its mean and variance
     * sub-potentials, and the stub this replaces returned {@code null}, which
     * produced silent {@code NullPointerException}s downstream of
     * {@code PGMXReader_1_0} and blocked the fixture {@code
     * BN-conditional-gaussian.pgmx} from round-tripping.
     * <p>
     * Expected XML shape (see {@code src/test/resources/BN-conditional-gaussian.pgmx}):
     * <pre>
     *   &lt;Potential type="Conditional Gaussian" role="conditionalProbability"&gt;
     *     &lt;Variables&gt;...&lt;/Variables&gt;
     *     &lt;Mean&gt;&lt;Potential type="Table"&gt;...&lt;/Potential&gt;&lt;/Mean&gt;
     *     &lt;Variance&gt;&lt;Potential type="Table"&gt;...&lt;/Potential&gt;&lt;/Variance&gt;
     *   &lt;/Potential&gt;
     * </pre>
     * Nested sub-potentials are read as {@link TablePotential} to match the
     * defaults used by {@link ConditionalGaussianPotential}'s main constructor.
     */
    public static Potential getConditionalGaussianPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                             List<Variable> variables) {
        ConditionalGaussianPotential cg = new ConditionalGaussianPotential(variables, xmlRole);
        cg.setMean(readNestedTablePotential(xmlPotential, XMLTags.MEAN, probNet));
        cg.setVariance(readNestedTablePotential(xmlPotential, XMLTags.VARIANCE, probNet));
        return cg;
    }

    private static TablePotential readNestedTablePotential(Element parent, XMLTags childTag, ProbNet probNet) {
        Element wrapper = parent.getChild(childTag.toString());
        Element nestedPotential = wrapper.getChild(XMLTags.POTENTIAL.toString());
        List<Variable> nestedVariables = new java.util.ArrayList<>();
        for (Element v : nestedPotential.getChild(XMLTags.VARIABLES.toString()).getChildren()) {
            nestedVariables.add(getVariable(v, probNet));
        }
        return getTablePotential(nestedPotential, probNet, PotentialRole.CONDITIONAL_PROBABILITY, nestedVariables);
    }

    /**
     * Parses a {@code <Potential type="Discretized Cauchy">} element into a
     * {@link DiscretizedCauchyPotential}.
     * <p>
     * Implemented as part of refactor A.2 so the paper describing continuous
     * extensions can be reproduced end-to-end: the stub this replaces returned
     * {@code null}, which produced silent {@code NullPointerException}s
     * downstream of {@code PGMXReader_1_0} and blocked the fixture
     * {@code BN-discretized-cauchy.pgmx} from round-tripping.
     * <p>
     * Expected XML shape (see {@code src/test/resources/BN-discretized-cauchy.pgmx}):
     * <pre>
     *   &lt;Potential type="Discretized Cauchy" role="conditionalProbability"&gt;
     *     &lt;Variables&gt;...&lt;/Variables&gt;
     *     &lt;Median&gt;&lt;Potential type="Table"&gt;...&lt;/Potential&gt;&lt;/Median&gt;
     *     &lt;Scale&gt;&lt;Potential type="Table"&gt;...&lt;/Potential&gt;&lt;/Scale&gt;
     *   &lt;/Potential&gt;
     * </pre>
     * Nested sub-potentials are read as {@link TablePotential} to match the
     * defaults used by {@link DiscretizedCauchyPotential}'s main constructor.
     */
    public static Potential getDiscretizedCauchyPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                           List<Variable> variables) {
        DiscretizedCauchyPotential dc = new DiscretizedCauchyPotential(variables, xmlRole);
        dc.setMedian(readNestedTablePotential(xmlPotential, XMLTags.MEDIAN, probNet));
        dc.setScale(readNestedTablePotential(xmlPotential, XMLTags.SCALE, probNet));
        return dc;
    }
}
