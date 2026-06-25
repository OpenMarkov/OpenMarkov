package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.IncompatibleEvidenceException;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration of States and Events representing an configuration of Findings in a potential used in a DESNet
 * TODO rename, How describe it?
 *
 * @author cmyago
 * @version 1.3 - 24/10/2023 - removed addEventFinding; this class is to be removed; check about equals and move addFinding() to superclass
 */
public class Configuration extends EvidenceCase {
    
    
    /**
     * Creates a Configuration object whose Finding objects are in findings
     *
     * @param findings List of Finding conforming the configuration
     */
    public Configuration(List<Finding> findings) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        //Create an empty EvidenceCase
        super();
        //Add findings
        for (Finding finding : findings) {
            addFinding(finding);
        }
    }
    
    
    /**
     * Creates a Configuration object with the same Finding objects as configuration
     *
     * @param configuration object used to create the new Configuration object
     */
    public Configuration(Configuration configuration) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        this(configuration.getFindings());
    }
    
    /**
     * Creates a Configuration object whose Finding objects are in findings
     *
     * @param findings Array of Finding with the findings which conform the new Configuration
     */
    public Configuration(Finding... findings) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        this(Arrays.asList(findings));
    }
    
    
    /**
     * Creates a Configuration object with the same Finding objects as evidenceCase
     *
     * @param evidenceCase object from which the Configuration is created
     */
    public Configuration(EvidenceCase evidenceCase) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        this(evidenceCase.getFindings());
    }
    
    /**
     * Returns a List of Finding sorted alphabetically by its Variable name
     *
     * @return a List of Finding sorted alphabetically by its Variable name
     */
    private List<Finding> getSortedFindings() {
        List<Finding> sortedFindings = getFindings();
        sortedFindings.sort((f1, f2) -> f1.getVariable().getName().compareTo(f2.getVariable().getName()));
        return sortedFindings;
    }

//24/10/2023; not necessary for DESSimulable
//    /**
//     * Converts the configuration to Map<Variable, Integer> where Variable represent the Variables of the Configuratiobn
//     * and Integer represents the number of the selected State. It is only valid for Finite States Variables.
//     * This is to be used with Potential#sampleCondigionedVariable(Map<Variable, Integer>)
//     * @return this Configuraton converted to Map<Variable, Integer>
//     */
//    public Map<Variable, Integer> convertToMap() throws InvalidStateException{
//// TODO :When org.openmarkov.core.model.network.potential.Potential#sampleCondigionedVariable will be changed to sampleCondigionedVariable(Map<Variable, Double>), this method will be changed too.
//        List<Finding> findings = getFindings();
//        Map<Variable, Integer> map = new HashMap<>();
//        for (Finding finding:findings){
//
//            Variable variable =finding.getVariable();
//            if (variable.getVariableType() != VariableType.FINITE_STATES)
//                throw new InvalidStateException("Variable" + variable.getName() +"hasn't finite states type");
//            Integer integer = finding.getStateIndex();
//            map.put(variable, new Integer(integer));
//
//        }
//        return map;
//    }


//    /**
//     * Indicates whether some other object is "equal to" this one. Two Configuration objects are considered equal if they have the same Finding objects.
//     * @param obj  the reference object with which to compare
//     * @return
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if ((obj == null) || !(obj instanceof Configuration))return false;
//        Configuration objAsConfiguration =(Configuration) obj;
//        List<Finding> objAsConfigurationSortedFindings = objAsConfiguration.getSortedFindings();
//        List<Finding> thisSortedFindings = this.getSortedFindings();
//        if (thisSortedFindings.size() != objAsConfigurationSortedFindings.size()) return false;
//        for (int i = 0; i< thisSortedFindings.size(); i++){
//            if (!
//                    ( (thisSortedFindings.get(i).getVariable().getBaseName().compareTo(objAsConfigurationSortedFindings.get(i).getVariable().getBaseName()) ==0)
//                && (thisSortedFindings.get(i).getState().compareTo(objAsConfigurationSortedFindings.get(i).getState()) ==0) )
//                ) {
//                return false;
//            }
//        }
//        return true;
//    }

//    /**
//     * Adds a new finding with the event Variable variable
//     * @param variable to be added to this Configuration
//     * @throws NoFindingException exception thrown when the type of variable is not EVENT
//     */
//    public void addEventFinding(Variable variable) throws NoFindingException{
//        if (variable.getVariableType() != VariableType.EVENT) throw new NoFindingException(variable.getName() + " has no type EVENT");
//        addFinding(variable,0);
//    }
    
    /**
     * Adds the Finding given by (variable,value) to the configuration if VariableType is FINITE_STATES, NUMERIC or EVENT.
     * Otherwise nothing is done. If it is type EVENT field value is ignored
     * //26/10/2023 EVENT and NUMERIC have the same behaviour
     *
     * @param variable
     * @param value
     */
    public void addFinding(Variable variable, double value) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Finding finding = null;
            switch (variable.getVariableType()) {
                case FINITE_STATES:
                    addFinding(new Finding(variable, (int) value));
                    break;
                case NUMERIC:
                case EVENT:
                    addFinding(new Finding(variable, value));
                    break;
                default:
                    return;
            }
    }
    
    
}
