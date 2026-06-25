/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.*;

import java.util.Map;

import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

/**
 * Reader for the PGMX format version 1.0. Extends {@link PGMXReader_0_2} and
 * redefines the minimum set of hooks where the 1.0 format diverges from 0.2.
 *
 * <p>Divergencias respecto a {@code PGMXReader_0_2} (ver
 * {@code docs/rediseno-io/pgmx-readers-diff.md}):</p>
 * <ul>
 *   <li>{@link #getPotential(Element, ProbNet)}: el formato 1.0 no requiere
 *       derivar el rol del atributo XML y asume {@code CONDITIONAL_PROBABILITY}
 *       por construcción (los potenciales de utilidad usan otra ruta).</li>
 *   <li>{@link #getPotential(Element, ProbNet, PotentialRole)}: se elimina la
 *       rama de compatibilidad {@code <UtilityVariable>} y el caso especial
 *       {@code Table + utilityVariable → ExactDistrPotential}. El formato 1.0
 *       ya no produce esa forma heredada.</li>
 *   <li>{@link #buildPotentialParsers()}: añade los parsers de
 *       {@link UnivariateDistrPotential} y {@link AugmentedProbTablePotential},
 *       exclusivos del formato 1.0.</li>
 * </ul>
 *
 * <p>No se introduce una clase base abstracta porque el delta es pequeño
 * (3 overrides sustanciales + 3 helpers nuevos). Si crece la divergencia,
 * reevaluar la opción β del plan de refactorización.</p>
 *
 * @author Manuel Arias
 */
public class PGMXReader_1_0 extends PGMXReader_0_2 {
    
    public PGMXReader_1_0() {
        super();
    }
    
