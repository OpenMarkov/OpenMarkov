/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JEditorPane;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The notice of the variables that a dataset lacks shows their names as they are, also when they have characters
 * that HTML reads as marks (error 437).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheNoticeOfMissingVariablesShowsTheirNamesTest {

    /** The text that Swing shows for that HTML, one line for each item of the list. */
    private static List<String> shownBySwing(String html) throws Exception {
        JEditorPane pane = new JEditorPane("text/html", html);
        String text = pane.getDocument().getText(0, pane.getDocument().getLength());
        return text.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
    }

    @Test void theNamesAreShownAsTheyAre() throws Exception {
        List<Variable> missing = List.of(new Variable("A<B"), new Variable("x<b>y</b>"), new Variable("a&lt;b"),
                                         new Variable("R&D"), new Variable("Normal"));

        List<String> shown = shownBySwing(BNEvaluationDialog.noticeOfTheVariablesNotIn(missing));

        assertEquals(List.of("The following variable(s) are not included in the dataset:",
                             "A<B", "Normal", "R&D", "a&lt;b", "x<b>y</b>"), shown);
    }
}
