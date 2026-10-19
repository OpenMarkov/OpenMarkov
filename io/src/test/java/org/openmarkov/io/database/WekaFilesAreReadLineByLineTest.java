/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.io.database.weka.ArffDataBaseIO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file of Weka is read one case per line, whatever characters its names have (errors 118, 119, 121 and 123).
 *
 * @author Manuel Arias
 */
class WekaFilesAreReadLineByLineTest {

    private static final String HEADER = """
            @relation test
            @attribute X {a,b}
            @attribute Y {c,d}
            @attribute Z {e,f}
            @data
            """;

    private static File file(String content, Charset charset) throws IOException {
        Path file = Files.createTempDirectory("weka").resolve("cases.arff");
        Files.writeString(file, content, charset);
        file.toFile().deleteOnExit();
        return file.toFile();
    }

    private static CaseDatabase read(String content) throws Exception {
        return new ArffDataBaseIO().load(file(content, StandardCharsets.UTF_8));
    }

    private static List<String> states(Variable variable) {
        return Arrays.stream(variable.getStates()).map(State::getName).toList();
    }

    /** The message of the refusal, which names the line. */
    private static String refusal(String content) throws IOException {
        File file = file(content, StandardCharsets.UTF_8);
        ParsingSourceException.CouldNotParseSourceException thrown = assertThrows(
                ParsingSourceException.CouldNotParseSourceException.class, () -> new ArffDataBaseIO().load(file));
        return thrown.originException.getMessage();
    }

    @Test
    void anUnknownValueIsAStateOfItsColumnAndEachLineIsACase() throws Exception {
        CaseDatabase database = read(HEADER + "a,c,e\na,?,e\nb,d,f\nb,d,e\n");

        assertEquals(List.of("c", "d", "?"), states(database.getVariables().get(1)));
        assertArrayEquals(new int[][]{{0, 0, 0}, {0, 2, 0}, {1, 1, 1}, {1, 1, 0}}, database.getCases());
    }

    @Test
    void anUnknownValueInTheLastColumnLosesNoCase() throws Exception {
        CaseDatabase database = read(HEADER + "a,c,e\na,c,?\nb,d,f\n");

        assertArrayEquals(new int[][]{{0, 0, 0}, {0, 0, 2}, {1, 1, 1}}, database.getCases());
    }

    @Test
    void aValueThatItsColumnDoesNotDeclareIsRefused() throws Exception {
        String message = refusal(HEADER + "a,c,e\na,g,e\n");

        assertTrue(message.contains("Line 7") && message.contains("«g»") && message.contains("«Y»"), message);
    }

    /** Whatever the lines before: an unknown value of an earlier case does not declare it (error 249). */
    @Test
    void aQuotedQuestionMarkThatItsColumnDoesNotDeclareIsRefused() throws Exception {
        String alone = refusal(HEADER + "a,'?',e\n");
        String afterAnUnknownValue = refusal(HEADER + "a,?,e\na,'?',e\n");

        assertTrue(alone.contains("Line 6") && alone.contains("«?»") && alone.contains("«Y»"), alone);
        assertTrue(afterAnUnknownValue.contains("Line 7") && afterAnUnknownValue.contains("«?»")
                   && afterAnUnknownValue.contains("[c, d]"), afterAnUnknownValue);
    }

    @Test
    void aQuotedQuestionMarkThatItsColumnDeclaresIsRead() throws Exception {
        CaseDatabase database = read("""
                @relation test
                @attribute X {a,b}
                @attribute Y {c,d,'?'}
                @data
                a,'?'
                b,?
                """);

        assertEquals(List.of("c", "d", "?"), states(database.getVariables().get(1)));
        assertArrayEquals(new int[][]{{0, 2}, {1, 2}}, database.getCases());
    }

    @Test
    void aLineWithTooFewValuesIsRefused() throws Exception {
        assertTrue(refusal(HEADER + "a,c,e\na,c\n").contains("Line 7"));
    }

    @Test
    void namesWithAccentsAndEñeKeepAllTheirLetters() throws Exception {
        CaseDatabase database = read("""
                @relation vehículos
                @attribute vehículo {camión,coche}
                @attribute conductor {niño,adulto}
                @data
                camión,adulto
                coche,niño
                """);

        assertEquals("vehículo", database.getVariables().getFirst().getName());
        assertEquals(List.of("camión", "coche"), states(database.getVariables().get(0)));
        assertEquals(List.of("niño", "adulto"), states(database.getVariables().get(1)));
        assertArrayEquals(new int[][]{{0, 1}, {1, 0}}, database.getCases());
    }

