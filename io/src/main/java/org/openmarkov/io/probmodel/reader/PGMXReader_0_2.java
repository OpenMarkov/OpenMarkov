/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Properties;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyContinuousVariables;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlySelfLoopsWithEventAndChanceNodes;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.BinomialPotential;
import org.openmarkov.core.model.network.potential.ConditionalGaussianPotential;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.DiscretizedCauchyPotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.ExponentialHazardPotential;
import org.openmarkov.core.model.network.potential.ExponentialPotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.WeibullHazardPotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeUtils;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;
import org.openmarkov.io.probmodel.strings.XMLValues;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PGMXReader_0_2 {
    
    public final Map<Class<? extends Potential>, PotentialParser> potentialParsers;
    
    public PGMXReader_0_2() {
        this.potentialParsers = buildPotentialParsers();
    }
    
    /**
     * Builds the map from potential class to its {@link PotentialParser}.
     * Subclasses override this method to add or replace parsers for a new format version.
     *
     * @return mutable map (keyed by potential class, ordered by insertion)
     */
    protected Map<Class<? extends Potential>, PotentialParser> buildPotentialParsers() {
        Map<Class<? extends Potential>, PotentialParser> map = new LinkedHashMap<>();
        map.put(UniformPotential.class, PGMXPotentialParsers::getUniformPotential);
        map.put(ProductPotential.class, PGMXPotentialParsers::getProductPotential);
        map.put(TablePotential.class, PGMXPotentialParsers::getTablePotential);
        map.put(ExactDistrPotential.class, PGMXPotentialParsers::getExactDistrPotential);
        map.put(TreeADDPotential.class, this::getTreeADDPotential);
        map.put(ICIPotential.class, PGMXPotentialParsers::getICIPotential);
        map.put(WeibullHazardPotential.class, PGMXPotentialParsers::getWeibullPotential);
        map.put(ExponentialHazardPotential.class, PGMXPotentialParsers::getExponentialHazardPotential);
        map.put(ExponentialPotential.class, PGMXPotentialParsers::getExponentialPotential);
        map.put(LinearCombinationPotential.class, PGMXPotentialParsers::getLinearRegressionPotential);
        map.put(FunctionPotential.class, PGMXPotentialParsers::getFunctionPotential);
        map.put(DeltaPotential.class, PGMXPotentialParsers::getDeltaPotential);
        map.put(BinomialPotential.class, PGMXPotentialParsers::getBinomialPotential);
        map.put(SumPotential.class, PGMXPotentialParsers::getSumPotential);
        map.put(SameAsPrevious.class, PGMXPotentialParsers::getSameAsPrevious);
        map.put(CycleLengthShift.class, PGMXPotentialParsers::getCycleLengthShiftPotential);
        map.put(ConditionalGaussianPotential.class, PGMXPotentialParsers::getConditionalGaussianPotential);
        map.put(DiscretizedCauchyPotential.class, PGMXPotentialParsers::getDiscretizedCauchyPotential);
        return map;
    }
    
    /**
     * Loads a ProbNetInfo from a PGMX file given by netName.
     *
     * @param networkSource = path + network name + extension. {@code String}
     *
     * @return The {@code ProbNet} readed or {@code null}
     *
     * @throws PGMXParserException if there is an error parsing the XML
     */
    public PGMXReader.NetworkAndEvidence read(URL networkSource) throws ProbNetParserException {
        FormatManager formatManager = FormatManager.getInstance();
        try {
            formatManager.checkVersion(networkSource);
            formatManager.checkStructure(networkSource);
        } catch (SAXException | IOException e) {
            throw new ProbNetParserException.PGMXInvalid(e.getMessage());
        }
        Element root = getRootElement(networkSource);
        ProbNet probNet = this.getProbNet(root, networkSource.getFile());
        this.getInferenceOptions(root, probNet);
        List<EvidenceCase> evidence = this.getEvidence(root, probNet);
        this.getPolicies(root, probNet);
        this.setVariableType(root, probNet);
        this.setDefaultStates(root, probNet);
        return new PGMXReader.NetworkAndEvidence(probNet, evidence);
    }
    
    /**
     * @param netName     Network name
     * @param inputStream InputStream[] of the network
     *
     * @return network version in a String
     */
    public static String getVersion(URL networkSource) throws ProbNetParserException.XMLInvalid, ProbNetParserException.CannotOpenFile {
        Element root = getRootElement(networkSource);
        return root.getAttributeValue(XMLAttributes.FORMAT_VERSION.toString());
    }
    
    /**
     * Gets the root element of a PGMX file
     *
     * @param stream  InputStream
     * @param netName Network name
     *
     * @return root Element
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    public static Element getRootElement(URL networkSource) throws ProbNetParserException.XMLInvalid, ProbNetParserException.CannotOpenFile {
        SAXBuilder builder = new SAXBuilder();
        builder.setJDOMFactory(new LocatedJDOMFactory());
        Document document;
        try {
            document = builder.build(networkSource);
        } catch (JDOMException e) {
            throw new ProbNetParserException.XMLInvalid(networkSource.getFile(), e);
        } catch (IOException e) {
            throw new ProbNetParserException.CannotOpenFile(networkSource.getFile());
        }
        return document.getRootElement();
    }
    
    /**
     * @param root    Root element
     * @param netName Network name
     *
     * @return ProbNet or null
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    public ProbNet getProbNet(Element root, String netName) throws ProbNetParserException {
        return getProbNet(root, netName, new HashMap<>());
    }
    
    /**
     * @param root    Root element
     * @param netName Network name
     * @param classes Classes
     *
     * @return ProbNet or null
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    protected ProbNet getProbNet(Element root, String netName, Map<String, ProbNet> classes) throws ProbNetParserException {
        Element xMLProbNet = root.getChild(getStringTagNetwork());
        ProbNet probNet = null;
        if (xMLProbNet != null) { // Read prob net if the xml file exists
            probNet = initializeProbNet(xMLProbNet, netName);
            getVariablesLinksAndPotentials(xMLProbNet, probNet);
            getNetworkAdvancedInformation(xMLProbNet, probNet, netName, classes);
        }
        return probNet;
    }
    
    /**
     * Reads agents, temporal unit, additional properties and OOPN
     *
     * @param xMLProbNet Root element
     * @param probNet    ProbNet
     * @param netName    Network name
     * @param classes    the classes
     *
     */
    protected void getNetworkAdvancedInformation(Element xMLProbNet, ProbNet probNet, String netName,
                                                 Map<String, ProbNet> classes) {
        getAgents(xMLProbNet, probNet);
        getTemporaUnit(xMLProbNet, probNet);
        getAdditionalProperties(xMLProbNet, probNet);
    }
    
    protected static void getTemporaUnit(Element xMLProbNet, ProbNet probNet) {
        Element temporalUnit = xMLProbNet.getChild(XMLTags.TIME_UNIT.toString());
        CycleLength temporalUnitProbNet = new CycleLength();
        if (temporalUnit != null) {
            try {
                String scaleAttribute = temporalUnit.getAttributeValue(XMLTags.VALUE.toString());
                if (scaleAttribute != null) {
                    double scale = Double.parseDouble(scaleAttribute);
                    temporalUnitProbNet.setValue(scale);
                }
            } catch (NumberFormatException e) {
                // TODO - Check this exception
            }
            String unitAttribute = temporalUnit.getAttributeValue(XMLAttributes.UNIT.toString());
            if (unitAttribute != null) {
                CycleLength.Unit unit = CycleLength.Unit.valueOf(unitAttribute);
                temporalUnitProbNet.setUnit(unit);
            }
        }
        probNet.setCycleLength(temporalUnitProbNet);
        
    }
    
    protected void getVariablesLinksAndPotentials(Element xMLProbNet, ProbNet probNet)
            throws PGMXParserException {
        //Proposed behaviour: if there is no variables exit silently and keep the network empty
        //getVariables( xMLProbNet, probNet );
        getVariables(xMLProbNet, probNet);
        getLinks(xMLProbNet, probNet);
        getPotentials(xMLProbNet, probNet);
    }
    
    /**
     * @return The string of the tag encloses the network
     */
    protected String getStringTagNetwork() {
        return XMLTags.PROB_NET.toString();
    }
    
    protected ProbNet initializeProbNet(Element xMLProbNet, String netName)
            throws PGMXParserException.NoNetworkTypeFound, PGMXParserException.UnknownNetworkType, PGMXParserException.ConstraintNotFound {
        // =
        // xMLProbNet.getAttribute(XMLAttributes.TYPE.toString());
        NetworkType networkType = getNetworkType(xMLProbNet);
        ProbNet probNet = new ProbNet(networkType);
        getAdditionalConstraints(probNet, xMLProbNet);
        // TODO Read Inference options
        // TODO Read Policies
        probNet = getConstraints(xMLProbNet, probNet);
        probNet.setComment(getProbNetComment(xMLProbNet, probNet));
        probNet.setName(FilenameUtils.getName(netName));
        getDecisionCriterion(xMLProbNet, probNet);
        return probNet;
    }
    
    protected void getAdditionalProperties(Element root, ProbNet probNet) {
        Element xmlAdditionalProperties = root.getChild(XMLTags.ADDITIONAL_PROPERTIES.toString());
        if (xmlAdditionalProperties != null) {
            List<Element> propertiesListElement = getXMLChildren(xmlAdditionalProperties);
            if (propertiesListElement != null && !propertiesListElement.isEmpty()) {
                for (Element propertyElement : propertiesListElement) {
                    String propertyName = getElementName(propertyElement);
                    String propertyValue = propertyElement.getAttributeValue(XMLAttributes.VALUE.toString());
                    probNet.putAdditionalProperty(propertyName, propertyValue);
                }
            }
        }
        Element xmlProperties = root.getChild(XMLTags.PROPERTIES.toString());
        if (xmlProperties != null) {
            List<Element> propertiesListElement = getXMLChildren(xmlProperties);
            if (propertiesListElement != null && !propertiesListElement.isEmpty()) {
                for (Element propertyElement : propertiesListElement) {
                    String propertyName = getElementName(propertyElement);
                    String propertyValue = propertyElement.getAttributeValue(XMLAttributes.VALUE.toString());
                    probNet.putAdditionalProperty(propertyName, propertyValue);
                }
            }
        }
    }
    
    /**
     * @param root    . {@code Element}
     * @param probNet . {@code ProbNet}
     */
    protected void getAgents(Element root, ProbNet probNet) {
        Element xmlAgentsRoot = root.getChild(XMLTags.AGENTS.toString());
        if (xmlAgentsRoot != null) {
            List<Element> xmlAgents = getXMLChildren(xmlAgentsRoot);
            ArrayList<StringWithProperties> agents = new ArrayList<>();
            for (Element agentElement : xmlAgents) {
                String agentName = getElementName(agentElement);
                StringWithProperties agent = new StringWithProperties(agentName);
                Properties agentProperties = getAdditionalProperties(agentElement);
                agent.put(agentProperties);
                agents.add(agent);
            }
            probNet.setAgents(agents);
        }
    }
    
    /**
     * @param root    . {@code Element}
     * @param probNet . {@code ProbNet}
     */
    protected void getDecisionCriterion(Element root, ProbNet probNet) {
        Element xmlCriteronRoot = root.getChild(XMLTags.DECISION_CRITERIA.toString());
        if (xmlCriteronRoot != null) {
            List<Element> xmlCriterion = getXMLChildren(xmlCriteronRoot);
            List<Criterion> criteria = new ArrayList<>();
            for (Element criterionElement : xmlCriterion) {
                String criterionName = getElementName(criterionElement);
                // Properties criterionProperties = getAdditionalProperties(criterionElement);
                String criterionUnit = criterionElement.getAttributeValue(XMLAttributes.UNIT.toString());
                Criterion decisionCriterion = new Criterion(criterionName, criterionUnit);
                /*
                 * if (criterionProperties != null) { decisionCriterion.put(criterionProperties); }
                 */
                criteria.add(decisionCriterion);
                // criterions.put(criterionName, criterionProperties);
            }
            probNet.setDecisionCriteria(criteria);
            // If the probNet has not the OnlyChanceNodes constraint
            // we create a default criterion
        } else if (!probNet.hasConstraintOfClass(OnlyChanceNodes.class)) {
            List<Criterion> criteria = new ArrayList<>();
            Criterion decisionCriterion = new Criterion();
            criteria.add(decisionCriterion);
            probNet.setDecisionCriteria(criteria);
        }
    }
    
    /**
     * @param agentElement . {@code Element}
     *
     * @return {@code Properties}
     */
    protected Properties getAdditionalProperties(Element agentElement) {
        Properties properties = null;
        List<Element> propertiesListElement = getXMLChildren(agentElement);
        if (propertiesListElement != null && !propertiesListElement.isEmpty()) {
            properties = new Properties();
            for (Element propertyElement : propertiesListElement) {
                String propertyName = getElementName(propertyElement);
                String propertyValue = propertyElement.getAttributeValue(XMLAttributes.VALUE.toString());
                properties.put(propertyName, propertyValue);
            }
        }
        return properties;
    }
    
    /**
     * Get the Inference Options from the PGMX
     *
     * @param root    Root element
     * @param probNet ProbNet
     */
    protected void getInferenceOptions(Element root, ProbNet probNet) {
        Element inferenceOptions = root.getChild(XMLTags.INFERENCE_OPTIONS.toString());
        if (inferenceOptions != null) {
            getMulticriteriaOptions(inferenceOptions, probNet);
            getTemporalOptions(inferenceOptions, probNet);
        }
        
    }
    
    /**
     * Get the Multicriteria Options from the PGMX
     *
     * @param inferenceOptions Inference options
     * @param probNet          ProbNet
     */
    protected void getMulticriteriaOptions(Element inferenceOptions, ProbNet probNet) {
        Element multicriteriaOptions = inferenceOptions.getChild(XMLTags.MULTICRITERIA_OPTIONS.toString());
        if (multicriteriaOptions != null) {
            Element multiCriteriaType = multicriteriaOptions.getChild(XMLTags.SELECTED_ANALYSIS_TYPE.toString());
            
            probNet.getInferenceOptions()
                   .getMultiCriteriaOptions()
                   .setMulticriteriaType(MulticriteriaOptions.Type.valueOf(multiCriteriaType.getValue()));
            
            Element unicriteria = multicriteriaOptions.getChild(XMLTags.UNICRITERION.toString());
            if (unicriteria != null) {
                
                Element scalesTag = unicriteria.getChild(XMLTags.SCALES.toString());
                if (scalesTag != null) {
                    List<Element> scales = getXMLChildren(scalesTag);
                    for (Element element : scales) {
                        for (Criterion probNetCriterion : probNet.getDecisionCriteria()) {
                            String criterionVariableString =
                                    element.getAttribute(XMLTags.CRITERION.toString()).getValue();
                            if (criterionVariableString.equals(probNetCriterion.getCriterionName())) {
                                probNetCriterion.setUnicriterizationScale(Double.parseDouble(element.getAttributeValue(XMLTags.VALUE.toString())));
                            }
                        }
                    }
                }
                
                Element mainUnit = unicriteria.getChild(XMLTags.UNIT.toString());
                if (mainUnit != null) {
                    probNet.getInferenceOptions().getMultiCriteriaOptions().setMainUnit(mainUnit.getValue());
                }
            }
            
            Element ceOptions = multicriteriaOptions.getChild(XMLTags.COST_EFFECTIVENESS.toString());
            if (ceOptions != null) {
                Element scalesTag = ceOptions.getChild(XMLTags.SCALES.toString());
                if (scalesTag != null) {
                    List<Element> scales = getXMLChildren(scalesTag);
                    for (Element element : scales) {
                        for (Criterion criterion : probNet.getDecisionCriteria()) {
                            if (element.getAttribute(XMLTags.CRITERION.toString())
                                       .getValue()
                                       .equals(criterion.getCriterionName())) {
                                criterion.setCeScale(Double.parseDouble(element.getAttributeValue(XMLTags.VALUE.toString())));
                            }
                        }
                    }
                }
                
                Element ceCriteria = ceOptions.getChild(XMLTags.CE_CRITERIA.toString());
                if (ceCriteria != null) {
                    List<Element> ce_criterion = getXMLChildren(ceCriteria);
                    for (Element element : ce_criterion) {
                        for (Criterion criterion : probNet.getDecisionCriteria()) {
                            if (element.getAttribute(XMLTags.CRITERION.toString())
                                       .getValue()
                                       .equals(criterion.getCriterionName())) {
                                criterion.setCECriterion(Criterion.CECriterion.valueOf(element.getAttributeValue(XMLTags.VALUE.toString())));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Get Temporal Evolution Options from the PGMX
     *
     * @param inferenceOptions Inference Options
     * @param probNet          ProbNet
     */
    protected void getTemporalOptions(Element inferenceOptions, ProbNet probNet) {
        Element temporalOptions = inferenceOptions.getChild(XMLTags.TEMPORAL_OPTIONS.toString());
        if (temporalOptions != null) {
            
            Element slices = temporalOptions.getChild(XMLTags.SLICES.toString());
            probNet.getInferenceOptions().getTemporalOptions().setHorizon(Integer.parseInt(slices.getValue()));
            
            Element transition = temporalOptions.getChild(XMLTags.TRANSITION.toString());
            probNet.getInferenceOptions()
                   .getTemporalOptions()
                   .setTransition(TemporalOptions.TransitionTime.valueOf(transition.getText()));
            
            Element discountsTag = temporalOptions.getChild(XMLTags.DISCOUNT_RATES.toString());
            List<Element> discounts = getXMLChildren(discountsTag);
            for (Element element : discounts) {
                for (Criterion criterion : probNet.getDecisionCriteria()) {
                    if (element.getAttribute(XMLTags.CRITERION.toString())
                               .getValue()
                               .equals(criterion.getCriterionName())) {
                        criterion.setDiscount(Double.parseDouble(element.getAttributeValue(XMLAttributes.VALUE.toString())));
                        criterion.setDiscountUnit(CycleLength.DiscountUnit.valueOf(element.getAttributeValue(XMLAttributes.UNIT.toString())));
                    }
                }
            }
            
        }
        
    }
    
    /**
     * Reads evidence
     *
     * @param root    Root element
     * @param probNet ProbNet
     *
     * @return List of evidence case
     */
    protected List<EvidenceCase> getEvidence(Element root, ProbNet probNet)
            throws PGMXParserException.EvidenceIncompatibleInFile {
        Element xMLEvidence = root.getChild(XMLTags.EVIDENCE.toString());
        List<EvidenceCase> evidence = new ArrayList<>();
        if (xMLEvidence != null) {
            List<Element> xmlEvidenceCases = getXMLChildren(xMLEvidence);
            for (Element xmlEvidenceCase : xmlEvidenceCases) {
                EvidenceCase evidenceCase = new EvidenceCase();
                List<Element> xmlFindings = getXMLChildren(xmlEvidenceCase);
                for (Element xmlFinding : xmlFindings) {
                    try {
                        Variable variable = probNet.getVariable(xmlFinding.getAttributeValue("variable"));
                        Finding finding;
                        if (variable.getVariableType() == VariableType.FINITE_STATES) {
                            String stateName = xmlFinding.getAttributeValue("state");
                            finding = new Finding(variable, variable.getStateIndex(stateName));
                        } else {
                            double numericalValue = Double.parseDouble(xmlFinding.getAttributeValue("numericValue"));
                            finding = new Finding(variable, numericalValue);
                        }
                        evidenceCase.addFinding(finding);
                    } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
                        throw new PGMXParserException.EvidenceIncompatibleInFile(e, xmlFinding);
                    }
                }
                evidence.add(evidenceCase);
            }
        }
        return evidence;
    }
    
    protected static void getAdditionalConstraints(ProbNet probNet, Element xMLProbNet) {
        if (parseXMLElement(xMLProbNet, XMLTags.ADDITIONAL_CONSTRAINTS)) {
            // TODO - Get the additional constraints
        }
    }
    
    protected static boolean parseXMLElement(Element xMLRoot, XMLTags additionalConstraints) {
        return false;
    }
    
    protected NetworkType getNetworkType(Element xMLProbNet)
            throws PGMXParserException.NoNetworkTypeFound, PGMXParserException.UnknownNetworkType {
        String sType = getStringXMLPotentialType(xMLProbNet);
        if (sType == null || sType.isEmpty()) {
            throw new PGMXParserException.NoNetworkTypeFound(xMLProbNet);
        }
        Class<? extends NetworkType> networkClass = NetworkTypeUtils.getNetworkClassByName(sType);
        if (networkClass == null) {
            throw new PGMXParserException.UnknownNetworkType(sType, xMLProbNet);
        }
        return NetworkTypeUtils.safeInstanciate(networkClass);
    }
    
    /**
     * @param root    . {@code Element}
     * @param probNet . {@code ProbNet}
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    protected ProbNet getConstraints(Element root, ProbNet probNet)
            throws PGMXParserException.ConstraintNotFound {
        // ProbNet network = null;
        Element xmlConstraintsRoot = root.getChild(XMLTags.ADDITIONAL_CONSTRAINTS.toString());
        if (xmlConstraintsRoot != null) {
            List<Element> xmlConstraints = getXMLChildren(xmlConstraintsRoot);
            for (Element constraintElement : xmlConstraints) {
                String constraintName = getElementName(constraintElement);
                try {
                    probNet.addConstraint((PNConstraint) Class.forName(constraintName).getConstructor().newInstance());
                } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException |
                         IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    throw new PGMXParserException.ConstraintNotFound(constraintName, root);
                }
                
            }
        }
        return probNet;
    }
    
    /**
     * @param root . {@code Element}
     */
    protected static String getProbNetComment(Element root, ProbNet probNet) {
        Element xmlComments = root.getChild(XMLTags.COMMENT.toString());
        String comment = null;
        if (xmlComments != null) {
            String showCommentWhenOpening = xmlComments.getAttributeValue(XMLAttributes.SHOW_WHEN_OPENING_NETWORK.toString());
            probNet.setShowCommentWhenOpening(Boolean.parseBoolean(showCommentWhenOpening));
            comment = textToHtml(xmlComments.getText());
        }
        return comment == null ? "" : comment;
    }
    
    /**
     * @param root . {@code Element}
     */
    protected static String getComment(Element root) {
        Element xmlComments = root.getChild(XMLTags.COMMENT.toString());
        String comment = null;
        if (xmlComments != null) {
            comment = textToHtml(xmlComments.getText());
        }
        return comment == null ? "" : comment;
    }
    
    /**
     * Reads Nodes:
     *
     * @param root    {@code Element}
     * @param probNet {@code ProbNet}
     */
    protected void getVariables(Element root, ProbNet probNet) throws PGMXParserException.VariableHasNoStates {
        Element xmlVariablesRoot = getXMLVariables(root);
        if (xmlVariablesRoot == null) {
            return;
        }
        List<Element> xmlVariables = getVariablesElements(xmlVariablesRoot);
        loadVariables(xmlVariables, probNet);
    }
    
    protected List<Element> getVariablesElements(Element xmlVariablesRoot) {
        return xmlVariablesRoot.getChildren();
    }
    
    protected void loadVariables(List<Element> xmlVariables, ProbNet probNet)
            throws PGMXParserException.VariableHasNoStates {
        for (Element variableElement : xmlVariables) {
            loadVariable(variableElement, probNet);
        }
    }
    
    protected void loadVariable(Element variableElement, ProbNet probNet)
            throws PGMXParserException.VariableHasNoStates {
        VariableType variableType = getXMLVariableType(variableElement);
        NodeType nodeType = getXMLNodeType(variableElement);
        String variableName = getVariableName(variableElement);
        
        loadVariableAdvancedInformation(variableElement, probNet, variableType, nodeType, variableName);
    }
    
    protected String getVariableName(Element variableElement) {
        return variableElement.getAttributeValue(XMLAttributes.NAME.toString());
    }
    
    protected Element getXMLRootStates(Element variableElement) {
        return variableElement.getChild(XMLTags.STATES.toString());
    }
    
    /**
     * @param variableElement the variable element
     * @param probNet         the prob net
     * @param variableType    the variable type
     * @param nodeType        the node type
     * @param variableName    the variable name
     */
    @SuppressWarnings("unlikely-arg-type")
    protected void loadVariableAdvancedInformation(
            Element variableElement,
            ProbNet probNet,
            VariableType variableType,
            NodeType nodeType,
            String variableName)
            throws PGMXParserException.VariableHasNoStates {
        
        String stringTimeSlice = variableElement.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
        if (stringTimeSlice != null) {
            variableName = variableName.replace(" [" + stringTimeSlice + "]", "");
        }
        
        // Coordinates
        int x = getXMLXCoordinate(variableElement);
        int y = getXMLYCoordinate(variableElement);
        
        Double precision = getXMLPrecision(variableElement);
        
        // States & intervals
        State[] states = null;
        Variable variable;
        
        if ((nodeType == NodeType.CHANCE) || (nodeType == NodeType.DECISION)) {
            if ((variableType == VariableType.FINITE_STATES) || (variableType == VariableType.DISCRETIZED)) {
                Element statesElement = getXMLRootStates(variableElement);
                if (statesElement != null) { // jlgozalo. 25/10/2009
                    states = getXMLStates(statesElement); // previously without null control
                } else {
                    throw new PGMXParserException.VariableHasNoStates(variableName, variableElement);
                }
            }
            if (variableType == VariableType.FINITE_STATES) {
                variable = new Variable(variableName, states);
            } else {
                if (variableType == VariableType.NUMERIC) {
                    variable = getXMLContinuousVariable(variableElement, variableName);
                } else { // DISCRETIZED variable. Read sub-intervals
                    Element thresholdsElement = variableElement.getChild(XMLTags.THRESHOLDS.toString());
                    variable = getXMLDiscretizedVariable(thresholdsElement, states, variableName);
                }
            }
        } else { // utility only??
            variable = new Variable(variableName);
            // decision criterion
            if (nodeType == NodeType.UTILITY) {
                Element decisionCriteria = variableElement.getChild(XMLTags.CRITERION.toString());
                if (decisionCriteria != null) {
                    for (Criterion criterion : probNet.getDecisionCriteria()) {
                        if (criterion.getCriterionName()
                                     .equals(decisionCriteria.getAttributeValue(XMLAttributes.NAME.toString()))) {
                            variable.setDecisionCriterion(criterion);
                            break;
                        }
                    }
                    // If the node has not have Decision Criteria, set the first of the probNet.
                } else {
                    variable.setDecisionCriterion(probNet.getDecisionCriteria().getFirst());
                }
            }
        }
        
        // Set timeSlice
        if (stringTimeSlice != null) {
            variable.setTimeSlice(Integer.parseInt(stringTimeSlice));
        }
        
        // Set unit
        Element xMLUnit = variableElement.getChild(XMLTags.UNIT.toString());
        if (xMLUnit != null) {
            String unit = xMLUnit.getText();
            variable.setUnit(new StringWithProperties(unit));
        }
        
        // other additionalProperties
        LinkedHashMap<String, String> properties = getProperties(variableElement);
        
        // Remove obsolete properties
        if (properties.get(XMLTags.TITLE) != null) {
            properties.remove(XMLTags.TITLE);
        }
        if (properties.get(XMLTags.RELEVANCE) != null) {
            String relevance = properties.get(XMLTags.RELEVANCE);
            if (Double.parseDouble(relevance) == Node.DEFAULT_RELEVANCE) {
                properties.remove(XMLTags.RELEVANCE);
            }
        }
        
        Node node = probNet.addNode(variable, nodeType);
        // always Observed property
        Element alwaysObserved = variableElement.getChild(XMLTags.ALWAYS_OBSERVED.toString());
        if (alwaysObserved != null) {
            node.setAlwaysObserved(true);
        }
        if (properties.get(XMLTags.PURPOSE.toString()) != null) {
            node.setPurpose(properties.get(XMLTags.PURPOSE.toString()));
            properties.remove(XMLTags.PURPOSE.toString());
        }
        // TODO revisar el posible error al convertir un string a número
        if (properties.get(XMLTags.RELEVANCE.toString()) != null) {
            node.setRelevance(Double.parseDouble(properties.get(XMLTags.RELEVANCE.toString())));
            properties.remove(XMLTags.RELEVANCE.toString());
        }
        // OOPN start
        if (variableElement.getAttribute(XMLAttributes.IS_INPUT.toString()) != null) {
            boolean isInput =
                    Boolean.parseBoolean(variableElement.getAttribute(XMLAttributes.IS_INPUT.toString()).getValue());
            node.setInput(isInput);
        }
        // OOPN end
        node.setComment(getComment(variableElement));
        // with the created node, put position (x, y)
        node.setCoordinateX(x);
        node.setCoordinateY(y);
        if (precision != null) {
            node.getVariable().setPrecision(precision);
        }
        for (String key : new ArrayList<>(properties.keySet())) {
            node.putAdditionalProperty(key, properties.get(key));
        }
    }
    
    protected static Variable getVariable(Element element, ProbNet probNet) {
        String variableName = element.getAttributeValue(XMLAttributes.NAME.toString());
        // strip the name from the time slice for backwards compatibility
        String timeSlice = element.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
        variableName = variableName.replace(" [" + timeSlice + "]", "");
        return (timeSlice == null) ? probNet.getVariable(variableName)
                : probNet.getVariable(variableName, Integer.parseInt(timeSlice));
    }
    
    protected String getElementName(Element element) {
        return element.getAttributeValue(XMLAttributes.NAME.toString());
    }
    
    protected static Double getXMLPrecision(Element variableElement) {
        Element precisionElement = variableElement.getChild(XMLTags.PRECISION.toString());
        Double precision = null;
        if (precisionElement != null) {
            precision = Double.valueOf(precisionElement.getText());
        }
        return precision;
    }
    
    protected static String getXMLPurpose(Element aditionalElement) {
        String purpose = "";
        if (aditionalElement != null) {
            Element purposeElement = aditionalElement.getChild(XMLTags.PURPOSE.toString());
            if (purposeElement != null) {
                purpose = purposeElement.getText();
            }
        }
        return purpose;
    }
    
    /**
     * @param aditionalElement . {@code Element}
     *
     * @return Double
     */
    protected static Double getXMLRelevance(Element aditionalElement) {
        double relevance = Node.DEFAULT_RELEVANCE;
        if (aditionalElement != null) {
            Element relevanceElement = aditionalElement.getChild(XMLTags.RELEVANCE.toString());
            if (relevanceElement != null) {
                relevance = Double.parseDouble(relevanceElement.getText());
            }
        }
        return relevance;
    }
    
    /**
     * Reads additionalProperties that have not a clear classification.
     *
     * @param variableElement . {@code Element}
     *
     * @return A {@code HashMap} with {@code key = String} and {@code value = Object}
     */
    protected LinkedHashMap<String, String> getProperties(Element variableElement) {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        Element others = variableElement.getChild(XMLTags.ADDITIONAL_PROPERTIES.toString());
        if (others == null) {
            others = variableElement.getChild(XMLTags.PROPERTIES.toString());
        }
        
        if (others != null) {
            List<Element> xmlProperties = getXMLChildren(others);
            for (Element xmlProperty : xmlProperties) { // additionalProperties
                String key = getElementName(xmlProperty);
                String value = xmlProperty.getAttributeValue(XMLAttributes.VALUE.toString());
                // try to discover the property type (double, boolean...)
                try { // try parse double
                    Double.parseDouble(value);
                    properties.put(key, value);
                } catch (NumberFormatException nd) {
                    try { // try parse int
                        Integer.parseInt(value);
                        properties.put(key, value);
                    } catch (NumberFormatException ni) { // try parse boolean
                        boolean bTrue = Boolean.parseBoolean(value);
                        boolean bFalse = "false".equalsIgnoreCase(value);
                        if (bTrue || bFalse) {
                            properties.put(key, Boolean.toString(bTrue));
                        } else { // Nor double nor integer nor boolean -> String
                            properties.put(key, value);
                        }
                    }
                }
            }
        }
        return properties;
    }
    
    /**
     * @param variableElement . {@code Element}
     *
     * @return variable type. {@code VariableType}
     */
    protected @Nullable VariableType getXMLVariableType(Element variableElement) {
        String role = getStringXMLPotentialType(variableElement);
        return Arrays.stream(VariableType.values()).filter(variableType -> role.contentEquals( variableType.toXMLTag()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * @param variableElement . {@code Element}
     *
     * @return node type. {@code NodeType}
     */
    protected NodeType getXMLNodeType(Element variableElement) {
        NodeType nodeType = null;
        // int i=0;
        String type = variableElement.getAttributeValue(XMLAttributes.ROLE.toString());
        for (NodeType iNodeType : NodeType.values()) {
            if (type.contentEquals(iNodeType.toString())) {
                nodeType = iNodeType;
                break;
            }
        }
        return nodeType;
    }
    
    /**
     * @param variableElement . {@code Element}
     *
     * @return Node X coordinate. {@code int}
     */
    protected static int getXMLXCoordinate(Element variableElement) {
        Element coordinatesElement = variableElement.getChild(XMLTags.COORDINATES.toString());
        int xCoordinate = 0;
        if (coordinatesElement != null) {
            String xString = coordinatesElement.getAttributeValue(XMLAttributes.X.toString());
            xCoordinate = Integer.parseInt(xString);
        }
        return xCoordinate;
    }
    
    /**
     * @param variableElement . {@code Element}
     *
     * @return Node Y coordinate. {@code int}
     */
    protected static int getXMLYCoordinate(Element variableElement) {
        Element coordinatesElement = variableElement.getChild(XMLTags.COORDINATES.toString());
        int yCoordinate = 0;
        if (coordinatesElement != null) {
            String yString = coordinatesElement.getAttributeValue(XMLAttributes.Y.toString());
            yCoordinate = Integer.parseInt(yString);
        }
        return yCoordinate;
    }
    
    /**
     * @param variableElement . {@code Element}
     *
     * @return variable states. {@code String[]}
     */
    protected State[] getXMLStates(Element variableElement) {
        List<Element> variableStatesElements = getStatesElements(variableElement);
        State[] states = new State[variableStatesElements.size()];
        int i = 0;
        for (Element stateElement : variableStatesElements) {
            String stateName = getStateName(stateElement);
            states[i++] = new State(stateName);
        }
        return states;
    }
    
    protected String getStateName(Element stateElement) {
        return stateElement.getAttributeValue(XMLAttributes.NAME.toString());
    }
    
    protected List<Element> getStatesElements(Element rootStates) {
        return rootStates.getChildren();
    }
    
    /**
     * @param variableElement . {@code Element}
     * @param variableName    . {@code String}
     *
     * @return A continuous variable. {@code Variable}
     */
    protected static Variable getXMLContinuousVariable(
            Element variableElement,
            String variableName) {
        
        boolean leftClosedDefined, leftClosed = false, rightClosedDefined, rightClosed = false;
        double min = Double.NEGATIVE_INFINITY;  // Default value
        double max = Double.POSITIVE_INFINITY;  // Default value
        
        // Get thresholds
        Element thresholdsElement = variableElement.getChild(XMLTags.THRESHOLDS.toString());
        List<Element> thresholds = thresholdsElement != null ? thresholdsElement.getChildren(XMLTags.THRESHOLD.toString()) : null;
        if ((thresholds != null) && (thresholds.size() > 1)) {
            
            // Left
            Element leftThreshold = thresholds.getFirst();
            String minString = leftThreshold.getAttributeValue(XMLAttributes.VALUE.toString());
            leftClosedDefined = minString.equalsIgnoreCase("-Infinity");
            if (!leftClosedDefined) {
                min = Double.parseDouble(minString);
                String leftClosedString = leftThreshold.getAttributeValue(XMLAttributes.BELONGS_TO.toString());
                leftClosed = leftClosedString != null && leftClosedString.contentEquals(XMLValues.LEFT.toString());
            }
            
            // Right
            Element rightThreshold = thresholds.get(1);
            String maxString = rightThreshold.getAttributeValue(XMLAttributes.VALUE.toString());
            rightClosedDefined = maxString.equalsIgnoreCase("+Infinity");
            if (!rightClosedDefined) {
                max = Double.parseDouble(maxString);
                String rightClosedString = rightThreshold.getAttributeValue(XMLAttributes.BELONGS_TO.toString());
                rightClosed = rightClosedString != null && rightClosedString.contentEquals(XMLValues.LEFT.toString());
            }
        }
        
        // Precision
        Element xMLPrecision = variableElement.getChild(XMLTags.PRECISION.toString());
        double precision = xMLPrecision != null ? Double.parseDouble(xMLPrecision.getText()) : 0.0;
        
        return new Variable(variableName, leftClosed, min, max, rightClosed, precision);
    }
    
    /**
     * @param states          the states
     * @param variableElement . {@code Element}
     * @param variableName    . {@code String}
     *
     * @return A discretized continuous variable. {@code Variable}
     */
    protected Variable getXMLDiscretizedVariable(
            Element variableElement,
            State[] states,
            String variableName) {
        
        Variable variable;
        // Continuous part. Continuous interval information is inferred from sub-intervals in discretized part (further on)
        if (variableElement != null) {
            List<Element> subIntervals = getXMLChildren(variableElement);
            if (subIntervals != null) {
                int numSubIntervals = subIntervals.size();
                double[] limits = new double[numSubIntervals];
                boolean[] belongsToLeftSide = new boolean[numSubIntervals];
                int numInterval = 0;
                for (Element subInterval : subIntervals) {
                    limits[numInterval] = Double.parseDouble(subInterval.getAttributeValue(
                            XMLAttributes.VALUE.toString()));
                    belongsToLeftSide[numInterval] = subInterval.getAttributeValue(
                            XMLAttributes.BELONGS_TO.toString()).equalsIgnoreCase("left");
                    numInterval++;
                }
                PartitionedInterval partitionedInterval = new PartitionedInterval(limits, belongsToLeftSide);
                variable = new Variable(variableName, states);
                // partitionedInterval, precision);
                // the order of the next two statement are important
                variable.setVariableType(VariableType.DISCRETIZED);
                variable.setPartitionedInterval(partitionedInterval);
            } else {
                variable = new Variable(variableName, states);
            }
        } else
            variable = new Variable(variableName, states);
        return variable;
    }
    
    /**
     * @param root    . {@code Element}
     * @param probNet . {@code ProbNet}
     *
     * @throws PGMXParserException PGMXParserException
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    protected void getLinks(
            Element root,
            ProbNet probNet)
            throws PGMXParserException {
        
        Element xmlLinksRoot = root.getChild(XMLTags.LINKS.toString());
        if (xmlLinksRoot != null) {
            List<Element> xmlLinks = getXMLChildren(xmlLinksRoot);
            for (Element xmlLink : xmlLinks) {
                // get link information from xmlLink
                List<Element> variablesElement = xmlLink.getChildren(XMLTags.VARIABLE.toString());
                // for the time being the name contains the time slice
                try {
                    Variable variable1 = getVariable(variablesElement.get(0), probNet);
                    Variable variable2 = getVariable(variablesElement.get(1), probNet);
                    Node node1 = probNet.getNode(variable1);
                    Node node2 = probNet.getNode(variable2);
                    boolean directed = xmlLink.getAttribute(XMLAttributes.DIRECTED.toString()).getBooleanValue();
                    // create link
                    probNet.addLink(variable1, variable2, directed);
                    // read link restriction potential
                    Element xmlPotential = xmlLink.getChild(XMLTags.POTENTIAL.toString());
                    if (xmlPotential != null) {
                        Potential potential = getPotential(xmlPotential, probNet);
                        Link<Node> link = probNet.getLink(node1, node2, directed);
                        link.initializesRestrictionsPotential();
                        link.setRestrictionsPotential((TablePotential) potential);
                    }
                    Element xmlRevelationCondition = xmlLink.getChild(XMLTags.REVELATION_CONDITIONS.toString());
                    if (xmlRevelationCondition != null) {
                        Link<Node> link = probNet.getLink(node1, node2, directed);
                        getRevelationConditions(xmlRevelationCondition, link);
                    }
                } catch (DataConversionException e) {
                    throw new PGMXParserException.DataCouldNotBeConverted(e.getMessage(), xmlLink);
                }
            }
        }
    }
    
    protected void getRevelationConditions(Element root, Link<Node> link)
            throws PGMXParserException.InvalidState {
        Node node = link.getFrom();
        Variable variable = node.getVariable();
        List<Element> xmlStates = root.getChildren(XMLTags.STATE.toString());
        for (Element elementState : xmlStates) {
            String stateName = getElementName(elementState);
            int stateIndex = variable.getStateIndex(stateName);
            if (stateIndex == -1) {
                throw new PGMXParserException.InvalidState(variable.getName(), stateName, elementState);
            }
            link.addRevealingState(variable.getStates()[stateIndex]);
        }
        List<Element> xmlThresholds = root.getChildren(XMLTags.THRESHOLD.toString());
        if (!xmlThresholds.isEmpty()) {
            for (int i = 0; i < xmlThresholds.size(); i += 2) {
                double[] limits = new double[2];
                boolean[] belongsToLeftSide = new boolean[2];
                for (int index = 0; index < 2; index++) {
                    Element subInterval = xmlThresholds.get(i + index);
                    limits[index] = Double.parseDouble(subInterval.getAttributeValue(XMLAttributes.VALUE.toString()));
                    belongsToLeftSide[index] =
                            subInterval.getAttributeValue(XMLAttributes.BELONGS_TO.toString()).contentEquals("left");
                }
                PartitionedInterval partitionedInterval = new PartitionedInterval(limits, belongsToLeftSide);
                link.addRevealingInterval(partitionedInterval);
            }
        }
    }
    
    /**
     * @param root    . {@code Element}
     * @param probNet . {@code ProbNet}
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    protected void getPotentials(Element root, ProbNet probNet)
            throws PGMXParserException {
        // Pool of names for those potentials declared in this net
        
        Element xmlPotentialsRoot = getRootPotentials(root);
        if (xmlPotentialsRoot != null) {
            
            List<Element> xmlPotentials = getPotentialsElements(xmlPotentialsRoot);
            for (Element xmlPotential : xmlPotentials) {
                Potential potential = getPotential(xmlPotential, probNet);
                probNet.addPotential(potential);
            }
        }
    }
    
    protected List<Element> getPotentialsElements(Element xmlPotentialsRoot) {
        return xmlPotentialsRoot.getChildren();
    }
    
    protected Element getRootPotentials(Element root) {
        return root.getChild(XMLTags.POTENTIALS.toString());
    }
    
    protected List<Variable> getReferencedVariables(Element xmlPotential, ProbNet probNet) {
        // get variables
        Element xmlRootVariables = getXMLPotentialVariables(xmlPotential);
        List<Variable> variables = new ArrayList<>();
        // List of variables referenced in this potential
        if (xmlRootVariables != null) {
            List<Element> xmlVariables = getXMLChildren(xmlRootVariables);
            int numVariables = xmlVariables.size();
            for (Element xmlVariable : xmlVariables) {
                Variable variable = getVariable(xmlVariable, probNet);
                if (!variables.contains(variable) || probNet.hasConstraintOfClass(OnlySelfLoopsWithEventAndChanceNodes.class)) {
                    variables.add(variable);
                }
            }
        }
        
        return variables;
    }
    
    /**
     * @param xmlPotential the xml potential
     * @param probNet      the prob net
     *
     * @return the result
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     * @author myebra
     */
    protected List<TreeADDBranch> getTreeADDBranches(Element xmlPotential, ProbNet probNet, Variable rootVariable,
                                                     PotentialRole xmlRole, List<Variable> variables)
            throws PGMXParserException {
        // get branches
        Element xmlRootBranches = xmlPotential.getChild(XMLTags.BRANCHES.toString());
        List<TreeADDBranch> branches = new ArrayList<>();
        List<Variable> parentVariables = new ArrayList<>(variables);
        // List of variables referenced in this potential
        if (xmlRootBranches != null) {
            List<Element> xmlBranches = getXMLChildren(xmlRootBranches);
            int numBranches = xmlBranches.size();
            for (Element xmlBranch : xmlBranches) {
                Element xmlSubpotential = xmlBranch.getChild(XMLTags.POTENTIAL.toString());
                Element xmlReference = xmlBranch.getChild(XMLTags.REFERENCE.toString());
                Element xmlLabel = xmlBranch.getChild(XMLTags.LABEL.toString());
                Potential potential = null;
                String reference = null;
                if (xmlSubpotential != null) {
                    potential = getPotential(xmlSubpotential, probNet, xmlRole);
                    // Hack for backwards compatibility of nested TreeADDPotentials
                    // that don't specify a list of variables
                    if (potential instanceof TreeADDPotential && potential.getVariables().isEmpty()
                            && !parentVariables.isEmpty()) {
                        potential.setVariables(parentVariables);
                    }
                } else if (xmlReference != null) {
                    reference = xmlReference.getText();
                } else {
                    throw new PGMXParserException.TreeADDWithoutPotentialOrReferenfe(xmlBranch);
                }
                TreeADDBranch branch = null;
                if (rootVariable.getVariableType() == VariableType.FINITE_STATES
                        || rootVariable.getVariableType() == VariableType.DISCRETIZED) {
                    List<State> states = getBranchStates(xmlBranch, rootVariable);
                    branch =
                            (potential != null) ? new TreeADDBranch(states, rootVariable, potential, parentVariables)
                                    : new TreeADDBranch(states, rootVariable, reference, parentVariables);
                } else if (rootVariable.getVariableType() == VariableType.NUMERIC) {
                    List<Threshold> thresholds = getThresholds(xmlBranch);
                    branch = (potential != null)
                            ? new TreeADDBranch(thresholds.get(0), thresholds.get(1), rootVariable,
                                                potential, parentVariables)
                            : new TreeADDBranch(thresholds.get(0), thresholds.get(1), rootVariable,
                                                reference, parentVariables);
                } else if (rootVariable.getVariableType() == VariableType.EVENT){
                    // - 18/04/2020 - added type event for be used in TreeWithEventsPotential
                    List<State> states = new ArrayList<>();
                    states.add(new State("event"));
                    branch =
                            ( potential != null ) ? new TreeADDBranch( Arrays.asList(rootVariable.getStates()), rootVariable, potential, parentVariables )
                                    : new TreeADDBranch( states, rootVariable, reference, parentVariables );
                }
                if (xmlLabel != null) {
                    branch.setLabel(xmlLabel.getText());
                }
                branches.add(branch);
            }
        }
        return branches;
    }
    
    /**
     * @param xmlBranch the xml branch
     *
     * @return the result
     *
     * @author myebra
     */
    protected List<Threshold> getThresholds(Element xmlBranch)
            throws PGMXParserException.TreeADDWithoutTwoThresholds {
        List<Threshold> thresholds = new ArrayList<>();
        Element xmlRootThresholds = xmlBranch.getChild(XMLTags.THRESHOLDS.toString());
        if (xmlRootThresholds != null) {
            List<Element> xmlThresholds = getXMLChildren(xmlRootThresholds);
            int numThresholds = xmlThresholds.size();
            if (numThresholds != 2)
                throw new PGMXParserException.TreeADDWithoutTwoThresholds(xmlThresholds, xmlBranch);
            for (int i = 0; i < numThresholds; i++) {
                Element xmlThreshold = xmlThresholds.get(i);
                float value = Float.parseFloat(xmlThreshold.getAttributeValue(XMLAttributes.VALUE.toString()));
                String belongsTo = xmlThreshold.getAttributeValue(XMLAttributes.BELONGS_TO.toString());
                boolean belongsToLeft = false;
                if (belongsTo.equals("left")) {
                    belongsToLeft = true;
                } else if (belongsTo.equals("right")) {
                    belongsToLeft = false;
                }
                Threshold threshold = new Threshold(value, belongsToLeft);
                thresholds.add(threshold);
            }
        }
        return thresholds;
    }
    
    /**
     * Gets the states of a branch.
     *
     * @param xmlBranch   {@code Element}
     * @param topVariable {@code Variable}
     *
     * @return {@code List} of {@code State}
     */
    protected List<State> getBranchStates(Element xmlBranch, Variable topVariable)
            throws PGMXParserException.FoundUnknownState {
        List<State> states = new ArrayList<>();
        Element xmlRootStates = xmlBranch.getChild(XMLTags.STATES.toString());
        if (xmlRootStates != null) {
            List<Element> xmlStates = getXMLChildren(xmlRootStates);
            for (Element xmlState : xmlStates) {
                String stateName = getElementName(xmlState);
                int stateIndex = topVariable.getStateIndex(stateName);
                if (stateIndex == -1) {
                    throw new PGMXParserException.FoundUnknownState(topVariable, stateName, xmlState);
                }
                states.add(topVariable.getStates()[stateIndex]);
            }
        }
        return states;
    }
    
    
    /**
     * @param xmlPotential the xml potential
     *
     * @return the result
     */
    protected Element getXMLRootTable(Element xmlPotential) {
        return xmlPotential.getChild(XMLTags.VALUES.toString());
    }
    
    /**
     * @param xmlRootUncertainTable the xml root uncertain table
     *
     * @return the result
     */
    protected static UncertainValue[] getUncertainValues(Element xmlRootUncertainTable) {
        List<Element> values = xmlRootUncertainTable.getChildren();
        int valuesSize = values.size();
        UncertainValue[] uncertainTable = new UncertainValue[valuesSize];
        for (int i = 0; i < valuesSize; i++) {
            Element xmlUncertainValue = values.get(i);
            uncertainTable[i] = getUncertainValue(xmlUncertainValue);
        }
        return uncertainTable;
    }
    
    /**
     * @param xmlUncertainValue the xml uncertain value
     *
     * @return the result
     */
    protected static UncertainValue getUncertainValue(Element xmlUncertainValue) {
        UncertainValue auxUncertainValue = null;
        String functionName = xmlUncertainValue.getAttributeValue(XMLAttributes.DISTRIBUTION.toString());
        if (functionName != null) {
            String name = xmlUncertainValue.getAttributeValue(XMLAttributes.NAME.toString());
            String[] arguments = xmlUncertainValue.getTextNormalize().split(" ");
            double[] parameters = new double[arguments.length];
            for (int i = 0; i < parameters.length; ++i) {
                parameters[i] = Double.parseDouble(arguments[i]);
            }
            ProbDensFunction function = ProbDensFunctionManager.getUniqueInstance()
                                                               .newInstance(functionName, parameters);
            auxUncertainValue = new UncertainValue(function, name);
        }
        return auxUncertainValue;
    }
    
    /**
     * @param probNet      . {@code ProbNet}
     * @param xmlPotential . {@code Element}
     *
     * @return {@code Potential}
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    protected Potential getPotential(Element xmlPotential, ProbNet probNet)
            throws PGMXParserException {
        return getPotential(xmlPotential, probNet, getPotentialRole(xmlPotential));
    }
    
    /**
     * @param xmlPotential the xml potential
     *
     * @return the result
     */
    protected PotentialRole getPotentialRole(Element xmlPotential) {
        String xmlPotentialRole = xmlPotential.getAttributeValue(XMLAttributes.ROLE.toString());
        PotentialRole xmlRole;
        if (xmlPotentialRole.equalsIgnoreCase("utility")) {
            xmlRole = PotentialRole.UNSPECIFIED;
        } else {
            xmlRole = PGMXReader_0_2.getPotentialRolByLabel(xmlPotentialRole);
        }
        return xmlRole;
    }
    
    /**
     * @param probNet      . {@code ProbNet}
     * @param xmlPotential . {@code Element}
     *
     * @return {@code Potential}
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    protected Potential getPotential(Element xmlPotential, ProbNet probNet, PotentialRole potentialRole)
            throws PGMXParserException {
        Potential potential;
        // get type and role of potential
        String sXmlPotentialType = getStringXMLPotentialType(xmlPotential);
        List<Variable> variables = getReferencedVariables(xmlPotential, probNet);
        Element xmlUtilityVariable = xmlPotential.getChild(XMLTags.UTILITY_VARIABLE.toString());
        boolean utilityVariableElement = false;
        if (xmlUtilityVariable != null) {
            Variable utilityVariable = getVariable(xmlUtilityVariable, probNet);
            variables.addFirst(utilityVariable);
            utilityVariableElement = true;
        }
        if (PotentialUtils.getNames(TablePotential.class).contains(sXmlPotentialType) && utilityVariableElement) {
            // Compatibility with old utility variable use
            potential = PGMXPotentialParsers.getExactDistrPotential(xmlPotential, probNet, potentialRole, variables);
        } else {
            potential = this.autoGetPotential(sXmlPotentialType, xmlPotential, probNet, potentialRole, variables);
        }
        Element xmlComment = xmlPotential.getChild(XMLTags.COMMENT.toString());
        if (xmlComment != null) {
            potential.setComment(xmlComment.getText());
        }
        return potential;
    }
    
    public final Potential autoGetPotential(String sXmlPotentialType, Element xmlPotential, ProbNet probNet,
                                            PotentialRole potentialRole, List<Variable> variables)
            throws PGMXParserException {
        Class<? extends Potential> potentialClass = PotentialUtils.getClassByName(sXmlPotentialType);
        if (potentialClass == null) {
            throw new PGMXParserException.PotentialTypeNotSupported(sXmlPotentialType, xmlPotential);
        }
        // Walk up the hierarchy: subclasses that inherit a parent's @PotentialType name
        // (e.g. UncertainTablePotential inheriting "Table") may not have their own entry.
        PotentialParser parser = null;
        for (Class<?> c = potentialClass; c != null && c != Object.class; c = c.getSuperclass()) {
            parser = this.potentialParsers.get(c);
            if (parser != null) break;
        }
        if (parser == null) {
            throw new PGMXParserException.PotentialTypeNotSupported(sXmlPotentialType, xmlPotential);
        }
        return parser.parse(xmlPotential, probNet, potentialRole, variables);
    }
    
    protected String getStringXMLPotentialType(Element xmlPotential) {
        return xmlPotential.getAttributeValue(XMLAttributes.TYPE.toString());
    }
    
    /** @author myebra */
    protected TreeADDPotential getTreeADDPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                   List<Variable> variables)
            throws PGMXParserException {
        Variable topVariable = null;
        // // Read the PotentialRole: innerPotentials of this TreeADD must have the
        // // same PotentialRole
        // if (xmlRole != PotentialRole.UTILITY && xmlRole != PotentialRole.CONDITIONAL_PROBABILITY) {
        // throw new PGMXParserException("Potential role "
        // + xmlRole.toString()
        // + "not supported inside TreeADD potential", xmlPotential);
        // }
        Element xmlTopVariable = xmlPotential.getChild(XMLTags.TOP_VARIABLE.toString());
        if (xmlTopVariable != null) {
            topVariable = getVariable(xmlTopVariable, probNet);
        }
        // Recursive reading of tree structure
        List<TreeADDBranch> branches = getTreeADDBranches(xmlPotential, probNet, topVariable, xmlRole, variables);
        // Builds the tree potential from the graph
        
        return new TreeADDPotential(variables, topVariable, xmlRole, branches);
    }
    
    protected static double[] parseDoubles(String string) {
        String[] sValues = string.split(" ");
        double[] table = new double[sValues.length];
        int i = 0;
        for (String sValue : sValues) {
            table[i++] = Double.parseDouble(sValue);
        }
        return table;
    }
    
    protected static VariableExpression[] getCovariates(Element xmlCovariates, List<Variable> variables) {
        VariableExpression[] covariates = new VariableExpression[xmlCovariates.getChildren().size()];
        int i = 0;
        for (Element xmlCovariate : xmlCovariates.getChildren()) {
            String xmlCovariateText = xmlCovariate.getText();
            var variableCovariate = variables.stream()
                                             .filter(var -> var.getName().equals(xmlCovariateText))
                                             .findFirst();
            if (variableCovariate.isPresent()) {
                covariates[i++] = variableCovariate.get().asVariableExpression();
            } else {
                covariates[i++] = new VariableExpression(variables, xmlCovariateText);
            }
        }
        return covariates;
    }
    
    /**
     * transform a text in a HTML string, if possible by a full substitution of the special characters "SymbolLT" and
     * "SymbolGT" in the equivalent "&lt;" and "&gt;" Please, pay attention that the equivalent format "&amp;lt;" and "&amp;gt;" are not
     * used here as JDOM is using the character "&amp;" to start a definition of an entity Ref class, so we need to avoid
     * it.
     */
    protected static String textToHtml(String htmlSection) {
        String result = htmlSection;
        result = result.replace("SymbolLT", "<");
        result = result.replace("SymbolGT", ">");
        return result;
    }
    
    // OOPN start
    
    // OOPN end
    
    /**
     * @param root    {@code Element}
     * @param probNet {@code ProbNet}
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    protected void getPolicies(Element root, ProbNet probNet)
            throws PGMXParserException {
        
        Element policiesRoot = root.getChild(XMLTags.POLICIES.toString());
        if (policiesRoot != null) {
            List<Element> xmlPotentialPolicies = getXMLChildren(policiesRoot);
            for (Element xmlPotential : xmlPotentialPolicies) {
                Potential potential = getPotential(xmlPotential, probNet);
                probNet.addPotential(potential);
            }
        }
    }
    
    /**
     * @param rootNetwork {@code Element}
     *
     * @return {@code Element}
     */
    protected Element getXMLVariables(Element rootNetwork) {
        
        return rootNetwork.getChild(XMLTags.VARIABLES.toString());
    }
    
    /**
     * @param xmlPotential {@code Element}
     *
     * @return {@code Element}
     */
    protected Element getXMLPotentialVariables(Element xmlPotential) {
        
        return xmlPotential.getChild(XMLTags.VARIABLES.toString());
    }
    
    /**
     * @param xmlRootVariables {@code Element}
     *
     * @return {@code List} of {@code Element}
     */
    protected List<Element> getXMLChildren(Element xmlRootVariables) {
        
        return xmlRootVariables.getChildren();
    }
    
    protected static PotentialRole getPotentialRolByLabel(String auxLabel) {
        for (PotentialRole role : PotentialRole.values()) {
            String u = role.toString();
            if (u.equals(auxLabel)) {
                return role;
            }
        }
        return null;
    }
    
    protected void setVariableType(Element root, ProbNet probNet) {
        Element variableTypeElement = root.getChild(XMLTags.VARIABLE_TYPE.toString());
        if (variableTypeElement == null) return;
        
        String variableTypeText = variableTypeElement.getText();
        if (variableTypeText == null) return;
        
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        
        PNConstraint variableTypeC = null;
        
        if (variableTypeText.equals(
                stringDatabase.getString("NetworkVariablesPanel.ConstraintVariableType.Items.onlydiscrete"))) {
            variableTypeC = new OnlyDiscreteVariables();
        } else if (variableTypeText.equals(
                stringDatabase.getString("NetworkVariablesPanel.ConstraintVariableType.Items.onlycontinuous"))) {
            variableTypeC = new OnlyContinuousVariables();
        }
        
        if (variableTypeC != null) {
            probNet.addConstraint(variableTypeC);
        }
    }
    
    protected void setDefaultStates(Element root, ProbNet probNet) {
        Element defaultStatesElement = root.getChild(XMLTags.DEFAULT_STATES.toString());
        if (defaultStatesElement == null) return;
        
        List<Element> XMLStates = defaultStatesElement.getChildren();
        State[] defaultStates = new State[XMLStates.size()];
        int i = 0;
        
        for (Element state : XMLStates) {
            defaultStates[i] = new State(state.getAttributeValue(XMLAttributes.NAME.toString()));
            i++;
        }
        
        probNet.setDefaultStates(defaultStates);
        
    }
    
    
}