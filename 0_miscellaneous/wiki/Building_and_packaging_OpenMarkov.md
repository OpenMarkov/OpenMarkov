<!-- TOC -->
* [1. The runnable jar (all operating systems)](#1-the-runnable-jar-all-operating-systems)
* [2. Native installers](#2-native-installers)
  * [2.1 What is available on each operating system](#21-what-is-available-on-each-operating-system)
  * [2.2 Building the Windows installers](#22-building-the-windows-installers)
  * [2.3 Linux and macOS](#23-linux-and-macos)
* [3. Troubleshooting](#3-troubleshooting)
<!-- TOC -->

This page explains how to produce something you can hand to a user: either a runnable jar or a
native installer. It assumes you already have a working OpenMarkov project, as described in
[Setting up an OpenMarkov project in an IDE](Setting_up_an_OpenMarkov_project_in_an_IDE.md).

Run every command from the root of the repository.

# 1. The runnable jar (all operating systems)

This is the quickest way to get a working copy of OpenMarkov, and the one you want most of the
time:

```
mvn -pl full -am -P GenerateFullJar package -DskipTests
```

It produces **`OpenMarkov.jar`** in the **root of the repository** — not under `full/target/`, which
is where Maven output usually goes. Run it with:

```
java -jar OpenMarkov.jar
```

The jar bundles every library OpenMarkov needs, and the same file works on Windows, Linux and
macOS. This is also what the continuous integration build publishes, so a jar that works for you
works for everybody else.

Its one requirement is that whoever runs it has a **JDK 25 or newer** installed. That is fine for
developers, but not for an end user who just wants to try OpenMarkov out — which is what the
installers below are for.

Drop `-DskipTests` if you want the full test suite to run first. It takes considerably longer.

# 2. Native installers

An installer bundles a Java runtime inside it, so the user does not have to install Java. It also
registers a start-menu entry, a desktop shortcut and the association for `.pgmx` files.

Installers are built by `jpackage`, a tool that comes with the JDK. **`jpackage` can only build
installers for the operating system it is running on**, so a Windows installer has to be built on
Windows. There is no way to build one from Linux or macOS.

## 2.1 What is available on each operating system

| Operating system | Status                                                                  |
|------------------|-------------------------------------------------------------------------|
| Windows          | Supported. Produces an `.msi` installer, an `.exe` installer and a portable application image. |
| Linux            | Not set up in the project yet. `jpackage` can produce `.deb` and `.rpm` packages, but nothing in the build calls it. |
| macOS            | Not set up in the project yet, and never tested.                        |

Note also that the continuous integration build runs on Linux and **never builds installers** — it
only publishes the jar. Installers exist only when somebody builds them by hand, and nothing
checks automatically that they still work.

## 2.2 Building the Windows installers

**Prerequisite: the WiX Toolset.** `jpackage` does not build `.msi` and `.exe` files by itself; it
drives the WiX Toolset, a separate open-source program that has to be installed and reachable from
the `PATH` environment variable. Download it from
[wixtoolset.org](https://wixtoolset.org/). Version 3.14 is the one that has been used with
`jpackage` the longest; recent JDKs also accept newer versions. If the build fails complaining that
it cannot find WiX, this is the reason.

Then, on a Windows machine:

```
mvn -pl full -am -P Installer package -DskipTests
```

The results appear in **`full/target/installer/`**:

* `OpenMarkov-0.3.msi` — the installer most users want.
* `OpenMarkov-0.3.exe` — the same thing in the other Windows installer format.
* `OpenMarkov/` — a portable folder that runs without installing anything, through
  `OpenMarkov/OpenMarkov.exe`.

The installers offer the user a choice of install directory, add a start-menu entry and a shortcut,
and associate `.pgmx` files with OpenMarkov.

## 2.3 Linux and macOS

On these systems the command above completes successfully but **produces no installers**: it
reports that it is skipping the `.msi` and `.exe` steps, because those formats do not exist outside
Windows. This is expected, not a failure.

If you need a Linux package in the meantime, `jpackage` can be called by hand once the build has
prepared the libraries. First run the command above so that `full/target/libs/` is populated, then:

```
jpackage --type deb --dest full/target/installer --name openmarkov --app-version 0.3 \
  --input full/target/libs --main-jar full-0.3.0-SNAPSHOT.jar \
  --main-class org.openmarkov.full.OpenMarkov \
  --icon gui/src/main/resources/icons/openmarkov.png \
  --linux-shortcut --license-file LICENSE
```

Use `--type rpm` for Red Hat-based distributions, or `--type app-image` for a portable folder. Two
details differ from the Windows configuration: the icon must be the `.png` one, because `.ico` is a
Windows format, and the package name must be lower case, because Debian package names do not allow
capitals.

# 3. Troubleshooting

**The build fails and mentions WiX, `candle.exe` or `light.exe`.** The WiX Toolset is missing or is
not on the `PATH`. See section 2.2.

**`Invalid or unsupported type: [msi]`.** You are building on Linux or macOS with the skipping
disabled. The `.msi` and `.exe` formats are Windows-only.

**`OpenMarkov.jar` is nowhere under `full/target/`.** That is correct: the `GenerateFullJar`
profile writes it to the root of the repository. The jar that does sit in `full/target/` contains
only the classes of the `full` module and is not runnable on its own.

**The jar does not start on a user's machine.** The jar needs a JDK 25 or newer installed. Give
them an installer instead, which carries its own Java runtime.