    /**
     * En 1.0 el rol se fija a {@link PotentialRole#CONDITIONAL_PROBABILITY} en
     * lugar de derivarlo del atributo XML {@code role} (diferencia respecto a
     * {@code PGMXReader_0_2#getPotential(Element, ProbNet)}).
     */
    @Override protected Potential getPotential(Element xmlPotential, ProbNet probNet) throws PGMXParserException {
        return this.getPotential(xmlPotential, probNet, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    /**
     * Variante 1.0 sin la rama de compatibilidad heredada: no se interpreta el
     * elemento {@code <UtilityVariable>} ni se reescribe {@code Table +
     * utilityVariable} como {@link org.openmarkov.core.model.network.potential.ExactDistrPotential}
     * (véase {@code PGMXReader_0_2#getPotential(Element, ProbNet, PotentialRole)}).
     * El formato 1.0 ya no emite esa forma.
     */
    @Override protected Potential getPotential(Element eXMLPotential, ProbNet probNet, PotentialRole potentialRole)
            throws PGMXParserException {
        // get type and role of potential
        String sXMLPotentialType = this.getStringXMLPotentialType(eXMLPotential);
        List<Variable> variables = this.getReferencedVariables(eXMLPotential, probNet);
        Potential potential = this.autoGetPotential(sXMLPotentialType, eXMLPotential, probNet, potentialRole, variables);
        Element xmlComment = eXMLPotential.getChild(XMLTags.COMMENT.toString());
        if (xmlComment != null) {
            potential.setComment(xmlComment.getText());
        }
        return potential;
    }
    
    /**
     * Añade al mapa heredado los parsers específicos del formato 1.0:
     * {@link UnivariateDistrPotential} y {@link AugmentedProbTablePotential}.
     */
    @Override
    protected Map<Class<? extends Potential>, PotentialParser> buildPotentialParsers() {
        Map<Class<? extends Potential>, PotentialParser> map = super.buildPotentialParsers();
        map.putAll(Map.of(
                UnivariateDistrPotential.class, PGMXReader_1_0::getUnivariateDistrPotential,
                AugmentedProbTablePotential.class, PGMXReader_1_0::getAugmentedProbTablePotential,
                TransitionTablePotential.class, this::getTransitionTablePotential,
                DistributionTablePotential.class, this::getDistributionTablePotential,
                IncrementPotential.class, this::getIncrementPotential,
                ExternalPotential.class, this::getExternalPotential,
                TreeWithEventsPotential.class, this::getTreeWithEventsPotential,
                TreeWithExcludedEventsPotential.class, this::getTreeWithExcludedEventsPotential,
                IndicatorPotential.class, this::getIndicatorPotential,
                PiecewiseExponentialPotential.class, this::getPiecewiseExponentialPotential
        ));
        return map;
    }
    
    protected static Potential getUnivariateDistrPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                           List<Variable> variables) {
        String univariateName = xmlPotential.getAttributeValue(XMLAttributes.DISTRIBUTION.toString());
        String parametrization = xmlPotential.getAttributeValue(XMLAttributes.PARAMETRIZATION.toString());
        Element xmlRootTable = xmlPotential.getChild(XMLTags.PARAMETERS.toString());
        if (xmlRootTable == null) {
            xmlRootTable = xmlPotential.getChild(XMLTags.VALUES.toString());
        }
        double[] table = parseDoubles(xmlRootTable.getTextNormalize());
        
        UnivariateDistrPotential potential;
        if (parametrization != null) {
            potential = new UnivariateDistrPotential(variables, univariateName, parametrization, xmlRole);
        } else {
            var probDensFunctionClass = ProbDensFunctionManager.getUniqueInstance()
                                                               .getProbDensFunctionClass(univariateName);
            if(probDensFunctionClass == null) {
                potential = new UnivariateDistrPotential(variables,xmlRole);
            }else{
                potential = new UnivariateDistrPotential(variables, probDensFunctionClass, xmlRole);
            }
        }
        
        List<Variable> vDistributionTable = new ArrayList<>(potential.getFiniteStatesVariables());
        vDistributionTable.addFirst(potential.getPseudoVariableDistribution());
        potential.getAugmentedProbTable().setValues(table);
        if (xmlPotential.getChild(XMLTags.FUNCTIONS.toString()) != null) {
            potential.setDistributionTable(
                    getAugmentedProbTable(xmlPotential, xmlRole, vDistributionTable, variables));
        }
        
        return potential;
    }
    
    // TODO Remove?
    // Answer: Why? Are we getting rid of AugmentedProbTablePotentials?
    protected static Potential getAugmentedProbTablePotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                              List<Variable> variables) {
        
        AugmentedProbTablePotential potential = new AugmentedProbTablePotential(variables, xmlRole);
        List<Variable> parameterVariables = potential.getParameterVariables();
        List<Variable> finiteStatesVariables = potential.getFiniteStatesVariables();
        
        potential.setAugmentedProbTable(getAugmentedProbTable(xmlPotential, xmlRole, finiteStatesVariables, parameterVariables));
        return potential;
    }
    
    /**
     * @param xmlPotential       {@code Element}
     * @param xmlRole            {@code PotentialRole}
     * @param vDistributionTable {@code List} of {@code Variable} of the potential
     * @param variables          the variables
     *
     * @return AugmentedProbTable
     */
    protected static AugmentedProbTable getAugmentedProbTable(Element xmlPotential, PotentialRole xmlRole,
                                                              List<Variable> vDistributionTable, List<Variable> variables) {
        String functionsValue = xmlPotential.getChild(XMLTags.FUNCTIONS.toString()).getValue();
        List<String> uncertainParametersList = Pattern.compile("\"(.*?)\"")
                                                      .matcher(functionsValue)
                                                      .results()
                                                      .map(m -> m.group(1))
                                                      .toList();
        VariableExpression[] functionValues = new VariableExpression[uncertainParametersList.size()];
        int i = 0;
        for (String uncertainParameter : uncertainParametersList) {
            functionValues[i++] = new VariableExpression(
                    Stream.concat(vDistributionTable.stream(), variables.stream()).distinct().toList(),
                    uncertainParameter);
        }
        return new AugmentedProbTable(vDistributionTable, xmlRole, functionValues);
    }
    
