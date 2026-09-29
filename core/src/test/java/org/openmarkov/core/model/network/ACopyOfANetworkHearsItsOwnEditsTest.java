/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.core.NodeCommentEdit;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An edit on a copy of a network marks the copy as modified, and whoever listens to the original
 * does not hear it.
 *
 * @author Manuel Arias
 */
class ACopyOfANetworkHearsItsOwnEditsTest {

    private static class Counter implements PNEditListener {
        int heard;

        @Override public void afterEditExecutes(PNEdit edit) {
            heard++;
        }
    }

    private static void editACopy(UnaryOperator<ProbNet> copier) throws Exception {
        ProbNet original = new ProbNet(BayesianNetworkType.getUniqueInstance());
        original.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
        Counter listenerOfTheOriginal = new Counter();
        original.getPNESupport().addListener(listenerOfTheOriginal);

        ProbNet copy = copier.apply(original);
        new NodeCommentEdit(copy.getNode("A"), "a comment").executeEdit();

        assertTrue(copy.getPNESupport().networkIsModified());
        assertEquals(0, listenerOfTheOriginal.heard);
    }

    @Tag(TestSpeed.FAST)
    @Test void aShallowCopy() throws Exception {
        editACopy(ProbNet::copy);
    }

    @Tag(TestSpeed.FAST)
    @Test void aDeepCopy() throws Exception {
        editACopy(ProbNet::deepCopy);
    }
}
