/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.action.base.StateAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static utility methods for managing agents in a {@link ProbNet}.
 * <p>
 * Agents are named entities (with optional properties) that can be
 * assigned to decision nodes in multiagent influence diagrams.
 * This class handles the add/remove/rearrange operations and the
 * cascading cleanup of agent references in nodes.
 * <p>
 * Follows the same pattern as {@link ProbNetClassifier} and
 * {@link VariableStateOperations}.
 *
 * @author Manuel Arias
 * @see ProbNet#getAgents()
 * @see ProbNet#setAgents(List)
 */
public final class ProbNetAgentManager {

    private ProbNetAgentManager() {
    }

    /**
     * Adds, removes, or rearranges an agent in the network's agent list.
     * <p>
     * For {@code REMOVE}, the agent is also cleared from any decision node
     * it was assigned to. For {@code DOWN}, {@code RENAME}, and {@code UP},
     * the entire agent list is replaced from {@code dataTable}.
     *
     * @param probNet     the network to modify
     * @param stateAction the action to perform (ADD, REMOVE, RENAME, UP, DOWN)
     * @param agentName   the name of the agent to modify
     * @param dataTable   the full agent table used for RENAME/UP/DOWN operations
     */
    public static void modifyAgent(ProbNet probNet, StateAction stateAction,
                                   String agentName, Object[][] dataTable) {
        List<StringWithProperties> agents = probNet.getAgents();
        switch (stateAction) {
            case ADD:
                if (agents == null) {
                    agents = new ArrayList<>();
                }
                agents.add(new StringWithProperties(agentName));
                probNet.setAgents(agents);
                break;
            case REMOVE:
                if (agents == null) break;
                StringWithProperties agentToRemove = null;
                for (StringWithProperties agente : agents) {
                    if (agente.getString().equals(agentName)) {
                        agentToRemove = agente;
                    }
                }
                agents.remove(agentToRemove);
                // Also delete this agent from any node it was assigned to
                if (agentToRemove != null) {
                    for (Node node : probNet.getNodes()) {
                        StringWithProperties nodeAgent = node.getVariable().getAgent();
                        if (nodeAgent != null && nodeAgent.getString().equals(agentName)) {
                            node.getVariable().setAgent(null);
                        }
                    }
                }
                probNet.setAgents(agents);
                break;
            case DOWN, RENAME, UP:
                List<StringWithProperties> oldAgents = agents == null ? List.of() : agents;
                Set<String> newNames = new HashSet<>();
                for (Object[] objects : dataTable) {
                    newNames.add((String) objects[0]);
                }
                ArrayList<StringWithProperties> modifiedAgent = new ArrayList<>();
                for (Object[] objects : dataTable) {
                    String name = (String) objects[0];
                    StringWithProperties agent = agentNamed(oldAgents, name);
                    if (agent == null) {
                        agent = renamed(probNet, oldAgents, newNames, name);
                    }
                    modifiedAgent.add(agent);
                }
                probNet.setAgents(modifiedAgent);
                break;
        }
    }
    private static StringWithProperties agentNamed(List<StringWithProperties> agents, String name) {
        for (StringWithProperties agent : agents) {
            if (agent.getString().equals(name)) {
                return agent;
            }
        }
        return null;
    }

    /**
     * Creates the agent {@code newName} with the properties of the agent that is no longer in the list,
     * and gives it the nodes of that agent.
     */
    private static StringWithProperties renamed(ProbNet probNet, List<StringWithProperties> oldAgents,
                                                Set<String> newNames, String newName) {
        StringWithProperties agent = new StringWithProperties(newName);
        for (StringWithProperties oldAgent : oldAgents) {
            if (!newNames.contains(oldAgent.getString())) {
                agent.put(oldAgent.getAdditionalProperties().clone());
                for (Node node : probNet.getNodes()) {
                    StringWithProperties nodeAgent = node.getVariable().getAgent();
                    if (nodeAgent != null && nodeAgent.getString().equals(oldAgent.getString())) {
                        node.getVariable().setAgent(agent);
                    }
                }
                break;
            }
        }
        return agent;
    }
}
