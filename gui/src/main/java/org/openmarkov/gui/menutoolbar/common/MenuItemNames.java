/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

/**
 * Constants for string database tokens used to identify menu items.
 * Each constant maps to a key in the localization XML files, used to resolve
 * menu labels, mnemonics, and tooltips.
 */
public class MenuItemNames {
	/**
	 * String database token for the menu 'File'.
	 */
	public static final String FILE_MENU = "File";

	/**
	 * String database token for the item 'File - New'.
	 */
	public static final String FILE_NEW_MENUITEM = FILE_MENU + ".New";

	/**
	 * String database token for the item 'File - Open'.
	 */
	public static final String FILE_OPEN_MENUITEM = FILE_MENU + ".Open";

	/**
	 * String database token for the item 'File - Open from URL'.
	 */
	public static final String FILE_OPEN_URL_MENUITEM = FILE_MENU + ".OpenURL";

	/**
	 * String database token for the item 'File - Save'.
	 */
	public static final String FILE_SAVE_MENUITEM = FILE_MENU + ".Save";

	/**
	 * String database token for the item 'File - Save'.
	 */
	public static final String FILE_SAVE_OPEN_MENUITEM = FILE_MENU + ".SaveOpen";

	/**
	 * String database token for the item 'File - Save as'.
	 */
	public static final String FILE_SAVEAS_MENUITEM = FILE_MENU + ".SaveAs";

	/**
	 * String database token for the item 'File - Close'.
	 */
	public static final String FILE_CLOSE_MENUITEM = FILE_MENU + ".Close";

	/**
	 * String database token for the item 'File - Load Evidence'.
	 */
	public static final String FILE_LOAD_EVIDENCE_MENUITEM = FILE_MENU + ".LoadEvidence";

	/**
	 * String database token for the item 'File - Save Evidence'.
	 */
	public static final String FILE_SAVE_EVIDENCE_MENUITEM = FILE_MENU + ".SaveEvidence";

	/**
	 * String database token for the item 'Expand network'.
	 */
	public static final String EXPAND_NETWORK_MENUITEM = "CostEffectiveness.ExpandNetwork";

	/**
	 * String database token for the item 'Expand network'.
	 */
	public static final String TEMPORAL_EVOLUTION_BY_CRITERION_MENUITEM = "CostEffectiveness.TemporalEvolutionByCriterion";

//	/**
//	 * String database token for the item 'Expand network for CE analysis'.
//	 */
//	public static final String EXPAND_NETWORK_CE_MENUITEM = "CostEffectiveness.ExpandNetworkCE";
	/**
	 * String database token for the item 'File - Network additionalProperties'.
	 */
	public static final String FILE_NETWORKPROPERTIES_MENUITEM = FILE_MENU + ".NetworkProperties";

	/**
	 * String database token for the item 'File - Exit'.
	 */
	public static final String FILE_EXIT_MENUITEM = FILE_MENU + ".Exit";

	/**
	 * String database token for the menu 'Edit'.
	 */
	public static final String EDIT_MENU = "Edit";

	/**
	 * String database token for the item 'Edit - Cut'.
	 */
	public static final String EDIT_CUT_MENUITEM = EDIT_MENU + ".Cut";

	/**
	 * String database token for the item 'Edit - Copy'.
	 */
	public static final String EDIT_COPY_MENUITEM = EDIT_MENU + ".Copy";

	/**
	 * String database token for the item 'Edit - Paste'.
	 */
	public static final String EDIT_PASTE_MENUITEM = EDIT_MENU + ".Paste";

	/**
	 * String database token for the item 'Edit - Remove'.
	 */
	public static final String EDIT_REMOVE_MENUITEM = EDIT_MENU + ".Remove";

	/**
	 * String database token for the item 'Edit - Undo'.
	 */
	public static final String EDIT_UNDO_MENUITEM = EDIT_MENU + ".Undo";

	/**
	 * String database token for the item 'Edit - Redo'.
	 */
	public static final String EDIT_REDO_MENUITEM = EDIT_MENU + ".Redo";

	/**
	 * String database token for the item 'Edit - Select all'.
	 */
	public static final String EDIT_SELECTALL_MENUITEM = EDIT_MENU + ".SelectAll";

	/**
	 * String database token for the item 'Edit - Auto-arrange'.
	 */
	public static final String EDIT_AUTOARRANGE_MENUITEM = EDIT_MENU + ".AutoArrange";

	/**
	 * String database token for the item 'Edit - Object selection'.
	 */
	public static final String EDIT_MODE_SELECTION_MENUITEM = EDIT_MENU + ".Mode.Selection";

