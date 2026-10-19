package org.openmarkov.io.probmodel.exception;

import org.jdom2.Element;
import org.jdom2.located.LocatedElement;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;

import java.util.List;

@SuppressWarnings("serial")
public abstract sealed class PGMXParserException extends ProbNetParserException {
    
    public final Element element;
    
    public PGMXParserException(Element element) {
        this.element = element;
    }
    
    @Override @Nullable public String getExceptionMessage() {
        String exceptionMessage = super.getExceptionMessage();
        if (element instanceof LocatedElement locatedElement) {
        }
        return super.getExceptionMessage();
    }
    
    
    public static final class NoNetworkTypeFound extends PGMXParserException {
        public NoNetworkTypeFound(Element element) {
            super(element);
        }
    }
    
    public static final class UnknownNetworkType extends PGMXParserException {
        public UnknownNetworkType(String unknownType, Element element) {
            super(element);
            this.unknownType = unknownType;
        }
        
        public final String unknownType;
    }
    
    public static final class ConstraintNotFound extends PGMXParserException {
        public ConstraintNotFound(String constraintName, Element element) {
            super(element);
            this.constraintName = constraintName;
        }
        
        public final String constraintName;
    }
    
    public static final class VariableHasNoStates extends PGMXParserException {
        public VariableHasNoStates(String variableName, Element element) {
            super(element);
            this.variableName = variableName;
        }
        
        public final String variableName;
    }
    
    public static final class NotAProbModelXMLFile extends PGMXParserException {
        public NotAProbModelXMLFile(String fileName, Element root) {
            super(root);
            this.fileName = fileName;
            this.rootName = root.getName();
        }
        
        public final String fileName;
        public final String rootName;
    }
    
    public static final class UnknownDistribution extends PGMXParserException {
        public UnknownDistribution(String variableName, String distributionName, @Nullable String parametrizationName,
                                   Element element) {
            super(element);
            this.variableName = variableName;
            this.distributionName = distributionName;
            this.parametrization = parametrizationName == null ? "without a parametrization"
                    : "with the parametrization " + parametrizationName;
        }
        
        public final String variableName;
        public final String distributionName;
        /** How the file names the parametrization, as the message says it. */
        public final String parametrization;
    }
    
    public static final class UncertaintyInAnEventTable extends PGMXParserException {
        public UncertaintyInAnEventTable(String variableName, Element element) {
            super(element);
            this.variableName = variableName;
        }
        
        public final String variableName;
    }
    
    /**
     * A tree of a relation with events that is split on a variable it cannot be split on: a tree with events is
     * split on one of the event parents, and a tree with excluded events on one of the other parents.
     */
    public static final class TreeSplitOnAWrongVariable extends PGMXParserException {
        public TreeSplitOnAWrongVariable(String variableName, String topVariableName, Element element) {
            super(element);
            this.variableName = variableName;
            this.topVariableName = topVariableName;
        }
        
        public final String variableName;
        public final String topVariableName;
    }
    
    /** A time to the event, in the indicator of an event, that is not a finite number not below 0. */
    public static final class TimeToEventOutOfRange extends PGMXParserException {
        public TimeToEventOutOfRange(String variableName, String value, Element element) {
            super(element);
            this.variableName = variableName;
            this.value = value;
        }
        
        public final String variableName;
        /** The value as the file writes it. */
        public final String value;
    }
    
    /** A probability of occurrence, in the indicator of an event, that is not a number between 0 and 1. */
    public static final class ProbabilityOfOccurrenceOutOfRange extends PGMXParserException {
        public ProbabilityOfOccurrenceOutOfRange(String variableName, String value, Element element) {
            super(element);
            this.variableName = variableName;
            this.value = value;
        }
        
        public final String variableName;
        /** The value as the file writes it. */
        public final String value;
    }
    
    /** An indicator of an event whose file has no time to the event. */
    public static final class TimeToEventMissing extends PGMXParserException {
        public TimeToEventMissing(String variableName, Element element) {
            super(element);
            this.variableName = variableName;
        }
        
