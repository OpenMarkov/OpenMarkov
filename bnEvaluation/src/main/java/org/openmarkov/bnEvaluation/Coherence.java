/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

/**
 * Represents the degree of coherence between a Bayesian network's variables
 * and those found in a case database.
 */
public enum Coherence {
    STRONG,
    WEAK,
    ZERO
}
