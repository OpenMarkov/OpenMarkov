/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The window of expressions inserts a variable or a function on the double click that its help asks for. Each click
 * of it inserted the text, so a double click put it twice (error 165 of the pending list).
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window needs a screen")
class ADoubleClickInsertsTheVariableOnceTest {

    private static <T extends Component> T find(Container container, Class<T> type, int skip) {
        int[] left = { skip };
        return find(container, type, left);
    }

    private static <T extends Component> T find(Container container, Class<T> type, int[] left) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component) && left[0]-- == 0) {
                return type.cast(component);
            }
            if (component instanceof Container inner) {
                T found = find(inner, type, left);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** The two clicks of a double click: Swing sends one with a count of 1 and another with 2. */
    private static void doubleClick(JList<?> list) {
        list.setSelectedIndex(0);
        for (int count = 1; count <= 2; count++) {
            list.dispatchEvent(new MouseEvent(list, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, count,
                                              false, MouseEvent.BUTTON1));
        }
    }

    @Test void aVariableAndAFunctionAreInsertedOnce() throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            SwingUtilities.invokeAndWait(() -> {
                ArithmeticExpressionDialog dialog = new ArithmeticExpressionDialog(null, List.of(new Variable("Age")), "");
                try {
                    JList<?> variables = find(dialog.getContentPane(), JList.class, 0);
                    JList<?> functions = find(dialog.getContentPane(), JList.class, 1);
                    JTextField expression = find(dialog.getContentPane(), JTextField.class, 0);

                    doubleClick(variables);
                    assertEquals("{Age}", expression.getText());

                    String function = String.valueOf(functions.getModel().getElementAt(0));
                    expression.setCaretPosition(expression.getText().length());
                    doubleClick(functions);
                    assertEquals("{Age}" + function + "()", expression.getText());
                } finally {
                    dialog.dispose();
                }
            });
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