	/**
	 * String database token for the item 'Edit - Chance nodes creation'.
	 */
	public static final String EDIT_MODE_CHANCE_MENUITEM = EDIT_MENU + ".Mode.Chance";

	/**
	 * String database token for the item 'Edit - Decision nodes creation'.
	 */
	public static final String EDIT_MODE_DECISION_MENUITEM = EDIT_MENU + ".Mode.Decision";

	/**
	 * String database token for the item 'Edit - Utility nodes creation'.
	 */
	public static final String EDIT_MODE_UTILITY_MENUITEM = EDIT_MENU + ".Mode.Utility";

	/**
	 * String database token for the item 'Edit - Links creation'.
	 */
	public static final String EDIT_MODE_LINK_MENUITEM = EDIT_MENU + ".Mode.Link";

	//TODO OOPN start
	/**
	 * String database token for the item 'Edit - Instance Input'.
	 */
	public static final String EDIT_MARKASINPUT_MENUITEM = EDIT_MENU + ".MarkAsInput";
	/**
	 * String database token for the item 'Edit - Instance Input'.
	 */
	public static final String EDIT_CLASS_MENUITEM = EDIT_MENU + ".EditClass";

	/**
	 * String database token for the item 'Edit - Instance Name'.
	 */
	public static final String EDIT_INSTANCE_NAME_MENUITEM = EDIT_MENU + ".EditInstanceName";

	/**
	 * String database token for the item 'Arity'.
	 */
	public static final String ARITY_MENUITEM = EDIT_MENU + ".Arity";
	//TODO OOPN end

    /**
     * String database token for the item 'Edit - Absorb Node'.
     */
    public static final String EDIT_ABSORBNODE_MENUITEM = EDIT_MENU + ".AbsorbNode";
    
    /**
     * String database token for the item 'Edit - Absorb Parents'.
     */
    public static final String EDIT_ABSORBPARENTS_MENUITEM = EDIT_MENU + ".AbsorbParents";
    
	/**
	 * String database token for the item 'Edit - Node additionalProperties'.
	 */
	public static final String EDIT_NODEPROPERTIES_MENUITEM = EDIT_MENU + ".NodeProperties";

	/**
	 * String database token for the item 'Edit - Node table'.
	 */
	public static final String EDIT_NODERELATION_MENUITEM = EDIT_MENU + ".NodePotential";

	/**
	 * String database token for the item 'Edit - Link additionalProperties'.
	 */
	public static final String EDIT_LINKPROPERTIES_MENUITEM = EDIT_MENU + ".LinkProperties";

	/**
	 * String database token for the item 'Edit - Link enable linkRestrictions'.
	 */
	public static final String EDIT_LINKRESTRICTION_ENABLE_MENUITEM = EDIT_MENU + ".LinkRestrictionEnable";

	/**
	 * String database token for the item 'Edit - Link disable linkRestrictions'.
	 */
	public static final String EDIT_LINKRESTRICTION_DISABLE_MENUITEM = EDIT_MENU + ".LinkRestrictionDisable";

	/**
	 * String database token for the item 'Edit - Link edit linkRestrictions'.
	 */
	public static final String EDIT_LINKRESTRICTION_EDIT_MENUITEM = EDIT_MENU + ".LinkRestrictionEdit";

	/**
	 * String database token for the item 'Edit - Link revelationArc'.
	 */
	public static final String EDIT_LINKREVELATIONARC_MENUITEM = EDIT_MENU + ".RevelationArc";

	/**
	 * String database token for the item 'Edit - Revert arc'.
	 */
	public static final String EDIT_INVERT_LINK_AND_UPDATE_POTENTIALS_MENUITEM = EDIT_MENU + ".InvertLinkAndUpdatePotentials";

	/**
	 * String database token for the item 'Edit - Switch to Inference mode'.
	 */
	public static final String EDIT_SWITCH_TO_INFERENCE_MODE_MENUITEM = EDIT_MENU + ".SwitchToInference";

	/**
	 * String database token for the menu 'Inference'.
	 */
	public static final String INFERENCE_MENU = "Inference";

	/**
	 * String database token for the item 'Inference - Switch to Edition mode'.
	 */
	public static final String INFERENCE_SWITCH_TO_EDITION_MODE_MENUITEM = INFERENCE_MENU + ".SwitchToEdition";

	/**
	 * String database token for the item 'Inference - Inference Options '.
	 */
	public static final String PROPAGATION_OPTIONS_MENUITEM = INFERENCE_MENU + ".Options";

