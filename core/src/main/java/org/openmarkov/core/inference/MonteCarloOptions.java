package org.openmarkov.core.inference;

import java.nio.file.Path;

/**
 * This class contains the parameter for doing Monte Carlo simulations.
 * There are three types or parameters: Those which establish how the simulation is carried out and what is calculated,
 * those which determine the simulation log and the input files
 *
 * @author cmyago
 * 04/10/2023 FIXME Merge with inference options
 * @version 1.3 21/02/2020 -changed textual log options having only one check for "detailedTextualLog"
 */
public class MonteCarloOptions implements Cloneable {

/*
 04/10/2023 FIXME Merge with inference options
 26/08/2023 FIXME Transform into register
 26/08/2023 FIXME why Cloneable interface?
*/

    //Simulation Options
    private int numSimulations = 1;
    private int numSeries = 1;

    //PSA Options
    /**
     * True if we are conducting a PSA analysis
     */
    private boolean psa = false;

    //Log options
    /**
     *
     */
    public static final String DESNET_RESULTS_DIRECTORY = "DESNetFiles";
    public static final String TEXTUAL_LOG_DIRECTORY = "TextualLog";
    public static final String EXPORT_DIRECTORY = "Export";


    /**
     * When true, a text log with any event and the assotiated changes is created
     */

    private boolean textualLog = false;


    /**
     * Input file with values for the variables
     */
    private Path inputFilePath = null;


    /**
     * Creates a new monteCarloOptions object
     */
    public MonteCarloOptions() {
    }

    /**
     * Creates a new monteCarloOptions object with the values of montecarloOptions
     *
     * @param monteCarloOptions
     */
    public MonteCarloOptions(MonteCarloOptions monteCarloOptions) {
        this.setNumSimulations(monteCarloOptions.numSimulations);
        this.setNumSeries(monteCarloOptions.numSeries);
        this.inputFilePath = monteCarloOptions.inputFilePath;
        this.psa = monteCarloOptions.isPsa();
        this.textualLog = monteCarloOptions.isTextualLog();
    }

    /**
     * This method returns the number of simulations per serie
     *
     * @return the number of simulations per serie
     */
    public int getNumSimulations() {
        return numSimulations;
    }

    /**
     * This method sets the number of simulations per serie
     */
    public void setNumSimulations(int numSimulations) {
        this.numSimulations = numSimulations;
    }


    /**
     * This method returns the number of series of simulations
     *
     * @returnthe number of series of simulations
     */
    public int getNumSeries() {
        return numSeries;
    }

    /**
     * This method sets the number of series of simulations
     */
    public void setNumSeries(int numSeries) {
        this.numSeries = numSeries;
    }


    /**
     * When true, a text log with any event and the associated changes is created
     */
    public boolean isTextualLog() {
//		return false;
        return textualLog;
    }

    /**
     * Sets the detailedTextLog option. When true the detailed text log is created.
     *
     * @param textualLog - true if the detailed text log is created, false otherwise
     */
    public void setTextualLog(boolean textualLog) {
        this.textualLog = textualLog;
    }


    //Input File Options

    public void setInputFilePath(Path inputFilePath) {
        this.inputFilePath = inputFilePath;
    }




    /**
     * Name of the input file with values for the variables
     */ /**
     * This method returns the name of the file with the input values for simulation
     *
     * @return the name of the file with the input values for simulation
     */
    public Path getInputFilePath() {
        return inputFilePath;
    }



    /*
     * This method clones this monteCarloOptions object
     * @return a clon of this monteCarloOptions object
     */
    public MonteCarloOptions clone() {
        return new MonteCarloOptions(this);
    }

    public void setPSA(boolean selected) {
        psa = selected;
    }

    /**
     *
     * @return  true if we are conducting a PSA analysis, false otherwise
     */

    public boolean isPsa() {
        return psa;
    }

    /**
     * Sets psa
     * @param psa indicates whether to carry out PSA or not
     */
    public void setPsa(boolean psa) {
        this.psa = psa;
    }
}
