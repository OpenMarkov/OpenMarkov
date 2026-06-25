/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

import org.openmarkov.java.enumUtils.EnumUtils;

import java.util.Properties;

/**
 * Enum representing the current operating system, detected at class-load time.
 * Provides a {@link #CURRENT_OS} constant for runtime OS checks.
 */
public enum OperatingSystem {
    WINDOWS,
    LINUX,
    OTHER;
    
    public String toString() {
        return EnumUtils.toTitleCase(this);
    }
    
    public static final OperatingSystem CURRENT_OS = getOperatingSystem();
    
    private static OperatingSystem getOperatingSystem() {
        OperatingSystem operatingSystem;
        Properties properties = System.getProperties();
        String osName = properties.getProperty("os.name");
        if (osName.toLowerCase().contains("windows")) {
            operatingSystem = OperatingSystem.WINDOWS;
        } else if (osName.toLowerCase().contains("linux")) {
            operatingSystem = OperatingSystem.LINUX;
        } else {
            operatingSystem = OperatingSystem.OTHER;
        }
        return operatingSystem;
    }
}
