package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.inference.annotation.InferenceManager;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.action.AddFindingEdit;
import org.openmarkov.gui.action.RemoveFindingEdit;
import org.openmarkov.gui.dialog.node.AddFindingDialog;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.exception.PreResolutionNodeInInferenceException;
import org.openmarkov.gui.graphic.*;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainPanelMenuAssistant;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.java.initialization.Lazy;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages pre-resolution and post-resolution evidence cases for inference,
 * including adding/removing findings and triggering evidence propagation.
 */
public class EvidenceManager {
    
    private final NetworkEditorPanel networkEditorPanel;
    /**
     * Pre resolution evidence
     */
    private EvidenceCase preResolutionEvidence;
    /**
     * Array of Evidence cases treated for this editor panel
     */
    private List<EvidenceCase> postResolutionEvidence;
    /**
     * This variable indicates which is the evidence case that is currently
     * being treated
     */
    private int currentCase;
    /**
     * Each position of this array indicates if the corresponding evidence case
     * is currently compiled (if true) or not (if false)
     */
    private final List<Boolean> evidenceCasesCompilationState;
    
    /**
     * Minimum value of the range of each utility node.
     */
    private final HashMap<Variable, Double> minUtilityRange;
    /**
     * Maximum value of the range of each utility node.
     */
    private final HashMap<Variable, Double> maxUtilityRange;
    /**
     * Inference algorithm used to evaluate this network
     */
    private final Lazy<InferenceAlgorithm> inferenceAlgorithm;
    
    EvidenceManager(NetworkEditorPanel networkEditorPanel) {
        this.networkEditorPanel = networkEditorPanel;
        this.preResolutionEvidence = new EvidenceCase();
        this.postResolutionEvidence = new ArrayList<>(1);
        this.currentCase = 0;
        this.postResolutionEvidence.add(this.currentCase, new EvidenceCase());
        this.evidenceCasesCompilationState = new ArrayList<>(1);
        this.evidenceCasesCompilationState.add(this.currentCase, false);
        this.minUtilityRange = new HashMap<>();
        this.maxUtilityRange = new HashMap<>();
        var inferenceManager = new InferenceManager();
        this.inferenceAlgorithm = Lazy.of(()->{
            InferenceAlgorithm inferenceAlgorithm = inferenceManager.getDefaultInferenceAlgorithm(this.networkEditorPanel.getVisualNetwork().getProbNet());
            if (inferenceAlgorithm == null) {
                throw new NotSupportedOperationException("there is no associated inference algorithm for " + this.networkEditorPanel.getVisualNetwork().getProbNet().localize());
            }
            return inferenceAlgorithm;
        });
    }
    
    
    /**
     * Returns the inference algorithm assigned to the panel.
     *
     * @return the inference algorithm assigned to the panel.
     */
    private InferenceAlgorithm getInferenceAlgorithm() {
        return this.inferenceAlgorithm.get();
    }
    
    List<EvidenceCase> getPostResolutionEvidence() {
        return this.postResolutionEvidence;
    }
    
    Double getMinUtilityRangeOf(Variable variable) {
        return this.minUtilityRange.get(variable);
    }
    
    Double getMaxUtilityRangeOf(Variable variable) {
        return this.maxUtilityRange.get(variable);
    }
    
    /**
     * This method adds a finding in a node.
     */
    public void addFinding() {
        VisualNode node = this.networkEditorPanel.getVisualNetwork().getLastSelectedNode();
        EvidenceCase currentEvidence = (this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) ?
                this.getCurrentEvidenceCase() : this.preResolutionEvidence;
        Finding finding = currentEvidence.getFinding(node.getNode().getVariable());
        new AddFindingDialog(GUIUtils.getOwner(this.networkEditorPanel), node.getNode()
                                                                             .getProbNet(), node.getNode()
                                                                                                .getVariable(), node, finding, this.networkEditorPanel).requestValues();
        this.networkEditorPanel.repaint();
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getMainPanelMenuAssistant()
                               .updateOptionsFindingsDependent(this.networkEditorPanel.getNetworkEditorPanel());
    }
    
