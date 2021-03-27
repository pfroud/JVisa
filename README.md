# JVisa

VISA (Virtual Instrument Software Architecture) is an API for communicating with test & measurement instruments.

JVisa is a library for using VISA instruments in a Java program.


## Motivation

This project is a fork of G&uuml;nter Fuchs's [project of the same name](https://sourceforge.net/projects/jvisa/), which is hosted on SourceForge.

G&uuml;nter set up [JNAerator](https://github.com/nativelibs4java/JNAerator) and [Java Native Access](https://github.com/java-native-access/jna) to interact with VISA DLLs. That was a significant step forward.

However, there are some issues in the original JVisa:

* Confusing inheritance between the `JVisa` and `JVisaInstrument` classes 
* Only one instrument can be opened at a time
* C-style error handling and output arguments

This fork of JVisa addresses those issues.

## Getting started

### Installing a VISA implementation

You must install a VISA implementation to use JVisa. **[National Instrument NI-VISA](https://www.ni.com/en-us/support/downloads/drivers/download.ni-visa.html) is recommended.**

As far as I know, four companies have written their own VISA implementation: Keysight, National Instruments, Rohde & Schwarz, and Tektronix.

The Nation Instruments implementation appears to be the most common. For instance, [PyVISA](https://pyvisa.readthedocs.io/en/master/getting.html) only supports NI-VISA, and Rigol software uses NI-VISA.

I tried out all four implementations and took notes and screenshots. See my [comparison of VISA implementations](comparison-of-visa-implementations)..

### Example code

The [`jvisa_example`](src/jvisa_example) folder contains a few example files. 

The file `LowLevelExample.java` shows how to use the JVisa classes directly.

There is also a small example of how to make a higher-level abstraction. The file `HighLevelExample.java` shows how `AbstractInstrument.java` and `PowerSupplyExample.java` let you call a method like `setVoltage(12)` instead of `write("source:voltage 12V")`. 

## Limitations

This fork of JVisa has only been tested on 64-bit Windows 7 and 10.

Not all VISA command have methods implemented, but it's definitley enough to do instrument automation.

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

