# Implementation status

The list of functions and attributes in this document is from the VISA specification. To access the spec, go to https://www.ivifoundation.org/specifications/default.html#visa-specifications then find the row labeled "VPP-4.3: The VISA Library".

This document corresponds to Revision 7.2 from May 19, 2022. Direct link to that revision: https://www.ivifoundation.org/downloads/VISA/vpp43_2022-05-19.pdf

* The Function column has a link the NI-VISA documentation for that function.
* The contents of the Purpose column is copied from the VISA specification.
* The Status column shows whether JVisa has an abstracted wrapper method for the function. 
    * "Not written" means I haven't written a wrapper method but could write one in the future.
    * "Won't implement" means the functionality should be written in your Java program.


## 3.3.1 Lifecycle Operations

| VISA spec section | Function                                                                                              | Purpose                                           | Status                                                                                                           |
|-------------------|-------------------------------------------------------------------------------------------------------|---------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| 3.3.1.1           | [`viClose()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viclose.html) | Close the specified session, event, or find list. | ✅ Used in `JVisaResourceManager.close()`, `JVisaInstrument.close()`, and `JVisaResourceManager.findResources()`. |


## 3.4.1 Characteristic Control Operations

| VISA spec section | Function                                                                                                            | Purpose                                                                        | Status                                            |
|-------------------|---------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|---------------------------------------------------|
| 3.4.1.1           | [`viGetAttribute()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vigetattribute.html) | Retrieve the state of an attribute.                                            | ✅ `JVisaInstrument.getAttribute*()`               |
| 3.4.1.2           | [`viSetAttribute()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/visetattribute.html) | Set the state of an attribute.                                                 | ✅ `JVisaInstrument.setAttribute()`                |
| 3.4.1.3           | [`viStatusDesc()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vistatusdesc.html)     | Return a user-readable description of the status code passed to the operation. | ✅ `JVisaResourceManager.getMessageForErrorCode()` |


## 3.5.1 Asynchronous Operation Control Operations

| VISA spec section | Function                                                                                                      | Purpose                                                               | Status      |
|-------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|-------------|
| 3.5.1.1           | [`viTerminate()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viterminate.html) | Request a VISA session to terminate normal execution of an operation. | Not written |


## 3.6.2 Access Control Operations

| VISA spec section | Function                                                                                                | Purpose                                             | Status       |
|-------------------|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------|--------------|
| 3.6.2.1           | [`viLock()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vilock.html)     | Establish an access mode to the specified resource. | Not written  |
| 3.6.2.2           | [`viUnlock()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viunlock.html) | Relinquish a lock for the specified resource.       | Not written  |


## 3.7.3 Event Operations

| VISA spec section | Function                                                                                                                    | Purpose                                                                          | Status                                   |
|-------------------|-----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|------------------------------------------|
| 3.7.3.1           | [`viEnableEvent()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vienableevent.html)           | Enable notification of a specified event.                                        | ✅ `JVisaInstrument.enableEvent()`        |
| 3.7.3.2           | [`viDisableEvent()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vidisableevent.html)         | Disable notification of an event type by the specified mechanisms.               | ✅ `JVisaInstrument.disableEvent()`       |
| 3.7.3.3           | [`viDiscardEvents()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vidiscardevents.html)       | Discard event occurrences for specified event types and mechanisms in a session. | ✅ `JVisaInstrument.discardEvents()`      |
| 3.7.3.4           | [`viWaitOnEvent()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viwaitonevent.html)           | Wait for an occurrence of the specified event for a given session.               | Not written                              |
| 3.7.3.5           | [`viInstallHandler()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viinstallhandler.html)     | Install handlers for event callbacks.                                            | ✅ `JVisaInstrument.installHandler()`     |
| 3.7.3.6           | [`viUninstallHandler()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viuninstallHandler.html) | Uninstall handlers for events.                                                   | ✅ `JVisaInstrument.removeEventHandler()` |
| 3.7.3.7           | [`viEventHandler()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vieventhandler.html)         | Event service handler procedure prototype.                                       | ✅ `interface JVisaEventCallback`         |


## 4.3.3 Access Functions and Operations