    // 24/10/2020 - Added Event queue behaviour property
    
    
    @Override
    protected void loadVariableAdvancedInformation(Element variableElement, ProbNet probNet, VariableType variableType, NodeType nodeType, String variableName) throws PGMXParserException.VariableHasNoStates {
        super.loadVariableAdvancedInformation(variableElement, probNet, variableType, nodeType, variableName);
        if (nodeType.equals(NodeType.EVENT)) {
            Node node = probNet.getNode(variableName);
            Variable variable = node.getVariable();
            // 24/10/2020 moved from parent
            variable.setVariableType(VariableType.EVENT);
            //
            // In parent method properties is a local variable. Consider moving to a class field
            HashMap<String, String> properties = this.getProperties(variableElement);
            //With conditional for compatibility with models created before adding events behaviour
            if (properties.get(XMLTags.ALWAYS_APPEND.toString()) != null) {
                boolean alwaysAppend = Boolean.parseBoolean(properties.get(XMLTags.ALWAYS_APPEND.toString()));
                node.setAlwaysAppend(alwaysAppend);
                node.removeAdditionalProperty(XMLTags.ALWAYS_APPEND.toString());
            }
        }
    }
    
    
    //  created in 03/2019.   Impossible configurations added in 08/09/2019. Distributions 15/09/2019. getDistributionTablePotential renamed from getTimeToEventTablePotential 24/05/2022
    // 14/08/2022 impossibleConfiguration removed
    // 28/08/2023 ExternalPotential added; MontecarloOptions added
    
    // 28/08/2023 MontecarloOptions added
    // FIXME 28/08/2023 update after decision for MonteCarlo options; some elements hardcoded
    @Override
    protected void getInferenceOptions(Element root, ProbNet probNet) {
        super.getInferenceOptions(root, probNet);
        this.getMonteCarloOptions(root.getChild(XMLTags.INFERENCE_OPTIONS.toString()), probNet);
        
    }
    
    /**
     * Reads the Monte Carlo options for evaluating DESnets
     *
     * @param inferenceOptions XML subtree with the inference options
     * @param probNet          probNet being created reading the pgmx file
     */
    protected void getMonteCarloOptions(Element inferenceOptions, ProbNet probNet) {
        if(inferenceOptions==null){
            return;
        }
        Element monteCarloOptions = inferenceOptions.getChild(XMLTags.MONTE_CARLO_OPTIONS.toString());
        if (monteCarloOptions == null) return;
        
        String childName = monteCarloOptions.getChildTextTrim(XMLTags.NUMBER_OF_SIMULATIONS.toString());
        if (!((childName == null) || (childName.isEmpty())))
            probNet.getInferenceOptions().getMonteCarloOptions().setNumSimulations(Integer.parseInt(childName));
        
        //28/08/2023 When using getValue, when there are extra spaces, /n, or /t (possibly introduced by hand) parseInt throws an exception
        //FIXME 28/08/2023 replace getValue for getChildTextTrim
//        Element numSeries = monteCarloOptions.getChild( XMLTags.NUMBER_OF_SERIES.toString() );
//        probNet.getInferenceOptions().getMonteCarloOptions().setNumSeries(Integer.parseInt( numSeries.getValue() ));
        childName = monteCarloOptions.getChildTextTrim(XMLTags.NUMBER_OF_SERIES.toString());
        if (!((childName == null) || (childName.isEmpty())))
            probNet.getInferenceOptions().getMonteCarloOptions().setNumSeries(Integer.parseInt(childName));
        
        childName = monteCarloOptions.getChildTextTrim(XMLTags.INPUT_DATA_FILE.toString());
        if (!((childName == null) || (childName.isEmpty())))
            try {
                probNet.getInferenceOptions().getMonteCarloOptions().setInputFilePath(Paths.get(childName));
            } catch (InvalidPathException e) {
                //If the file is not valid do nothing
            }
    }
    
