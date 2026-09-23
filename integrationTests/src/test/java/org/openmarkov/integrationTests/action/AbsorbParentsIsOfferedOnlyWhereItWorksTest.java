/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.action;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.AbsorbParentsEdit;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * "Absorb parents" is offered only for a node whose potential says how to combine its parents
 * (a sum, a product or a formula), and absorbing works wherever it is offered.
 *
 * @author Manuel Arias
 */
public class AbsorbParentsIsOfferedOnlyWhereItWorksTest {

    private static ProbNet read(URL url) throws Exception {
        return new PGMXReader().read(url).probNet();
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aCycleLengthShiftIsNotOffered() throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/mid/MID-Chancellor-new.pgmx")).findFirst().orElseThrow();
        ProbNet net = read(url);

        assertFalse(BasicOperations.haveParentsAndAreAllAbsorbable(net.getNode("Time in treatment [1]")));
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void absorbingWorksWhereverItIsOffered() throws Exception {
        List<String> failures = new ArrayList<>();
        List<URL> urls = Networks.getNetworks().filter(u -> u.getPath().endsWith(".pgmx")).toList();
        for (URL url : urls) {
            ProbNet net;
            try {
                net = read(url);
            } catch (Exception unreadable) {
                continue;
            }
            for (Node node : net.getNodes()) {
                if (!BasicOperations.haveParentsAndAreAllAbsorbable(node)) {
                    continue;
                }
                ProbNet fresh = read(url);
                try {
                    new AbsorbParentsEdit(fresh, fresh.getNode(node.getName())).executeEdit();
                } catch (Exception | Error e) {
                    failures.add(url.getPath() + " [" + node.getName() + "]: " + e);
                }
            }
        }
        assertEquals(List.of(), failures);
    }
}
