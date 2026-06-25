package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.DistributionTablePotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.inference.DES.exception.OnlyDESNetsAllowedException;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Performs DES simulation using a DESNet
 *
 * @author cmyago
 * @version 5.2 - 12/03/2023 - removed necessity of an Initial Event; now events without an event ancestor are enqueued at clock=0; Fixme self-loops?
 */
public class DESInference {
    
    /**
     * When the network has no Decision nodes
     */
    static String NODEC = "No Decision";
    /**
     * ProbNet
     */
    private ProbNet probNet;
    /**
     * Inference and log options
     */
    private MonteCarloOptions monteCarloOptions;
    
    /**
     * Manages the external data (from an input file) to carry out the simulation
     */
    private DataFromFile dataFromFile;
    
    /**
     * Decision criteria
     */
    private List<EqualCriterion> criteria;
    //Time
    private double timeHorizon;
    /**
     * References to all DESRecords in the DESnet
     */
    private final List<DESRecord> desRecords = new ArrayList<>();
    /**
     * Results for every criterion in every simulation
     */
    SimulationSummaryResults simulationSummaryResults = null;

//Results
    /**
     * List of Decision Nodes
     */
    List<Node> lDecision = null;

//Logs
    /**
     * Currently only one decision node is considered
     */
    Node decisionNode = null;

//Decision - 14/05/2020 Currently there is only one decision although the algorithm may accept several decisions in the future.
//What about simulating several decisions at the same time by sampling them??
    /**
     * Variable of the Decision Node of the model
     */
    Variable decisionVariable = null;
    //Events
    private EventEvaluation eventEvaluation = null;
    //Chance
    private ChanceEvaluation chanceEvaluation = null;
    //Utility
    private UtilityEvaluation utilityEvaluation = null;
    //FIXME merge with OM Log;
    private DESLogTextWriter desLogTextWriter;
    
    private int progress;
    
    List<TablePotential> tablePotentials = null;
    
    //Constructor
    public DESInference(ProbNet probNet, ProgressMonitor simulationProgressMonitor) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, IOException {
        long start = System.currentTimeMillis();
        try {
            if (!initialize(probNet))
                return;
        } catch (OpenMarkovException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Cannot simulate", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (monteCarloOptions.isPsa()) {
            
            ProbNet psaProbNet = probNet.copy();
            psaProbNet.getNodes(NodeType.EVENT);
            List<Node> eventNodes = psaProbNet.getNodes(NodeType.EVENT);
            
            //TablePotentials for DistributionTablePotentials
//                List<TablePotential>
            tablePotentials = psaProbNet.getPotentials()
                                        .stream()
                                        .filter(potential -> (potential instanceof DistributionTablePotential))
                                        .map(potential -> (DistributionTablePotential) potential)
                                        .map(DistributionTablePotential::getTablePotential)
                                        .collect(Collectors.toList());
            
            //TablePotentials for TransitionTablePotentials
            tablePotentials.addAll(
                    psaProbNet.getPotentials()
                              .stream()
                              .filter(potential -> (potential instanceof TransitionTablePotential))
                              .map(potential -> (TransitionTablePotential) potential)
                              .map(TransitionTablePotential::getTablePotential)
                              .collect(Collectors.toList())
            );
            
            //TablePotentials  ExactDistrPotential
            tablePotentials.addAll(
                    psaProbNet.getPotentials()
                              .stream()
                              .filter(potential -> (potential instanceof ExactDistrPotential))
                              .map(potential -> (ExactDistrPotential) potential)
                              .map(ExactDistrPotential::getTablePotential)
                              .collect(Collectors.toList())
            
            );
        }
        simulationProgressMonitor.setMaximum(monteCarloOptions.getNumSeries() * decisionVariable.getStates().length * monteCarloOptions.getNumSimulations());
        progress = 0;
        for (int series = 0; series < monteCarloOptions.getNumSeries(); series++) {
            if (monteCarloOptions.isPsa()) {
                tablePotentials.forEach(tablePotential -> samplePSATablePotential(tablePotential, ThreadLocalRandom.current()));
            }
            simulationSummaryResults.addEmptySeries();
            if (!simulateOneSeries(simulationProgressMonitor, series))
                return;
        }
        
        simulationSummaryResults.calculateStatisticalProperties();
        // Time after simulation
        double endSimulation = System.currentTimeMillis();
        desLogTextWriter.endLog();
        double elapsedTime = ((endSimulation - start) / 1000.0);
        
        //Simulation Time
        /*JOptionPane.showMessageDialog(null,
                "The simulation has ended. Simulation took " + ((endSimulation - start) / 1000) + " seconds.\n",
                "DES simulation",
                JOptionPane.INFORMATION_MESSAGE
        );*/
        //Showing results in a Window.
        if (monteCarloOptions.isPsa()) {
            CEDESDialog dialog = new CEDESDialog(probNet, simulationSummaryResults);
            showResultsWindow(elapsedTime, dialog.getJContentPane());
        } else showResultsWindow(elapsedTime, null);
        
        
    }
    
