/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import java.util.List;

/**
 * Contains a ProbNet and a list of Evidence Cases
 */
public record ProbNetInfo(ProbNet probNet, List<EvidenceCase> evidence, ProbNetReader reader, ProbNetWriter writer) {

}