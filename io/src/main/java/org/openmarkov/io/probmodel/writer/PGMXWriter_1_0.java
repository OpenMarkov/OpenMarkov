/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.writer;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;

/**
 * @author Manuel Arias
 * @version 0.5
 */
@FormatType(name = "PGMXWriter0_5", extensions = {"pgmx", "xml"}, description = "OpenMarkov.1.0")
public class PGMXWriter_1_0 extends PGMXWriter_0_2 {
    
    /**
     * Writes the probabilistic network with evidence in PGMX 1.0 format.
     *
     * @param netName   path + network name + extension
     * @param probNet   the probabilistic network to write
     * @param evidences list of evidence cases to include
     */
    @Override
    public void write(String netName, ProbNet probNet, List<EvidenceCase> evidences) throws WriterException.CannotCreateFile, WriterException.TryingToWriteAProbNetWithoutName, WriterException.TryingToWriteANullProbNet {
        formatVersion = "1.0.0";
        super.write(netName, probNet, evidences);
    }
    
    /**
     * Builds all child XML elements of the ProbNet element for version 1.0 format.
     *
     * @param probNet        the probabilistic network
     * @param probNetElement the parent XML element to populate
     */
    @Override protected void getProbNetChildren(ProbNet probNet, Element probNetElement) {
        getAdditionalConstraints(probNet, probNetElement, new Element(XMLTags.ADDITIONAL_CONSTRAINTS.toString()));
        getProbNetComment(probNet, probNetElement, new Element(XMLTags.COMMENT.toString()));
        getDecisionCriteria(probNet, probNetElement, new Element(XMLTags.DECISION_CRITERIA.toString()));
        getAgents(probNet, probNetElement, new Element(XMLTags.AGENTS.toString()));
        getLanguage(probNet, probNetElement, new Element(XMLTags.LANGUAGE.toString()));
        getTemporaUnit(probNet, probNetElement);
        getAdditionalProperties(probNet, probNetElement);
        getVariables(probNet, probNetElement, new Element(XMLTags.VARIABLES.toString()));
        getLinks(probNet, probNetElement, new Element(XMLTags.LINKS.toString()));
        getPotentials(probNet, probNetElement, new Element(XMLTags.POTENTIALS.toString()));
    }
    
    /**
     * @param probNet the prob net
     *            . {@code ProbNet}
     * @param probNetElement the prob net element
     *            . {@code Element}
     */
    
    
    /**
     * Converts a {@code Properties} object into an XML element containing property sub-elements.
     *
     * @param properties the properties to serialize
     *
     * @return the XML element representing the additional properties
     */
    @Override protected Element getPropertiesElement(Properties properties) {
        Element additionalPropertiesElement = new Element(XMLTags.ADDITIONAL_PROPERTIES.toString());
        for (String propertyName : properties.getKeySet()) {
            Element propertyElement = new Element(XMLTags.PROPERTY.toString());
            propertyElement.setAttribute(XMLAttributes.NAME.toString(), propertyName);
            propertyElement.setAttribute(XMLAttributes.VALUE.toString(), properties.get(propertyName).toString());
            additionalPropertiesElement.addContent(propertyElement);
        }
        return additionalPropertiesElement;
    }
    
    
    /**
     * Writes the link restriction potential as an XML sub-element if the link has restrictions.
     *
     * @param link        the link whose restriction is checked
     * @param linkElement the XML element to attach the restriction to
     */
    @Override protected void getLinkRestriction(Link<Node> link, Element linkElement) {
        double[] table = ((TablePotential) link.getRestrictionsPotential()).getValues();
        
        boolean hasRestriction = false;
        for (int i = 0; i < table.length; i++) {
            if (table[i] == 0.0) {
                hasRestriction = true;
                break;
            }
        }
        if (hasRestriction) {
            Potential potential = link.getRestrictionsPotential();
            Element restrictionPotential = new Element(XMLTags.POTENTIAL.toString());
            String potentialType = PotentialUtils.getPotentialName(potential.getClass());
            restrictionPotential.setAttribute(XMLAttributes.TYPE.toString(), potentialType);
            Element variables = new Element(XMLTags.VARIABLES.toString());
            Element variable1 = new Element(XMLTags.VARIABLE.toString());
            variable1.setAttribute(XMLAttributes.NAME.toString(), potential.getVariable(0).getName());
            Element variable2 = new Element(XMLTags.VARIABLE.toString());
            variable2.setAttribute(XMLAttributes.NAME.toString(), potential.getVariable(1).getName());
            variables.addContent(variable1);
            variables.addContent(variable2);
            Element valuesElement = new Element(XMLTags.VALUES.toString())
                    .setText(getValuesInAString(((TablePotential) link.getRestrictionsPotential()).getValues()));
            restrictionPotential.addContent(variables);
            restrictionPotential.addContent(valuesElement);
            linkElement.addContent(restrictionPotential);
        }
    }
    
