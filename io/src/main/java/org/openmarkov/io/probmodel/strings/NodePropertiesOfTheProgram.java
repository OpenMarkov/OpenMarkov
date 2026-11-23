/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel.strings;

import java.util.Set;

/**
 * The names that a ProbModelXML file uses, among the additional properties of a node, for data of the program
 * itself. A property that the user adds to a node cannot have one of them: on opening, it would be taken for that data.
 *
 * @author Manuel Arias
 */
public final class NodePropertiesOfTheProgram {

    public static final Set<String> NAMES = Set.of(XMLTags.RELEVANCE.toString(), XMLTags.PURPOSE.toString(),
                                                   XMLTags.TITLE.toString(), XMLTags.ALWAYS_APPEND.toString());

    private NodePropertiesOfTheProgram() {
    }
}
