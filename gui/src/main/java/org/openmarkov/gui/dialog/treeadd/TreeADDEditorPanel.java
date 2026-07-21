/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.node.PotentialEditDialog;
import org.openmarkov.gui.exception.*;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * {@code JScrollPane} for creating and modifying {@code TreeADDPotential}s
 *
 * @author jfernandez
 * @author myebra
 */
public class TreeADDEditorPanel extends JScrollPane implements ActionListener {
    @Serial
    private static final long serialVersionUID = -6230911169585766424L;
    protected final JPopupMenu contextualMenu = new JPopupMenu();
    // menu to start painting the treeADD with the panel in blank
    protected final JMenu submenuAddStartTree = new JMenu();
    protected final JMenuItem editPotential = new LocalizedMenuItem("TreeADD.EditPotential", ActionCommands.EDIT_POTENTIAL);
    protected final JMenuItem associateStates = new LocalizedMenuItem("TreeADD.JoinBranches", ActionCommands.JOIN_BRANCHES);
    protected final JMenuItem dissociateStates = new LocalizedMenuItem("TreeADD.DissociateStates",
                                                                 ActionCommands.REMOVE_STATES);
    protected final JMenuItem removeVariables = new LocalizedMenuItem("TreeADD.RemoveVariables",
                                                                ActionCommands.REMOVE_VARIABLES);
    protected final JMenuItem removeSubtree = new LocalizedMenuItem("TreeADD.RemoveSubtree", ActionCommands.REMOVE_SUBTREE);
    protected final JMenuItem addVariables = new LocalizedMenuItem("TreeADD.AddVariables", ActionCommands.ADD_VARIABLES);
    protected final JMenuItem splitInterval = new LocalizedMenuItem("TreeADD.SplitInterval", ActionCommands.SPLIT_INTERVAL);
    protected final JMenuItem changeInterval = new LocalizedMenuItem("TreeADD.ChangeInterval",
                                                               ActionCommands.CHANGE_INTERVAL);
    protected final JMenuItem setLabel = new LocalizedMenuItem("TreeADD.SetLabel", ActionCommands.SET_LABEL);
    protected final JMenuItem removeLabel = new LocalizedMenuItem("TreeADD.RemoveLabel", ActionCommands.REMOVE_LABEL);
    protected final JMenuItem setReference = new LocalizedMenuItem("TreeADD.SetReference", ActionCommands.SET_REFERENCE);
    protected final JMenuItem removeReference = new LocalizedMenuItem("TreeADD.RemoveReference",
                                                                ActionCommands.REMOVE_REFERENCE);
    protected final TreeADDPotential rootTreeADDPotential;
    protected JTree jTree;
    protected boolean readOnlyMode = true;
    // Variables of the treeADDPotential root of the tree
    protected List<Variable> treeVariables;
    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    // when clicking a branch you can set a potential or add a subtree to that
    // branch
    protected final JMenu addSubtree = new JMenu(stringDatabase.getString("TreeADD.AddSubtree"));
    protected final JMenu changeRootVariable = new JMenu(stringDatabase.getString("TreeADD.ChangeVariable"));
    // Mouse event detection
    private int xx, yy;
    private final Node node;
    
    /**
     * Shows the tree in read only mode
     *
     * @param node the node
     * @param cellRenderer the cell renderer
     * @param readOnly the read only
     */
    public TreeADDEditorPanel(TreeADDCellRenderer cellRenderer, Node node, boolean readOnly) {
        // A copy of the potential
        this.node = node;
        this.rootTreeADDPotential = new TreeADDPotential((TreeADDPotential) node.getPotentials().getFirst());
        setupUserInterface(cellRenderer);
        
        setReadOnly(readOnly);
    }
    
    public TreeADDEditorPanel(TreeADDCellRenderer cellRenderer, Node node) {
        this(cellRenderer, node, false);
    }
    
    // 08/04/2020
    public TreeADDEditorPanel(TreeADDCellRenderer cellRenderer, Node node, TreeADDPotential treeADDPotential, boolean readOnly) {
        this.rootTreeADDPotential = treeADDPotential;
        this.node = node;
        setupUserInterface(cellRenderer);
        
        setReadOnly(readOnly);
    }
    
    public TreeADDEditorPanel(TreeADDCellRenderer cellRenderer, Node node, TreeADDPotential treeADDPotential) {
        this(cellRenderer, node, treeADDPotential, false);
    }
    
    private void setupUserInterface(TreeADDCellRenderer cellRenderer) {
        TreeADDModel model = new TreeADDModel(rootTreeADDPotential);
        jTree = new JTree(model);
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeExpansionListener(new TreeADDExpansionAdapter(this));
        jTree.addTreeWillExpandListener(new TreeADDWillExpandAdapter(this));
        // Allows JTree nodes to accept CR/LF codes
        jTree.setShowsRootHandles(true);
        jTree.setRowHeight(0);
        jTree.setCellRenderer(cellRenderer);
        jTree.setUI(new TreeADDUserInterface());
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
        setViewportView(jTree);
        jTree.addMouseListener(new TreeADDMouseAdapter(this));
        contextualMenu.setInvoker(jTree);
        // menu to create the root treeADD start painting the tree
        submenuAddStartTree.setText(stringDatabase.getString("TreeADD.StartNode"));
    }
    
    public TreeADDPotential getTreePotential() {
        return rootTreeADDPotential;
    }
    
    public void treeExpanded(TreeExpansionEvent event) {
        TreePath treepath = event.getPath();
        Object tn1 = treepath.getLastPathComponent();
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        if (!model.isLeaf(tn1)) {// the object is a treeADDPotential
            // Expand path adding branches to the treeADD path
            for (int i = 0; i < model.getChildCount(tn1); i++) {
                Object child = model.getChild(tn1, i);// this must be a branch
                jTree.expandPath(treepath.pathByAddingChild(child));
            }
        }
    }
    
    // When clicking on a tree only can change top variable. If the tree does
    // not have subtrees trees are only permitted to change buttom up order
    protected void setContextualMenuTreeADD(MouseEvent e, TreeADDPotential treeADD) {
        contextualMenu.removeAll();
        changeRootVariable.removeAll();
        // change root variable
        List<TreeADDBranch> branches = treeADD.getBranches();
        boolean hasSubTrees = false;
        Variable currentRootVariable = treeADD.getRootVariable();
        Variable conditionedVariable = treeADD.getConditionedVariable();
        List<Variable> newPosibleRootVariables = new ArrayList<>();
        for (Variable variable : treeADD.getVariables()) {
            if (variable != currentRootVariable && variable != conditionedVariable) {
                newPosibleRootVariables.add(variable);
            }
        }
        if (!newPosibleRootVariables.isEmpty()) {
            for (Variable variable : treeADD.getVariables()) {
                if (variable != currentRootVariable && variable != conditionedVariable) {
                    JMenuItem posibleRootVariable = new JMenuItem(variable.getName());
                    posibleRootVariable.addActionListener(this);
                    posibleRootVariable.setActionCommand(ActionCommands.CHANGE_ROOT_VARIABLE);
                    changeRootVariable.add(posibleRootVariable);
                }
            }
            contextualMenu.add(changeRootVariable);
        }
    }
    
