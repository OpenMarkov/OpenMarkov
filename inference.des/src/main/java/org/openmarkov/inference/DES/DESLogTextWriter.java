package org.openmarkov.inference.DES;

import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.openmarkov.core.inference.MonteCarloOptions.DESNET_RESULTS_DIRECTORY;
import static org.openmarkov.core.inference.MonteCarloOptions.TEXTUAL_LOG_DIRECTORY;

/**
 * Write a log of the Monte Carlo simulations in a text file. That is a
 * text log to track individual simulations.
 *
 * @author cmyago
 * @version 2.1 - 11/04/2022 - do nothing if textualLog is not set
 * @version 3 - 13/01/2023 - adapted to new way of working with random numbers and recoding
 */
class DESLogTextWriter {
    //Constants
    private static final String CLOCK="clock";
    private static final String UTILITYCAPS="Utility/Payoff";
    private static final String UTILITY="utility/payoff";

    /**
     * Represents US notation dut to it is the notation used
     */
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    /**
     * Numbers are printed with 3 decimals
     */
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###", symbols);
    private MonteCarloOptions monteCarloOptions = null;
    private final Variable decisionVariable = null;
    private PrintWriter printWriter;

    /**
     * Creates a new DESLogTextWriter
     *
     * @param probNet
     */
    DESLogTextWriter(ProbNet probNet) {
        monteCarloOptions = probNet.getInferenceOptions().getMonteCarloOptions();
        if (!monteCarloOptions.isTextualLog()) return;
        try {
            printWriter = new PrintWriter(createFilename(probNet.getName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs the starting of a new simulation whose number is simulationNumber in seriesNumber and the Decision variable has the value decisionValue
     *
     * @param seriesNumber     - series number
     * @param simulationNumber - simulation number
     * @param decisionValue    - value of the decision variable
     */
    public void newSimulation(int seriesNumber, int simulationNumber, String decisionName,String decisionValue) {
        if (!monteCarloOptions.isTextualLog()) return;
        printWriter.println("");
        printWriter.println("Series: " + (seriesNumber + 1) + ". Simulation: " + (simulationNumber + 1) + ". " + decisionName +": " + decisionValue);
        printWriter.println("================================================");
    }

    /**
     * Logs the list of scheduled events (the "resulting queue")
     *
     * @param scheduledEventList - list of scheduled events
     */
    public void logScheduledEventList(List<EventRecord> scheduledEventList) {
        if (!monteCarloOptions.isTextualLog()) return;
        printWriter.println();
        String empty = scheduledEventList.isEmpty()?" empty":"";
        printWriter.println("Resulting queue:"+empty);
        for (EventRecord eventRecord : scheduledEventList) {
            printWriter.println("\tEvent scheduled: " + eventRecord.getRecordNode().getName());
            printWriter.println("\t\tTime to occur: " + decimalFormat.format(eventRecord.getTimeOfOccurrence()));
        }
    }


    /**
     * Logs the scheduling of an event
     *
     * @param eventRecord scheduled event to be logged
     */
    public void logScheduledEvent(EventRecord eventRecord) {
        if (!monteCarloOptions.isTextualLog()) return;
        printWriter.println("\tEvent scheduled: " + eventRecord.getRecordNode().getName());
        printWriter.println("\t\tTime to occur: " + decimalFormat.format(eventRecord.getTimeOfOccurrence()));
    }

    /**
     * Log a happened event
     *
     * @param eventRecord - event data
     */
    public void logEvent(EventRecord eventRecord) {
        if (!monteCarloOptions.isTextualLog()) return;

        String eventName = eventRecord.getRecordNode().getName();
        if (eventRecord.isTerminal()) {
            eventName += " -> terminal event;";
        } else {
            eventName += ";";
        }
        printWriter.println();
        printWriter.println("Event triggered: " + eventName + " "+ CLOCK + ": " + decimalFormat.format(eventRecord.getTimeOfOccurrence()));

    }

    /**
     * Logs a change in the variable stored in chanceRecord
     *
     * @param chanceRecords - contains the variable and the logging data
     */
    public void logChanceChange(Collection<ChanceRecord> chanceRecords) {
        if (!monteCarloOptions.isTextualLog()) return;
        if (chanceRecords.isEmpty()) return;
        printWriter.println("\tUpdate chance variables");

        chanceRecords.forEach(chanceRecord -> {
        String variableValue="";
        if (chanceRecord.getRecordVariable().getVariableType() == VariableType.FINITE_STATES){
            variableValue= chanceRecord.getRecordVariable().getStateName((int)chanceRecord.getVariableValue());
        } else {
            variableValue+= chanceRecord.getVariableValue();
        }
            printWriter.println("\t\t"+chanceRecord.getRecordVariable().getName()+ ": " + variableValue);
        });

//        printWriter.println("\t\tChance Variable affected: " + chanceRecord.getRecordVariable().getName());
//        if (chanceRecord.getRecordVariable().getVariableType() == VariableType.FINITE_STATES){
//            printWriter.println("\t\tOld value: " +chanceRecord.getPreviousStateValue());
//        } else {
//            printWriter.println("\t\tOld value: " + chanceRecord.getPreviousVariableValue());
//        }
//        printWriter.println("\t\t\tsince: "+decimalFormat.format(chanceRecord.getPreviousChangeClock()));
//        printWriter.println("\t\t\ttime spent in value: " + decimalFormat.format(chanceRecord.getClock()-chanceRecord.getPreviousChangeClock()));

    }


    /**
     * Logs a change in the variable stored in utilityRecord
     *
     * @param utilityRecords - updated utilities to be logged
     */
    public void logUtilityUpdate(Collection<UtilityRecord> utilityRecords) {
        if (!monteCarloOptions.isTextualLog()) return;
        if (utilityRecords.isEmpty()) return;
        printWriter.println("\tUpdate instantaneous value of utilities/payoffs");
        utilityRecords.forEach(utilityRecord -> {
                String utilityType = utilityRecord.isCumulative()?" (cumulative)":" (immediate)";
                printWriter.println("\t\t"+utilityRecord.getRecordVariable().getName() + utilityType + ": " + utilityRecord.getVariableValue());
        });

    }

    /**
     * Logs a change in the variable stored in utilityRecord
     *
     * @param utilityRecords - contains the variable and the logging data
     */
    public void logImmediateUtilityAccrual(Collection<UtilityRecord> utilityRecords) {
        if (!monteCarloOptions.isTextualLog()) return;
        if (utilityRecords.isEmpty()) return;
        printWriter.println("\tAccrue immediate utilities/payoffs");
        utilityRecords.forEach(utilityRecord -> {
            printWriter.println("\t\t"+utilityRecord.getRecordVariable().getName());
            printWriter.println("\t\t\tInstantaneous value: " + utilityRecord.getVariableValue());
            printWriter.println("\t\t\tValue accrued (total): "+decimalFormat.format(utilityRecord.getAccruedUtility()) );
            printWriter.println("\t\t\tDiscounted accrued value (total): "+decimalFormat.format(utilityRecord.getAccruedDiscountedUtility()) );
        });
    }
    public void logCumulativeUtilityAccrual(Collection<UtilityRecord> utilityRecords, double previousEvaluationTime, double clock){
        if (!monteCarloOptions.isTextualLog()) return;
        if (utilityRecords.isEmpty()) return;
        //FIXME Careful comparing two doubles; here we are comparing when previousEvaluationTime has been asigned from clock
        if (clock == previousEvaluationTime) return;
        printWriter.println("\tAccrue cumulative utilities/payoffs from "+CLOCK+"="+decimalFormat.format(previousEvaluationTime)
                + " to "+ CLOCK +"="+decimalFormat.format(clock) +" (interval length "+ decimalFormat.format(clock - previousEvaluationTime)+")");
        utilityRecords.forEach(utilityRecord -> {
            printWriter.println("\t\t"+utilityRecord.getRecordVariable().getName());
            printWriter.println("\t\t\tInstantaneous value in the interval: " + utilityRecord.getVariableValue());
            printWriter.println("\t\t\tValue accrued (total): "+decimalFormat.format(utilityRecord.getAccruedUtility()) );
            printWriter.println("\t\t\tDiscounted accrued value (total): "+decimalFormat.format(utilityRecord.getAccruedDiscountedUtility()) );
        });

    }



    /**
     * Ends textual log
     */
    public void endLog() {
        if (!monteCarloOptions.isTextualLog()) return;
        printWriter.close();
    }


    /**
     * Creates a filename for the textual log in path
     *
     * @param probNetName
     * @return a textual log filename
     */
    private File createFilename(String probNetName) {

        String filename = probNetName.substring(0, probNetName.indexOf('.'));
        filename += "-";
        filename += new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
        filename += ".log";

        File resultsDirectory = new File(DESNET_RESULTS_DIRECTORY);
        if (!resultsDirectory.exists()) {
            resultsDirectory.mkdir();
        }
        File logDirectory = new File(resultsDirectory, TEXTUAL_LOG_DIRECTORY);
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }

        return new File(logDirectory, filename);
    }

    /**
     * Logs the time horizon of the simulation
     *
     * @param timeHorizon time horizon of the simulation
     */
    public void logTimeHorizon(double timeHorizon) {
        if (!monteCarloOptions.isTextualLog()) return;
        printWriter.println("Time horizon: " + timeHorizon);
    }

    /**
     * Logs a summary of the simulation
     *
     * @param criteriaResults simulation data
     */
    public void logOneSimulationResults(CriteriaValues criteriaResults) {
        if (!monteCarloOptions.isTextualLog()) return;
        printWriter.println("Summary");
//        printWriter.println(criteriaResults.toString());

    }


}
