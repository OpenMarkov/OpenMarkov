/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;
import java.util.function.Function;

/**
 * Styles a two-column Scores {@link JTable} so that section-title rows look
 * like headings and data rows look like a flat, modern read-only listing.
 *
 * <p>Section rows are detected by an empty value column (column 1). The styler
 * applies bold typography, a subtle background and hairline separators to those
 * rows; data rows get alternating background, an indented label column and a
 * right-aligned numeric value column. Tooltips on label cells are produced by
 * a caller-supplied function.</p>
 */
public final class ScoresTableStyler {

    private static final Color SECTION_HEADER_BG = new Color(0xEC, 0xEF, 0xF4);
    private static final Color SECTION_SEPARATOR = new Color(0xCF, 0xD8, 0xDC);
    private static final Color ROW_ALT_BG        = new Color(0xF7, 0xF8, 0xFA);

    private ScoresTableStyler() {
        // utility
    }

    /**
     * Applies the styling in place to {@code table}.
     *
     * @param table          the Scores table to style
     * @param labelTooltipFn function returning a tooltip for the label of a
     *                       data row (may return {@code null} to mean "no
     *                       tooltip"); never invoked for section rows
     */
    public static void style(JTable table, Function<String, String> labelTooltipFn) {
        table.setRowHeight(Math.max(table.getRowHeight() + 6, 26));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setVisible(false);
            header.setPreferredSize(new Dimension(0, 0));
        }
        if (table.getColumnModel().getColumnCount() >= 2) {
            table.getColumnModel().getColumn(0).setPreferredWidth(340);
            table.getColumnModel().getColumn(1).setPreferredWidth(140);
        }
        table.setDefaultRenderer(Object.class, new ScoresCellRenderer(labelTooltipFn));
    }

    /** Convenience overload for callers that don't need tooltips. */
    public static void style(JTable table) {
        style(table, label -> null);
    }

    private static final class ScoresCellRenderer extends DefaultTableCellRenderer {
        private static final Border SECTION_BORDER = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, SECTION_SEPARATOR),
                BorderFactory.createEmptyBorder(4, 12, 4, 12));
        private static final Border LABEL_BORDER = BorderFactory.createEmptyBorder(2, 28, 2, 8);
        private static final Border VALUE_BORDER = BorderFactory.createEmptyBorder(2, 12, 2, 16);

        private final Function<String, String> labelTooltipFn;

        ScoresCellRenderer(Function<String, String> labelTooltipFn) {
            this.labelTooltipFn = labelTooltipFn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            // Always render unselected to keep the read-only look.
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            boolean section = isSectionRow(t, row);
            Font base = t.getFont();
            if (section) {
                setFont(base.deriveFont(Font.BOLD));
                setBackground(SECTION_HEADER_BG);
                setBorder(SECTION_BORDER);
                setHorizontalAlignment(SwingConstants.LEFT);
                setToolTipText(null);
            } else {
                setFont(base.deriveFont(Font.PLAIN));
                setBackground((row % 2 == 0) ? t.getBackground() : ROW_ALT_BG);
                if (col == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setBorder(VALUE_BORDER);
                    setToolTipText(null);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBorder(LABEL_BORDER);
                    setToolTipText(Optional.ofNullable(value)
                            .filter(v -> v instanceof String)
                            .map(v -> labelTooltipFn.apply((String) v))
                            .orElse(null));
                }
            }
            return this;
        }

        private static boolean isSectionRow(JTable t, int row) {
            Object v1 = t.getValueAt(row, 1);
            return v1 == null || v1.toString().isEmpty();
        }
    }
}
