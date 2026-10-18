package org.openmarkov.core.expression;

import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableExpression extends ReferencedExpression<Variable> {
    
    public VariableExpression(List<Variable> possibleVariables, String expression) {
        super(
                possibleVariables.stream().collect(Collectors.toMap(Variable::getName, v -> v)),
                withBraces(expression, possibleVariables.stream().map(Variable::getName).toList()),
                Variable::getName,
                v -> null
        );
    }

    /**
     * @return this expression written on {@code newVariable} wherever it was written on
     * {@code oldVariable}, whether it held the variable itself or only its name between braces;
     * this very expression when it does not mention it. An expression keeps the variables it
     * refers to, so replacing a variable in a potential does not reach its expressions by itself.
     */
    public VariableExpression replacing(Variable oldVariable, Variable newVariable) {
        return replacing(Map.of(oldVariable, newVariable));
    }

    /**
     * @return this expression with each key of {@code replacements} replaced by its value, all at once,
     * so that a variable whose new name is the old name of another is not replaced twice; this very
     * expression when it mentions none of them.
     */
    public VariableExpression replacing(Map<Variable, Variable> replacements) {
        List<Variable> references = references();
        boolean holdsAVariable = references.stream().anyMatch(replacements::containsKey);
        Map<String, String> newNames = new HashMap<>();
        replacements.forEach((oldVariable, newVariable) -> newNames.put(oldVariable.getName(), newVariable.getName()));
        String expression = asStringExpression();
        String replaced = BRACED.matcher(expression).replaceAll(braced -> {
            String name = braced.group().substring(1, braced.group().length() - 1);
            return Matcher.quoteReplacement(newNames.containsKey(name) ? "{" + newNames.get(name) + "}" : braced.group());
        });
        if (!holdsAVariable && replaced.equals(expression)) {
            return this;
        }
        List<Variable> possibleVariables = new ArrayList<>(replacements.values());
        for (Variable reference : references) {
            if (!replacements.containsKey(reference)
                    && possibleVariables.stream().noneMatch(v -> v.getName().equals(reference.getName()))) {
                possibleVariables.add(reference);
            }
        }
        return new VariableExpression(possibleVariables, replaced);
    }

    private static final Pattern BRACED = Pattern.compile("\\{[^\\{]+?\\}");

    /**
     * Puts braces around the names of variables written without them, as in pow(Age [1],8), which
     * could never be evaluated otherwise. The longest names go first, and a name is taken only as a
     * whole word, not as a number and not before a parenthesis, which would make it a function.
     */
    static String withBraces(String expression, List<String> names) {
        if (expression == null || names.isEmpty()) {
            return expression;
        }
        List<String> candidates = names.stream()
                                       .filter(name -> !name.isBlank() && !isNumber(name))
                                       .sorted(Comparator.comparingInt(String::length).reversed())
                                       .toList();
        StringBuilder result = new StringBuilder();
        Matcher braced = BRACED.matcher(expression);
        int start = 0;
        while (braced.find()) {
            result.append(bracesInPlainText(expression.substring(start, braced.start()), candidates));
            result.append(braced.group());
            start = braced.end();
        }
        result.append(bracesInPlainText(expression.substring(start), candidates));
        return result.toString();
    }

    private static String bracesInPlainText(String text, List<String> names) {
        boolean[] taken = new boolean[text.length()];
        TreeMap<Integer, String> found = new TreeMap<>();
        for (String name : names) {
            for (int at = text.indexOf(name); at >= 0; at = text.indexOf(name, at + 1)) {
                int end = at + name.length();
                if (isFree(taken, at, end) && !isWordCharacter(text, at - 1) && !isWordCharacter(text, end)
                        && !isFollowedByParenthesis(text, end)) {
                    Arrays.fill(taken, at, end, true);
                    found.put(at, name);
                }
            }
        }
        StringBuilder result = new StringBuilder();
        int position = 0;
        for (var entry : found.entrySet()) {
            result.append(text, position, entry.getKey()).append('{').append(entry.getValue()).append('}');
            position = entry.getKey() + entry.getValue().length();
        }
        return result.append(text.substring(position)).toString();
    }

    private static boolean isFree(boolean[] taken, int from, int to) {
        for (int i = from; i < to; i++) {
            if (taken[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWordCharacter(String text, int index) {
        if (index < 0 || index >= text.length()) {
            return false;
        }
        char c = text.charAt(index);
        return Character.isLetterOrDigit(c) || c == '_' || c == '.';
    }

    private static boolean isFollowedByParenthesis(String text, int index) {
        String rest = text.substring(index).stripLeading();
        return rest.startsWith("(");
    }

    private static boolean isNumber(String name) {
        try {
            Double.parseDouble(name);
            return true;
        } catch (NumberFormatException notANumber) {
            return false;
        }
    }
    
    public static final class Common {
        
        public static final VariableExpression COMPLEMENT = new VariableExpression(Collections.emptyList(), "#");
        public static final VariableExpression CONSTANT = new VariableExpression(Collections.emptyList(), "Constant");
        public static final VariableExpression GAMMA = new VariableExpression(Collections.emptyList(), "Gamma");
        public static final VariableExpression ZERO = new VariableExpression(List.of(), "0");
        
    }
    
}
