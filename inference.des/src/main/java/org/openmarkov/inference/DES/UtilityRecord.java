package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.inference.DES.exception.EventIsNotParentOf;
import org.openmarkov.inference.DES.exception.NodeMustBeUtility;

/**
 * Stores data from Variables from Utility nodes. Utilities are updated when an event is triggered
 *
 * @author cmyago
 * @version 2  - 07/2022 - adapted to new version of the algorithm previous values removed
 */
public class UtilityRecord extends DESRecord {

    /**
     * True is represents an Event utility. That is a punctual utility.
     * It is an Event utility y one of its parents is an Utility Node
     */
    private final boolean cumulative;
    /**
     * Discount rate for this recordVariable
     */
    private final double discountRate;
    private EqualCriterion criterion;
    /**
     * Accrued utility along the simulation
     */
    private double accruedUtility;
    /**
     * Accrued discounted utility along the simulation
     */
    private double accruedDiscountedUtility;


    /**
     * @param utilityNode
     */
    public UtilityRecord(Node utilityNode) throws OpenMarkovException {
        super(utilityNode);
        if (utilityNode.getNodeType() != NodeType.UTILITY) throw new NodeMustBeUtility(utilityNode);
        criterion = new EqualCriterion(utilityNode.getVariable().getDecisionCriterion());
        cumulative = eventParents.isEmpty();
        discountRate = recordVariable.getDecisionCriterion().getDiscount();
    }


    /**
     * Resets the UtilityRecord
     */
    @Override
    public void clear() {
        super.clear();
        accruedUtility = 0;
        accruedDiscountedUtility = 0;
    }


    /**
     * Returns the utility gathered by recordVariable between the last time it was computed and this time
     *
     * @param eventRecord event which has caused the computation
     * @return the utility gathered by recordVariable between the last time it was computed and this time
     * @throws OpenMarkovException if the node of eventRecord is not an ancestor of recordNode
     */
    public void accrueImmediateUtility(EventRecord eventRecord) throws OpenMarkovException {
        if (eventRecord.getRecordNode().isChild(recordNode)) {
            accruedUtility += variableValue;
            accruedDiscountedUtility += instantDiscount(variableValue, eventRecord.getTimeOfOccurrence());
        } else {
            throw new EventIsNotParentOf(eventRecord.getRecordNode(),  recordNode);
        }
    }


    /**
     * Returns the utility gathered by recordVariable between the clock and the last update.
     * This is only computed if the instantaneous value given by variableValue has changed.
     *
     * @param previousEvaluationTime
     * @param clock
     * @throws OpenMarkovException if the node of eventRecord is not an ancestor of recordNode
     */
    public void accrueCumulativeUtility(double previousEvaluationTime, double clock) {
        //Cumulative Utility
        double utility = variableValue * (clock - previousEvaluationTime);
        accruedUtility += utility;
        double discountedUtility = utility;
        if (discountRate != 0) {
            discountedUtility = intervalDiscountedValue(previousEvaluationTime, clock);
        }
        accruedDiscountedUtility += discountedUtility;
    }


    /**
     * Returns the discounted between startTime and endTime with instant value
     * variableValue and the discount rate given by the node Criterion discount rate.
     * DiscountedValue = (variableValue/rate)*(exp (-rate*startTime)  - exp (-rate*endTime) )
     * TODO --> Problem with discount. I find different discount formulae in different models
     *
     * @param startTime start time
     * @param endTime   end time
     * @return the discounted between startTime and endTime with instant value variableValue and
     * the discount rate given by the node Criterion discount rate
     */
    private double intervalDiscountedValue(double startTime, double endTime) {
        //Graves2021 and Caro2015
        double usedRate = discountRate;
        // Davis2014
        double instantdrq = Math.log(1 + discountRate);
        usedRate = instantdrq;
        //
        double discountedValue = (variableValue / usedRate) * (Math.exp(-usedRate * startTime) - Math.exp(-usedRate * endTime));

        return discountedValue;
        //10/2020 Changed to compare with DSU
        // 10/2021 Used to compare with Graves2021
//        double	discountedValue = (variableValue /discountRate)*(Math.exp(-discountRate*startTime)-Math.exp(-discountRate*endTime));
//        r <- (1 + ar)^(1/365)-1
//        (exp(-r*A)-exp(-r*B)) / r
//        double r = Math.pow( (1.0 +discountRate),1.0/365.0) -1.0;
//        double a =Math.exp(-r*startTime*365);
//        double b = Math.exp(-r*endTime*365);
//        double	discountedValue = (a-b)/r;
//        discountedValue *= (instantValue/365);
        // 10/2020 Used for TSD discount
//       double instantdrq = Math.log(1+discountRate);
//        double	discountedValue = (instantValue /instantdrq)*(Math.exp(-instantdrq*startTime)-Math.exp(-instantdrq*endTime));


    }

    /**
     * Returns the discounted value of variableValue at time with the discount rate given by the node Criterion discount rate
     * Discounted_value = value/(1+discount_rate)^time
     *
     * @param variableValue
     * @param time
     * @return
     */
    private double instantDiscount(double variableValue, double time) {
        double discountRate = recordVariable.getDecisionCriterion().getDiscount();
        double discountedValue = variableValue * Math.pow(1 + discountRate, -time);
        return discountedValue;
    }


    /**
     * Decision criteria of recordNode
     */
    public EqualCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(EqualCriterion criterion) {
        this.criterion = criterion;
    }

    /**
     * Accrued utility since the beginning of the simulation
     */
    public double getAccruedUtility() {
        return accruedUtility;
    }

    /**
     * Accrued discounted utility since the beginning of the simulation
     */
    public double getAccruedDiscountedUtility() {
        return accruedDiscountedUtility;
    }

    public boolean isCumulative() {
        return cumulative;
    }


}
