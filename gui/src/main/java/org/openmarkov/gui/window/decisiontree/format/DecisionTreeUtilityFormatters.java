/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import org.openmarkov.core.model.decisiontree.CENodeEvaluation;
import org.openmarkov.core.model.decisiontree.DecisionTreeNodeEvaluation;
import org.openmarkov.core.model.decisiontree.UnicritNodeEvaluation;

import java.text.DecimalFormat;

/**
 * Single entry point for formatting the utility carried by the evaluation of a
 * decision-tree node.
 *
 * <p>Dispatches by the kind of evaluation to {@link DoubleUtilityFormatter} or
 * {@link CEPUtilityFormatter}; future analysis types only need to register a
 * new formatter here.
 */
public final class DecisionTreeUtilityFormatters {

    private DecisionTreeUtilityFormatters() {
    }

    /**
     * Formats the utility of the given evaluation, choosing the right formatter
     * by the kind of evaluation. Returns an empty string when the node has not
     * been evaluated yet or its evaluation carries no utility.
     */
    public static String format(DecisionTreeNodeEvaluation evaluation, DecimalFormat df, boolean addSlashPrefix) {
        return switch (evaluation) {
            case null -> "";
            case UnicritNodeEvaluation unicrit ->
                    DoubleUtilityFormatter.INSTANCE.format(unicrit.getUtility(), df, addSlashPrefix);
            case CENodeEvaluation ce ->
                    (ce.getCep() == null) ? "" : CEPUtilityFormatter.INSTANCE.format(ce.getCep(), df, addSlashPrefix);
        };
    }
}
