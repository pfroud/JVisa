/**
 * @license Copyright 2014-2018 Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Modifications by Peter Froud, June 2018
 */
package xyz.froud.jvisa;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.nio.ByteBuffer;


/**
 * The Visa resource manager "scans the system to find all the devices connected to it through the various interface buses and then controls the access to them."
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/resourcemanager.html">The Resource Manager</a>
 */
public class JVisaResourceManager implements  AutoCloseable {

    /**
     * A unique logical identifier to the Visa session. In the C API, this is called ViSession.
     */
    private final NativeLong RESOURCE_MANAGER_HANDLE;

    public final JVisaLibrary VISA_LIBRARY;

    /**
     * Creates a session for a default resource manager.
     *
     * @throws JVisaException if the resource manager couldn't be opened
     * @throws UnsatisfiedLinkError if the native shared library (.dll or .so or .dylib file) couldn't be loaded
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viopendefaultrm.html">viOpenDefaultRM</a>
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

        VISA_LIBRARY = Native.load(nativeLibraryName, JVisaLibrary.class);

        final NativeLongByReference pointerToResourceManagerHandle = new NativeLongByReference();
        final NativeLong errorCode = VISA_LIBRARY.viOpenDefaultRM(pointerToResourceManagerHandle);
        JVisaUtils.checkError(this, errorCode, "viOpenDefaultRM");
        RESOURCE_MANAGER_HANDLE = pointerToResourceManagerHandle.getValue();
    }

    /**
     * Closes the resource manager.
     *
     * @throws JVisaException if the resource manager couldn't be closed
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viclose.html">viClose</a>
     */
    @Override
    public void close() throws JVisaException {
        final NativeLong errorCode = VISA_LIBRARY.viClose(RESOURCE_MANAGER_HANDLE);
        JVisaUtils.checkError(this, errorCode, "viClose");
    }

    /**
     * Returns the alias for a resource. The resource does not need to be opened.
     * <p>
     * If you're using NI-VISA, aliases are stored in "C:\ProgramData\National Instruments\NIvisa\visaconf.ini".<br>
     * You can also use NI MAX (National Instruments Measurement &amp; Automation Explorer) to read and change aliases.<br>
     * I think the native shared library (.dll or .so or .dylib file) only lets you read aliases, not change them.
     *
     * @param resourceName resource name to get the alias for
     *
     * @return the alias, or empty string(?) if the specified resource doesn't have an alias
     * @throws JVisaException if the API call to get the alias failed
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viparsersrcex.html">viParseRsrcEx</a>
     */
    public String getInstrumentAlias(String resourceName) throws JVisaException {
        final ByteBuffer aliasBuf = ByteBuffer.allocate(128);

        final NativeLong errorCode = VISA_LIBRARY.viParseRsrcEx(RESOURCE_MANAGER_HANDLE,
                JVisaUtils.stringToByteBuffer(resourceName), // ViRsrc rsrcName
                new NativeLongByReference(), //ViPUInt16 intfType
                new NativeLongByReference(), //ViPUInt16 intfNum
                new NativeLongByReference(), //ViChar rsrcClass[]
                ByteBuffer.allocate(128), //ViChar expandedUnaliasedName[]
                aliasBuf //ViChar aliasIfExists[]
        );
        JVisaUtils.checkError(this, errorCode, "viParseRsrcEx");
        return JVisaUtils.byteBufferToString(aliasBuf);
    }

    /**
     * Opens an instrument session.
     *
     * @param resourceName resource name to open
     *
     * @return a JVisaInstrument instance for the instrument
     * @throws JVisaException if the resource couldn't be opened
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viopen.html">viOpen</a>
     */
    public JVisaInstrument openInstrument(String resourceName) throws JVisaException {

        final NativeLongByReference instrumentHandle = new NativeLongByReference();

        final NativeLong errorCode = VISA_LIBRARY.viOpen(RESOURCE_MANAGER_HANDLE,
                JVisaUtils.stringToByteBuffer(resourceName),
                new NativeLong(0), // ViAccessMode accessMode - 0 (VI_NULL) for default access mode
                new NativeLong(0), // ViUInt32 openTimeout - how long to wait before returning error. Only when the access mode equals locking?
                instrumentHandle
        );
        JVisaUtils.checkError(this, errorCode, "viOpen");
        return new JVisaInstrument(this, instrumentHandle, resourceName);
    }

    /**
     * Search for connected VISA resources (without filtering).
     *
     * @return array of VISA resource names found.
     * @throws JVisaException if the process of finding resources failed, or if no resources were found.
     * @see #findResources(String)
     */
    public String[] findResources() throws JVisaException {
        // question mark matches any one character
        return findResources("?*");
    }

