package org.openmarkov.core.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.Writer;

public class OpenMarkovLogger {
    
    public static final Logger LOGGER = LogManager.getLogger(OpenMarkovLogger.class.getName());
    
    private static final int MAX_BUFFER_SIZE = 500_000;
    
    private static final BoundedStringWriter LOG_BUFFER;
    
    static {
        LOG_BUFFER = new BoundedStringWriter(OpenMarkovLogger.MAX_BUFFER_SIZE);
        //This "if" won't be triggered in some tests that use logging, but it will on normal execution of OpenMarkov.
        if (LogManager.getContext(false) instanceof LoggerContext context) {
            Configuration config = context.getConfiguration();
            Appender consoleAppender = config.getAppender("StringLog");
            Layout<?> layout = consoleAppender.getLayout();
            WriterAppender captureAppender = WriterAppender.createAppender((StringLayout) layout, null, OpenMarkovLogger.LOG_BUFFER, "OpenMarkovStringAppender", false, true);
            captureAppender.start();
            config.addAppender(captureAppender);
            LoggerConfig loggerConfig = config.getLoggerConfig(OpenMarkovLogger.LOGGER.getName());
            loggerConfig.addAppender(captureAppender, Level.ALL, null);
            context.updateLoggers();
        }
    }
    
    public static synchronized String getCapturedLogs() {
        return OpenMarkovLogger.LOG_BUFFER.toString();
    }
    
    
    /**
     * Internal thread-safe circular buffer Writer that discards oldest logs when max capacity is reached.
     */
    private static class BoundedStringWriter extends Writer {
        private final StringBuilder builder;
        private final int maxCapacity;
        
        public BoundedStringWriter(int maxCapacity) {
            this.maxCapacity = maxCapacity;
            this.builder = new StringBuilder(Math.min(maxCapacity, 1024));
        }
        
        @Override
        public synchronized void write(char[] cbuf, int off, int len) {
            builder.append(cbuf, off, len);
            if (builder.length() > maxCapacity) {
                builder.delete(0, builder.length() - maxCapacity);
            }
        }
        
        @Override
        public synchronized void flush() {
        }
        
        @Override
        public synchronized void close() {
        }
        
        @Override
        public synchronized String toString() {
            return builder.toString();
        }
    }
}