/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.mode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking an {@link EditionMode} subclass as a discoverable edition
 * state plugin, specifying its display name, toolbar icon, and optional cursor.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface EditionState {
    String name();
    
    String icon();
    
    String cursor() default "[default]";
}
