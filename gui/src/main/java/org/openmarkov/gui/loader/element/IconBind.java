package org.openmarkov.gui.loader.element;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.exception.ResourceNotFoundException;

import javax.swing.*;
import java.net.URL;

/**
 * Safe bindings to icons files.
 *
 * @author jrico
 */
public enum IconBind {
    
    NEW_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "new.png"),
    OPEN_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "open.png"),
    OPEN_URL_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "openURL.png"),
    SAVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "save.png"),
    CLOSE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "close.png"),
    SETTINGS_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "cog.png"),
    UNDO_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "undo.png"),
    REDO_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "redo.png"),
    ACCEPT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "green_ok.png"),
    APPLY_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "green_apply.png"),
    SELECTION_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "selection.png"),
    CHANCE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "chance.png"),
    DECISION_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "decision.png"),
    EVENT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH +"event.png"),
    UTILITY_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "utility.png"),
    LINK_PARENT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "link.png"),
    LINK_CHILD_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "linkUp.png"),
    EDIT_PROBABILITIES_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "edit_probabilities.png"),
    ZOOM_IN_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "zoomin.png"),
    ZOOM_OUT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "zoomout.png"),
    CUT_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "cut.png"),
    COPY_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "copy.png"),
    PASTE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "paste.png"),
    REMOVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "remove.png"),
    ARROW_UP_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "arrowUp.png"),
    ARROW_DOWN_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "arrowDown.png"),
    PLUS_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "plus.png"),
    MINUS_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "minus.png"),
    INFINITE_POSITIVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "positiveInfinite.png"),
    INFINITE_NEGATIVE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "negativeInfinite.png"),
    OPENMARKOV_LOGO_16(Locations.STANDARD_RESOURCE_ICONS_PATH + "OM_16p4.png"),
    EDITION_MODE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "edition_mode.png"),
    INFERENCE_MODE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "inference_mode.png"),
    SIMULATION_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH +"simulate.png"),
    
    CREATE_NEW_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "createNewCase.png"),
    GO_TO_FIRST_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goFirst.png"),
    GO_TO_PREVIOUS_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goPrevious.png"),
    GO_TO_NEXT_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goNext.png"),
    GO_TO_LAST_EVIDENCE_CASE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "goLast.png"),
    CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "clearOutAllCases.png"),
    PROPAGATE_EVIDENCE_ENABLED(Locations.STANDARD_RESOURCE_ICONS_PATH + "propagate_evidence.png"),
    UNCERTAINTY(Locations.STANDARD_RESOURCE_ICONS_PATH + "uncertainty2.png"),
    DECISION_TREE(Locations.STANDARD_RESOURCE_ICONS_PATH + "dectree.png"),
    OPTIMAL_STRATEGY(Locations.STANDARD_RESOURCE_ICONS_PATH + "optimalStrategy.png"),
    COST_EFFECTIVENESS(Locations.STANDARD_RESOURCE_ICONS_PATH + "costEffectiveness.png"),
    SENS_ANALYSIS(Locations.STANDARD_RESOURCE_ICONS_PATH + "sensAnalysis.png");
    
    public final String fileName;
    
    IconBind(String fileName) {
        this.fileName = fileName;
    }
    
    public ImageIcon icon() {
        URL icon = IconBind.class.getResource(this.fileName);
        if (icon == null) {
            throw new UnreachableException(new ResourceNotFoundException(this.fileName));
        }
        return ImageLoader.load(icon);
    }
    
    
}
