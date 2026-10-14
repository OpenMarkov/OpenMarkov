/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.MIDType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Changing the cycle length of a network is undone like any other edit (error 73).
 *
 * @author Manuel Arias
 */
class CycleLengthEditTest {

	@Test
	void theNewLengthIsKeptAndUndoingGivesBackTheOldOne() throws Exception {
		ProbNet net = new ProbNet(MIDType.getUniqueInstance());
		net.getPNESupport().setWithUndo(true);
		net.setCycleLength(new CycleLength(Unit.YEAR, 1));

		new CycleLengthEdit(net, new CycleLength(Unit.MONTH, 6)).executeEdit();
		assertEquals(Unit.MONTH, net.getCycleLength().getUnit());
		assertEquals(6, net.getCycleLength().getValue());

		net.getPNESupport().undo();
		assertEquals(Unit.YEAR, net.getCycleLength().getUnit());
		assertEquals(1, net.getCycleLength().getValue());
	}

	@Test
	void aNetworkWithoutLengthGetsOneAndUndoingLeavesItWithout() throws Exception {
		ProbNet net = new ProbNet(MIDType.getUniqueInstance());
		net.getPNESupport().setWithUndo(true);
		net.setCycleLength(null);

		new CycleLengthEdit(net, new CycleLength(Unit.WEEK, 2)).executeEdit();
		assertEquals(Unit.WEEK, net.getCycleLength().getUnit());

		net.getPNESupport().undo();
		assertNull(net.getCycleLength());
	}
}
