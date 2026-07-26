/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class stores an {@code ArrayList} of {@code Findings} and can
 * search them with the name.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @see Finding
 * @since OpenMarkov 1.0
 */
public class EvidenceCase implements ClassLocalizable {
    
    // Attribute
    /**
     * List of findings {@code HashMap} of key={@code Variable} and
     * value={@code Finding}.
     */
    protected final HashMap<Variable, Finding> findings;
    
    // Constructors
    
    /**
     * @param findings {@code HashMap} of key={@code Variable} and value=
     *                 {@code Finding}.
     */
    // TODO Javadoc: this is a constructor.
    public EvidenceCase(HashMap<Variable, Finding> findings) {
        this.findings = findings;
    }
    
    /**
     * Constructor
     *
     * @param findings Findings
     */
    public EvidenceCase(List<Finding> findings) {
        this.findings = new HashMap<>();
        for (Finding finding : findings) {
            try {
                this.addFinding(finding);
            } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther ignored) {
                //If finding is incompatible with other, don't add it.
            }
        }
    }
    
    public EvidenceCase() {
        findings = new HashMap<>();
    }
    
    /**
     * Copy constructor
     *
     * @param evidenceCase Evidence case
     */
    public EvidenceCase(EvidenceCase evidenceCase) {
        this.findings = evidenceCase == null ? new HashMap<>() : new HashMap<>(evidenceCase.findings);
    }
    
    // Methods
    
    /**
     * Condition: There is a finding for this variable in the evidence
     *
     * @param variable Variable
     *
     * @return The state assigned to the variable. {@code int}.
     */
    public int getState(Variable variable) {
        return getFinding(variable).getStateIndex();
    }
    
    /**
     * Condition: There is a finding for this variable in the evidence
     *
     * @param variable {@code Variable}.
     *
     * @return The value of a evidence for a continuous or hybrid variable if it
     * exists: {@code double}.
     */
    public double getNumericalValue(Variable variable) {
        return getFinding(variable).getNumericalValue();
    }
    
    /**
     * @param finding . {@code Finding}.
     */
    public void addFinding(Finding finding) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        if (!isCompatible(finding)) {
            Finding alreadyExistingFinding = findings.get(finding.getVariable());
            throw new IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther(finding, alreadyExistingFinding);
        }
        if (!findings.containsKey(finding.getVariable())) {
            findings.put(finding.getVariable(), finding);
        }
    }
    
    /**
     * @param finding . {@code Finding}.
     *
     */
    public void changeFinding(Finding finding) {
        try {
            findings.remove(finding.getVariable());
            addFinding(finding);
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
            throw new UnrecoverableException(e);
        }
    }
    
    /**
     * @param findings . {@code Collection} of {@code Finding}s.
     */
    public void addFindings(Collection<Finding> findings) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        for (Finding finding : findings) {
            addFinding(finding);
        }
    }
    
    /**
     * @param probNet      Network
     * @param variableName Variable name
     * @param stateName    {@code Finding}.
     */
    public void addFinding(ProbNet probNet, String variableName, String stateName) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Variable variable = probNet.getVariable(variableName);
        int stateIndex = variable.getStateIndex(stateName);
        addFinding(new Finding(variable, stateIndex));
    }
    
    /**
     * @param probNet      Network
     * @param variableName Variable name
     * @param value        {@code Finding}.
     */
    public void addFinding(ProbNet probNet, String variableName, double value)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Variable variable = probNet.getVariable(variableName);
        addFinding(new Finding(variable, value));
    }
    
    /**
     * @param variable {@code Variable}.
     *
     * @return Finding
     *
     */
    public @Nullable Finding removeFinding(Variable variable) {
        Finding finding = getFinding(variable);
        if (finding == null) {
            return null;
        }
        return findings.remove(finding.getVariable());
    }
    
    /**
     * Removes the finding for the variable with the given name.
     *
     * @param variableName {@code String}.
     *
     * @return the removed {@code Finding}, or {@code null} if no finding matched
     */
    public @Nullable Finding removeFinding(String variableName) {
        for (Variable variable : findings.keySet()) {
            if (variable.getName().contentEquals(variableName)) {
                return findings.remove(variable);
            }
        }
        return null;
    }
    
    /**
     * @return The set of variables associated to the set of findings in the
     * same order: {@code ArrayList} of {@code Variable}.
     */
    public List<Variable> getVariables() {
        return new ArrayList<>(findings.keySet());
    }
    
    /**
     * Condition: There is a finding for this variable in the evidence
     *
     * @param variable {@code String}.
     *
     * @return finding {@code Finding}.
     */
    public Finding getFinding(Variable variable) {
        return findings.get(variable);
    }
    
    /**
     * @return findings: {@code ArrayList} of {@code Finding}s.
     */
    public List<Finding> getFindings() {
        return new ArrayList<>(findings.values());
    }
    
    /**
     * @return findings: {@code ArrayList} of {@code Finding}s.
     */
    public Map<Variable, String> getFindingsMap() {
        return this.findings.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey,
                                 entry -> entry.getValue().getState()));
    }
    
    /**
     * Returns true if the evidence case contains a finding for this variable.
     *
     * @param variable . {@code Variable}
     *
     * @return {@code boolean}.
     */
    public boolean contains(Variable variable) {
        return findings.containsKey(variable);
    }
    
    /**
     * @param variables . {@code ArrayList} of {@code Variable}s.
     *
     * @return {@code boolean}.
     */
    public boolean existsEvidence(List<Variable> variables) {
        for (Variable variable : variables) {
            if (findings.get(variable) != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Keeps the nodes of the received {@code probNet} that has not
     * received evidence.
     *
     * @param probNet {@code ProbNet}.
     *
     * @return An {@code ArrayList} of {@code Node}s.
     */
    public List<Node> getRemainingNodes(ProbNet probNet) {
        List<Node> probNetNodes = probNet.getNodes();
        List<Node> remainingNodes = new ArrayList<>();
        for (Node node : probNetNodes) {
            if (!contains(node.getVariable())) {
                remainingNodes.add(node);
            }
        }
        return remainingNodes;
    }
    
    /**
     * @return {@code true} if there are no findings. {@code boolean}
     */
    public boolean isEmpty() {
        return findings.isEmpty();
    }
    
    /**
     * Overrides {@code toString} method. Mainly for test purposes. It
     * writes the name of the variables and the findings.
     */
    public String toString() {
        String string = "[";
        Collection<Finding> findingsCollection = findings.values();
        for (Finding finding : findingsCollection) {
            if (string.compareTo("[") != 0) {
                string = string + ", ";
            }
            string = string + finding.toString();
        }
        string = string + "]\n";
        return string;
    }
    
    /**
     * Returns this evidence extended with the findings that the deterministic potentials of
     * {@code probNet} force: if, once every parent is observed, a table leaves a single state with
     * non-zero probability, that state is certain and becomes a finding. Repeated until nothing new
     * is induced.
     *
     * <p>This evidence is left untouched; the extension is returned. It used to be applied in place,
     * under the name {@code extendEvidence}, and that mattered: the network editor hands its own
     * evidence - the one the user sees in the panel - straight to
     * {@link org.openmarkov.core.inference.tasks.TaskUtilities}, so extending in place made findings
     * the user never entered appear in front of them. Returning a new case removes that by
     * construction, rather than by every caller remembering to copy first.
     *
     * @param probNet Network whose deterministic potentials may force new findings
     * @return a new evidence case with this one's findings plus the induced ones
     */
    public EvidenceCase extendedWith(ProbNet probNet) {
        EvidenceCase extended = new EvidenceCase(this);
        extended.extendInPlace(probNet);
        return extended;
    }

    private void extendInPlace(ProbNet probNet) {
        // No test on the network type. There used to be one - it left at once unless the network was
        // a MID - and nothing recorded why: it arrived with the very first commit of the repository.
        // Inducing findings is not a MID-only idea. TablePotential implements getInducedFindings, so
        // a deterministic table in a plain Bayesian network does force a value: if, once every parent
        // is observed, only one state keeps a non-zero probability, that state is certain. Narrowing
        // that to one network type meant the two callers in TaskUtilities silently did nothing for
        // every other type, while reading as though they extended the evidence always.
        for (Potential potential : probNet.getPotentials()) {
            List<Finding> newFindings = (List<Finding>) potential.getInducedFindings(this);
            for (Finding newFinding : newFindings) {
                findings.put(newFinding.getVariable(), newFinding);
            }
        }
        Queue<Finding> pendingFindings = new LinkedList<>(findings.values());
        while (!pendingFindings.isEmpty()) {
            Finding oldFinding = pendingFindings.poll();
            Variable oldVariable = oldFinding.getVariable();
            List<Potential> potentials = probNet.getPotentials(oldVariable);
            for (Potential potential : potentials) {
                List<Finding> newFindings = (List<Finding>) potential.getInducedFindings(this);
                for (Finding newFinding : newFindings) {
                    if (!findings.containsKey(newFinding.getVariable())) {
                        findings.put(newFinding.getVariable(), newFinding);
                        pendingFindings.add(newFinding);
                    }
                }
            }
        }
        
    }
    
    /**
     * Ensures that the {@code newFinding} is not inconsistent with the
     * actual evidence.
     *
     * @param newFinding . {@code Finding}
     *
     * @return {@code boolean}
     */
    public boolean isCompatible(Finding newFinding) {
        Variable variable = newFinding.getVariable();
        Finding existingFinding = findings.get(variable);
        if (existingFinding == null) {
            return true;
        }
        VariableType variableType = variable.getVariableType();
        return switch (variableType) {
            case FINITE_STATES -> newFinding.stateIndex == existingFinding.stateIndex;
            case NUMERIC -> newFinding.numericalValue == existingFinding.numericalValue;
            case DISCRETIZED -> (newFinding.stateIndex == existingFinding.stateIndex) || (
                    newFinding.numericalValue == existingFinding.numericalValue
            );
            case EVENT -> true;
        };
    }
    
    /**
     * Creates a new evidence case with all temporal findings shifted backwards
     * by the given time difference. Non-temporal findings are copied as-is.
     *
     * @param timeDifference the number of time slices to shift backwards
     * @param probNet        the network used to resolve shifted variables
     * @return a new {@code EvidenceCase} with shifted findings
     */
    public EvidenceCase shiftEvidenceBackwards(int timeDifference, ProbNet probNet) {
        try {
            EvidenceCase shiftedEvidence = new EvidenceCase();
            for (Finding finding : findings.values()) {
                Variable findingVariable = finding.getVariable();
                // generate shifted finding
                if (findingVariable.isTemporal()) {
                    if (probNet.containsShiftedVariable(findingVariable, -timeDifference)) {
                        Variable shiftedVariable = probNet.getShiftedVariable(findingVariable, -timeDifference);
                        Finding shiftedFinding = new Finding(shiftedVariable, finding.stateIndex);
                        shiftedFinding.numericalValue = finding.numericalValue;
                        shiftedEvidence.addFinding(shiftedFinding);
                    }
                } else {
                    // add non-temporal findings
                    shiftedEvidence.addFinding(finding);
                }
            }
            return shiftedEvidence;
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
            // Unreachable code
            throw new UnreachableException("shifted finding: ", e);
        }
    }
    
    /**
     * @return The number of findings in the evidence case
     */
    public int getNumberOfFindings() {
        if (findings == null) {
            return 0;
        }
        return findings.size();
    }
    
    /**
     * Fuse this EvidenceCase with the input parameter
     *
     * @param evidenceCaseToFuse Evidence case to fuse
     * @param overwrite          if true the findings in the parameter will overwrite those in
     *                           this EvidenceCase
     */
    public void fuse(EvidenceCase evidenceCaseToFuse, boolean overwrite) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        if (evidenceCaseToFuse == null) {
            return;
        }
        for (Finding finding : evidenceCaseToFuse.getFindings()) {
            if (this.contains(finding.getVariable())) {
                if (overwrite) {
                    changeFinding(finding);
                }
            } else {
                this.addFinding(finding);
            }
        }
    }
    
}