    /**
     * Sampling TablePotentials for DistributionTablePotential
     *
     * @param tablePotential
     */
    private void samplePSATablePotential(TablePotential tablePotential, Random random) {
        if (tablePotential.getUncertainValues() != null) {
            for (int i = 0; i < tablePotential.getUncertainValues().length; i++) {
                if (tablePotential.getUncertainValues()[i] != null) {
                    tablePotential.getUncertainValues()[i] = new UncertainValue(tablePotential.getUncertainValues()[i].getSample(random));
                }
            }
        }
        
    }
    
    
    //Constructor
    
    /**
     * Simulates the probNet using a DES algorithm. Takes the simulation parameters from the MonteCarloOptions field of probNet
     *
     * @param probNet network to be simulated
     */
    public DESInference(ProbNet probNet, ProgressMonitor simulationProgressMonitor, boolean psa) throws IOException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {


//      System.out.println("Calling " + DistributionTablePotential.calling);
        long start = System.currentTimeMillis();
        
        try {
            if (!initialize(probNet))
                return;
        } catch (OpenMarkovException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Cannot simulate", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Create list with the results of the criteria
        
        simulationProgressMonitor.setMaximum(monteCarloOptions.getNumSeries() * decisionVariable.getStates().length * monteCarloOptions.getNumSimulations());
        progress = 0;
        for (int series = 0; series < monteCarloOptions.getNumSeries(); series++) {
            simulationSummaryResults.addEmptySeries();
            simulationProgressMonitor.setNote(String.format("Doing series %d of %d...", series + 1, monteCarloOptions.getNumSeries()));
            if (!simulateOneSeries(simulationProgressMonitor, series))
                return;
        }
        //Parameters hardcoded
//        simulationSummaryResults.calculateStatisticalProperties(criteria.get(0), criteria.get(1), decisionVariable.getStateName(0), decisionVariable.getStateName(1));
        simulationSummaryResults.calculateStatisticalProperties();
        // Time after simulation
        double endSimulation = System.currentTimeMillis();
        desLogTextWriter.endLog();
        double elapsedTime = ((endSimulation - start) / 1000.0);
        
        //Simulation Time
        /*JOptionPane.showMessageDialog(null,
                "The simulation has ended. Simulation took " + ((endSimulation - start) / 1000) + " seconds.\n",
                "DES simulation",
                JOptionPane.INFORMATION_MESSAGE
        );*/
        //Showing results in a Window.
        
        showResultsWindow(elapsedTime, null);
        
    }
    
    /**
     * @param simulationProgressMonitor progress monitor
     * @param series
     *
     * @return true if the series has been successfully simulated
     */
    private boolean simulateOneSeries(ProgressMonitor simulationProgressMonitor, int series) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        forAllRandomProviders(DESRandomProvider::setRandomGenerator);
        dataFromFile.resetData();
        for (int individual = 0; individual < monteCarloOptions.getNumSimulations(); individual++) {
            forAllRandomProviders(DESRandomProvider::nextIndividual);
            int dataIndex = dataFromFile.nextDataIndex();
            for (State decisionState : decisionVariable.getStates()) {
                //All the interventions begin with the same index in the nodes' random sequences
                forAllRandomProviders(DESRandomProvider::resetIndex);
                desRecords.forEach(record -> record.getRecordPotential().resetSimulation());
                
                if (simulationProgressMonitor.isCanceled()) {
                    return false;
                }
                simulationProgressMonitor.setProgress(++progress);
                
                desLogTextWriter.newSimulation(series, individual, decisionVariable.getName(), decisionState.getName());
                Finding decisionFinding = new Finding(decisionVariable, decisionState);
                //The nodes in the DESnet have the random generator with the sequence established and the index of the sequence (@see DESRecord)
                CriteriaValues simulationValues = evaluateIndividual(decisionFinding, dataIndex);
                
                desLogTextWriter.logOneSimulationResults(simulationValues);
                simulationSummaryResults.addCriteriaValues(series, decisionState, individual, simulationValues);
            }
        }
        return true;
    }
    
    
    /**
     * Applies the given action to the random providers of all the DES records.
     *
     * @param action
     */
    private void forAllRandomProviders(Consumer<DESRandomProvider> action) {
        desRecords.forEach(desRecord -> action.accept(desRecord.desRandomProvider));
    }
    
