# Install Java, an IDE, and Git

## 1. Install a Java Development Kit (JDK)

There are several JDKs, in two main tracks: [OpenJDK](https://openjdk.java.net), which is open-source, and [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html), which is commercial since version 9, as explained in [this page](https://www.oracle.com/java/technologies/java-se-support-roadmap.html). In order to compile OpenMarkov, you need version 8 or newer. 

Microsoft has compiled [OpenJDK 11 builds](https://www.microsoft.com/openjdk) for different operating systems; this is the option we recommend for Windows and iOS. Linux distributions usually include more recent versions of OpenJDK. If you wish to use the most recent version for any operating system, download the binary files from the [OpenJDK site](https://openjdk.java.net). 

Alternatively, you may use the [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) installer for your operating system, but if you intend to release compiled Java programs, keep in mind the restrictions specified in its licence. 

## 2. Install an IDE

If you wish to browse OpenMarkov's code or use it as an API, we recommend you to use an IDE, such as IntelliJ, Eclipse, or Netbeans. In this tutorial we explain the installation of IntelliJ and Eclipse on Windows and Linux. We do not cover other IDEs or other operating systems because none of the researchers in our group are using them, but the installation is similar.

### 2a. Install Eclipse

We recommend the latest version of Eclipse. Users of Linux distributions, such as Debian and derivatives (Ubuntu and Linux Mint), should avoid the distribution repositories because the Eclipse versions they contain are fairly outdated.

In [Eclipse's official page](https://www.eclipse.org/downloads/eclipse-packages/), download the installer for your operating system.

In Linux, unzip it and run the file *eclipse-inst*.

You will be greeted by a list with different installation bundles. Choose one of these:

- Eclipse IDE for Java Developers
- Eclipse IDE for Java EE Developers (it is larger because it contains more plugins),

and follow the installer instructions.


### 2b. Install IntelliJ

Get a version of IntelliJ < 2019.3 (newer versions don't import automatically maven subfolders [on March 2020]) for your operating system from [its official page](https://www.jetbrains.com/idea/download). Choose between:

- Community, which is open-source
- Ultimate, which is commercial.

In Linux, unzip the tar file and follow the instructions in *Install-Linux-tar*. 

The installation and update/downgrade of IntelliJ can be simplyfied greatly using the [JetBrains Toolbox](https://www.jetbrains.com/toolbox-app/) application, both in Windows and Linux.


## 3. Install Git

Maven is already included in both IntelliJ and Eclipse, but Git is not. 

In Windows, install it from [its official page](https://git-scm.com/).

In Linux, type in the terminal:

`sudo apt install git` (where *apt* will be your distribution package manager).

When you are done, [**clone OpenMarkov**](https://bitbucket.org/cisiad/org.openmarkov/wiki/First_download) to your computer.