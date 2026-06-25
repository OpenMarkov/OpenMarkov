package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.core.stringformat.StringFormat;

import java.util.*;
import java.util.stream.IntStream;

import static org.openmarkov.core.logging.OpenMarkovLogger.LOGGER;

@ImplementationRequirements(
        hasToExtendOneOfTheseClasses = Exception.class
)
/**
 * Common interface for exceptions declared in OpenMarkov.
 *
 * @author jrico
 */
public interface IOpenMarkovException extends Localizable {
    
    /**
     * Gets a message for this exception, which might be null.
     *
     * @return The message for this exception, which might be null.
     */
    @Nullable String getExceptionMessage();
    
    /**
     * Gets a title for this exception, which might be null.
     *
     * @return The title for this exception, which might be null.
     */
    @Nullable String getExceptionTitle();
    
    
    /**
     * Searches for a {@link String} in the {@link StringDatabase#getUniqueInstance()} matching the key
     * 'name of this exception + .title'.
     * <p>
     * If the said key exists, it also tries to solve the contents of that Localized String using every field possible
     * from the Exception class and its superclasses.
     * <p>
     * This method is intended when you don't write the title in the code but in a Bundle File.
     *
     * @return A String title from Bundle file which was formatted with every field inherited by the class extending
     * {@link IOpenMarkovException}.
     */
    static @Nullable String autoGetExceptionTitle(IOpenMarkovException openMarkovException) {
        return IOpenMarkovException.localizeWithOpenMarkov(openMarkovException, BundleSearch.BUNDLE_SUFFIX_TITLE);
    }
    
    /**
     * Searches for a {@link String} in the {@link StringDatabase#getUniqueInstance()} matching the key
     * 'name of this exception + .message'.
     * <p>
     * If the said key exists, it also tries to solve the contents of that Localized String using every field possible
     * from the Exception class and its superclasses.
     * <p>
     * This method is intended when you don't write the message in the code but in a Bundle File.
     *
     * @return A String message from Bundle file which was formatted with every field inherited by the class extending
     * {@link IOpenMarkovException}.
     */
    static @Nullable String autoGetExceptionMessage(IOpenMarkovException openMarkovException) {
        return IOpenMarkovException.localizeWithOpenMarkov(openMarkovException, BundleSearch.BUNDLE_SUFFIX_MESSAGE);
    }
    
    /**
     * Turns any kind of exception to an {@link IOpenMarkovException}, the conversion varies depending on the Exception
     * sent:
     * <ul>
     *   <li>If it is already a {@link IOpenMarkovException}: it returns said exception</li>
     *   <li>If there is a localization key in {@link StringDatabase#getUniqueInstance()} with a title or message
     *   matching "class name + .title or .message": A {@link LocalizedJavaException} containing the exception.</li>
     *   <li>In any other case: A {@link UnlocalizedJavaException}</li>
     * </ul>
     *
     * @param exception The exception to turn into {@link IOpenMarkovException}.
     *
     * @return an exception turned into {@link IOpenMarkovException}.
     */
    @SuppressWarnings("unchecked")
    static @NotNull IOpenMarkovException of(@Nullable Exception exception) {
        if (exception instanceof IOpenMarkovException alreadyPrepared) {
            return alreadyPrepared;
        }
        @Nullable Class<Exception> exceptionClass = (Class<Exception>) exception.getClass();
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        while (exceptionClass != null) {
            String exceptionClassName = exceptionClass.getName();
            if (exceptionClassName.contains(".")) {
                exceptionClassName = exceptionClassName.substring(exceptionClassName.lastIndexOf('.') + 1);
            }
            String title = stringDatabase
                    .getNullableString(exceptionClassName + BundleSearch.BUNDLE_SUFFIX_TITLE);
            String message = stringDatabase
                    .getNullableString(exceptionClassName + BundleSearch.BUNDLE_SUFFIX_MESSAGE);
            if (title != null || message != null) {
                LocalizedJavaException localizedJavaError = new LocalizedJavaException(exceptionClassName);
                localizedJavaError.setStackTrace(exception.getStackTrace());
                localizedJavaError.initCause(exception.getCause());
                return localizedJavaError;
            }
            try {
                exceptionClass = (Class<Exception>) exceptionClass.getSuperclass();
                if (exceptionClass.isAssignableFrom(Exception.class) && exceptionClass != Exception.class) {
                    exceptionClass = null;
                }
            } catch (ClassCastException ignored) {
                exceptionClass = null;
            }
        }
        UnlocalizedJavaException unlocalizedJavaError = new UnlocalizedJavaException(exception);
        unlocalizedJavaError.setStackTrace(exception.getStackTrace());
        unlocalizedJavaError.initCause(exception.getCause());
        return unlocalizedJavaError;
    }
    
    
    /**
     * It attempts to find a localized string by the name of the {@link IOpenMarkovException}'s class.
     * <p>
     * If found, it will also format said String using the fields of the class as parameters.
     *
     * @param openMarkovException the exception to localize
     * @param suffixKey           the suffix to append to the exception's class name when generating the localization key
     *
     * @return the formatted and localized string if successful, or null if no localization was found
     */
    private static @Nullable String localizeWithOpenMarkov(IOpenMarkovException openMarkovException, String suffixKey) {
        Class<? extends IOpenMarkovException> exceptionClass = openMarkovException.getClass();
        String preStringKey = exceptionClass.getSimpleName();
        preStringKey += suffixKey;
        String stringKey = preStringKey;
        
        @Nullable String preformatedString = null;
        var exceptionBundle = BundleSearch.findExceptionBundle(exceptionClass);
        if (exceptionBundle.isPresent()) {
            String prefixedKey = stringKey;
            if (exceptionBundle.get().prefixKey != null && !exceptionBundle.get().prefixKey.isBlank()) {
                prefixedKey = exceptionBundle.get().prefixKey + prefixedKey;
            }
            preformatedString = StringDatabase.getUniqueInstance().getString(prefixedKey);
        }
        if (preformatedString == null) {
            preformatedString = StringDatabase.getUniqueInstance().getNullableString(stringKey);
        }
        if (preformatedString == null) {
            return null;
        }
        Map<String, Object> fieldsAndValues = new HashMap<>();
        var classForFields = openMarkovException.getClass();
        while (classForFields != IOpenMarkovException.class) {
            Arrays.stream(classForFields.getDeclaredFields()).forEach(field -> {
                try {
                    field.setAccessible(true);
                    if (!fieldsAndValues.containsKey(field.getName())) {
                        fieldsAndValues.put(field.getName(), field.get(openMarkovException));
                    }
                } catch (IllegalAccessException ex) {
                    LOGGER.warn(ex);
                }
            });
            classForFields = (Class<? extends IOpenMarkovException>) classForFields.getSuperclass();
        }
        return StringFormat.apply(preformatedString, fieldsAndValues);
    }
    