    /**
     * Performs one simulation for a certain decision given by decisionFinding
     * TODO Store all results in only one CriteriaValues object
     *
     * @param decisionFinding decision for which the simulation is performed
     * @param dataIndex
     *
     * @return the simulation results
     */
    private CriteriaValues evaluateIndividual(Finding decisionFinding, int dataIndex) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
//

//Result of the simulation
        CriteriaValues criteriaResults;
//Starting time
        double clock = 0;

//Restarting simulation structure (clearing records and scheduled event list)
        //13/03/2023 - removing the necessity of an initial event.
//1. Set up the simulation.
        // It is necessary to compute orphan nodes first because they can affect the "constant" events
//1.1 Compute orphan nodes value
        chanceEvaluation.startSimulation(decisionFinding, dataIndex);
        //Queue initialised with orphanEvents (v2)
//1.2 Enqueue orphan events
        eventEvaluation.startSimulation(decisionFinding);
//1.3 Compute utility nodes value
        utilityEvaluation.startSimulation(decisionFinding);
        
        EventRecord eventHappened = null;
//2 Loop performing the simulation
        while (eventEvaluation.isEventInQueue()) {

//2.1 Get event from scheduledEventList (queue)
            eventHappened = eventEvaluation.getNextEvent();

//2.2 Advancing time
            clock = eventHappened.getTimeOfOccurrence();
//2.3 Accrue cumulative utilities
            try {
                utilityEvaluation.accrueCumulativeUtility(clock);
            } catch (OpenMarkovException e) {
                e.printStackTrace();
            }
//2.4 Update value of Chance and Utility Variables
            chanceEvaluation.update(eventHappened);
            utilityEvaluation.update(eventHappened);

//2.5 Accrue immediate utilities
            utilityEvaluation.accrueImmediateUtility(eventHappened);

//2.6 Is nextEvent terminal?
            if (eventHappened.isTerminal()) {
                eventEvaluation.emptyEventQueue();
            } else {
//                utilityEvaluation.finishUpdate(desLogTextWriter::logUtilityUpdate);
                //09/02/2025
                eventEvaluation.update(eventHappened);
            }
//2.7 Update queue; 09/02/2025; the restriction that a terminal node cannot have chidren isn't set. FIXME I'm not setting it until debug is finished; therefore this sentence is into the if

//            eventEvaluation.update(eventHappened);
        }
//3. Compute criteria results.Out of the loop because scheduledEventList if empty.
//28/08/2023; with the algorithm v2, there is no necessity of an event;
        if ((eventHappened == null) || (!eventHappened.isTerminal()))
            try {
                desLogTextWriter.logTimeHorizon(timeHorizon);
                utilityEvaluation.accrueCumulativeUtility(timeHorizon);
            } catch (OpenMarkovException e) {
                e.printStackTrace();
            }
        criteriaResults = utilityEvaluation.computeCriteriaResults();
        
