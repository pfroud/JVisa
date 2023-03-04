# JVisa

VISA (Virtual Instrument Software Architecture) is an API for communicating with test & measurement instruments.

JVisa is a library for using VISA instruments in a Java program.

JVisa has been tested on Windows 7, Windows 10, Windows 11, and macOS 10.15. I think it should work on Linux too.

A small number of VISA functions been implemented, but it's definitely enough to do instrument automation.

## Motivation

This project is a fork of G&uuml;nter Fuchs's [project of the same name](https://sourceforge.net/projects/jvisa/), which is hosted on SourceForge.

G&uuml;nter set up [JNAerator](https://github.com/nativelibs4java/JNAerator) and [Java Native Access](https://github.com/java-native-access/jna) to interact with VISA DLLs. That was a significant step forward.

However, there were some issues in the original JVisa:

* Confusing inheritance between the `JVisa` and `JVisaInstrument` classes 
* Only one instrument can be opened at a time
* C-style error handling and output arguments

This fork of JVisa addresses those issues.

## Getting started

### Install a VISA implementation

You must install a VISA implementation to use JVisa. **[National Instrument NI-VISA](https://www.ni.com/en-us/support/downloads/drivers/download.ni-visa.html) is recommended.**

As far as I know, four companies have written their own VISA implementation: Keysight, National Instruments, Rohde & Schwarz, and Tektronix.

The Nation Instruments implementation appears to be the most common. For instance, [PyVISA](https://pyvisa.readthedocs.io/en/latest/introduction/getting.html#backend) only supports NI-VISA, and Rigol software uses NI-VISA.

I tried out all four implementations and took notes and screenshots. See my [comparison of VISA implementations](comparison-of-visa-implementations).

### Add JVisa to your project

#### Using Maven, Gradle, etc

Theoretically you can use [JitPack](https://jitpack.io/) although I have not tried it with JVisa.

Example for Maven:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.pfroud</groupId>
    <artifactId>JVisa</artifactId>
    <version>JVisa-1.0</version>
</dependency>
```

#### Using a Jar file

To manually download JVisa jar files, go to https://github.com/pfroud/JVisa/releases.

JVisa depends on the [Java Native Access (JNA)](https://github.com/java-native-access/jna) library.

If you want a single jar file containing both JVisa and JNA, use `JVisa-[version]-with-dependencies.jar`.

If you already have a JNA jar file, you can use `JVisa-[version].jar`.

To download JNA jar files, I recommend https://mvnrepository.com/artifact/net.java.dev.jna/jna. First, click the version number of the newest version. Then, find the table row labeled "Files" and click on "jar".
 

### Basic usage

Start by creating a Resource Manager:

```java
JVisaResourceManager rm = new JVisaResourceManager();
```

You can search for available instruments, or you can directly open an instrument if you already know its resource name:
```java
String[] resourcesNames = rm.findResources();

JVisaInstrument instrument = rm.openInstrument("USB0::0xFFFF::0x1234::123456789123456789::INSTR");
```


Examples of how to interact with the instrument:
```java
String manufacturerName = instrument.getManufacturerName();
String modelName = instrument.getModelName();

instrument.write("source:voltage 12V");
instrument.write("output on");
String response = instrument.queryString("measure:current?");
```

When finished:
```java
instrument.close();
rm.close();
```

### Complete example code files

The [`jvisa_example`](src/main/java/xyz/froud/jvisa_example) folder contains a few example files: 

* The file [`FindResourcesExample.java`](src/main/java/xyz/froud/jvisa_example/FindResourcesExample.java) is a ready-to-run example of the `ResourceManager#findResources()` method.
* The file [`IdentificationQueryExample.java`](src/main/java/xyz/froud/jvisa_example/IdentificationQueryExample.java) shows how to open instruments and send a query.

There is also a small example of how to make a higher-level abstraction. The file [`AbstractionExample.java`](src/main/java/xyz/froud/jvisa_example/abstraction/AbstractionExample.java) shows how [`AbstractInstrument.java`](src/main/java/xyz/froud/jvisa_example/abstraction/AbstractInstrument.java) and [`PowerSupplyExample.java`](src/main/java/xyz/froud/jvisa_example/abstraction/PowerSupplyExample.java) let you call a method like `setVoltage(12)` instead of `write("source:voltage 12V")`. 

### Run examples from jar file

It is possible to run the examples directly from the jar file.

If you have a JVisa jar file which contains the JNA dependency:
```bash
java -classpath "C:\path\to\JVisa-[version]-with-dependencies.jar" xyz.froud.jvisa_example.FindResourcesExample
```

Otherwise, specify a path to a JNA jar file, separated with a semicolon:
```bash
java -classpath "C:\path\to\JVisa-[version].jar;C:\path\to\jna-[version].jar" xyz.froud.jvisa_example.FindResourcesExample
```

For documentation about the `java` command, see https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html.

## Glossary

Here are some initialisms you might encounter when learning about test & measurement software.

* USBTMC: USB Test & Measurement Class.

* IVI: [Interchangeable Virtual Instruments](http://www.ivifoundation.org/) and the Foundation with the same name. Their [Shared Components](  
http://www.ivifoundation.org/shared_components/Default.aspx) library 

*  IEEE-488 aka GPIB: [General Purpose Interface Bus](https://en.wikipedia.org/wiki/IEEE-488). Specifies a physical connector and the signals used.

* SCPI :  [Standard Commands for Programmable Instruments
](https://en.wikipedia.org/wiki/Standard_Commands_for_Programmable_Instruments). Gives us command syntax like `measure:volrage:dc?`.

* LXI: [LAN eXtensions for Instrumentation](https://en.wikipedia.org/wiki/LAN_eXtensions_for_Instrumentation). Communicate with test & measurement instruments over Ethernet.

* PXI: [PCI eXtensions for Instrumentation](https://en.wikipedia.org/wiki/PCI_eXtensions_for_Instrumentation). Communicate with test & measurement instruments over PCI. Based on [CompactPCI](https://en.wikipedia.org/wiki/CompactPCI), which is different from PCI on desktop motherboards.

* VXI: [VME eXtensions for Instrumentation](https://en.wikipedia.org/wiki/VME_eXtensions_for_Instrumentation). Communicate with test & measurement instruments over the [VMEbus](https://en.wikipedia.org/wiki/VMEbus).

* ASRL [stands for](https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visaresourcesyntaxandexamples.html) asynchronous serial. When using NI-VISA on Windows, the VISA resource name to comunicate through a serial port starts with "ASRL" instead of "COM". But watch out because the ASRL number may be different from the COM number!
