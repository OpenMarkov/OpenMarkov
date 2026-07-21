package org.openmarkov.core.logging;

import org.apache.logging.log4j.LogManager;

public class OpenMarkovLogger {
    
    public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(OpenMarkovLogger.class.getName());
    
    public static void debug(String message){
        LOGGER.debug(message);
    }
    
}