    /**
     * @param e the e
     * @param branch the branch
     */
    protected void setContextualMenuBranch(MouseEvent e, TreeADDBranch branch, TreePath branchPath) {
        contextualMenu.removeAll();
        addSubtree.removeAll();
        associateStates.removeAll();
        dissociateStates.removeAll();
        removeSubtree.removeAll();
        splitInterval.removeAll();
        changeInterval.removeAll();
        
        List<Variable> possibleRootVariables = possibleRootVariables(branch, branchPath);
        
        // if {variables}-{rootVariable}-{conditionedVariable} is not empty
        // so you can add also a subtree to the branch
        if (!possibleRootVariables.isEmpty() && !(branch.getPotential() instanceof TreeADDPotential)) {
            for (Variable variable : possibleRootVariables) {
                // To add possible rootVariables popups
                JMenuItem posibleRootVariable = new JMenuItem(variable.getName());
                posibleRootVariable.setActionCommand(ActionCommands.ADD_SUBTREE);
                posibleRootVariable.addActionListener(this);
                addSubtree.add(posibleRootVariable);
            }
            contextualMenu.add(addSubtree);
            contextualMenu.add(new JSeparator());
        }
        // remove subtree
        if (branch.getPotential() instanceof TreeADDPotential) {
            contextualMenu.add(removeSubtree);
            contextualMenu.add(new JSeparator());
        }
        // dissociate branches
        if (branch.getRootVariable().getVariableType() == VariableType.FINITE_STATES
                || branch.getRootVariable().getVariableType() == VariableType.DISCRETIZED) {
            // joining branches
            contextualMenu.add(associateStates);
            if (branch.getBranchStates().size() > 1) {
                contextualMenu.add(dissociateStates);
            }
        } else if (branch.getRootVariable().getVariableType() == VariableType.NUMERIC) {
            // Split intervals
            contextualMenu.add(splitInterval);
            // Change interval
            double minDomainLimit = branch.getRootVariable().getPartitionedInterval().getMin();
            double maxDomainLimit = branch.getRootVariable().getPartitionedInterval().getMax();
            double min = branch.getLowerBound().getLimit();
            double max = branch.getUpperBound().getLimit();
            if (minDomainLimit != min || maxDomainLimit != max) {
                contextualMenu.add(changeInterval);
            }
        }
        if (!branch.isReference() && !branch.isLabeled()) {
            contextualMenu.add(new JSeparator());
            contextualMenu.add(setLabel);
        }
        if (branch.isLabeled()) {
            contextualMenu.add(new JSeparator());
            contextualMenu.add(removeLabel);
        }
        
        Map<String, TreeADDBranch> labeledPotentials = rootTreeADDPotential.getLabeledBranches();
        boolean suitableLabeledPotentialFound = false;
        for (TreeADDBranch labeledBranch : labeledPotentials.values()) {
            Potential labeledPotential = labeledBranch.getPotential();
            suitableLabeledPotentialFound |= new HashSet<>(branch.getParentVariables()).containsAll(labeledPotential.getVariables());
        }
        if (suitableLabeledPotentialFound) {
            contextualMenu.add(new JSeparator());
            contextualMenu.add(setReference);
        }
        if (branch.isReference()) {
            contextualMenu.add(new JSeparator());
            contextualMenu.add(removeReference);
        }
    }
    
    /**
     * @param e the e
     * @param branch the branch
     */
    protected void setContextualMenuPotential(MouseEvent e, TreeADDBranch branch, TreePath branchPath) {
        contextualMenu.removeAll();
        addVariables.removeAll();
        removeVariables.removeAll();
        
        Potential potential = branch.getPotential();
        List<Variable> addableVariables = branch.getAddableVariables();
        // Remove also those finite state variables that have a single state
        TreePath parentPath = branchPath.getParentPath(); // treeADD
        while (parentPath.getLastPathComponent() != rootTreeADDPotential) {
            TreePath grandParentPath = parentPath.getParentPath();// branch
            if (grandParentPath.getLastPathComponent() instanceof TreeADDBranch treeBranch) {
                if ((
                        treeBranch.getRootVariable().getVariableType() == VariableType.FINITE_STATES
                                || treeBranch.getRootVariable().getVariableType() == VariableType.FINITE_STATES
                ) && treeBranch.getBranchStates().size() == 1) {
                    addableVariables.remove(treeBranch.getRootVariable());
                }
            }
            parentPath = grandParentPath;
        }
        
        // Potential Edition, any case it is possible to edit branch's potential
        if (!(potential instanceof TreeADDPotential)) {
            contextualMenu.add(editPotential);
            contextualMenu.add(new JSeparator());
            // Adding Variables to potential
            if (!addableVariables.isEmpty()) {
                contextualMenu.add(addVariables);
            }
            // remove potential variables
            if (potential.getVariables().size() > 1) {
                contextualMenu.add(removeVariables);
            }
        }
    }
    
    /**
     * Finds the list of variables that can be added to the potential
     *
     * @param branch the branch
     * @param branchPath the branch path
     *
     * @return the list of variables that can be added to the potential
     */
    private List<Variable> possibleRootVariables(TreeADDBranch branch, TreePath branchPath) {
        List<Variable> possibleRootVariables = new ArrayList<>();
        
        // Offer all the variables of the potential that are not utility variables
        ProbNet probNet = node.getProbNet();
        for (Variable variable : branch.getParentVariables()) {
            //if (probNet.getNode(variable).getNodeType() != NodeType.UTILITY)
                possibleRootVariables.add(variable);
        }
        // Except the current root variable and the conditioned variable
        possibleRootVariables.remove(branch.getRootVariable());
        possibleRootVariables.remove(branch.getPotential().getConditionedVariable());
        
        // Remove also those finite state variables that have a single state
        TreePath parentPath = branchPath.getParentPath(); // treeADD
        while (parentPath.getLastPathComponent() != rootTreeADDPotential) {
            TreePath grandParentPath = parentPath.getParentPath();// branch
            if (grandParentPath.getLastPathComponent() instanceof TreeADDBranch treeBranch) {
                if ((
                        treeBranch.getRootVariable().getVariableType() == VariableType.FINITE_STATES
                                || treeBranch.getRootVariable().getVariableType() == VariableType.FINITE_STATES
                ) && treeBranch.getBranchStates().size() == 1) {
                    possibleRootVariables.remove(treeBranch.getRootVariable());
                }
            }
            parentPath = grandParentPath;
        }
        
        return possibleRootVariables;
    }
    
