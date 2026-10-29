/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Node;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

/**
 * Panel shown for a potential whose type has no editing panel: it tells the user to choose another relation type and
 * leaves the potential unchanged.
 *
 * @author Manuel Arias
 */
@SuppressWarnings("serial")
public class NotEditablePotentialPanel extends PotentialPanel {

    public NotEditablePotentialPanel(Node node) {
        setLayout(new BorderLayout());
        JLabel message = new JLabel(StringDatabase.getUniqueInstance()
                                                  .getString("NodeProbsValuesTablePanel.jLabelNotEditable.Text"),
                                    SwingConstants.CENTER);
        message.setName("jLabelNotEditable");
        add(message, BorderLayout.CENTER);
    }

    @Override public void setData(Node node) {
    }

    @Override public void close() {
    }
}
