package org.openmarkov.inference.DES;

import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.EqualCriterion;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * This class associates a set of criteria with its simulation results
 *
 * @version 2.1 13/11/2020 added discounted values
 * @autor cmyago
 */
public class CriteriaValues {
    /**
     * TreeMap where the key is the Criterion and the value its simulationResults
     */
    private HashMap<EqualCriterion, SimulationResults> criteriaValuesHashMap;
    /**
     * Set of criteria
     */
    private final List<EqualCriterion> criteria;

    CriteriaValues(List<EqualCriterion> criteria) {
        this(criteria, 1);
    }

    /**
     * Creates a CriteriaValues with the set of criteria given by criteria and any criteria has numberOfValues values of simulation
     *
     * @param criteria
     * @param numberOfValues
     */
    CriteriaValues(List<EqualCriterion> criteria, int numberOfValues) {
        this.criteria = criteria;
        criteriaValuesHashMap = new HashMap<EqualCriterion, SimulationResults>();
        for (EqualCriterion criterion : criteria) {
            criteriaValuesHashMap.put(criterion, new SimulationResults(numberOfValues));
        }
    }

    /**
     * Sets value in position for criterion
     *
     * @param criterion
     * @param position
     * @param value
     * @param discountedValue
     */
    public void setValue(EqualCriterion criterion, int position, double value, double discountedValue) {
        SimulationResults simulationValues = criteriaValuesHashMap.get(criterion);
        simulationValues.setValue(position, value, discountedValue);
    }

    /**
     * Sets value for criterion in position=0
     *
     * @param criterion
     * @param value
     * @param discountedValue
     */
    public void setValue(EqualCriterion criterion, double value, double discountedValue) {
        setValue(criterion, 0, value, discountedValue);
    }


    /**
     * Sums value to the value of criterion stored in position
     *
     * @param criterion
     * @param position
     * @param value
     * @param discountedValue
     */
    public void sumValue(EqualCriterion criterion, int position, double value, double discountedValue) {
        SimulationResults simulationValues = criteriaValuesHashMap.get(criterion);
        simulationValues.sumValue(position, value, discountedValue);
    }


    /**
     * Sums value to the value of criterion stored in position
     *
     * @param criterion
     * @param value
     * @param discountedValue
     */
    public void sumValue(EqualCriterion criterion, double value, double discountedValue) {
        sumValue(criterion, 0, value, discountedValue);
    }


    /**
     * This method gets the value for criterion in position
     *
     * @param criterion
     * @param position
     */
    public double getValue(EqualCriterion criterion, int position) {
        SimulationResults simulationValues = criteriaValuesHashMap.get(criterion);
        double[] valueArray = simulationValues.getValues();
        return (valueArray[position]);
    }


    /**
     * This method gets  value for criterion in position=0
     *
     * @param criterion
     */
    public double getValue(EqualCriterion criterion) {
        return getValue(criterion, 0);
    }


    /**
     * This method sets in the position simulationNumber and correspondent criterion the values in criteriaToAdd
     *
     * @param criteriaToAdd
     * @param simulationNumber
     */
    public void setValuesForCriteria(CriteriaValues criteriaToAdd, int simulationNumber) {
        for (EqualCriterion criterionToAdd : criteriaToAdd.getCriteria()) {
            SimulationResults simulationResults = criteriaValuesHashMap.get(criterionToAdd);
            SimulationResults simulationResultsToAdd = criteriaToAdd.getCriteriaValuesHashMap().get(criterionToAdd);
            simulationResults.setValue(simulationNumber, simulationResultsToAdd.getValue(), simulationResultsToAdd.getDiscountedValue());
        }
    }

//    /**
//     * This method add the value of every criterion of criteriaValues the value of every criterion of this CriteriaValues
//     * It is intended to CriteriaValues with only one value
//     * @param criteriaValues - criteriaValues with only one value per criterion
//     */
//    public void sumValues(CriteriaValues criteriaValues) {
//
//        for (EqualCriterion criterion:criteriaValues.getCriteria()){
//            SimulationResultsOD simulationResults = criteriaValuesHashMap.get(criterion);
//            SimulationResultsOD simulationResultsToAdd =criteriaValues.getSimulationResults(criterion);
//            double valueToAdd = simulationResultsToAdd.getValue();
//            double discountedValueToAdd =
//            simulationResults.sumValue(valueToAdd, ) ;
//        }
//
//    }

    /**
     * True if criterion is in CriteriaValues
     *
     * @param criterion
     * @return
     */
    public boolean contains(EqualCriterion criterion) {
        return criteriaValuesHashMap.containsKey(criterion);
    }


    public List<EqualCriterion> getCriteria() {
        return criteria;
    }


    /**
     * Returns the simulationResults assotiated to criterion
     *
     * @param criterion
     * @return
     */
    public SimulationResults getSimulationResults(EqualCriterion criterion) {
        return criteriaValuesHashMap.get(criterion);
    }


    public HashMap<EqualCriterion, SimulationResults> getCriteriaValuesHashMap() {
        return criteriaValuesHashMap;
    }

    public void setCriteriaValuesHashMap(HashMap<EqualCriterion, SimulationResults> criteriaValuesHashMap) {
        this.criteriaValuesHashMap = criteriaValuesHashMap;
    }

    /**
     * @param monteCarloOptions
     */
    public void calculateStatisticalProperties(MonteCarloOptions monteCarloOptions) {
        Collection<SimulationResults> simulationResultsCollection = criteriaValuesHashMap.values();
        for (SimulationResults simulationResult : simulationResultsCollection) {
            simulationResult.calculateStatisticalProperties(monteCarloOptions);
        }
    }


    @Override
    public String toString() {
//        String result = "";
//        for (EqualCriterion key: criteriaValuesHashMap.keySet()){
//                result+= key.getCriterionName() +": "+  criteriaValuesHashMap.get(key).toString() + "\n";
//        }
//        return result;


        String result = "";
        for (EqualCriterion key : criteriaValuesHashMap.keySet()) {
            result += key.getCriterionName() + ";" + criteriaValuesHashMap.get(key).toString() + ";";
        }
        return result;
    }


}
