/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

/**
 * Marker interface for potentials that can represent a cost-effectiveness
 * utility: a numeric outcome quantified in the currency of the
 * cost-effectiveness analysis (e.g. cost, QALY, cost-effectiveness ratio).
 *
 * <p>Implementations can be returned by variable-elimination cores and
 * consumed by CE-specific tasks (VECEAnalysis, VECEPSA, ...). The interface
 * is intentionally empty; it exists to replace unchecked downcasts in the
 * CE pipeline with a typed return value.</p>
 */
public interface CEUtilityPotential {
}