    /**
     *
     */
    @Override public void actionPerformed(ActionEvent ae) {
        String actionComand = ae.getActionCommand();
        TreePath path = jTree.getPathForLocation(xx, yy);
        if (path == null) {
            return;
        }
        Object node = path.getLastPathComponent();
        // Most actions target a branch, not the leaf Potential — navigate to the parent branch.
        // Skip this for CHANGE_ROOT_VARIABLE which operates on the root TreeADDPotential itself.
        if (node instanceof Potential && !ActionCommands.CHANGE_ROOT_VARIABLE.equals(actionComand)) {
            path = path.getParentPath();
            if (path == null) {
                return;
            }
            node = path.getLastPathComponent();
        }
        switch (actionComand) {
            case ActionCommands.ADD_SUBTREE ->
                addSubtree(ae, (TreeADDBranch) node, path);
            case ActionCommands.EDIT_POTENTIAL -> {
                editPotential(ae, (TreeADDBranch) node, path);
            }
            case ActionCommands.CHANGE_ROOT_VARIABLE ->
                    changeRootVariable(ae, (TreeADDPotential) node, path);
            case ActionCommands.JOIN_BRANCHES ->
                associateStates(ae, (TreeADDBranch) node, path);
            case ActionCommands.REMOVE_SUBTREE ->
                removeSubtree(ae, (TreeADDBranch) node, path);
            case ActionCommands.ADD_VARIABLES ->
                addVariablesToPotential(ae, (TreeADDBranch) node, path);
            case ActionCommands.REMOVE_STATES -> {
                try {
                    dissociateStates(ae, (TreeADDBranch) node, path);
                } catch (RemovingAllStatesIsNotAllowedException e) {
                    throw new UnrecoverableException(e);
                }
            }
            case ActionCommands.REMOVE_VARIABLES ->
                removeVariablesFromPotential(ae, (TreeADDBranch) node, path);
            case ActionCommands.SPLIT_INTERVAL -> {
                // node must be a branch
                try {
                    splitInterval(ae, (TreeADDBranch) node, path);
                } catch (InvalidLimitInTreeADDException | TriedToSplitIntervalOutsideBoundsException e) {
                    throw new UnrecoverableException(e);
                }
            }
            case ActionCommands.CHANGE_INTERVAL -> {
                try {
                    changeInterval(ae, (TreeADDBranch) node, path);
                } catch (InvalidArgumentException | ChangeDomainOfTreeADDIsNotAllowedException e) {
                    throw new UnrecoverableException(e);
                }
            }
            case ActionCommands.SET_LABEL -> setLabel(ae, (TreeADDBranch) node, path);
            case ActionCommands.REMOVE_LABEL -> removeLabel(ae, (TreeADDBranch) node, path);
            case ActionCommands.SET_REFERENCE -> setReference(ae, (TreeADDBranch) node, path);
            case ActionCommands.REMOVE_REFERENCE -> removeReference(ae, (TreeADDBranch) node, path);
            default -> throw new UnreachableException(new UnexpectedMenuActionException(actionComand));
        }
    }
    
