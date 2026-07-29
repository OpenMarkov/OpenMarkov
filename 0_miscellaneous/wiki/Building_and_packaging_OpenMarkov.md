<!-- TOC -->
* [1. The runnable jar (all operating systems)](#1-the-runnable-jar-all-operating-systems)
* [2. Native installers](#2-native-installers)
  * [2.1 The command](#21-the-command)
  * [2.2 What each operating system produces](#22-what-each-operating-system-produces)
  * [2.3 Windows](#23-windows)
  * [2.4 Linux](#24-linux)
  * [2.5 macOS](#25-macos)
  * [2.6 Changing the version number](#26-changing-the-version-number)
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
mvn -pl full -am -P GenerateFullJar clean package -DskipTests --batch-mode
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
registers a menu entry, a shortcut and the association for `.pgmx` files.

Installers are built by `jpackage`, a tool that comes with the JDK. **`jpackage` can only build
installers for the operating system it is running on**, so a Windows installer has to be built on
Windows and a Linux package on Linux. There is no way to cross-build them.

## 2.1 The command

The same command on every operating system:

```
mvn -pl full -am -P Installer clean package -DskipTests --batch-mode
```

The results appear in **`full/target/installer/`**. Which files appear there depends on the system
you ran it on; see the next section. The build never fails for asking for a format the system
cannot produce — those steps are skipped, and the log says so:

```
--- exec:3.1.1:exec (create-msi) @ full --- skipping execute as per configuration
```

Every piece of that command earns its place:

* **`-pl full -am`** builds the `full` module together with the modules it depends on (`-pl` is
  *projects list*, `-am` is *also make*). This matters: without `-am`, Maven does not rebuild those
  modules and instead takes them from your local repository (`~/.m2`), which may hold jars from
  weeks ago — you would get an installer full of stale code without any warning. With `-am` they are
  all built in the same Maven session and the installer carries the code you have right now.
* **`clean`** deletes `target/` first, so no class file from an earlier build survives into the
  installer.
* **`-DskipTests`** skips the test suite. Drop it if you want the tests to run first.

Running `mvn clean install -am -P Installer -DskipTests` from the root works too and is equally
safe about using current code; it is just slower, because it builds every module of the project
rather than only the ones `full` needs, and it also copies all of them into `~/.m2`.

Note that the continuous integration build **never builds installers** — it only publishes the jar.
Installers exist only when somebody builds them by hand, and nothing checks automatically that they
still work.

## 2.2 What each operating system produces

| Built on | Files you get in `full/target/installer/`                                       |
|----------|--------------------------------------------------------------------------------|
| Windows  | `OpenMarkov-0.3.msi`, `OpenMarkov-0.3.exe`, and the portable folder `OpenMarkov/` |
| Linux    | `openmarkov_0.3_amd64.deb`, `openmarkov-0.3-1.x86_64.rpm`, and the portable folder `OpenMarkov/` |
| macOS    | Nothing. Not set up; see [2.5](#25-macos).                                      |

The portable folder is an application image: it carries its own Java runtime and runs without being
installed, through `OpenMarkov/OpenMarkov.exe` on Windows or `OpenMarkov/bin/OpenMarkov` on Linux.
It is useful when the user cannot install software on their machine.

## 2.3 Windows

**Prerequisite: the WiX Toolset.** `jpackage` does not build `.msi` and `.exe` files by itself; it
drives the WiX Toolset, a separate open-source program that has to be installed and reachable from
the `PATH` environment variable. Download it from [wixtoolset.org](https://wixtoolset.org/).
Version 3.14 is the one that has been used with `jpackage` the longest; recent JDKs also accept
newer versions. If the build fails complaining that it cannot find WiX, this is the reason.

The `.msi` is the one most users want; the `.exe` is the same thing in the other Windows installer
format. Both let the user choose the install directory, add a start-menu entry and a shortcut, and
associate `.pgmx` files with OpenMarkov.

## 2.4 Linux

**Prerequisites.** Each package format needs its own program installed on the machine that builds
it:

| Format | Needs                                                                   |
|--------|-------------------------------------------------------------------------|
| `.deb` | `fakeroot` and `dpkg-deb` (the latter comes with the `dpkg` package)     |
| `.rpm` | `rpmbuild` (Debian and Ubuntu call the package `rpm`; Fedora and openSUSE call it `rpm-build`) |

On Debian or Ubuntu, `sudo apt install fakeroot dpkg rpm` covers both. If one of them is missing,
`jpackage` fails for that format only.

Use the `.deb` for Debian, Ubuntu and their derivatives, and the `.rpm` for Red Hat, Fedora,
openSUSE and theirs. Both install OpenMarkov under `/opt/openmarkov`, add a menu entry under
*Science*, and associate `.pgmx` files. The package is named `openmarkov`, in lower case, because
Debian does not allow capitals in package names, while the application itself keeps the name
`OpenMarkov`.

Install and remove them the usual way:

```
sudo apt install ./full/target/installer/openmarkov_0.3_amd64.deb    # Debian, Ubuntu
sudo dnf install ./full/target/installer/openmarkov-0.3-1.x86_64.rpm # Fedora, Red Hat
```

## 2.5 macOS

The build produces nothing on macOS, and completes without error. Two things are missing, and
neither is a matter of adding a few lines to the `pom.xml`:

* **The icon.** `jpackage` wants an `.icns` file on macOS, and the repository only has `.ico`
  (Windows) and `.png` (Linux). Giving it the wrong format is not a warning: `jpackage` stops with
  an error and leaves a half-built application image behind.
* **Signing.** Recent versions of macOS refuse to run an application that is not signed and
  *notarized* — sent to Apple for checking. That requires a paid Apple developer account. Without
  it, the installer would be built but users could not open it.

Because of this, the portable application image is enabled on Windows and Linux but left switched
off on macOS, where nobody in the team can test the result.

## 2.6 Changing the version number

The version the installers show (`0.3`) is **not** `${project.version}`, because `jpackage` rejects
any version carrying a suffix such as `-SNAPSHOT`. It lives in a single property near the top of
`full/pom.xml`:

```xml
<installer.app.version>0.3</installer.app.version>
```

Change it there and every format picks it up.

# 3. Troubleshooting

**The build fails and mentions WiX, `candle.exe` or `light.exe`.** The WiX Toolset is missing or is
not on the `PATH`. See section 2.3.

**`Invalid or unsupported type: [msi]`.** You are building on Linux or macOS with the skipping
disabled. The `.msi` and `.exe` formats are Windows-only.

**`The specified icon "…openmarkov.ico" is not a PNG file`.** Despite the wording, this is not a
warning: outside Windows `jpackage` stops with it and leaves the application image incomplete. It
means the icon property is resolving to the Windows icon on a machine that is not Windows.

**`Warning: app-image dir not generated by jpackage`.** Harmless. `jpackage` prints it while
building the `.rpm`; the package is produced correctly.

**The installer contains code from weeks ago.** You left out `-am`, so Maven took the other modules
from `~/.m2` instead of rebuilding them. See section 2.1.

**`OpenMarkov.jar` is nowhere under `full/target/`.** That is correct: the `GenerateFullJar`
profile writes it to the root of the repository. The jar that does sit in `full/target/` contains
only the classes of the `full` module and is not runnable on its own.

**The jar does not start on a user's machine.** The jar needs a JDK 25 or newer installed. Give
them an installer instead, which carries its own Java runtime.