    /** Files written before in ISO-8859-1, which the reader of before also read. */
    @Test
    void aFileInIso88591IsStillRead() throws Exception {
        File file = file("@relation r\n@attribute vehículo {camión,coche}\n@data\ncamión\n", StandardCharsets.ISO_8859_1);

        CaseDatabase database = new ArffDataBaseIO().load(file);

        assertEquals(List.of("camión", "coche"), states(database.getVariables().getFirst()));
        assertArrayEquals(new int[][]{{0}}, database.getCases());
    }

    @Test
    void theKeywordsMayBeWrittenInAnyCase() throws Exception {
        CaseDatabase database = read("@Relation r\n@Attribute X {a,b}\n@ATTRIBUTE Y {c,d}\n@Data\nb,c\n");

        assertEquals(2, database.getVariables().size());
        assertArrayEquals(new int[][]{{1, 0}}, database.getCases());
    }

    @Test
    void aHeaderThatCannotBeReadIsRefused() throws Exception {
        assertTrue(refusal("@relation r\nX {a,b}\n@data\na\n").contains("Line 2"));
    }

    /** Which columns can be read is a decision still to be taken (error 120): until then, they are refused. */
    @Test
    void aColumnOfNumbersIsRefusedNamingIt() throws Exception {
        String message = refusal("@relation r\n@attribute age numeric\n@data\n32\n");

        assertTrue(message.contains("«age»"), message);
    }

    @Test
    void commentsQuotesAndBlanksAreUnderstood() throws Exception {
        CaseDatabase database = read("""
                % A comment
                @relation 'C:\\Users\\Ana\\Datos ñ+á\\casos'

                @attribute 'Smoker?' {'yes, a lot', no}
                @attribute "Rate 50%" { low , high }
                @data
                % Another comment
                'yes, a lot' , high
                no,'low'
                """);

        assertEquals(List.of("Smoker?", "Rate 50%"), database.getVariables().stream().map(Variable::getName).toList());
        assertEquals(List.of("yes, a lot", "no"), states(database.getVariables().getFirst()));
        assertArrayEquals(new int[][]{{0, 1}, {1, 0}}, database.getCases());
    }

    /** The sample file that Weka comes with, «weather.nominal.arff». */
    @Test
    void theNominalWeatherOfWekaIsRead() throws Exception {
        CaseDatabase database = read("""
                @relation weather.symbolic

                @attribute outlook {sunny, overcast, rainy}
                @attribute temperature {hot, mild, cool}
                @attribute humidity {high, normal}
                @attribute windy {TRUE, FALSE}
                @attribute play {yes, no}

                @data
                sunny,hot,high,FALSE,no
                sunny,hot,high,TRUE,no
                overcast,hot,high,FALSE,yes
                rainy,mild,high,FALSE,yes
                rainy,cool,normal,FALSE,yes
                rainy,cool,normal,TRUE,no
                overcast,cool,normal,TRUE,yes
                sunny,mild,high,FALSE,no
                sunny,cool,normal,FALSE,yes
                rainy,mild,normal,FALSE,yes
                sunny,mild,normal,TRUE,yes
                overcast,mild,high,TRUE,yes
                overcast,hot,normal,FALSE,yes
                rainy,mild,high,TRUE,no
                """);

        assertEquals(5, database.getVariables().size());
        assertEquals(14, database.getCases().length);
        assertArrayEquals(new int[]{2, 1, 0, 0, 1}, database.getCases()[13]);
    }

    /** OpenMarkov writes the folder in the name of the relation. */
    @Test
    void aDatabaseSavedInAFolderWithTildeEñeAndPlusIsReadBack() throws Exception {
        Path folder = Files.createTempDirectory("carpeta ñ+á");
        File file = folder.resolve("casos.arff").toFile();
        file.deleteOnExit();
        CaseDatabase saved = new CaseDatabase(List.of(new Variable("X", "a", "b"), new Variable("Y", "c", "d")),
                                              new int[][]{{0, 1}, {1, 0}, {1, 1}});

        new ArffDataBaseIO().save(file, saved);
        CaseDatabase read = new ArffDataBaseIO().load(file);

        assertEquals(2, read.getVariables().size());
        assertArrayEquals(saved.getCases(), read.getCases());
        assertFalse(states(read.getVariables().getFirst()).contains("?"));
    }
}