	/**
	 * String database token for the item 'Inference - Create New Evidence Case'.
	 */
	public static final String INFERENCE_CREATE_NEW_EVIDENCE_CASE_MENUITEM = INFERENCE_MENU + ".CreateNewEvidenceCase";

	/**
	 * String database token for the item 'Inference - Go To First Evidence Case'.
	 */
	public static final String INFERENCE_GO_TO_FIRST_EVIDENCE_CASE_MENUITEM = INFERENCE_MENU + ".GoToFirstEvidenceCase";

	/**
	 * String database token for the item 'Inference - Go To Previous Evidence Case'.
	 */
	public static final String INFERENCE_GO_TO_PREVIOUS_EVIDENCE_CASE_MENUITEM = INFERENCE_MENU
			+ ".GoToPreviousEvidenceCase";

	/**
	 * String database token for the item 'Inference - Go To Next Evidence Case'.
	 */
	public static final String INFERENCE_GO_TO_NEXT_EVIDENCE_CASE_MENUITEM = INFERENCE_MENU + ".GoToNextEvidenceCase";

	/**
	 * String database token for the item 'Inference - Go To Last Evidence Case'.
	 */
	public static final String INFERENCE_GO_TO_LAST_EVIDENCE_CASE_MENUITEM = INFERENCE_MENU + ".GoToLastEvidenceCase";

	/**
	 * String database token for the item 'Inference - Clear Out All Evidence Cases'.
	 */
	public static final String INFERENCE_CLEAR_OUT_ALL_EVIDENCE_CASES_MENUITEM = INFERENCE_MENU
			+ ".ClearOutAllEvidenceCases";

	/**
	 * String database token for the item 'Inference - Propagate Evidence'.
	 */
	public static final String INFERENCE_PROPAGATE_EVIDENCE_MENUITEM = INFERENCE_MENU + ".PropagateEvidence";

	/**
	 * String database token for the item 'Inference - Expand Node'.
	 */
	public static final String INFERENCE_EXPAND_NODE_MENUITEM = INFERENCE_MENU + ".Expansion";

	/**
	 * String database token for the item 'Inference - Contract Node'.
	 */
	public static final String INFERENCE_CONTRACT_NODE_MENUITEM = INFERENCE_MENU + ".Contraction";

	/**
	 * String database token for the item 'Inference - AddFinding'.
	 */
	public static final String INFERENCE_ADD_FINDING_MENUITEM = INFERENCE_MENU + ".AddFinding";

	/**
	 * String database token for the item 'Inference - RemoveFinding'.
	 */
	public static final String INFERENCE_REMOVE_FINDING_MENUITEM = INFERENCE_MENU + ".RemoveFinding";

	/**
	 * String database token for the item 'Inference - RemoveFinding'.
	 */
	public static final String INFERENCE_REMOVE_ALL_FINDINGS_MENUITEM = INFERENCE_MENU + ".RemoveAllFindings";

	/**
	 * String database token for the menu 'Inference - Inference Options"
	 */
	public static final String INFERENCE_OPTIONS_MENUITEM = INFERENCE_MENU + ".InferenceOptions";

	/**
	 * String database token for the menu 'View'.
	 */
	public static final String VIEW_MENU = "View";
    
    /**
     * String database token for the item 'View - Change scale'.
     */
    public static final String VIEW_CHANGE_SCALE = VIEW_MENU + ".ChangeScale";
    
    /**
     * String database token for the item 'View - Go next tab'.
     */
    public static final String VIEW_GO_NEXT_TAB = VIEW_MENU + ".GoNextTab";
    
    /**
     * String database token for the item 'View - Go previous tab'.
     */
    public static final String VIEW_GO_PREVIOUS_TAB = VIEW_MENU + ".GoPreviousTab";

	/**
	 * String database token for the item 'View - Nodes'.
	 */
	public static final String VIEW_NODES_MENU = VIEW_MENU + ".Nodes";

	/**
	 * String database token for the item 'View - Nodes - By name'.
	 */
	public static final String VIEW_NODES_BYNAME_MENUITEM = VIEW_NODES_MENU + ".ByName";

	/**
	 * String database token for the item 'View - Nodes - By title'.
	 */
	public static final String VIEW_NODES_BYTITLE_MENUITEM = VIEW_NODES_MENU + ".ByTitle";

	/**
	 * String database token for the item 'View - ZoomManager'.
	 */
	public static final String VIEW_ZOOM_MENU = VIEW_MENU + ".ZoomManager";

