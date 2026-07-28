/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

/**
 * What a decision tree carries as its utility: a single number, or a cost-effectiveness
 * function of the willingness to pay.
 *
 * @author Manuel Arias
 */
public enum EvaluationType {
    UNICRITERION,
    CE;
}
