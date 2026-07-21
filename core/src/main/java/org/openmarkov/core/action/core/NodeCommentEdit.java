/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;

/**
 * {@code NetworkCommentEdit} is a simple edit that allow modify a network
 * comment.
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
@SuppressWarnings("serial") public class NodeCommentEdit extends PNEdit {
    
    private final String currentComment;
    private final String newComment;
    
    private final Node node;
    
    /**
     * Creates a {@code NodeCommentEdit} with the node, new comment and
     * type of comment specified.
     *
     * @param node       Node
     * @param newComment New comment
     */
    public NodeCommentEdit(Node node, String newComment) {
        super(node.getProbNet());
        this.newComment = newComment;
        this.currentComment = node.getComment();
        this.node = node;
    }
    
    // Methods
    @Override protected void doEdit() {
        node.setComment(newComment);
    }
    
    @Override public void undo() {
        super.undo();
        node.setComment(currentComment);
    }
}
