package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PurposeType;
import org.openmarkov.inference.DES.exception.NodeMustBeEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores event data.
 *
 * @author cmyago
 * @version 2.1 -16/08/2022 - time of occurrence is variableValue
 * @version 2.2 -26/10/2023 - TTE is variableValue; time of occurrence variable added
 */
public class EventRecord extends DESRecord {


    /**
     * Is a terminal event. With this event the simulation has ended
     */
    private boolean terminal = false;


    /**
     * Is initial event. With this event the simulation has ended
     */
    private boolean initial = false;

    private double timeOfOccurrence = 0;

    /**
     * Event descendants of NODETYPE= CHANCE
     */
    private HashSet<ChanceRecord> chanceDescendants;
    //Immediate utility
    private Collection<UtilityRecord> utilityChildren;
    /**
     * Event descendants of NODETYPE= UTILITY
     */
    private Collection<UtilityRecord> utilityDescendants;

    /**
     * Children  of NODETYPE= EVENT
     */
    private HashSet<EventRecord> eventChildren;

    //12/03/2023 - removing the necessity of an initial node
    /**
     * True if it hasn't an event parent or event ancestor
     */
    private final boolean isEventOrphan;

    //12/03/2023 - end

    /**
     * Constructor. Currently the event is only once in the queue altough it can be inserted again after it has happened
     *
     * @param event Node with NodeType.EVENT stored in this EventRecord
     * @throws OpenMarkovException
     */
    public EventRecord(Node event) throws OpenMarkovException {
        super(event);
        if (event.getNodeType() != NodeType.EVENT) throw new NodeMustBeEvent(event);
        terminal = event.getPurpose().equals(PurposeType.TERMINAL_EVENT.getName());
        initial = event.getPurpose().equals(PurposeType.INITIAL_EVENT.getName());
        isEventOrphan = !event.getParents().stream().anyMatch(eventParent->eventParent.getNodeType()==NodeType.EVENT);
    }


    @Override
    public void clear() {
        super.clear();
        this.clock = 0;
    }

    public void findDescendants(DESInference desInference) {
        eventChildren = new HashSet<>();
        utilityChildren = new HashSet<>();

        chanceDescendants = new HashSet<>();
        utilityDescendants = new HashSet<>();

        //Event children
        recordNode.getChildren().stream().filter(node -> node.getNodeType() == NodeType.EVENT)
                .forEach(
                        child -> eventChildren.add((EventRecord) desInference.getEventEvaluation().getDESRecord(child))
                );
        //UtilityChildren
        recordNode.getChildren().stream().filter(node -> node.getNodeType() == NodeType.UTILITY)
                .forEach(
                        child -> utilityChildren.add((UtilityRecord) desInference.getUtilityEvaluation().getDESRecord(child))
                );


        //Chance and Utility Descendants
        utilityDescendants.addAll(utilityChildren);
        List<Node> pending = recordNode.getChildren().stream().filter(node -> node.getNodeType() == NodeType.CHANCE).collect(Collectors.toList());

        while (!pending.isEmpty()) {
            Node chanceNode = pending.remove(0);
            chanceDescendants.add((ChanceRecord) desInference.getChanceEvaluation().getDESRecord(chanceNode));
            for (Node child : chanceNode.getChildren()) {
                //08/03/2023 - self-loop
                if (child.equals(chanceNode)) continue;
                switch (child.getNodeType()) {
                    case CHANCE:
                        if (!pending.contains(child)) {
                            pending.add(child);
                        }
                        break;
                    case UTILITY:
                        utilityDescendants.add((UtilityRecord) desInference.getUtilityEvaluation().getDESRecord(child));
                        break;
                }
            }
        }

    }


    public void setVariableValue(Configuration configuration, double clock) {
//        super.setVariableValue(configuration);
//        timeOfOccurrence =variableValue + clock;
        try {
            //24/10/2023; considering an indeterminate number of random numbers to sample the potential
//            variableValue = recordPotential.sampleConditionedVariable(desRandomProvider.getRandomNumber(), configuration);
            variableValue = recordPotential.sampleConditionedVariable(desRandomProvider.getRandomNumbers(recordPotential.numRandomNumbersNeeded()), configuration);
            timeOfOccurrence =variableValue + clock;
            variableValue = timeOfOccurrence;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public String toString() {


        String result = super.toString();
        String sEvent = getRecordNode().getName();
        String sIsTerminal = Boolean.valueOf(terminal).toString();
        String sStateWhenHappened = "";
        String sStartTTE = Double.toString(clock);

        result += sEvent + SEPARATOR +
                sIsTerminal + SEPARATOR +
                sStateWhenHappened + SEPARATOR +
                sStartTTE + SEPARATOR;

        return result;
    }


    /**
     * Is a terminal event. With this event the simulation has ended
     */
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }


    /**
     * Is initial event. With this event the simulation has ended
     */
    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public HashSet<EventRecord> getEventChildren() {
        return eventChildren;
    }


    public Collection<UtilityRecord> getUtilityDescendants() {
        return utilityDescendants;
    }

    public Collection<UtilityRecord> getUtilityChildren() {
        return utilityChildren;
    }

    /**
     * Event descendants of NODETYPE= CHANCE
     */
    public HashSet<ChanceRecord> getChanceDescendants() {
        return chanceDescendants;
    }

    /**
     * True if it has an event parent
     */
    public boolean isEventOrphan() {
        return isEventOrphan;
    }

    /**
     * Time of occurrence of the event; TTE + clock
     */
    public double getTimeOfOccurrence() {
        return timeOfOccurrence;
    }

    public void setTimeOfOccurrence(double timeOfOccurrence) {
        this.timeOfOccurrence = timeOfOccurrence;
    }



}
