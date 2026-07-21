/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.database.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CSVDataBaseIOTest {
    
    final private String[] variablesNames = {"id", "cataract", "operation", "success", "sex", "age", "fupDur",
            "opType", "valDur", "cexDur", "breakType", "numBreaks", "bPVR", "caPVR", "cpPVR", "pvr", "smlBreak",
            "medBreak", "lrgBreak", "supt", "inft", "supn", "infn", "ante", "post", "loc48", "loc57", "loc48rrd",
            "loc57rrd", "va", "pvd", "vh", "quad", "fovea", "fupVa", "fupRetOff", "fupOil", "phthisis"};
    // Attributes
    private final String fileName = "/csv/Cataract-Data2.csv";
    
    @BeforeEach public void setUp() {
    }
    
    @Tag(TestSpeed.SLOW)
    @Test public void testOpenDBFile() throws EmptyDatabaseException, java.io.FileNotFoundException {
        CSVDataBaseIO databaseIO = new CSVDataBaseIO();
        URL file = getClass().getResource(fileName);
        CaseDatabase database = databaseIO.load(new File(file.getFile()));
        List<Variable> variables = database.getVariables();
        // test number of variables
        assertEquals(variablesNames.length, variables.size());
        // test variables names
        boolean nameContained;
        for (Variable variable : variables) {
            nameContained = false;
            for (String name : variablesNames) {
                if (variable.getName().contentEquals(name)) {
                    nameContained = true;
                    break;
                }
            }
            assertTrue(nameContained);
        }
        // test values (check state names, not raw indices, to be independent of state ordering)
        int[][] data = database.getCases();
        assertEquals("2", variables.get(0).getStateName(data[1][0]));
        assertEquals("114", variables.get(0).getStateName(data[12][0]));
        assertEquals("377", variables.get(0).getStateName(data[19][0]));
        assertEquals("0", variables.get(1).getStateName(data[2][1]));
        
    }
    
}