    /**
     * Reads an EventTablePotential from the Element xmlPotential
     *
     * @param xmlPotential
     * @param xmlRole
     * @param variables
     *
     * @return the EventTablePotential read
     *
     */
    private final Potential getTransitionTablePotential
    (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables) {
        Element xmlRootTable = this.getXMLRootTable(xmlPotential);
        double[] table = PGMXReader_0_2.parseDoubles(xmlRootTable.getTextNormalize());
        TransitionTablePotential potential;
        potential = new TransitionTablePotential(variables, xmlRole);
        potential.getTablePotential().setValues(table);
        //14/08/2022 impossibleConfiguration removed
        //            addImpossibleConfigurations(xmlPotential, probNet, potential);
        return potential;
        
    }
    
    /**
     * Reads an EventTablePotential from the Element xmlPotential
     *
     * @param xmlPotential
     * @param xmlRole
     * @param variables
     *
     * @return the EventTablePotential read
     *
     */
    protected Potential getDistributionTablePotential
    (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables) {
        Element distribution = xmlPotential.getChild(XMLTags.VALUE.toString());
        String distributionName = distribution.getAttributeValue(XMLAttributes.DISTRIBUTION.toString());
        //08/10/2020 -parametrization added
        String distributionParametrization = distribution.getAttributeValue(XMLAttributes.PARAMETRIZATION.toString());
        
        
        Element xmlRootTable = this.getXMLRootTable(xmlPotential);
        Element xmlUncertain = xmlPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString());
        
