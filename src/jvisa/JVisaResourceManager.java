/**
 * @license
 *
 * Copyright 2014-2018 Günter Fuchs (gfuchs@acousticmicroscopy.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Modifications by Peter Froud, June 2018
 */
package jvisa;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;
import static jvisa.JVisaUtils.stringToByteBuffer;

/**
 * The Visa resource manager manages and communicates with resources, attributes, events, etc.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 *
 */
public class JVisaResourceManager {

    // A unique logical identifier to the Visa session. In the C API, this is called ViSession.
    private final NativeLong RESOURCE_MANAGER_HANDLE;

    public final JVisaLibrary VISA_LIBRARY;

    /**
     * Creates a session for a default resource manager.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viopendefaultrm/
     *
     * 32-bit Windows is not supported.<br>
     * See http://stackoverflow.com/questions/21486086/cant-load-personal-dll-with-jna-from-netbeans
     *
     * @throws JVisaException if the resource manager couldn't be opened
     * @throws UnsatisfiedLinkError if the Visa DLL couldn't be loaded
     */
    public JVisaResourceManager() throws JVisaException, UnsatisfiedLinkError {
        VISA_LIBRARY = (JVisaLibrary) Native.loadLibrary("nivisa64.dll", JVisaLibrary.class);

        final NativeLongByReference pointerToResourceManagerHandle = new NativeLongByReference();
        final NativeLong nativeStatus = VISA_LIBRARY.viOpenDefaultRM(pointerToResourceManagerHandle);

        JVisaUtils.throwForStatus(this, nativeStatus, "viOpenDefaultRM");
        RESOURCE_MANAGER_HANDLE = pointerToResourceManagerHandle.getValue();
    }

    /**
     * Closes the resource manager.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viclose/
     *
     * @throws jvisa.JVisaException if the resource manager couldn't be closed
     */
    public void close() throws JVisaException {
        final NativeLong nativeStatus = VISA_LIBRARY.viClose(RESOURCE_MANAGER_HANDLE);
        JVisaUtils.throwForStatus(this, nativeStatus, "viClose");
    }

    /**
     * Returns the alias for an instrument. The instrument does not need to be opened.
     *
     * If you're using NI-VISA, aliases are stored in "C:\ProgramData\National Instruments\NIvisa\visaconf.ini".<br>
     * You can also use NI MAX (National Instruments Measurement &amp; Automation Explorer) to read and change aliases.<br>
     * I think the DLL only lets you read aliases, not change them.
     *
     * @param resourceName name of the resource to get the alias for
     * @return the alias, or empty string(?) if the specified resource doesn't have an alias
     * @throws jvisa.JVisaException if the API call to get the alias failed
     */
    public String getInstrumentAlias(String resourceName) throws JVisaException {
        final ByteBuffer resourceNameBuf = JVisaUtils.stringToByteBuffer(resourceName);
        final ByteBuffer aliasBuf = ByteBuffer.allocate(128);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viparsersrcex/
        final NativeLong visaStatus = VISA_LIBRARY.viParseRsrcEx(
                RESOURCE_MANAGER_HANDLE,
                resourceNameBuf,
                new NativeLongByReference(), //ViPUInt16 intfType
                new NativeLongByReference(), //ViPUInt16 intfNum
                new NativeLongByReference(), //ViChar rsrcClass[]
                ByteBuffer.allocate(128), //ViChar expandedUnaliasedName[]
                aliasBuf
        );
        JVisaUtils.throwForStatus(this, visaStatus, "viParseRsrcEx");
        return new String(aliasBuf.array()).trim();
    }

    /**
     * Opens an instrument session.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viopen/
     *
     * @param resourceName name of the resource to open, for example TCPIP::192.168.1.106::INSTR
     * @return a JVisaInstrument instance for the instrument
     * @throws jvisa.JVisaException if the instrument couldn't be opened
     */
    public JVisaInstrument openInstrument(String resourceName) throws JVisaException {
        final NativeLongByReference instrumentHandle = new NativeLongByReference();
        final ByteBuffer resourceNameBuf = JVisaUtils.stringToByteBuffer(resourceName);

        final NativeLong visaStatus = VISA_LIBRARY.viOpen(RESOURCE_MANAGER_HANDLE,
                resourceNameBuf,
                new NativeLong(0), // ViAccessMode accessMode - 0 for default access mode
                new NativeLong(0), // ViUInt32 openTimeout - how long to wait before returning error. Only when the access mode equals locking?
                instrumentHandle
        );
        JVisaUtils.throwForStatus(this, visaStatus, "viOpen");
        return new JVisaInstrument(this, instrumentHandle, resourceName);
    }

    /**
     * Search for connected VISA resources.
     *
     * Currently it is set to only search for USB instruments. You can change the filterExpression.
     *
     * @return array of VISA resource names found.
     * @throws JVisaException if the process for finding instruments failed, or if no instruments were found.
     */
    public String[] findResources() throws JVisaException {

        /*
         National Instruments says the filter is a regular expression but they're liars.
         Here, the question mark "matches any one character" which is not what it does in a regex.
         The star does the same thing as in a real regular expression.
         */
        final ByteBuffer filterExpression = stringToByteBuffer("USB?*");

        final NativeLongByReference countPtr = new NativeLongByReference();
        final NativeLongByReference findListPtr = new NativeLongByReference();

        final ByteBuffer descrBuf = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindrsrc/
        final NativeLong visaStatus = VISA_LIBRARY.viFindRsrc(RESOURCE_MANAGER_HANDLE,
                filterExpression, findListPtr, countPtr, descrBuf);
        JVisaUtils.throwForStatus(this, visaStatus, "viFindRsrc");

        final int foundCount = (int) countPtr.getValue().longValue();
        final String[] foundResources = new String[foundCount];

        if (foundCount > 0) {
            // The buffer gets populated with the first result
            foundResources[0] = new String(descrBuf.array()).trim();
        }

        for (int i = 1; i < foundCount; i++) {
            // If more than one resources were found, we need to allocate a new buffer and call another function
            final ByteBuffer descrBufNext = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);

            // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindnext/
            final NativeLong visaStatus2 = VISA_LIBRARY.viFindNext(findListPtr.getValue(), descrBufNext);
            JVisaUtils.throwForStatus(this, visaStatus2, "viFindNext");

            foundResources[i] = new String(descrBuf.array()).trim();
        }
        return foundResources;
    }

    /**
     * Converts a VISA status code to a human-readable description.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vistatusdesc/
     *
     * @param statusCode return value from a DLL call
     * @return human-readable description about the error code
     */
    public String getStatusDescription(NativeLong statusCode) {

        // "Note  The size of the desc parameter should be at least 256 bytes."
        final ByteBuffer errDescBuf = ByteBuffer.allocate(256);

        final NativeLong errorCode2 = VISA_LIBRARY.viStatusDesc(RESOURCE_MANAGER_HANDLE, statusCode, errDescBuf);

        long errorCode2Long = errorCode2.longValue();
        if (errorCode2Long != 0) {
            System.err.printf("viStatusDesc() returned 0x%H while trying to get description for code 0x%H\n",
                    errorCode2Long, statusCode.longValue());
            return String.format("<couldn't get description for the status code %d>", statusCode.longValue());
        }
        return new String(errDescBuf.array()).trim();
    }

}