| VISA spec section | Function                                                                                                              | Purpose                                                        | Status                                                |
|-------------------|-----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------|-------------------------------------------------------|
| 4.3.3.1           | [`viOpenDefaultRM()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viopendefaultrm.html) | Return a session to the Default Resource Manager resource.     | ✅ `JVisaResourceManager` constructor                  |
| 4.3.3.2           | [`viOpen()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viopen.html)                   | Open a session to the specified device.                        | ✅ `JVisaResourceManager.openInstrument()`             |
| 4.3.3.3           | [`viParseRsrc()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viparsersrc.html)         | Parse a resource string to get the interface information.      | Not written                                           |
| 4.3.3.4           | [`viParseRsrcEx()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viparsersrcex.html)     | Parse a resource string to get extended interface information. | ✅ Used in `JVisaResourceManager.getInstrumentAlias()` |


## 4.4.2 Search Operations

| VISA spec section | Function                                                                                                    | Purpose                                                                            | Status                                   |
|-------------------|-------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|------------------------------------------|
| 4.4.2.1           | [`viFindRsrc()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vifindrsrc.html) | Query a VISA system to locate the resources associated with a specified interface. | ✅ `JVisaResourceManager.findResources()` |
| 4.4.2.2           | [`viFindNext()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vifindnext.html) | Return the next resource found during a previous call to `viFindRsrc()`.           | ✅ `JVisaResourceManager.findResources()` |


## 6.1 Basic I/O Services

