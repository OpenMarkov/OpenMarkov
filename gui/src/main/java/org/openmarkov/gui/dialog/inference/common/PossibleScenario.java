/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The selectors of the scenario of a decision, one per informational predecessor, in their order. Each selector
 * offers only the states that are possible with the findings and with the states chosen in the selectors before it,
 * so that the scenario chosen is always possible. A predecessor with a finding shows its state and cannot be chosen.
 *
 * @author Manuel Arias
 */
public class PossibleScenario {

    private final ProbNet probNet;
    private final EvidenceCase evidence;
    private final LinkedHashMap<JComboBox<String>, Variable> selectors = new LinkedHashMap<>();
    /** Whether the selectors are being changed here, and not by the user. */
    private boolean adjusting = false;

    /**
     * @param probNet  the network
     * @param evidence the findings before the resolution
     */
    public PossibleScenario(ProbNet probNet, EvidenceCase evidence) {
        this.probNet = probNet;
        this.evidence = evidence;
    }

    /**
     * @param onChange what to do after the user chooses a state
     * @return a new selector of the states of {@code variable}, the next one of the scenario
     */
    public JComboBox<String> selector(Variable variable, Runnable onChange) {
        JComboBox<String> selector = new JComboBox<>(stateNames(variable));
        if (evidence.contains(variable)) {
            selector.setSelectedItem(evidence.getFinding(variable).getState());
            selector.setEnabled(false);
        }
        selector.addActionListener(e -> {
            if (!adjusting) {
                offerPossibleStates(new ArrayList<>(selectors.keySet()).indexOf(selector) + 1);
                onChange.run();
            }
        });
        selectors.put(selector, variable);
        return selector;
    }

    /** Makes each selector offer only its possible states; to be called once all of them have been added. */
    public void offerPossibleStates() {
        offerPossibleStates(0);
    }

    /** From the selector {@code from} on, each one offers its possible states and keeps its choice if it is one. */
    private void offerPossibleStates(int from) {
        adjusting = true;
        try {
            List<JComboBox<String>> all = new ArrayList<>(selectors.keySet());
            for (int i = from; i < all.size(); i++) {
                JComboBox<String> selector = all.get(i);
                Variable variable = selectors.get(selector);
                if (evidence.contains(variable)) {
                    continue;
                }
                EvidenceCase before = findingsOf(all.subList(0, i));
                List<String> possible = new ArrayList<>();
                for (State state : variable.getStates()) {
                    EvidenceCase withState = new EvidenceCase(before);
                    withState.changeFinding(new Finding(variable, state));
                    if (isPossible(withState)) {
                        possible.add(state.getName());
                    }
                }
                if (possible.isEmpty()) {
                    // The findings are impossible already: no state can help it
                    possible = Arrays.asList(stateNames(variable));
                }
                Object chosen = selector.getSelectedItem();
                selector.setModel(new DefaultComboBoxModel<>(possible.toArray(new String[0])));
                selector.setSelectedItem(possible.contains(chosen) ? chosen : possible.getFirst());
            }
        } finally {
            adjusting = false;
        }
    }

    /** @return the findings before the resolution and the states chosen in {@code chosen} */
    private EvidenceCase findingsOf(List<JComboBox<String>> chosen) {
        EvidenceCase findings = new EvidenceCase(evidence);
        for (JComboBox<String> selector : chosen) {
            Variable variable = selectors.get(selector);
            findings.changeFinding(new Finding(variable, variable.getState(selector.getSelectedItem().toString())));
        }
        return findings;
    }

    /** Whether some findings can happen; if that cannot be computed, they are taken as possible. */
    private boolean isPossible(EvidenceCase findings) {
        try {
            return VEPropagation.findingsArePossible(probNet, findings);
        } catch (IncompatibleEvidenceException impossible) {
            return false;
        } catch (OpenMarkovException cannotBeComputed) {
            return true;
        }
    }

    /** @return the state chosen in each selector, as findings */
    public List<Finding> findings() {
        List<Finding> findings = new ArrayList<>();
        for (Map.Entry<JComboBox<String>, Variable> entry : selectors.entrySet()) {
            State state = entry.getValue().getState(entry.getKey().getSelectedItem().toString());
            if (state != null) {
                findings.add(new Finding(entry.getValue(), state));
            }
        }
        return findings;
    }

    private static String[] stateNames(Variable variable) {
        return Arrays.stream(variable.getStates()).map(State::getName).toArray(String[]::new);
    }
}
