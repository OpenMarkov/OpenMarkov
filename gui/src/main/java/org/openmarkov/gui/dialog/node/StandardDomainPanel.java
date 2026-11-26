
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.util.GUIDefaultStates;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Panel presenting radio buttons for selecting one of the standard domains on offer (e.g.
 * absent/present, no/yes, negative/positive), which are the ones the user wrote in the preferences
 * followed by the ones the program brings.
 */
@SuppressWarnings("serial") public class StandardDomainPanel extends JPanel {
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final ArrayList<JRadioButton> radioButtons = new ArrayList<JRadioButton>();
    
    private final List<List<String>> states;
    private final Node node;
    private final VariableType variableType;
    
    private final static String[] NUMERIC_DOMAINS = new String[]{
            "[0,1]",
            "(0,1)",
            "(0, +∞)",
            "[0, +∞)",
            "(-∞, 0)",
            "(-∞, 0]",
            "(-∞, +∞)",
    };
    
    public StandardDomainPanel(Node node) {
        this.node = node;
        this.variableType = this.node.getVariable().getVariableType();
        this.states = switch (this.variableType) {
            case FINITE_STATES, DISCRETIZED, EVENT -> GUIDefaultStates.getAllDomains();
            case NUMERIC -> Arrays.stream(StandardDomainPanel.NUMERIC_DOMAINS).map(List::of).toList();
        };
        initialize();
        repaint();
    }
    
    public void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // ButtonGroup buttonGroup = new ButtonGroup();
        for (List<String> defaultState : states) {
            JRadioButton radioButton = new JRadioButton(GUIDefaultStates.getString(defaultState));
            // radioButton.addItemListener(this);
            radioButtons.add(radioButton);
            buttonGroup.add(radioButton);
            radioButton.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    chosenStates = defaultState;
                }
            });
            add(radioButton, BorderLayout.CENTER);
        }
    }
    
    
    public ArrayList<JRadioButton> getRadioButtons() {
        return radioButtons;
    }
    
    public List<String> getChosenStates() {
        return Collections.unmodifiableList(this.chosenStates);
    }
    
    /** @return whether a domain has been chosen */
    public boolean hasChosenStates() {
        return chosenStates != null;
    }
    
    private List<String> chosenStates;
}
