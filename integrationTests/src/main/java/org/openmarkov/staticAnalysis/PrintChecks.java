package org.openmarkov.staticAnalysis;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionEnumConstantDeclaration;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrintChecks {
    
    public static class ToCheckValue {
        public String source = "";
        public String reasonDescription = "";
        public List<ToCheck.ReasonKind> reasonKind = new ArrayList<>();
        public List<Integer> relatesToIssues = new ArrayList<>();
    }
    
    private static Stream<ToCheckValue> getAllToChecks() {
        return ParseUtils.baseOpenMarkovParsedClasses()
                         .flatMap(parsedClass -> parsedClass.compilationUnit().findAll(AnnotationExpr.class).stream())
                         .map(PrintChecks::extractCheckValue)
                         .filter(Objects::nonNull);
    }
    
    public static void main(String[] args) {
        HashMap<ToCheck.ReasonKind, ArrayList<ToCheckValue>> toCheckValues = new HashMap<>();
        getAllToChecks().forEach(toCheckValue -> {
            if (toCheckValue.reasonKind.isEmpty()) {
                if (!toCheckValues.containsKey(null)) {
                    toCheckValues.put(null, new ArrayList<>());
                }
                toCheckValues.get(null).add(toCheckValue);
                return;
            }
            toCheckValue.reasonKind.forEach(reasonKind -> {
                if (!toCheckValues.containsKey(reasonKind)) {
                    toCheckValues.put(reasonKind, new ArrayList<>());
                }
                toCheckValues.get(reasonKind).add(toCheckValue);
            });
        });
        toCheckValues
                .keySet()
                .stream()
                .sorted(Comparator.nullsLast(
                        Comparator.comparing(Enum::ordinal)))
                .forEach(reasonKind -> {
                    String printableName = reasonKind == null ? "UNCATEGORISED" : reasonKind.name()
                                                                                            .replace("_", " ");
                    var checks = toCheckValues.get(reasonKind);
                    System.out.println("- " + printableName + " - (" + checks.size() + " items)");
                    checks.forEach(check -> {
                        var issuesString = "";
                        if (!check.relatesToIssues.isEmpty()) {
                            issuesString = " (relates to issues " +
                                    check.relatesToIssues.stream()
                                                         .map(Object::toString)
                                                         .collect(Collectors.joining(", "))
                                    + ")";
                        }
                        System.out.println("- " + check.source + ": " + check.reasonDescription + issuesString);
                    });
                    System.out.println();
                });
        
        System.out.println();
    }
    
    
    private static @Nullable ToCheckValue extractCheckValue(AnnotationExpr annotationExpr) {
        var annotationClassName = annotationExpr.resolve().getQualifiedName();
        if (ParseUtils.classForName(annotationClassName) != ToCheck.class) {
            return null;
        }
        var toCheckValue = new ToCheckValue();
        annotationExpr
                .getChildNodes()
                .stream()
                .filter(node -> node instanceof MemberValuePair)
                .map(node -> (MemberValuePair) node)
                .forEach(memberValuePair -> {
                    Expression value = memberValuePair.getValue();
                    String fieldName = memberValuePair.getNameAsString();
                    Object interpretedValue = interpret(value);
                    try {
                        ToCheckValue.class.getDeclaredField(fieldName).set(toCheckValue, interpretedValue);
                    } catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
                        try {
                            ToCheckValue.class.getDeclaredField(fieldName)
                                              .set(toCheckValue, new ArrayList<>(List.of(interpretedValue)));
                        } catch (NoSuchFieldException | IllegalAccessException | RuntimeException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        toCheckValue.source = ParseUtils.getSourceLine(annotationExpr);
        return toCheckValue;
    }
    
    private static @Nullable Object interpret(Expression expression) {
        return switch (expression) {
            case NullLiteralExpr ignored -> null;
            case IntegerLiteralExpr integerLiteralExpr -> Integer.valueOf(integerLiteralExpr.getValue());
            case BooleanLiteralExpr booleanLiteralExpr -> Boolean.valueOf(booleanLiteralExpr.getValue());
            case DoubleLiteralExpr doubleLiteralExpr -> Double.valueOf(doubleLiteralExpr.getValue());
            case LongLiteralExpr longLiteralExpr -> Long.valueOf(longLiteralExpr.getValue());
            case CharLiteralExpr charLiteralExpr -> charLiteralExpr.getValue().charAt(0);
            case StringLiteralExpr stringLiteralExpr -> stringLiteralExpr.getValue();
            case ArrayInitializerExpr arrayInitializerExpr ->
                    new ArrayList<>(arrayInitializerExpr.getValues().stream().map(PrintChecks::interpret).toList());
            case FieldAccessExpr fieldAccessExpr -> {
                var name = fieldAccessExpr.getNameAsString();
                yield switch (fieldAccessExpr.resolve()) {
                    case ReflectionEnumConstantDeclaration enumConstantDeclaration -> {
                        ResolvedType type = enumConstantDeclaration.getType();
                        Class<?> enumClass = ParseUtils.classForName(type.asReferenceType().getQualifiedName());
                        var enumConstants = enumClass.getEnumConstants();
                        yield Arrays.stream(enumConstants)
                                    .filter(constant -> constant.toString().equals(name))
                                    .findFirst()
                                    .orElse(null);
                    }
                    default -> name;
                };
            }
            case BinaryExpr binaryExpr -> {
                var left = interpret(binaryExpr.getLeft());
                var right = interpret(binaryExpr.getRight());
                yield switch (binaryExpr.getOperator()) {
                    case PLUS -> {
                        if (left instanceof String || right instanceof String) {
                            yield String.valueOf(left) + String.valueOf(right);
                        }
                        yield null;
                    }
                    default -> null;
                };
            }
            default -> null;
        };
    }
    
}
