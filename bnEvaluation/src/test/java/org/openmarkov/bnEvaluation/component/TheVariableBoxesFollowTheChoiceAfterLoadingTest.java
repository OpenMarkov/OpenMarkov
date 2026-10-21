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
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In «Data preprocessing», the box of each variable can be ticked after loading a dataset when «Selected variables»
 * is chosen, and not when «All variables» is (error 219).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheVariableBoxesFollowTheChoiceAfterLoadingTest {

    @Test void theBoxesAreEnabledWithSelectedVariables() throws Exception {
        PreprocessingOptionsPanel panel = new PreprocessingOptionsPanel();
        panel.updateForDatabase(dataset());
        button(panel, "selectedVariablesRadioButton").doClick();

        panel.updateForDatabase(dataset());

        assertTrue(button(panel, "selectedVariablesRadioButton").isSelected());
        assertTrue(Arrays.stream(boxes(panel)).allMatch(Component::isEnabled));
    }

    @Test void theBoxesAreDisabledWithAllVariables() throws Exception {
        PreprocessingOptionsPanel panel = new PreprocessingOptionsPanel();
        panel.updateForDatabase(dataset());
        button(panel, "selectedVariablesRadioButton").doClick();
        button(panel, "allVariablesRadioButton").doClick();

        panel.updateForDatabase(dataset());

        assertTrue(Arrays.stream(boxes(panel)).noneMatch(Component::isEnabled));
    }

    private static CaseDatabase dataset() {
        return new CaseDatabase(List.of(new Variable("A", "x", "y"), new Variable("B", "x", "y")),
                                new int[][]{{0, 0}, {1, 1}});
    }

    private static Component[] boxes(PreprocessingOptionsPanel panel) throws Exception {
        Component[] boxes = ((Container) field(panel, "varSelectionPanel")).getComponents();
        assertEquals(2, boxes.length);
        return boxes;
    }

    private static AbstractButton button(PreprocessingOptionsPanel panel, String name) throws Exception {
        return (AbstractButton) field(panel, name);
    }

    private static Object field(PreprocessingOptionsPanel panel, String name) throws Exception {
        Field field = PreprocessingOptionsPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(panel);
    }
}
