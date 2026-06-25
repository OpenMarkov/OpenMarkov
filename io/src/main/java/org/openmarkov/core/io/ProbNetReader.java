/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io;

import org.openmarkov.core.exception.ParserException;

import java.io.IOException;
import java.net.URL;

@FunctionalInterface public interface ProbNetReader {
    
    /**
     * @param file    File
     * @param networkSource = path + network name + extension. {@code String}
     *
     * @return A {@code ProbNetInfo} or {@code null}
     *
     * @throws ParserException ParserException
     */
    ProbNetInfo read(URL networkSource) throws IOException, ParserException;
    
    
}