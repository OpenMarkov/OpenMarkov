package org.openmarkov.inference.DES;

import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.*;

/**
 * Stores the simulation results for every series of simulations. It keeps individual and aggregated results.
 *
 * @author cmyago
 * @version 2 03/01/2022
 */
public class SimulationSummaryResults {

    private final InferenceOptions inferenceOptions;
    private final MonteCarloOptions monteCarloOptions;

    private final List<EqualCriterion> criteria;
    private final Variable decisionVariable;


    /**
     * List with the results of every simulation series: List<HashMap<Intervention, Simulation results per decision criteria>>
     * TODO --> Consider not to keep all the series until simulation ends but writing to disk the results when one series finishes and remove data to save RAM. We have tried to use arrays to save memory but it does not fulfill simulation memory requirements
     */
    private final List<HashMap<String, CriteriaValues>> seriesList;
    /**
     * Array with the ICER for every series
     */
    private final double[] icerSeries;
    /**
     * Array with the discounted ICER for every series
     */
    private final double[] discountedIcerSeries;


    /**
     * Creates a new SimulationSummaryResults object according to inferenceOptions, decisionVariable and criteria
     *
     * @param inferenceOptions - values to perform the inference
     * @param decisionVariable - variable with the different health interventions
     * @param criteria         - list of decision criteria
     */
    SimulationSummaryResults(InferenceOptions inferenceOptions, Variable decisionVariable, List<EqualCriterion> criteria) {
        this.inferenceOptions = inferenceOptions;
        this.monteCarloOptions = inferenceOptions.getMonteCarloOptions();
        this.decisionVariable = decisionVariable;

        seriesList = new ArrayList();
        icerSeries = new double[monteCarloOptions.getNumSeries()];
        discountedIcerSeries = new double[monteCarloOptions.getNumSeries()];
        this.criteria = criteria;

    }

    /**
     * Adds a new series structure to the list of series. Afterwards it will be populated with the simulation values
     */
    public void addEmptySeries() {
        HashMap<String, CriteriaValues> seriesData = new HashMap<>();
        for (State decisionState : decisionVariable.getStates()) {
            CriteriaValues criteriaValues = new CriteriaValues(criteria, monteCarloOptions.getNumSimulations());
            seriesData.put(decisionState.getName(), criteriaValues);
        }
        seriesList.add(seriesData);
    }


    /**
     * Adds the results of a single simulation for all the criteriaToAdd given by [seriesNumber, decisionState, simulationNumber] )
     *
     * @param seriesNumber     - number of series for which data are stored
     * @param decisionState    - decision interventio
     * @param simulationNumber
     * @param criteriaToAdd
     */
    public void addCriteriaValues(int seriesNumber, State decisionState, int simulationNumber, CriteriaValues criteriaToAdd) {
        CriteriaValues criteriaValues = seriesList.get(seriesNumber).get(decisionState.getName());
        criteriaValues.setValuesForCriteria(criteriaToAdd, simulationNumber);
    }

    /**
     * Calculates the statistical properties for every decision in every series based on the inference options. It also computes the ICER.
     * ICER = (cost_intervention2 - cost_intervention1)/(effectiveness_intervention2 - effectiveness_intervention1)
     *
     */
    public void calculateStatisticalProperties() {
        for (HashMap<String, CriteriaValues> series : seriesList) {
            //HashMap<String, CriteriaValues>-->HashMap<intervention, CriteriaValues>
            Collection<CriteriaValues> criteriaValuesCollection = series.values();
            //<CriteriaValues>
            for (CriteriaValues criteriaValues : criteriaValuesCollection) {
                criteriaValues.calculateStatisticalProperties(monteCarloOptions);
            }
//            calculateICER(cost, effectiveness, intervention1, intervention2);
        }

    }

