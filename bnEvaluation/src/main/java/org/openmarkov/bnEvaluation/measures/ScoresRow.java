/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

/**
 * Single row in the structured Scores view of a {@link MeasuresSet}.
 *
 * <p>A list of {@code ScoresRow}s is the typed model that replaces the prior
 * {@code String[][]} where "data" rows and "section title" rows were
 * distinguished only by leaving the value column empty.</p>
 */
public sealed interface ScoresRow {

    /** Section heading that introduces the rows that follow. */
    record Section(String title) implements ScoresRow {}

    /** Numeric data row: a labelled measure with its scalar value. */
    record Data(String label, double value) implements ScoresRow {}
}
