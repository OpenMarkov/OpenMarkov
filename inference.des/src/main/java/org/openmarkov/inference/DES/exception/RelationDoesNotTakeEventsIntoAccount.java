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
 * The relation of a node has an event among its parents but is of a kind that does not take events into account,
 * such as a table: the simulation would read out of it when the event happens (error 98 of the pending list).
 */
public class RelationDoesNotTakeEventsIntoAccount extends OpenMarkovException {

    public final Node node;
    /** The name of the kind of relation, as the editor shows it */
    public final String relation;
    public final Node event;

    public RelationDoesNotTakeEventsIntoAccount(Node node, String relation, Node event) {
        this.node = node;
        this.relation = relation;
        this.event = event;
    }
}