    /**
     * Writes all potentials (excluding decision node policies) to the XML structure.
     *
     * @param probNet           the probabilistic network
     * @param probNetElement    the parent ProbNet XML element
     * @param potentialsElement the potentials container XML element
     */
    @Override protected void getPotentials(ProbNet probNet, Element probNetElement, Element potentialsElement) {
        // HashMap of declared TablePotentials
        List<Potential> potentials = probNet.getPotentials();
        for (Potential potential : potentials) {
            Variable potentialVariable = potential.getVariable(0);
            // Do not write here policies
            if ((probNet.getNode(potentialVariable).getNodeType() != NodeType.DECISION)
                    && (potential.getPotentialRole() != PotentialRole.POLICY)) {
                Element potentialElement = new Element(XMLTags.POTENTIAL.toString());
                getPotential(probNet, potential, potentialElement);
                potentialsElement.addContent(potentialElement);
            }
        }
        probNetElement.addContent(potentialsElement);
    }
    
    /**
     * Writes a single potential's attributes, variables, and body to the XML element.
     *
     * @param probNet          the probabilistic network context
     * @param potential        the potential to serialize
     * @param potentialElement the XML element to populate
     */
    @Override protected void getPotential(ProbNet probNet, Potential potential, Element potentialElement) {
        getPotentialAttributesAndVariables(potential, potentialElement);
        getPotentialBody(probNet, potential, potentialElement);
    }
    
    /**
     * Sets the potential type attribute and writes the variables element for a potential.
     *
     * @param potential        the potential whose attributes are written
     * @param potentialElement the XML element to populate
     */
    protected static void getPotentialAttributesAndVariables(Potential potential, Element potentialElement) {
        /*
         * TablePotential tablePotential = null;
         * if (potential instanceof ExactDistrPotential) { ExactDistrPotential
         * exactDistrPotential = (ExactDistrPotential) potential; tablePotential =
         * exactDistrPotential.getTablePotential(); } Potential wrapped = tablePotential
         * == null ? potential : tablePotential;
         */
        
        String potentialType = PotentialUtils.getPotentialName(potential.getClass());
        if (potential.getClass() == ExactDistrPotential.class) {
            potentialType = PotentialUtils.getPotentialName(UnivariateDistrPotential.class);
            String distribution = "Exact";
            potentialElement.setAttribute(XMLAttributes.TYPE.toString(), potentialType);
            potentialElement.setAttribute(XMLAttributes.DISTRIBUTION.toString(), distribution);
        } else {
            potentialElement.setAttribute(XMLAttributes.TYPE.toString(), potentialType);
        }
        
        // TODO add function attribute
        
        getPotentialComment(potential, potentialElement);
        
        // TODO add aditionalProperties child
        
        getPotentialVariables(potential, potentialElement);
    }
    
    /**
     * Writes the variables sub-element for a potential if it has any variables.
     *
     * @param potential        the potential whose variables are written
     * @param potentialElement the XML element to attach the variables to
     */
    protected static void getPotentialVariables(Potential potential, Element potentialElement) {
        List<Variable> potentialVariables = potential.getVariables();
        if (!potentialVariables.isEmpty()) {
            writePotentialVariables(potentialVariables, potentialElement);
        }
    }
    
