package org.openmarkov.core.stringformat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.logging.OpenMarkovLogger;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allows creating messages using messages defined in a natural language way, being based on
 * {@link MessageFormat}, but instead of accessing values through indexes, they are accessed by name.
 * <p>
 * Having the following message pattern:
 * <pre>{@code "The {NetName} net created at {CreationDate, date, short} is not a {DesiredNetType} network, and this functionality is only available for {DesiredNetType}"}</pre>
 * ... you can apply the arguments of {@code NetName}, {@code CreationDate}, and {@code DesiredNetType}.
 * <br><br>
 * In this example:
 * <pre>{@code
 * String pattern = "The {NetName} net created at {CreationDate,date,short} is not a {DesiredNetType} network," +
 *         " and this functionality is only available for {DesiredNetType}";
 * Map<String, Object> values = Map.of(
 *         "NetName", "MyNet",
 *         "CreationDate", Date.UTC(2023 - 1900, 5, 3, 12, 12, 15),
 *         "DesiredNetType", "Bayesian"
 * );
 * String formatedMessage = StringFormat.apply(pattern, values);
 * System.out.println(formatedMessage);
 * }</pre>
 * <p><br>
 * ... the output will print:
 * <pre>{@code The MyNet net created at 03/06/2023 is not a Bayesian network, and this functionality is only available for Bayesian}</pre>
 *
 * @author jrico
 */
@SuppressWarnings("UnnecessaryJavaDocLink")
public class StringFormat {
    /*
        Pattern is:
        (?x)
            \s*
            (?<functionOrAttributeMark>\.|\#)
            \s*
            (?<functionOrAttribute>\w+?)
            \s*
     */
    private static final Pattern FUNCTION_AND_ATTRIBUTES_REGEX = Pattern.compile("(?x)\\s*(?<functionOrAttributeMark>[.\\#])\\s*(?<functionOrAttribute>\\w+)\\s*");
    
    /*
        Pattern is:
        (?x)
        \{
            (\s*(?<name>\w+?)\s*)
            (?<functionAndAttributes>(?x)FUNCTION_AND_ATTRIBUTES_REGEX)*
            (,\s*(?<format>\w+?)\s*)?
            (,\s*(?<style>\w+?)\s*)?
            (?<unused>,\w*?)?
        }
     */
    private static final Pattern NAMED_PARAMETER_REGEX = Pattern.compile("(?x)\\{(\\s*(?<name>\\w+?)\\s*)(?x)(?<functionAndAttributes>(" + StringFormat.FUNCTION_AND_ATTRIBUTES_REGEX.pattern() + ")*)?(,\\s*(?<format>\\w+?)\\s*)?(,\\s*(?<style>\\w+?)\\s*)?(?<unused>,\\w*?)?}");
    
    
    /**
     * Gets the {@code arguments} names of a {@code pattern} following the format of {@link StringFormat}.
     *
     * @return the {@code arguments} names of a {@code pattern}.
     */
    public static List<String> extractParameterNames(CharSequence pattern) {
        var alreadyFoundParameters = new HashSet<String>();
        return StringFormat.NAMED_PARAMETER_REGEX
                .matcher(pattern)
                .results()
                .map(match -> match.group(1))
                .filter(alreadyFoundParameters::add)
                .toList();
    }
    
    /**
     * Checks whether the provided pattern uses the syntax defined in StringFormat.
     *
     * @param pattern Pattern to be checked for StringFormat syntax.
     *
     * @return whether the provided pattern uses the syntax defined in StringFormat.
     */
    public static boolean isStringFormatUsed(@NotNull CharSequence pattern) {
        return StringFormat.NAMED_PARAMETER_REGEX.matcher(pattern).find();
    }
    
    /**
     * Gets all the formatting found in the pattern.
     *
     * @param pattern Pattern to be checked for StringFormat syntax.
     *
     * @return all the formatting found in the pattern.
     */
    public static Stream<Formatting> getAllFormattings(CharSequence pattern) {
        return StringFormat.NAMED_PARAMETER_REGEX
                .matcher(pattern)
                .results()
                .map(StringFormat::extractFormatting);
    }
    
