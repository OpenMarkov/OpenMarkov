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
 * The relation of a node is of a kind that the simulation cannot use, such as the uniform one that adding or removing
 * a link leaves (error 99 of the pending list).
 */
public class RelationCannotBeSimulated extends OpenMarkovException {

    public final Node node;
    /** The name of the kind of relation, as the editor shows it */
    public final String relation;

    public RelationCannotBeSimulated(Node node, String relation) {
        this.node = node;
        this.relation = relation;
    }
}