        return criteriaResults;
    }
    
    /**
     * Provisionally presents the simulation results
     */
    private void showResultsWindow(double simulationTime, JPanel psaPanel) {
        boolean[] usingDiscount = new boolean[criteria.size()];
        for (int criterionNum = 0; criterionNum < criteria.size(); criterionNum++)
            usingDiscount[criterionNum] = criteria.get(criterionNum).getDiscount() > 0;
        DESResultsWindow.presentResults(
                simulationSummaryResults.getMonteCarloOptions().getNumSimulations(),
                simulationTime,
                simulationSummaryResults.toString(),
                decisionVariable.getName(),
                decisionVariable.getNumStates(),
                usingDiscount,
                psaPanel
        );
    }
    
    
    /**
     * Initialize simulations. Performed at the beginning of the set of simulations performed.
     */
    protected boolean initialize(ProbNet probNet) throws OpenMarkovException, IOException {
        
        if (!(probNet.getNetworkType() instanceof DESNetworkType)) throw new OnlyDESNetsAllowedException(probNet);
        
        this.probNet = probNet;
        //Inference options for montecarlo
        monteCarloOptions = probNet.getInferenceOptions().getMonteCarloOptions();
        //Time
        this.timeHorizon = probNet.getInferenceOptions().getTemporalOptions().getHorizon();
        
        //26/08/2023 - Data from file
        
        dataFromFile = new DataFromFile(monteCarloOptions.getInputFilePath());
        
        
        //Decision criteria
        this.criteria = new ArrayList<>();
        //FIXME Currently cannot perform evaluation if there is not at least one decision Criteria
        if (probNet.getDecisionCriteria().size() < 2) {
            
            JOptionPane.showMessageDialog(null, "For simulating the network at least two decision criteria are needed", "Cannot simulate", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        //FIXME Currently only one decision node is supported
        if (probNet.getNodes(NodeType.DECISION).size() > 1) {
            
            JOptionPane.showMessageDialog(null, "Current implementation only supports one decision node", "Cannot simulate", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        
        for (Criterion standardCriterion : probNet.getDecisionCriteria()) {
            this.criteria.add(new EqualCriterion(standardCriterion));
        }


// Individual text log.
/*
Null Pointer thrown   UchanceSimulation.finishUpdate(desLogTextWriter::logChanceChange); UtilityEvaluation 111
and others. Always created until this problem would be solved
 */
        desLogTextWriter = new DESLogTextWriter(probNet);

//        try {
        //Chance
        chanceEvaluation = new ChanceEvaluation(probNet, this);
        //Utility
        utilityEvaluation = new UtilityEvaluation(probNet, criteria, desLogTextWriter, this);
        //Events
        eventEvaluation = new EventEvaluation(probNet, this);
        eventEvaluation.findDescendants();
        
        desRecords.addAll(chanceEvaluation.getDesRecordHashMap().values());
        desRecords.addAll(utilityEvaluation.getDesRecordHashMap().values());
        desRecords.addAll(eventEvaluation.getDesRecordHashMap().values());
/*
        } catch (OpenMarkovException e) {
            e.printStackTrace();
        }*/
        
        
        //Decision
        lDecision = probNet.getNodes(NodeType.DECISION);
        
        if (!lDecision.isEmpty()) {
            decisionNode = lDecision.get(0);
            decisionVariable = decisionNode.getVariable();
        } else {
            State noDecision = new State(NODEC);
            State[] noDecisionArray = new State[1];
            noDecisionArray[0] = noDecision;
            decisionVariable = new Variable("decisionVariable", noDecisionArray);
        }
        
        //Results
        simulationSummaryResults = new SimulationSummaryResults(probNet.getInferenceOptions(), decisionVariable, criteria);
        return true;
    }
    
    /**
     * Inference and log options
     */
    public MonteCarloOptions getMonteCarloOptions() {
        return monteCarloOptions;
    }
    
    /**
     * Decision criteria
     */
    public List<EqualCriterion> getCriteria() {
        return criteria;
    }
    
    
    /**
     * Text log to track individual simulations
     */
    public DESLogTextWriter getDesLogTextWriter() {
        return desLogTextWriter;
    }
    
    /**
     * Simulates Chance Nodes
     */
    public ChanceEvaluation getChanceEvaluation() {
        return chanceEvaluation;
    }
    
    /**
     * Simulates Utility Nodes
     */
    public UtilityEvaluation getUtilityEvaluation() {
        return utilityEvaluation;
    }
    
    public EventEvaluation getEventEvaluation() {
        return eventEvaluation;
    }
    
    public double getTimeHorizon() {
        return timeHorizon;
    }
    
    /**
     * Manages the external data (from an input file) to carry out the simulation
     */
    public DataFromFile getDataFromFile() {
        return dataFromFile;
    }
}
