package org.openmarkov.core.localize.spi;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.localize.*;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.spi.ResourceBundleProvider;

/**
 * This interfaces allows for individual repositories with XML localizations files to be automatically loaded in
 * OpenMarkov's Core's {@link StringDatabase}.
 * <p>
 * This means a repository that wants to use XML localizations files only needs to implement this class somewhere in the
 * project and allow this class to be exported in the {@code module-info.java}.
 * <p>
 * The implementor class it is advised to be located at a package named {@code localize}, and it only needs to override
 * the {@link LocalizeResourcesProvider#getRootOfResources()} specifying where the directory containing the localization
 * directory is located at (Yes, you read that right, it specifies the directory containing the {@code localize}
 * directory, not the {@code localize} directory itself).
 * <p>
 * For example, {@code org.openmarkov.learning.gui} has its localization files at
 * {@code src/main/resources/learning/gui/localize}, so it creates a class implementing this interface to allow
 * OpenMarkov's Core to load the XML localizations files it has inside it.
 * <p>
 * Following the instructions said here, there should be a package called {@code localize}, since the root of the java
 * files is {@code org.openmarkov.learning.gui}, then the package is {@code org.openmarkov.learning.gui.localize}, and
 * inside there you will find a class that implements LocalizeResourceProvider telling the source of the unique resource
 * of this repository that has the {@code localize} directory, this is {@code src/main/resources/learning/gui}, but
 * its only needed to write the part after {@code resources}, so we write {@code /learning/gui}, meaning this is how it
 * is implemented:
 *
 * <blockquote><pre>
 * {@code
 * package org.openmarkov.learning.gui.localize;
 *
 * import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
 *
 * public class LearningGUIResourceBundleProvider implements LocalizeResourcesProvider {
 *     @Override public String getRootOfResources() {
 *         return "/learning/gui"; //This points to src/main/resources/learning/gui/localize
 *                                                                    /////////////
 *     }
 * }
 * }
 * </pre></blockquote><br>
 * <p>
 * And the {@code module-info.java} looks like this:
 *
 * <blockquote><pre>
 * {@code
 * module org.openmarkov.learning.gui {
 *     //...
 *     exports org.openmarkov.learning.gui.localize;
 *     //...
 * }
 * }
 * </pre></blockquote><br>
 *
 * @author mluque, jrico
 */
