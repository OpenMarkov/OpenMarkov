/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a {@link org.openmarkov.gui.menutoolbar.toolbar.ToolBarBasic} subclass
 * as a discoverable toolbar plugin. The {@link ToolbarManager} scans the classpath for classes
 * annotated with this annotation.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface Toolbar {
    /** The unique name used to identify and activate this toolbar. */
    String name();
}
