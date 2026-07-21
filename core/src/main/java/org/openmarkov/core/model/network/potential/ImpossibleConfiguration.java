package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.Finding;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for implementing the management of parent configurations which cannot be possible for a node in a model
 * FIXME Throw exception when configurations does not exist??
 * FIXME Keep or not?
 * @author cmyago
 * @version 1.0 - 2019
 */
public interface ImpossibleConfiguration {


    /**
     * Sets configuration as impossible. That is, this configuration cannot exit in the model
     * @param configuration - the configuration which cannot exist in the model
     */
    public void addImpossibleConfiguration(Configuration configuration) ;

    /**
     * Sets configuration as possible. That is, this configuration can exit in the model.
     *
     * @param configuration - the configuration which now may exist in the model
     */
    public void removeImpossibleConfiguration(Configuration configuration);

    /**
     * Returns true if the potential has some impossible configuration of parents. That is a configuration which cannot be possible in the model.
     * @return true if the potential has impossible configurations
     */
    public boolean hasImpossibleConfiguration();

    /**
     * Returns true if configuration is possible or not.
     * @param configuration - the configuration to check
     * @return true if the configuration is impossible, false otherwise
     */
    public boolean isImpossibleConfiguration(Configuration configuration);

    /**
     * Returns true if the list of findings form an impossible configuration
     * Useful when having events treated internally in a TableWithEvents. Events are joined together in a TableWithEvents whereas treated as different variables in other cases
     * @param findings - set of findings
     * @return
     */
    public boolean isImpossibleConfiguration(List<Finding> findings);


    /**
     * Returns true if there is a possible configuration with finding
     * @param finding finding to check a possible configuration with it
     * @return true if there is a possible configuration with finding
     */
    public boolean hasCompatiblePossibleConfiguration(Finding finding);



    /**
     * This method returns an ArrayList with the impossible configurations of the potential. If there is not impossible configurations an empy array is returned
     * @return an ArrayList with the impossible configurations of the potential.
     */
    public ArrayList<Configuration> getImpossibleConfigurations();

    /**
     * Adds an ArrayList of impossible configurations to the potential
     * @param impossibleConfigurations the ArrayList with the set of impossible configurations
     */
    public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations);
}
