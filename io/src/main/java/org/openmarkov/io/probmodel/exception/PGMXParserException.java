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
}
