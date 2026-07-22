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
import javax.swing.UIManager;
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
 *
 * <p>All colours are derived at render time from the table's own background and
 * foreground, which the look-and-feel (FlatLaf) sets according to the active
 * light or dark theme. The styler therefore stays legible under both themes and
 * follows a runtime theme switch, instead of hard-coding light tones.</p>
 */
public final class ScoresTableStyler {

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

    /**
     * Blends {@code base} a fraction of the way towards {@code toward}. Because {@code toward} is
     * the table's foreground (text) colour, a small positive fraction darkens a light theme and
     * lightens a dark one, giving a subtle, always-legible shade in both.
     *
     * @param base     the colour to shade (the table background)
     * @param toward   the colour to shade towards (the table foreground)
     * @param fraction how far to move, in {@code [0, 1]}
     * @return the blended colour
     */
    private static Color blend(Color base, Color toward, double fraction) {
        return new Color(
                channel(base.getRed(), toward.getRed(), fraction),
                channel(base.getGreen(), toward.getGreen(), fraction),
                channel(base.getBlue(), toward.getBlue(), fraction));
    }

    private static int channel(int from, int to, double fraction) {
        int value = (int) Math.round(from + (to - from) * fraction);
        return Math.max(0, Math.min(255, value));
    }

    /** The theme's alternating-row colour if the look-and-feel defines one, otherwise a subtle shade. */
    private static Color alternateRowColor(Color base, Color text) {
        Color themed = UIManager.getColor("Table.alternateRowColor");
        return (themed != null) ? themed : blend(base, text, 0.05);
    }

    private static final class ScoresCellRenderer extends DefaultTableCellRenderer {
        /** Colourless paddings; they carry insets only, so they are theme-independent. */
        private static final Border SECTION_PADDING = BorderFactory.createEmptyBorder(4, 12, 4, 12);
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
            Color base = t.getBackground();
            Color text = t.getForeground();
            boolean section = isSectionRow(t, row);
            Font font = t.getFont();
            if (section) {
                setFont(font.deriveFont(Font.BOLD));
                setBackground(blend(base, text, 0.10));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 1, 0, blend(base, text, 0.22)),
                        SECTION_PADDING));
                setHorizontalAlignment(SwingConstants.LEFT);
                setToolTipText(null);
            } else {
                setFont(font.deriveFont(Font.PLAIN));
                setBackground((row % 2 == 0) ? base : alternateRowColor(base, text));
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