    private void changeInterval(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        
        TreePath parentPath = path.getParentPath();
        TreeADDPotential parentTreeADD = (TreeADDPotential) parentPath.getLastPathComponent();
        ChangeIntervalDialog dialog = new ChangeIntervalDialog(GUIUtils.getOwner(this), branch);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        boolean minBelongsToLeft = false;
        boolean maxBelongsToLeft = false;
        double minDomainLimit = parentTreeADD.getRootVariable().getPartitionedInterval().getMin();
        double maxDomainLimit = parentTreeADD.getRootVariable().getPartitionedInterval().getMax();
        boolean isLeftClosed = branch.getRootVariable().getPartitionedInterval().isLeftClosed(); // true
        // ->
        // [)
        boolean isRightClosed = branch.getRootVariable().getPartitionedInterval().isRightClosed();
        boolean minBelongsToLeftDomain = !isLeftClosed;
        boolean maxBelongsToLeftDomain = isRightClosed;
        if (dialog.requestValues() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        ChangeIntervalPanel panel = dialog.getChangeIntervalPanel();
        Double lowerBound = Double.parseDouble(panel.getMin().getText());
        Double upperBound = Double.parseDouble(panel.getMax().getText());
        JComboBox<String> minLimit = panel.minBelongsToLeft();// ( or [
        if (minLimit.getSelectedItem() == "(") {
            minBelongsToLeft = true;
        } else if (minLimit.getSelectedItem() == "[") {
            minBelongsToLeft = false;
        }
        JComboBox<String> maxLimit = panel.maxBelongsToLeft();// ) or ]
        if (maxLimit.getSelectedItem() == ")") {
            maxBelongsToLeft = false;
        } else if (maxLimit.getSelectedItem() == "]") {
            maxBelongsToLeft = true;
        }
        List<TreeADDBranch> parentBranches = parentTreeADD.getBranches();
        int branchIndex = 0;
        for (int i = 0; i < parentBranches.size(); i++) {
            if (parentBranches.get(i).equals(branch)) {
                branchIndex = i;
            }
        }
        
        
        // limit situations
        if (lowerBound < minDomainLimit) {
            throw new InvalidArgumentException(lowerBound, "lower bound", "must be bigger or equal to the lower bound of the domain (" + minDomainLimit + ")");
        }
        if (upperBound > maxDomainLimit) {
            throw new InvalidArgumentException(upperBound, "upper bound", "must be less or equal to the upper bound of the domain (" + maxDomainLimit + ")");
        }
        if (lowerBound > branch.getUpperBound().getLimit()) {
            throw new InvalidArgumentException(lowerBound, "lower bound", "must be less or equal to the upper bound of the branch's upper bound (" + branch.getUpperBound()
                                                                                                                                                           .getLimit() + ")");
        }
        if (upperBound < branch.getLowerBound().getLimit()) {
            throw new InvalidArgumentException(lowerBound, "upper bound", "must be bigger or equal to the upper bound of the branch's lower bound (" + branch.getLowerBound()
                                                                                                                                                             .getLimit() + ")");
        }
        boolean triesToChangeDomain =
                upperBound.floatValue() > maxDomainLimit
                        || lowerBound.floatValue() < minDomainLimit
                        || (upperBound == maxDomainLimit && !maxBelongsToLeftDomain && maxBelongsToLeft)
                        || (lowerBound == minDomainLimit && minBelongsToLeftDomain && !minBelongsToLeft);
        if (triesToChangeDomain) {
            throw new ChangeDomainOfTreeADDIsNotAllowedException();
        }
        
        // max limit
        if (!(
                upperBound == branch.getUpperBound().getLimit() && maxBelongsToLeft == branch.getUpperBound()
                                                                                             .belongsToLeft()
        )) {
            List<TreeADDBranch> followingBranches = new ArrayList<TreeADDBranch>();
            parentBranches.get(branchIndex).setUpperBound(new Threshold(upperBound, maxBelongsToLeft));
            if (branchIndex != parentBranches.size()) {
                // branch selected to change interval is not the last
                // one
                for (int i = branchIndex + 1; i < parentBranches.size(); i++) {
                    followingBranches.add(parentBranches.get(i));
                }
                for (int i = 0; i < followingBranches.size(); i++) {
                    if (upperBound.equals(followingBranches.get(i).getUpperBound().getLimit())
                            && maxBelongsToLeft == followingBranches.get(i).getUpperBound().belongsToLeft()) {
                        for (int j = branchIndex + 1; j <= i + branchIndex + 1; j++) {
                            parentBranches.remove(branchIndex + 1);
                        }
                        break;
                    } else if (upperBound.equals(followingBranches.get(i).getUpperBound().getLimit())
                            && !followingBranches.get(i).getUpperBound().belongsToLeft() && maxBelongsToLeft) {
                        for (int j = branchIndex + 1; j <= i + branchIndex + 1; j++) {
                            parentBranches.remove(branchIndex + 1);
                        }
                        // Change the next
                        if (i + branchIndex + 1 <= parentBranches.size()) {
                            parentBranches.get(branchIndex + 1).getLowerBound().setBelongsToLeft(true);
                        }
                        break;
                    } else if (upperBound.equals(followingBranches.get(i).getUpperBound().getLimit())
                            && followingBranches.get(i).getUpperBound().belongsToLeft() && !maxBelongsToLeft) {
                        for (int j = branchIndex + 1; j <= i + branchIndex; j++) {
                            parentBranches.remove(branchIndex + 1);
                        }
                        // Change the next
                        if (i + branchIndex + 1 <= parentBranches.size()) {
                            parentBranches.get(branchIndex + 1).setLowerBound(new Threshold(upperBound, false));
                        }
                        break;
                    } else if (upperBound.floatValue() < followingBranches.get(i).getUpperBound().getLimit()) {
                        for (int j = branchIndex + 1; j <= i + branchIndex; j++) {
                            parentBranches.remove(branchIndex + 1);
                        }
                        parentBranches.get(branchIndex + 1)
                                      .setLowerBound(new Threshold(upperBound, maxBelongsToLeft));
                        break;
                    }
                }
            }
        }
        // min limit
        if (!(
                lowerBound.floatValue() == branch.getLowerBound().getLimit() && minBelongsToLeft == branch
                        .getLowerBound().belongsToLeft()
        )) {
            ArrayList<TreeADDBranch> previousBranches = new ArrayList<TreeADDBranch>();
            parentBranches.get(branchIndex).setLowerBound(new Threshold(lowerBound, minBelongsToLeft));
            if (branchIndex != 0) {// branch selected to change
                // interval is not the first
                // one
                for (int i = branchIndex - 1; i >= 0; i--) {
                    previousBranches.add(parentBranches.get(i));
                }
            }
            ArrayList<TreeADDBranch> aux = new ArrayList<TreeADDBranch>();
            int initialParentSize = parentBranches.size();
            for (int i = parentBranches.size() - 1; i >= 0; i--) {
                aux.add(parentBranches.get(i));
            }
            int auxIndex = parentBranches.size() - 1 - branchIndex;
            for (int i = 0; i < previousBranches.size(); i++) {
                if (lowerBound.equals(previousBranches.get(i).getLowerBound().getLimit())
                        && minBelongsToLeft == previousBranches.get(i).getLowerBound().belongsToLeft()) {
                    for (int j = auxIndex + 1; j <= i + (auxIndex + 1); j++) {
                        aux.remove(auxIndex + 1);
                    }
                    break;
                } else if (lowerBound.equals(previousBranches.get(i).getLowerBound().getLimit())
                        && !previousBranches.get(i).getLowerBound().belongsToLeft() && minBelongsToLeft) {
                    for (int j = auxIndex + 1; j <= i + (auxIndex + 1); j++) {
                        aux.remove(auxIndex + 1);
                    }
                    // Change the next
                    if (i + auxIndex + 1 <= parentBranches.size()) {
                        aux.get(auxIndex + 1).getUpperBound().setBelongsToLeft(true);
                    }
                    break;
                } else if (lowerBound.equals(previousBranches.get(i).getLowerBound().getLimit())
                        && previousBranches.get(i).getLowerBound().belongsToLeft() && !minBelongsToLeft) {
                    for (int j = auxIndex + 1; j <= i + (auxIndex + 1); j++) {
                        aux.remove(auxIndex + 1);
                    }
                    // Change the next
                    if (i + auxIndex + 1 <= parentBranches.size()) {
                        aux.get(auxIndex + 1).getUpperBound().setBelongsToLeft(false);
                    }
                    break;
                } else if (lowerBound > previousBranches.get(i).getLowerBound().getLimit()) {
                    for (int j = auxIndex + 1; j <= i + auxIndex; j++) {
                        aux.remove(auxIndex + 1);
                    }
                    aux.get(auxIndex + 1).setUpperBound(new Threshold(lowerBound, minBelongsToLeft));
                    break;
                }
            }
            for (int i = aux.size() - 1; i >= 0; i--) {
                parentBranches.set(i, aux.get(aux.size() - 1 - i));
            }
            for (int i = aux.size(); i < initialParentSize; i++) {
                parentBranches.remove(aux.size());
            }
        }
        model.notifyTreeStructureChanged(parentPath);
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
    }
    
    private enum SplitSide {
        LEFT_SIDE_OF_FIRST,
        RIGHT_SIDE_OF_SECOND,
        IN_BETWEEN_FIRST_AND_SECOND,
    }
    
    /**
     * Splits interval in a branch which top variable is continuous
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void splitInterval(ActionEvent ae, TreeADDBranch branch, TreePath path) throws InvalidLimitInTreeADDException, TriedToSplitIntervalOutsideBoundsException {
        TreePath parentPath = path.getParentPath();
        TreeADDPotential parentTreeADD = (TreeADDPotential) parentPath.getLastPathComponent();
        SplitIntervalDialog dialog = new SplitIntervalDialog(GUIUtils.getOwner(this));
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        if (dialog.requestValues() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        boolean belongsToLeft = false;
        SplitIntervalPanel panel = dialog.getJPanelSplitInterval();
        if (panel.belongsToLeft().isSelected()) {
            belongsToLeft = true;
        } else if (panel.belongsToRight().isSelected()) {
            belongsToLeft = false;
        }
        float introducedLimit = Float.parseFloat(panel.getLimit().getText());
        Threshold minFirstInterval = branch.getLowerBound();
        Threshold maxSecondInterval = branch.getUpperBound();
        List<TreeADDBranch> parentBranches = parentTreeADD.getBranches();
        List<TreeADDBranch> newBranches = new ArrayList<TreeADDBranch>();
        // Top variable domain
        double minDomainLimit = parentTreeADD.getRootVariable().getPartitionedInterval().getMin();
        double maxDomainLimit = parentTreeADD.getRootVariable().getPartitionedInterval().getMax();
        boolean isLeftClosed = branch.getRootVariable().getPartitionedInterval().isLeftClosed(); // true
        // ->
        // [)
        boolean isRightClosed = branch.getRootVariable().getPartitionedInterval().isRightClosed();
        boolean minBelongsToLeftDomain = !isLeftClosed;
        boolean maxBelongsToLeftDomain = isRightClosed;
        if (minFirstInterval.getLimit() == introducedLimit && minFirstInterval.belongsToLeft() == belongsToLeft) {
            throw new InvalidLimitInTreeADDException(minFirstInterval, introducedLimit);
        }
        if (maxSecondInterval.getLimit() == introducedLimit && maxSecondInterval.belongsToLeft() == belongsToLeft) {
            throw new InvalidLimitInTreeADDException(maxSecondInterval, introducedLimit);
        }
        SplitSide splitSide;
        if (minFirstInterval.getLimit() == introducedLimit && !minFirstInterval.belongsToLeft() && belongsToLeft) {
            splitSide = SplitSide.LEFT_SIDE_OF_FIRST;
        } else if (maxSecondInterval.getLimit() == introducedLimit && maxSecondInterval.belongsToLeft() && !belongsToLeft) {
            splitSide = SplitSide.RIGHT_SIDE_OF_SECOND;
        } else if (minFirstInterval.isBelow(introducedLimit) && maxSecondInterval.isAbove(introducedLimit)) {
            splitSide = SplitSide.IN_BETWEEN_FIRST_AND_SECOND;
        } else {
            throw new TriedToSplitIntervalOutsideBoundsException();
        }
        boolean isChangingVariableDomain = (minDomainLimit == minFirstInterval.getLimit() && introducedLimit == minFirstInterval
                .getLimit() && minBelongsToLeftDomain && belongsToLeft) || (maxDomainLimit == maxSecondInterval.getLimit()
                && introducedLimit == maxSecondInterval.getLimit() && !maxBelongsToLeftDomain
                && !belongsToLeft);
        TreeADDBranch leftBranchToAdd = null;
        TreeADDBranch rightBranchToAdd = null;
        switch (splitSide) {
            case LEFT_SIDE_OF_FIRST -> {
                minFirstInterval.setBelongsToLeft(true);
                leftBranchToAdd = new TreeADDBranch(minFirstInterval,
                                                    new Threshold(minFirstInterval.getLimit(), true), branch.getRootVariable(),
                                                    branch.getPotential().copy(), branch.getParentVariables());
                rightBranchToAdd = branch;
            }
            case RIGHT_SIDE_OF_SECOND -> {
                maxSecondInterval.setBelongsToLeft(false);
                leftBranchToAdd = branch;
                rightBranchToAdd = new TreeADDBranch(new Threshold(maxSecondInterval.getLimit(), false),
                                                     new Threshold(maxSecondInterval.getLimit(), true), branch.getRootVariable(),
                                                     branch.getPotential().copy(), branch.getParentVariables());
            }
            case IN_BETWEEN_FIRST_AND_SECOND -> {
                Threshold maxFirstInterval = new Threshold(introducedLimit, belongsToLeft);
                Threshold minSecondInterval = new Threshold(introducedLimit, belongsToLeft);
                leftBranchToAdd = new TreeADDBranch(minFirstInterval, maxFirstInterval,
                                                    branch.getRootVariable(), branch.getPotential()
                                                                                    .copy(), branch.getParentVariables());
                rightBranchToAdd = new TreeADDBranch(minSecondInterval, maxSecondInterval,
                                                     branch.getRootVariable(), branch.getPotential()
                                                                                     .copy(), branch.getParentVariables());
            }
        }
        for (TreeADDBranch parentBranch : parentBranches) {
            if (branch == parentBranch) {
                newBranches.add(leftBranchToAdd);
                newBranches.add(rightBranchToAdd);
            } else {
                newBranches.add(parentBranch);
            }
        }
        parentTreeADD.setBranches(newBranches);
        model.notifyTreeStructureChanged(parentPath);
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
        if (isChangingVariableDomain) {
            JOptionPane.showMessageDialog(this.getParent(), "Be careful, you changed variable domain");
        }
    }
    
    /**
     * Removes a subtree from a branch
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void removeSubtree(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        TreePath parentPath = path.getParentPath();
        TreeADDPotential parentTreeADD = (TreeADDPotential) parentPath.getLastPathComponent();
        Potential subPotential = branch.getPotential();
        if (!(subPotential instanceof TreeADDPotential)) {
            throw new UnreachableException(new WrongClassException(TreeADDPotential.class, subPotential.getClass()));
        }
        List<Variable> potentialVariables = new ArrayList<Variable>();
        if (parentTreeADD.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
            Variable conditionedVariable = branch.getParentVariables().get(0);
            potentialVariables.add(conditionedVariable);
            //Empty else
        /*    
        } else if (parentTreeADD.isUtility()) {
            // potentialVariables.add(parentTreeADD.getUtilityVariable());
        }
        */
        }
        //
        UniformPotential newPotential = new UniformPotential(potentialVariables, parentTreeADD.getPotentialRole());
        /*
        if (parentTreeADD.getPotentialRole() == PotentialRole.UTILITY) {
            newPotential.setUtilityVariable(parentTreeADD.getUtilityVariable());
        }
        */
        //
        branch.setPotential(newPotential);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        model.notifyTreeStructureChanged(path);
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
    }
    
    private void addVariablesToPotential(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        TreeADDPotential parentTreeADD = (TreeADDPotential) path.getParentPath().getLastPathComponent();
        AddVariablesDialog dialog = new AddVariablesDialog(GUIUtils.getOwner(this), branch, parentTreeADD);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        if (dialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            AddVariablesCheckBoxPanel panel = dialog.getJPanelVariables();
            List<JCheckBox> checkBoxes = panel.getCheckBoxes();
            List<Variable> newVariables = new ArrayList<Variable>();
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    String variableName = checkBox.getText();
                    for (Variable variable : parentTreeADD.getVariables()) {
                        if (variable.getName().equals(variableName)) {
                            newVariables.add(variable);
                        }
                        break;
                    }
                }
            }
            Potential potential = branch.getPotential();
            for (Variable newVariable : newVariables) {
                potential = potential.addVariable(newVariable);
            }
            branch.setPotential(potential);
            model.notifyTreeInsert(path, potential);
            model.notifyTreeStructureChanged(path);
            jTree.expandPath(path);
        }
    }
    
    private void removeVariablesFromPotential(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        TreeADDPotential parentTreeADD = (TreeADDPotential) path.getParentPath().getLastPathComponent();
        RemoveVariablesDialog dialog = new RemoveVariablesDialog(GUIUtils.getOwner(this), branch, parentTreeADD);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        if (dialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            List<JCheckBox> checkBoxes = ((RemoveVariablesCheckBoxPanel) dialog.getJPanelVariables()).getCheckBoxes();
            List<Variable> variablesToEliminate = new ArrayList<Variable>();
            List<Variable> branchVariables = branch.getPotential().getVariables();
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    String variableName = checkBox.getText();
                    for (Variable variable : branchVariables) {
                        if (variable.getName() == variableName) {
                            variablesToEliminate.add(variable);
                        }
                    }
                }
            }
            for (Variable variable : variablesToEliminate) {
                branchVariables.remove(variable);
            }
            UniformPotential newPotential = new UniformPotential(branchVariables, parentTreeADD.getPotentialRole());
            //There is not utilityVariable any more
            /*
            if (parentTreeADD.isUtility()) {
                newPotential.setUtilityVariable(parentTreeADD.getUtilityVariable());
            }
            */
            //
            branch.setPotential(newPotential);
            model.notifyTreeInsert(path, newPotential);
            model.notifyTreeStructureChanged(path);
            jTree.expandPath(path);
        }
    }
    
    /**
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void dissociateStates(ActionEvent ae, TreeADDBranch branch, TreePath path) throws RemovingAllStatesIsNotAllowedException {
        if (!(branch instanceof TreeADDBranch)) {
            throw new UnreachableException(new WrongClassException(TreeADDBranch.class, branch == null ? null : branch.getClass()));
        }
        TreeADDPotential parentTreeADD = (TreeADDPotential) path.getParentPath().getLastPathComponent();
        RemoveStatesDialog dialog = new RemoveStatesDialog(GUIUtils.getOwner(this), branch, parentTreeADD);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        if (dialog.requestValues() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        List<JCheckBox> checkBoxes = ((RemoveStatesCheckBoxPanel) dialog.getJPanelRemoveStates()).getCheckBoxes();
        List<State> statesToEliminate = new ArrayList<State>();
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                String stateName = checkBox.getText();
                for (State state : parentTreeADD.getRootVariable().getStates()) {
                    if (state.getName().equals(stateName)) {
                        statesToEliminate.add(state);
                    }
                }
            }
        }
        // Check if all checkboxes has been selected, it is an inconsistency
        if (checkBoxes.size() == statesToEliminate.size()) {
            throw new RemovingAllStatesIsNotAllowedException(parentTreeADD);
        }
        List<TreeADDBranch> newTreeADDBranches = new ArrayList<TreeADDBranch>();
        for (TreeADDBranch treeParentBranch : parentTreeADD.getBranches()) {
            List<State> states = treeParentBranch.getBranchStates();
            if (!branch.getBranchStates().containsAll(states)) {
                newTreeADDBranches.add(treeParentBranch);
            }
        }
        // Updating branches
        List<State> branchStates = branch.getBranchStates();
        for (State state : statesToEliminate) {
            branchStates.remove(state);
        }
        branch.setStates(branchStates);
        List<Variable> variables = new ArrayList<Variable>();
        //parentTreeADD is TreeADDPotential.There is not utilityVariable any more
        //
                /*
                if (parentTreeADD.isUtility()) {
                    variables.add(parentTreeADD.getUtilityVariable());
                } else if (parentTreeADD.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
                    variables.add(parentTreeADD.getVariables().get(0));
                }
                */
        
        if ((parentTreeADD.isAdditive()) || (
                parentTreeADD.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY
        )) {
            variables.add(parentTreeADD.getVariables().get(0));
        }
        //
        TreeADDBranch newBranch = new TreeADDBranch(statesToEliminate, branch.getRootVariable(),
                                                    branch.getPotential().copy(), branch.getParentVariables());
        newTreeADDBranches.add(branch);
        newTreeADDBranches.add(newBranch);
        // Updating tree
        parentTreeADD.setBranches(newTreeADDBranches);
        model.notifyTreeStructureChanged(path.getParentPath());
        jTree.expandPath(path.getParentPath());
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
    }
    
    private void associateStates(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        TreePath parentPath = path.getParentPath();
        TreeADDPotential parentTreeADD = (TreeADDPotential) parentPath.getLastPathComponent();
        // BranchStatesCheckBoxPanel checkBoxPanel = new
        // BranchStatesCheckBoxPanel(treeADDBranch, parentTreeADD);
        AddStatesToBranchDialog dialog = new AddStatesToBranchDialog(GUIUtils.getOwner(this), branch, parentTreeADD);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        // This must be a treeADD
        if (dialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            List<JCheckBox> checkBoxes = ((AddStatesCheckBoxPanel) dialog.getJPanelBranchStates()).getCheckBoxes();
            List<State> newBranchStates = new ArrayList<State>(branch.getBranchStates());
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    String stateName = checkBox.getText();
                    for (State state : parentTreeADD.getRootVariable().getStates()) {
                        if (state.getName() == stateName) {
                            newBranchStates.add(state);
                        }
                    }
                }
            }
            // Reorder new states
            List<State> newOrderedStates = new ArrayList<State>();
            State[] correctOrderStates = parentTreeADD.getRootVariable().getStates();
            for (State state : correctOrderStates) {
                for (State newState : newBranchStates) {
                    if (state == newState) {
                        newOrderedStates.add(state);
                    }
                }
            }
            // Updating branches
            List<TreeADDBranch> newTreeADDBranches = new ArrayList<TreeADDBranch>();
            branch.setStates(newOrderedStates);
            newTreeADDBranches.add(branch);
            for (TreeADDBranch treeBranch : parentTreeADD.getBranches()) {
                List<State> states = treeBranch.getBranchStates();
                if (!newBranchStates.containsAll(states)) {
                    for (State state : newBranchStates) {
                        states.remove(state);
                    }
                    treeBranch.setStates(states);
                    newTreeADDBranches.add(treeBranch);
                }
            }
            // Updating tree
            parentTreeADD.setBranches(newTreeADDBranches);
            model.notifyTreeStructureChanged(parentPath);
            jTree.expandPath(path);
            for (int i = 0; i < jTree.getRowCount(); i++) {
                jTree.expandRow(i);
            }
        }
    }
    
    /**
     * @param ae the ae
     * @param treeADDPotential the tree add potential
     * @param path the path
     */
    private void changeRootVariable(ActionEvent ae, TreeADDPotential treeADDPotential, TreePath path) {
        List<Variable> variables = treeADDPotential.getVariables();
        JMenuItem menuRootVariable = (JMenuItem) ae.getSource();
        // to get the variable
        Variable newRootVariable = null;
        for (Variable variable : variables) {
            if (variable.getName().equals(menuRootVariable.getText())) {
                newRootVariable = variable;
            }
        }
        List<Variable> potentialVariables = new ArrayList<Variable>();
        if (treeADDPotential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
            potentialVariables.add(treeADDPotential.getVariables().get(0));
            // Empty else
        /*    
        } else if (treeADDPotential.isUtility()) {
            // potentialVariables.add(treeADDPotential.getUtilityVariable());
        }
        */
        }
        //
        UniformPotential potential = new UniformPotential(potentialVariables, treeADDPotential.getPotentialRole());
        // There is not utilityVariable any more
        /*
        if (treeADDPotential.getPotentialRole() == PotentialRole.UTILITY) {
            potential.setUtilityVariable(treeADDPotential.getUtilityVariable());
        }
        */
        //
        treeADDPotential.setRootVariable(newRootVariable);
        List<TreeADDBranch> newBranches = new ArrayList<TreeADDBranch>();
        if (newRootVariable.getVariableType() == VariableType.FINITE_STATES
                || newRootVariable.getVariableType() == VariableType.DISCRETIZED) {
            // for (State state : newTopVariable.getStates()) {
            for (int i = newRootVariable.getStates().length - 1; i >= 0; i--) {
                List<State> branchStates = new ArrayList<State>();
                // branchStates.add(state);
                branchStates.add(newRootVariable.getStates()[i]);
                newBranches.add(new TreeADDBranch(branchStates, newRootVariable, potential, variables));
            }
        } else if (newRootVariable.getVariableType() == VariableType.NUMERIC) {
            // Top variable domain
            double minDomainLimit = newRootVariable.getPartitionedInterval().getMin();
            double maxDomainLimit = newRootVariable.getPartitionedInterval().getMax();
            // true -> [)
            boolean isLeftClosed = newRootVariable.getPartitionedInterval().isLeftClosed();
            boolean isRightClosed = newRootVariable.getPartitionedInterval().isRightClosed();
            boolean minBelongsToLeftDomain = !isLeftClosed;
            boolean maxBelongsToLeftDomain = isRightClosed;
            newBranches.add(new TreeADDBranch(new Threshold(minDomainLimit, minBelongsToLeftDomain),
                                              new Threshold(maxDomainLimit, maxBelongsToLeftDomain), newRootVariable, potential, variables));
        }
        treeADDPotential.setBranches(newBranches);
        // treeADDPotential = newTree;
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        // model.fireNodesChanged(path);
        model.notifyTreeStructureChanged(path);
        jTree.expandPath(path);
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
    }
    
    // when clicking on a branch
    private void addSubtree(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        List<Variable> parentVariables = branch.getParentVariables();
        JMenuItem menuRootVariable = (JMenuItem) ae.getSource();
        // to get the variable
        Variable newRootVariable = null;
        for (Variable variable : parentVariables) {
            if (variable.getName() == menuRootVariable.getText()) {
                newRootVariable = variable;
            }
        }
        State[] branchingStates = null;
        PartitionedInterval partitionedInterval = null;
        if (isRootVariableUsedBefore(path, newRootVariable)) {
            // if the root variable has already appeared before in the tree,
            // restrict its number of states or the interval in which it is defined.
            if (newRootVariable.getVariableType() == VariableType.FINITE_STATES
                    || newRootVariable.getVariableType() == VariableType.DISCRETIZED) {
                List<State> groupedStates = null;
                TreePath parentPath = path.getParentPath(); // treeADD
                while (parentPath.getLastPathComponent() != rootTreeADDPotential) {
                    TreePath grandParentPath = parentPath.getParentPath();// branch
                    if (grandParentPath.getLastPathComponent() instanceof TreeADDBranch treeADDBranch) {
                        // if a branch has more then one state is possible to
                        // offer, as top variable, the top variable of this
                        // branch
                        TreePath greatGrandFatherPath = grandParentPath.getParentPath();
                        if (treeADDBranch.getBranchStates().size() > 1
                                && ((TreeADDPotential) greatGrandFatherPath.getLastPathComponent()).getRootVariable()
                                == newRootVariable) {
                            groupedStates = treeADDBranch.getBranchStates();
                            break;
                        }
                    }
                    parentPath = grandParentPath;
                }
                branchingStates = new State[groupedStates.size()];
                for (int i = 0; i < groupedStates.size(); i++) {
                    branchingStates[i] = groupedStates.get(i);
                }
            } else if (newRootVariable.getVariableType() == VariableType.NUMERIC) {
                TreePath parentPath = path.getParentPath(); // treeADD
                while (parentPath.getLastPathComponent() != rootTreeADDPotential) {
                    TreePath grandParentPath = parentPath.getParentPath();// branch
                    if (grandParentPath.getLastPathComponent() instanceof TreeADDBranch treeADDBranch) {
                        // if a branch has more then one state is possible to
                        // offer, as top variable, the top variable of this
                        // branch
                        // TreePath greatGrandFatherPath =
                        // grandParentPath.getParentPath();
                        if (treeADDBranch.getRootVariable() == newRootVariable) {
                            Threshold min = treeADDBranch.getLowerBound();
                            Threshold max = treeADDBranch.getUpperBound();
                            partitionedInterval = new PartitionedInterval(min.belongsToLeft(), min.getLimit(),
                                                                          max.getLimit(), max.belongsToLeft());
                            break;
                        }
                    }
                    parentPath = grandParentPath;
                }
            }
        }
        TreeADDPotential newTreeADD = null;
        List<Variable> newTreeVariables = new ArrayList<Variable>(parentVariables);
        //  Now there is not utilityVariable
       /*
        if (rootTreeADDPotential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
            if((branchingStates != null && branchingStates.length != 0) || partitionedInterval !=  null){
                newTreeADD = new TreeADDPotential(newTreeVariables,
                        newRootVariable,
                        branchingStates,
                        partitionedInterval,
                        rootTreeADDPotential.getPotentialRole());
            } else {
                newTreeADD = new TreeADDPotential(newTreeVariables,
                        newRootVariable,
                        rootTreeADDPotential.getPotentialRole());
            }
            
        } else if (rootTreeADDPotential.getPotentialRole() == PotentialRole.UTILITY) {
            if((branchingStates != null && branchingStates.length != 0) || partitionedInterval !=  null) {
                newTreeADD = new TreeADDPotential(rootTreeADDPotential.getUtilityVariable(),
                        newTreeVariables,
                        newRootVariable,
                        branchingStates,
                        partitionedInterval);
            } else {
                newTreeADD = new TreeADDPotential(rootTreeADDPotential.getUtilityVariable(),
                        newTreeVariables,
                        newRootVariable);
            }
        }
        */
        if ((rootTreeADDPotential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) || rootTreeADDPotential
                .isAdditive()) {
            if ((branchingStates != null && branchingStates.length != 0) || partitionedInterval != null) {
                newTreeADD = new TreeADDPotential(newTreeVariables, newRootVariable, branchingStates,
                                                  partitionedInterval, rootTreeADDPotential.getPotentialRole());
            } else {
                newTreeADD = new TreeADDPotential(newTreeVariables, newRootVariable,
                                                  rootTreeADDPotential.getPotentialRole());
            }
        }
        //
        // set the new tree to its owner branch
        branch.setPotential(newTreeADD);
        // update the tree recursively bottom-up
        TreePath parentPath = path.getParentPath();
        Object previousParentPath = path.getLastPathComponent();
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        while (parentPath != null) {
            previousParentPath = parentPath;
            parentPath = parentPath.getParentPath();
        }
        model.notifyTreeStructureChanged((TreePath) previousParentPath);
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
        
        // parenthPath is always null
        //jTree.expandPath((TreePath) parentPath);
        
        // The notification is already done with the notifyTreeStructureChanged method
        //model.notifyTreeInsert(path, newTreeADD);
        
        // The tree is already expanded in the for clause
        //jTree.expandPath(path);
        
        // Same as above
        //jTree.expandPath(path.pathByAddingChild(newTreeADD));
    }
    
    /**
     * Returns if root variable has appeared before
     *
     * @param path the path
     * @param rootVariable the root variable
     *
     * @return true iff the root variable has been previously used
     */
    private boolean isRootVariableUsedBefore(TreePath path, Variable rootVariable) {
        
        boolean rootVariableAlreadyUsed = false;
        TreePath parentPath = path.getParentPath(); // treeADD
        while (parentPath.getLastPathComponent() != rootTreeADDPotential && !rootVariableAlreadyUsed) {
            TreePath grandParentPath = parentPath.getParentPath();// branch
            if (grandParentPath.getLastPathComponent() instanceof TreeADDBranch treeADDBranch) {
                rootVariableAlreadyUsed = treeADDBranch.getRootVariable() == rootVariable;
            }
            parentPath = grandParentPath;
        }
        return rootVariableAlreadyUsed;
    }
    
    /**
     * Action to edit a potential
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void editPotential(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        TreePath parentPath = path.getParentPath();
        TreeADDPotential parentTreeADD = (TreeADDPotential) parentPath.getLastPathComponent();
        Potential potential = branch.getPotential();
        ProbNet probNet = node.getProbNet();
        ProbNet dummyProbNet = new ProbNet();
        for (Variable variable : potential.getVariables()) {
            dummyProbNet.addNode(variable, probNet.getNode(variable).getNodeType());
        }
        if(probNet.getNetworkType() instanceof DESNetworkType){
            dummyProbNet = new ProbNet(DESNetworkType.getUniqueInstance());
            dummyProbNet.addPotential(potential, probNet);
        }
        dummyProbNet.addPotential(potential);
        Variable conditionedVariable = parentTreeADD.getConditionedVariable();
        Node dummy = dummyProbNet.getNode(conditionedVariable);
        for (Variable variable : potential.getVariables()) {
            if (variable.equals(conditionedVariable)) {
                continue;
            }
            List<Potential> originalPotentials = probNet.getNode(variable).getPotentials();
            dummyProbNet.getNode(variable).setPotentials(originalPotentials);
            dummyProbNet.addLink(variable, conditionedVariable, true);
        }
        PotentialEditDialog dialog = new PotentialEditDialog(GUIUtils.getOwner(this), dummy, false);
        if (dialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            Potential retPotential = dummy.getPotentials().get(0);
            // There is not utilityVariable any more
            /*
            if (potential.isUtility()) {
                retPotential.setUtilityVariable(parentTreeADD.getUtilityVariable());
            }
            */
            //
            if (parentTreeADD.getPotentialRole() != retPotential.getPotentialRole()) {
                PotentialRole expected = parentTreeADD.getPotentialRole();
                PotentialRole found = retPotential
                        .getPotentialRole();
                throw new UnreachableException(new WrongRoleException(expected, found));
            }
            branch.setPotential(retPotential);
            TreeADDModel model = (TreeADDModel) jTree.getModel();
            model.notifyTreeStructureChanged(path);
            jTree.expandPath(path);
            int i = jTree.getRowForPath(path);
            jTree.expandRow(i);
        }
    }
    
    /**
     * Sets label for branch
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void setLabel(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        String label = JOptionPane.showInputDialog(null, "Enter label name: ", "Set label", 1);
        if (label != null && !label.isEmpty()) {
            branch.setLabel(label);
            TreeADDModel model = (TreeADDModel) jTree.getModel();
            model.notifyTreeStructureChanged(path);
        }
    }
    
    /**
     * Removes the label from a branch
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void removeLabel(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        branch.setLabel(null);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        model.notifyTreeStructureChanged(path);
    }
    
    /**
     * Sets a reference to another branch
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void setReference(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        SetReferenceDialog dialog = new SetReferenceDialog(GUIUtils.getOwner(this), branch, rootTreeADDPotential);
        dialog.setVisible(true);
        if (dialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
            TreeADDModel model = (TreeADDModel) jTree.getModel();
            model.notifyTreeStructureChanged(path);
        }
    }
    
    /**
     * Remove reference from a branch
     *
     * @param ae the ae
     * @param branch the branch
     * @param path the path
     */
    private void removeReference(ActionEvent ae, TreeADDBranch branch, TreePath path) {
        Potential referencedPotential = branch.getPotential();
        branch.setPotential(referencedPotential.copy());
        branch.setReference(null);
        TreeADDModel model = (TreeADDModel) jTree.getModel();
        model.notifyTreeStructureChanged(path);
    }
    
    public void setReadOnly(boolean readOnly) {
        
        if (!readOnlyMode && readOnly) {
            // menu to add a subtree to a branch
            addVariables.removeActionListener(this);
            editPotential.removeActionListener(this);
            associateStates.removeActionListener(this);
            dissociateStates.removeActionListener(this);
            removeVariables.removeActionListener(this);
            removeSubtree.removeActionListener(this);
            splitInterval.removeActionListener(this);
            changeInterval.removeActionListener(this);
            setLabel.removeActionListener(this);
            setReference.removeActionListener(this);
            removeLabel.removeActionListener(this);
            removeReference.removeActionListener(this);
        } else if (readOnlyMode && !readOnly) {
            // menu to add a subtree to a branch
            addVariables.addActionListener(this);
            editPotential.addActionListener(this);
            associateStates.addActionListener(this);
            dissociateStates.addActionListener(this);
            removeVariables.addActionListener(this);
            removeSubtree.addActionListener(this);
            splitInterval.addActionListener(this);
            changeInterval.addActionListener(this);
            setLabel.addActionListener(this);
            setReference.addActionListener(this);
            removeLabel.addActionListener(this);
            removeReference.addActionListener(this);
        }
        readOnlyMode = readOnly;
    }
    
    /**
     * TODO: Convert to Inner Class of the Viewer?
     */
    private static class TreeADDExpansionAdapter implements TreeExpansionListener {
        private final TreeADDEditorPanel treeADDEditorPanel;
        
        TreeADDExpansionAdapter(TreeADDEditorPanel treeADDEditorPanel) {
            this.treeADDEditorPanel = treeADDEditorPanel;
        }
        
        @Override public void treeExpanded(TreeExpansionEvent event) {
            treeADDEditorPanel.treeExpanded(event);
        }
        
        @Override public void treeCollapsed(TreeExpansionEvent event) {
            // Ignore
        }
    }
    
    /**
     * TODO: Convert to Inner Class of the Viewer?
     */
    private static class TreeADDWillExpandAdapter implements TreeWillExpandListener {
        private final TreeADDEditorPanel treeADDEditorPanel;
        
        TreeADDWillExpandAdapter(TreeADDEditorPanel treeADDEditorPanel) {
            this.treeADDEditorPanel = treeADDEditorPanel;
        }
        
        @Override public void treeWillExpand(TreeExpansionEvent event) {
            // Ignore
        }
        
        @Override public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            Object triedToExpand = event.getPath().getLastPathComponent();
            if (!(triedToExpand instanceof TreeADDPotential) && !(triedToExpand instanceof TreeADDBranch)) {
                // Exception used to stop and expand/collapse from happening.
                throw new ExpandVetoException(event);
            }
        }
    }
    
    /**
     * @author jfernandez
     * @author myebra
     */
    private class TreeADDMouseAdapter extends MouseAdapter {
        private final TreeADDEditorPanel treeADDEditorPanel;
        
        TreeADDMouseAdapter(TreeADDEditorPanel adaptee) {
            this.treeADDEditorPanel = adaptee;
        }
        
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                treeADDEditorPanel.xx = e.getX();
                treeADDEditorPanel.yy = e.getY();
                actionPerformed(new ActionEvent(this, 0, ActionCommands.EDIT_POTENTIAL));
            }
        }
        
        @Override public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showContextualMenu(e);
            }
        }
        
        @Override public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showContextualMenu(e);
            }
        }
        
        private void showContextualMenu(MouseEvent e) {
            treeADDEditorPanel.xx = e.getX();
            treeADDEditorPanel.yy = e.getY();
            TreePath path = treeADDEditorPanel.jTree.getPathForLocation(treeADDEditorPanel.xx, treeADDEditorPanel.yy);
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node instanceof TreeADDBranch branch) {
                    treeADDEditorPanel.setContextualMenuBranch(e, branch, path);
                } else if (node instanceof Potential) {
                    if (node instanceof TreeADDPotential potential) {
                        treeADDEditorPanel.setContextualMenuTreeADD(e, potential);
                    } else {
                        TreePath parentPath = path.getParentPath();
                        Object parent = parentPath.getLastPathComponent();
                        if (parent instanceof TreeADDBranch) {
                            treeADDEditorPanel.setContextualMenuPotential(e, (TreeADDBranch) parent, parentPath);
                        }
                    }
                }
                treeADDEditorPanel.contextualMenu.show(e.getComponent(), treeADDEditorPanel.xx, treeADDEditorPanel.yy);
            }
            
        }
    }
}