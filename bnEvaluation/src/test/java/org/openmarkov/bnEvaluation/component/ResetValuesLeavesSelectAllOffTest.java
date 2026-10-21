/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.component;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.AbstractButton;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In «Data preprocessing», «Reset values» goes back to «All variables» and leaves «Select/unselect all variables»
 * off, as choosing «All variables» does; it left that box on, and ticking it unticked all the variables (error 319).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ResetValuesLeavesSelectAllOffTest {

    @Test void afterResetValuesSelectAllIsOffAndAllTheVariablesAreKept() throws Exception {
        PreprocessingOptionsPanel panel = new PreprocessingOptionsPanel();
        CaseDatabase dataset = new CaseDatabase(List.of(new Variable("A", "x", "y"), new Variable("B", "x", "y")),
                                                new int[][]{{0, 0}, {1, 1}});
        panel.updateForDatabase(dataset);
        button(panel, "selectedVariablesRadioButton").doClick();

        panel.reset();

        AbstractButton selectAll = button(panel, "selectDeselectCheckBox");
        assertTrue(button(panel, "allVariablesRadioButton").isSelected());
        assertFalse(selectAll.isEnabled());
        assertFalse(selectAll.isSelected());
        assertEquals(dataset.getVariables(), panel.buildRequest().selectedVariables());
    }

    private static AbstractButton button(PreprocessingOptionsPanel panel, String name) throws Exception {
        Field field = PreprocessingOptionsPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (AbstractButton) field.get(panel);
    }
}
