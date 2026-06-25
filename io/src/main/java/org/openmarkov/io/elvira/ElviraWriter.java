/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIModelType;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;

/**
 * This class writes a {@code ProbNet} in elvira format.
 *
 * @author Manuel Arias
 */
@FormatType(name = "ElviraWriter", extensions = "elv", description = "Elvira")
public class ElviraWriter implements ProbNetWriter {
    
    // Attributes
    
    // @Override
    
    /**
     * This method is used to translate openmarkov potentials to elvira potentials.
     * This method modify {@code elviraPotential} table.
     *
     * @param openMarkovPotential A {@code TablePotential}
     *
     * @return A {@code TablePotential} with the same variables but in
     * Elvira order: Conditioned variable the last one and the first
     * configuration equals to (yes, yes...yes), increasing first the
     * right-most variable: Conf. 0 = (yes, yes...yes) -&gt; Conf. 1 =
     * (yes, yes...no), etc.
     */
    private static TablePotential openMarkov2ElviraPotential(TablePotential openMarkovPotential) {
        List<Variable> potentialVariables = openMarkovPotential.getVariables();
        int numVariables = potentialVariables.size();
        List<Variable> elviraVariables = new ArrayList<Variable>(numVariables);
        for (int i = 0; i < numVariables; i++) {
            elviraVariables.add(potentialVariables.get(numVariables - i - 1));
        }
        
        TablePotential elviraPotential = (TablePotential) openMarkovPotential.reorder(elviraVariables);
        
        // Invert potential values
        double[] table = elviraPotential.getValues();
        double aux;
        int sizePotential = table.length, halfPotential = sizePotential / 2;
        for (int i = 0; i < halfPotential; i++) {
            aux = table[i];
            table[i] = table[sizePotential - i - 1];
            table[sizePotential - i - 1] = aux;
        }
        
        return elviraPotential;
    }
    
