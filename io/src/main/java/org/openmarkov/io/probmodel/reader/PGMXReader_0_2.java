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
import org.openmarkov.core.exception.InvalidArgumentException;
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
import org.openmarkov.core.io.format.annotation.XMLParsers;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.LinkOperations;
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
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
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
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PGMXReader_0_2 {
    
    /** What the messages of the reader call the text of an element. */
    protected static final String TEXT = "value";
    
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
        map.put(ConditionalGaussianPotential.class, this::getConditionalGaussianPotential);
        map.put(DiscretizedCauchyPotential.class, this::getDiscretizedCauchyPotential);
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
    public static Element getRootElement(URL networkSource) throws ProbNetParserException.XMLInvalid, ProbNetParserException.CannotOpenFile {
        SAXBuilder builder = XMLParsers.saxBuilder();
        builder.setJDOMFactory(new LocatedJDOMFactory());
        Document document;
        try {
            document = builder.build(networkSource);
        } catch (JDOMException e) {
            throw new ProbNetParserException.XMLInvalid(networkSource.getFile(), e);
        } catch (IOException e) {
            throw new ProbNetParserException.CannotOpenFile(networkSource.getFile(), e);
        }
        return document.getRootElement();
    }
    
    /**
     * @param root    Root element
     * @param netName Network name
     *
     * @return the network
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed or has no network
     */
    public ProbNet getProbNet(Element root, String netName) throws ProbNetParserException {
        return getProbNet(root, netName, new HashMap<>());
    }
    
    /**
     * @param root    Root element
     * @param netName Network name
     * @param classes Classes
     *
     * @return the network
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed or has no network
     */
    protected ProbNet getProbNet(Element root, String netName, Map<String, ProbNet> classes) throws ProbNetParserException {
        Element xMLProbNet = root.getChild(getStringTagNetwork());
        if (xMLProbNet == null) {
            throw new PGMXParserException.FileWithoutNetwork(netName, getStringTagNetwork(), root);
        }
        ProbNet probNet = initializeProbNet(xMLProbNet, netName);
        // Before the variables, which name their agent
        getAgents(xMLProbNet, probNet);
        getVariablesLinksAndPotentials(xMLProbNet, probNet);
        getNetworkAdvancedInformation(xMLProbNet, probNet, netName, classes);
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
                                                 Map<String, ProbNet> classes)
            throws PGMXParserException.DataNotValid, PGMXParserException.DataMissing {
        getTemporaUnit(xMLProbNet, probNet);
        getAdditionalProperties(xMLProbNet, probNet);
    }
    
    protected static void getTemporaUnit(Element xMLProbNet, ProbNet probNet) throws PGMXParserException.DataNotValid {
        Element temporalUnit = xMLProbNet.getChild(XMLTags.TIME_UNIT.toString());
        CycleLength temporalUnitProbNet = new CycleLength();
        if (temporalUnit != null) {
            String scaleAttribute = temporalUnit.getAttributeValue(XMLTags.VALUE.toString());
            if (scaleAttribute != null) {
                temporalUnitProbNet.setValue(doubleIn(temporalUnit, XMLTags.VALUE.toString(), scaleAttribute));
            }
            String unitAttribute = temporalUnit.getAttributeValue(XMLAttributes.UNIT.toString());
            if (unitAttribute != null) {
                temporalUnitProbNet.setUnit(enumIn(CycleLength.Unit.class, temporalUnit, XMLAttributes.UNIT.toString(),
                                                   unitAttribute));
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
            throws PGMXParserException.NoNetworkTypeFound, PGMXParserException.UnknownNetworkType,
                   PGMXParserException.ConstraintNotFound, PGMXParserException.DataMissing {
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
    
    protected void getAdditionalProperties(Element root, ProbNet probNet) throws PGMXParserException.DataMissing {
        Element xmlAdditionalProperties = root.getChild(XMLTags.ADDITIONAL_PROPERTIES.toString());
        if (xmlAdditionalProperties != null) {
            List<Element> propertiesListElement = getXMLChildren(xmlAdditionalProperties);
            if (propertiesListElement != null && !propertiesListElement.isEmpty()) {
                for (Element propertyElement : propertiesListElement) {
                    String propertyName = requiredName(propertyElement, "a property of the network");
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
                    String propertyName = requiredName(propertyElement, "a property of the network");
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
    protected void getAgents(Element root, ProbNet probNet) throws PGMXParserException.DataMissing {
        Element xmlAgentsRoot = root.getChild(XMLTags.AGENTS.toString());
        if (xmlAgentsRoot != null) {
            List<Element> xmlAgents = getXMLChildren(xmlAgentsRoot);
            ArrayList<StringWithProperties> agents = new ArrayList<>();
            for (Element agentElement : xmlAgents) {
                String agentName = requiredName(agentElement, "an agent");
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
    protected void getDecisionCriterion(Element root, ProbNet probNet) throws PGMXParserException.DataMissing {
        Element xmlCriteronRoot = root.getChild(XMLTags.DECISION_CRITERIA.toString());
        List<Criterion> criteria = new ArrayList<>();
        if (xmlCriteronRoot != null) {
            for (Element criterionElement : getXMLChildren(xmlCriteronRoot)) {
                String criterionName = requiredName(criterionElement, "a decision criterion");
                String criterionUnit = criterionElement.getAttributeValue(XMLAttributes.UNIT.toString());
                criteria.add(new Criterion(criterionName, criterionUnit));
            }
        }
        // A network that may have utility nodes always has a criterion
        if (criteria.isEmpty() && !probNet.hasConstraintOfClass(OnlyChanceNodes.class)) {
            criteria.add(new Criterion());
        }
        if (xmlCriteronRoot != null || !criteria.isEmpty()) {
            probNet.setDecisionCriteria(criteria);
        }
    }
    
    /**
     * @param agentElement . {@code Element}
     *
     * @return the properties of the agent; empty if it has none
     */
    protected Properties getAdditionalProperties(Element agentElement) throws PGMXParserException.DataMissing {
        Properties properties = new Properties();
        // The writers put them in a block, as they do with those of a variable
        Element block = agentElement.getChild(XMLTags.ADDITIONAL_PROPERTIES.toString());
        if (block == null) {
            block = agentElement.getChild(XMLTags.PROPERTIES.toString());
        }
        List<Element> propertiesListElement = block == null ? null : getXMLChildren(block);
        if (propertiesListElement != null) {
            for (Element propertyElement : propertiesListElement) {
                String propertyName = requiredName(propertyElement, "a property of an agent");
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
    protected void getInferenceOptions(Element root, ProbNet probNet)
            throws PGMXParserException.DiscountOutOfRange, PGMXParserException.DiscountMissing,
                   PGMXParserException.DataMissing, PGMXParserException.DataNotValid,
                   PGMXParserException.UnknownCriterion {
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
    protected void getMulticriteriaOptions(Element inferenceOptions, ProbNet probNet)
            throws PGMXParserException.DataMissing, PGMXParserException.DataNotValid,
                   PGMXParserException.UnknownCriterion {
        Element multicriteriaOptions = inferenceOptions.getChild(XMLTags.MULTICRITERIA_OPTIONS.toString());
        if (multicriteriaOptions != null) {
            Element multiCriteriaType = requiredChild(multicriteriaOptions, XMLTags.SELECTED_ANALYSIS_TYPE.toString());
            
            probNet.getInferenceOptions()
                   .getMultiCriteriaOptions()
                   .setMulticriteriaType(enumIn(MulticriteriaOptions.Type.class, multiCriteriaType, TEXT,
                                                multiCriteriaType.getText()));
            
            Element unicriteria = multicriteriaOptions.getChild(XMLTags.UNICRITERION.toString());
            if (unicriteria != null) {
                
                Element scalesTag = unicriteria.getChild(XMLTags.SCALES.toString());
                if (scalesTag != null) {
                    List<Element> scales = getXMLChildren(scalesTag);
                    for (Element element : scales) {
                        String criterionName = requiredAttribute(element, XMLTags.CRITERION.toString());
                        criterionOfTheNetwork(probNet, criterionName, element.getName(), element)
                                .setUnicriterizationScale(scaleIn(element));
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
                        String criterionName = requiredAttribute(element, XMLTags.CRITERION.toString());
                        criterionOfTheNetwork(probNet, criterionName, element.getName(), element)
                                .setCeScale(scaleIn(element));
                    }
                }
                
                Element ceCriteria = ceOptions.getChild(XMLTags.CE_CRITERIA.toString());
                if (ceCriteria != null) {
                    List<Element> ce_criterion = getXMLChildren(ceCriteria);
                    for (Element element : ce_criterion) {
                        String criterionName = requiredAttribute(element, XMLTags.CRITERION.toString());
                        Criterion criterion = criterionOfTheNetwork(probNet, criterionName, element.getName(), element);
                        String value = requiredAttribute(element, XMLTags.VALUE.toString());
                        criterion.setCECriterion(enumIn(Criterion.CECriterion.class, element,
                                                        XMLTags.VALUE.toString(), value));
                    }
                }
            }
        }
    }
    
    /**
     * @return the scale that {@code scaleElement} writes in its attribute {@code Value}
     */
    private static double scaleIn(Element scaleElement)
            throws PGMXParserException.DataMissing, PGMXParserException.DataNotValid {
        String value = requiredAttribute(scaleElement, XMLTags.VALUE.toString());
        return doubleIn(scaleElement, XMLTags.VALUE.toString(), value);
    }
    
    /**
     * Get Temporal Evolution Options from the PGMX
     *
     * @param inferenceOptions Inference Options
     * @param probNet          ProbNet
     */
    protected void getTemporalOptions(Element inferenceOptions, ProbNet probNet)
            throws PGMXParserException.DiscountOutOfRange, PGMXParserException.DiscountMissing,
                   PGMXParserException.DataMissing, PGMXParserException.DataNotValid,
                   PGMXParserException.UnknownCriterion {
        Element temporalOptions = inferenceOptions.getChild(XMLTags.TEMPORAL_OPTIONS.toString());
        if (temporalOptions != null) {
            
            Element slices = requiredChild(temporalOptions, XMLTags.SLICES.toString());
            probNet.getInferenceOptions().getTemporalOptions().setHorizon(integerIn(slices, TEXT, slices.getText()));
            
            Element transition = requiredChild(temporalOptions, XMLTags.TRANSITION.toString());
            probNet.getInferenceOptions()
                   .getTemporalOptions()
                   .setTransition(enumIn(TemporalOptions.TransitionTime.class, transition, TEXT, transition.getText()));
            
            // Without discount rates, each criterion keeps its default discount
            Element discountsTag = temporalOptions.getChild(XMLTags.DISCOUNT_RATES.toString());
            List<Element> discounts = discountsTag == null ? List.of() : getXMLChildren(discountsTag);
            for (Element element : discounts) {
                String criterionName = requiredAttribute(element, XMLTags.CRITERION.toString());
                Criterion criterion = criterionOfTheNetwork(probNet, criterionName, element.getName(), element);
                criterion.setDiscount(getXMLDiscount(element, criterion.getCriterionName()));
                String unit = requiredAttribute(element, XMLAttributes.UNIT.toString());
                criterion.setDiscountUnit(enumIn(CycleLength.DiscountUnit.class, element,
                                                 XMLAttributes.UNIT.toString(), unit));
            }
            
        }
        
    }
    
    /**
     * @return the child {@code childName} of {@code element}
     *
     * @throws PGMXParserException.DataMissing if {@code element} does not have it, or it has no text
     */
    private static Element requiredChild(Element element, String childName) throws PGMXParserException.DataMissing {
        Element child = element.getChild(childName);
        if (child == null || child.getText().isBlank()) {
            throw new PGMXParserException.DataMissing(element.getName(), childName, element);
        }
        return child;
    }
    
    /**
     * @return the value of the attribute {@code attributeName} of {@code element}
     *
     * @throws PGMXParserException.DataMissing if {@code element} does not have it, or it is blank
     */
    private static String requiredAttribute(Element element, String attributeName)
            throws PGMXParserException.DataMissing {
        String written = element.getAttributeValue(attributeName);
        if (written == null || written.isBlank()) {
            throw new PGMXParserException.DataMissing(element.getName(), attributeName, element);
        }
        return written;
    }
    
    /**
     * @return the constant of {@code type} that {@code written}, the {@code dataName} of {@code element}, names
     */
    private static <E extends Enum<E>> E enumIn(Class<E> type, Element element, String dataName, String written)
            throws PGMXParserException.DataNotValid {
        try {
            return Enum.valueOf(type, written.strip());
        } catch (IllegalArgumentException notAConstant) {
            throw new PGMXParserException.DataNotValid(element.getName(), dataName, written.strip(), element);
        }
    }
    
    /**
     * @return the number that {@code written}, the {@code dataName} of {@code element}, writes
     */
    private static double doubleIn(Element element, String dataName, String written)
            throws PGMXParserException.DataNotValid {
        try {
            return Double.parseDouble(written.strip());
        } catch (NumberFormatException notANumber) {
            throw new PGMXParserException.DataNotValid(element.getName(), dataName, written.strip(), element);
        }
    }
    
    /**
     * @return the integer that {@code written}, the {@code dataName} of {@code element}, writes
     */
    protected static int integerIn(Element element, String dataName, String written)
            throws PGMXParserException.DataNotValid {
        try {
            return parseInteger(written);
        } catch (NumberFormatException notAnInteger) {
            throw new PGMXParserException.DataNotValid(element.getName(), dataName, written.strip(), element);
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
            throws PGMXParserException.EvidenceIncompatibleInFile, PGMXParserException.DataMissing,
                   PGMXParserException.UnknownVariable, PGMXParserException.InvalidState,
                   PGMXParserException.DataNotValid {
        Element xMLEvidence = root.getChild(XMLTags.EVIDENCE.toString());
        List<EvidenceCase> evidence = new ArrayList<>();
        if (xMLEvidence != null) {
            List<Element> xmlEvidenceCases = getXMLChildren(xMLEvidence);
            for (Element xmlEvidenceCase : xmlEvidenceCases) {
                EvidenceCase evidenceCase = new EvidenceCase();
                List<Element> xmlFindings = getXMLChildren(xmlEvidenceCase);
                for (Element xmlFinding : xmlFindings) {
                    try {
                        String variableName = requiredAttribute(xmlFinding, "variable");
                        Variable variable = probNet.getVariable(variableName);
                        if (variable == null) {
                            throw new PGMXParserException.UnknownVariable("a finding", variableName, xmlFinding);
                        }
                        Finding finding;
                        if (variable.getVariableType() == VariableType.FINITE_STATES) {
                            String stateName = requiredAttribute(xmlFinding, "state");
                            if (!variable.containsState(stateName)) {
                                throw new PGMXParserException.InvalidState(variableName, stateName, xmlFinding);
                            }
                            finding = new Finding(variable, variable.getStateIndex(stateName));
                        } else {
                            double numericalValue = doubleIn(xmlFinding, "numericValue",
                                                             requiredAttribute(xmlFinding, "numericValue"));
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
    protected void getVariables(Element root, ProbNet probNet) throws PGMXParserException.WrongNumberOfValues, PGMXParserException.VariableHasNoStates, PGMXParserException.UnknownAgent, PGMXParserException.PrecisionOutOfRange, PGMXParserException.NodeWithoutRole, PGMXParserException.UnknownNodeRole, PGMXParserException.NodeWithoutVariableType, PGMXParserException.UnknownVariableType, PGMXParserException.DataMissing, PGMXParserException.UnknownCriterion {
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
            throws PGMXParserException.VariableHasNoStates, PGMXParserException.UnknownAgent, PGMXParserException.PrecisionOutOfRange,
                   PGMXParserException.NodeWithoutRole, PGMXParserException.UnknownNodeRole,
                   PGMXParserException.NodeWithoutVariableType, PGMXParserException.UnknownVariableType,
                   PGMXParserException.DataMissing, PGMXParserException.UnknownCriterion,
                   PGMXParserException.WrongNumberOfValues {
        for (Element variableElement : xmlVariables) {
            loadVariable(variableElement, probNet);
        }
    }
    
    protected void loadVariable(Element variableElement, ProbNet probNet)
            throws PGMXParserException.VariableHasNoStates, PGMXParserException.UnknownAgent, PGMXParserException.PrecisionOutOfRange,
                   PGMXParserException.NodeWithoutRole, PGMXParserException.UnknownNodeRole,
                   PGMXParserException.NodeWithoutVariableType, PGMXParserException.UnknownVariableType,
                   PGMXParserException.DataMissing, PGMXParserException.UnknownCriterion,
                   PGMXParserException.WrongNumberOfValues {
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
    /**
     * @return the agent of {@code probNet} that {@code agentElement} names
     * @throws PGMXParserException.UnknownAgent if the network has no agent with that name
     */
    /**
     * @return the decision criterion of the network named {@code criterionName}
     *
     * @throws PGMXParserException.UnknownCriterion if the network has none with that name
     */
    private static Criterion criterionOfTheNetwork(ProbNet probNet, String criterionName, String elementName,
                                                   Element element) throws PGMXParserException.UnknownCriterion {
        for (Criterion criterion : probNet.getDecisionCriteria()) {
            if (criterion.isNamed(criterionName)) {
                return criterion;
            }
        }
        throw new PGMXParserException.UnknownCriterion(elementName, criterionName, element);
    }
    
    private static StringWithProperties agentOfTheNetwork(ProbNet probNet, String variableName, Element agentElement)
            throws PGMXParserException.UnknownAgent {
        String agentName = agentElement.getAttributeValue(XMLAttributes.NAME.toString());
        if (probNet.getAgents() != null) {
            for (StringWithProperties agent : probNet.getAgents()) {
                if (agent.getString().equals(agentName)) {
                    return agent;
                }
            }
        }
        throw new PGMXParserException.UnknownAgent(variableName, agentName, agentElement);
    }
    
    protected void loadVariableAdvancedInformation(
            Element variableElement,
            ProbNet probNet,
            VariableType variableType,
            NodeType nodeType,
            String variableName)
            throws PGMXParserException.VariableHasNoStates, PGMXParserException.UnknownAgent,
                   PGMXParserException.PrecisionOutOfRange, PGMXParserException.DataMissing,
                   PGMXParserException.UnknownCriterion, PGMXParserException.WrongNumberOfValues {
        
        String stringTimeSlice = variableElement.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
        String nameWithTimeSlice = variableName;
        if (stringTimeSlice != null) {
            variableName = variableName.replace(" [" + stringTimeSlice + "]", "");
            nameWithTimeSlice = variableName + " [" + stringTimeSlice + "]";
        }
        
        // Coordinates
        int x = getXMLXCoordinate(variableElement);
        int y = getXMLYCoordinate(variableElement);
        
        Double precision = getXMLPrecision(variableElement, nameWithTimeSlice);
        
        // States & intervals
        State[] states = null;
        Variable variable;
        
        if ((nodeType == NodeType.CHANCE) || (nodeType == NodeType.DECISION)) {
            if ((variableType == VariableType.FINITE_STATES) || (variableType == VariableType.DISCRETIZED)) {
                Element statesElement = getXMLRootStates(variableElement);
                if (statesElement != null) { // jlgozalo. 25/10/2009
                    states = getXMLStates(statesElement); // previously without null control
                    for (State state : states) {
                        if (state.getName() == null) {
                            throw new PGMXParserException.DataMissing("a state of " + nameWithTimeSlice, "name",
                                                                      statesElement);
                        }
                    }
                } else {
                    throw new PGMXParserException.VariableHasNoStates(nameWithTimeSlice, variableElement);
                }
            }
            if (variableType == VariableType.FINITE_STATES) {
                variable = new Variable(variableName, states);
            } else {
                if (variableType == VariableType.NUMERIC) {
                    variable = getXMLContinuousVariable(variableElement, variableName, precision == null ? 0.0 : precision);
                } else { // DISCRETIZED variable. Read sub-intervals
                    Element thresholdsElement = variableElement.getChild(XMLTags.THRESHOLDS.toString());
                    if (thresholdsElement != null && variableType == VariableType.DISCRETIZED) {
                        checkNumberOfValues(getXMLChildren(thresholdsElement).size(), states.length + 1,
                                            "the variable " + nameWithTimeSlice, thresholdsElement);
                    }
                    variable = getXMLDiscretizedVariable(thresholdsElement, states, variableName);
                }
            }
        } else { // utility only??
            variable = new Variable(variableName);
            // decision criterion
            if (nodeType == NodeType.UTILITY) {
                Element decisionCriteria = variableElement.getChild(XMLTags.CRITERION.toString());
                if (decisionCriteria != null) {
                    String criterionName = requiredAttribute(decisionCriteria, XMLAttributes.NAME.toString());
                    variable.setDecisionCriterion(criterionOfTheNetwork(probNet, criterionName,
                                                                        "Variable " + nameWithTimeSlice,
                                                                        decisionCriteria));
                    // If the node has not have Decision Criteria, set the first of the probNet.
                } else {
                    variable.setDecisionCriterion(probNet.getDecisionCriteria().getFirst());
                }
            }
        }
        
        // Set timeSlice
        if (stringTimeSlice != null) {
            variable.setTimeSlice(parseInteger(stringTimeSlice));
        }
        
        // Set unit
        Element xMLUnit = variableElement.getChild(XMLTags.UNIT.toString());
        if (xMLUnit != null) {
            String unit = xMLUnit.getText();
            variable.setUnit(new StringWithProperties(unit));
        }
        
        Element xMLAgent = variableElement.getChild(XMLTags.AGENT.toString());
        if (xMLAgent != null) {
            variable.setAgent(agentOfTheNetwork(probNet, nameWithTimeSlice, xMLAgent));
        }
        
        // other additionalProperties
        LinkedHashMap<String, String> properties = getProperties(variableElement);
        
        // Remove obsolete properties
        if (properties.get(XMLTags.TITLE.toString()) != null) {
            properties.remove(XMLTags.TITLE.toString());
        }
        if (properties.get(XMLTags.RELEVANCE.toString()) != null) {
            String relevance = properties.get(XMLTags.RELEVANCE.toString());
            if (Double.parseDouble(relevance) == Node.DEFAULT_RELEVANCE) {
                properties.remove(XMLTags.RELEVANCE.toString());
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
    
    /** @return the variable of {@code probNet} that {@code element} names, or null if the network has none */
    protected static @Nullable Variable findVariable(Element element, ProbNet probNet)
            throws PGMXParserException.DataMissing {
        String variableName = requiredAttribute(element, XMLAttributes.NAME.toString());
        // strip the name from the time slice for backwards compatibility
        String timeSlice = element.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
        variableName = variableName.replace(" [" + timeSlice + "]", "");
        return (timeSlice == null) ? probNet.getVariable(variableName)
                : probNet.getVariable(variableName, parseInteger(timeSlice));
    }

    /**
     * @param place where the file names the variable, for the message, as in "a link"
     * @return the variable of {@code probNet} that {@code element} names
     */
    protected static Variable getVariable(Element element, ProbNet probNet, String place)
            throws PGMXParserException.DataMissing, PGMXParserException.UnknownVariable {
        Variable variable = findVariable(element, probNet);
        if (variable == null) {
            throw new PGMXParserException.UnknownVariable(place, nodeNameOf(element), element);
        }
        return variable;
    }

    /** @throws PGMXParserException.WrongNumberOfValues if {@code element}, in {@code place}, does not have one of the {@code accepted} numbers of numbers */
    static void checkNumberOfValues(int found, int[] accepted, String place, Element element)
            throws PGMXParserException.WrongNumberOfValues {
        if (Arrays.stream(accepted).noneMatch(size -> size == found)) {
            String expected = Arrays.stream(accepted).mapToObj(String::valueOf).collect(Collectors.joining(" or "));
            throw new PGMXParserException.WrongNumberOfValues(place, element.getName(), expected, found, element);
        }
    }

    /** @throws PGMXParserException.WrongNumberOfValues if {@code element}, in {@code place}, does not have {@code expected} numbers */
    static void checkNumberOfValues(int found, int expected, String place, Element element)
            throws PGMXParserException.WrongNumberOfValues {
        checkNumberOfValues(found, new int[]{expected}, place, element);
    }

    /** @return how a message names the relation whose variables, its node first, are {@code variables} */
    static String relationOf(List<Variable> variables) {
        return variables.isEmpty() ? "a relation" : "the relation of " + variables.getFirst().getName();
    }
    
    /** @return the name of {@code element}, which the message calls {@code description} if it has none */
    protected String requiredName(Element element, String description) throws PGMXParserException.DataMissing {
        String name = getElementName(element);
        if (name == null) {
            throw new PGMXParserException.DataMissing(description, "name", element);
        }
        return name;
    }

    protected String getElementName(Element element) {
        return element.getAttributeValue(XMLAttributes.NAME.toString());
    }
    
    /**
     * @return the precision of the variable, or null if the file does not give it
     *
     * @throws PGMXParserException.PrecisionOutOfRange if it is not a finite number not below 0, the only ones the
     *                                                 values of the variable can be rounded to
     */
    protected static Double getXMLPrecision(Element variableElement, String variableName)
            throws PGMXParserException.PrecisionOutOfRange {
        Element precisionElement = variableElement.getChild(XMLTags.PRECISION.toString());
        if (precisionElement == null) {
            return null;
        }
        double precision;
        try {
            precision = Double.parseDouble(precisionElement.getText().strip());
        } catch (NumberFormatException notANumber) {
            precision = Double.NaN;
        }
        if (!Double.isFinite(precision) || precision < 0) {
            throw new PGMXParserException.PrecisionOutOfRange(variableName, precisionElement.getText().strip(),
                                                              precisionElement);
        }
        return precision;
    }
    
    /**
     * @return the discount that {@code discountElement} writes; a number that is not finite, which the window of the
     * inference options could not show, no number at all, or no value, is refused with a message that names the
     * criterion
     */
    private static double getXMLDiscount(Element discountElement, String criterionName)
            throws PGMXParserException.DiscountOutOfRange, PGMXParserException.DiscountMissing {
        String written = discountElement.getAttributeValue(XMLAttributes.VALUE.toString());
        if (written == null) {
            throw new PGMXParserException.DiscountMissing(criterionName, discountElement);
        }
        double discount;
        try {
            discount = Double.parseDouble(written.strip());
        } catch (NumberFormatException notANumber) {
            discount = Double.NaN;
        }
        if (!Double.isFinite(discount)) {
            throw new PGMXParserException.DiscountOutOfRange(criterionName, written.strip(), discountElement);
        }
        return discount;
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
    protected LinkedHashMap<String, String> getProperties(Element variableElement)
            throws PGMXParserException.DataMissing {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        Element others = variableElement.getChild(XMLTags.ADDITIONAL_PROPERTIES.toString());
        if (others == null) {
            others = variableElement.getChild(XMLTags.PROPERTIES.toString());
        }
        
        if (others != null) {
            List<Element> xmlProperties = getXMLChildren(others);
            for (Element xmlProperty : xmlProperties) { // additionalProperties
                String key = requiredName(xmlProperty, "a property of " + nodeNameOf(variableElement));
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
    
    /** @return the variable type written in the type of {@code variableElement}, which must match it exactly */
    protected VariableType getXMLVariableType(Element variableElement)
            throws PGMXParserException.NodeWithoutVariableType, PGMXParserException.UnknownVariableType {
        String type = getStringXMLPotentialType(variableElement);
        if (type == null) {
            throw new PGMXParserException.NodeWithoutVariableType(nodeNameOf(variableElement), variableElement);
        }
        for (VariableType variableType : VariableType.values()) {
            if (type.equals(variableType.toXMLTag())) {
                return variableType;
            }
        }
        throw new PGMXParserException.UnknownVariableType(type, nodeNameOf(variableElement), variableElement);
    }
    
    /** @return the node type written in the role of {@code variableElement}, which must match it exactly */
    protected NodeType getXMLNodeType(Element variableElement)
            throws PGMXParserException.NodeWithoutRole, PGMXParserException.UnknownNodeRole {
        String role = variableElement.getAttributeValue(XMLAttributes.ROLE.toString());
        if (role == null) {
            throw new PGMXParserException.NodeWithoutRole(nodeNameOf(variableElement), variableElement);
        }
        for (NodeType nodeType : NodeType.values()) {
            if (role.equals(nodeType.toString())) {
                return nodeType;
            }
        }
        throw new PGMXParserException.UnknownNodeRole(role, nodeNameOf(variableElement), variableElement);
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
            xCoordinate = parseInteger(xString);
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
            yCoordinate = parseInteger(yString);
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
     * @param precision       the precision of the variable, already read
     *
     * @return A continuous variable. {@code Variable}
     */
    protected static Variable getXMLContinuousVariable(
            Element variableElement,
            String variableName,
            double precision) {
        
        boolean leftClosedDefined, lowerBelongsToLeft = false, rightClosedDefined, rightClosed = false;
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
                lowerBelongsToLeft = leftClosedString != null && leftClosedString.contentEquals(XMLValues.LEFT.toString());
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
        
        // A lower limit that belongs to the left is out of the domain
        return new Variable(variableName, !lowerBelongsToLeft, min, max, rightClosed, precision);
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
                    Variable variable1 = getVariable(variablesElement.get(0), probNet, "a link");
                    Variable variable2 = getVariable(variablesElement.get(1), probNet, "a link");
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
                        LinkOperations.initializesRestrictionsPotential(link);
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
                checkEveryReferenceHasItsLabel(potential, xmlPotential);
                probNet.addPotential(potential);
            }
        }
    }
    
    /**
     * @throws PGMXParserException.ReferenceWithoutLabel if a branch of a tree of {@code potential} references a label
     *                                                   that no branch of that tree has
     */
    private static void checkEveryReferenceHasItsLabel(Potential potential, Element xmlPotential)
            throws PGMXParserException.ReferenceWithoutLabel {
        Collection<TreeADDPotential> trees = switch (potential) {
            case TreeADDPotential tree -> List.of(tree);
            case TreeWithEventsPotential treeWithEvents -> treeWithEvents.getTrees().values();
            case TreeWithExcludedEventsPotential treeWithExcludedEvents ->
                    Stream.ofNullable(treeWithExcludedEvents.getNoEventTree()).toList();
            default -> List.of();
        };
        for (TreeADDPotential tree : trees) {
            List<TreeADDBranch> referencesWithoutLabel = tree.getReferencesWithoutLabel();
            if (!referencesWithoutLabel.isEmpty()) {
                throw new PGMXParserException.ReferenceWithoutLabel(potential.getVariables().getFirst().getName(),
                                                                    referencesWithoutLabel.getFirst().getReference(),
                                                                    xmlPotential);
            }
        }
    }
    
    protected List<Element> getPotentialsElements(Element xmlPotentialsRoot) {
        return xmlPotentialsRoot.getChildren();
    }
    
    protected Element getRootPotentials(Element root) {
        return root.getChild(XMLTags.POTENTIALS.toString());
    }
    
    protected List<Variable> getReferencedVariables(Element xmlPotential, ProbNet probNet)
            throws PGMXParserException.DataMissing, PGMXParserException.UnknownVariable {
        // get variables
        Element xmlRootVariables = getXMLPotentialVariables(xmlPotential);
        List<Variable> variables = new ArrayList<>();
        // List of variables referenced in this potential
        if (xmlRootVariables != null) {
            List<Element> xmlVariables = getXMLChildren(xmlRootVariables);
            int numVariables = xmlVariables.size();
            for (Element xmlVariable : xmlVariables) {
                Variable variable = getVariable(xmlVariable, probNet, relationOf(variables));
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
        return this.getBranches(xmlPotential.getChild(XMLTags.BRANCHES.toString()), probNet, rootVariable, xmlRole,
                                variables);
    }
    
    /**
     * @param xmlRootBranches the element with the branches of a tree, or null if it has none
     *
     * @return the branches of the tree split on {@code rootVariable}
     *
     * @throws PGMXParserException if the PGMX file cannot be parsed
     */
    protected List<TreeADDBranch> getBranches(Element xmlRootBranches, ProbNet probNet, Variable rootVariable,
                                              PotentialRole xmlRole, List<Variable> variables)
            throws PGMXParserException {
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
                    throw new PGMXParserException.TreeADDWithoutPotentialOrReference(xmlBranch);
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
                double value = Double.parseDouble(xmlThreshold.getAttributeValue(XMLAttributes.VALUE.toString()));
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
                parameters[i] = finiteNumber(arguments[i]);
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
        return getPotentialWithRole(xmlPotential, probNet, roleForItsNode(getPotentialRole(xmlPotential), xmlPotential, probNet));
    }
    
    private Potential getPotentialWithRole(Element xmlPotential, ProbNet probNet, PotentialRole role)
            throws PGMXParserException {
        try {
            return getPotential(xmlPotential, probNet, role);
        } catch (InvalidArgumentException invalid) {
            throw notFinite(invalid, xmlPotential);
        }
    }
    
    /** @return the role of utility if {@code written} is that of a probability and the node of the relation is a utility; else {@code written} */
    private PotentialRole roleForItsNode(PotentialRole written, Element xmlPotential, ProbNet probNet)
            throws PGMXParserException {
        if (written != PotentialRole.CONDITIONAL_PROBABILITY) {
            return written;
        }
        Element xmlNode = xmlPotential.getChild(XMLTags.UTILITY_VARIABLE.toString());
        if (xmlNode == null) {
            Element xmlVariables = xmlPotential.getChild(XMLTags.VARIABLES.toString());
            xmlNode = xmlVariables == null ? null : xmlVariables.getChild(XMLTags.VARIABLE.toString());
        }
        if (xmlNode == null) {
            return written;
        }
        Node node = probNet.getNode(getVariable(xmlNode, probNet, "a relation"));
        return node != null && node.getNodeType() == NodeType.UTILITY ? PotentialRole.UNSPECIFIED : written;
    }
    
    private static final String NOT_FINITE = "it is not a finite number";
    
    /**
     * @return the number that {@code written} writes
     *
     * @throws InvalidArgumentException if it is NaN or an infinite; {@link #notFinite} tells it from any other
     */
    static double finiteNumber(String written) {
        double number = Double.parseDouble(written);
        if (!Double.isFinite(number)) {
            throw new InvalidArgumentException((Object) written.strip(), NOT_FINITE);
        }
        return number;
    }
    
    /**
     * @return the refusal of the file for the number that {@link #finiteNumber} did not take in {@code xmlPotential}
     *
     * @throws InvalidArgumentException {@code invalid} itself, if it does not come from {@link #finiteNumber}
     */
    static PGMXParserException.NumberNotFinite notFinite(InvalidArgumentException invalid, Element xmlPotential) {
        if (!NOT_FINITE.equals(invalid.reason)) {
            throw invalid;
        }
        return new PGMXParserException.NumberNotFinite(nodeOfTheRelation(xmlPotential), String.valueOf(invalid.value),
                                                       xmlPotential);
    }
    
    /** @return the name of the node of {@code xmlPotential}: its utility variable if it has one, else its first variable */
    static String nodeOfTheRelation(Element xmlPotential) {
        Element utilityVariable = xmlPotential.getChild(XMLTags.UTILITY_VARIABLE.toString());
        return utilityVariable == null ? firstVariableOf(xmlPotential) : nodeNameOf(utilityVariable);
    }
    
    /**
     * @param xmlPotential the xml potential
     *
     * @return the result
     */
    protected PotentialRole getPotentialRole(Element xmlPotential) throws PGMXParserException {
        String xmlPotentialRole = xmlPotential.getAttributeValue(XMLAttributes.ROLE.toString());
        if (xmlPotentialRole == null) {
            throw new PGMXParserException.PotentialWithoutRole(xmlPotential);
        }
        PotentialRole xmlRole;
        if (xmlPotentialRole.equalsIgnoreCase("utility")) {
            xmlRole = PotentialRole.UNSPECIFIED;
        } else {
            xmlRole = PGMXReader_0_2.getPotentialRolByLabel(xmlPotentialRole);
            if (xmlRole == null) {
                throw new PGMXParserException.UnknownPotentialRole(xmlPotentialRole, firstVariableOf(xmlPotential), xmlPotential);
            }
        }
        return xmlRole;
    }

    /** @return the name of the first variable of {@code xmlPotential}, which is its node, or "?" if it has none */
    static String firstVariableOf(Element xmlPotential) {
        Element variables = xmlPotential.getChild(XMLTags.VARIABLES.toString());
        Element variable = variables == null ? null : variables.getChild(XMLTags.VARIABLE.toString());
        return variable == null ? "?" : nodeNameOf(variable);
    }

    /** @return the name of the node of {@code variable}, with its time slice if it has one */
    static String nodeNameOf(Element variable) {
        String name = variable.getAttributeValue(XMLAttributes.NAME.toString());
        String timeSlice = variable.getAttributeValue(XMLAttributes.TIME_SLICE.toString());
        return (timeSlice == null || name.endsWith(" [" + timeSlice + "]")) ? name : name + " [" + timeSlice + "]";
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
            Variable utilityVariable = getVariable(xmlUtilityVariable, probNet, "a relation");
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
        if (sXmlPotentialType == null) {
            throw new PGMXParserException.PotentialWithoutType(nodeOf(variables), xmlPotential);
        }
        PotentialParser parser = this.parserOf(sXmlPotentialType);
        if (parser == null) {
            throw new PGMXParserException.PotentialTypeNotSupported(sXmlPotentialType, nodeOf(variables), xmlPotential);
        }
        return parser.parse(xmlPotential, probNet, potentialRole, variables);
    }

    /** @return the name of the first variable of a relation, which is its node, or "?" if it has none */
    private static String nodeOf(List<Variable> variables) {
        return variables.isEmpty() ? "?" : variables.getFirst().getName();
    }
    
    /**
     * @param sXmlPotentialType the type of a relation, as a file names it
     *
     * @return whether this reader can read a relation of that type
     */
    public boolean canReadPotentialType(String sXmlPotentialType) {
        return this.parserOf(sXmlPotentialType) != null;
    }
    
    /** @return the parser of the relations of the type {@code sXmlPotentialType}, or null if there is none */
    private PotentialParser parserOf(String sXmlPotentialType) {
        Class<? extends Potential> potentialClass = PotentialUtils.getClassByName(sXmlPotentialType);
        // Walk up the hierarchy: subclasses that inherit a parent's @PotentialType name
        // (e.g. UncertainTablePotential inheriting "Table") may not have their own entry.
        for (Class<?> c = potentialClass; c != null && c != Object.class; c = c.getSuperclass()) {
            PotentialParser parser = this.potentialParsers.get(c);
            if (parser != null) {
                return parser;
            }
        }
        return null;
    }
    
    protected String getStringXMLPotentialType(Element xmlPotential) {
        return xmlPotential.getAttributeValue(XMLAttributes.TYPE.toString());
    }
    
    /** Reads a Conditional Gaussian, whose mean and variance are relations nested in {@code <Mean>} and {@code <Variance>}. */
    protected Potential getConditionalGaussianPotential(Element xmlPotential, ProbNet probNet, PotentialRole role,
                                                        List<Variable> variables) throws PGMXParserException {
        ConditionalGaussianPotential gaussian = new ConditionalGaussianPotential(variables, role);
        gaussian.setMean(getParameter(xmlPotential, XMLTags.MEAN, probNet, variables));
        gaussian.setVariance(getParameter(xmlPotential, XMLTags.VARIANCE, probNet, variables));
        return gaussian;
    }

    /** Reads a Discretized Cauchy, whose median and scale are relations nested in {@code <Median>} and {@code <Scale>}. */
    protected Potential getDiscretizedCauchyPotential(Element xmlPotential, ProbNet probNet, PotentialRole role,
                                                      List<Variable> variables) throws PGMXParserException {
        DiscretizedCauchyPotential cauchy = new DiscretizedCauchyPotential(variables, role);
        cauchy.setMedian(getParameter(xmlPotential, XMLTags.MEDIAN, probNet, variables));
        cauchy.setScale(getParameter(xmlPotential, XMLTags.SCALE, probNet, variables));
        return cauchy;
    }

    /** @return the relation nested in the child {@code tag} of {@code xmlPotential}, which the file must have */
    private Potential getParameter(Element xmlPotential, XMLTags tag, ProbNet probNet, List<Variable> variables)
            throws PGMXParserException {
        Element parameter = xmlPotential.getChild(tag.toString());
        Element nestedPotential = parameter == null ? null : parameter.getChild(XMLTags.POTENTIAL.toString());
        if (nestedPotential == null) {
            throw new PGMXParserException.RelationParameterMissing(variables.getFirst().getName(), tag.toString(),
                                                                   xmlPotential);
        }
        // The variables of a nested relation are the parents, so the node is named here
        String type = getStringXMLPotentialType(nestedPotential);
        if (type == null) {
            throw new PGMXParserException.PotentialWithoutType(nodeOf(variables), nestedPotential);
        }
        if (!canReadPotentialType(type)) {
            throw new PGMXParserException.PotentialTypeNotSupported(type, nodeOf(variables), nestedPotential);
        }
        return getPotential(nestedPotential, probNet, PotentialRole.CONDITIONAL_PROBABILITY);
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
            topVariable = getVariable(xmlTopVariable, probNet, relationOf(variables));
        }
        // Recursive reading of tree structure
        List<TreeADDBranch> branches = getTreeADDBranches(xmlPotential, probNet, topVariable, xmlRole, variables);
        // Builds the tree potential from the graph
        
        return new TreeADDPotential(variables, topVariable, xmlRole, branches);
    }
    
    /**
     * @return the integer written in {@code text}, ignoring the blanks around it
     */
    protected static int parseInteger(String text) {
        return Integer.parseInt(text.strip());
    }
    
    protected static double[] parseDoubles(String string) {
        if (string.isBlank()) {
            return new double[0];
        }
        String[] sValues = string.split(" ");
        double[] table = new double[sValues.length];
        int i = 0;
        for (String sValue : sValues) {
            table[i++] = finiteNumber(sValue);
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
                // A relation of this element is a policy, even if the file gives it the role of a probability
                PotentialRole role = getPotentialRole(xmlPotential);
                Potential potential = getPotentialWithRole(xmlPotential, probNet,
                        role == PotentialRole.CONDITIONAL_PROBABILITY ? PotentialRole.POLICY : role);
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
    
    protected void setDefaultStates(Element root, ProbNet probNet) throws PGMXParserException.DataMissing {
        Element defaultStatesElement = root.getChild(XMLTags.DEFAULT_STATES.toString());
        if (defaultStatesElement == null) return;
        
        List<Element> XMLStates = defaultStatesElement.getChildren();
        State[] defaultStates = new State[XMLStates.size()];
        int i = 0;
        
        for (Element state : XMLStates) {
            defaultStates[i] = new State(requiredName(state, "a default state of the network"));
            i++;
        }
        
        probNet.setDefaultStates(defaultStates);
        
    }
    
    
}
