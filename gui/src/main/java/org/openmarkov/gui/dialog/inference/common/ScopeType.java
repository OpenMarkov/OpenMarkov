/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

/**
 * Enumeration of analysis scope types for inference: global analysis or
 * analysis conditioned on a specific decision.
 */
public enum ScopeType {
	// Analysis type options
    GLOBAL("ScopeSelector.Scenario.Global"),
    DECISION("ScopeSelector.Scenario.Decision");

	private final String display;

	ScopeType(String display) {
		this.display = display;
	}

	@Override public String toString() {
		return display;
	}
}
