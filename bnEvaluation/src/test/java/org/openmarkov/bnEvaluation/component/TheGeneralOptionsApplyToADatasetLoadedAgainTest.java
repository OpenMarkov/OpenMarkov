/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.component;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.DataPreprocessor;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.commonComponents.GeneralMode;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.MissingValues;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In «Data preprocessing», the rows of the variables of a dataset loaded after choosing the options for all the
 * variables take those options, which are the ones applied; they took the default ones (error 318).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheGeneralOptionsApplyToADatasetLoadedAgainTest {

    @Test void theRowsTakeTheOptionsChosenForAllTheVariables() throws Exception {
        PreprocessingOptionsPanel panel = new PreprocessingOptionsPanel();
        panel.updateForDatabase(dataset());
        field(panel, "missingValuesComboBox", JComboBox.class).setSelectedItem("Erase records with missing values");
        JComboBox<?> discretize = field(panel, "discretizeComboBox", JComboBox.class);
        for (int index = 0; index < discretize.getItemCount(); index++) {
            GeneralMode<?> mode = (GeneralMode<?>) discretize.getItemAt(index);
            if (mode.commonValueToSet() == Discretization.Option.EQUAL_WIDTH) {
                discretize.setSelectedIndex(index);
            }
        }
        field(panel, "numIntervalsCheckBox", AbstractButton.class).doClick();
        field(panel, "numIntervalsSpinner", JSpinner.class).setValue(5);

        panel.updateForDatabase(dataset());

        DataPreprocessor.Request request = panel.buildRequest();
        assertEquals(Map.of("Age", MissingValues.Option.ELIMINATE, "Sex", MissingValues.Option.ELIMINATE),
                     request.missingValuesOptions());
        assertEquals(Map.of("Age", Discretization.Option.EQUAL_WIDTH, "Sex", Discretization.Option.NONE),
                     request.discretizeOptions());
        assertEquals(5, request.numIntervals().get("Age"));
    }

    /** A numeric variable and one that is not. */
    private static CaseDatabase dataset() {
        return new CaseDatabase(List.of(new Variable("Age", "20", "30", "40"), new Variable("Sex", "male", "female")),
                                new int[][]{{0, 0}, {1, 1}, {2, 0}});
    }

    private static <T> T field(PreprocessingOptionsPanel panel, String name, Class<T> type) throws Exception {
        Field field = PreprocessingOptionsPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(panel));
    }
}