    /**
     * Writes the body of a potential, including univariate distribution potentials.
     *
     * @param probNet          the probabilistic network context
     * @param potential        the potential whose body is written
     * @param potentialElement the XML element to populate
     */
    @Override protected void getPotentialBody(ProbNet probNet, Potential potential, Element potentialElement) {
        super.getPotentialBody(probNet, potential, potentialElement);
        if (potential instanceof UnivariateDistrPotential) { // New from the previous version of the writer
            getUnivariateDistrPotential(potentialElement, (UnivariateDistrPotential) potential);
        }
        if (potential instanceof DistributionTablePotential) {
            // encapsulated in 16/12/2019 - 15/04/2020 added IncrementPotential and Tree With Events Ptential
            getDistributionTablePotential(potential, potentialElement);
        }
        if (potential instanceof TransitionTablePotential) {
            getTransitionTablePotential(potential, potentialElement);
        }
        if (potential instanceof TreeWithEventsPotential) {
            getTreeWithEventsPotential(probNet, potential, potentialElement);
        }
        //14/03/2023 TreeWithExcludedEvents
        if (potential instanceof TreeWithExcludedEventsPotential) {
            getTreeWithExcludedEventsPotential(probNet, potential, potentialElement);
        }
        //18/03/2023 IndicatorPotential
        if (potential instanceof IndicatorPotential) {
            getIndicatorPotential(probNet, potential, potentialElement);
        }
        //25/10/2023
        if (potential instanceof PiecewiseExponentialPotential) {
            getPiecewiseExponentialPotential(probNet, potential, potentialElement);
        }
    }
    
    /**
     * Adds a UnivariateDistrPotential to the XML structure, including distribution
     * parameters and function expressions.
     *
     * @param xmlElement the XML element to attach the distribution data to
     * @param potential  the univariate distribution potential to serialize
     */
    protected void getUnivariateDistrPotential(Element xmlElement, UnivariateDistrPotential potential) {
        xmlElement.setAttribute(XMLAttributes.DISTRIBUTION.toString(), potential.getProbDensFunctionUnivariateName());
        xmlElement.setAttribute(XMLAttributes.PARAMETRIZATION.toString(), potential.getProbDensFunctionParametrizationName());
        Element parametersElement = new Element(XMLTags.PARAMETERS.toString());
        parametersElement.setText(getValuesInAString(potential.getDistributionTable().getValues()));
        getAugmentedProbTablePotential(xmlElement, potential.getAugmentedProbTable());
        // Write table values to the XML file
        xmlElement.addContent(parametersElement);
    }
    
    /**
     * Writes the function expressions of an augmented probability table to the XML structure.
     *
     * @param xmlElement         the XML element to attach the functions to
     * @param AugmentedProbTable the augmented probability table containing function values
     */
    @Override protected void getAugmentedProbTablePotential(Element xmlElement, AugmentedProbTable AugmentedProbTable) {
        Element parametersElement = new Element(XMLTags.FUNCTIONS.toString());
        
        VariableExpression[] functionValues = AugmentedProbTable.getFunctionValues();
        for (VariableExpression function : functionValues) {
            parametersElement.addContent("\"" + function.asStringExpression() + "\" ");
        }
        // Write table values to the XML file
        xmlElement.addContent(parametersElement);
    }
    
    
    //  16/12/2019 - 24/05/2022 DistributionTablePotential renamed from TimeToEventTablePotential - 14/08/2022 impossibleConfiguration removed
    
    /**
     * This method writes a DistributionTablePotential in the .pgmx
     *
     * @param potential
     * @param potentialElement
     */
    private void getTransitionTablePotential(Potential potential, Element potentialElement) {
        getValuesTablePotential(((TransitionTablePotential) potential).getTablePotential(), potentialElement);
        //14/08/2022 impossibleConfiguration removed
//		if (	((TransitionTablePotential) potential).hasImpossibleConfiguration()){
//			writeImpossibleConfiguration(((TransitionTablePotential) potential).getImpossibleConfigurations(), potentialElement);
//		}
    
    }
    