    /**
     * This method removes findings from selected nodes.
     */
    public void removeFinding() throws PreResolutionNodeInInferenceException, DoEditException {
        this.networkEditorPanel.setPropagationActive(this.networkEditorPanel.isAutomaticPropagation());
        List<VisualNode> selectedNodes = this.networkEditorPanel.getVisualNetwork().getSelectedNodes();
        for (VisualNode visualNode : selectedNodes) {
            Variable variable = visualNode.getNode().getVariable();
            switch (this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode()) {
                case EDITION -> {
                    if (visualNode.isPreResolutionFinding() && this.preResolutionEvidence.getFinding(variable) != null) {
                        try {
                            new RemoveFindingEdit(visualNode, this.preResolutionEvidence, variable).executeEdit();
                        } catch (DoEditException e) {
                            throw new UnreachableException(e);
                        }
                    }
                }
                case INFERENCE -> {
                    if (visualNode.isPreResolutionFinding()) {
                        throw new PreResolutionNodeInInferenceException(visualNode);
                    }
                    if (visualNode.isPostResolutionFinding()
                            && this.postResolutionEvidence.get(this.currentCase)
                                                                      .getFinding(variable) != null) {
                        this.postResolutionEvidence.get(this.currentCase)
                                                               .removeFinding(variable);
                        visualNode.setPostResolutionFinding(false);
                    }
                }
            }
        }
        try {
            if ((this.networkEditorPanel.isPropagationActive()) && (this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE)) {
        /*
        23/10/2014
        Solving issue 226
        https://bitbucket.org/cisiad/org.openmarkov.issues/issue/226/remove-finding-in-inference-mode-displays
        The previously code was setting the propagation active to false when the propagation was actually success,
        and thus the propagation was not being performed
        */
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            }
        } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                 IncompatibleEvidenceException | CannotNormalizePotentialException | ConstraintViolatedException e) {
            this.networkEditorPanel.setPropagationActive(false);
            throw new UnreachableException(e);
        } finally {
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getInferenceToolBar()
                                   .setCurrentEvidenceCaseName(this.currentCase);
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getMainPanelMenuAssistant()
                                   .updateOptionsFindingsDependent(this.networkEditorPanel.getNetworkEditorPanel());
            this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
            this.networkEditorPanel.repaint();
        }
    }
    
    /**
     * This method returns the current Evidence Case.
     *
     * @return the current Evidence Case.
     */
    public EvidenceCase getCurrentEvidenceCase() {
        return this.postResolutionEvidence.get(this.currentCase);
    }
    
    /**
     * This method returns the Evidence Case.
     *
     * @param caseNumber the number of the case to be returned.
     *
     * @return the selected Evidence Case.
     */
    public EvidenceCase getEvidenceCase(int caseNumber) {
        return this.postResolutionEvidence.get(caseNumber);
    }
    
    /**
     * This method returns list of evidence cases
     *
     * @return the list of Evidence Cases.
     */
    public List<EvidenceCase> getEvidence() {
        List<EvidenceCase> evidence = this.postResolutionEvidence.stream()
                                                                             .filter(postResolutionEvidenceCase -> !postResolutionEvidenceCase.isEmpty())
                                                                             .collect(Collectors.toList());
        if (!evidence.isEmpty() || !this.preResolutionEvidence.isEmpty()) {
            evidence.addFirst(this.preResolutionEvidence);
        }
        return evidence;
    }
    
    /**
     * This method returns the number of the Evidence Case that is currently
     * selected
     *
     * @return the number of the current Evidence Case.
     */
    public int getCurrentCase() {
        return this.currentCase;
    }
    
    public EvidenceCase getPreResolutionEvidence() {
        return this.preResolutionEvidence;
    }
    
    /**
     * This method returns the number of Evidence Cases that the ArrayList is
     * currently holding .
     *
     * @return the number of Evidence Cases in the ArrayList.
     */
    public int getNumberOfCases() {
        return this.postResolutionEvidence.size();
    }
    
    /**
     * This method returns a boolean indicating if the case number passed as
     * parameter is currently compiled.
     *
     * @param caseNumber number of the evidence case.
     *
     * @return the compilation state of the case.
     */
    public boolean getEvidenceCasesCompilationState(int caseNumber) {
        return this.evidenceCasesCompilationState.get(caseNumber);
    }
    
    /**
     * This method sets the list of evidence cases
     *
     * @param preResolutionEvidence   pre-resolution evidence.
     * @param postResolutionInference a list of evidence case.
     */
    public void setEvidence(EvidenceCase preResolutionEvidence, List<EvidenceCase> postResolutionInference) {
        this.postResolutionEvidence = (postResolutionInference == null) ?
                new ArrayList<>() :
                postResolutionInference;
        this.preResolutionEvidence = Optional.ofNullable(preResolutionEvidence)
                                                         .orElseGet(EvidenceCase::new);
        if (this.postResolutionEvidence.isEmpty()) {
            this.postResolutionEvidence.add(new EvidenceCase());
        }
        this.currentCase = this.postResolutionEvidence.size() - 1;
        // Update visual info on evidence
        for (VisualNode node : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
            node.setPostResolutionFinding(false);
        }
        for (EvidenceCase evidenceCase : this.postResolutionEvidence) {
            for (Finding finding : evidenceCase.getFindings()) {
                for (VisualNode node : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
                    if (node.getNode().getVariable().equals(finding.getVariable())) {
                        node.setPostResolutionFinding(true);
                    }
                }
            }
        }
        for (VisualNode node : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
            node.setPreResolutionFinding(false);
        }
        for (Finding finding : preResolutionEvidence.getFindings()) {
            for (VisualNode node : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
                if (node.getNode().getVariable().equals(finding.getVariable())) {
                    node.setPreResolutionFinding(true);
                }
            }
        }
        // Update evidenceCasesCompilationState
        this.evidenceCasesCompilationState.clear();
        for (int i = 0; i < this.postResolutionEvidence.size(); ++i) {
            this.evidenceCasesCompilationState.add(false);
        }
    }
    
    /**
     * This method updates the value of each state for each node in the network
     * with the current individual probabilities.
     */
    public void updateIndividualProbabilitiesAndUtilities() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        // if some visualNode has a number of values different from the
        // number of evidence cases in memory, we need to recreate its
        // visual states and consider that the network has been changed.
        for (VisualNode visualNode : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
            InnerBox innerBox = visualNode.getInnerBox();
            if (innerBox instanceof FSVariableBox || innerBox instanceof NumericVariableBox) {
                VisualState visualState;
                if (innerBox instanceof FSVariableBox) {
                    visualState = ((FSVariableBox) innerBox).getVisualState(0);
                } else { // (innerBox instanceof NumericVariableBox)
                    visualState = ((NumericVariableBox) innerBox).getVisualState();
                }
                this.updateVisualStateAndEvidence(innerBox, visualState);
            }
        }
        
        try {
            if ((this.networkEditorPanel.isPropagationActive()) && (this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE)) {
                // if the network has been changed, propagation must be done in
                // each evidence case in memory. Otherwise, only propagation in
                // current case is needed.
                for (int i = 0; i < this.postResolutionEvidence.size(); i++) {
                    this.doPropagation(this.getEvidenceCase(i), i);
                }
                this.updateNodesFindingState(this.postResolutionEvidence.get(this.currentCase));
            } else if (this.evidenceCasesCompilationState.get(this.currentCase) == false) {
                // Even if propagation mode is manual, a propagation should be
                // done the first time that inference mode is selected
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            }
        } finally {
            this.networkEditorPanel.updateAllVisualStates("", this.currentCase);
            this.networkEditorPanel.repaint();
        }
        
    }
    
    /**
     * @param innerBox the inner box
     * @param visualState the visual state
     */
    private void updateVisualStateAndEvidence(InnerBox innerBox, VisualState visualState) {
        if (visualState.getNumberOfValues() != this.postResolutionEvidence.size()) {
            innerBox.updateNumCases(this.postResolutionEvidence.size());
            for (int i = 0; i < this.postResolutionEvidence.size(); i++) {
                this.evidenceCasesCompilationState.set(i, false);
            }
        }
    }
    
    /**
     * This method removes all the findings established in the current evidence
     * case.
     */
    public void removeAllFindings() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.networkEditorPanel.setPropagationActive(this.networkEditorPanel.isAutomaticPropagation());
        List<VisualNode> visualNodes = this.networkEditorPanel.getVisualNetwork().getAllNodes();
        for (VisualNode visualNode : visualNodes) {
            visualNode.setPostResolutionFinding(false);
        }
        List<Finding> findings = this.postResolutionEvidence.get(this.currentCase)
                                                                        .getFindings();
        for (Finding finding : findings) {
            this.postResolutionEvidence.get(this.currentCase)
                                                   .removeFinding(finding.getVariable());
        }
        try {
            this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
        } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                 IncompatibleEvidenceException | CannotNormalizePotentialException e) {
            this.networkEditorPanel.setPropagationActive(false);
            throw e;
        } finally {
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getInferenceToolBar()
                                   .setCurrentEvidenceCaseName(this.currentCase);
            this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getMainPanelMenuAssistant()
                                   .updateOptionsFindingsDependent(this.networkEditorPanel.getNetworkEditorPanel());
        }
    }
    
    /**
     * This method removes the findings that a node could have in all the
     * evidence cases in memory. It is invoked when a change takes place in
     * properties or probabilities of a the node
     *
     * @param node the node in which to remove the findings.
     */
    void removeNodeEvidenceInAllCases(Node node) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        try {
            for (int i = 0; i < this.postResolutionEvidence.size(); i++) {
                List<Finding> findings = this.postResolutionEvidence.get(i).getFindings();
                for (Finding finding : findings) {
                    if (node.getVariable() == (finding.getVariable())) {
                        this.postResolutionEvidence.get(i).removeFinding(finding.getVariable());
                        if (this.networkEditorPanel.isAutomaticPropagation() && (this.getInferenceAlgorithm() != null)) {
                            this.doPropagation(this.postResolutionEvidence.get(i), i);
                        }
                        if (i == this.currentCase) {
                            List<VisualNode> visualNodes = this.networkEditorPanel.getVisualNetwork().getAllNodes();
                            for (VisualNode visualNode : visualNodes) {
                                if (visualNode.getNode() == node) {
                                    visualNode.setPostResolutionFinding(false);// ...asaez....PENDIENTE........
                                }
                            }
                        }
                    }
                }
            }
        } catch (NotEvaluableNetworkException | NonProjectablePotentialException | NotEnoughMemoryException |
                 IncompatibleEvidenceException | CannotNormalizePotentialException e) {
            this.networkEditorPanel.setPropagationActive(false);
            throw e;
        } finally {
            this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getMainPanelMenuAssistant()
                                   .updateOptionsFindingsDependent(this.networkEditorPanel.getNetworkEditorPanel());
            this.networkEditorPanel.repaint();
        }
    }
    
    /**
     * This method returns true if there are any finding in the current evidence
     * case.
     *
     * @return true if the current evidence case has at least one finding.
     */
    public boolean areThereFindingsInCase() {
        List<Finding> findings = this.postResolutionEvidence.get(this.currentCase)
                                                                        .getFindings();
        return findings != null && !findings.isEmpty();
    }
    
    /**
     * This method returns the number of the Evidence Case that is currently
     * selected
     *
     * @param visualNode a node
     * @param state      the visual state in which the finding is going to be
     *                   set.
     */
    void toggleFinding(VisualNode visualNode, VisualState state) throws IncompatibleEvidenceException, DoEditException, NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, CannotNormalizePotentialException, ConstraintViolatedException {
        this.setNewFinding(visualNode, null, new Finding(visualNode.getNode()
                                                                   .getVariable(), state.getStateIndex()), true);
    }
    
    /**
     * This method returns the number of the Evidence Case that is currently
     * selected
     *
     * @param visualNode a node.
     * @param finding    a finding.
     * @param toggle     a boolean value.
     */
    public void setNewFinding(VisualNode visualNode, Finding previousFinding, Finding finding, boolean toggle) throws IncompatibleEvidenceException, DoEditException, NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, CannotNormalizePotentialException, ConstraintViolatedException {
        Variable variable = visualNode.getNode().getVariable();
        
        boolean isInferenceMode = this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE;
        EvidenceCase evidenceCase = (isInferenceMode) ? this.postResolutionEvidence.get(this.currentCase) : this.preResolutionEvidence;
        this.networkEditorPanel.setPropagationActive(this.networkEditorPanel.isAutomaticPropagation());
        boolean alreadyHasFinding = evidenceCase.contains(variable);
        Finding oldFinding = null;
        if (alreadyHasFinding) {
            // There is already a finding. Remove it
            oldFinding = evidenceCase.removeFinding(variable);
        }
        // Add finding (unless we were toggling evidence)
        if (!alreadyHasFinding || !toggle || !Objects.equals(oldFinding.getState(), finding.getState())) {
            evidenceCase.addFinding(finding);
            if (isInferenceMode) {
                visualNode.setPostResolutionFinding(true);
            } else {
                new AddFindingEdit(visualNode, evidenceCase, previousFinding, finding).executeEdit();
            }
        }
        // Flag current case as not compiled
        if (isInferenceMode) {
            this.evidenceCasesCompilationState.set(this.currentCase, false);
        } else {
            Collections.fill(this.evidenceCasesCompilationState, false);
        }
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        
        
        try {
            // If propagation is active, do propagation
            if ((this.networkEditorPanel.isPropagationActive()) && (this.evidenceCasesCompilationState.get(this.currentCase) == false) && (isInferenceMode)) {
                this.doPropagation(evidenceCase, this.currentCase);
            }
        } catch (NonProjectablePotentialException | NotEnoughMemoryException | NotEvaluableNetworkException |
                 IncompatibleEvidenceException | CannotNormalizePotentialException | ConstraintViolatedException e) {
            evidenceCase.removeFinding(variable);
            if (alreadyHasFinding) {
                try {
                    evidenceCase.addFinding(oldFinding);
                } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther ex) {
                    throw new UnreachableException(ex);
                }
            }
            visualNode.setPostResolutionFinding(alreadyHasFinding);
            throw e;
        } finally {
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getMainPanelMenuAssistant()
                                   .updateOptionsFindingsDependent(this.networkEditorPanel.getNetworkEditorPanel());
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel().getMainPanelMenuAssistant()
                                   .updateOptionsPropagationTypeDependent(this.networkEditorPanel.getNetworkEditorPanel());// ..
            this.networkEditorPanel.repaint();
        }
        
    }
    
    /**
     * Calculates minUtilityRange and maxUtilityRange fields. It is an
     * approximate implementation. The correct computation is given by a method
     * with the same name, but commented above.
     *
     * @throws NonProjectablePotentialException if the potential cannot be projected
     */
    private void calculateMinAndMaxUtilityRanges() throws NonProjectablePotentialException {
        List<Variable> utilityVariables = this.networkEditorPanel.getVisualNetwork().getProbNet().getVariables(NodeType.UTILITY);
        for (Variable utility : utilityVariables) {
            ProbNet newNet = this.networkEditorPanel.getVisualNetwork().getProbNet().copy();
            newNet = TaskUtilities.extendPreResolutionEvidence(newNet, this.preResolutionEvidence);
            Node node = newNet.getNode(utility);
            this.minUtilityRange.put(utility, UtilityFunctionComputer.approximateMinUtility(node));
            this.maxUtilityRange.put(utility, UtilityFunctionComputer.approximateMaxUtility(node));
        }
    }
    
    /**
     * This method does the propagation of the evidence in the network
     *
     * @param evidenceCase the evidence case with which the propagation must be
     *                     done.
     * @param caseNumber   number of this evidence case.
     */
    public void doPropagation(EvidenceCase evidenceCase, int caseNumber) throws NonProjectablePotentialException, NotEnoughMemoryException, NotEvaluableNetworkException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        Map<Variable, TablePotential> individualProbabilities = null;
        try {
            this.calculateMinAndMaxUtilityRanges();
            Propagation vePosteriorValues = new VEPropagation(this.networkEditorPanel.getVisualNetwork().getProbNet());
            vePosteriorValues.setVariablesOfInterest(this.networkEditorPanel.getVisualNetwork().getProbNet().getVariables());
            vePosteriorValues.setPreResolutionEvidence(this.preResolutionEvidence);
            vePosteriorValues.setPostResolutionEvidence(evidenceCase);
            individualProbabilities = vePosteriorValues.getPosteriorValues();
        } catch (OutOfMemoryError e) {
            boolean approximateInferenceWarningGiven = false;
            if (!approximateInferenceWarningGiven) {
                throw new NotEnoughMemoryException(e);
            }
        }
        this.updateNodesFindingState(evidenceCase);
        this.networkEditorPanel.getInferencePresenter().paintInferenceResults(caseNumber, individualProbabilities, evidenceCase);
        this.evidenceCasesCompilationState.set(caseNumber, true);
        this.networkEditorPanel.repaint();
    }
    
    /**
     * This method updates the "finding state" of each node
     *
     * @param evidenceCase the evidence case with which the update must be done.
     */
    public void updateNodesFindingState(EvidenceCase evidenceCase) {
        for (VisualNode visualNode : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
            visualNode.setPreResolutionFinding(false);
            visualNode.setPostResolutionFinding(false);
        }
        for (Finding finding : evidenceCase.getFindings()) {
            Variable variable = finding.getVariable();
            for (VisualNode visualNode : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
                if (variable.getName().equals(visualNode.getNode().getName())) {
                    visualNode.setPostResolutionFinding(true);
                }
            }
        }
        for (Finding finding : this.preResolutionEvidence.getFindings()) {
            Variable variable = finding.getVariable();
            for (VisualNode visualNode : this.networkEditorPanel.getVisualNetwork().getAllNodes()) {
                if (variable.getName().equals(visualNode.getNode().getName())) {
                    visualNode.setPreResolutionFinding(true);
                }
            }
        }
        this.networkEditorPanel.repaint();
    }
    
    /**
     * This method creates a new evidence case
     */
    public void createNewEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        EvidenceCase newEvidenceCase = new EvidenceCase();
        EvidenceCase currentEvidenceCase = this.getCurrentEvidenceCase();
        List<Finding> currentFindings = currentEvidenceCase.getFindings();
        for (Finding currentFinding : currentFindings) {
            newEvidenceCase.addFinding(currentFinding);
        }
        this.addNewEvidenceCase(newEvidenceCase);
    }
    
    /**
     * This method adds a new evidence case
     */
    public void addNewEvidenceCase(EvidenceCase newEvidenceCase) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.networkEditorPanel.setPropagationActive(this.networkEditorPanel.isAutomaticPropagation());
        this.postResolutionEvidence.add(newEvidenceCase);
        this.currentCase = (this.postResolutionEvidence.size() - 1);
        this.evidenceCasesCompilationState.add(this.currentCase, false);
        this.networkEditorPanel.updateAllVisualStates("new", this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        if (this.networkEditorPanel.isPropagationActive() && this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
            try {
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            } catch (NonProjectablePotentialException | NotEnoughMemoryException | NotEvaluableNetworkException |
                     IncompatibleEvidenceException | CannotNormalizePotentialException e) {
                this.networkEditorPanel.setPropagationActive(false);
                throw e;
            }
        }
    }
    
    /**
     * This method makes the first evidence case to be the current
     */
    public void goToFirstEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.currentCase = 0;
        this.networkEditorPanel.updateAllVisualStates("", this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        if ((this.networkEditorPanel.isPropagationActive()) && (this.evidenceCasesCompilationState.get(this.currentCase) == false) && (
                this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE
        )) {
            try {
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            } catch (IncompatibleEvidenceException | NonProjectablePotentialException | NotEvaluableNetworkException |
                     NotEnoughMemoryException | CannotNormalizePotentialException e) {
                this.networkEditorPanel.setPropagationActive(false);
                throw e;
            }
        } else {
            this.updateNodesFindingState(this.postResolutionEvidence.get(this.currentCase));
        }
    }
    
    /**
     * This method makes the previous evidence case to be the current
     */
    public void goToPreviousEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        if (!(this.currentCase > 0)) {
            throw new UnreachableException(new IllegalStateException("Go-to-previous button should have been disabled when at first evidence case"));
        }
        this.currentCase--;
        this.networkEditorPanel.updateAllVisualStates("", this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        if ((this.networkEditorPanel.isPropagationActive()) && (this.evidenceCasesCompilationState.get(this.currentCase) == false) && (
                this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE
        )) {
            try {
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            } catch (NonProjectablePotentialException | NotEnoughMemoryException | NotEvaluableNetworkException |
                     IncompatibleEvidenceException | CannotNormalizePotentialException e) {
                this.networkEditorPanel.setPropagationActive(false);
                throw e;
            }
        } else {
            this.updateNodesFindingState(this.postResolutionEvidence.get(this.currentCase));
        }
    }
    
    /**
     * This method makes the next evidence case to be the current
     */
    public void goToNextEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        if (!(this.currentCase < (this.postResolutionEvidence.size() - 1))) {
            throw new UnreachableException(new IllegalStateException("Go-to-next button should have been disabled when at last evidence case"));
        }
        this.currentCase++;
        this.networkEditorPanel.updateAllVisualStates("", this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        if ((this.networkEditorPanel.isPropagationActive()) && (this.evidenceCasesCompilationState.get(this.currentCase) == false) && (
                this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE
        )) {
            try {
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            } catch (NonProjectablePotentialException | NotEnoughMemoryException | NotEvaluableNetworkException |
                     IncompatibleEvidenceException | CannotNormalizePotentialException e) {
                this.networkEditorPanel.setPropagationActive(false);
                throw e;
            }
        } else {
            this.updateNodesFindingState(this.postResolutionEvidence.get(this.currentCase));
        }
    }
    
    /**
     * This method makes the last evidence case to be the current
     */
    public void goToLastEvidenceCase() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.currentCase = (this.postResolutionEvidence.size() - 1);
        this.networkEditorPanel.updateAllVisualStates("", this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        if ((this.networkEditorPanel.isPropagationActive()) && (this.evidenceCasesCompilationState.get(this.currentCase) == false) && (
                this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE
        )) {
            try {
                this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
            } catch (NonProjectablePotentialException | NotEnoughMemoryException | NotEvaluableNetworkException |
                     IncompatibleEvidenceException | CannotNormalizePotentialException e) {
                this.networkEditorPanel.setPropagationActive(false);
                throw e;
            }
        } else {
            this.updateNodesFindingState(this.postResolutionEvidence.get(this.currentCase));
        }
    }
    
    /**
     * This method clears out all the evidence cases. It returns to an 'initial
     * state' in which there is only an initial evidence case with no findings
     * (corresponding to prior probabilities)
     */
    public void clearOutAllEvidenceCases() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.networkEditorPanel.setPropagationActive(this.networkEditorPanel.isAutomaticPropagation());
        this.postResolutionEvidence.clear();
        this.evidenceCasesCompilationState.clear();
        EvidenceCase newEvidenceCase = new EvidenceCase();
        this.postResolutionEvidence.add(newEvidenceCase);
        this.currentCase = 0;
        this.evidenceCasesCompilationState.add(this.currentCase, false);
        this.networkEditorPanel.updateAllVisualStates("clear", this.currentCase);
        this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                               .getInferenceToolBar()
                               .setCurrentEvidenceCaseName(this.currentCase);
        this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
        try {
            this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
        } catch (NonProjectablePotentialException | NotEnoughMemoryException | NotEvaluableNetworkException |
                 IncompatibleEvidenceException | CannotNormalizePotentialException e) {
            this.networkEditorPanel.setPropagationActive(false);
            throw e;
        }
    }
    
    /**
     * This method does the propagation of the evidence for all the evidence
     * cases in memory.
     *
     * @param mainPanelMenuAssistant the menu assistant associated to the main
     *                               panel.
     */
    public void propagateEvidence(MainPanelMenuAssistant mainPanelMenuAssistant) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        this.networkEditorPanel.setPropagationActive(true);
        if (this.networkEditorPanel.getNetworkEditorPanel().getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
            for (int i = 0; i < this.getNumberOfCases(); i++) {
                if (this.evidenceCasesCompilationState.get(i) == false) {
                    try {
                        this.doPropagation(this.postResolutionEvidence.get(this.currentCase), this.currentCase);
                    } catch (NonProjectablePotentialException | NotEnoughMemoryException |
                             NotEvaluableNetworkException | IncompatibleEvidenceException |
                             CannotNormalizePotentialException e) {
                        this.networkEditorPanel.setPropagationActive(false);
                        throw e;
                    }
                }
            }
            this.networkEditorPanel.getVisualNetwork().setSelectedAllNodes(false);
            this.networkEditorPanel.updateAllVisualStates("", this.currentCase);
            this.networkEditorPanel.getNetworkEditorPanel().getMainPanel()
                                   .getInferenceToolBar()
                                   .setCurrentEvidenceCaseName(this.currentCase);
            this.updateNodesFindingState(this.postResolutionEvidence.get(this.currentCase));
        }
        mainPanelMenuAssistant.updateOptionsEvidenceCasesNavigation(this.networkEditorPanel.getNetworkEditorPanel());
        mainPanelMenuAssistant.updateOptionsPropagationTypeDependent(this.networkEditorPanel.getNetworkEditorPanel());
        mainPanelMenuAssistant.updateOptionsFindingsDependent(this.networkEditorPanel.getNetworkEditorPanel());
    }
    
}
