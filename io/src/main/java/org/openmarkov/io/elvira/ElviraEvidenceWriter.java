/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

/**
 * @author Manuel Arias
 */
public class ElviraEvidenceWriter {
    
    // Methods
    
    /**
     * Writes an evidence case to a file in Elvira format.
     *
     * @param fileName path + network name + extension
     * @param evidence the evidence case to write
     * @throws IOException if an I/O error occurs while writing
     */
    public static void writeEvidenceCase(String fileName, EvidenceCase evidence) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        PrintWriter out = new PrintWriter(writer);
        writeEvidencePreamble(out, evidence);
        writeFindings(out, evidence);
        out.println("\n}");
        out.close();
    }
    
    private static void writeEvidencePreamble(PrintWriter out, EvidenceCase evidence) {
        out.println("//	   Evidence case");
        out.println("//	   Elvira format\n");
        out.println("evidence NoName {\n");
        out.println("//	   Evidence additionalProperties\n");
        out.println("title = " + '"' + "Untitled" + '"' + ";");
        out.println("version = 1.0;\n");
    }
    
    private static void writeFindings(PrintWriter out, EvidenceCase evidence) {
        for (Finding finding : evidence.getFindings()) {
            int stateIndex = finding.getStateIndex();
            Variable variable = finding.getVariable();
            int numStates = variable.getNumStates();
            int elviraEvidenceStateIndex = numStates - stateIndex - 1;
            out.println(finding.getVariable().getName() + " = " + elviraEvidenceStateIndex + ",     // " + variable
                    .getStateName(stateIndex));
        }
    }
    
}
