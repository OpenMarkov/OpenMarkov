package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Part of the evaluation algorithm corresponding to event nodes
 *
 * @author cmyago
 * @version 1.4 -16/08/2022 - time of occurrence is variableValue
 */
public class EventEvaluation extends GenericEvaluation<EventRecord> {


    //Events

    //13/03/2023 - removing the necessity of an initial event.
//    /**
//     * Initial Event Node
//     */
//    private final Node initialEvent;
    private final List<EventRecord> orphanEvents = new ArrayList<>();

    /**
     * Queue of possible events ordered by closer time of occurrence
     */
    private List<EventRecord> queue = new ArrayList<>();


    //Constructor
    EventEvaluation(ProbNet probNet, DESInference desInference) throws OpenMarkovException {
        super(probNet, NodeType.EVENT, EventRecord.class, desInference);
        //13/03/2023 - removing the necessity of an initial event.
        //Sets initial event
//        initialEvent = nodeList.stream()
//                .filter(node -> node.getPurpose().equals(PurposeType.INITIAL_EVENT.getName())).findFirst().get();
        //Set orphan list
        orphanEvents.addAll( getDesRecordHashMap().values().stream().filter(EventRecord::isEventOrphan).collect(Collectors.toList()));
        //12/03/2023 - end
    }

    /**
     * Finds the event descendants and event children for every event node
     */
    public void findDescendants() {
        getDesRecordHashMap().values().forEach(eventRecord -> eventRecord.findDescendants(desInference));
    }

    @Override
    void startSimulation(Finding decisionFinding) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        super.startSimulation(decisionFinding);
        queue = new ArrayList<>();
        //13/03/2023 - removing the necessity of an initial event.
//        queue.add(getDesRecordHashMap().get(initialEvent));
        updateOrphanEvents();
        //13/03/2023 end
    }

    /**
     * Updates the value of the variables when an event happens. Data from event happened is stored in eventRecord*
     * Adds to queue(with its timeStamp and tte computed) every Event node child of nextEvent.getNodeRecord compatible with its parent configuration
     * An event is considered compatible with its parent configuration if it has not been marked as impossible (FIXME what to do with impossibleConfiguration)
     *
     * @param eventHappened - Event happened data
     */
    @Override
    void update(EventRecord eventHappened) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {

        //Calculating TTE
        //

        // When there is a self-loop the triggered event must be updated last
        for (EventRecord eventRecord : eventHappened.getEventChildren().stream().filter(eventRecord -> !eventRecord.equals(eventHappened)).collect(Collectors.toList())) {
//FIXME deal with Configurations
            updateChildEvent(eventHappened, eventRecord);

        }
        if (eventHappened.getEventChildren().contains(eventHappened))
            updateChildEvent(eventHappened, eventHappened);
        queue.sort((EventRecord s1, EventRecord s2) -> (int) Math.signum(s1.getTimeOfOccurrence() - s2.getTimeOfOccurrence()));

        if (!eventHappened.isTerminal()) {
            desInference.getDesLogTextWriter().logScheduledEventList(queue);
        }

    }

    private void updateChildEvent(EventRecord eventHappened, EventRecord eventRecord) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Configuration configuration = new Configuration();
        try {
            //26/10/2023; the findings in the events now have its variableValue
            configuration.addFinding(eventHappened.getRecordVariable(), eventHappened.getVariableValue());
            //Compute tte
            if (eventRecord.hasDecisionParent()) {
                configuration.addFinding(decisionFinding);
            }
            for (Node chanceParent : eventRecord.getParentsByType(NodeType.CHANCE)) {
                configuration.addFinding(chanceParent.getVariable(), desInference.getChanceEvaluation().getDESRecord(chanceParent).getVariableValue());
            }

            //14/08/2022 changed use variablevalue
            eventRecord.setVariableValue(configuration, eventHappened.getTimeOfOccurrence());

//25/10/2020 The event is added to the queue depending on its behaviour
            if (!eventRecord.getRecordNode().isAlwaysAppend()) {
                String finalEventName = eventRecord.getRecordNode().getName();
                //Method equals in "org.openmarkov.core.model.network.Node" checks equality in Variable which is not overridden
                queue.removeIf(queuedEvent -> queuedEvent.getRecordNode().getName().equals(finalEventName));
            }


            if ((eventRecord.getTimeOfOccurrence()) <= desInference.getTimeHorizon()) {
                queue.add(eventRecord);
                //Log the event
                desInference.getDesLogTextWriter().logScheduledEvent(eventRecord);
            }

        } catch (OpenMarkovException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //13/03/2023 - removing the necessity of an initial event.
    /**
     * Adds orphan events to queue at clock=0;
     * FIXME -->merge with #update
     */
    void updateOrphanEvents() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        //Calculating TTE
        for (EventRecord eventRecord : orphanEvents) {
//FIXME deal with Configurations
            Configuration configuration = new Configuration();
            try {
                //Compute tte
                if (eventRecord.hasDecisionParent()) {
                    configuration.addFinding(decisionFinding);
                }
                for (Node chanceParent : eventRecord.getParentsByType(NodeType.CHANCE)) {
                    configuration.addFinding(chanceParent.getVariable(), desInference.getChanceEvaluation().getDESRecord(chanceParent).getVariableValue());
                }
                //14/08/2022 changed use variablevalue
                //26/10/2023 changed again :-). variableValue is TTE
                eventRecord.setVariableValue(configuration,0);
//25/10/2020 The event is added to the queue depending on its behaviour; FIXME check
                if (!eventRecord.getRecordNode().isAlwaysAppend()) {
                    String finalEventName = eventRecord.getRecordNode().getName();
                    //Method equals in "org.openmarkov.core.model.network.Node" checks equality in Variable which is not overridden
                    queue.removeIf(queuedEvent -> queuedEvent.getRecordNode().getName().equals(finalEventName));
                }

                if ((eventRecord.getTimeOfOccurrence()) <= desInference.getTimeHorizon()) {
                    queue.add(eventRecord);
                    //Log the event
                    desInference.getDesLogTextWriter().logScheduledEvent(eventRecord);
                }

            } catch (OpenMarkovException e) {
                e.printStackTrace();
            }

        }
        queue.sort((EventRecord s1, EventRecord s2) -> (int) Math.signum(s1.getTimeOfOccurrence() - s2.getTimeOfOccurrence()));


        desInference.getDesLogTextWriter().logScheduledEventList(queue);
    }
    //End


    /**
     * Extracts the first event from the event queue (scheduledEventList)
     *
     * @return the first event from the event queue
     */
    EventRecord getNextEvent() {
        EventRecord eventHappened = queue.remove(0);
        desInference.getDesLogTextWriter().logEvent(eventHappened);
        return eventHappened;
    }


    /**
     * Returns true if the happened event  is terminal.
     *
     * @param eventHappened
     * @return true if the happened event is terminal
     */
    boolean finalEventHappened(EventRecord eventHappened) {
        return eventHappened.isTerminal();
    }

    /**
     * Returns true if there are events in the events queue.
     *
     * @return true in the events queue (scheduledEventList) has at least one event
     */
    boolean isEventInQueue() {
        return !queue.isEmpty();
    }


    /**
     * Empties the event queue
     */
    public void emptyEventQueue() {
        queue.clear();
    }
}
