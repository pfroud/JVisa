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
 * Modifications by Peter Froud, Lumenetix Inc
 * June 2018
 *
 * Very useful tool: NI IO Trace
 * "C:\Program Files (x86)\National Instruments\NI IO Trace\NI IO Trace.exe"
 * Shows calls to nivisa.dll!
 */
package jvisa;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;
import static jvisa.JVisaUtils.stringToByteBuffer;
import visatype.VisatypeLibrary;

/**
 * The Visa resource manager manages and communicates with resources, attributes, events, etc.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud (pfroud@lumenetix.com)
 *
 */
public class JVisaResourceManager {

    /* resourceManagerHandle is a unique logical identifier to the Visa session.
    The C API calls this ViSession.*/
    private final NativeLong resourceManagerHandle;

    public final JVisaLibrary library;

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
        library = (JVisaLibrary) Native.loadLibrary("nivisa64.dll", JVisaLibrary.class);

        NativeLongByReference pointerToResourceManagerHandle = new NativeLongByReference();
        NativeLong nativeStatus = library.viOpenDefaultRM(pointerToResourceManagerHandle);

        JVisaUtils.throwForStatus(this, nativeStatus, "viOpenDefaultRM");
        resourceManagerHandle = pointerToResourceManagerHandle.getValue();
    }

    /**
     * Closes the resource manager.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viclose/
     *
     * @throws jvisa.JVisaException if the resource manager couldn't be closed
     */
    public void close() throws JVisaException {
        NativeLong nativeStatus = library.viClose(resourceManagerHandle);
        JVisaUtils.throwForStatus(this, nativeStatus, "viClose");
    }

    /**
     * Returns the alias for an instrument. The instrument does not need to be opened.
     *
     * Aliases are stored in "C:\ProgramData\National Instruments\NIvisa\visaconf.ini".<br>
     * You can also use NI MAX (National Instruments Measurement & Automation Explorer).<br>
     * I don't think the DLL lets you set aliases.
     *
     * @param resourceName name of the resource to get the alias for
     * @return the alias, or empty string(?) if the specified resource doesn't have an alias
     * @throws jvisa.JVisaException if the API call to get the alias failed
     */
    public String getInstrumentAlias(String resourceName) throws JVisaException {
        ByteBuffer resourceNameBuf = JVisaUtils.stringToByteBuffer(resourceName);
        ByteBuffer aliasBuf = ByteBuffer.allocate(128);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viparsersrcex/
        NativeLong visaStatus = library.viParseRsrcEx(
                resourceManagerHandle,
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
        NativeLongByReference instrumentHandle = new NativeLongByReference();
        ByteBuffer resourceNameBuf = JVisaUtils.stringToByteBuffer(resourceName);

        NativeLong visaStatus = library.viOpen(resourceManagerHandle,
                resourceNameBuf,
                new NativeLong(0), // ViAccessMode accessMode - 0 for default access mode
                new NativeLong(0), // ViUInt32 openTimeout - how long to wait before returning error. Only when the access mode equals locking?
                instrumentHandle
        );
        JVisaUtils.throwForStatus(this, visaStatus, "viOpen");
        return new JVisaInstrument(this, instrumentHandle, resourceName);
    }

    public String[] findResources() throws JVisaException {

        /*
         National Instruments says this is a regular expression but they're liars.
         Here, the question mark "matches any one character" which is not what it does in a regex.
         The star does the same thing as in a real regular expression.
         */
        ByteBuffer filterExpression = stringToByteBuffer("?*");

        NativeLongByReference countPtr = new NativeLongByReference();
        NativeLongByReference findList = new NativeLongByReference();

        final int descrLen = 256;
        ByteBuffer descr = ByteBuffer.allocate(descrLen);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindrsrc/
        NativeLong visaStatus = library.viFindRsrc(resourceManagerHandle,
                filterExpression, findList, countPtr, descr);
        JVisaUtils.throwForStatus(this, visaStatus, "viFindRsrc");

        long numFound = countPtr.getValue().longValue();
        String[] rv = new String[(int) numFound];
        if (numFound > 0) {
            rv[0] = new String(descr.array()).trim();
        }

        for (int i = 1; i < numFound; i++) {
            descr = ByteBuffer.allocate(descrLen);

            // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindnext/
            visaStatus = library.viFindNext(findList.getValue(), descr);

            JVisaUtils.throwForStatus(this, visaStatus, "viFindNext");
            rv[i] = new String(descr.array()).trim();
        }
        return rv;
    }

    /**
     * Converts a VISA status code to a human-readable description.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vistatusdesc/
     *
     * @param statusCode
     * @return status description
     */
    public String getStatusDescription(NativeLong statusCode) {

        ByteBuffer errDescBuf = ByteBuffer.allocate(256);

        NativeLong errorCode = library.viStatusDesc(resourceManagerHandle, statusCode, errDescBuf);

        long errorCodeLong = errorCode.longValue();
        if (errorCodeLong != VisatypeLibrary.VI_SUCCESS) {
            System.err.printf("viStatusDesc() returned 0x%H while trying to get description for code 0x%H\n",
                    errorCodeLong, statusCode.longValue());
            return "<couldn't get description for the status code>";
        }
        return new String(errDescBuf.array()).trim();
    }

}
