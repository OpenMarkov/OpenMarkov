/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception.testlocalize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

/**
 * Test-only bundle provider: gives the exception-framework tests a localized
 * exception to exercise, without touching the production bundles.
 *
 * @author Manuel Arias
 */
public class ExceptionFrameworkTestBundleProvider implements LocalizeResourcesProvider {

    @Override public @NotNull String getRootOfResources() {
        return "/exceptionframeworktest";
    }
}
