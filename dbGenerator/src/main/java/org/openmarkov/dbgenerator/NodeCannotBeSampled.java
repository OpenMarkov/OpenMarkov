/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator;

import org.openmarkov.core.exception.OpenMarkovException;

/**
 * A node that the generator cannot sample: it is not a chance node, it has no relation, or its relation cannot be
 * sampled.
 *
 * @author Manuel Arias
 */
public class NodeCannotBeSampled extends OpenMarkovException {
    
    public final String nodeName;
    
    public NodeCannotBeSampled(String nodeName) {
        this.nodeName = nodeName;
    }
}