	/**
	 * String database token for the item 'View - ZoomManager - ZoomManager in'.
	 */
	public static final String VIEW_ZOOM_IN_MENUITEM = VIEW_ZOOM_MENU + ".ZoomIn";

	/**
	 * String database token for the item 'View - ZoomManager - ZoomManager out'.
	 */
	public static final String VIEW_ZOOM_OUT_MENUITEM = VIEW_ZOOM_MENU + ".ZoomOut";

	/**
	 * String database token for the item 'View - ZoomManager - 500%'.
	 */
	public static final String VIEW_ZOOM_500_MENUITEM = VIEW_ZOOM_MENU + ".x500";

	/**
	 * String database token for the item 'View - ZoomManager - 200%'.
	 */
	public static final String VIEW_ZOOM_200_MENUITEM = VIEW_ZOOM_MENU + ".x200";

	/**
	 * String database token for the item 'View - ZoomManager - 150%'.
	 */
	public static final String VIEW_ZOOM_150_MENUITEM = VIEW_ZOOM_MENU + ".x150";

	/**
	 * String database token for the item 'View - ZoomManager - 100%'.
	 */
	public static final String VIEW_ZOOM_100_MENUITEM = VIEW_ZOOM_MENU + ".x100";

	/**
	 * String database token for the item 'View - ZoomManager - 75%'.
	 */
	public static final String VIEW_ZOOM_75_MENUITEM = VIEW_ZOOM_MENU + ".x75";

	/**
	 * String database token for the item 'View - ZoomManager - 50%'.
	 */
	public static final String VIEW_ZOOM_50_MENUITEM = VIEW_ZOOM_MENU + ".x50";

	/**
	 * String database token for the item 'View - ZoomManager - 25%'.
	 */
	public static final String VIEW_ZOOM_25_MENUITEM = VIEW_ZOOM_MENU + ".x25";

	/**
	 * String database token for the item 'View - ZoomManager - 10%'.
	 */
	public static final String VIEW_ZOOM_10_MENUITEM = VIEW_ZOOM_MENU + ".x10";

	/**
	 * String database token for the item 'View - ZoomManager - Other'.
	 */
	public static final String VIEW_ZOOM_OTHER_MENUITEM = VIEW_ZOOM_MENU + ".Other";

	/* Tree contextual menu */
	/**
	 * String database token for the menu 'Tools'.
	 */
	public static final String TREE_MENU = "Tree";
	/**
	 * String database token for the item 'Tree - Expand next'.
	 */
	public static final String TREE_EXPAND_NEXT_MENUITEM = TREE_MENU + ".ExpandNext";

	/**
	 * String database token for the item 'Tree - Expand all'.
	 */
	public static final String TREE_EXPAND_ALL_MENUITEM = TREE_MENU + ".ExpandAll";
	/**
	 * String database token for the item 'Tree - Open network'.
	 */
	public static final String TREE_OPEN_NETWORK_MENUITEM = TREE_MENU + ".OpenNetwork";

	/**
	 * String database token for the item 'Tree - Show CEP'.
	 */
	public static final String TREE_SHOW_CEP_MENUITEM = TREE_MENU + ".ShowCEP";

    /**
     * String database token for the item 'Tree - Save Graphviz'.
     */
    public static final String TREE_SAVE_GRAPHVIZ = TREE_MENU + ".SaveGraphviz";

	/* End tree contextual menu */

	/**
	 * String database token for the menu 'Tools'.
	 */
	public static final String TOOLS_MENU = "Tools";

	/**
	 * String database token for the item 'Tools - Learning'.
	 */
	public static final String LEARNING_MENUITEM = TOOLS_MENU + ".Learning";

	/**
	 * String database token for the item 'Tools - CostEffectiveness'.
	 */
	public static final String COSTEFFECTIVENESS_SUBMENU = TOOLS_MENU + ".CostEffectiveness";

	/**
	 * String database token for the item 'Tools - CostEffectiveness'.
	 */
	public static final String COSTEFFECTIVENESSDETERMINISTIC_MENUITEM = TOOLS_MENU + ".CostEffectivenessDeterministic";

	/**
	 * String database token for the item 'Tools - CostEffectiveness'.
	 */
	public static final String COSTEFFECTIVENESSSENSITIVITY_MENUITEM = TOOLS_MENU + ".CostEffectivenessSensitivity";

	/**
	 * String database token for the item 'Tools - Sensitivity Analysis'.
	 */
	public static final String SENSITIVITYANALYSIS_SUBMENU = TOOLS_MENU + ".SensitivityAnalysis";