    /**
     * Ignores evidence, as evidence is stores in another file in Elvira
     */
    @Override
    public void write(String netName, ProbNet probNet, List<EvidenceCase> evidence) throws WriterException.CannotCreateFile, WriterException.UnknownNetworkType, WriterException.ICIModelNotSupportedByElvira {
        if (probNet.getAdditionalProperties().get("hasElviraProperties") == null) {
            generateElviraProperties(probNet);
        }
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(netName), Charset.forName("windows-1252"))))) {
            ElviraUtil.swapNameAndTitle(probNet);
            writeElviraNetwork(out, probNet);
        } catch (FileNotFoundException | NonProjectablePotentialException e) {
            throw new WriterException.CannotCreateFile(netName);
        }
        ElviraUtil.swapNameAndTitle(probNet); // Restore previous version
    }
    
    /**
     * Writes the complete Elvira network (preamble, nodes, links, relations) to the output.
     *
     * @param out     the writer to output to
     * @param probNet the probabilistic network to serialize
     * @throws WriterException if the network type is unknown or an ICI model is unsupported
     */
    private static void writeElviraNetwork(PrintWriter out, ProbNet probNet) throws WriterException.UnknownNetworkType, WriterException.ICIModelNotSupportedByElvira, NonProjectablePotentialException {
        writeElviraPreamble(out, probNet);
        writeElviraNodes(out, probNet);
        writeElviraLinks(out, probNet);
        writeElviraRelations(out, probNet);
    }
    
    /**
     * Writes the Elvira file header: network type, name, and general properties.
     *
     * @param out     the writer to output to
     * @param probNet the probabilistic network
     * @throws WriterException if the network type is not recognized
     */
    private static void writeElviraPreamble(PrintWriter out, ProbNet probNet) throws WriterException.UnknownNetworkType {
        // preamble comment
        out.println("//	   Network");
        out.println("//	   Elvira format");
        out.println();
        
        //Object object = infoNet.get("InfluenceDiagram");
        NetworkType networkType = probNet.getNetworkType();
        
        if (networkType instanceof InfluenceDiagramType) {
            out.print("idiagram ");
        } else if (networkType instanceof BayesianNetworkType) {
            out.print("bnet ");
        } else {
            throw new WriterException.UnknownNetworkType(networkType, List.of(InfluenceDiagramType.class, BayesianNetworkType.class));
        }
        
        out.print('"');
        
        if (probNet.getName() != null) {
            out.println(probNet.getName() + '"' + " {");
        } else {
            out.println("NoNameNet" + '"' + " {");
        }
        out.println();
        
        // additionalProperties bnet comment
        out.println("//		 Network Properties");
        out.println();
        
        //kindofgraph = "...";
        Object objKindOfGraph = probNet.getAdditionalProperties().get("KindOfGraph");
        if (objKindOfGraph != null) {
            String kindOfGraph = objKindOfGraph.toString();
            out.println("kindofgraph = " + '"' + kindOfGraph + '"' + ';');
        }
        
        // comment = "...";
        String comment = probNet.getComment();
        if (comment != null) {
            out.print("comment = ");
            out.print('"');
            out.print(comment);
            out.print('"');
            out.println(";");
        }
        
        // author = "...";
		/*String author = (String) infoNet.get("AuthorNet");
		if (author != null) {
			out.print("author = ");
			out.print('"');
			out.print(author);
			out.print('"');
			out.println(";");
		}

		// whochanged = "...";
		String whochanged = (String) infoNet.get("WhoChanged");
		if (whochanged != null) {
			out.print("whochanged = ");
			out.print('"');
			out.print(whochanged);
			out.print('"');
			out.println(";");
		}

		// whenchanged = "...";
		String whenchanged = (String) infoNet.get("WhenChanged");
		if (whenchanged != null) {
			out.print("whenchanged = ");
			out.print('"');
			out.print(whenchanged);
			out.print('"');
			out.println(";");
		}

		// visualprecision = "...";
		Object objVisualPrecision = infoNet.get("VisualPrecision");
		if (objVisualPrecision != null) {
			String visualPrecision = objVisualPrecision.toString();
			out.println("visualprecision = " + '"' + visualPrecision + '"'
					+ ';');
		}

		// version = ...;
		Object objVersion = infoNet.get("Version");
		if (objVersion != null) {
			String version = objVersion.toString();
			out.println("version = " + version + ';');
		}*/
        
        // node default states
        //Object objDefaultStates = infoNet.get("DefaultNodeStates");
        Object objDefaultStates = probNet.getDefaultStates();
        if (objDefaultStates != null) {
            State[] defaultStates = (State[]) objDefaultStates;
            out.print("default node states = (");
            for (int i = defaultStates.length - 1; i >= 1; i--) {
                out.print('"' + defaultStates[i].getName() + '"' + " , ");
            }
            out.println('"' + defaultStates[0].getName() + '"' + ");");
        }
        out.println();
    }
    
    /**
     * Writes all variable (node) definitions in Elvira format.
     *
     * @param out     the writer to output to
     * @param probNet the probabilistic network whose nodes are written
     */
    private static void writeElviraNodes(PrintWriter out, ProbNet probNet) {
        // write coment
        out.println("// Variables");
        out.println();
        
        // write nodes
        List<Node> nodes = probNet.getNodes();
        for (Node node : nodes) {
            if (node.getVariable().getName().contains(" ")) {
                out.print("node \"" + node.getVariable().getName() + "\"(");
            } else {
                out.print("node " + node.getVariable().getName() + "(");
            }
            
            VariableType variableKind = node.getVariable().
                                            getVariableType();
            
            switch (variableKind) {
                case FINITE_STATES: {
                    out.print("finite-states");
                    break;
                }
                case NUMERIC: {
                    out.print("continuous");
                    break;
                }
                case DISCRETIZED: {
                    out.print("hybrid");
                    break;
                }
            }
            
            out.println(") {");
            
            // write comment
            //hay dos tipos de comentarios para un nodo (de definición y de
            //tablas de probabilidad) en OpenMarkov
            String comment = node.getComment();
            
            if (comment != null) {
                out.print("comment = ");
                out.print('"');
                out.print(comment);
                out.print('"');
                out.println(';');
            }
            
            // write kind of node
            NodeType nodeType = node.getNodeType();
            String nodeKindName = nodeType.name();
            out.println("kind-of-node = " + nodeKindName.toLowerCase() + ";");
            
            // write kind of variable
            variableKind = node.getVariable().getVariableType();
            out.print("type-of-variable = ");
            switch (variableKind) {
                case FINITE_STATES: {
                    out.print("finite-states");
                    break;
                }
                case NUMERIC: {
                    out.print("continuous");
                    break;
                }
                case DISCRETIZED: {
                    out.print("hybrid");
                    break;
                }
            }
            out.println(';');
            
            // write posX
            int coordinateX = (int) node.getCoordinateX();
            out.println("pos_x =" + Integer.toString(coordinateX) + ";");
            
            // write posY
            int coordinateY = (int) node.getCoordinateY();
            out.println("pos_y =" + Integer.toString(coordinateY) + ";");
            
            // write node relevance
            double relevance = node.getRelevance();
            if (relevance > Double.MIN_VALUE) {
                out.println("relevance = " + Double.toString(relevance) + ";");
            }
            
            // write purpose node
            String purpose = node.getPurpose();
            if (purpose != null) {
                out.print("purpose = ");
                out.print('"');
                out.print(purpose);
                out.print('"');
                out.println(';');
            }
            
            // write number of states
            //TODO revisar el uso de "UseDefaultStates"
            if (variableKind != VariableType.NUMERIC) {
                int numStates = node.getVariable().getNumStates();
                boolean defaultStates = false;
                if ((node.getAdditionalProperties().get("UseDefaultStates") != null) && Boolean
                        .parseBoolean(node.getAdditionalProperties().get("UseDefaultStates"))) {
                    defaultStates = true;
                    out.print("//");
                }
                out.println("num-states = " + numStates + ";");
                if (!defaultStates) { // print states
                    State[] reverseOrderStates = node.getVariable().getStates();
                    
                    State[] states = new State[numStates];
                    for (int i = 0; i < numStates; i++) {
                        states[i] = reverseOrderStates[numStates - i - 1];
                    }
                    
                    out.print("states = (");
                    int numStates_1 = states.length - 1;
                    for (int i = 0; i < numStates_1; i++) {
                        if (isInteger(states[i].getName())) {
                            out.print(states[i].getName() + " ");
                        } else {
                            out.print('"' + states[i].getName() + '"' + " ");
                        }
                    }
                    if (isInteger(states[numStates_1].getName())) {
                        out.println(states[numStates_1].getName() + ");");
                    } else {
                        out.println('"' + states[numStates_1].getName() + '"' + ");");
                    }
                }
            } else {
                String min = node.getAdditionalProperties().get("Min");
                if (min != null) {
                    out.println("min = " + min + ";");
                }
                String max = node.getAdditionalProperties().get("Max");
                if (max != null) {
                    out.println("max = " + max + ";");
                }
                String precision = node.getAdditionalProperties().get("Precision");
                if (precision != null) {
                    out.println("precision = " + precision + ";");
                }
            }
            
            // end of node
            out.println('}');
            out.println();
        }
        
    }
    
    /**
     * Writes all directed links between nodes in Elvira format.
     *
     * @param out     the writer to output to
     * @param probNet the probabilistic network whose links are written
     */
    private static void writeElviraLinks(PrintWriter out, ProbNet probNet) {
        // links comment
        out.println("//		 Links of the associated graph:");
        out.println();
        
        // links
        List<Node> nodes = probNet.getNodes();
        for (Node parentNode : nodes) {
            List<Node> children = parentNode.getChildren();
            for (Node child : children) {
                out.print("link ");
                if (parentNode.getVariable().getName().contains(" ")) {
                    out.print("\"" + parentNode.getVariable().getName() + "\" ");
                } else
                    out.print(parentNode.getVariable().getName() + " ");
                if (child.getVariable().getName().contains(" ")) {
                    out.println("\"" + child.getVariable().getName() + "\";");
                } else
                    out.println(child.getVariable().getName() + ";");
                out.println();
            }
        }
    }
    
    /**
     * Writes all potential (relation) definitions in Elvira format.
     *
     * @param out     the writer to output to
     * @param probNet the probabilistic network whose potentials are written
     * @throws WriterException if an ICI model type is not supported by Elvira
     */
    private static void writeElviraRelations(PrintWriter out, ProbNet probNet) throws WriterException.ICIModelNotSupportedByElvira, NonProjectablePotentialException {
        // relations comment
        out.println("//		Network Relationships:");
        out.println();
        
        // relations
        List<Potential> potentials = probNet.getPotentials();
        for (Potential potential : potentials) {
            writeElviraTablePotential(out, potential);
        }
        out.println('}');
        out.println();
    }
    
    /**
     * Writes a single potential (table or ICI) in Elvira format.
     *
     * @param out       the writer to output to
     * @param potential the potential to serialize
     * @throws WriterException if an ICI model type is not supported by Elvira
     */
    private static void writeElviraTablePotential(PrintWriter out, Potential potential) throws WriterException.ICIModelNotSupportedByElvira, NonProjectablePotentialException {
        writeCommonElviraPotentialPreamble(out, potential);
        TablePotential elviraPotential;
        if (potential.getClass() != TablePotential.class) {
            if (potential instanceof ICIPotential) {
                writeICIElviraPotentialPreamble(out, potential.getVariables());
                writeICIElviraPotentialBody(out, (ICIPotential) potential);
            } else {
                elviraPotential = potential.tableProject(null, null);
                writeElviraTable(out, openMarkov2ElviraPotential(elviraPotential));
            }
        } else {
            writeElviraTable(out, openMarkov2ElviraPotential((TablePotential) potential));
        }
    }
    
    /**
     * Writes boilerplate metadata for a canonical model sub-potential.
     *
     * @param out       the writer to output to
     * @param variables the variables of the sub-potential
     */
    private static void writeSubPotentialTrash(PrintWriter out, List<Variable> variables) {
        out.println("comment = \"new\";");
        writeICIElviraPotentialPreamble(out, variables);
    }
    
    private static void writeICIElviraPotentialPreamble(PrintWriter out, List<Variable> variables) {
        out.println("kind-of-relation = potential;");
        out.println("active=false;");
        out.print("name-of-relation = ");
        for (Variable variable : variables) {
            out.print(variable.getName());
        }
        if (variables.size() == 1) {
            out.print("Residual");
        }
        out.print(";");
        out.println("deterministic=false;");
    }
    
    private static void writeCommonElviraPotentialPreamble(PrintWriter out, Potential potential) {
        out.print("relation ");
        // The potentials in OpenMarkov are stored in the opposite in Elvira
        // The same method do the two conversions:
        // Elvira -> OpenMarkov and OpenMarkov -> Elvira
        Variable firstVariablePotential = potential.getVariables().get(0);
        if (firstVariablePotential.getDecisionCriterion() != null) {
            writeUtilityVariable(out, firstVariablePotential);
        }
        writeVariables(out, potential.getVariables());
        
        out.println('{');
    }
    
    private static void writeUtilityVariable(PrintWriter out, Variable utilityVariable) {
        if (utilityVariable != null) {
            if (utilityVariable.getName().contains(" ")) {
                out.print("\"" + utilityVariable.getName() + "\" ");
            } else
                out.print(utilityVariable.getName() + " ");
        }
    }
    
    private static void writeVariables(PrintWriter out, List<Variable> variables) {
        int numVariables = variables.size();
        for (int i = 0; i < numVariables; i++) {
            if (variables.get(i).getName().contains(" ")) {
                out.print("\"" + variables.get(i).getName() + "\" ");
            } else
                out.print(variables.get(i).getName() + " ");
        }
    }
    
    private static void writeElviraTable(PrintWriter out, TablePotential elviraPotential) {
        Map<String, Object> infoPotential = elviraPotential.properties;
        if ((infoPotential != null) && (!infoPotential.isEmpty())) {
            String comment = (String) infoPotential.get("comment");
            if ((comment != null) && (!comment.isEmpty())) {
                out.println("comment = " + '"' + comment + '"' + ";");
            }
            String kindOfRelation = (String) infoPotential.get("kindrelation");
            if (kindOfRelation != null) {
                out.println("kind-of-relation = " + kindOfRelation + ";");
            }
            String deterministic = infoPotential.get("deterministic").toString();
            if (deterministic != null) {
                out.println("deterministic=" + deterministic + ";");
            }
        }
        
        // write table
        writeElviraTable(out, null, elviraPotential.getValues());
        out.println();
    }
    
    private static void writeElviraTable(PrintWriter out, List<Variable> variables, double[] values) {
        if (variables != null) {
            TablePotential openMarkovPotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                                    values);
            TablePotential elviraPotential = openMarkov2ElviraPotential(openMarkovPotential);
            values = elviraPotential.getValues();
        }
        out.print("values = table(");
        for (int i = 0; i < values.length; i++) {
            out.print(values[i]);
            if (i < values.length - 1) {
                out.print(" ");
            }
            if (((i + 1) % 20) == 0) {
                out.println();
            }
        }
        out.println(" );");
        out.println('}');
    }
    
    private static void writeICIElviraPotentialBody(PrintWriter out, ICIPotential potential) throws WriterException.ICIModelNotSupportedByElvira {
        out.println("values = function ");
        out.print("          ");
        ICIModelType modelType = potential.getModelType();
        switch (modelType) {
            case OR:
                out.print("Or");
                break;
            case CAUSAL_MAX:
                out.print("CausalMax");
                break;
            case GENERAL_MAX:
                out.print("GeneralizedMax");
                break;
            case AND:
                out.print("And");
                break;
            case CAUSAL_MIN:
                out.print("CausalMin");
                break;
            case GENERAL_MIN:
                out.print("GeneralizedMin");
                break;
            default:
                throw new WriterException.ICIModelNotSupportedByElvira(modelType);
        }
        out.print("(");
        List<Variable> potentialVariables = potential.getVariables();
        Variable conditionedVariable = potentialVariables.get(0);
        int numVariables = potentialVariables.size();
        for (int i = 1; i < numVariables; i++) {
            Variable conditioningVariable = potentialVariables.get(i);
            out.print(conditionedVariable.getName() + conditioningVariable.getName() + ",");
        }
        out.println(conditionedVariable.getName() + "Residual);");
        out.println();
        out.println("henrionVSdiez = \"Diez\";");
        out.println("}");
        out.println();
        
        // Write sub-potentials
        for (int i = 1; i < numVariables; i++) {
            Variable conditioningVariable = potentialVariables.get(i);
            ArrayList<Variable> subPotentialVariables = new ArrayList<Variable>(2);
            out.print("relation ");
            subPotentialVariables.add(conditionedVariable);
            subPotentialVariables.add(conditioningVariable);
            writeVariables(out, subPotentialVariables);
            out.println(" {");
            writeSubPotentialTrash(out, subPotentialVariables);
            double[] noisyParameters = potential.getNoisyParameters(conditioningVariable);
            writeElviraTable(out, subPotentialVariables, noisyParameters);
            out.println();
        }
        // Write residual potential
        ArrayList<Variable> residualVariable = new ArrayList<Variable>(1);
        residualVariable.add(conditionedVariable);
        out.print("relation ");
        writeVariables(out, residualVariable);
        out.println(" {");
        writeSubPotentialTrash(out, residualVariable);
        double[] leakyParameters = potential.getLeakyParameters();
        writeElviraTable(out, residualVariable, leakyParameters);
        out.println();
    }
    
    /**
     * Generate a {@code HashMap} with the {@code probNet}
     * additionalProperties to write in a elvira format file.
     *
     * @param probNet {@code ProbNet}
     */
    private static void generateElviraProperties(ProbNet probNet) {
        //TODO: The elviraNetworkProperties HashMap is filled, but it is ignored.
        HashMap<String, Object> elviraNetworkProperties = new HashMap<>();
        elviraNetworkProperties.put("ProbNet", probNet);
        elviraNetworkProperties.put("Name", probNet.getName());
        elviraNetworkProperties.put("DefaulNodeStates", probNet.getDefaultStates());
        
        @SuppressWarnings("rawtypes") Class networkTypeClass = probNet.getNetworkType().getClass();
        if (networkTypeClass == BayesianNetworkType.class) {
            elviraNetworkProperties.put("BayesNet", probNet);
        } else if (networkTypeClass == InfluenceDiagramType.class) {
            elviraNetworkProperties.put("InfluenceDiagram", probNet);
        }
        
        // Nodes additionalProperties
        List<Node> nodes = probNet.getNodes();
        for (Node node : nodes) {
            // sets the known node additionalProperties
            ArrayList<String> statesNames = new ArrayList<String>();
            State[] states = node.getVariable().getStates();
            for (int i = 0; i < states.length; i++) {
                statesNames.add(states[i].getName());
            }
            Map<String, String> infoNode = new LinkedHashMap<>(node.getAdditionalProperties());
            ElviraUtil.putPropertyArray(infoNode, "NodeStates", statesNames);
            NodeType nodeType = node.getNodeType();
            infoNode.put("NodeType", nodeType.toString());
            if (nodeType == NodeType.UTILITY) {
                infoNode.put("TypeOfVariable", VariableType.NUMERIC.toXMLTag());
            } else {
                infoNode.put("TypeOfVariable", VariableType.FINITE_STATES.toXMLTag());
            }
            node.setAdditionalProperties(infoNode);
        }
    }
    
    /**
     * Checks whether the given string represents an integer value.
     *
     * @param string the string to test
     * @return {@code true} if the string contains an integer
     */
    private static boolean isInteger(String string) {
        try {
            int integer = Integer.parseInt(string);
            int numDigits = 0;
            do {
                integer = integer / 10;
                numDigits++;
            } while (integer > 0);
            if (numDigits != string.length()) {
                return false;
            }
        } catch (NumberFormatException n) {
            return false;
        }
        return true;
    }
    
}