    /**
     * Calculates the statistical properties for every decision in every series based on the inference options. It also computes the ICER.
     * ICER = (cost_intervention2 - cost_intervention1)/(effectiveness_intervention2 - effectiveness_intervention1)
     *
     * @param cost          decision criteria for cost when computing icer
     * @param effectiveness decision criteria for cost when computing icer
     * @param intervention1 decision intervention for control
     * @param intervention2 decision intervention for treatment
     */
    public void calculateStatisticalProperties(EqualCriterion cost, EqualCriterion effectiveness, String intervention1, String intervention2) {
        for (HashMap<String, CriteriaValues> series : seriesList) {
            //HashMap<String, CriteriaValues>-->HashMap<intervention, CriteriaValues>
            Collection<CriteriaValues> criteriaValuesCollection = series.values();
            //<CriteriaValues>
            for (CriteriaValues criteriaValues : criteriaValuesCollection) {
                criteriaValues.calculateStatisticalProperties(monteCarloOptions);
            }
            calculateICER(cost, effectiveness, intervention1, intervention2);
        }

    }




    public List<EqualCriterion> getCriteria() {
        return criteria;
    }


    public Variable getDecisionVariable() {
        return decisionVariable;
    }


    /**
     * List with one element per series. Each series is a treeMap storing the results for each decision Value.
     * Each of these results is CriteriaValues with the results of the criterion for each simulation of the series.
     */
    public List<HashMap<String, CriteriaValues>> getSeriesList() {
        return seriesList;
    }


    public MonteCarloOptions getMonteCarloOptions() {
        return monteCarloOptions;
    }

    /**
     * Calculates the ICER for decision interventions intervention1 and intervention2 for every series of simulations with cost and effectiveness given by the criteria cost and effectiveness.
     * ICER = (cost_intervention2 - cost_intervention1)/(effectiveness_intervention2 - effectiveness_intervention1)
     *
     * @param cost          decision criteria for cost when computing icer
     * @param effectiveness decision criteria for cost when computing icer
     * @param intervention1 decision intervention for control
     * @param intervention2 decision intervention for treatment
     */
    private void calculateICER(EqualCriterion cost, EqualCriterion effectiveness, String intervention1, String intervention2) {

        for (int i = 0; i < monteCarloOptions.getNumSeries(); i++) {
            HashMap<String, CriteriaValues> seriesResult = seriesList.get(i);
            CriteriaValues resultsIntervention1 = seriesResult.get(intervention1);
            CriteriaValues resultsIntervention2 = seriesResult.get(intervention2);
            double costIntervention1 = resultsIntervention1.getSimulationResults(cost).getMean();
            double costIntervention2 = resultsIntervention2.getSimulationResults(cost).getMean();
            double effIntervention1 = resultsIntervention1.getSimulationResults(effectiveness).getMean();
            double effIntervention2 = resultsIntervention2.getSimulationResults(effectiveness).getMean();
            icerSeries[i] = (costIntervention2 - costIntervention1) / (effIntervention2 - effIntervention1);
            double discountedCostIntervention1 = resultsIntervention1.getSimulationResults(cost).getDiscountedMean();
            double discountedCostIntervention2 = resultsIntervention2.getSimulationResults(cost).getDiscountedMean();
            double discountedEffIntervention1 = resultsIntervention1.getSimulationResults(effectiveness).getDiscountedMean();
            double discountedEIntervention2 = resultsIntervention2.getSimulationResults(effectiveness).getDiscountedMean();
            discountedIcerSeries[i] = (discountedCostIntervention2 - discountedCostIntervention1) / (discountedEIntervention2 - discountedEffIntervention1);


        }

    }


    @Override
    public String toString() {
//        String result = "";
//        int i=0;
//        for (HashMap<String, CriteriaValues> series: seriesList){
//            result += "SERIES " + i + "\n";
//            for (Map.Entry<String, CriteriaValues> entry:series.entrySet())
//                result += entry.getKey() + ":\n"+ entry.getValue() ;
//            result += "ICER: " + icerSeries[i] + ". Discounted ICER: " + discountedIcerSeries[i++];
//            result += "\n\n";
//        }

        String result = "";
        int i = 0;
        for (HashMap<String, CriteriaValues> series : seriesList) {
            result += "SERIES;" + i + ";";
            for (Map.Entry<String, CriteriaValues> entry : series.entrySet())
                result += entry.getKey() + ";" + entry.getValue() + ";";
            result += "ICER;" + icerSeries[i] + ";Discounted ICER;" + discountedIcerSeries[i++];
            result += "\n";
        }


        return result;
    }
}
