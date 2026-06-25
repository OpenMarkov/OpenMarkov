/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.xmlbif;

import org.jdom2.Element;

import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.xmlbif.strings.XMLBIFTags;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@FormatType(name = "XMLBIFReader", version = "", extension = "xml", description = "Weka")
public class XMLBIFReader extends PGMXReader_0_2 {

    @Override
	protected Element getRootPotentials(Element root) {
		return root;
	}

	@Override
	public PGMXReader.NetworkAndEvidence read(URL networkSource) throws ProbNetParserException {
		return new PGMXReader.NetworkAndEvidence(this.getProbNet(getRootElement(networkSource), networkSource.getFile()), null);
	}
	
	@Override
	protected String getStringTagNetwork() {
		return XMLBIFTags.NETWORK.toString();
	}

    @Override
	protected ProbNet initializeProbNet(Element xMLProbNet, String netName) {
    	return new ProbNet();
	}


	@Override
	protected Element getXMLVariables(Element rootNetwork) {
		return rootNetwork;
	}


	@Override
	protected List<Element> getVariablesElements(Element xmlVariablesRoot) {
		return xmlVariablesRoot.getChildren(XMLBIFTags.VARIABLE.toString());
	}

	@Override
	protected List<Element> getPotentialsElements(Element xmlPotentialsRoot) {
		
		return xmlPotentialsRoot.getChildren(XMLBIFTags.DEFINITION.toString());
	}

	@Override
	protected PotentialRole getPotentialRole(Element xmlPotential) {
		return PotentialRole.CONDITIONAL_PROBABILITY;
	}

	@Override
	protected void getNetworkAdvancedInformation(Element xMLProbNet, ProbNet probNet, String netName,
			Map<String, ProbNet> classes) {
		
	}

	@Override
	protected String getStringXMLPotentialType(Element xmlPotential) {
        return PotentialUtils.getPotentialName(TablePotential.class);
	}

	@Override
	protected void getLinks(Element root, ProbNet probNet) {
		// TODO
	}

	
	@Override
	protected VariableType getXMLVariableType(Element variableElement) {
		return VariableType.FINITE_STATES;
	}

	@Override
	protected Element getXMLPotentialVariables(Element xmlPotential) {
		return xmlPotential;
	}

	@Override
	protected List<Element> getXMLChildren(Element xmlRootVariables) {
		List<Element> elements = new ArrayList<>();
		elements.add(xmlRootVariables.getChild(XMLBIFTags.FOR.toString()));
		elements.addAll(xmlRootVariables.getChildren(XMLBIFTags.GIVEN.toString()));
		return elements;
	}

	@Override
	protected String getElementName(Element element) {
		return element.getText();
	}

	@Override
	protected Element getXMLRootTable(Element xmlPotential) {
		return xmlPotential.getChild(XMLBIFTags.TABLE.toString());
	}

	/* (non-Javadoc)
	 * @see org.openmarkov.io.probmodel.PGMXReader_0_2#getXMLNodeType(org.jdom2.Element)
	 * In this moment we only consider chance variables (TYPE="nature"). However, the format specification also includes
	 * decision and utility nodes.
	 */
	@Override
	protected NodeType getXMLNodeType(Element variableElement) {
		return NodeType.CHANCE;
	}

	@Override
	protected String getVariableName(Element variableElement) {
		return variableElement.getChild(XMLBIFTags.NAME.toString()).getText();
	}

	@Override
	protected Element getXMLRootStates(Element variableElement) {
		return variableElement;
	}

	@Override
	protected List<Element> getStatesElements(Element rootStates) {
		return rootStates.getChildren(XMLBIFTags.OUTCOME.toString());
	}

	@Override
	protected String getStateName(Element stateElement) {
		return stateElement.getText();
	}

 }