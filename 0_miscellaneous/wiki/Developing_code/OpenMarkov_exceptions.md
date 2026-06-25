### OpenMarkov Exceptions ###

OpenMarkov exceptions use a specific frame to display a Swing error message and to access the text that should go into that message.

Imagine that you catch a `NullPointerException` and you want to tell the user that *there is no free lunch*:

##### Modyfing the text for the exception
1. Add the text you want to show the user (title and message) to the xml string database that adresses localization:
    1. In the plugin package that throws the exception
    2. In the gui package otherwise
2. The exception should be formatted as it's here:
```xml
    <Reality>
        <NoFreeLunch>
            <title value="No free lunch"/>
            <message value="Nothing is free and lunch above everything"/>
        </NoFreeLunch>
    </Reality>
```
Note that the token of this exception will be the list of enclosing fields separated by dots: `Reality.NoFreeLunch`

##### Creating the exception
1. Now create an `OpenMarkovException` with the token as argument.
2. Wrap this exception by a `LocalizedException` with the `OpenMarkovException` as first argument and the parent GUI component as second (you don't throw the localized exception)
3. The `LocalizedException.showException()` method will show an error message with the title and the body (message) from the xml.

The complete java code for the example above is:
```java
catch(NullPointerException e)  {
    LocalizedException NoFreeLunchException = new LocalizedException(new OpenMarkovException("Reality.NoFreeLunch"), this);
    NoFreeLunchException.showException();
}
```

#### Formatted message ####
`OpenMarkovException` formats strings the same way than `System.out.printf` does: `OpenMarkovException("No %s, then no %s, "lunch", "work")`. But it only allows string arguments.
The placeholder allow different commands:

`%-5s`

`%`   Beginning of format  
`-`   OPTIONAL. Use left-justification on minimal whitespaces. See below.  
`5`   Minimum whitespaces for the print. Right-justifies. Empty means 0.  
`s`   Type code (string coerced)