    /**
     * Returns a string to represent the Exception, and the format depends on the nullability of the Title and Message:
     * <ul>
     *  <li>If both title and message are present: "ClassName: Title\nMessage".</li>
     *  <li>If only the title is present: "ClassName: Title".</li>
     *  <li>If only the message is present: "ClassName: Message".</li>
     *  <li>If neither is present: "ClassName".</li>
     * </ul>
     *
     * @return A string representation of the exception including the class name, exception title, and/or exception
     * message, depending on nullability.
     */
    @SuppressWarnings({"ConstantValue"})
    static @NotNull String toString(IOpenMarkovException exception) {
        String className = exception.getClass().getName();
        String title = exception.getExceptionTitle();
        String message = exception.getExceptionMessage();
        if (title != null) {
            title = title.replace("\\n", System.lineSeparator());
        }
        if (message != null) {
            message = message.replace("\\n", System.lineSeparator());
        }
        //Title: Present, Message: Present
        if (title != null && message != null) {
            return className + ": " + title + System.lineSeparator() + message;
        }
        //Title: Present, Message: Null
        if (title != null && message == null) {
            return className + ": " + title;
        }
        //Title: Null,    Message: Present
        if (title == null && message != null) {
            return className + ": " + message;
        }
        //Title: Null,    Message: Null
        return className;
    }
    
    
    /**
     * Utility class that simplifies searching for exception bundles.
     */
    //endregion
    class BundleSearch {
        public static final String BUNDLE_SUFFIX_TITLE = ".title";
        public static final String BUNDLE_SUFFIX_MESSAGE = ".message";
        
        /**
         * A list containing modules and their corresponding exception bundles.
         */
        private static final List<ExceptionBundleInfo> MODULES_TO_EXCEPTIONS_BUNDLES;
        
        static {
            List<ExceptionBundleInfo> modulesToBundles = Arrays.asList(
                    new ExceptionBundleInfo("org.openmarkov.sensitivityanalysis", "sensitivityanalysisexceptions", ""),
                    new ExceptionBundleInfo("org.openmarkov.stochasticPropagationOutput", "stochasticpropagationoutput", "exception.")
            );
            Comparator<ExceptionBundleInfo> dotsComparator = Comparator.comparingInt(bundleInfo -> BundleSearch.countOccurrencesOfCharacter(bundleInfo.module, '.'));
            modulesToBundles.sort(dotsComparator.reversed());
            MODULES_TO_EXCEPTIONS_BUNDLES = Collections.unmodifiableList(modulesToBundles);
        }
        
        /**
         * Find the ExceptionBundle that further matches this exception class.
         *
         * @param exceptionClass the class of the exception
         *
         * @return the ExceptionBundle that further matches this exception class.
         */
        static Optional<ExceptionBundleInfo> findExceptionBundle(Class<? extends IOpenMarkovException> exceptionClass) {
            String exceptionClassName = exceptionClass.getName();
            return BundleSearch.MODULES_TO_EXCEPTIONS_BUNDLES
                    .stream()
                    .filter(moduleAndBundleName -> exceptionClassName.startsWith(moduleAndBundleName.module()))
                    .findFirst();
        }
        
        /**
         * Counts the occurrences of a specific character in a given character sequence.
         *
         * @param charSequence the character sequence in which to count occurrences
         * @param c            the character whose occurrences are to be counted
         *
         * @return the number of times the specified character appears in the character sequence
         */
        @SuppressWarnings("SameParameterValue")
        private static int countOccurrencesOfCharacter(CharSequence charSequence, char c) {
            return (int) IntStream.range(0, charSequence.length())
                                  .filter(characterIndex -> charSequence.charAt(characterIndex) == c)
                                  .count();
        }
        
        /**
         * Represents information related to bundle that should have localizations for exceptions.
         *
         * @param module     The name of the module where the exception bundle is born of.
         * @param bundleName The name of the resource bundle containing the exception bundle.
         * @param prefixKey  A prefix used when resolving keys.
         */
        record ExceptionBundleInfo(String module, String bundleName, String prefixKey) {
        }
    }
    
    @Override @NotNull default String path() {
        return this.getClass().getName();
    }

    @Override @NotNull default String localize(LocalizationFormatter formatter) {
        return this.getExceptionTitle() + ": " + this.getExceptionMessage();
    }
    
    
}
