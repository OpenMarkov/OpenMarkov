/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.model.network.potential.Potential;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a {@link PotentialPanel} subclass as a plugin for editing one or more
 * {@link Potential} types. Discovered at runtime by {@link PotentialPanelManager}.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface PotentialPanelPlugin {
    Class<? extends Potential>[] potentialClasses();
}
