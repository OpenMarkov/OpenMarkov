/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A ProbModelXML file whose network the reader cannot find, because there is no ProbNet element or because its
 * elements are in an XML namespace, is rejected with a message that says so (error 394).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFileWithoutAReadableNetworkIsRejectedSayingSoTest {

    private static final String WITHOUT_NETWORK = "(?s)\\s*<ProbNet .*</ProbNet>";
    private static final String NETWORK = "(?s)<ProbNet (.*)</ProbNet>";
    private static final String END = "</ProbModelXML>";
    private static final String OPTIONS = "<InferenceOptions><MulticriteriaOptions><SelectedAnalysisType>UNICRITERION"
                                          + "</SelectedAnalysisType></MulticriteriaOptions></InferenceOptions>" + END;
    private static final String ROOT = "<ProbModelXML ";
    private static final String NAMESPACE = "http://www.cisiad.uned.es/ProbModelXML";

    @TempDir Path directory;

    /** A copy of a test network with, for each pair of strings, the first match of a regex replaced. */
    private URL copyOf(String network, String... regexesAndReplacements) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(network).toURI()));
        for (int i = 0; i < regexesAndReplacements.length; i += 2) {
            String regex = regexesAndReplacements[i];
            assertTrue(Pattern.compile(regex).matcher(text).find(), network + " does not contain " + regex);
            text = text.replaceFirst(regex, regexesAndReplacements[i + 1]);
        }
        return Files.writeString(directory.resolve(network), text).toUri().toURL();
    }

    private static Object open(URL url) throws Exception {
        return FormatManager.getInstance().getProbNetReader(url).read(url);
    }

    private static String messageOpening(URL url) {
        OpenMarkovException exception = assertThrows(PGMXParserException.class, () -> open(url));
        return exception.getExceptionMessage();
    }

    private static String withoutNetwork(URL url) {
        return "File " + url.getFile() + " has no network: its root element has no ProbNet element.";
    }

    private static String inANamespace(URL url) {
        return "File " + url.getFile() + " cannot be read: its elements are in the XML namespace " + NAMESPACE
               + ", and ProbModelXML elements have no namespace.";
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void theUnchangedFileIsRead(String network) throws Exception {
        assertNotNull(open(copyOf(network)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileWithoutProbNet(String network) throws Exception {
        URL url = copyOf(network, WITHOUT_NETWORK, "");
        assertEquals(withoutNetwork(url), messageOpening(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileWithoutProbNetButWithInferenceOptions(String network) throws Exception {
        URL url = copyOf(network, WITHOUT_NETWORK, "", END, OPTIONS);
        assertEquals(withoutNetwork(url), messageOpening(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileWithProbnetWrittenInOtherCase(String network) throws Exception {
        URL url = copyOf(network, NETWORK, "<Probnet $1</Probnet>");
        assertEquals(withoutNetwork(url), messageOpening(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileInANamespace(String network) throws Exception {
        URL url = copyOf(network, ROOT, ROOT + "xmlns=\"" + NAMESPACE + "\" ");
        assertEquals(inANamespace(url), messageOpening(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileInANamespaceWithInferenceOptions(String network) throws Exception {
        URL url = copyOf(network, ROOT, ROOT + "xmlns=\"" + NAMESPACE + "\" ", END, OPTIONS);
        assertEquals(inANamespace(url), messageOpening(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MID-mammography.pgmx", "BN-conditional-gaussian.pgmx"})
    void aFileThatOnlyDeclaresAPrefixIsRead(String network) throws Exception {
        assertNotNull(open(copyOf(network, ROOT, ROOT + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")));
    }
}
