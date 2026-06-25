In OpenMarkov, tests are written using [JUnit5](https://junit.org/junit5/), which you can use through OpenMarkov's 
pom.xml if your project is a child of OpenMarkov, or you can get it as specified 
[here](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api) in the Maven Repository.

Table of contents:
<!-- TOC -->
* [Creating tests](#creating-tests)
* [Using multiple test functions in the same test class](#using-multiple-test-functions-in-the-same-test-class)
* [Try catch in tests](#try-catch-in-tests)
* [Executing only fast tests](#executing-only-fast-tests)
<!-- TOC -->

# Creating tests
Test classes are written in ``src/test/java``, in there you can create classes and mark functions 
with the ``@Test`` annotation, this will make JUnit to launch your function, and if it is able to 
complete it with no errors, it will consider that function test as completed.

This test class, for example, asserts adding 1 and 2 together results in 3:

```java
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyFirstTest {
    
    @Test public void testSum() {
        assertTrue(1+2==3);
    }
}
```

# Using multiple test functions in the same test class
You can define multiple functions in a test class that are marked with ``@Test``, the only catch on
this is how you use the fields of the test class, for this, JUnit also allows you to have fields in
your class, and you can initialize their values in a function called ``setUp``.

Said values might be changed by your different function tests, which might lead to a more complex 
design, which is usually avoided in OpenMarkov to offer clarity and ease of maintenance, to avoid 
this, we can mark the test class with ``@TestInstance(TestInstance.Lifecycle.PER_METHOD)``, and
also to mark the ``setUp`` function ``@BeforeEach``, making the class fields to be reinstantiated
per each test function call, as the own test class gets reinstantiated.

For example, this test class has two functions, each one of them uses the variable ```name``` that 
is initialized as ```OpenMarkov```, then the first test sets it to ``Markov``, but since the 
instance as new for the second test, this second test sees the value of `OpenMarkov`, not `Markov`:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MyFirstTest {
    private String name;
    
    @BeforeEach public void setUp() {
        this.name = "OpenMarkov";
    }
    
    @Test public void test1() {
        this.name = "Markov";
        assertEquals(this.name, "Markov");
    }
    
    @Test public void test2() {
        assertEquals(this.name, "OpenMarkov");
        assertTrue(1 + 2 == 3);
    }
}
```

# Try catch in tests
The test functions can use ``try`` and ``catch``, but you must beware of how you will be handling 
them, if catching a specific error means the test was wrong, you must tell JUnit the test has 
failed using 
[org.junit.jupiter.api.Assertions.fail](https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html#fail-java.lang.String-),
otherwise, if you catch it and the function can still run, it will finish the execution and it will
take it as passed, for example, this test where an overflow happens will actually be taken as
correct:

````java
@Test
public void wrong(){
	try{
		int overflow = Integer.MAX_VALUE/0;
	} catch (RuntimeException e){
		e.printStackTrace();
	}
}
````

To make it fail, we have two main alternatives:

- Throwing the error: By adding a throws clause to your function (Not required for Runtime
  exceptions), JUnit will catch the exception and then it will specify the test was wrong.
  <br>Using this alternative is little boilerplate, but it might be harder to detect where
  exceptions are being thrown in big test functions, making it ideal for small tests:

````java
@Test
public void rightThrowingException() throws ArithmeticException{
    int overflow = Integer.MAX_VALUE/0;
}
````

- Catching exceptions and failing: By surrounding code with try catch, you can call the
  [org.junit.jupiter.api.Assertions.fail](https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html#fail-java.lang.String-),
  function on the exceptions that should mean your function test has gone wrong.
  <br>This alternative allows a more refined control at the expense of more boilerplate code, which
  might be more ideal on big test functions:

````java
@Test
public void rightCatchingException(){
	try{
		int overflow = Integer.MAX_VALUE/0;
	} catch (RuntimeException e){
		fail(e);
	}
}
````

# Executing only fast tests
In OpenMarkov, test functions are marked with a speed parameter specifying how long it takes to 
complete them, having ``TestSpeed.SLOW`` for functions taking at least 300ms to complete, 
``TestSpeed.MEDIUM`` for functions taking between 50 and 300ms, and ``TestSpeed.FAST`` for functions
taking less than 50ms (Although the duration of these tests will depend on the machine that it 
executes it, meaning it can vary).

Marking them is as simple as to add ``@Tag(TestSpeed.YourSpeedHere)`` to the test, such as:

````java
@Tag(TestSpeed.SLOW)
@Test
public void mySlowTest(){
  ...
}
````

In OpenMarkov, slow and medium functions are marked with their respective speeds, although fast
functions aren't marked with it as most are fast to execute, this means executing fast tests is the 
same as executing all the tests that aren't tagged with Medium or Slow.

To simplify this, you can take the
[Run Configurations](https://bitbucket.org/cisiad/org.openmarkov.integrationtests/src/development/config/intellij/runConfigurations.zip)
of the integrationTests repository, and export it's contents inside the ``.idea`` directory of your
project.

![import configurations.png](https://bitbucket.org/repo/x8Mz4kE/images/1571684042-import%20configurations.png)

Now close IntelliJ and reopen it, you'll see three new configurations in the 
``Run / Debug Configurations``, and you can simply click on them to execute 
them:

![configurations.png](https://bitbucket.org/repo/x8Mz4kE/images/3243006683-configurations.png)

These configurations are the following:

- Test fast (Parallel): Executes only fast tests, and in parallel.
- Test medium (Parallel): Executes both fast and medium tests, and in parallel.
- Test all (Parallel): Executes only every tests, and in parallel.