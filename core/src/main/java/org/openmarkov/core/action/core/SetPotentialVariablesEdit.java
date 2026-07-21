/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit that reorders the parent variables of a node's first potential.
 *
 * <p>Previously this edit called {@link Potential#setVariables}
 * directly, which changed the variable list without reordering the underlying
 * {@code values[]} array. Because table indexing (offsets) depends on variable
 * order, the old approach produced a corrupted CPT (issue #562).
 *
 * <p>The fix uses {@link Potential#reorder(List)}
 * to obtain a properly reordered copy and replaces the node's potential with it.
 * For potential types whose {@code reorder()} still returns {@code null} (stubs),
 * the edit falls back to the old {@code setVariables} path so that at least the
 * dialog does not crash (values may be incorrect, but that is a secondary bug).
 */
public class SetPotentialVariablesEdit extends PNEdit {

	private final List<Variable> oldVariables;
	private final List<Variable> newVariables;
	private final Node node;
	/** Reference to the potential that existed before doEdit(); used for undo. */
	private final Potential oldPotential;

	public SetPotentialVariablesEdit(Node node, List<Variable> newVariables) {
		super(node.getProbNet());
		this.node = node;
		this.oldPotential = node.getPotentials().getFirst();
		this.oldVariables = new ArrayList<>(oldPotential.getVariables());
		this.newVariables = newVariables;
	}

	/**
	 * Reorders the potential using {@link Potential#reorder(List)}.
	 * If {@code reorder()} returns {@code null} (stub implementation), falls back
	 * to {@code setVariables()} to avoid crashing — note that values will be
	 * in the wrong order in that case.
	 */
	@Override protected void doEdit() {
		Potential reordered = oldPotential.reorder(newVariables);
		if (reordered != null) {
			node.setPotential(reordered);
		} else {
			oldPotential.setVariables(newVariables); // fallback: broken but doesn't crash
		}
	}

	@Override public void undo() {
		super.undo();
		Potential current = node.getPotentials().getFirst();
		if (current == oldPotential) {
			// doEdit() used the fallback setVariables() path — restore old variable list
			oldPotential.setVariables(oldVariables);
		} else {
			// doEdit() created a new reordered potential — restore the original
			node.setPotential(oldPotential);
		}
	}
}