    /**
     * This method writes a TransitionTablePotential in the .pgmx
     *
     * @param potential
     * @param potentialElement
     */
    private void getDistributionTablePotential(Potential potential, Element potentialElement) {
        DistributionTablePotential distributionTablePotential = (DistributionTablePotential) potential;
        Element dElement = new Element(XMLTags.VALUE.toString());
        dElement.setAttribute(XMLAttributes.DISTRIBUTION.toString(), distributionTablePotential.getDistributionName());
        //08/10/2020
        dElement.setAttribute(XMLAttributes.PARAMETRIZATION.toString(), distributionTablePotential.getParametrizationName());
        potentialElement.addContent(dElement);
        getValuesTablePotential(distributionTablePotential.getTableWithEvents().getTablePotential(), potentialElement);
        
        //15/12/2019 Added Function Values to DistributionTablePotential (24/05/2022 renamed from TimeToEventTablePotential), TODO check if these functions will be extended to every TableWithEvents
        if (distributionTablePotential.hasFunctionValues()) {
            writeTableWithFunctions(distributionTablePotential, potentialElement);
        }
        //14/08/2022 impossibleConfiguration removed
//		if (	((DistributionTablePotential) potential).hasImpossibleConfiguration()){
//			writeImpossibleConfiguration(((DistributionTablePotential) potential).getImpossibleConfigurations(), potentialElement);
//		}
    
    }
    //
    //  09/2019
//	/**
//	 * TODO Look at Evidence Case and add
//	 * Write impossible configurations for Potentials implementing the ImpossibleConfiguration interface
//	 * @param impossibleConfigurations
//	 */
//	private void writeImpossibleConfiguration(ArrayList<Configuration> impossibleConfigurations,Element potentialElement) {
//		Element icElements = new Element("ImpossibleConfigurations");
//		for (Configuration iConfiguration:impossibleConfigurations){
//			Element icElement = new Element("ImpossibleConfiguration");
//			for (Finding finding: iConfiguration.getFindings()){
//				Element findingElement = new Element(XMLTags.FINDING.toString());
//				findingElement.setAttribute("variable", finding.getVariable().getName());
//				if (finding.getVariable().getVariableType() == VariableType.FINITE_STATES) {
//					findingElement.setAttribute("state", finding.getState());
//					} else {
//						findingElement.setAttribute("numericValue", new Double(finding.getNumericalValue()).toString());
//					}
//
//				icElement.addContent(findingElement);
//			}
//			icElements.addContent(icElement);
//		}
//		potentialElement.addContent(icElements);
//	}
    //
    
    // 15/12/2019
    
    /**
     * Writes the function values of a DistributionTablePotential
     *
     * @param distributionTablePotential
     */
    private void writeTableWithFunctions(DistributionTablePotential distributionTablePotential, Element potentialElement) {
        //TODO move "FunctionValues"
        Element functionValuesElement = new Element("FunctionValues");
        
        String[] functionValues = distributionTablePotential.getTableWithEvents()
                                                            .getTableWithFunctions()
                                                            .getFunctionValues();
        for (int i = 0; i < functionValues.length; i++) {
            functionValuesElement.addContent(getFunctionElement(functionValues[i]));
        }
        potentialElement.addContent(functionValuesElement);
    }
    
    //
    
    
    // 15/04/2020
    
    
    private void getTreeWithEventsPotential(ProbNet probNet, Potential potential, Element potentialElement) {
        TreeWithEventsPotential treeWithEventsPotential = (TreeWithEventsPotential) potential;
        List<Variable> events = treeWithEventsPotential.getEvents();
        for (Variable event : events) {
            try {
                getTreeAddPotential(treeWithEventsPotential.getTree(event), potentialElement, probNet);
            } catch (IncompatibleEvidenceException.VariableMustBeEvent e) {
                throw new UnrecoverableException(e);
            }
        }
    }
    
    //
    // 14/03/2023
    private void getTreeWithExcludedEventsPotential(ProbNet probNet, Potential potential, Element potentialElement) {
        TreeWithExcludedEventsPotential treeWithExcludedEventsPotential = (TreeWithExcludedEventsPotential) potential;
        List<Variable> events = treeWithExcludedEventsPotential.getEvents();
        getTreeAddPotential(treeWithExcludedEventsPotential.getNoEventTree(), potentialElement, probNet);
        
    }
    //
    
    // 18/03/2023
    private void getIndicatorPotential(ProbNet probNet, Potential potential, Element potentialElement) {
        IndicatorPotential indicatorPotential = (IndicatorPotential) potential;
        Element tteElement = new Element(XMLTags.TTE.toString());
        tteElement.setText(String.valueOf(indicatorPotential.getTte()));
        potentialElement.addContent(tteElement);
        Element pOccurrenceElement = new Element(XMLTags.P_OCCURRENCE.toString());
        pOccurrenceElement.setText(String.valueOf(indicatorPotential.getpOccurrence()));
        potentialElement.addContent(pOccurrenceElement);
    }
    
    //
    
