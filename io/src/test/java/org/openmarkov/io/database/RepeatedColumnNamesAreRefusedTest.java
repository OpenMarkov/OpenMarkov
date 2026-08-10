/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.io.database.excel.CSVDataBaseIO;
import org.openmarkov.io.database.weka.ArffDataBaseIO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Two columns of the same name leave a case with no way of saying which one it means: the value of
 * one of them would be dropped when the case is evaluated. The file is refused when it is read.
 *
 * @author Manuel Arias
 */
class RepeatedColumnNamesAreRefusedTest {

	private static File fileWith(String name, String content) throws IOException {
		Path file = Files.createTempDirectory("repeatedColumns").resolve(name);
		Files.writeString(file, content, StandardCharsets.UTF_8);
		return file.toFile();
	}

	@Test
	void aCommaSeparatedFileWithTwoColumnsOfTheSameNameIsRefused() throws IOException {
		File file = fileWith("repeated.csv", "A,B,A\na1,b1,a2\na1,b1,a1\n");

		ParsingSourceException.RepeatedVariableNames thrown = assertThrows(
				ParsingSourceException.RepeatedVariableNames.class,
				() -> new CSVDataBaseIO().load(file),
				"The file was read, and one of the two columns would be lost without a word");

		assertEquals("A", thrown.variableName, "The message does not name the repeated column");
		assertEquals(file.getName(), thrown.source, "The message does not name the file");
	}

	@Test
	void aWekaFileWithTwoAttributesOfTheSameNameIsRefused() throws IOException {
		File file = fileWith("repeated.arff", """
				@RELATION test
				@ATTRIBUTE A {a1,a2}
				@ATTRIBUTE B {b1,b2}
				@ATTRIBUTE A {a1,a2}
				@DATA
				a1,b1,a2
				""");

		assertThrows(ParsingSourceException.RepeatedVariableNames.class, () -> new ArffDataBaseIO().load(file),
				"The file was read, and one of the two attributes would be lost without a word");
	}

	@Test
	void aFileWhoseColumnsHaveDistinctNamesIsStillRead() throws IOException {
		File file = fileWith("distinct.csv", "A,B\na1,b1\na2,b2\n");

		assertDoesNotThrow(() -> new CSVDataBaseIO().load(file), "A file with no repeated column was refused");
	}
}
