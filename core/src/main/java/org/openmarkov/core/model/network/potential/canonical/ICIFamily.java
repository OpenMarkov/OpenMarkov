/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

/**
 * Enumerates the families of Independent Causal Influence (ICI) models:
 * OR (includes OR, causal MAX, general MAX), AND (includes AND, causal MIN,
 * general MIN), and TUNING.
 */
public enum ICIFamily {
    OR,
    AND,
    TUNING;
}
