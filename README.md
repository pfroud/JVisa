# JVisa

<!-- TODO consider adding AbstractInstrument -->
<!-- TODO add example code -->

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

## Installing a VISA implementation

Before using JVisa, you must install a VISA implementation.

[According](https://www.tek.com/support/faqs/what-tekvisa-and-how-can-i-use-it-communicate-and-control-my-instrument) to Tektronix,

>Each VISA [implementation] is comprised of a communications driver, a USBTMC driver (USB Test and Measurement Class driver), a VISA software library and documentation, an instrument connection manager, an instrument communication tool, and an instrument communication logger.

As far as I know, four companies have written their own VISA implementation: National Instruments, Keysight, Tektronix, and Rohde & Schwarz.

The Nation Instruments one appears to be the most well-regarded. (For instance, [PyVISA](https://pyvisa.readthedocs.io/en/master/getting.html) supports only National Instruments.)

See my comparison of those VISA implementation [here](comparison-of-visa-implementations).


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

