/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import java.text.DecimalFormat;

/**
 * Renders the utility value carried by a decision tree node into a
 * presentation-ready string. Each kind of analysis (plain expected utility,
 * cost-effectiveness, ...) provides its own implementation.
 *
 * <p>This interface lives in the GUI module because formatting is presentation
 * logic; the model classes ({@code DecisionTreeNode}, {@code DecisionTreeBranch})
 * are intentionally kept free of any dependency on {@link DecimalFormat}.
 *
 * @param <T> The utility type carried by the tree node.
 */
public interface DecisionTreeUtilityFormatter<T> {

    /**
     * Formats a utility value.
     *
     * @param utility       The utility value (may be {@code null} when the tree
     *                      has not been fully evaluated yet).
     * @param df            The decimal format to use for numeric components.
     * @param addSlashPrefix When {@code true}, the formatter is allowed to prepend
     *                      a {@code " /"} separator if it produces visible content.
     * @return A non-null string suitable for direct rendering in a label.
     */
    String format(T utility, DecimalFormat df, boolean addSlashPrefix);
}
