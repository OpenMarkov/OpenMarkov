/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.DES.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;

/**
 * The simulation admits chance nodes with finite states or numeric ones, not those divided into intervals (error 106
 * of the pending list).
 */
public class ChanceNodeCannotBeDiscretized extends OpenMarkovException {

    public final Node node;

    public ChanceNodeCannotBeDiscretized(Node node) {
        this.node = node;
    }
}