@SuppressWarnings({"DuplicateStringLiteralInspection", "InterfaceMayBeAnnotatedFunctional"})
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public interface LocalizeResourcesProvider extends ResourceBundleProvider {
    
    String URL_PROTOCOL_JAR_FILE = "jar:file:/";
    String URL_PROTOCOL_FILE = "file:/";
    
    /**
     * Executes {@code addBundleSource} over every file of {@code bundleFile} if it is a directory, or over it if it is
     * a file.
     * <p>
     * Since {@code addBundleSource} is a function that processes how a Bundle is added, this means it adds every bundle
     * source.
     */
    private static void addBundlesOfFile(@NotNull URL bundleFile, @NotNull Consumer<? super BundleSource> addBundleSource) {
        File localizationFile;
        try {
            localizationFile = new File(bundleFile.toURI());
        } catch (URISyntaxException e) {
            throw new UnreachableException("Localization file " + bundleFile + " could not be located.", e);
        }
        if (localizationFile.isFile()) {
            addBundleSource.accept(new BundleSource.FileSource(localizationFile));
        } else {
            try (var files = Files.walk(localizationFile.toPath())) {
                files.filter(file -> file.toFile().isFile())
                     .map(file -> (BundleSource) new BundleSource.FileSource(file.toFile()))
                     .forEach(addBundleSource);
            } catch (IOException ioException) {
                throw new UnreachableException("Localization file " + bundleFile + " could not be accesed, while it was previously used.", ioException);
            }
        }
    }
    
    /**
     * Executes {@code addBundleSource} over every file starting with the same name as the entry of {@code bundleFile}.
     * <p>
     * Since {@code addBundleSource} is a function that processes how a Bundle is added, this means it adds every bundle
     * source.
     */
    private static void addBundlesOfJar(@NotNull URL bundleFile, @NotNull Consumer<? super BundleSource> addBundleSource) {
        String fileName = bundleFile.toString().substring("jar:file:".length());
        int entrySeparatorIndex = fileName.indexOf('!');
        try (JarFile jarFile = new JarFile(fileName.substring(0, entrySeparatorIndex))) {
            String askedEntries = fileName.substring(entrySeparatorIndex + 2);
            Collections.list(jarFile.entries()).stream()
                       .filter(entry -> !entry.isDirectory())
                       .filter(entry -> entry.getName().startsWith(askedEntries))
                       .map(entry -> new BundleSource.JarSource(jarFile, entry))
                       .forEach(addBundleSource);
        } catch (IOException ioException) {
            OpenMarkovLogger.LOGGER.debug("Localization file in jar " + bundleFile + " could not be located.", ioException);
        }
    }
    
    /**
     * Processes a class localization file into a {@link StringBundle}.
     *
     * @param inputStream the input stream containing the XML fill.
     * @return a {@link StringBundle} containing the extracted localizations.
     */
    private static @Nullable RawStringBundle classLocalizationsFileToBundle(@NotNull InputStream inputStream) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            var keysAndLocalizations = new HashMap<String, String>();
            parser.parse(inputStream, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("ClassLocalization".equalsIgnoreCase(qName) || "Localization".equalsIgnoreCase(qName)) {
                        super.startElement(uri, localName, qName, attributes);
                        String className = attributes.getValue("class");
                        String defaultTranslation = attributes.getValue("value");
                        var lengthsAndTranslations = Arrays.stream(LocalizationFormatter.LocalizationFormatterLength.values())
                                                           .filter(length -> length != LocalizationFormatter.LocalizationFormatterLength.UNSPECIFIED)
                                                           .map(length -> Pair.of(length, attributes.getValue(length.toString()
                                                                                                                    .toLowerCase())))
                                                           .filter(pair -> pair.getRight() != null)
                                                           .toList();
                        if (defaultTranslation == null) {
                            var shortestLength = lengthsAndTranslations.stream().findFirst();
                            if (shortestLength.isPresent()) {
                                defaultTranslation = shortestLength.get().getRight();
                            }
                        }
                        keysAndLocalizations.put(className, defaultTranslation);
                        lengthsAndTranslations.forEach(
                                pair -> keysAndLocalizations.put(className + "." + pair.getLeft()
                                                                                       .toString()
                                                                                       .toLowerCase(), pair.getRight()));
                    }
                }
            });
            return new RawStringBundle(keysAndLocalizations);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
        
    }
    
    /**
     * Retrieves the tag name of the first XML element found in the provided file's {@link InputStream}.
     * <p>
     * It returns null if the XML file isn't valid, or if the file isn't a XML.
     *
     * @param inputStream the input stream containing the XML data
     * @return the tag name of the first XML element.
     * @throws IOException if an I/O error occurs while processing the input stream.
     */
    static @Nullable String getFirstElementTagName(@NotNull InputStream inputStream) throws IOException {
        try {
            final String[] firstElement = new String[1];
            SAXParserFactory.newInstance().newSAXParser().parse(inputStream, new DefaultHandler() {
                private boolean firstElementFound = false;
                
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if (!this.firstElementFound) {
                        firstElement[0] = qName;
                        this.firstElementFound = true;
                    }
                }
            });
            return firstElement[0];
        } catch (SAXException | ParserConfigurationException e) {
            return null;
        }
    }
    
    /**
     * Gets the root of the individual resources' directory.
     * <p>
     * This is different from the root of {@code resources}, but rather a unique directory inside {@code resources},
     * for example, inside {@code gui}, the unique resource directory is located at {@code src/main/resources/gui}, so
     * its unique root is {@code /gui}.
     * <p>
     * The root should be indicated with {@code /} at its beginning to indicate the absolute path from the
     * {@code src/main/resources} directory, this means returning {@code /gui} was valid, but returning {@code gui}
     * shouldn't, as then the path will be relative from the class that implements this interface.
     *
     * @return root of the individual resources' directory.
     */
    @NotNull String getRootOfResources();
    
    // Override to avoid other implementors to just implementing returning null, as this method is never used nor was
    // ever implemented.
    @Override
    default @Nullable ResourceBundle getBundle(String baseName, Locale locale) {
        return null;
    }
    
    /**
     * Gets the localization bundles matching a certain locale.
     * <p>
     * This is done by reading the contents of {@code getRootOfResources()} + {@code /localize}, so in
     * {@code org.openmarkov.gui}, whose {@code getRootOfResources()} is {@code /gui}, it searches for localization
     * files in {@code src/main/resources/gui/localize}, meaning said directory should have XML files with localization
     * contents.
     * <p>
     * The localization files are checked against the locale's code, so for example, if you use the
     * {@link Locale#ENGLISH}, then this bundle will be filled just with those XML localization files that end with
     * {@code _en.xml}, like {@code Dialogs_en.xml} or {@code Buttons_en.xml}, but not {@code Dialogs_es.xml}.
     *
     * @return localization bundles matching a certain locale.
     */
    default Map<String, StringBundle> getBundlesMap(Locale locale) {
        Map<String, StringBundle> bundles = new LinkedHashMap<>();
        URL localizationResourcesURL = this.getClass().getResource(this.getRootOfResources() + "/localize");
        String localizationSuffix = "_" + locale.getLanguage() + ".xml";
        if (localizationResourcesURL == null) {
            System.err.println("There is no localize folder in the directory " + this.getRootOfResources() +
                                       " of module " + this.getClass().getModule().getName());
            return Map.of();
        }
        boolean isJarFile = localizationResourcesURL.toString()
                                                    .startsWith(LocalizeResourcesProvider.URL_PROTOCOL_JAR_FILE);
        boolean isFile = localizationResourcesURL.toString()
                                                 .startsWith(LocalizeResourcesProvider.URL_PROTOCOL_FILE);
        Consumer<BundleSource> addBundleSource = source -> {
            if (!source.fileName().endsWith(localizationSuffix))
                return;
            try {
                String firstElementTagName = LocalizeResourcesProvider.getFirstElementTagName(source.inputStream());
                String baseName = source.fileName()
                                        .substring(0, source.fileName().length() - localizationSuffix.length());
                StringBundle stringBundle;
                if ("ClassLocalizations".equalsIgnoreCase(firstElementTagName)) {
                    stringBundle = LocalizeResourcesProvider.classLocalizationsFileToBundle(source.inputStream());
                } else {
                    stringBundle = new XMLStringBundle(new XMLResourceBundle(source.inputStream()));
                }
                if (stringBundle != null) {
                    bundles.put(baseName, stringBundle);
                }
            } catch (IOException ignored) {
            }
        };
        if (isJarFile) {
            LocalizeResourcesProvider.addBundlesOfJar(localizationResourcesURL, addBundleSource);
        } else if (isFile) {
            LocalizeResourcesProvider.addBundlesOfFile(localizationResourcesURL, addBundleSource);
        }
        return bundles;
    }
    
    /**
     * Represents different sources where a Bundle can come from, currently there are sources for:
     * <p>
     * - Raw files, represented by {@link FileSource}, these are mostly used when developing, as classes and resource
     * files are taken from the {@code target} directory.
     * - Jar files, represented by {@link JarSource}, only one of this is taken and only when generating the final .jar
     * that is created for production use; This .jar contains everything in the project, and this includes the XML
     * localization files we read here.
     */
    sealed abstract class BundleSource permits BundleSource.FileSource, BundleSource.JarSource {
        
        /**
         * Opens an {@link InputStream} for accessing the contents of this source.
         *
         * @return an {@link InputStream} for accessing the contents of this source.
         * @throws IOException Happens when the file doesn't exist or any other kind of I/O error.
         */
        abstract @NotNull InputStream inputStream() throws IOException;
        
        /**
         * Gets the name of the file including its extension.
         *
         * @return the name of the file including its extension.
         */
        abstract @NotNull String fileName();
        
        /**
         * Represents the source represented by a single {@link File}.
         */
        private static final class FileSource extends BundleSource {
            private final @NotNull File file;
            
            /**
             * Basic constructor giving {@link File} {@code file}.
             */
            private FileSource(@NotNull File file) {
                this.file = file;
            }
            
            @Override @NotNull InputStream inputStream() throws FileNotFoundException {
                return new FileInputStream(this.file);
            }
            
            @Override @NotNull String fileName() {
                return this.file.getName();
            }
        }
        
        /**
         * Represents the source of a JarEntry as both its {@link JarEntry} and the {@link JarFile} that contains it.
         */
        private static final class JarSource extends BundleSource {
            private final @NotNull JarFile jarFile;
            private final @NotNull JarEntry jarEntry;
            
            /**
             * Basic constructor giving both {@link JarFile} {@code jarFile} and {@link JarEntry} {@code jarEntry}.
             */
            private JarSource(@NotNull JarFile jarFile, @NotNull JarEntry jarEntry) {
                this.jarFile = jarFile;
                this.jarEntry = jarEntry;
            }
            
            @Override @NotNull InputStream inputStream() throws IOException {
                return this.jarFile.getInputStream(this.jarEntry);
            }
            
            @Override @NotNull String fileName() {
                var lastSeparatorIndex = this.jarEntry.getName().lastIndexOf('/');
                return this.jarEntry.getName().substring(lastSeparatorIndex + 1);
            }
        }
    }
    
}
