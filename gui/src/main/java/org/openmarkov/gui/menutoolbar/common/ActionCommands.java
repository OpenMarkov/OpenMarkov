/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

/**
 * This class defines the constants used to identify the actions invoked by the
 * user.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo Add help menus (previously to change to dynamic version),
 * ficheros usados recientemente y cambios de lenguaje
 * @version 1.2 jrico converted this file from a 'class' file to an 'enum' file.
 */
public enum ActionCommands {
    
    /** Action invoked when the user wants to create a new network. */
    NEW_NETWORK("NewNetwork"),
    /** Action invoked when the user wants to open a network. */
    OPEN_NETWORK("OpenNetwork"),
    /** Action invoked when the user wants to open a network from URL. */
    OPEN_NETWORK_URL("OpenNetworkURL"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_1_FILE("OpenLastRecentNetwork1"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_2_FILE("OpenLastRecentNetwork2"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_3_FILE("OpenLastRecentNetwork3"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_4_FILE("OpenLastRecentNetwork4"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_5_FILE("OpenLastRecentNetwork5"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_6_FILE("OpenLastRecentNetwork6"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_7_FILE("OpenLastRecentNetwork7"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_8_FILE("OpenLastRecentNetwork8"),
    /** Action invoked when the user wants to open a network. */
    OPEN_LAST_9_FILE("OpenLastRecentNetwork9"),
    /** Action invoked when the user wants to save a network. */
    SAVE_NETWORK("SaveNetwork"),
    /** Action invoked when the user wants to save and open newly the same network. */
    SAVE_OPEN_NETWORK("SaveOpenNetwork"),
    /** Action invoked when the user wants to save a network as another one. */
    SAVEAS_NETWORK("SaveAsNetwork"),
    /** Action invoked when the user wants to close a network. */
    CLOSE_TAB("CloseNetwork"),
    /** Action invoked when the user wants to load evidence. */
    LOAD_EVIDENCE("LoadEvidence"),
    /** Action invoked when the user wants to save evidence. */
    SAVE_EVIDENCE("SaveEvidence"),
    /** Action invoked when the user wants to edit the additionalProperties of a network. */
    NETWORK_PROPERTIES("NetworkProperties"),
    /** Action invoked when the user wants to exit the application. */
    EXIT_APPLICATION("ExitApplication"),
    /** Action invoked when the user wants to set the nodes to paint its foreground by title. */
    BYTITLE_NODES("ByTitleNodes"),
    /** Action invoked when the user wants to set the nodes to paint its foreground by name. */
    BYNAME_NODES("ByNameNodes"),
    /** Represents all the actions related to the nodes viewing. */
    NODES("Nodes"),
    /** Represents all the actions related to the variable zoomManager options. */
    ZOOM("ZoomManager"),
    /** Action invoked when the user wants to select all the objects. */
    SELECT_ALL("SelectAll"),
    /** Action invoked when the user wants to auto-arrange the nodes of
     *  the current network using stress-majorization with directional
     *  bias (parent-above-child for Bayesian DAGs). */
    AUTO_ARRANGE("AutoArrange"),
    /** Prefix used for edition mode changes */
    EDITION_MODE_PREFIX("Edit.Mode"),
    /** Action invoked when the user wants to activate the selection option. */
    OBJECT_SELECTION("Edit.Mode.Selection"),
    /** Action invoked when the user wants to activate the chance node creation option. */
    CHANCE_CREATION("Edit.Mode.Chance"),
    /** Action invoked when the user wants to activate the decision node creation option. */
    DECISION_CREATION("Edit.Mode.Decision"),
    /** Action invoked when the user wants to activate the chance node creation option. */
    EVENT_CREATION("Edit.Mode.Event"),
    /** Action invoked when the user wants to activate the utility node creation option. */
    UTILITY_CREATION("Edit.Mode.Utility"),
    /** Action invoked when the user wants to activate the link creation option. */
    LINK_CREATION("Edit.Mode.Link"),
    /**
     * Action invoked when the user wants to change the working mode by
     * pressing the button in the standard tool bar
     * (switching from Edition to Inference mode or vice versa).
     */
    CHANGE_WORKING_MODE("ChangeWorkingMode"),
    /**
     * Action invoked when the user wants to change to inference mode
     * using the option in the Edit menu
     */
    CHANGE_TO_INFERENCE_MODE("ChangeToInferenceMode"),
    /**
     * Action invoked when the user wants to change to edition mode
     * using the option in the Inference menu
     */
    CHANGE_TO_EDITION_MODE("ChangeToEditionMode"),
    /**
     * Action invoked when the user wants to change the working mode by
     * pressing the button in the standard tool bar
     * (switching from Edition to Inference mode or vice versa).
     */
    MC_SIMULATE_NETWORK("MCSimulateNetwork"),
    /** Action invoked when the user wants to change the Expansion Threshold. */
    SET_NEW_EXPANSION_THRESHOLD("SetNewExpansionThreshold"),
    /** Action invoked when the user wants to create a new evidence case. */
    CREATE_NEW_EVIDENCE_CASE("CreateNewEvidenceCase"),
    /** Action invoked when the user wants to go to the first evidence case. */
    GO_TO_FIRST_EVIDENCE_CASE("GoToFirstEvidenceCase"),
    /** Action invoked when the user wants to go to the previous evidence case. */
    GO_TO_PREVIOUS_EVIDENCE_CASE("GoToPreviousEvidenceCase"),
    /** Action invoked when the user wants to go to the next evidence case. */
    GO_TO_NEXT_EVIDENCE_CASE("GoToNextEvidenceCase"),
    /** Action invoked when the user wants to go to the last evidence case. */
    GO_TO_LAST_EVIDENCE_CASE("GoToLastEvidenceCase"),
    /** Action invoked when the user wants to clear out all evidence cases. */
    CLEAR_OUT_ALL_EVIDENCE_CASES("ClearOutAllEvidenceCases"),
    /** Action invoked when the user wants to propagate inference. */
    PROPAGATE_EVIDENCE("PropagateEvidence"),
    /** Action invoked when the user wants to undo an operation. */
    UNDO("Undo"),
    /** Action invoked when the user wants to redo an operation. */
    REDO("Redo"),
    /**
     * Action invoked when the user wants to absorb a node into the net for arc reversal algorithm.
     * It is relevant in the case of chance or decision nodes with an only utility child.
     */
    ABSORB_NODE("AbsorbNode"),
    /** Absorb parents */
    ABSORB_PARENTS("AbsorbParents"),
    /** Action invoked when the user wants to show the additionalProperties of a node. */
    NODE_PROPERTIES("NodeProperties"),
    /** Action invoked when the user wants to show the table of a node. */
    EDIT_POTENTIAL("NodePotential"),
    /** Action invoked when the user wants to impose a policy in a decision node. */
    EVENT_EDIT_TIME_TO_EVENT("EditTimeToEvent"),
    /** Action invoked when the user wants to impose a policy in a decision node. */
    DECISION_IMPOSE_POLICY("ImposePolicy"),
    /** Action invoked when the user wants to modify the policy of a decision node. */
    DECISION_EDIT_POLICY("EditPolicy"),
    /** Action invoked when the user wants to remove a policy from a decision node. */
    DECISION_REMOVE_POLICY("RemovePolicy"),
    /** Action invoked when the user wants to show the expected utility of a decision node. */
    DECISION_SHOW_EXPECTED_UTILITY("ShowExpectedUtility"),
    /** Action invoked when the user wants to show the optimal policy of a decision node. */
    DECISION_SHOW_OPTIMAL_POLICY("ShowOptimalPolicy"),
    /** Action invoked when the user wants to show the decision tree. */
    DECISION_TREE("DecisionTree"),
    /** Action invoked when the user wants to show the optimal strategy of a decision node. */
    DECISION_SHOW_OPTIMAL_STRATEGY("ShowOptimalStrategy"),
    /* Tree contextual menu actions */
    /** Action invoked when the user wants to expand one level of a decision tree. */
    TREE_EXPAND_NEXT("ExpandNext"),
    /** Action invoked when the user wants to expand all levels of a decision tree. */
    TREE_EXPAND_ALL("ExpandAll"),
    /** Action invoked when the user wants to open the associated network of a node of the tree. */
    TREE_OPEN_NETWORK("OpenAssociatedNetwork"),
    /** Action invoked when the user wants to do something not yet implemented. TODO Rewrite */
    TREE_SHOW_CEP("ShowCEP"),
    /** Action invoked when the user wants to obtain a Graphviz/dot structure of a sub-tree. */
    TREE_SAVE_GRAPHVIZ("SaveGraphViz"),
    /* End tree contextual menu actions */
    /** Action invoked when the user wants to expand a node. */
    NODE_EXPANSION("NodeExpansion"),
    /** Action invoked when the user wants to contract a node. */
    NODE_CONTRACTION("NodeContraction"),
    /** Action invoked when the user wants to add a finding to a node. */
    NODE_ADD_FINDING("NodeAddFinding"),
    /** Action invoked when the user wants to remove a finding from a node. */
    NODE_REMOVE_FINDING("NodeRemoveFinding"),
    /** Action invoked when the user wants to remove all the finding of the current evidence case. */
    NODE_REMOVE_ALL_FINDINGS("NodeRemoveAllFindings"),
    /** Action invoked when the user wants to show the additionalProperties of a link. */
    LINK_PROPERTIES("LinkProperties"),
    /** Action invoked when the user wants to disable the linkRestrictions of a link. */
    LINK_RESTRICTION_DISABLE_PROPERTIES("LinkRestrictionDisableProperties"),
    /** Action invoked when the user wants to disable the linkRestrictions of a link. */
    LINK_RESTRICTION_EDIT_PROPERTIES("LinkRestrictionEditProperties"),
    /** Action invoked when the user wants to show the revlationArc conditions of a link */
    LINK_REVELATIONARC_PROPERTIES("RevelationArcProperties"),
    /** Action invoked when the user wants to invert a link updating its potentials. */
    INVERT_LINK_AND_UPDATE_POTENTIALS("InvertLinkAndUpdatePotentials"),
    /** Action invoked when the user wants to view a toolbar */
    VIEW_TOOLBARS("View.Toolbars"),
    /** Action invoked when the user wants to increment the zoomManager of the panel. */
    ZOOM_IN("ZoomIn"),
    /** Action invoked when the user wants to decrement the zoomManager of the panel. */
    ZOOM_OUT("ZoomOut"),
    /** Action invoked when the user wants to cut to clipboard. */
    CLIPBOARD_CUT("ClipboardCut"),
    /** Action invoked when the user wants to copy to clipboard. */
    CLIPBOARD_COPY("ClipboardCopy"),
    /** Action invoked when the user wants to paste from clipboard. */
    CLIPBOARD_PASTE("ClipboardPaste"),
    /** Action invoked when the user wants to remove an object. */
    OBJECT_REMOVAL("ObjectRemoval"),
    /** Action invoked when the user wants to learn a network. */
    LEARNING("Tools.Learning"),
    /** Action invoked when the user wants to obtain the optimal interventions. */
    COST_EFFECTIVENESS_DETERMINISTIC("Tools.CostEffectivenessDeterministic"),
    /** Action invoked when the user wants to obtain the optimal interventions. */
    COST_EFFECTIVENESS_SENSITIVITY("Tools.CostEffectivenessSensitivity"),
    /** Action invoked for Deterministic Sensitivity Analysis. */
    SENSITIVITY_ANALYSIS_DETERMINISTIC("Tools.SensitivityAnalysisDeterministic"),
    /** Action invoked for Probabilistic Sensitivity Analysis. */
    SENSITIVITY_ANALYSIS_PROBABILISTIC("Tools.SensitivityAnalysisProbabilistic"),
    /** Action invoked for Sensitivity Analysis. */
    SENSITIVITY_ANALYSIS("Tools.SensitivityAnalysis"),
    
    /** Action invoked when the user wants to expand the network. */
    EXPAND_NETWORK("CostEffectiveness.ExpandNetwork"),
    
    /** Action invoked when the user wants to display the temporal evolution by criterion. */
    TEMPORAL_EVOLUTION_BY_CRITERION("CostEffectiveness.TemporalEvolutionByCriterion"),

//	/**
//	 * Action invoked when the user wants to expands the network for CE analysis.
//	 */
//	EXPAND_NETWORK_CE("CostEffectiveness.ExpandNetworkCE"),
    /** Action invoked when the user wants to configure OPENMARKOV options */
    CONFIGURATION("Tools.Configuration"),
    /** Action invoked when the user wants to set the inference options. */
    PROPAGATION_OPTIONS("PropagationOptions"),
    /** TODO - Action invoked when the user wants to set the multicriteria options */
    INFERENCE_OPTIONS("InferenceOptions"),
    /** TODO - Action invoked when the user wants to set the temporal options */
    TEMPORAL_OPTIONS("TemporalOptions"),
    /** Action invoked when the user wants to change the language */
    HELP_CHANGE_LANGUAGE("Help.ChangeLanguage"),
    /** Action invoked when the user wants to open the shortcuts window */
    HELP_SHORTCUTS("Help.Shortcuts"),
    /** Action invoked when the user wants to open the "About..." */
    HELP_ABOUT("Help.About"),
    /** Action invoked when the user wants to assign uncertainty to potential */
    UNCERTAINTY_ASSIGN("Uncertainty.Assign"),
    /** Action invoked when the user wants to edit the uncertainty of potential */
    UNCERTAINTY_EDIT("Uncertainty.Edit"),
    /** Action invoked when the user wants to remove uncertainty on potential */
    UNCERTAINTY_REMOVE("Uncertainty.Remove"),
    // 25/08/2019 impossible configuration; 14/12/2019 add function for a Table With Events
    /** Action invoked when the user wants to establish a configuration as impossible in a TTE potential */
    SET_IMPOSSIBLE_CONFIGURATION("ImpossibleConfiguration.Set"),
    /** Action invoked when the user wants to establish a configuration as possible in a TTE potential */
    UNSET_IMPOSSIBLE_CONFIGURATION("ImpossibleConfiguration.UnSet"),
    /** Action invoked when the user wants add a function in a Table With Events */
    ADD_FUNCTION("AddFunction"),
    /** Action invoked when the user wants to log temporal evolution */
    LOG("Log"),
    /** Action invoked when the user selects another class to instantiate */
    CHANGE_ACTIVE_CLASS("PRM.ChangeActiveClass"),
    /** Action invoked when the user selects temporal evolution menu item */
    TEMPORAL_EVOLUTION_ACTION("Temporal.Evolution"),
    /** Action invoked when the user selects temporal evolution menu item */
    NEXT_SLICE_NODE("Edit.NextSliceNode"),
    /** Used only to guarantee that all zoomManager actions commands begin the same. */
    ZOOM_PREFIX("Zoom_");
    
    public String getCommandName() {
        return this.commandName;
    }
    
    private final String commandName;
    
    ActionCommands(String commandName) {
        this.commandName = commandName;
    }
    
    private static final HashMap<String, ActionCommands> COMMAND_NAME_TO_ENUM;

    private static final ActionCommands[] OPEN_LAST_FILE_COMMANDS = {
        OPEN_LAST_1_FILE, OPEN_LAST_2_FILE, OPEN_LAST_3_FILE,
        OPEN_LAST_4_FILE, OPEN_LAST_5_FILE, OPEN_LAST_6_FILE,
        OPEN_LAST_7_FILE, OPEN_LAST_8_FILE, OPEN_LAST_9_FILE
    };

    static {
        COMMAND_NAME_TO_ENUM = new HashMap<>();
        for (ActionCommands command : ActionCommands.values()) {
            ActionCommands.COMMAND_NAME_TO_ENUM.put(command.commandName, command);
        }
    }

    public static @Nullable ActionCommands of(String commandName) {
        return ActionCommands.COMMAND_NAME_TO_ENUM.get(commandName);
    }

    /** Returns the action command for opening the recent file at the given index (0-based),
     *  or null if the index is out of range. */
    public static @Nullable ActionCommands openLastFileCommandAt(int index) {
        if (index < 0 || index >= OPEN_LAST_FILE_COMMANDS.length) return null;
        return OPEN_LAST_FILE_COMMANDS[index];
    }

    /** Returns the 0-based index into the recent-files list if this command is one of the
     *  OPEN_LAST_N_FILE commands, or an empty OptionalInt otherwise. */
    public Optional<Integer> openRecentFileIndex() {
        for (int i = 0; i < OPEN_LAST_FILE_COMMANDS.length; i++) {
            if (this == OPEN_LAST_FILE_COMMANDS[i]) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Checks if the action command corresponds to a zoomManager action command.
     *
     * @param actionCommand action command.
     *
     * @return true if the action command corresponds to a zoomManager action command;
     * otherwise, false.
     */
    public static boolean isZoomActionCommand(String actionCommand) {
        int lengthZoomPrefix = ZOOM_PREFIX.commandName.length();
        if (actionCommand.length() < (lengthZoomPrefix + 1)) {
            return false;
        }
        if (actionCommand.substring(0, lengthZoomPrefix).equals(ZOOM_PREFIX.commandName)) {
            try {
                Integer.parseInt(actionCommand.substring(lengthZoomPrefix));
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Returns the zoomManager value of a zoomManager action command.
     *
     * @param actionCommand action command.
     *
     * @return the value of zoomManager of the action command or 0 if the action
     * command isn't a zoomManager menu item.
     */
    public static double getValueZoomActionCommand(String actionCommand) {
        int lengthZoomPrefix = ZOOM_PREFIX.commandName.length();
        return (isZoomActionCommand(actionCommand)) ? (
                Double.parseDouble(actionCommand.substring(lengthZoomPrefix)) / 100
        ) : 0;
    }
    
    /**
     * Returns the action command associated with the specified zoomManager value.
     *
     * @param zoom value of the zoomManager.
     *
     * @return a string that represents an action command associated with the
     * zoomManager value.
     */
    public static String getZoomActionCommandValue(double zoom) {
        return ZOOM_PREFIX.commandName + (int) Math.round(zoom * 100);
    }
    
    @Override public String toString() {
        return this.commandName;
    }
}
