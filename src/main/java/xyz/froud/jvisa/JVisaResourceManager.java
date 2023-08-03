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
import com.sun.jna.Platform;
import com.sun.jna.ptr.NativeLongByReference;

import java.nio.ByteBuffer;
import java.util.Set;

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
    public JVisaResourceManager() throws JVisaException, UnsatisfiedLinkError {
        this(null);
    }

    /**
     * Creates a session for a default resource manager.
     *
     * @param nativeLibraryName VISA native library name
     * @throws JVisaException if the resource manager couldn't be opened
     * @throws UnsatisfiedLinkError if the native shared library (.dll or .so or .dylib file) couldn't be loaded
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viopendefaultrm.html">viOpenDefaultRM</a>
     */
    public JVisaResourceManager(String nativeLibraryName) throws JVisaException, UnsatisfiedLinkError {

        /*
        You do NOT need to include the file extension when passing the library name to JNA.

        If the library name is "asdf", the actual file loaded depends on the OS:
            Windows: "asdf.dll"
            macOS:   "libasdf.dylib"
            Linus:   "libasdf.so"

        Here's a helpful StackOverflow answer: https://stackoverflow.com/a/37329511/7376577

        The interesting calls as of JNA version 5.13.0 are:
            1. com.sun.jna.Native.load(String, Class, Map) https://github.com/java-native-access/jna/blob/4962fd7758493b7395e86578705d8a32f6238872/src/com/sun/jna/Native.java#L615
            2. com.sun.jna.Library.Handler(String, Class, Map) https://github.com/java-native-access/jna/blob/e96f30192e9455e7cc4117cce06fc3fa80bead55/src/com/sun/jna/Library.java#L176
            3. com.sun.jna.NativeLibrary.getInstance(String, Map) https://github.com/java-native-access/jna/blob/e96f30192e9455e7cc4117cce06fc3fa80bead55/src/com/sun/jna/NativeLibrary.java#L462
            4. com.sun.jna.NativeLibrary.loadLibrary(String, Map) https://github.com/java-native-access/jna/blob/e96f30192e9455e7cc4117cce06fc3fa80bead55/src/com/sun/jna/NativeLibrary.java#L173
            5. com.sun.jna.NativeLibrary.findLibraryPath(String, Collection) https://github.com/java-native-access/jna/blob/e96f30192e9455e7cc4117cce06fc3fa80bead55/src/com/sun/jna/NativeLibrary.java#L734
            6. com.sun.jna.NativeLibrary.mapSharedLibraryName(String) https://github.com/java-native-access/jna/blob/e96f30192e9455e7cc4117cce06fc3fa80bead55/src/com/sun/jna/NativeLibrary.java#L777
            7. java.lang.System.mapLibraryName(String) https://download.java.net/java/GA/jdk14/docs/api/java.base/java/lang/System.html#mapLibraryName(java.lang.String)

        To enable logging, call this Java code:
            System.setProperty("jna.debug_load", "true");
        or add this argument to java.exe:
            -Djna.debug_load=true

         */
        if (nativeLibraryName == null) {
            // If the native library name is not specified, Inferring from the environment.
            if (Platform.isWindows()) {
                nativeLibraryName = Platform.is64Bit() ? "visa64" : "visa32";
            } else {
                nativeLibraryName = "visa";
            }
        }

        VISA_LIBRARY = Native.load(nativeLibraryName, JVisaLibrary.class);

        final NativeLongByReference pointerToResourceManagerHandle = new NativeLongByReference();
        final NativeLong errorCode = VISA_LIBRARY.viOpenDefaultRM(pointerToResourceManagerHandle);
        checkError(errorCode, "viOpenDefaultRM");

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
        checkError(errorCode, "viClose");
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
        checkError(errorCode, "viParseRsrcEx");
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
        checkError(errorCode, "viOpen");
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

        // The resource name gets repeatledly populated in this buffer.
        ByteBuffer resourceNameBuf = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);

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
        checkError(errorCodeFindRsrc, "viFindRsrc");

        final int resourcesFoundCount = (int) countPtr.getValue().longValue();
        final String[] rv = new String[resourcesFoundCount];

        if (resourcesFoundCount > 0) {
            // The first resource name is returned from viFindRsrc().
            rv[0] = JVisaUtils.byteBufferToString(resourceNameBuf);
        }

        for (int i = 1; i < resourcesFoundCount; i++) {
            // Now we need to call viFindNext() for all remaining resources.
            resourceNameBuf = ByteBuffer.allocate(JVisaLibrary.VI_FIND_BUFLEN);
            final NativeLong errorCodeFindNext = VISA_LIBRARY.viFindNext(
                    findListPtr.getValue(), //ViFindList findList
                    resourceNameBuf //ViChar instrDesc[]
            );
            checkError(errorCodeFindNext, "viFindNext");
            rv[i] = JVisaUtils.byteBufferToString(resourceNameBuf);
        }

        // Close the findList after use.
        final NativeLong errorCodeClose = VISA_LIBRARY.viClose(findListPtr.getValue());
        checkError(errorCodeClose, "viClose");

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
    private String getMessageForErrorCode(NativeLong errorCodeToGetDescriptionFor) {

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

      /**
     * @see
     * <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/completion_codes.html">Completion
     * Codes</a>
     */
    private final Set<Integer> SUCCESS_CODES = Set.of(
            // VI_SUCCESS is defined in visatype.h which is not part of JVisaLibrary.java
            //
            // Specified event is already enabled for at least one of the specified mechanisms.
            JVisaLibrary.VI_SUCCESS_EVENT_EN,
            //
            // Specified event is already disabled for at least one of the specified mechanisms.
            JVisaLibrary.VI_SUCCESS_EVENT_DIS,
            //
            // Operation completed successfully, but queue was already empty.
            JVisaLibrary.VI_SUCCESS_QUEUE_EMPTY,
            //
            // The specified termination character was read.
            JVisaLibrary.VI_SUCCESS_TERM_CHAR,
            //
            // The number of bytes read is equal to the input count.
            JVisaLibrary.VI_SUCCESS_MAX_CNT,
            //
            // Session opened successfully, but the device at the specified address is not responding.
            JVisaLibrary.VI_SUCCESS_DEV_NPRESENT,
            //
            // The path from trigSrc to trigDest is already mapped.
            JVisaLibrary.VI_SUCCESS_TRIG_MAPPED,
            //
            // Wait terminated successfully on receipt of an event notification. There is still at least one more event occurrence of the requested type(s) available for this session.
            JVisaLibrary.VI_SUCCESS_QUEUE_NEMPTY,
            //
            // Event handled successfully. Do not invoke any other handlers on this session for this event.
            JVisaLibrary.VI_SUCCESS_NCHAIN,
            //
            // Operation completed successfully, and this session has nested shared locks.
            JVisaLibrary.VI_SUCCESS_NESTED_SHARED,
            //
            // Operation completed successfully, and this session has nested exclusive locks.
            JVisaLibrary.VI_SUCCESS_NESTED_EXCLUSIVE,
            //
            // Asynchronous operation request was actually performed synchronously.
            JVisaLibrary.VI_SUCCESS_SYNC
    );

    /**
     * If the status code indicates an error, this method will get a human-readable message for the
     * error code and throw a JVisaException.
     *
     * @param rm the resource manager used for this VISA session
     * @param errorCode the value returned by a JVisaLibrary call
     * @param cFunctionName name of the C function corresponding to the call to the native shared library (.dll or .so or .dylib file)
     * @throws JVisaException if the status code means the call failed
     */
    protected final void checkError(NativeLong errorCode, String cFunctionName) throws JVisaException {
        final long statusCode = errorCode.longValue();
        if (statusCode != 0 && !SUCCESS_CODES.contains((int)statusCode)) {
            final String messageForErrorCode = getMessageForErrorCode(errorCode);
            throw new JVisaException(statusCode, cFunctionName, messageForErrorCode);
        }
    }

}
