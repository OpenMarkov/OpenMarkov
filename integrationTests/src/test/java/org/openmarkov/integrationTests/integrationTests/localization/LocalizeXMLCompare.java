/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.integrationTests.localization;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.openmarkov.plugin.PluginSearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Localization is no longer applied to multiple languages, just English")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LocalizeXMLCompare {
    
    private static final int LANGUAGE_CODE_PLUS_EXTENSION_LENGHT = 7;
    private static final Language LANGUAGE_ENGLISH = new Language("English", "_en.xml", "en");
    
    private static Stream<Language> languages() {
        return Stream.of(
                LANGUAGE_ENGLISH,
                new Language("Spanish", "_es.xml", "es")
        );
    }
    
    private static Stream<? extends LocalizeResourcesProvider> getLocalizationProviders() {
        return PluginSearch.init()
                           .childrenOf(LocalizeResourcesProvider.class)
                           .stream()
                           .map(localizeClass -> {
                               try {
                                   return localizeClass.getDeclaredConstructor().newInstance();
                               } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                                        InvocationTargetException e) {
                                   return null;
                               }
                           })
                           .filter(Objects::nonNull);
    }
    
    private static Stream<LocalizeResourceAndFiles> getLocalizationProvidersAndFiles() {
        return getLocalizationProviders().map(localizeResourcesProvider -> {
            var localizeDir = localizeResourcesProvider.getClass().getResource(
                    localizeResourcesProvider.getRootOfResources() + "/localize");
            File[] localizeDirFiles = new File(localizeDir.getFile().substring(1)).listFiles();
            return new LocalizeResourceAndFiles(localizeResourcesProvider, Arrays.stream(localizeDirFiles)
                                                                                 .map(File::getName)
                                                                                 .filter(file -> file.toLowerCase()
                                                                                                     .endsWith("xml"))
                                                                                 //Removes extension from file
                                                                                 .map(file -> file.substring(0, file.length() - LocalizeXMLCompare.LANGUAGE_CODE_PLUS_EXTENSION_LENGHT))
                                                                                 .distinct());
        });
    }
    
    private static Stream<FileAndLocalizeResource> getLocalizationFilesAndProviders() {
        return getLocalizationProvidersAndFiles().flatMap(localizeResourceAndFiles ->
                                                                  localizeResourceAndFiles.filesNameStream.map(file ->
                                                                                                                       new FileAndLocalizeResource(file, localizeResourceAndFiles.localizeResourcesProvider))
        );
    }
    
    private static Stream<Pair<FileAndLocalizeResource, Language>> getLocalizationFilesAndProvidersWithLanguages() {
        var languages = LocalizeXMLCompare.languages()
                                          .filter(language -> language != LocalizeXMLCompare.LANGUAGE_ENGLISH)
                                          .toList();
        if (languages.isEmpty()) {
            return Stream.empty();
        }
        return getLocalizationFilesAndProviders().flatMap(fileAndLocalizeResource ->
                                                                  languages.stream()
                                                                           .map(language -> Pair.of(fileAndLocalizeResource, language)));
    }
    
    private static @Nullable InputStream fileToInputStream(LocalizeResourcesProvider resourceBundleProvider, String localizationFile) {
        String absoluteLocalizationFile = resourceBundleProvider.getRootOfResources() + "/localize/" + localizationFile;
        return resourceBundleProvider.getClass().getResourceAsStream(absoluteLocalizationFile);
    }
    
    private static @NotNull Set<String> getKeysForLanguage(String bundleName, Language language) {
        StringDatabase.getUniqueInstance().setLanguage(language.languageCode);
        var fileBundle = StringDatabase.getUniqueInstance().getAllBundles().get(bundleName);
        assertNotNull(fileBundle, "Could not find " + language.description.toLowerCase() + " stringBundle for " + fileBundle);
        return new HashSet<>(fileBundle.getKeys());
    }
    
    @ParameterizedTest
    @MethodSource("getLocalizationFilesAndProviders")
    public void checkSameFiles(FileAndLocalizeResource fileAndLocalizeResource) {
        var languages = LocalizeXMLCompare.languages();
        var missingLanguages = languages.filter(language ->
                                                        fileToInputStream(fileAndLocalizeResource.localizeResourcesProvider, fileAndLocalizeResource.file + language.suffix) == null)
                                        .toList();
        if (!missingLanguages.isEmpty()) {
            String missingLanguagesStream = missingLanguages.stream()
                                                            .map(Language::description)
                                                            .collect(Collectors.joining(", "));
            fail("The file " + fileAndLocalizeResource.file + " of " + fileAndLocalizeResource.localizeResourcesProvider.getClass()
                                                                                                                                       .getName() + " is missing in languages: " + missingLanguagesStream);
        }
    }
    
    @ParameterizedTest
    @MethodSource("getLocalizationFilesAndProvidersWithLanguages")
    public void checkSameStructure(Pair<FileAndLocalizeResource, Language> fileAndLanguage) throws IOException, JDOMException {
        var provider = fileAndLanguage.getLeft().localizeResourcesProvider;
        var file = fileAndLanguage.getLeft().file;
        var language = fileAndLanguage.getRight();
        checkStructure(provider, file + LocalizeXMLCompare.LANGUAGE_ENGLISH.suffix, file + language.suffix);
    }
    
    @ParameterizedTest
    @MethodSource("getLocalizationFilesAndProvidersWithLanguages")
    public void checkSameValues(Pair<FileAndLocalizeResource, Language> fileAndLanguage) {
        var file = fileAndLanguage.getLeft().file;
        var language = fileAndLanguage.getRight();
        
        var englishKeys = getKeysForLanguage(file, LANGUAGE_ENGLISH);
        var localLanguageKeys = getKeysForLanguage(file, language);
        var missingKeysInLocalLanguage = englishKeys.stream()
                                                    .filter(key -> !localLanguageKeys.contains(key))
                                                    .sorted()
                                                    .collect(Collectors.joining(", "));
        var missingKeysInEnglishLanguage = localLanguageKeys.stream()
                                                            .filter(key -> !englishKeys.contains(key))
                                                            .sorted()
                                                            .collect(Collectors.joining(", "));
        if (!missingKeysInEnglishLanguage.isEmpty() || !missingKeysInLocalLanguage.isEmpty()) {
            fail(System.lineSeparator() + "Missing keys in " + LANGUAGE_ENGLISH.description + ": " + missingKeysInEnglishLanguage + System.lineSeparator() +
                                        "Missing keys in " + language.description + ": " + missingKeysInLocalLanguage
            );
        }
    }
    
    private void checkStructure(LocalizeResourcesProvider localizeResourcesProvider, String englishXML, String spanishXML) throws IOException, JDOMException {
        Document englishXMLDocument = getXMLDocument(localizeResourcesProvider, englishXML);
        Document spanishXMLDocument = getXMLDocument(localizeResourcesProvider, spanishXML);
        Element rootEn = englishXMLDocument.getRootElement();
        Element rootEs = spanishXMLDocument.getRootElement();
        checkElements(rootEn, rootEs);
    }
    
    private void checkElements(Element rootEn, Element rootEs) {
        
        if (!rootEn.getName().equals(rootEs.getName())) {
            fail(
                    ". The XML labels are not the same, as '" + rootEn.getName() + "' != '" + rootEs.getName() + "'.");
        }
        
        if (rootEn.getChildren().size() != rootEs.getChildren().size()) {
            fail(
                    ". The XML label '" + rootEn.getName() + "' have a different number of children " + rootEn
                            .getChildren().size() + " != " + rootEs.getChildren().size());
        }
        
        for (int i = 0; i < rootEn.getChildren().size(); i++) {
            Element childEN = rootEn.getChildren().get(i);
            Element childES = rootEs.getChildren().get(i);
            checkElements(childEN, childES);
        }
    }
    
    private Document getXMLDocument(LocalizeResourcesProvider localizeResourcesProvider, String xmlDocument) throws IOException, JDOMException {
        // Get file if not included.
        InputStream stream = fileToInputStream(localizeResourcesProvider, xmlDocument);
        
        // Get root element.
        SAXBuilder builder = new SAXBuilder();
        builder.setJDOMFactory(new LocatedJDOMFactory());
        
        Document document = builder.build(stream);
        
        
        return document;
    }
    
    record LocalizeResourceAndFiles(LocalizeResourcesProvider localizeResourcesProvider,
                                    Stream<String> filesNameStream) {
    }
    
    record FileAndLocalizeResource(String file, LocalizeResourcesProvider localizeResourcesProvider) {
    }
    
    record Language(String description, String suffix, String languageCode) {
    }
}
