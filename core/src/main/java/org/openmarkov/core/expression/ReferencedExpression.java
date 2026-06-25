package org.openmarkov.core.expression;

import net.sourceforge.jeval.EvaluationException;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.java.regexUtils.SplitByRegex;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The difference between a normal expression (A {@link String}) and a ReferencedExpression is that the latter
 * holds references to the variables that are used in the expression.
 */
public class ReferencedExpression<T> {
    private static final Pattern EXTRACT_VARIABLE_PATTERN = Pattern.compile("\\{([^\\{]+?)\\}");
    private final List<T> references;
    private final Function<T, String> stringifyReference;
    private final List<? extends ExpressionContent<T>> contents;
    private final Function<T, @Nullable String> resolveToJEval;
    
    public ReferencedExpression(Map<String, T> references, String expression, Function<T, String> stringifyReference, Function<T, @Nullable String> resolveToJEval) {
        this.references = new ArrayList<>();
        this.stringifyReference = stringifyReference;
        this.resolveToJEval = resolveToJEval;
        var allRanges = SplitByRegex.splitAll(EXTRACT_VARIABLE_PATTERN, expression);
        this.contents = allRanges
                .stream()
                .map(subString -> {
                    Matcher matcher = EXTRACT_VARIABLE_PATTERN.matcher(subString);
                    if (!matcher.find()) {
                        return new ExpressionContent.UnparsedExpression<T>(subString);
                    }
                    if (!references.containsKey(matcher.group(1))) {
                        return new ExpressionContent.UnparsedExpression<T>(subString);
                    }
                    return new ExpressionContent.VariableReference<T>(references.get(matcher.group(1)));
                }).toList();
    }
    
    public final String asStringExpression() {
        return contents.stream()
            .map(content -> (ExpressionContent<T>) content) // Forzamos el tipo aquí
            .map(expressionContent -> switch (expressionContent) {
                case ExpressionContent.VariableReference<T> vr -> 
                        "{" + this.stringifyReference.apply(vr.reference()) + "}";
                case ExpressionContent.UnparsedExpression<T> ue -> 
                        ue.unparsed();
            }).collect(Collectors.joining());
    }
    
    public final String processedExpression(Map<T, String> variablesValues) throws NonProjectablePotentialException.CannotResolveVariable {
        var variableResolver = new VariableResolver<T>(this, variablesValues);
        StringBuilder resolved = new StringBuilder();
        for (ExpressionContent<T> expressionContent : this.contents) {
            resolved.append(expressionContent.resolve(variableResolver));
        }
        return resolved.toString();
    }
    
    public List<T> references() {
        List<T> refs = new ArrayList<>();
        for (ExpressionContent<T> content : contents) {
            if (content instanceof ExpressionContent.VariableReference<T> vr) {
                refs.add(vr.reference());
            }
        }
        return refs;
    }
    
    sealed interface ExpressionContent<T> {
        record VariableReference<T>(T reference) implements ExpressionContent<T> {
            @Override public String resolve(VariableResolver<T> variableResolver) throws NonProjectablePotentialException.CannotResolveVariable {
                if (variableResolver.constantsFromValue.containsKey(reference)) {
                    return variableResolver.constantsFromValue.get(reference);
                }
                String variableName = variableResolver.referencedExpression.stringifyReference.apply(reference);
                if (variableResolver.constantsFromName.containsKey(variableName)) {
                    return variableResolver.constantsFromName.get(variableName);
                }
                String inlineResolve = variableResolver.referencedExpression.resolveToJEval.apply(reference);
                if (inlineResolve != null) {
                    return inlineResolve;
                }
                throw new NonProjectablePotentialException.CannotResolveVariable(variableName);
            }
        }
        
        record UnparsedExpression<T>(String unparsed) implements ExpressionContent<T> {
            @Override public String resolve(VariableResolver<T> variableResolver) {
                return unparsed;
            }
        }
        
        String resolve(VariableResolver<T> variableResolver) throws NonProjectablePotentialException.CannotResolveVariable;
        
    }
    
    @Override public String toString() {
        return this.asStringExpression();
    }
    
    public String evaluateWith(Map<T, String> variablesValues) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        String processedExpression = this.processedExpression(variablesValues);
        try {
            return new net.sourceforge.jeval.Evaluator().evaluate(processedExpression);
        } catch (EvaluationException e) {
            throw new NonProjectablePotentialException.CannotEvaluate(processedExpression, e);
        }
    }
    
    record VariableResolver<T>(ReferencedExpression<T> referencedExpression, Map<T, String> constantsFromValue,
                               Map<String, String> constantsFromName) {
        public VariableResolver(ReferencedExpression<T> referencedExpression, Map<T, String> constantsFromValue) {
            this(referencedExpression, constantsFromValue, resolveConstantsFromName(referencedExpression, constantsFromValue));
        }
        
        private static <T> Map<String, String> resolveConstantsFromName(ReferencedExpression<T> referencedExpression, Map<T, String> constantsFromValue) {
            HashMap<String, String> constantsFromName = new HashMap<>();
            for (Map.Entry<T, String> entry : constantsFromValue.entrySet()) {
                String name = referencedExpression.stringifyReference.apply(entry.getKey());
                if (!constantsFromName.containsKey(name)) {
                    constantsFromName.put(name, entry.getValue());
                }
            }
            return constantsFromName;
        }
    }
}
