package org.openmarkov.integrationTests.staticAnalysis.localization.autolocalization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.localize.ConsiderAutoLocalizationIsValid;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.localize.StringBundle;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.openmarkov.core.stringformat.StringFormat;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * See the method {@link ValidateClassLocalization#validateAutoLocalization()}, which is the purpose of this test class.
 *
 * @author jrico
 */
public class ValidateClassLocalization {
    
    /**
     * List of all {@link Bundle}, including their {@link StringBundle} preloaded.
     */
    private static final List<Bundle> BUNDLES = StringDatabase
            .getBundleProviders()
            .flatMap(Bundle::of)
            .toList();
    /**
     * Map where every module has a list of {@link Bundle}s that are defined in said module.
     */
    private static final Map<Module, List<Bundle>> MODULES_AND_BUNDLES = ValidateClassLocalization.BUNDLES
            .stream()
            .collect(Collectors.groupingBy(bundle -> bundle.provider.getClass().getModule()));
    /**
     * Map where every module has a list of {@link ClassLocalizable}s that are defined in said module.
     */
    private static final Map<Module, List<Class<? extends ClassLocalizable>>> MODULES_AND_AUTOLOCALIZABLES =
            PluginSearch.init()
                        .childrenOf(ClassLocalizable.class)
                        .stream()
                        .filter(localizableClass -> !localizableClass.isInterface() && !Modifier.isAbstract(localizableClass.getModifiers()))
                        .filter(ClassUtils::isProductionClass)
                        .collect(Collectors.groupingBy(Class::getModule));
    /**
     * List where every {@link ClassLocalizable} of every module is associated to the Bundles that can be accessed in
     * said module.
     */
    private static final List<AutolocalizablesAndBundles> LOCALIZABLES_AND_ACCESIBLE_BUNDLES = ValidateClassLocalization.MODULES_AND_AUTOLOCALIZABLES
            .entrySet()
            .stream()
            .map(moduleAndLocalizable -> {
                var targetModule = moduleAndLocalizable.getKey();
                List<Bundle> resourceProviders = ValidateClassLocalization.MODULES_AND_BUNDLES
                        .entrySet()
                        .stream()
                        .filter(providersModule -> targetModule.canRead(providersModule.getKey()))
                        .flatMap(providersModule -> providersModule.getValue()
                                                                   .stream())
                        .toList();
                return new AutolocalizablesAndBundles(resourceProviders, moduleAndLocalizable.getValue());
            }).toList();
    
    /**
     * Searches for every {@link ClassLocalizable} class and then gets their localization String in the
     * {@link StringDatabase#getUniqueInstance()} to verify it is well written.
     * <p>
     * A localization String is well written when:
     * <ul>
     *   <li>A localization String is specified for the class.</li>
     *   <li>The localization String is written in a stringBundle that is accessible to the class.</li>
     *   <li>All the values (See {@link StringFormat}) of the localization String correspond to fields of the class.
     *   </li>
     *   <li>All the pseudocodes written on the values can be resolved.
     *   </li>
     * </ul>
     * If a localization String was to be wrong, this test will fail specifying which where wrong, why, and it will also
     * print some advice and example code on how to solve it.
     */
    @Test void validateAutoLocalization() {
        List<Error> errors = ValidateClassLocalization.findAllAutoLocalizationErrors();
        if (errors.isEmpty()) {
            return;
        }
        ValidateClassLocalization.failWithLocalizationErrors(errors);
    }
    
    private static void failWithLocalizationErrors(List<Error> errors) {
        var errorClassAndErrors = errors.stream().collect(Collectors.groupingBy(Error::getClass));
        
        String errorsDescription = errorClassAndErrors
                .entrySet()
                .stream()
                .map(entry -> {
                    var unspecifiedErrorsForClass = entry.getValue();
                    return switch (unspecifiedErrorsForClass.getFirst()) {
                        case Error.FieldOrMethodMissing fieldOrMethodMissing -> {
                            var errorsForClass = unspecifiedErrorsForClass
                                    .stream().map(v -> (Error.FieldOrMethodMissing) v)
                                    .sorted(Comparator.comparing(v -> v.classWithMissingComponent.getName()))
                                    .toList();
                            String subErrorsDetails = errorsForClass
                                    .stream()
                                    .map(error ->
                                                 "\t- No "
                                                         + error.marker.toString().toLowerCase()
                                                         + " "
                                                         + error.missingComponent
                                                         + " in "
                                                         + error.classWithMissingComponent.getName()
                                                         + " of module "
                                                         + error.classWithMissingComponent.getModule().getName()
                                                         + " when localizing "
                                                         + error.localizableClass.getName())
                                    .distinct()
                                    .collect(Collectors.joining(System.lineSeparator()));
                            yield "Some fields and methods are missing:" + System.lineSeparator() + subErrorsDetails;
                        }
                        case Error.LocalizationInInaccessibleBundle localizationInInaccessibleBundle -> {
                            var errorsForClass = unspecifiedErrorsForClass
                                    .stream().map(v -> (Error.LocalizationInInaccessibleBundle) v)
                                    .sorted(Comparator.comparing(v -> v.localizableClass.getName()))
                                    .toList();
                            String subErrorsDetails = errorsForClass
                                    .stream()
                                    .map(error ->
                                                 "\t- Class " + error.localizableClass.getName() + " is localized in " + error.wrongBundle.stringBundle + "_en.xml of module " + error.wrongBundle.provider.getClass()
                                                                                                                                                                                                           .getModule()
                                                                                                                                                                                                           .getName() + ", but it should be in " + error.localizableClass.getModule()
                                                                                                                                                                                                                                                                         .getName())
                                    .distinct()
                                    .collect(Collectors.joining(System.lineSeparator()));
                            yield "Some classes are localized in the wrong module, you can apply this movements:" + System.lineSeparator() + subErrorsDetails;
                        }
                        case Error.LocalizationMissing localizationMissing -> {
                            var errorsForClass = unspecifiedErrorsForClass
                                    .stream()
                                    .map(v -> (Error.LocalizationMissing) v)
                                    .sorted(Comparator.comparing(v -> v.localizableClass.getName()))
                                    .toList();
                            String subErrorsDetails = errorsForClass
                                    .stream()
                                    .map(error ->
                                                 "    <Localization class=\"" + error.localizableClass.getName()
                                                         + "\"" + System.lineSeparator() +
                                                         "                  value=\"\"/>")
                                    .distinct()
                                    .collect(Collectors.joining(System.lineSeparator()));
                            yield "Some classes are not localized, you can add them with:" + System.lineSeparator() + subErrorsDetails;
                        }
                        case Error.UsedClassIsNotLocalizable usedClassIsNotLocalizable -> {
                            var errorsByClass = unspecifiedErrorsForClass
                                    .stream()
                                    .map(error -> (Error.UsedClassIsNotLocalizable) error)
                                    .collect(Collectors.groupingBy(e -> e.localizableClass));
                            var errorsByClassExplained = errorsByClass
                                    .entrySet()
                                    .stream()
                                    .map(errorByClassEntry -> {
                                        var sourceCass = errorByClassEntry.getKey();
                                        var unlocalizableClasses = errorByClassEntry.getValue()
                                                                                    .stream()
                                                                                    .map(e -> e.inlocalizableClass.getName())
                                                                                    .sorted()
                                                                                    .collect(Collectors.joining(", "));
                                        return "\t- Improper values in the localization of " + sourceCass.getName() + " are: " + unlocalizableClasses + ".";
                                    })
                                    .collect(Collectors.joining(System.lineSeparator()));
                            
                            yield "Localization of some classes use values that cannot be properly shown to the user, " +
                                    "as instead of showing a string representing said value, " +
                                    "it will show the memory address of said value: "
                                    + System.lineSeparator()
                                    + errorsByClassExplained;
                        }
                    };
                })
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
        fail(System.lineSeparator() + System.lineSeparator() + errorsDescription + System.lineSeparator() + System.lineSeparator());
    }
    
    private static @NotNull List<Error> findAllAutoLocalizationErrors() {
        List<Error> errors = new ArrayList<>();
        ValidateClassLocalization.LOCALIZABLES_AND_ACCESIBLE_BUNDLES
                .stream()
                .flatMap(autolocalizablesAndBundles ->
                                 autolocalizablesAndBundles.localizablesClasses
                                         .stream()
                                         .filter(autolocalizableClass -> autolocalizableClass.getDeclaredAnnotation(ConsiderAutoLocalizationIsValid.class) == null)
                                         .map(autoLocalizableClass -> new AutolocalizableAndBundles(autolocalizablesAndBundles.bundles, autoLocalizableClass))
                )
                .forEach(autolocalizableAndBundles -> {
                    Class<? extends ClassLocalizable> localizableClass = autolocalizableAndBundles.localizablesClass;
                    String keyName = localizableClass.getName();
                    @Nullable Bundle localizedBundle;
                    Optional<Bundle> accessibleBundle = autolocalizableAndBundles.bundles
                            .stream()
                            .filter(bundles -> bundles.stringBundle.getString(keyName) != null)
                            .findFirst();
                    if (accessibleBundle.isPresent()) {
                        localizedBundle = accessibleBundle.get();
                    } else {
                        Optional<Bundle> inaccessibleBundle = ValidateClassLocalization.BUNDLES.stream()
                                                                                               .filter(bundles -> bundles.stringBundle.getString(keyName) != null)
                                                                                               .findFirst();
                        if (inaccessibleBundle.isPresent()) {
                            localizedBundle = inaccessibleBundle.get();
                            errors.add(new Error.LocalizationInInaccessibleBundle(localizableClass, localizedBundle));
                        } else {
                            errors.add(new Error.LocalizationMissing(localizableClass));
                            return;
                        }
                    }
                    String localizedString = localizedBundle.stringBundle.getString(keyName);
                    var formattings = StringFormat.getAllFormattings(localizedString).toList();
                    for (var formatting : formattings) {
                        Class<?> argumentClass = localizableClass;
                        if (!"this".equals(formatting.field())) {
                            var fieldClass = new StringFormat.PseudoCode(StringFormat.PseudoCode.Marker.FIELD, formatting.field())
                                    .resolveClassesThatShouldBeOpen(argumentClass);
                            if (!fieldClass.found()) {
                                errors.add(new Error.FieldOrMethodMissing(localizableClass, localizableClass, formatting.field(), StringFormat.PseudoCode.Marker.FIELD));
                                continue;
                            }
                            argumentClass = fieldClass.value().get(fieldClass.value().size() - 1);
                        }
                        for (var pseudocode : formatting.pseudocode()) {
                            var classesFound = pseudocode.resolveClassesThatShouldBeOpen(argumentClass);
                            if (classesFound.found()) {
                                argumentClass = classesFound.value().get(classesFound.value().size() - 1);
                            } else {
                                errors.add(new Error.FieldOrMethodMissing(localizableClass, argumentClass, pseudocode.methodOrAttributeName(), pseudocode.marker()));
                            }
                        }
                    }
                    
                    var classesUsedInLocalization = new HashSet<Class<Object>>();
                    for (var formatting : formattings) {
                        try {
                            var lastMethodOrFieldUsed = formatting.lastMethodOrFieldUsed(autolocalizableAndBundles.localizablesClass.getDeclaredField(formatting.field()));
                            switch (lastMethodOrFieldUsed) {
                                case Method method -> {
                                    Arrays.stream(method.getGenericParameterTypes())
                                          .flatMap(ValidateClassLocalization::extractParameterizedTypes)
                                          .forEach(classesUsedInLocalization::add);
                                    classesUsedInLocalization.add((Class<Object>) method.getReturnType());
                                }
                                case Field field -> {
                                    ValidateClassLocalization.extractParameterizedTypes(field.getGenericType())
                                                             .forEach(classesUsedInLocalization::add);
                                    classesUsedInLocalization.add((Class<Object>) field.getType());
                                }
                                case null, default -> {
                                }
                            }
                        } catch (NoSuchFieldException e) {
                        }
                    }
                    classesUsedInLocalization
                            .stream()
                            .filter(usedClassInLocalization -> {
                                boolean willBeProperlyLocalized = false;
                                if (ValidateClassLocalization.OPENMARKOV_CLASSES.contains(usedClassInLocalization)) {
                                    willBeProperlyLocalized = Localizable.class.isAssignableFrom(usedClassInLocalization);
                                } else {
                                    willBeProperlyLocalized = implementsToString(usedClassInLocalization)
                                            || usedClassInLocalization.isPrimitive()
                                            || Collection.class.isAssignableFrom(usedClassInLocalization)
                                            || Map.class.isAssignableFrom(usedClassInLocalization);
                                }
                                return !willBeProperlyLocalized;
                            })
                            .forEach(usedClassInLocalization -> errors.add(new Error.UsedClassIsNotLocalizable(localizableClass, usedClassInLocalization)));
                });
        return errors;
    }
    
    private static boolean implementsToString(Class<?> usedClassInLocalization) {
        while (usedClassInLocalization.isArray()) {
            usedClassInLocalization = usedClassInLocalization.getComponentType();
        }
        while (usedClassInLocalization != null && usedClassInLocalization != Object.class) {
            try {
                usedClassInLocalization.getDeclaredMethod("toString");
                return true;
            } catch (NoSuchMethodException e) {
            }
            usedClassInLocalization = usedClassInLocalization.getSuperclass();
        }
        return false;
    }
    
    private static Stream<Class<Object>> extractParameterizedTypes(Type generic) {
        if (generic instanceof ParameterizedType parameterizedType) {
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                         .filter(t -> t instanceof Class)
                         .map(t -> (Class<Object>) t);
        }
        return Stream.empty();
    }
    
    /**
     * Represents every kind of error related to an improper localization of a {@link ClassLocalizable} class.
     */
    public static abstract sealed class Error permits Error.FieldOrMethodMissing, Error.LocalizationInInaccessibleBundle, Error.LocalizationMissing, Error.UsedClassIsNotLocalizable {
        
        Class<? extends ClassLocalizable> localizableClass;
        
        public void tryOverrideLocalizableClass(Class<? extends ClassLocalizable> localizableClass) {
            if (this.localizableClass == null) {
                this.localizableClass = localizableClass;
            }
        }
        
        /**
         * Represents the error of an {@link ClassLocalizable} class that has no localization file.
         */
        static final class LocalizationMissing extends Error {
            
            /**
             * Constructs a {@code LocalizationMissing} error with the specified {@link ClassLocalizable} class.
             *
             * @param localizableClass the class that is missing localization.
             */
            LocalizationMissing(Class<? extends ClassLocalizable> localizableClass) {
                this.localizableClass = localizableClass;
            }
        }
        
        /**
         * Represents the error of an {@link ClassLocalizable} class whose localization value is in a stringBundle not
         * accessible from this class.
         */
        static final class LocalizationInInaccessibleBundle extends Error {
            Class<? extends ClassLocalizable> localizableClass;
            Bundle wrongBundle;
            
            /**
             * Constructs an error representing an {@link ClassLocalizable} class whose localization value is in a stringBundle
             * not accessible from this class.
             *
             * @param localizableClass the class whose localization is in an inaccessible stringBundle.
             * @param wrongBundle      the stringBundle containing localization value, but that is not accessible to the class.
             */
            LocalizationInInaccessibleBundle(Class<? extends ClassLocalizable> localizableClass, Bundle wrongBundle) {
                this.localizableClass = localizableClass;
                this.wrongBundle = wrongBundle;
            }
        }
        
        /**
         * Represents the error of a field or method that cannot be resolved reflectively when resolving the
         * localization String of the {@link ClassLocalizable} class.
         */
        static final class FieldOrMethodMissing extends Error {
            Class<? extends ClassLocalizable> localizableClass;
            Class<?> classWithMissingComponent;
            String missingComponent;
            StringFormat.PseudoCode.Marker marker;
            
            /**
             * Constructs the error to represent a field or method that could not be resolved while resolving the
             * localization String.
             *
             * @param localizableClass          the {@link ClassLocalizable} class being localized.
             * @param classWithMissingComponent the class containing the missing field or method.
             * @param missingComponent          the name of the missing field or method.
             * @param marker                    the marker associated with the formatting process.
             */
            FieldOrMethodMissing(Class<? extends ClassLocalizable> localizableClass, Class<?> classWithMissingComponent, String missingComponent, StringFormat.PseudoCode.Marker marker) {
                this.localizableClass = localizableClass;
                this.classWithMissingComponent = classWithMissingComponent;
                this.missingComponent = missingComponent;
                this.marker = marker;
            }
        }
        
        /**
         * Represents the error of a field or method that cannot be resolved reflectively when resolving the
         * localization String of the {@link ClassLocalizable} class.
         */
        static final class UsedClassIsNotLocalizable extends Error {
            Class<? extends ClassLocalizable> localizableClass;
            Class<?> inlocalizableClass;
            
            /**
             * Constructs the error to represent a field or method that could not be resolved while resolving the
             * localization String.
             *
             * @param localizableClass   the {@link ClassLocalizable} class being localized.
             * @param inlocalizableClass the class containing the missing field or method.
             */
            UsedClassIsNotLocalizable(Class<? extends ClassLocalizable> localizableClass, Class<?> inlocalizableClass) {
                this.localizableClass = localizableClass;
                this.inlocalizableClass = inlocalizableClass;
            }
        }
    }
    
    /**
     * List of {@link ClassLocalizable} classes associated with the bundles they can access (Meaning these
     * {@link ClassLocalizable}s come all from the same module).
     */
    private record AutolocalizablesAndBundles(List<Bundle> bundles,
                                              List<Class<? extends ClassLocalizable>> localizablesClasses) {
    }
    
    /**
     * A {@link ClassLocalizable} class associated with the stringBundle it can access.
     */
    private record AutolocalizableAndBundles(List<Bundle> bundles,
                                             Class<? extends ClassLocalizable> localizablesClass) {
    }
    
    /**
     * Aggregate of all the information to represent a Bundle file.
     *
     * @param provider     The {@link LocalizeResourcesProvider} where this stringBundle came from.
     * @param bundleName   The name of the stringBundle.
     * @param stringBundle The resolved {@link StringBundle}.
     */
    public record Bundle(LocalizeResourcesProvider provider, String bundleName, StringBundle stringBundle) {
        
        /**
         * Creates a stream of bundles created from every Bundle in
         * {@link LocalizeResourcesProvider#getBundlesMap(Locale)}.
         *
         * @param provider Used to retrieve all the localization resource bundles.
         *
         * @return a stream of bundles created from every Bundle in
         * {@link LocalizeResourcesProvider#getBundlesMap(Locale)}.
         */
        public static Stream<Bundle> of(LocalizeResourcesProvider provider) {
            return provider.getBundlesMap(Locale.ENGLISH)
                           .entrySet()
                           .stream()
                           .map(entry -> new Bundle(provider, entry.getKey(), entry.getValue()));
        }
    }
    
    private static final Set<Class<?>> OPENMARKOV_CLASSES = PluginSearch.init()
                                                                        .stream()
                                                                        .collect(Collectors.toSet());
}
