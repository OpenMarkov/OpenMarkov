/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

/**
 * The user stopped an inference before it finished.
 *
 * @author Manuel Arias
 */
public class InferenceStoppedException extends RuntimeException implements IOpenMarkovException {

    public InferenceStoppedException() {
        super("The inference was stopped before it finished.");
    }

    @Override public String getExceptionMessage() {
        return getMessage();
    }

    @Override public String getExceptionTitle() {
        return "Inference stopped";
    }

    @Override public String toString() {
        return this.localize();
    }
}
