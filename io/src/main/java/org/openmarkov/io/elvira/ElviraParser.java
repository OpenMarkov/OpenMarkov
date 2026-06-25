/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIModelType;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reads a probabilistic network in Elvira format and builds a
 * {@code ProbNet}
 *
 * @author Manuel Arias
 * @version 1.1 carmenyago - adapted parser to new IO methods for OpenMarkov 0.3.x by filling loadProbNetInfo(String netName, InputStream... file)
 */
@FormatType(name = "ElviraParser", extensions = "elv", description = "Elvira") public class ElviraParser
		implements ProbNetReader {

	// Attributes
	private ProbNet probNet;

	private final ElviraScanner scanner;

	private String fileName;

	/**
	 * Store in this variable all the ICIPotentials.
	 */
	private final ArrayList<ICIPotential> iciPotentials;

	/**
	 * Store in this variable all the sub-potentials corresponding to
     * ICIPotentials. The key = {@code String} is the
     * {@code name-of-relation}.<p>
	 * When the process of reading potentials finishes the
     * {@code iciPotentials} are traversed to read all the sub-potentials
     * {@code name-of-relation}s and this variable is used to locate its
	 * sub-potentials.
	 */
	private final HashMap<String, TablePotential> subPotentials;

	private boolean continuousVariable;

	private final Criterion decisionCriterion;

	// Constructor

	/**
	 * Creates a parser using the specified scanner.
	 *
     * @param scanner the Elvira lexical scanner to use
	 */
	public ElviraParser(ElviraScanner scanner) {
		this.scanner = scanner;
		// Only for canonical models
		iciPotentials = new ArrayList<ICIPotential>();
		subPotentials = new HashMap<String, TablePotential>();
		fileName = null;
		decisionCriterion = new Criterion("Effectiveness", "Eff-Unit");
	}

	/** Creates a parser with a default scanner instance. */
	public ElviraParser() {
		this(ElviraScanner.getUniqueInstance());
	}

	/**
	 * This method is used to translate elvira potentials to openmarkov potentials.
	 *
     * @param elviraPotential A {@code TablePotential}
     * @return A {@code TablePotential} with the same variables but
	 * in OpenMarkov order: First the conditioned variable and the first
	 * configuration equals to (no, no...no), increasing first the left-most
	 * variable: Conf. 0 = (no, no...no) -&gt; Conf. 1 = (yes, no...no), etc.
	 */
	static TablePotential elvira2OpenMarkovPotential(TablePotential elviraPotential) {
		List<Variable> elviraVariables = elviraPotential.getVariables();
		List<Variable> auxVariables = elvira2OpenMarkovVariables(elviraVariables);

		// Invert potential values
		double[] table = elviraPotential.getValues();
		double aux;
		int sizePotential = table.length, halfPotential = sizePotential / 2;
		for (int i = 0; i < halfPotential; i++) {
			aux = table[i];
			table[i] = table[sizePotential - i - 1];
			table[sizePotential - i - 1] = aux;
		}

		// Creation of openmarkov potential reordering configurations
		TablePotential openMarkovPotential = (TablePotential) elviraPotential.reorder(auxVariables);
		return openMarkovPotential;
	}

	/**
	 * This method is used to translate ICI elvira potentials to ICI openmarkov
	 * potentials.
	 *
     * @param elviraPotential A {@code ICIPotential}
     * @return A {@code ICIPotential} with the same variables but
	 * in OpenMarkov order: First the conditioned variable
	 */
	static ICIPotential elvira2ICIOpenMarkovPotential(ICIPotential elviraPotential) {
		List<Variable> elviraVariables = elviraPotential.getVariables();
		List<Variable> auxVariables = new ArrayList<Variable>();
		int numVariables = elviraVariables.size();

		// Change variables order. Conditioned variable is the last one.
		for (int i = numVariables - 1; i >= 0; i--) {
			auxVariables.add(elviraVariables.get(i));
		}

		ICIModelType model = elviraPotential.getModelType();

		// Creation of ICI OpenMarkov potential
        ICIPotential openMarkovPotential;
		if ((model == ICIModelType.OR) || (model == ICIModelType.CAUSAL_MAX) || (model == ICIModelType.GENERAL_MAX)) {
			openMarkovPotential = new MaxPotential(model, auxVariables);
		} else {
			openMarkovPotential = new MinPotential(model, auxVariables);
		}
		openMarkovPotential.properties = elviraPotential.properties;
		return openMarkovPotential;
	}

	/**
	 * Reverses the order of variables from Elvira convention (conditioned variable last)
	 * to OpenMarkov convention (conditioned variable first).
	 *
	 * @param elviraVariables the variables in Elvira order
	 * @return the variables in OpenMarkov order
	 */
	static List<Variable> elvira2OpenMarkovVariables(List<Variable> elviraVariables) {
		int numVariables = elviraVariables.size();
		List<Variable> auxVariables = new ArrayList<Variable>();
		// Change variables order. Conditioned variable is the last one.
		for (int i = numVariables - 1; i >= 0; i--) {
			auxVariables.add(elviraVariables.get(i));
		}
		return auxVariables;
	}
	
	@Override public ProbNetInfo read(URL networkSource) throws IOException, ProbNetParserException {
		this.fileName = networkSource.getFile();
		scanner.initializeScanner(fileName, networkSource.openStream());
		// Load probNet
		probNet = new ProbNet();
		getConstraints();
		ElviraToken token;
		try {
			token = getGeneralInfo();
			token = getNodes(token);
			if (token.getReservedWord() == ReservedWord.LINK) {
				token = getLinks(token);
			}
			getPotentials(token);
			ElviraUtil.swapNameAndTitle(probNet);
		} catch (ProbNetParserException e) {
			e.setFilename(fileName);
			e.setLineNumber(scanner.lineno());
			throw e;
		}
		addSubPotentials(); // Only for canonical models
		return new ProbNetInfo(probNet, null, this, new ElviraWriter());
	}
	
	/**
	 * Reads the probNet type, creates the right compound constraint and
     * associate that constraint to {@code probNet}
	 *
	 * @throws ProbNetParserException if parser occurs
	 */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
	private void getConstraints() throws ProbNetParserException {
		ElviraToken token;
		try {
			token = scanner.getNextToken();
		} catch (IOException e) {
			throw new ProbNetParserException.CannotReadConstraint();
		}
		if (token.getTokenType() != TokenType.RESERVED) {
			throw new ProbNetParserException.ProbabilisticNetworkTypeMissing();
		}
		try {
			if (token.getReservedWord() == ReservedWord.BNET) {
				probNet.setNetworkType(BayesianNetworkType.getUniqueInstance());
				probNet.setName(token.getStringValue1());
			} else if (token.getReservedWord() == ReservedWord.IDIAGRAM) {
				probNet.setNetworkType(InfluenceDiagramType.getUniqueInstance());
				probNet.setName(token.getStringValue1());
			} else if (token.getReservedWord() == ReservedWord.IDIAGRAMSV) {
				probNet.setNetworkType(InfluenceDiagramType.getUniqueInstance());
				probNet.setName(token.getStringValue1());
			} else {
				throw new ProbNetParserException.ProbabilisticNetworkTypeNotRecognized(token.getReservedWord()
				                                                                            .toString());
			}
        } catch (ConstraintViolatedException e) {
            throw new UnreachableException(e);
        }
    }

	/**
	 * Gets some general information of the probNet and puts it in
     * {@code probNet.properties}
	 *
	 * @return Next token corresponding to a node (end of general information).
     * {@code ElviraToken}
	 * @throws ProbNetParserException if parser occurs
	 */
	private ElviraToken getGeneralInfo() throws IOException, ProbNetParserException {

		ElviraToken token = scanner.getNextToken();
		ReservedWord reservedWord = token.getReservedWord();
		while (token.getReservedWord() != ReservedWord.NODE) {
			if (reservedWord == ReservedWord.KIND_OF_NODE) {
				probNet.putAdditionalProperty("KindOfGraph", token.getStringValue1());
			} else if (reservedWord == ReservedWord.VISUALPRECISION) {
				probNet.putAdditionalProperty("VisualPrecision", Double.toString(token.getDoubleValue()));
			} else if (reservedWord == ReservedWord.VERSION) {
				probNet.putAdditionalProperty("Version", Double.toString(token.getDoubleValue()));
			} else if (reservedWord == ReservedWord.DEFAULT) {
				String[] reverseOrderStatesNames = token.getStringListValue();
				int numStates = reverseOrderStatesNames.length;
				State[] states = new State[numStates];
				for (int i = 0; i < numStates; i++) {
					states[i] = new State(reverseOrderStatesNames[numStates - i - 1]);
				}
				probNet.setDefaultStates(states);
			} else if (reservedWord == ReservedWord.KIND_OF_GRAPH) {
				probNet.putAdditionalProperty("KindOfGraph", token.getStringValue1());
			} else if (reservedWord == ReservedWord.TITLE) {
				probNet.putAdditionalProperty("TitleNet", token.getStringValue1());
			} else if (reservedWord == ReservedWord.WHENCHANGED) {
				probNet.putAdditionalProperty("WhenChanged", token.getStringValue1());
			} else if (reservedWord == ReservedWord.WHOCHANGED) {
				probNet.putAdditionalProperty("WhoChanged", token.getStringValue1());
			}
			token = scanner.getNextToken();
			reservedWord = token.getReservedWord();
		}
		return token;
	}

	/**
	 * Reads nodes (variables)
	 *
     * @param token {@code ElviraToken}
	 * @throws ProbNetParserException if parser occurs
	 * @throws IOException if an I/O error occurs
	 */
	private ElviraToken getNodes(ElviraToken token) throws IOException, ProbNetParserException {
		do {
			token = getNode(token);
		} while ((token.getReservedWord() != ReservedWord.LINK) && (token.getReservedWord() != ReservedWord.RELATION));
		return token;
	}

	/**
     * @param token {@code ElviraToken}
     * @return token. {@code ElviraToken}
	 * @throws ProbNetParserException if parser occurs
	 * @throws IOException if an I/O error occurs
	 */
	private ElviraToken getNode(ElviraToken token) throws IOException, ProbNetParserException {
		Node node = null;
		String variableName = token.getStringValue1();
		NodeType nodeType = NodeType.CHANCE;
		Variable variable = null;
		HashMap<String, String> infoNode = new HashMap<String, String>();
		int numStates = -1;
		do {
			token = scanner.getNextToken();
			ReservedWord reservedWord = token.getReservedWord();
			if (reservedWord == ReservedWord.TITLE) {
				token = scanner.getNextToken();
				infoNode.put("Title", token.getIdentifierString());

			} else if (reservedWord == ReservedWord.KIND_OF_NODE) {
				token = scanner.getNextToken();
				reservedWord = token.getReservedWord();
				if (reservedWord == ReservedWord.CHANCE) {
					nodeType = NodeType.CHANCE;
				} else if (reservedWord == ReservedWord.DECISION) {
					nodeType = NodeType.DECISION;
				} else {
					nodeType = NodeType.UTILITY;
				}
			} else if (reservedWord == ReservedWord.TYPE_OF_VARIABLE) {
				token = scanner.getNextToken(); // FINITE_STATES or CONTINUOUS
				if (token.isReservedWord()) {
					if (token.getReservedWord() == ReservedWord.CONTINUOUS) {
						continuousVariable = true;
						nodeType = NodeType.UTILITY;
					} else {
						continuousVariable = false;
					}
				}
			} else if (reservedWord == ReservedWord.POSX) {
				infoNode.put("CoordinateX", Integer.toString(token.getIntegerValue()));
				//probNet.getPr
			} else if (reservedWord == ReservedWord.POSY) {
				infoNode.put("CoordinateY", Integer.toString(token.getIntegerValue()));
			} else if (reservedWord == ReservedWord.PRECISION) {
				infoNode.put("Precision", Double.valueOf(token.getIntegerValue()).toString());
			} else if (reservedWord == ReservedWord.MIN) {
				infoNode.put("Min", Double.toString(token.getDoubleValue()));
			} else if (reservedWord == ReservedWord.MAX) {
				infoNode.put("Max", Double.toString(token.getDoubleValue()));
			} else if (reservedWord == ReservedWord.RELEVANCE) {
				infoNode.put("Relevance", Double.toString(token.getDoubleValue()));
			} else if (reservedWord == ReservedWord.PURPOSE) {
				infoNode.put("Purpose", token.getStringValue1());
			} else if (reservedWord == ReservedWord.NUM_STATES) {
				numStates = token.getIntegerValue();
			} else if (reservedWord == ReservedWord.STATES) {
				String[] reverseOrderStatesNames = token.getStringListValue();
				if (numStates < 0) {
					numStates = reverseOrderStatesNames.length;
				} else if (numStates != reverseOrderStatesNames.length) {
					throw new ProbNetParserException.WrongNumberOfStates(variableName, numStates, reverseOrderStatesNames.length);
				}
				State[] statesNames = new State[numStates];
				for (int i = 0; i < numStates; i++) {
					statesNames[i] = new State(reverseOrderStatesNames[numStates - i - 1]);
				}
				variable = new Variable(variableName, statesNames);
				node = probNet.addNode(variable, nodeType);
				//node.properties = infoNode;

			} else if (reservedWord == ReservedWord.COMMENT) {
				infoNode.put("Comment", token.getStringValue1());
			}
		} while ((token.getReservedWord() != ReservedWord.NODE) && (token.getReservedWord() != ReservedWord.LINK) && (
				token.getReservedWord() != ReservedWord.RELATION
		));
		if (variable == null) {
			if (continuousVariable) {
                double min = Double.parseDouble(infoNode.get("Min"));
                double max = Double.parseDouble(infoNode.get("Max"));
                double precision = Double.parseDouble(infoNode.get("Precision"));
                variable = new Variable(variableName, true, min, max, true, precision);
            } else { // default states
				State[] statesNames = probNet.getDefaultStates();
				variable = new Variable(variableName, statesNames);
            }
            node = probNet.addNode(variable, nodeType);
        }
		int coordX = (infoNode.containsKey("CoordinateX")) ? Integer.parseInt(infoNode.get("CoordinateX")) : 150;
		int coordY = (infoNode.containsKey("CoordinateY")) ? Integer.parseInt(infoNode.get("CoordinateY")) : 50;
		node.setCoordinateX(coordX);
		node.setCoordinateY(coordY);

		if (infoNode.get("Comment") != null)
            node.setComment(infoNode.get("Comment"));
		if (infoNode.get("Purpose") != null)
            node.setPurpose(infoNode.get("Purpose"));
		if (infoNode.get("Relevance") != null)
			node.setRelevance(Double.parseDouble(infoNode.get("Relevance")));
		if (infoNode.get("Min") != null)
			node.putAdditionalProperty("Min", infoNode.get("Min"));
		if (infoNode.get("Max") != null)
			node.putAdditionalProperty("Max", infoNode.get("Max"));
		if (infoNode.get("Precision") != null)
			node.putAdditionalProperty("Precision", infoNode.get("Precision"));
		if (infoNode.get("Title") != null) {
			node.putAdditionalProperty("Title", infoNode.get("Title"));
		}
		return token;
	}

	/**
	 * Reads and creates links between variables.
	 *
     * @param token {@code ElviraToken}
     * @return token. {@code ElviraToken}
	 * @throws IOException if an I/O error occurs
	 * @throws ProbNetParserException if parser occurs
	 */
	private ElviraToken getLinks(ElviraToken token) throws IOException, ProbNetParserException {
		do {
			String variable1Name = token.getStringValue1();
			String variable2Name = token.getStringValue2();
			Node node1 = probNet.getNode(variable1Name);
			Node node2 = probNet.getNode(variable2Name);
			if (node1 == null) throw new ProbNetParserException.MissingVariable(variable1Name);
			if (node2 == null) throw new ProbNetParserException.MissingVariable(variable2Name);
			probNet.addLink(node1, node2, true);
			token = scanner.getNextToken();
		} while (token.getReservedWord() == ReservedWord.LINK);
		return token;
	}

	/**
	 * Reads potentials information and create potentials.
	 *
     * @param token {@code ElviraToken}
	 * @throws ProbNetParserException if parser occurs
	 * @throws IOException if an I/O error occurs
	 */
	private void getPotentials(ElviraToken token) throws IOException, ProbNetParserException {
		do {
			// Gets relation variables
			String[] variablesListNames = token.getStringListValue();
			List<Variable> variables = new ArrayList<Variable>();
			for (String variableName : variablesListNames) {
				Variable variable = probNet.getVariable(variableName);
				if (variable == null) throw new ProbNetParserException.MissingVariable(variableName);
				variables.add(variable);
			}
			Potential potential = getPotential(variables);
			if (potential != null) { // null->sub-potential of a canonical model
				probNet.addPotential(potential);
			}
			token = scanner.getNextToken();
		} while (token.getReservedWord() != ReservedWord.RIGHTCB);
	}

	/**
	 * Reads and create one potential
	 *
     * @param variables {@code ArrayList} of {@code Variable}
     * @return potential. {@code Potential}
	 * @throws ProbNetParserException if parser occurs
	 * @throws IOException if an I/O error occurs
	 */
	private Potential getPotential(List<Variable> variables) throws IOException, ProbNetParserException {
        boolean isUtilityPotential = variables.get(0).getVariableType() == VariableType.NUMERIC;
        Potential potential = null;
		HashMap<String, Object> properties = new HashMap<String, Object>();
		ElviraToken token = scanner.getNextToken();
		ReservedWord reservedWord = token.getReservedWord();
		do {
			if (reservedWord == ReservedWord.COMMENT) {
				properties.put("comment", token.getStringValue1());
			} else if (reservedWord == ReservedWord.KIND_OF_RELATION) {
				properties.put("kindrelation", token.getStringValue1());
			} else if (reservedWord == ReservedWord.ACTIVE) {
				properties.put("active", token.getBooleanValue());
			} else if (reservedWord == ReservedWord.NAME) {
				properties.put("name", token.getStringValue1());
			} else if (reservedWord == ReservedWord.DETERMINISTIC) {
				properties.put("deterministic", token.getBooleanValue());
			} else if (reservedWord == ReservedWord.HENRIONVSDIEZ) {
				properties.put("henrionVSdiez", token.getStringValue1());
			} else if (reservedWord == ReservedWord.NAME_OF_RELATION) {
				properties.put("nameOfRelation", token.getStringValue1());
			} else if (reservedWord == ReservedWord.FUNCTION) {//Canonical model
				token = scanner.getNextToken(); // Type of canonical model
				reservedWord = token.getReservedWord();
				ICIModelType model;
				if (reservedWord == ReservedWord.OR) {
					model = ICIModelType.OR;
					potential = new MaxPotential(model, variables);
				} else if (reservedWord == ReservedWord.CAUSAL_MAX) {
					model = ICIModelType.CAUSAL_MAX;
					potential = new MaxPotential(model, variables);
				} else if (reservedWord == ReservedWord.GENERALIZED_MAX) {
					model = ICIModelType.GENERAL_MAX;
					potential = new MaxPotential(model, variables);
				} else if (reservedWord == ReservedWord.AND) {
					model = ICIModelType.AND;
					potential = new MinPotential(model, variables);
				} else if (reservedWord == ReservedWord.CAUSAL_MIN) {
					model = ICIModelType.CAUSAL_MIN;
					potential = new MinPotential(model, variables);
				} else if (reservedWord == ReservedWord.PRODUCT) {
					potential = new ProductPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
				} else if (reservedWord == ReservedWord.SUM) {
					potential = new SumPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
				} else {
					model = ICIModelType.GENERAL_MIN;
					potential = new MinPotential(model, variables);
				}
				if (reservedWord != ReservedWord.PRODUCT && reservedWord != ReservedWord.SUM) {
					potential.properties = properties;
					properties.put("Relations", token.getStringListValue());
					iciPotentials.add((ICIPotential) potential);
				}
			} else if ((reservedWord == ReservedWord.TABLE) || (reservedWord == ReservedWord.GENERALIZED_TABLE)) {
				PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
				if (isUtilityPotential) {
					Variable variable = variables.get(0);
					variable.setDecisionCriterion(decisionCriterion);
				}
				variables = elvira2OpenMarkovVariables(variables);
				TablePotential tablePotential = new TablePotential(variables, role);
				tablePotential.setValues(token.getDoublesTableValue());
				tablePotential = elvira2OpenMarkovPotential(tablePotential);

				tablePotential.properties = properties;
				// If it is part of a canonical model adds it to the last
				// ICIPotential
				String nameOfRelation = (String) tablePotential.properties.get("nameOfRelation");
				potential = tablePotential;
				if (nameOfRelation != null) {// Canonical model sub-potential
					// Add a sub-potential only if there is no other previously
					// with the same name.
					// This is impossible to happen if the writer is well done.
					if (subPotentials.get(nameOfRelation) == null) {
						subPotentials.put(nameOfRelation, tablePotential);
					}
					potential = null; // Do not add yet this potential
				}
			}
			token = scanner.getNextToken();
			reservedWord = token.getReservedWord();
		} while (token.getReservedWord() != ReservedWord.RIGHTCB);
		return potential;
	}

	/**
     * Adds the sub-potentials stored in {@code subPotentials} to
     * {@code ICIPotentials}.
	 *
	 * @throws ProbNetParserException if remains one or more sub-potential or there are
	 *                         some missing relation.
	 */
	private void addSubPotentials() throws ProbNetParserException.MissingPotential, ProbNetParserException.SomeSubpotentialsArentLinkedToAnICIPotential {
		for (ICIPotential potential : iciPotentials) {
			String[] relations = (String[]) potential.properties.get("Relations");
			for (String relation : relations) {
				TablePotential subPotential = subPotentials.get(relation);
				if (subPotential == null) {
					throw new ProbNetParserException.MissingPotential(relation);
                }
                if (subPotential.getVariables().size() > 1) {
                    potential.setNoisyParameters(subPotential.getVariable(1), //parent
                                                 subPotential.getValues());
                } else {
                    potential.setLeakyParameters(subPotential.getValues());
                }
                subPotentials.remove(relation);
                
            }
        }
        if (!subPotentials.isEmpty()) {
			throw new ProbNetParserException.SomeSubpotentialsArentLinkedToAnICIPotential(subPotentials);
		}
	}

	public String toString() {
        String parser = "";
		if (fileName != null) {
			parser = parser + "File: " + fileName;
		} else {
			parser = parser + "No file";
		}
		return parser;
	}

}
