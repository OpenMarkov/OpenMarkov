package org.openmarkov.core.testTags;

public class TestSpeed {
    
    /**
     * Should be applied to test that takes less than 50 milliseconds to complete
     */
    public static final String FAST = "speed_fast";
    
    /**
     * Should be applied to test that takes at least 50 milliseconds to complete
     */
    public static final String MEDIUM = "speed_medium";
    
    /**
     * Should be applied to test that takes at least 300 milliseconds to complete
     */
    public static final String SLOW = "speed_slow";
}