| VISA spec section | Function                                                                                                              | Purpose                                                            | Status                          |
|-------------------|-----------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|---------------------------------|
| 6.1.1             | [`viRead()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viread.html)                   | Read data from device synchronously.                               | ✅ `JVisaInstrument.readBytes()` |
| 6.1.2             | [`viReadAsync()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vireadasync.html)         | Read data from device asynchronously.                              | Not written                     |
| 6.1.3             | [`viReadToFile()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vireadtofile.html)       | Read data synchronously, and store the transferred data in a file. | ❌ Won't implement               |
| 6.1.4             | [`viWrite()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viwrite.html)                 | Write data to device synchronously.                                | ✅ `JVisaInstrument.write()`     |
| 6.1.5             | [`viWriteAsync()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viwriteasync.html)       | Write data to device asynchronously.                               | Not written                     |
| 6.1.6             | [`viWriteFromFile()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viwritefromfile.html) | Take data from a file and write it out synchronously.              | ❌ Won't implement               |
| 6.1.7             | [`viAssertTrigger()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viasserttrigger.html) | Assert software or hardware trigger.                               | Not written                     |
| 6.1.8             | [`viReadSTB()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vireadstb.html)             | Read a status byte of the service request.                         | Not written                     |
| 6.1.9             | [`viClear()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viclear.html)                 | Clear a device.                                                    | ✅ `JVisaInstrument.clear()`     |


## 6.2 Formatted I/O Services

| VISA spec section | Function                                                                                                    | Purpose                                                                                                                    | Status            |
|-------------------|-------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|-------------------|
| 6.2.1             | [`viSetBuf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/visetbuf.html)     | Set the size for the formatted I/O and/or serial communication buffer(s).                                                  | ❌ Won't implement |
| 6.2.2             | [`viFlush()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viflush.html)       | Manually flush the specified buffers associated with formatted I/O operations and/or serial communication.                 | ❌ Won't implement |
| 6.2.3             | [`viPrintf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viprintf.html)     | Convert, format, and send the parameters `arg1, arg2,` ... to the device as specified by the format string.                | ❌ Won't implement |
| 6.2.4             | [`viVPrintf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vivprintf.html)   | Convert, format, and send `params` to the device as specified by the format string.                                        | ❌ Won't implement |
| 6.2.5             | [`viSPrintf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/visprintf.html)   | Same as `viPrintf()`, except the data is written to a user-specified buffer rather than the device.                        | ❌ Won't implement |
| 6.2.6             | [`viVSPrintf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vivsprintf.html) | Same as `viVPrintf()`, except that the data is written to a user-specified buffer rather than a device.                    | ❌ Won't implement |
| 6.2.7             | [`viBufWrite()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vibufwrite.html) | Similar to `viWrite()`, except the data is written to the formatted I/O write buffer rather than directly to the device.   | ❌ Won't implement |
| 6.2.8             | [`viScanf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viscanf.html)       | Read, convert, and format data using the format specifier. Store the formatted data in the `arg1, arg2` parameters.        | ❌ Won't implement |
| 6.2.9             | [`viVScanf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vivscanf.html)     | Read, convert, and format data using the format specifier. Store the formatted data in `params` .                          | ❌ Won't implement |
| 6.2.10            | [`viSScanf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/visscanf.html)     | Same as `viScanf()` , except that the data is read from a user-specified buffer instead of a device.                       | ❌ Won't implement |
| 6.2.11            | [`viVSScanf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vivsscanf.html)   | Same as `viVScanf()`, except that the data is read from a user-specified buffer instead of a device.                       | ❌ Won't implement |
| 6.2.12            | [`viBufRead()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vibufread.html)   | Similar to `viRead()`, except that the operation uses the formatted I/O read buffer for holding data read from the device. | ❌ Won't implement |
| 6.2.13            | [`viQueryf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viqueryf.html)     | Perform a formatted write and read through a single operation invocation.                                                  | ❌ Won't implement |
| 6.2.14            | [`viVQueryf()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vivqueryf.html)   | Perform a formatted write and read through a single operation invocation.                                                  | ❌ Won't implement |


## 6.3 Memory I/O Services

| VISA spec section | Function                                                                                                                               | Status       |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------|--------------|
| 6.3.1             | [`viIn8()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viin8_viin16_viin32.html)                        | Not written  |
| 6.3.2             | [`viIn16()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viin8_viin16_viin32.html)                       | Not written  |
| 6.3.3             | [`viIn32()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viin8_viin16_viin32.html)                       | Not written  |
| 6.3.4             | [`viIn64()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viin8_viin16_viin32.html)                       | Not written  |
| 6.3.5             | [`viOut8()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viout8_viout16_viout32.html)                    | Not written  |
| 6.3.6             | [`viOut16()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viout8_viout16_viout32.html)                   | Not written  |
| 6.3.7             | [`viOut32()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viout8_viout16_viout32.html)                   | Not written  |
| 6.3.8             | [`viOut64()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viout8_viout16_viout32.html)                   | Not written  |
| 6.3.9             | [`viMoveIn8()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)        | Not written  |
| 6.3.10            | [`viMoveIn16()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)       | Not written  |
| 6.3.11            | [`viMoveIn32()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)       | Not written  |
| 6.3.12            | [`viMoveIn64()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)       | Not written  |
| 6.3.13            | [`viMoveIn8Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)      | Not written  |
| 6.3.14            | [`viMoveIn16Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)     | Not written  |
| 6.3.15            | [`viMoveIn32Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)     | Not written  |
| 6.3.16            | [`viMoveIn64Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimovein8_vimovein16_vimovein32.html)     | Not written  |
| 6.3.17            | [`viMoveOut8()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html)    | Not written  |
| 6.3.18            | [`viMoveOut16()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html)   | Not written  |
| 6.3.19            | [`viMoveOut32()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html)   | Not written  |
| 6.3.20            | [`viMoveOut64()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html)   | Not written  |
| 6.3.21            | [`viMoveOut8Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html)  | Not written  |
| 6.3.22            | [`viMoveOut16Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html) | Not written  |
| 6.3.23            | [`viMoveOut32Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html) | Not written  |
| 6.3.24            | [`viMoveOut64Ex()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveout8_vimoveout16_vimoveout32.html) | Not written  |
| 6.3.25            | [`viMove()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimove.html)                                    | Not written  |
| 6.3.26            | [`viMoveEx()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimove.html)                                  | Not written  |
| 6.3.27            | [`viMoveAsync()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveasync.html)                          | Not written  |
| 6.3.28            | [`viMoveAsyncEx()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimoveasync.html)                        | Not written  |
| 6.3.29            | [`viMapAddress()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimapaddress.html)                        | Not written  |
| 6.3.30            | [`viMapAddressEx()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimapaddress.html)                      | Not written  |
| 6.3.31            | [`viUnmapAddress()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viunmapaddress.html)                    | Not written  |
| 6.3.32            | [`viPeek8()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipeek8_vipeek16_vipeek32.html)                | Not written  |
| 6.3.33            | [`viPeek16()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipeek8_vipeek16_vipeek32.html)               | Not written  |
| 6.3.34            | [`viPeek32()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipeek8_vipeek16_vipeek32.html)               | Not written  |
| 6.3.35            | [`viPeek64()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipeek8_vipeek16_vipeek32.html)               | Not written  |
| 6.3.36            | [`viPoke8()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipoke8_vipoke16_vipoke32.html)                | Not written  |
| 6.3.37            | [`viPoke16()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipoke8_vipoke16_vipoke32.html)               | Not written  |
| 6.3.38            | [`viPoke32()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipoke8_vipoke16_vipoke32.html)               | Not written  |
| 6.3.39            | [`viPoke64()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipoke8_vipoke16_vipoke32.html)               | Not written  |


