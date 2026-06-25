package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Part of the evaluation algorithm corresponding to Utility nodes
 *
 * @author cmyago
 * @version 1.5 - 25/05/2022 - separating updating and accrual
 */
public class UtilityEvaluation extends GenericEvaluation<UtilityRecord> {

    /**
     * Cumulative utility nodes
     */
    Collection<UtilityRecord> cumulativeRecords;
    /**
     * Last time the DESnet was evaluated
     */
    private double previousEvaluationTime;

//Text Log

    /**
     * @param probNet
     * @param desLogTextWriter
     * @throws OpenMarkovException
     */
    UtilityEvaluation(ProbNet probNet, List<EqualCriterion> criteria, DESLogTextWriter desLogTextWriter, DESInference desInference) throws OpenMarkovException {
        super(probNet, NodeType.UTILITY, UtilityRecord.class, desInference);
        cumulativeRecords = new ArrayList<>();
        cumulativeRecords.addAll(getDesRecordHashMap().values().stream().filter(UtilityRecord::isCumulative).collect(Collectors.toList()));
    }

    @Override
    public void startSimulation(Finding decisionFinding) {
        desRecordHashMap.values().forEach(UtilityRecord::clear);
        this.decisionFinding = decisionFinding;
        updateOrphanNodes();
        previousEvaluationTime = 0;


    }

    /**
     * Update orphan UTILITY nodes
     */
    void updateOrphanNodes() {
        orphanRecords.forEach(utilityRecord -> {
            try {
                Configuration configuration = new Configuration();
                if (utilityRecord.hasDecisionParent()) {
                    configuration.addFinding(decisionFinding);
                }
                for (Node chanceParent : utilityRecord.getParentsByType(NodeType.CHANCE)) {
                    Variable parentVariable = chanceParent.getVariable();
                    configuration.addFinding(parentVariable, desInference.getChanceEvaluation().getDESRecord(chanceParent).getVariableValue());
                }
                utilityRecord.setVariableValue(configuration);

            } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
                throw new UnrecoverableException(e);
            }

        });
    }

    /**
     * Updates the value of the variables when an event happens. Data from event happened is stored in eventRecord*
     *
     * @param eventHappened - Event happened data
     */
    @Override
    void update(EventRecord eventHappened) {
        Collection<UtilityRecord> utilityDescendants = eventHappened.getUtilityDescendants();
        utilityDescendants.forEach(utilityRecord -> {
//                utilityRecord.setVariableValue(null);
                    try {
                        Configuration configuration = new Configuration();
                        //FIXME duplicate code.
                        if (utilityRecord.hasDecisionParent()) {
                            configuration.addFinding(decisionFinding);
                        }
                        //18/03/2023 FIXME When to add an event parent?
                        if ((eventHappened != null) && (utilityRecord.getRecordNode().isParent(eventHappened.getRecordNode()))) {
                            configuration.addFinding(eventHappened.getRecordVariable(),eventHappened.getVariableValue());
                        }

                        for (Node chanceParent : utilityRecord.getParentsByType(NodeType.CHANCE)) {
                            Variable parentVariable = chanceParent.getVariable();
                            configuration.addFinding(parentVariable, desInference.getChanceEvaluation().getDESRecord(chanceParent).getVariableValue());
                        }
                        utilityRecord.setVariableValue(configuration);

                    } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
                        throw new UnrecoverableException(e);
                    }

                }

        );
        desInference.getDesLogTextWriter().logUtilityUpdate(utilityDescendants);
    }


    /**
     * Updates the value of Chance variables when an event happens. Data from event happened is stored in eventHappened*
     *
     * @param eventHappened - Event happened data
     */
    void accrueImmediateUtility(EventRecord eventHappened) {

        Collection<UtilityRecord> utilityChildren = eventHappened.getUtilityChildren();
        utilityChildren.forEach(utilityChild -> {
            try {
                Configuration configuration = new Configuration();
                if (utilityChild.hasDecisionParent()) {
                    configuration.addFinding(decisionFinding);
                }
                configuration.addFinding(eventHappened.getRecordVariable(),0);
                for (Node chanceParent : utilityChild.getParentsByType(NodeType.CHANCE)) {
                    Variable parentVariable = chanceParent.getVariable();
                    configuration.addFinding(parentVariable, desInference.getChanceEvaluation().getDESRecord(chanceParent).getVariableValue());
                }
                utilityChild.accrueImmediateUtility(eventHappened);

            } catch (OpenMarkovException e) {
                throw new UnrecoverableException(e);
            }
            desInference.getDesLogTextWriter().logImmediateUtilityAccrual(utilityChildren);
        });

    }


    /**
     * Compute utility from every cumulative utility node since it was last computed to the time the simulation ends.
     * When clock has been reached or a terminal node has happened
     * If any Chance variable has been updated by this event, an exception is raised.
     *
     * @param clock time when simulation ends.
     * @throws OpenMarkovException If any Chance variable has been updated in this algorithm iteration
     */
    void accrueCumulativeUtility(double clock) throws OpenMarkovException {
        cumulativeRecords.forEach(utilityRecord -> utilityRecord.accrueCumulativeUtility(previousEvaluationTime, clock));
        desInference.getDesLogTextWriter().logCumulativeUtilityAccrual(cumulativeRecords,previousEvaluationTime,clock);
        previousEvaluationTime = clock;
    }

    /**
     * Gets an aggregated value of each decision criteria by adding the individual values accrued in each utility node.
     * Each utility node only contributes to a single decision criteria.
     * Changed to test discounted. It will return CriteriaValues
     *
     * @return CriteriaValues object with the simulation utility value of each criteria
     * @see CriteriaValues
     * @see Criterion
     */
    public CriteriaValues computeCriteriaResults() {
        CriteriaValues criteriaResults = new CriteriaValues(desInference.getCriteria());
        for (Node utility : nodeList) {
            UtilityRecord utilityRecord = getDesRecordHashMap().get(utility);
            criteriaResults.sumValue(utilityRecord.getCriterion(), utilityRecord.getAccruedUtility(), utilityRecord.getAccruedDiscountedUtility());
        }
        return criteriaResults;
    }

}