	/**
	 * String database token for the item 'Tools - Sensitivity Analysis - Deterministic'.
	 */
	public static final String SENSITIVITYANALYSISDETERMINISTIC_MENUITEM = TOOLS_MENU
			+ ".SensitivityAnalysisDeterministic";

	/**
	 * String database token for the item 'Tools - Sensitivity Analysis - Probabilistic'.
	 */
	public static final String SENSITIVITYANALYSISPROBABILISTIC_MENUITEM = TOOLS_MENU
			+ ".SensitivityAnalysisProbabilistic";

	/**
	 * String database token for the item 'Tools - Configuration'.
	 */
	public static final String CONFIGURATION_MENUITEM = TOOLS_MENU + ".Configuration";

	/**
	 * String database token for the menu 'Help'.
	 */
	public static final String HELP_MENU = "Help";

	/**
	 * String database token for the item 'Help - Help'.
	 */
	public static final String HELP_HELP_MENUITEM = HELP_MENU + ".Help";

	/**
	 * String database token for the item 'Help - Language'.
	 */
	public static final String HELP_CHANGELANGUAGE_MENUITEM = HELP_MENU + ".ChangeLanguage";

	/**
	 * String database token for the item 'Help - Shortcuts'.
	 */
	public static final String HELP_SHORTCUTS_MENUITEM = HELP_MENU + ".Shortcuts";

	/**
	 * String database token for the item 'Help - About'.
	 */
	public static final String HELP_ABOUT_MENUITEM = HELP_MENU + ".About";

	/**
	 * String database token for the item 'Assign'. Only (momently) in contextual menu
	 */
	public static final String UNCERTAINTY_ASSIGN_MENUITEM = "Uncertainty.Assign";

	/**
	 * String database token for the item 'Edit'. Only (momently) in contextual menu
	 */
	public static final String UNCERTAINTY_EDIT_MENUITEM = "Uncertainty.Edit";

	/**
	 * String database token for the item 'Remove'. Only (momently) in contextual menu
	 */
	public static final String UNCERTAINTY_REMOVE_MENUITEM = "Uncertainty.Remove";

	public static final String EDIT_LOG_MENUITEM = EDIT_MENU + ".Log";
	
	// 26/08/2019 Contextual Menu Item for setting in the TTE potential impossible configurations
	// 14/12/2019 Added contextual menu item for adding a function to a TableWithEvents
	/**
	 * String database token for the TTE contextual menu item Set impossible configuration.
	 */
	public static final String SET_IMPOSSIBLE_CONFIGURATION_MENUITEM = "ImpossibleConfiguration.Set" ;
	
	/**
	 * String database token for the TTE contextual menu item Set possible configuration.
	 */
	public static final String UNSET_IMPOSSIBLE_CONFIGURATION_MENUITEM = "ImpossibleConfiguration.UnSet";
	
	/**
	 * String database token for the TTE contextual menu item Set possible configuration.
	 */
	public static final String ADD_FUNCTION = "AddFunction";

	/**
	 * String database token for the item 'Impose Policy' in decision node's contextual menu
	 */
	public static final String DECISION_IMPOSE_POLICY_MENUITEM = "Decision.ImposePolicy";

	/**
	 * String database token for the item 'Edit Policy' in decision node's contextual menu
	 */
	public static final String DECISION_EDIT_POLICY_MENUITEM = "Decision.EditPolicy";

	/**
	 * String database token for the item 'Remove Policy' in decision node's contextual menu
	 */
	public static final String DECISION_REMOVE_POLICY_MENUITEM = "Decision.RemovePolicy";
	
	/**
	 * String database token for the item 'Impose Policy' in decision node's contextual menu
	 */
	public static final String EVENT_EDIT_TIME_TO_EVENT_MENUITEM = "Event.EditTimeToEvent";
	
	/**
	 * String database token for the item 'Show Expected Utility' in decision node's contextual menu
	 */
	public static final String SHOW_EXPECTED_UTILITY_MENUITEM = "Decision.ShowExpectedUtility";

	/**
	 * String database token for the item 'Show Optimal Policy' in decision node's contextual menu
	 */
	public static final String SHOW_OPTIMAL_POLICY_MENUITEM = "Decision.ShowOptimalPolicy";

	/**
	 * String database token for the item 'Temporal Evolution' in temporal node's contextual menu
	 */
	public static final String TEMPORAL_EVOLUTION_MENUITEM = "CostEffectiveness.TemporalEvolution";

	/**
	 * String database token for the item 'Next Slice' in a temporal node's contextual menu
	 */
	public static final String NEXT_SLICE_NODE = "Edit.NextSliceNode";

}