## 6.4 Shared Memory Services

| VISA spec section | Function                                                                                                        | Purpose                                                                    | Status      |
|-------------------|-----------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|-------------|
| 6.4.1             | [`viMemAlloc()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimemalloc.html)     | Allocate memory from a device’s memory region.                             | Not written |
| 6.4.2             | [`viMemAllocEx()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimemallocex.html) | Allocate memory from a device’s memory region.                             | Not written |
| 6.4.3             | [`viMemFree()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimemfree.html)       | Free memory previously allocated using `viMemAlloc()` or `viMemAllocEx()`. | Not written |
| 6.4.4             | [`viMemFreeEx()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimemfreeex.html)   | Free memory previously allocated using `viMemAlloc()` or `viMemAllocEx()`. | Not written |


## 6.5 Interface Specific Services

| VISA spec section | Function                                                                                                                        | Purpose                                                                                                                     | Not written in JVisa |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|----------------------|
| 6.5.1             | [`viGpibControlREN()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vigpibcontrolren.html)         | Controls the state of the GPIB REN interface line, and optionally the remote/local state of the device.                     | Not written          |
| 6.5.2             | [`viGpibControlATN()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vigpibcontrolatn.html)         | Controls the state of the GPIB ATN interface line, and optionally the active controller state of the local interface board. | Not written          |
| 6.5.3             | [`viGpibSendIFC()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vigpibsendifc.html)               | Pulse the interface clear line (IFC) for at least 100 us.                                                                   | Not written          |
| 6.5.4             | [`viGpibCommand()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vigpibcommand.html)               | Write GPIB command bytes on the bus.                                                                                        | Not written          |
| 6.5.5             | [`viGpibPassControl()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vigpibpasscontrol.html)       | Tell the GPIB device at the specified address to become controller in charge (CIC).                                         | Not written          |
| 6.5.6             | [`viVxiCommandQuery()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vivxicommandquery.html)       | Send the device a miscellaneous command or query and/or retrieve the response to a previous query.                          | Not written          |
| 6.5.7             | [`viAssertIntrSignal()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viassertintrsignal.html)     | Asserts the specified device interrupt or signal.                                                                           | Not written          |
| 6.5.8             | [`viAssertUtilSignal()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viassertutilsignal.html)     | Asserts the specified utility bus signal.                                                                                   | Not written          |
| 6.5.9             | [`viMapTrigger()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vimaptrigger.html)                 | Map the specified trigger source line to the specified destination line.                                                    | Not written          |
| 6.5.10            | [`viUnmapTrigger()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viunmaptrigger.html)             | Undo a previous map from the specified trigger source line to the specified destination line.                               | Not written          |
| 6.5.11            | [`viUsbControlOut()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viusbcontrolout.html)           | Send arbitrary data to the USB device on the control port.                                                                  | Not written          |
| 6.5.12            | [`viUsbControlIn()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/viusbcontrolin.html)             | Request arbitrary data from the USB device on the control port.                                                             | Not written          |
| 6.5.13            | [`viPxiReserveTriggers()`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vipxireservetriggers.html) | Reserves multiple trigger lines that the caller can then map and/or assert.                                                 | Not written          |