        double[] table = parseDoubles(xmlRootTable.getTextNormalize());
        DistributionTablePotential potential;
        potential = new DistributionTablePotential(variables, xmlRole, distributionName, distributionParametrization);
        potential.getTableWithEvents().setValues(table);
        // 20/05/2024 Begin PSA
        UncertainValue[] uncertainValues;
        if (xmlUncertain != null) {
            uncertainValues = getUncertainValues(xmlUncertain);
            potential.getTableWithEvents().setUncertainValues(uncertainValues);
        }
        // End PSA
        // 15/12/2019 added functionValues to tableWithEvents
        if (potential.hasFunctionValues()) {
            this.addFunctionValues(xmlPotential, potential);
            
        }
        //14/08/2022 impossibleConfiguration removed
        //            addImpossibleConfigurations(xmlPotential, probNet,  potential);
        return potential;
        
    }
    
    /**
     * 15/12/2019
     * Added function values to TimeToEventTable
     *
     * @param xmlPotential
     * @param potential
     */
    private void addFunctionValues(Element xmlPotential, DistributionTablePotential potential) {
        
        Element eFunctionValues = xmlPotential.getChild("FunctionValues");
        List<Element> eFunctionValuesList = eFunctionValues.getChildren();
        List<String> sFunctionValues = new ArrayList<>();
        for (Element eFunctionValue : eFunctionValuesList) {
            sFunctionValues.add(eFunctionValue.getText());
        }
        potential.getTableWithEvents()
                 .getTableWithFunctions()
                 .setFunctionValues(sFunctionValues.stream().toArray(String[]::new));
    }
    
    //14/08/2022 impossibleConfiguration removed
    //    /**
    //     * Reads impossible configurations from potentials implementing the ImpossibleConfiguration interface
    //     * TODO change configurations for being used by all potentials with ImpossibleConfigurations
    //     * TODO create the strings variables
    //     * @param xmlPotential
    //     * @param probNet
    //     * @param potentialWithImpossibleConfigurations
    //     */
    //    private void addImpossibleConfigurations(Element xmlPotential, ProbNet probNet, ImpossibleConfiguration potentialWithImpossibleConfigurations) {
    //        List<Configuration> impossibleConfigurations = potentialWithImpossibleConfigurations.getImpossibleConfigurations();
    //        Element eICs = xmlPotential.getChild("ImpossibleConfigurations");
    //
    //        if (eICs != null) {
    //            List<Element> iCElements = eICs.getChildren();
    //            for (Element iC : iCElements) {
    //                List<Element> eFindings = iC.getChildren();
    //                List<Finding> configuration = new ArrayList<Finding>();
    //                for (Element eFinding : eFindings) {
    //                    String variableName = eFinding.getAttribute("variable").getValue();
    //                    String stateName = eFinding.getAttribute("state").getValue();
    //                    Variable variable = null;
    //                    State state = null;
    //                    try {
    //                        //TODO Change; think about configurations/EvidenceCases and make it independant from potential
    //                        if (variableName.compareTo("Events") ==0){
    //                            List<Variable> variables=null;
    //                            if (potentialWithImpossibleConfigurations instanceof TransitionTablePotential) {
    //                               variables = ((TransitionTablePotential) potentialWithImpossibleConfigurations).getTablePotential().getVariables();
    //                          } else if   (potentialWithImpossibleConfigurations instanceof DistributionTablePotential){
    //                               variables = ((DistributionTablePotential) potentialWithImpossibleConfigurations).getTableWithEvents().getTablePotential().getVariables();
    //                          }
    //                          variable = variables.stream().filter(v -> v.getName().equals("Events"))
    //                                  .findAny()
    //                                  .orElse(null);
    //                        } else
    //                            variable = probNet.getVariable(variableName);
    //                             state = variable.getState(stateName);
    
    /// /                    } catch (NodeNotFoundException e) {
    /// /                        e.printStackTrace();
    /// /                    } catch (InvalidStateException e) {
    /// /                        e.printStackTrace();
    //                    } catch (Exception e){
    //                        e.printStackTrace();
    //                    }
    //
    //                    configuration.add(new Finding(variable, state));
    //                }
    //                impossibleConfigurations.add(new Configuration(configuration));
    //            }
    //        }
    //    }
    protected Potential getIncrementPotential
    (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables) {
        return new IncrementPotential(variables, xmlRole);
    }
    
    
    protected Potential getTreeWithEventsPotential
            (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables) throws PGMXParserException {
        Element xmlRootTable = this.getXMLRootTable(xmlPotential);
        
        TreeWithEventsPotential potential = null;
        try {
            potential = new TreeWithEventsPotential(variables, xmlRole);
            List<Variable> noEventVariables = variables.subList(1, variables.size())
                                                       .stream()
                                                       .filter(variable -> variable.getVariableType() != VariableType.EVENT)
                                                       .toList();
            int i = 1;
            while (xmlPotential.getChildren().size() > 1) {
                // Each TreeADD is encoded by two elements: Top Variable and Branches
                // It is coded this way to use getTreeADDBranches instead of repeating code. FIXME merge
                //Top Variable + branches
                Element xmlTopVariable = xmlPotential.getChild(XMLTags.TOP_VARIABLE.toString());
                Variable topVariable = getVariable(xmlTopVariable, probNet);
                xmlPotential.removeContent(1);
                List<Variable> branchVariables = new ArrayList<>();
                branchVariables.add(variables.get(0));
                branchVariables.add(topVariable);
                branchVariables.addAll(noEventVariables);
                List<TreeADDBranch> branches = this.getTreeADDBranches(xmlPotential, probNet, topVariable, xmlRole, branchVariables);
                xmlPotential.removeContent(1);
                TreeADDPotential treeADDPotential = new TreeADDPotential(branchVariables, topVariable, xmlRole, branches);
                potential.setTree(topVariable, treeADDPotential);
                
            }
        } catch (Exception e) {
            //            throw new PGMXParserException("Exception in .", xmlPotential);
            e.printStackTrace();
        }
        return potential;
        
    }
    
    
    //14/03/2023 - Tree potential in events where events does not take part in computing the TTE FIXME Check
    protected Potential getTreeWithExcludedEventsPotential
    (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables) throws PGMXParserException {
        Element xmlRootTable = this.getXMLRootTable(xmlPotential);
        
        TreeWithExcludedEventsPotential potential = null;
        try {
            potential = new TreeWithExcludedEventsPotential(variables, xmlRole);
            List<Variable> noEventVariables = variables.subList(1, variables.size())
                                                       .stream()
                                                       .filter(variable -> variable.getVariableType() != VariableType.EVENT)
                                                       .toList();
            //TreeADD; only one child
            // Each TreeADD is encoded by two elements: Top Variable and Branches
            // It is coded this way to use getTreeADDBranches instead of repeating code. FIXME merge
            //Top Variable + branches
            Element xmlTopVariable = xmlPotential.getChild(XMLTags.TOP_VARIABLE.toString());
            Variable topVariable = getVariable(xmlTopVariable, probNet);
            xmlPotential.removeContent(1);
            List<Variable> branchVariables = new ArrayList<>();
            branchVariables.add(variables.get(0));
            branchVariables.add(topVariable);
            branchVariables.addAll(noEventVariables);
            List<TreeADDBranch> branches = this.getTreeADDBranches(xmlPotential, probNet, topVariable, xmlRole, branchVariables);
            xmlPotential.removeContent(1);
            TreeADDPotential treeADDPotential = new TreeADDPotential(branchVariables, topVariable, xmlRole, branches);
            potential.setNoEventTree(treeADDPotential);
            
        } catch (Exception e) {
            //            throw new PGMXParserException("Exception in .", xmlPotential);
            e.printStackTrace();
        }
        return potential;
        
    }
    //18/03/2023
    
    /**
     * @param xmlPotential
     * @param probNet
     * @param role
     * @param variables
     *
     * @return
     *
     */
    protected Potential getIndicatorPotential(Element xmlPotential, ProbNet probNet, PotentialRole role,
                                              List<Variable> variables) {
        Element xmlTte = xmlPotential.getChild(XMLTags.TTE.toString());
        Element xmlPOcurrence = xmlPotential.getChild(XMLTags.P_OCCURRENCE.toString());
        IndicatorPotential indicatorPotential = new IndicatorPotential(variables, role, Double.parseDouble(xmlTte.getText()), Double.parseDouble(xmlPOcurrence.getText()));
        return indicatorPotential;
    }
    
    //28/08/2023 ExternalPotential added
    protected Potential getExternalPotential
    (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables) {
        return new ExternalPotential(variables, xmlRole);
    }
    
    // 25/10/2023
    protected Potential getPiecewiseExponentialPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                         List<Variable> variables) {
        FunctionPotential initTimeFunction = (FunctionPotential) PGMXPotentialParsers.getFunctionPotential(xmlPotential, probNet, xmlRole, variables);
        String lifeTableIntervalsStr = xmlPotential.getChildText(XMLTags.SLICES.toString());
        String[] numbersString = lifeTableIntervalsStr.split("\\s+");
        double[] lifeTableIntervals = Arrays.stream(numbersString).mapToDouble(Double::parseDouble).toArray();
        String lifeTableProbString = xmlPotential.getChildText(XMLTags.VALUES.toString());
        numbersString = lifeTableProbString.split("\\s+");
        double[] lifeTableProb = Arrays.stream(numbersString).mapToDouble(Double::parseDouble).toArray();
        TreeMap<Double, Double> lifeTableValues = new TreeMap<>();
        for (int i = 0; i < lifeTableIntervals.length; i++) {
            lifeTableValues.put(lifeTableIntervals[i], lifeTableProb[i]);
        }
        boolean useRates = Boolean.parseBoolean(xmlPotential.getChildText(XMLTags.RATES.toString()));
        return new PiecewiseExponentialPotential(variables, xmlRole, lifeTableValues, initTimeFunction, useRates);
    }
}
