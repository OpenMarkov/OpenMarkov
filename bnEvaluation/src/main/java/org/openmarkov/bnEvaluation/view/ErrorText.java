/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.view;

import org.openmarkov.core.exception.IOpenMarkovException;

/**
 * The text an error window shows for an exception.
 *
 * @author Manuel Arias
 */
public final class ErrorText {

    private ErrorText() {
    }

    /** @return the message of an OpenMarkov exception, or that of any other; never null nor blank */
    public static String of(Throwable throwable) {
        String text = throwable instanceof IOpenMarkovException openMarkovException
                ? openMarkovException.getExceptionMessage() : throwable.getMessage();
        return (text == null || text.isBlank()) ? throwable.getClass().getSimpleName() : text;
    }
}