        public final String variableName;
    }
    
    /** An indicator of an event whose file has no probability of occurrence. */
    public static final class ProbabilityOfOccurrenceMissing extends PGMXParserException {
        public ProbabilityOfOccurrenceMissing(String variableName, Element element) {
            super(element);
            this.variableName = variableName;
        }
        
        public final String variableName;
    }
    
    /** The precision of a variable that is not a finite number not below 0, the only ones its values can be rounded to. */
    public static final class PrecisionOutOfRange extends PGMXParserException {
        public PrecisionOutOfRange(String variableName, String value, Element element) {
            super(element);
            this.variableName = variableName;
            this.value = value;
        }
        
        public final String variableName;
        /** The value as the file writes it. */
        public final String value;
    }
    
    public static final class UnknownAgent extends PGMXParserException {
        public UnknownAgent(String variableName, String agentName, Element element) {
            super(element);
            this.variableName = variableName;
            this.agentName = agentName;
        }
        
        public final String variableName;
        public final String agentName;
    }
    
    public static final class DataCouldNotBeConverted extends PGMXParserException {
        public DataCouldNotBeConverted(String reason, Element element) {
            super(element);
            this.reason = reason;
        }
        
        public final String reason;
    }
    
    public static final class EvidenceIncompatibleInFile extends PGMXParserException {
        public EvidenceIncompatibleInFile(IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther sourceException, Element element) {
            super(element);
            this.sourceException = sourceException;
        }
        
        public final IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther sourceException;
    }
    
    public static final class InvalidState extends PGMXParserException {
        public InvalidState(String variableName, String stateName, Element element) {
            super(element);
            this.variableName = variableName;
            this.stateName = stateName;
        }
        
        public final String variableName;
        public final String stateName;
    }
    
    public static final class TreeADDWithoutPotentialOrReferenfe extends PGMXParserException {
        public TreeADDWithoutPotentialOrReferenfe(Element element) {
            super(element);
        }
    }
    
    /** A branch of a tree that references a label that no branch of that tree has. */
    public static final class ReferenceWithoutLabel extends PGMXParserException {
        public ReferenceWithoutLabel(String variableName, String reference, Element element) {
            super(element);
            this.variableName = variableName;
            this.reference = reference;
        }
        
        public final String variableName;
        public final String reference;
    }
    
    public static final class TreeADDWithoutTwoThresholds extends PGMXParserException {
        public TreeADDWithoutTwoThresholds(List<Element> thresholds, Element element) {
            super(element);
            this.thresholds = thresholds;
        }
        
        public final List<Element> thresholds;
    }
    
    public static final class FoundUnknownState extends PGMXParserException {
        public FoundUnknownState(Variable variable, String stateName, Element element) {
            super(element);
            this.variable = variable;
            this.stateName = stateName;
        }
        
        public final Variable variable;
        public final String stateName;
    }
    
    public static final class PotentialTypeNotSupported extends PGMXParserException {
        public PotentialTypeNotSupported(String potentialType, Element element) {
            super(element);
            this.potentialType = potentialType;
        }
        
        public final String potentialType;
    }
    
    public static final class DeltaPotentialWithoutState extends PGMXParserException {
        public DeltaPotentialWithoutState(PotentialRole role, Element element) {
            super(element);
            this.role = role;
        }
        
        public final PotentialRole role;
    }
    
    public static final class BinomialPotentialMissingCasesAndProbabilities extends PGMXParserException {
        public BinomialPotentialMissingCasesAndProbabilities(PotentialRole role, Element element) {
            super(element);
            this.role = role;
        }
        
        public final PotentialRole role;
    }
    
    public static final class CannotAssignPotentialToStaticVariable extends PGMXParserException {
        public CannotAssignPotentialToStaticVariable(String potentialType, Element element) {
            super(element);
            this.potentialType = potentialType;
        }

        public final String potentialType;
    }

    public static final class PotentialWithoutRole extends PGMXParserException {
        public PotentialWithoutRole(Element element) {
            super(element);
        }
    }
}