    // 25/10/2023
    private void getPiecewiseExponentialPotential(ProbNet probNet, Potential potential, Element potentialElement) {
        PiecewiseExponentialPotential piecewiseExponentialPotential = (PiecewiseExponentialPotential) potential;
        Element initValueFunctionElement = getFunctionElement(piecewiseExponentialPotential.getInitTimeFunction()
                                                                                           .getFunction()
                                                                                           .asStringExpression());
        potentialElement.addContent(initValueFunctionElement);
        Element useRatesElement = new Element(XMLTags.RATES.toString());
        useRatesElement.setText(String.valueOf(piecewiseExponentialPotential.isUseRates()));
        potentialElement.addContent(useRatesElement);
        Element lifeTableValuesElement = new Element(XMLTags.SLICES.toString());
        lifeTableValuesElement.addContent(Arrays.toString(piecewiseExponentialPotential.getPiecewiseTable()
                                                                                       .keySet()
                                                                                       .toArray(new Double[0]))
                                                .replaceAll("[\\[\\],]", ""));
        potentialElement.addContent(lifeTableValuesElement);
        Element lifeTableProbElement = new Element(XMLTags.VALUES.toString());
        lifeTableProbElement.addContent(Arrays.toString(piecewiseExponentialPotential.getPiecewiseTable()
                                                                                     .values()
                                                                                     .toArray(new Double[0]))
                                              .replaceAll("[\\[\\],]", ""));
        potentialElement.addContent(lifeTableProbElement);
    }
    //
    
    // 28/08/2023 writing Monte Carlo options for DES
    
    /**
     * Writes Inference Options in the PGMX
     * <p>
     * FIXME to be merged with super class when DESnets are integrated
     *
     * @param probNet
     * @param root
     */
    @Override
    protected void writeInferenceOptions(ProbNet probNet, Element root) {
        Element inferenceOptionsElement = new Element(XMLTags.INFERENCE_OPTIONS.toString());
        
        if (probNet.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType() != null) {
            getMulticriteriaOptions(probNet, inferenceOptionsElement);
        }
        
        if (!probNet.hasConstraintOfClass(OnlyAtemporalVariables.class)) {
            getTemporalOptions(probNet, inferenceOptionsElement);
        }
        // Added; the rest is the same code in the parent class
        if (probNet.getNetworkType() instanceof DESNetworkType) {
            getMonteCarloOptions(probNet, inferenceOptionsElement);
        }
        //
        root.addContent(inferenceOptionsElement);
        
    }
    
    /**
     * Writes Monte Carlo Options for DESnets in the PGMX
     *
     * @param probNet
     * @param inferenceOptionsElement
     */
    protected void getMonteCarloOptions(ProbNet probNet, Element inferenceOptionsElement) {
        Element monteCarloOptions = new Element(XMLTags.MONTE_CARLO_OPTIONS.toString());
        
        Element numSimulations = new Element(XMLTags.NUMBER_OF_SIMULATIONS.toString());
        numSimulations.addContent(String.valueOf(probNet.getInferenceOptions()
                                                        .getMonteCarloOptions()
                                                        .getNumSimulations()));
        monteCarloOptions.addContent(numSimulations);
        
        Element numSeries = new Element(XMLTags.NUMBER_OF_SERIES.toString());
        numSeries.addContent(String.valueOf(probNet.getInferenceOptions().getMonteCarloOptions().getNumSeries()));
        monteCarloOptions.addContent(numSeries);
        
        if (probNet.getInferenceOptions().getMonteCarloOptions().getInputFilePath() != null) {
            Element inputFile = new Element(XMLTags.INPUT_DATA_FILE.toString());
            inputFile.addContent(probNet.getInferenceOptions().getMonteCarloOptions().getInputFilePath().toString());
            monteCarloOptions.addContent(inputFile);
        }
        inferenceOptionsElement.addContent(monteCarloOptions);
    }
    
    
    // 25/10/2020 - Method overridden to add property to events in DESNets
    
    
    /**
     * @param variableElement
     * @param additionalElement
     * @param node
     */
    @Override
    protected void getAdditionalProperties(Element variableElement, Element additionalElement, Node node) {
        super.getAdditionalProperties(variableElement, additionalElement, node);
        if ((node.getNodeType() == NodeType.EVENT)) {
            if (additionalElement.getParent() == null) variableElement.addContent(additionalElement);
            Element propertyElement = new Element(XMLTags.PROPERTY.toString());
            propertyElement.setAttribute(XMLAttributes.NAME.toString(), XMLTags.ALWAYS_APPEND.toString());
            propertyElement.setAttribute(XMLAttributes.VALUE.toString(), Boolean.toString(node.isAlwaysAppend()));
            additionalElement.addContent(propertyElement);
        }
    }
    
}