    /**
     * Extracts a formatting from a {@link MatchResult} taken from {@link StringFormat#NAMED_PARAMETER_REGEX}.
     *
     * @param matchResult the match result containing the captured groups from the pattern.
     *
     * @return a formatting from a {@link MatchResult} taken from {@link StringFormat#NAMED_PARAMETER_REGEX}.
     */
    private static Formatting extractFormatting(MatchResult matchResult) {
        var argName = matchResult.group(1);
        var functionAndAttributesAsked = matchResult.group(3);
        var format = matchResult.group(8);
        var style = matchResult.group(10);
        var pseudocode = StringFormat.FUNCTION_AND_ATTRIBUTES_REGEX
                .matcher(functionAndAttributesAsked)
                .results()
                .map(functionAndAttributesMatchResult -> {
                    var functionOrAttributeMark = functionAndAttributesMatchResult.group(1);
                    var methodOrAttributeName = functionAndAttributesMatchResult.group(2);
                    var marker = "#".equals(functionOrAttributeMark) ? PseudoCode.Marker.FIELD : PseudoCode.Marker.METHOD;
                    return new PseudoCode(marker, methodOrAttributeName);
                })
                .toList();
        return new Formatting(argName, format, style, pseudocode);
    }
    
    /**
     * Applies the {@code arguments} to a {@code pattern} following the rules indicated at {@link StringFormat}
     *
     * @return Result of applying {@code arguments} to a {@code pattern}.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static String apply(CharSequence pattern, Map<String, Object> arguments) {
        return StringFormat.NAMED_PARAMETER_REGEX
                .matcher(pattern)
                .replaceAll(matchResult -> {
                    var formatting = StringFormat.extractFormatting(matchResult);
                    if (!arguments.containsKey(formatting.field)) {
                        return StringDatabase.surrondAsUnknown(formatting.field);
                    }
                    var argument = arguments.get(formatting.field);
                    if (argument == null) {
                        return StringDatabase.surrondAsUnknown(formatting.field + " is null");
                    }
                    
                    String resolvingStep = formatting.field;
                    for (var pseudocode : formatting.pseudocode) {
                        resolvingStep += switch (pseudocode.marker) {
                            case METHOD -> ".";
                            case FIELD -> "#";
                        };
                        resolvingStep += pseudocode.methodOrAttributeName;
                        var resolved = pseudocode.resolve(argument);
                        if (resolved.found()) {
                            argument = resolved.value();
                            if (argument == null) {
                                return StringDatabase.surrondAsUnknown(resolvingStep + " is null");
                            }
                        } else {
                            return StringDatabase.surrondAsUnknown("Cannot resolve " + resolvingStep);
                        }
                    }
                    argument = StringFormat.resolveArrayAsList(argument);
                    boolean isOpenMarkovFormat = "om".equalsIgnoreCase(formatting.format) || "openmarkov".equalsIgnoreCase(formatting.format);
                    /*
                    if (argument instanceof Localizable localizable) {
                        String localizationFormat = formatting.style == null || !isOpenMarkovFormat ? null : formatting.style;
                        LocalizationFormatter localizationFormatter = LocalizationFormatter.of(localizationFormat);
                        String localized = localizable.localize(localizationFormatter);
                        return Matcher.quoteReplacement(localized);
                    }
                    */
                    var originalArgumentClass = argument.getClass();
                    if (StringFormat.LOCALIZERS.stream()
                                               .anyMatch(localizer -> localizer.cls.isAssignableFrom(originalArgumentClass))) {
                        String localizationFormat = formatting.style == null || !isOpenMarkovFormat ? null : formatting.style;
                        LocalizationFormatter localizationFormatter = LocalizationFormatter.of(localizationFormat);
                        String localized = StringFormat.internalLocalize(argument, localizationFormatter);
                        return Matcher.quoteReplacement(localized);
                    }
                    if (formatting.style != null && !isOpenMarkovFormat) {
                        String formatter = "{0," + formatting.format + "," + formatting.style + "}";
                        try {
                            return Matcher.quoteReplacement(MessageFormat.format(formatter, argument));
                        } catch (IllegalArgumentException | NullPointerException e) {
                            OpenMarkovLogger.LOGGER.warn("Cannot use formatting and styling options " + formatter + " to format the "
                                                                 + argument.getClass().getName()
                                                                 + " argument " + argument, e);
                        }
                    }
                    if (formatting.format != null && !isOpenMarkovFormat) {
                        String formatter = "{0," + formatting.format + "}";
                        try {
                            return Matcher.quoteReplacement(MessageFormat.format(formatter, argument));
                        } catch (IllegalArgumentException | NullPointerException e) {
                            OpenMarkovLogger.LOGGER.warn("Cannot use formatting options " + formatter + " to format the "
                                                                 + argument.getClass().getName()
                                                                 + " argument " + argument, e);
                        }
                    }
                    return Matcher.quoteReplacement(argument.toString());
                });
    }
    
    private static Object resolveArrayAsList(Object object) {
        if (!object.getClass().isArray()) {
            return object;
        }
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < Array.getLength(object); i++) {
            Object item = Array.get(object, i);
            if (item.getClass().isArray()) {
                Collection resolvedItem = (List) resolveArrayAsList(item);
                list.addAll(resolvedItem);
            } else {
                list.add(item);
            }
        }
        return list;
    }
    
    private static final List<Localizer> LOCALIZERS = List.of(
            new Localizer<>(Localizable.class, (localizable, form)
                    -> localizable.localize(form)),
            new Localizer<>(Collection.class, (obj, form) -> {
                Stream<Object> stream = (Stream<Object>) obj.stream();
                return form.listSeparator.globalPrefix() +
                        stream.map(indObj ->
                                           form.listSeparator.itemPrefix() +
                                                   StringFormat.internalLocalize(indObj, form)
                                                   + form.listSeparator.itemSuffix()
                              
                              )
                              .collect(Collectors.joining(form.listSeparator.separator()))
                        + form.listSeparator.globalSuffix();
            }),
            new Localizer<>(Map.class, (obj, form) -> {
                var stream = (Stream<Map.Entry<Object, Object>>) obj.entrySet().stream();
                return form.listSeparator.globalPrefix() +
                        stream.map(entry ->
                                           form.listSeparator.itemPrefix()
                                                   + StringFormat.internalLocalize(entry.getKey(), form)
                                                   + form.listSeparator.itemSuffix()
                                                   + ": "
                                                   + StringFormat.internalLocalize(entry.getValue(), form))
                              .collect(Collectors.joining(form.listSeparator.separator()))
                        + form.listSeparator.globalSuffix();
            })
    
    
    );
    
    private static String internalLocalize(Object obj, LocalizationFormatter formatter) {
        return StringFormat.LOCALIZERS.stream()
                                      .filter(localizer -> localizer.cls.isAssignableFrom(obj.getClass()))
                                      .map(localizer -> (String) localizer.localize.apply(obj, formatter))
                                      .findFirst()
                                      .orElse(obj.toString());
    }
    
    record Localizer<T>(Class<T> cls, BiFunction<T, LocalizationFormatter, String> localize) {
    
    }
    
    /**
     * TODO: Document.
     */
    public static String applyOnObject(CharSequence pattern, Object argument) {
        HashMap<String, Object> fields = StringFormat.extractFieldsToMap(argument);
        fields.put("this", argument);
        return StringFormat.apply(pattern, fields);
    }
    
    /**
     * Reflectively gets all the fields of an object over a HashMap.
     * <p>
     * In said HashMap the keys are the values that were reflectively discovered, and the value is the value of the field.
     *
     * @param object The object to extract its fields from
     *
     * @return all the fields of an object over a HashMap.
     */
    public static @NotNull HashMap<String, Object> extractFieldsToMap(@Nullable Object object) {
        if (object == null) {
            return new HashMap<>();
        }
        Class<?> sourceClass = object.getClass();
        HashMap<String, Object> fields = new HashMap<>();
        while (sourceClass != null) {
            for (var field : sourceClass.getDeclaredFields()) {
                if (fields.containsKey(field.getName()))
                    continue;
                try {
                    field.setAccessible(true);
                } catch (InaccessibleObjectException e) {
                    if (field.getType().getName().startsWith("org.openmarkov")) {
                        OpenMarkovLogger.LOGGER.warn("Inaccessible field: " + field.getName() + " in class: " + sourceClass.getName(), e);
                    }
                }
                try {
                    fields.put(field.getName(), field.get(object));
                } catch (IllegalAccessException ignored) {
                }
            }
            sourceClass = sourceClass.getSuperclass();
        }
        return fields;
    }
    
    
    /**
     * Represents the outcome of an operation, indicating whether a value was found, or not.
     *
     * @param value The value associated with the found item, or {@code null} if no item was found.
     * @param found A boolean indicating if the item is present or not.
     * @param <T>   The type of the value.
     */
    public record FoundOrNot<T>(@Nullable T value, boolean found) {
        static <T> FoundOrNot<T> notFound() {
            return new FoundOrNot<>(null, false);
        }
        
        static <T> FoundOrNot<T> aFound(T value) {
            return new FoundOrNot<>(value, true);
        }
        
    }
    
    /**
     * Represents a formatting configuration extracted from a string pattern.
     *
     * <p>
     * Take this pattern as an example: {@code {networkHolder.getNetwork#name,om,short}}.
     *
     * @param field      The field.
     *                   <br><br>
     *                   In the example this is {@code networkHolder}.<br>
     * @param format     The format that applies to the extracted field.
     *                   <br><br>
     *                   In the example this is {@code om}.<br>
     * @param style      The style defined for the extracted formatting.
     *                   <br><br>
     *                   In the example this is {@code short}.<br>
     * @param pseudocode A list of pseudo-code elements related to the field's formatting operations.
     *                   <br><br>
     *                   In the example this is {@code .getNetwork} and {@code #name}.<br>
     */
    public record Formatting(String field, String format, String style, List<PseudoCode> pseudocode) {
        
        public @Nullable AccessibleObject lastMethodOrFieldUsed(Field field) {
            AccessibleObject resolvingStep = field;
            for (var pseudocode : this.pseudocode) {
                resolvingStep = pseudocode.resolveAccessible(field.getType()).value();
                if (resolvingStep == null) {
                    return null;
                }
            }
            return resolvingStep;
        }
    }
    
    /**
     * Represents how to resolve pseudocode on the field side of {@link StringFormat#NAMED_PARAMETER_REGEX}.
     * <p>
     * The {@link Marker} tells whether what it is inteded to discover is a field or a method, and
     * {@link PseudoCode#methodOrAttributeName} is the name of said field or method.
     *
     * @param marker                Specifies whether the target is a method or a field.
     * @param methodOrAttributeName The name of the method or field to be resolved.
     */
    public record PseudoCode(Marker marker, String methodOrAttributeName) {
        
        /**
         * Reflectively resolves the method or field (Depending on the {@link PseudoCode#marker}) on an object.
         *
         * @param argument The object instance from which the method or field should be resolved.
         *
         * @return A {@code FoundOrNot<Object>} instance indicating whether it could find and get the result of said
         * field or method.
         * <p>
         * If said {@link FoundOrNot#found} is false, then it could not get the field or method through reflections.
         */
        public FoundOrNot<Object> resolve(Object argument) {
            var discoveryOrder = switch (marker) {
                case METHOD -> Stream.of(Marker.METHOD, Marker.FIELD);
                case FIELD -> Stream.of(Marker.FIELD, Marker.METHOD);
            };
            return discoveryOrder.map(marker -> marker.resolve(argument, methodOrAttributeName))
                                 .filter(FoundOrNot::found)
                                 .findFirst()
                                 .orElse(new FoundOrNot<>(null, false));
        }
        
        public FoundOrNot<AccessibleObject> resolveAccessible(Class<?> argumentClass) {
            var discoveryOrder = switch (marker) {
                case METHOD -> Stream.of(Marker.METHOD, Marker.FIELD);
                case FIELD -> Stream.of(Marker.FIELD, Marker.METHOD);
            };
            return discoveryOrder.map(marker -> marker.resolveAccesibleObject(argumentClass, methodOrAttributeName))
                                 .filter(FoundOrNot::found)
                                 .findFirst()
                                 .orElse(FoundOrNot.notFound());
        }
        
        /**
         * Gets a list of classes that should be open for being able to resolve the method or field reflectively over a
         * class.
         *
         * @param argument The class for which the resolution is discovered.
         *
         * @return A {@code FoundOrNot<List<Class<?>>} list of classes that should be open.
         * <p>
         * If {@link FoundOrNot#found} is false, then no field or method exists by indicated the name for the argument
         * class.
         */
        public FoundOrNot<List<Class<?>>> resolveClassesThatShouldBeOpen(Class<?> argument) {
            var discoveryOrder = switch (marker) {
                case METHOD -> Stream.of(Marker.METHOD, Marker.FIELD);
                case FIELD -> Stream.of(Marker.FIELD, Marker.METHOD);
            };
            return discoveryOrder.map(marker -> marker.resolveClassesThatShouldBeOpen(argument, methodOrAttributeName))
                                 .filter(FoundOrNot::found)
                                 .findFirst()
                                 .orElse(new FoundOrNot<>(null, false));
        }
        
        /**
         * Represents whether a reflective operation is to resolve the value of a field or a method.
         */
        public enum Marker {
            METHOD, FIELD;
            
            /**
             * Reflectively resolves a method or field for the given object.
             *
             * @param argument              The object from which the field or method is to be resolved.
             * @param methodOrAttributeName The name of the method or field to resolve.
             *
             * @return A {@code FoundOrNot<Object>} containing the result of the resolution.
             * <p>
             * If {@link FoundOrNot#found} is false, then the value could not be resolved reflectively.
             */
            public @NotNull FoundOrNot<Object> resolve(Object argument, String methodOrAttributeName) {
                var accessibleObject = this.resolveAccesibleObject(argument.getClass(), methodOrAttributeName);
                if (!accessibleObject.found()) {
                    return FoundOrNot.notFound();
                }
                try {
                    return switch (accessibleObject.value) {
                        case Method method -> FoundOrNot.aFound(method.invoke(argument));
                        case Field field -> FoundOrNot.aFound(field.get(argument));
                        case null, default -> FoundOrNot.notFound();
                    };
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return FoundOrNot.notFound();
                }
            }
            
            public @NotNull FoundOrNot<AccessibleObject> resolveAccesibleObject(Class<?> fromClass, String methodOrAttributeName) {
                while (fromClass != null) {
                    switch (this) {
                        case METHOD -> {
                            try {
                                var method = fromClass.getDeclaredMethod(methodOrAttributeName);
                                method.setAccessible(true);
                                return new FoundOrNot<>(method, true);
                            } catch (InaccessibleObjectException | NoSuchMethodException ignored) {
                            }
                        }
                        case FIELD -> {
                            try {
                                var field = fromClass.getDeclaredField(methodOrAttributeName);
                                field.setAccessible(true);
                                return new FoundOrNot<>(field, true);
                            } catch (InaccessibleObjectException | NoSuchFieldException ignored) {
                            }
                        }
                    }
                    fromClass = fromClass.getSuperclass();
                }
                return new FoundOrNot<>(null, false);
            }
            
            /**
             * Gets a list of classes that should be open for being able to resolve the named field or method
             * reflectively over a class.
             *
             * @param argument              The class for which the resolution is discovered.
             * @param methodOrAttributeName The name of the field or method.
             *
             * @return A {@code FoundOrNot<List<Class<?>>} list of classes that should be open.
             * <p>
             * If {@link FoundOrNot#found} is false, then no field or method exists by indicated the name for the argument
             * class.
             */
            public FoundOrNot<List<Class<?>>> resolveClassesThatShouldBeOpen(Class<?> argument, String methodOrAttributeName) {
                var argumentClass = argument;
                while (argumentClass != null) {
                    switch (this) {
                        case METHOD -> {
                            try {
                                var method = argumentClass.getDeclaredMethod(methodOrAttributeName);
                                method.setAccessible(true);
                                return new FoundOrNot<>(List.of(method.getDeclaringClass(), method.getReturnType()), true);
                            } catch (InaccessibleObjectException | NoSuchMethodException ignored) {
                            }
                        }
                        case FIELD -> {
                            try {
                                var field = argumentClass.getDeclaredField(methodOrAttributeName);
                                field.setAccessible(true);
                                return new FoundOrNot<>(List.of(field.getDeclaringClass(), field.getType()), true);
                            } catch (InaccessibleObjectException | NoSuchFieldException ignored) {
                            }
                        }
                    }
                    argumentClass = argumentClass.getSuperclass();
                }
                return new FoundOrNot<>(null, false);
            }
        }
    }
    
}
