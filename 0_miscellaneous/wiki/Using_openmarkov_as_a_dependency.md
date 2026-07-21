## Using OpenMarkov as an external dependency or an API

If you have a Maven project, and you wish to use OpenMarkov as an external dependency, the simplest
(but not only) way of doing so is by using [JitPack](https://jitpack.io/), which requires you to add
the ``jitpack.io`` repository to your pom.xml, like this:

````xml

<project>
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
</project>
````

Once added, you can add the modules you want from OpenMarkov, to your ``<repositories>`` like this:

````xml

<dependency>
    <groupId>com.github.OpenMarkov.OpenMarkov</groupId>
    <artifactId>*Your desired module name*</artifactId>
    <version>development-SNAPSHOT</version>
</dependency>
````

Note that if an OpenMarkov's module (``parent``) depends on another (``child``), then depending on
it will give you both (So if ``parent`` depends on ``child``, adding ``parent`` will also give you
``child``, but only adding ``child`` won't give you ``parent``). This means that, since there is a
module named ``full`` that depends on most of the modules, depending on ``full`` would give you
access to most of the modules in a single go.

As an example, we have created the project
[Example API](https://github.com/OpenMarkov/exampleapi/tree/development). You can clone it on your
computer using Git and rename it, and you can check the ```pom.xml``` file to notice it depends on
the ``io`` and ``inference`` modules.