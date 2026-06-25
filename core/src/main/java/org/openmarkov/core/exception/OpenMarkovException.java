/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * Base class for all checked domain exceptions in OpenMarkov.
 * <p>
 * Catching this type allows callers to handle any expected domain-level failure
 * (e.g. parser errors, edit failures, incompatible evidence) without needing to
 * enumerate every concrete subtype.
 * <p>
 * The i18n support (localised title and message) is provided automatically by
 * {@link IBundledOpenMarkovException}: subclasses only need to register a
 * {@code .properties} entry for their simple class name.
 * <p>
 * Infrastructure exceptions ({@link UnreachableException}, {@link UnrecoverableException})
 * do NOT extend this class — they represent programming errors or fatal system
 * conditions, not domain errors.
 *
 * @see IBundledOpenMarkovException
 */
public abstract class OpenMarkovException extends Exception implements IBundledOpenMarkovException {
    
    protected OpenMarkovException() {
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
}
