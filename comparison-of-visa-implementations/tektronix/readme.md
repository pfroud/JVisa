# Tektronix TekVISA aka OpenChoice 

## Basic info

**Version tested:** 4.1.1.22

**Installer size:** 99.9 MB

**Link:** [download link](https://www.tek.com/oscilloscope/tds7054-software/tekvisa-connectivity-software-v411)

**DLL name:** `tkVisa32.dll` and `tkVisa64.dll`

**Documentation:** 266-page PDF programming manual and a two-page PDF reference card. The reference card has a list of functions with one-sentence descriptions, a table of attributes, and four small code examples.

**Examples:** includes twelve C examples and three Matlab examples.

## Discussion

The name of this software is confusing. I think Tektonix's VISA implementation is called TekVISA, but all the GUI programs are called OpenChoice.

Speaking of the GUI, the design is hilariously outdated. Everything uses a violet color theme, sometimes with a textured gradient background. All the buttons are pill-shaped. The design suggests that this software hasn't been updated in a long time.

### Integration between tools

Instrument Manager can other TekVISA utilities, and you can add your own to the list:

<p align="center" style="text-align: center">
<img src="tek-add-entry.png?raw=true" alt="Dialog to add entry to list of utilities">
</p>

Some TekVISA utilities have a Tools dropdown menu where you can launch other TekVISA utilities:

<p align="center" style="text-align: center">
<img src="tek-tools-menus.png?raw=true" alt="TekVISA tools drop-down menu">
</p>

The Talker Lister program has Notepad in its Tools menu (above left). When you click it, Windows Notepad opens like normal, but then Talker Listener starts typing into the blank Notepad file: `Talker Listener Script: <<Script0>>`
How weird! That would be super confusing if you weren't expecting it.

You can't open the programming manual or examples from any of the Help menus.

Instrument aliases are set with the Properties button in Instrument Manager.

### System tray icon

A system tray icon has a little popup menu where you can start Instrument Manager and Call Monitor:

<p align="center" style="text-align: center">
<img src="tek-system-tray.png?raw=true" alt="TekVISA system tray menu">
</p>

### Install location

TekVisa gets installed in this folder: `C:\Program Files\IVI Foundation\VISA\Win64\TekVISA`.

That's *inside* the folder for [IVI Foundation](https://www.ivifoundation.org/), which is the group who maintains the VISA specification. \

The other three VISA implementations make their own folder in Program Files. This should follow the same convention.

TekVISA should be installed in its own folder in Program Files, but there's no way to change the install location.

### Start menu folder

The installer makes a folder in the Start Menu with links to tools, documentation, and examples:

<p align="center" style="text-align: center">
<img src="tek-start-menu-folder.png?raw=true" alt="TekVISA start menu folder">
</p>

Looks good. But, some of the shortcuts don't work! Here's the target of Programming Manual shortcut:

`C:\Users\Peter\Desktop\AcroRd32.exe C:\PROGRA~1\IVIFOU~1\VISA\Win64\TekVISA\Manuals\TekVISAProgMan.pdf`

Yes, they put shortcuts to a PDF reader instead of the PDF itself! This doesn't work because I don't have Acrobat Reader installed, and it would definitely not get installed to the desktop. How extraordinary.

## Screenshots

Instrument Manager:
<p align="center" style="text-align: center">
<img src="tek-instrument-manager.PNG?raw=true" alt="TekVISA Instrument Manager screenshot">
</p>

Talker Listener:
<p align="center" style="text-align: center">
<img src="tek-talker-listener.PNG?raw=true" alt="TekVISA Talker Listener screenshot">
</p>

Call Monitor:
<p align="center" style="text-align: center">
<img src="tek-call-monitor.PNG?raw=true" alt="TekVISA Call Monitor screenshot">
</p>

Conflict Manager:
<p align="center" style="text-align: center">
<img src="tek-conflict-manager.PNG?raw=true" alt="TekVISA Conflict Manager screenshot">
</p>
