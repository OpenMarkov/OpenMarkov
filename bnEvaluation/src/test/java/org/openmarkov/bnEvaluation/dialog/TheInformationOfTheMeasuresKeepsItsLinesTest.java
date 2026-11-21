/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JTextPane;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What an evaluation says of itself is shown with a line for each of its sentences, not all in one (error 373).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheInformationOfTheMeasuresKeepsItsLinesTest {

    private static List<String> linesOf(String text) {
        return text.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
    }

    @Test void theBoxHasALineForEachSentence() throws Exception {
        MeasureMatrix matrix = new MeasureMatrix(MeasureType.CONFUSIONMATRIX, new String[]{"no", "yes"}, "X-ray");
        matrix.setMatrix(new int[][]{{3, 1}, {2, 4}}, 10);
        MeasuresSet measures = new MeasuresSet("Evaluation of the asia network");
        measures.addMeasureMatrix(matrix);
        measures.setNotAllVariablesAreUsed();
        String information = measures.getMeasureInformation();

        JTextPane pane = ResultsDialog.informationPane("text/plain", information);

        String shown = pane.getDocument().getText(0, pane.getDocument().getLength());
        assertEquals(3, linesOf(information).size());
        assertEquals(linesOf(information), linesOf(shown));
    }
}