    /**
     * Search for connected VISA resources with a filter on the resource name.
     *
     * @param filterExpression it says it's a regular expression but it's not. Use {@code "?*"} to match all resources.
     * <table>
     *     <tr>
     *         <th>Special Characters and Operators</th>
     *         <th>Meaning</th>
     *     </tr>
     *     <tr>
     *         <td><code>?</code></td>
     *         <td>Matches any one character.</td>
     *     </tr>
     *     <tr>
     *         <td><code>\</code></td>
     *         <td>Escape the next character.</td>
     *     </tr>
     *     <tr>
     *         <td><code>[list]</code></td>
     *         <td>Matches any one character from the enclosed list. You can use a hyphen to match a range of characters.</td> </tr>
     *     <tr>
     *         <td><code>[^list]</code></td>
     *         <td>Matches any character NOT in the enclosed list. You can use a hyphen to match a range of characters.</td> </tr>
     *     <tr>
     *         <td><code>*</code></td>
     *         <td>Matches zero or more occurrences of the preceding character or expression.</td>
     *     </tr>
     *     <tr>
     *         <td><code>+</code></td>
     *         <td>Matches one or more occurrences of the preceding character or expression.</td>
     *     </tr>
     *     <tr>
     *         <td><code>Exp|exp</code></td>
     *         <td>Matches either the preceding or following expression. The or operator <code>|</code> matches the
     *         entire expression that precedes or follows it and not just the character that precedes or follows it.
     *         For example, <code>VXI|GPIB</code> means <code>(VXI)|(GPIB)</code>, not <code>VX(I|G)PIB</code>.</td> </tr>
     *     <tr>
     *         <td><code>(exp)</code></td>
     *         <td>Grouping characters or expressions.</td>
     *     </tr>
     * </table>
     *
     * @return array of VISA resource names found.
     * @throws JVisaException if the process for finding resources failed, or if no resources were found.
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vifindrsrc.html">viFindRsrc</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vifindnext.html">viFindNext</a>
         */
    public String[] findResources(String filterExpression) throws JVisaException {

        // Will be set to the number of resources found.
        final NativeLongByReference countPtr = new NativeLongByReference();

        // Will be set to "a handle identifying this search session".
        final NativeLongByReference findListPtr = new NativeLongByReference();

        // The resource name gets populated in this buffer.
        final ByteBuffer resourceNameBuf = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);

            /*
        The viFindRsrc() function only populates the buffer with the first resource name found.
        If more than one resource is found, you have to repeatedly call viFindNext().
             */
        final NativeLong errorCodeFindRsrc = VISA_LIBRARY.viFindRsrc(RESOURCE_MANAGER_HANDLE,
                JVisaUtils.stringToByteBuffer(filterExpression), //ViString expr
                findListPtr, // ViPFindList findList
                countPtr, //ViPUInt32 retcnt
                resourceNameBuf //ViChar instrDesc[]
        );
        JVisaUtils.checkError(this, errorCodeFindRsrc, "viFindRsrc");

        final int resourcesFoundCount = (int) countPtr.getValue().longValue();
        final String[] rv = new String[resourcesFoundCount];

        if (resourcesFoundCount > 0) {
            // The first resource name is returned from viFindRsrc().
            rv[0] = JVisaUtils.byteBufferToString(resourceNameBuf);
        }

        for (int i = 1; i < resourcesFoundCount; i++) {
            // Now we need to call viFindNext() for all remaining resources.
            resourceNameBuf.clear();
            final NativeLong errorCodeFindNext = VISA_LIBRARY.viFindNext(
                    findListPtr.getValue(), //ViFindList findList
                    resourceNameBuf //ViChar instrDesc[]
            );
            JVisaUtils.checkError(this, errorCodeFindNext, "viFindNext");
            rv[i] = JVisaUtils.byteBufferToString(resourceNameBuf);
        }

        // Close the findList after use.
        final NativeLong errorCodeClose = VISA_LIBRARY.viClose(findListPtr.getValue());
        JVisaUtils.checkError(this, errorCodeClose, "viClose");

        return rv;
    }

    /**
     * Converts a VISA error code to a human-readable description.
     *
     * @param errorCodeToGetDescriptionFor return value from a call to the native shared library (.dll or .so or .dylib file)
     *
     * @return human-readable description about the error code
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vistatusdesc.html">viStatusDesc</a>
     */
    public String getMessageForErrorCode(NativeLong errorCodeToGetDescriptionFor) {

        // "Note  The size of the desc parameter should be at least 256 bytes."
        final ByteBuffer messageBuf = ByteBuffer.allocate(256);

        final NativeLong errorCodeFromViStatusDesc = VISA_LIBRARY.viStatusDesc(RESOURCE_MANAGER_HANDLE, errorCodeToGetDescriptionFor, messageBuf);

        long errorCode2Long = errorCodeFromViStatusDesc.longValue();
        if (errorCode2Long != 0) {
            System.err.printf("viStatusDesc() returned 0x%H while trying to get description for code 0x%H\n",
                    errorCode2Long, errorCodeToGetDescriptionFor.longValue());
            return String.format("<couldn't get description for the status code %d>", errorCodeToGetDescriptionFor.longValue());
        }
        return JVisaUtils.byteBufferToString(messageBuf);
    }

}
