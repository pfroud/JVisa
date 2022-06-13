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
 *
 * Modifications by Peter Froud, June 2018
 */
package xyz.froud.jvisa;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;
import static xyz.froud.jvisa.JVisaUtils.stringToByteBuffer;

/**
 * The Visa resource manager "scans the system to find all the devices connected to it through the various interface buses and then controls the access to them."
 *
 * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/resourcemanager.html">The Resource Manager</a>
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
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viopendefaultrm.html">viOpenDefaultRM</a>
     *
     *
     * @throws JVisaException if the resource manager couldn't be opened
     * @throws UnsatisfiedLinkError if the native shared library (.dll or .so or .dylib file) couldn't be loaded
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public JVisaResourceManager() throws JVisaException, UnsatisfiedLinkError {

        final String nativeLibraryName;

        /*
        You do NOT need to include the file extension when passing the library name to JNA.

        If the library name is "asdf", the actual file loaded depends on the OS:
            Windows: "asdf.dll"
            macOS:   "libasdf.dylib"
            Linus:   "libasdf.so"

        Here's a helpful StackOverflow answer:
        https://stackoverflow.com/a/37329511/7376577

        You can see what JNA is doing here:
        https://github.com/java-native-access/jna/blob/69bf22f5051853e95a3b9725ca19b92cdcfd793f/src/com/sun/jna/NativeLibrary.java#L757
         */
        if (System.getProperty("os.name").startsWith("Windows")) {
            /*
            I'm pretty sure that the bitness of the OS, JVM, and Visa DLL must match.

            When you run a 32-bit program on 64-bit Windows, it runs in a
            compatibility layer called WOW64 (Windows-32-bit on Windows-64-bit).

            The WOW64 system lies to 32-bit programs and tells them that the OS bitness
            is 32-bit. So, a 32-bit JVM running on 64-bit Windows will have the "os.arch"
            system property set to "x86".

            To find the actual OS bitness, we need to check some environment variables.

            These environment variables tell us the bitness of the operating system,
            NOT the architecture of the physical hardware CPU.

            On 64-bit Windows:
                To get 64-bit Command Prompt, run C:\Windows\System32\cmd.exe.
                    PROCESSOR_ARCHITECTURE is "AMD64"
                    PROCESSOR_ARCHITEW6432 is not defined.
                To get 32-bit Command Prompt, run C:\Windows\SysWOW64\cmd.exe.
                    PROCESSOR_ARCHITECTURE is "x86".
                    PROCESSOR_ARCHITEW6432 is "AMD64".
            On 32-bit Windows:
                There is only C:\Windows\System32\cmd.exe.
                    PROCESSOR_ARCHITECTURE is "x86".
                    PROCESSOR_ARCHITEW6432 is not defined.

            See https://stackoverflow.com/a/5940770/7376577
             */
            final String PROCESSOR_ARCHITECTURE = System.getenv("PROCESSOR_ARCHITECTURE");
            final String PROCESSOR_ARCHITEW6432 = System.getenv("PROCESSOR_ARCHITEW6432");
            final boolean isWindows64bit
                    = (PROCESSOR_ARCHITECTURE != null && PROCESSOR_ARCHITECTURE.endsWith("64"))
                    || (PROCESSOR_ARCHITEW6432 != null && PROCESSOR_ARCHITEW6432.endsWith("64"));

            if (isWindows64bit) {
                // 64-bit Windows. Verify the JVM is also 64-bit.
                final String architectureOfJvmNotWindows = System.getProperty("os.arch");
                if (architectureOfJvmNotWindows.endsWith("64")) {
                    nativeLibraryName = "visa64";
                } else {
                    throw new RuntimeException("it appears this is a 32-bit JVM running on 64-bit Windows - JNA will not work!");
                }
            } else {
                // 32-bit Windows
                nativeLibraryName = "visa32";
            }

        } else {
            // not Windows, probably macOS or Linux
            nativeLibraryName = "visa";
        }

        VISA_LIBRARY = (JVisaLibrary) Native.load(nativeLibraryName, JVisaLibrary.class);

        final NativeLongByReference pointerToResourceManagerHandle = new NativeLongByReference();
        final NativeLong nativeStatus = VISA_LIBRARY.viOpenDefaultRM(pointerToResourceManagerHandle);
        JVisaUtils.throwForStatus(this, nativeStatus, "viOpenDefaultRM");
        RESOURCE_MANAGER_HANDLE = pointerToResourceManagerHandle.getValue();
    }

    /**
     * Closes the resource manager.
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viclose.html">viClose</a>
     *
     * @throws JVisaException if the resource manager couldn't be closed
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
     * I think the native shared library (.dll or .so or .dylib file) only lets you read aliases, not change them.
     *
     * @param resourceName name of the resource to get the alias for
     * @return the alias, or empty string(?) if the specified resource doesn't have an alias
     * @throws JVisaException if the API call to get the alias failed
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
                aliasBuf //ViChar aliasIfExists[]
        );
        JVisaUtils.throwForStatus(this, visaStatus, "viParseRsrcEx");
        return new String(aliasBuf.array()).trim();
    }

    /**
     * Opens an instrument session.
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viopen.html">viOpen</a>
     *
     * @param resourceName name of the resource to open, for example TCPIP::192.168.1.106::INSTR
     * @return a JVisaInstrument instance for the instrument
     * @throws JVisaException if the instrument couldn't be opened
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
     * @return array of VISA resource names found.
     * @throws JVisaException if the process for finding instruments failed, or if no instruments were found.
     */
    public String[] findResources() throws JVisaException {

        /*
         National Instruments says the filter is a regular expression but they're liars.
         Here, the question mark "matches any one character" which is not what it does in a regex.
         The star does the same thing as in a real regular expression.

         The filter expression "?*" matches all resources.
         */
        final ByteBuffer filterExpression = stringToByteBuffer("?*");

        final NativeLongByReference countPtr = new NativeLongByReference();
        final NativeLongByReference findListPtr = new NativeLongByReference();

        final ByteBuffer descrBufFirst = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindrsrc/
        final NativeLong visaStatus = VISA_LIBRARY.viFindRsrc(RESOURCE_MANAGER_HANDLE,
                filterExpression, findListPtr, countPtr, descrBufFirst);
        JVisaUtils.throwForStatus(this, visaStatus, "viFindRsrc");

        final int foundCount = (int) countPtr.getValue().longValue();
        final String[] foundResources = new String[foundCount];

        if (foundCount > 0) {
            /*
            The viFindRsrc() function populates the buffer with the first result.
            If more than one instrument was found, you have to use viFindNext().
             */
            foundResources[0] = new String(descrBufFirst.array()).trim();
        }

        for (int i = 1; i < foundCount; i++) {
            /*
            If more than one resource was found, we need to allocate another
            buffer and call another Visa function for each one.
             */
            final ByteBuffer descrBufNext = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);

            // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindnext/
            final NativeLong visaStatus2 = VISA_LIBRARY.viFindNext(findListPtr.getValue(), descrBufNext);
            JVisaUtils.throwForStatus(this, visaStatus2, "viFindNext");

            foundResources[i] = new String(descrBufNext.array()).trim();
        }
        return foundResources;
    }

    /**
     * Converts a VISA status code to a human-readable description.
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vistatusdesc.html">viStatusDesc</a>
     *
     * @param statusCode return value from a call to the native shared library (.dll or .so or .dylib file)
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
