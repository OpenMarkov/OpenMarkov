package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.graphic.FSVariableBox;
import org.openmarkov.gui.graphic.NumericVariableBox;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.graphic.VisualState;

import java.util.Map;

/**
 * Responsible for painting inference results (probability bars, expected utilities)
 * on the visual nodes after evidence propagation.
 */
class InferencePresenter {
    private final NetworkEditorPanel networkEditorPanel;
    
    InferencePresenter(NetworkEditorPanel networkEditorPanel) {
        this.networkEditorPanel = networkEditorPanel;
    }
    
    /**
     * This method fills the visualStates with the proper values to be
     * represented after the evaluation of the evidence case
     *
     * @param caseNumber              number of this evidence case.
     * @param individualProbabilities the results of the evaluation for each
     *                                variable.
     */
    void paintInferenceResults(int caseNumber, Map<Variable, ? extends TablePotential> individualProbabilities,
                               EvidenceCase evidence) {
        for (VisualNode visualNode : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
            Node node = visualNode.getNode();
            switch (node.getNodeType()) {
                case CHANCE, DECISION ->
                        this.paintInferenceResultsChanceOrDecisionNode(caseNumber, individualProbabilities, evidence, visualNode);
                case UTILITY -> this.paintInferenceResultsUtilityNode(caseNumber, individualProbabilities, visualNode);
                case SV_SUM, SV_PRODUCT -> {
                }
            }
        }
        this.networkEditorPanel.repaint();
    }
    
    /**
     * This method fills the visualStates of a utility node with the proper
     * values to be represented after the evaluation of the evidence case
     *
     * @param caseNumber              number of this evidence case.
     * @param individualProbabilities the results of the evaluation for each
     *                                variable.
     * @param visualNode              a node.
     */
    private void paintInferenceResultsUtilityNode(int caseNumber, Map<Variable, ? extends TablePotential> individualProbabilities,
                                                  VisualNode visualNode) {
        // It is a utility node
        Variable variable = visualNode.getNode().getVariable();
        NumericVariableBox innerBox = (NumericVariableBox) visualNode.getInnerBox();
        VisualState visualState = innerBox.getVisualState();
        visualState.setStateValue(caseNumber, individualProbabilities.get(variable).getValues()[0]);
        innerBox.setMinValue(this.networkEditorPanel.getEvidenceManager().getMinUtilityRangeOf(variable));
        innerBox.setMaxValue(this.networkEditorPanel.getEvidenceManager().getMaxUtilityRangeOf(variable));
    }
    
    /**
     * This method fills the visualStates of a chance or decision node with the
     * proper values to be represented after the evaluation of the evidence case
     *
     * @param caseNumber              number of this evidence case.
     * @param individualProbabilities the results of the evaluation for each variable.
     * @param evidence                evidence.
     * @param visualNode              a node.
     */
    private void paintInferenceResultsChanceOrDecisionNode(int caseNumber,
                                                           Map<Variable, ? extends TablePotential> individualProbabilities, EvidenceCase evidence, VisualNode visualNode) {
        Variable variable = visualNode.getNode().getVariable();
        TablePotential tablePotential = individualProbabilities.get(variable);
        if (variable.getVariableType() != VariableType.NUMERIC) {
            switch (tablePotential.getNumVariables()) {
                case 0 -> {
                    if ((visualNode.getInnerBox()) instanceof FSVariableBox) {
                        FSVariableBox innerBox = (FSVariableBox) visualNode.getInnerBox();
                        for (int i = 0; i < innerBox.getNumStates(); i++) {
                            VisualState visualState = innerBox.getVisualState(i);
                            visualState.setStateValue(caseNumber, (1.0 / innerBox.getNumStates()));
                        }
                    }
                    visualNode.setPostResolutionFinding(false);
                    // END OF
                    // PROVISIONAL2.............asaez...Comprobar si es innecesario
                    // este Provisional2............
                }
                case 1 -> {
                    double[] values = tablePotential.getValues();
                    if ((visualNode.getInnerBox()) instanceof FSVariableBox) {
                        FSVariableBox innerBox = (FSVariableBox) visualNode.getInnerBox();
                        for (int i = 0; i < innerBox.getNumStates(); i++) {
                            VisualState visualState = innerBox.getVisualState(i);
                            visualState.setStateValue(caseNumber, values[i]);
                        }
                    }
                    // PROVISIONAL2: Currently the propagation
                    // algorithm is returning a TablePotential
                    // with 0 variables when the node has a Uniform
                    // relation
                }
                default -> throw new UnrecoverableException(new NotSupportedOperationException(
                        "Table potentials with more than 1 variables aren't supported yet, meaning potential "
                                + variable.getName() + " cannot be treated yet"));
            }
            
        } else  // if numeric variable
        {
            double value = (evidence.contains(variable)) ? evidence.getNumericalValue(variable) : Double.NaN;
            value = (this.networkEditorPanel.getEvidenceManager().getPreResolutionEvidence().contains(variable)) ?
                    this.networkEditorPanel.getEvidenceManager().getPreResolutionEvidence().getNumericalValue(variable) :
                    value;
            NumericVariableBox innerBox = (NumericVariableBox) visualNode.getInnerBox();
            innerBox.getVisualState().setStateValue(caseNumber, value);
        }
        
    }
}
