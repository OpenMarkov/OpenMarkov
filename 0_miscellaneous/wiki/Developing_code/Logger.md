Since version 0.3 OpenMarkov implements the log of Apache log4j.

>All information written to the log is meant for the developer so it should be in English.

Every information that a java class generates and the developer deems as valuable (debugging, execution time) should be written to that log.

#### Log priority levels
Every log has some levels to mark the importance of the information, in log4j they are (extracted from the Level.class):
```java
    /** A severe error that will prevent the application from continuing. */
    public static final Level FATAL;

    /** An error in the application, possibly recoverable. */
    public static final Level ERROR;

    /** An event that might possible lead to an error. */
    public static final Level WARN;

    /** An event for informational purposes. */
    public static final Level INFO;

    /** A general debugging event. */
    public static final Level DEBUG;

    /** A fine-grained debug message, typically capturing the flow through the application. */
    public static final Level TRACE;
```
Setting the log to show any priority level (e.g. WARN) will show every more serious level also (e.g. ERROR and FATAL).

##### Adding a log to a class
To add a log create a Logger object and initialize it with:  
`LogManager.getLogger(<yourClassName>.class.getName());`

##### Submit info to a log
To add info to the log, call the method of the level you want use:
`logger.warn(<String>);`

For logging errors:
`logger.error("The potential can't be converted to table", exceptionObject);`
Will print first the message, then the exception with its traceback.

Finally, the log is written in the folder that contains OpenMarkov per session, overwriting the previous log.