## Attributes

todo add columns for read and write

| Attribute                                                                                                                                                                         | Status |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------| 
| [`VI_ATTR_4882_COMPLIANT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_4882_compliant.html)                                                 | status |
| [`VI_ATTR_ASRL_ALLOW_TRANSMIT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_allow_transmit.html)                                       | status |
| [`VI_ATTR_ASRL_AVAIL_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_avail_num.html)                                                 | status |
| [`VI_ATTR_ASRL_BAUD`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_baud.html)                                                           | status |
| [`VI_ATTR_ASRL_BREAK_LEN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_break_len.html)                                                 | status |
| [`VI_ATTR_ASRL_BREAK_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_break_state.html)                                             | status |
| [`VI_ATTR_ASRL_CONNECTED`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_connected.html)                                                 | status |
| [`VI_ATTR_ASRL_CTS_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_cts_state.html)                                                 | status |
| [`VI_ATTR_ASRL_DATA_BITS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_data_bits.html)                                                 | status |
| [`VI_ATTR_ASRL_DCD_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_dcd_state.html)                                                 | status |
| [`VI_ATTR_ASRL_DISCARD_NULL`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_discard_null.html)                                           | status |
| [`VI_ATTR_ASRL_DSR_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_dsr_state.html)                                                 | status |
| [`VI_ATTR_ASRL_DTR_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_dtr_state.html)                                                 | status |
| [`VI_ATTR_ASRL_END_IN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_end_in.html)                                                       | status |
| [`VI_ATTR_ASRL_END_OUT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_end_out.html)                                                     | status |
| [`VI_ATTR_ASRL_FLOW_CNTRL`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_flow_cntrl.html)                                               | status |
| [`VI_ATTR_ASRL_PARITY`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_parity.html)                                                       | status |
| [`VI_ATTR_ASRL_REPLACE_CHAR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_replace_char.html)                                           | status |
| [`VI_ATTR_ASRL_RI_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_ri_state.html)                                                   | status |
| [`VI_ATTR_ASRL_RTS_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_rts_state.html)                                                 | status |
| [`VI_ATTR_ASRL_STOP_BITS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_stop_bits.html)                                                 | status |
| [`VI_ATTR_ASRL_WIRE_MODE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_wire_mode.html)                                                 | status |
| [`VI_ATTR_ASRL_XOFF_CHAR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_xoff_char.html)                                                 | status |
| [`VI_ATTR_ASRL_XON_CHAR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_asrl_xon_char.html)                                                   | status |
| [`VI_ATTR_BUFFER`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_buffer.html)                                                                 | status |
| [`VI_ATTR_CMDR_LA`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_cmdr_la.html)                                                               | status |
| [`VI_ATTR_DEST_ACCESS_PRIV`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_dest_access_priv.html)                                             | status |
| [`VI_ATTR_DEST_BYTE_ORDER`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_dest_byte_order.html)                                               | status |
| [`VI_ATTR_DEST_INCREMENT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_dest_increment.html)                                                 | status |
| [`VI_ATTR_DEV_STATUS_BYTE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_dev_status_byte.html)                                               | status |
| [`VI_ATTR_DMA_ALLOW_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_dma_allow_en.html)                                                     | status |
| [`VI_ATTR_EVENT_TYPE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_event_type.html)                                                         | status |
| [`VI_ATTR_FDC_CHNL`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_fdc_chnl.html)                                                             | status |
| [`VI_ATTR_FDC_MODE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_fdc_mode.html)                                                             | status |
| [`VI_ATTR_FDC_USE_PAIR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_fdc_use_pair.html)                                                     | status |
| [`VI_ATTR_FILE_APPEND_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_file_append_en.html)                                                 | status |
| [`VI_ATTR_GPIB_ADDR_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_addr_state.html)                                               | status |
| [`VI_ATTR_GPIB_ATN_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_atn_state.html)                                                 | status |
| [`VI_ATTR_GPIB_CIC_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_cic_state.html)                                                 | status |
| [`VI_ATTR_GPIB_HS488_CBL_LEN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_hs488_cbl_len.html)                                         | status |
| [`VI_ATTR_GPIB_NDAC_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_ndac_state.html)                                               | status |
| [`VI_ATTR_GPIB_PRIMARY_ADDR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_primary_addr.html)                                           | status |
| [`VI_ATTR_GPIB_READDR_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_readdr_en.html)                                                 | status |
| [`VI_ATTR_GPIB_RECV_CIC_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_recv_cic_state.html)                                       | status |
| [`VI_ATTR_GPIB_REN_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_ren_state.html)                                                 | status |
| [`VI_ATTR_GPIB_SECONDARY_ADDR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_secondary_addr.html)                                       | status |
| [`VI_ATTR_GPIB_SRQ_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_srq_state.html)                                                 | status |
| [`VI_ATTR_GPIB_SYS_CNTRL_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_sys_cntrl_state.html)                                     | status |
| [`VI_ATTR_GPIB_UNADDR_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_gpib_unaddr_en.html)                                                 | status |
| [`VI_ATTR_IMMEDIATE_SERV`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_immediate_serv.html)                                                 | status |
| [`VI_ATTR_INTF_INST_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_intf_inst_name.html)                                                 | status |
| [`VI_ATTR_INTF_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_intf_num.html)                                                             | status |
| [`VI_ATTR_INTF_TYPE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_intf_type.html)                                                           | status |
| [`VI_ATTR_INTR_STATUS_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_intr_status_id.html)                                                 | status |
| [`VI_ATTR_IO_PROT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_io_prot.html)                                                               | status |
| [`VI_ATTR_JOB_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_job_id.html)                                                                 | status |
| [`VI_ATTR_MAINFRAME_LA`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_mainframe_la.html)                                                     | status |
| [`VI_ATTR_MANF_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_manf_id.html)                                                               | status |
| [`VI_ATTR_MANF_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_manf_name.html)                                                           | status |
| [`VI_ATTR_MAX_QUEUE_LENGTH`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_max_queue_length.html)                                             | status |
| [`VI_ATTR_MEM_BASE/VI_ATTR_MEM_BASE_32/VI_ATTR_MEM_BASE_64`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_mem_base.html)                     | status |
| [`VI_ATTR_MEM_SIZE/VI_ATTR_MEM_SIZE_32/VI_ATTR_MEM_SIZE_64`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_mem_size.html)                     | status |
| [`VI_ATTR_MEM_SPACE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_mem_space.html)                                                           | status |
| [`VI_ATTR_MODEL_CODE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_model_code.html)                                                         | status |
| [`VI_ATTR_MODEL_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_model_name.html)                                                         | status |
| [`VI_ATTR_OPER_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_oper_name.html)                                                           | status |
| [`VI_ATTR_PXI_ACTUAL_LWIDTH`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_actual_lwidth.html)                                           | status |
| [`VI_ATTR_PXI_BUS_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_bus_num.html)                                                       | status |
| [`VI_ATTR_PXI_CHASSIS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_chassis.html)                                                       | status |
| [`VI_ATTR_PXI_DEST_TRIG_BUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_dest_trig_bus.html)                                           | status |
| [`VI_ATTR_PXI_DEV_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_dev_num.html)                                                       | status |
| [`VI_ATTR_PXI_DSTAR_BUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_dstar_bus.html)                                                   | status |
| [`VI_ATTR_PXI_DSTAR_SET`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_dstar_set.html)                                                   | status |
| [`VI_ATTR_PXI_FUNC_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_func_num.html)                                                     | status |
| [`VI_ATTR_PXI_IS_EXPRESS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_is_express.html)                                                 | status |
| [`VI_ATTR_PXI_MAX_LWIDTH`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_max_lwidth.html)                                                 | status |
| [`VI_ATTR_PXI_MEM_BASE_BAR0/1/2/3/4/5`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_mem_base_barx.html)                                 | status |
| [`VI_ATTR_PXI_MEM_SIZE_BAR0/1/2/3/4/5`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_mem_size_barx.html)                                 | status |
| [`VI_ATTR_PXI_MEM_TYPE_BAR0/1/2/3/4/5`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_mem_type_barx.html)                                 | status |
| [`VI_ATTR_PXI_RECV_INTR_DATA`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_recv_intr_data.html)                                         | status |
| [`VI_ATTR_PXI_RECV_INTR_SEQ`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_recv_intr_seq.html)                                           | status |
| [`VI_ATTR_PXI_SLOT_LBUS_LEFT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_slot_lbus_left.html)                                         | status |
| [`VI_ATTR_PXI_SLOT_LBUS_RIGHT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_slot_lbus_right.html)                                       | status |
| [`VI_ATTR_PXI_SLOT_LWIDTH`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_slot_lwidth.html)                                               | status |
| [`VI_ATTR_PXI_SLOTPATH`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_slotpath.html)                                                     | status |
| [`VI_ATTR_PXI_SRC_TRIG_BUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_src_trig_bus.html)                                             | status |
| [`VI_ATTR_PXI_STAR_TRIG_BUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_star_trig_bus.html)                                           | status |
| [`VI_ATTR_PXI_STAR_TRIG_LINE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_star_trig_line.html)                                         | status |
| [`VI_ATTR_PXI_TRIG_BUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_pxi_trig_bus.html)                                                     | status |
| [`VI_ATTR_RD_BUF_OPER_MODE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rd_buf_oper_mode.html)                                             | status |
| [`VI_ATTR_RD_BUF_SIZE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rd_buf_size.html)                                                       | status |
| [`VI_ATTR_RECV_INTR_LEVEL`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_recv_intr_level.html)                                               | status |
| [`VI_ATTR_RECV_TRIG_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_recv_trig_id.html)                                                     | status |
| [`VI_ATTR_RET_COUNT/VI_ATTR_RET_COUNT_32/VI_ATTR_RET_COUNT_64`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_ret_count.html)                 | status |
| [`VI_ATTR_RM_SESSION`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rm_session.html)                                                         | status |
| [`VI_ATTR_RSRC_CLASS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_class.html)                                                         | status |
| [`VI_ATTR_RSRC_IMPL_VERSION`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_impl_version.html)                                           | status |
| [`VI_ATTR_RSRC_LOCK_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_lock_state.html)                                               | status |
| [`VI_ATTR_RSRC_MANF_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_manf_id.html)                                                     | status |
| [`VI_ATTR_RSRC_MANF_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_manf_name.html)                                                 | status |
| [`VI_ATTR_RSRC_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_name.html)                                                           | status |
| [`VI_ATTR_RSRC_SPEC_VERSION`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_rsrc_spec_version.html)                                           | status |
| [`VI_ATTR_SEND_END_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_send_end_en.html)                                                       | status |
| [`VI_ATTR_SIGP_STATUS_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_sigp_status_id.html)                                                 | status |
| [`VI_ATTR_SLOT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_slot.html)                                                                     | status |
| [`VI_ATTR_SRC_ACCESS_PRIV`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_src_access_priv.html)                                               | status |
| [`VI_ATTR_SRC_BYTE_ORDER`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_src_byte_order.html)                                                 | status |
| [`VI_ATTR_SRC_INCREMENT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_src_increment.html)                                                   | status |
| [`VI_ATTR_STATUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_status.html)                                                                 | status |
| [`VI_ATTR_SUPPRESS_END_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_suppress_end_en.html)                                               | status |
| [`VI_ATTR_TCPIP_ADDR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tcpip_addr.html)                                                         | status |
| [`VI_ATTR_TCPIP_DEVICE_NAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tcpip_device_name.html)                                           | status |
| [`VI_ATTR_TCPIP_HOSTNAME`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tcpip_hostname.html)                                                 | status |
| [`VI_ATTR_TCPIP_KEEPALIVE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tcpip_keepalive.html)                                               | status |
| [`VI_ATTR_TCPIP_NODELAY`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tcpip_nodelay.html)                                                   | status |
| [`VI_ATTR_TCPIP_PORT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tcpip_port.html)                                                         | status |
| [`VI_ATTR_TERMCHAR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_termchar.html)                                                             | status |
| [`VI_ATTR_TERMCHAR_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_termchar_en.html)                                                       | status |
| [`VI_ATTR_TMO_VALUE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_tmo_value.html)                                                           | status |
| [`VI_ATTR_TRIG_ID`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_trig_id.html)                                                               | status |
| [`VI_ATTR_USB_ALT_SETTING`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_alt_setting.html)                                               | status |
| [`VI_ATTR_USB_BULK_IN_PIPE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_bulk_in_pipe.html)                                             | status |
| [`VI_ATTR_USB_BULK_IN_STATUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_bulk_in_status.html)                                         | status |
| [`VI_ATTR_USB_BULK_OUT_PIPE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_bulk_out_pipe.html)                                           | status |
| [`VI_ATTR_USB_BULK_OUT_STATUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_bulk_out_status.html)                                       | status |
| [`VI_ATTR_USB_CLASS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_class.html)                                                           | status |
| [`VI_ATTR_USB_CTRL_PIPE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_ctrl_pipe.html)                                                   | status |
| [`VI_ATTR_USB_END_IN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_end_in.html)                                                         | status |
| [`VI_ATTR_USB_INTFC_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_intfc_num.html)                                                   | status |
| [`VI_ATTR_USB_INTR_IN_PIPE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_intr_in_pipe.html)                                             | status |
| [`VI_ATTR_USB_INTR_IN_STATUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_intr_in_status.html)                                         | status |
| [`VI_ATTR_USB_MAX_INTR_SIZE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_max_intr_size.html)                                           | status |
| [`VI_ATTR_USB_NUM_INTFCS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_num_intfcs.html)                                                 | status |
| [`VI_ATTR_USB_NUM_PIPES`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_num_pipes.html)                                                   | status |
| [`VI_ATTR_USB_PROTOCOL`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_protocol.html)                                                     | status |
| [`VI_ATTR_USB_RECV_INTR_DATA`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_recv_intr_data.html)                                         | status |
| [`VI_ATTR_USB_RECV_INTR_SIZE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_recv_intr_size.html)                                         | status |
| [`VI_ATTR_USB_SERIAL_NUM`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_serial_num.html)                                                 | status |
| [`VI_ATTR_USB_SUBCLASS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_usb_subclass.html)                                                     | status |
| [`VI_ATTR_USER_DATA/VI_ATTR_USER_DATA_32/VI_ATTR_USER_DATA_64`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_user_data.html)                 | status |
| [`VI_ATTR_VXI_DEV_CLASS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_dev_class.html)                                                   | status |
| [`VI_ATTR_VXI_LA`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_la.html)                                                                 | status |
| [`VI_ATTR_VXI_TRIG_DIR`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_trig_dir.html)                                                     | status |
| [`VI_ATTR_VXI_TRIG_LINES_EN`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_trig_lines_en.html)                                           | status |
| [`VI_ATTR_VXI_TRIG_STATUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_trig_status.html)                                               | status |
| [`VI_ATTR_VXI_TRIG_SUPPORT`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_trig_support.html)                                             | status |
| [`VI_ATTR_VXI_VME_INTR_STATUS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_vme_intr_status.html)                                       | status |
| [`VI_ATTR_VXI_VME_SYSFAIL_STATE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_vxi_vme_sysfail_state.html)                                   | status |
| [`VI_ATTR_WIN_ACCESS`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_win_access.html)                                                         | status |
| [`VI_ATTR_WIN_ACCESS_PRIV`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_win_access_priv.html)                                               | status |
| [`VI_ATTR_WIN_BASE_ADDR/VI_ATTR_WIN_BASE_ADDR_32/VI_ATTR_WIN_BASE_ADDR_64`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_win_base_addr.html) | status |
| [`VI_ATTR_WIN_BYTE_ORDER`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_win_byte_order.html)                                                 | status |
| [`VI_ATTR_WIN_SIZE/VI_ATTR_WIN_SIZE_32/VI_ATTR_WIN_SIZE_64`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_win_size.html)                     | status |
| [`VI_ATTR_WR_BUF_OPER_MODE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_wr_buf_oper_mode.html)                                             | status |
| [`VI_ATTR_WR_BUF_SIZE`](https://www.ni.com/docs/en-US/bundle/ni-visa-api-ref/page/ni-visa-api-ref/vi_attr_wr_buf_size.html)                                                       | status |

