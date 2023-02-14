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

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import xyz.froud.jvisa.eventhandling.JVisaEventHandler;
import xyz.froud.jvisa.eventhandling.JVisaEventType;

import java.nio.ByteBuffer;

/**
 * Represents a Visa instrument. This is a wrapper around the native C instrument handle.
 * <p>
 * To use this class, call {@link JVisaResourceManager#openInstrument} from a JVisaResourceManager instance.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 */
public class JVisaInstrument implements AutoCloseable {

    private final static int DEFAULT_BUFFER_SIZE = 1024;

    private final NativeLong INSTRUMENT_HANDLE;
    private final JVisaResourceManager RESOURCE_MANAGER;
    private final JVisaLibrary VISA_LIBRARY;
    public final String RESOURCE_NAME;

    /**
     * A string appended to the end of every string sent to the instrument. If it null then nothing is appended.
     *
     * @see <a href="https://pyvisa.readthedocs.io/en/latest/introduction/communication.html">PyVISA docs page
     * "Communicating with your instrument"</a>
     */
    private String writeTerminator = null;

    public JVisaInstrument(JVisaResourceManager resourceManager, NativeLongByReference instrumentHandle, String resourceName) {
        RESOURCE_MANAGER = resourceManager;
        VISA_LIBRARY = resourceManager.VISA_LIBRARY;
        INSTRUMENT_HANDLE = instrumentHandle.getValue();
        RESOURCE_NAME = resourceName;
    }

    /**
     * Sends a command and receives its response. It insists in receiving at least a given number of bytes.
     *
     * @param command string to send
     * @param bufferSize size of buffer to allocate. The size can be set smaller since it gets allocated with readCount.
     * @return response from instrument as a String
     * @throws JVisaException if the write operation fails or the read operation fails
     */
    public String sendAndReceiveString(String command, int bufferSize) throws JVisaException {
        write(command);
        return readString(bufferSize);
    }

    /**
     * Sends a command and receives its response. It receives as many bytes as the instrument is sending. (That is probably wrong, it can receive maximum DEFAULT_BUFFER_SIZE bytes)
     *
     * @param command string to send
     * @return response from instrument as a String
     * @throws JVisaException if the write operation fails or the read operation fails
     */
    public String sendAndReceiveString(String command) throws JVisaException {
        write(command);
        return readString(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Sends a command and receives its response. It insists in receiving at least a given number of bytes.
     *
     * @param command string to send
     * @param bufferSize size of buffer to allocate. The size can be set smaller since it gets allocated with readCount.
     * @return response from instrument as a ByteBuffer
     * @throws JVisaException if the write operation fails or the read operation fails
     */
    public ByteBuffer sendAndReceiveBytes(String command, int bufferSize) throws JVisaException {
        write(command);
        return readBytes(bufferSize);
    }

    /**
     * Sends a command and receives its response. It receives as many bytes as the instrument is sending.
     *
     * @param command string to send
     * @return response from instrument as a ByteBuffer
     * @throws JVisaException if the write operation fails or the read operation fails
     */
    public ByteBuffer sendAndReceiveBytes(String command) throws JVisaException {
        write(command);
        return readBytes(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Sends a command to the instrument. If setWriteTerminator() was called with a non-null string, the terminator will
     * be appended to the string before sending it to the instrument.
     *
     * @param command the command to send
     *
     * @throws JVisaException if the write operation fails
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viwrite.html">viWrite</a>
     */
    public void write(String command) throws JVisaException {
        final String commandWithTerminator;
        if (writeTerminator != null) {
            commandWithTerminator = command + writeTerminator;
        } else {
            commandWithTerminator = command;
        }
        final ByteBuffer buffer = JVisaUtils.stringToByteBuffer(commandWithTerminator);
        final int commandLength = commandWithTerminator.length();

        final NativeLongByReference returnCount = new NativeLongByReference();
        final NativeLong errorCode = VISA_LIBRARY.viWrite(INSTRUMENT_HANDLE,
                buffer,
                new NativeLong(commandLength),
                returnCount
        );
        RESOURCE_MANAGER.checkError(errorCode, "viWrite");

        final long count = returnCount.getValue().longValue();
        if (count != commandLength) {
            throw new JVisaException(String.format("Could only write %d instead of %d bytes.",
                    count, commandLength));
        }
    }

    /**
     * Reads data from the instrument, e.g. a command response or data.
     *
     * @param bufferSize size of response buffer in bytes
     * @return response from instrument as bytes
     * @throws JVisaException if the read operation fails
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viread.html">viRead</a>
     */
    protected ByteBuffer readBytes(int bufferSize) throws JVisaException {
        final NativeLongByReference readCountNative = new NativeLongByReference();
        final ByteBuffer responseBuf = ByteBuffer.allocate(bufferSize);

        final NativeLong errorCode = VISA_LIBRARY.viRead(INSTRUMENT_HANDLE,
                responseBuf,
                new NativeLong(bufferSize),
                readCountNative
        );
        RESOURCE_MANAGER.checkError(errorCode, "viRead");

        final long readCount = readCountNative.getValue().longValue();
        responseBuf.limit((int) readCount);
        return responseBuf;
    }

    /**
     * Reads a string from the instrument, e.g. a command response.
     *
     * @param bufferSize size of response buffer in bytes
     * @return response from the instrument as a String
     * @throws JVisaException if the read operation fails
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viread.html">viRead</a>
     */
    public String readString(int bufferSize) throws JVisaException {
        final ByteBuffer buf = readBytes(bufferSize);
        // TODO check for off-by-one error
        return new String(buf.array(), 0, buf.limit()).trim();
    }

    /**
     * reads a string from the instrument, usually a command response.
     *
     * @return status of the operation
     * @throws JVisaException if the read operation fails
     */
    public String readString() throws JVisaException {
        return readString(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Clears the device input and output buffers. The corresponding VISA function is not implemented in the libreVisa library.
     *
     * @throws JVisaException if the clear operation failed
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viclear.html">viClear</a>
     */
    public void clear() throws JVisaException {
        final NativeLong errorCode = VISA_LIBRARY.viClear(INSTRUMENT_HANDLE);
        RESOURCE_MANAGER.checkError(errorCode, "viClear");
    }

    /**
     * Closes an instrument session.
     *
     * @throws JVisaException if the instrument couldn't be closed
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viclosehtml">viClose</a>
     */
    @Override
    public void close() throws JVisaException {
        final NativeLong errorCode = VISA_LIBRARY.viClose(INSTRUMENT_HANDLE);
        RESOURCE_MANAGER.checkError(errorCode, "viClose");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_tmo_value.html">VI_ATTR_TMO_VALUE</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setTimeout(int timeoutMilliseconds) throws JVisaException {
        final NativeLong errorCode = VISA_LIBRARY.viSetAttribute(INSTRUMENT_HANDLE,
                new NativeLong(JVisaLibrary.VI_ATTR_TMO_VALUE),
                new NativeLong(timeoutMilliseconds)
        );

        RESOURCE_MANAGER.checkError(errorCode, "viSetAttribute");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vigetattribute.html">viGetAttribute</a>
     */
    public String getAttributeString(int attr) throws JVisaException {
        return getAttributeString(attr, DEFAULT_BUFFER_SIZE);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vigetattribute.html">viGetAttribute</a>
     */
    public String getAttributeString(int attr, int bufferSize) throws JVisaException {
        /*
        Calling memory.getString() calls the native method
            com.sun.jna.Native.getStringBytes()
        so I think it does something more interesting than getting bytes
        from the memory then calling the String constructor on the bytes.
         */
        return getAttributeMemory(attr, bufferSize).getString(0);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vigetattribute.html">viGetAttribute</a>
     */
    public byte[] getAttributeBytes(int attr) throws JVisaException {
        return getAttributeBytes(attr, DEFAULT_BUFFER_SIZE);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vigetattribute.html">viGetAttribute</a>
     */
    public byte[] getAttributeBytes(int attr, int bufferSize) throws JVisaException {
        return getAttributeMemory(attr, bufferSize).getByteArray(0, bufferSize);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vigetattribute.html">viGetAttribute</a>
     */
    private Memory getAttributeMemory(int attr, int bufferSize) throws JVisaException {
        final Memory rv = new Memory(bufferSize);

        final NativeLong errorCode = VISA_LIBRARY.viGetAttribute(INSTRUMENT_HANDLE, new NativeLong(attr), rv);
        RESOURCE_MANAGER.checkError(errorCode, "viGetAttribute");

        // apparently we can't dispose or free or finalize a Memory, just need to let JVM call finalize()
        return rv;
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setAttribute(int attr, long value)throws JVisaException{
        final NativeLong status = VISA_LIBRARY.viSetAttribute(
                INSTRUMENT_HANDLE,
                new NativeLong(attr),
                new NativeLong(value)
        );
        RESOURCE_MANAGER.checkError(status, "viSetAttribute");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_manf_name.html">VI_ATTR_MANF_NAME</a>
     */
    public String getManufacturerName() throws JVisaException {
        return getAttributeString(JVisaLibrary.VI_ATTR_MANF_NAME);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_model_name.html">VI_ATTR_MODEL_NAME</a>
     */
    public String getModelName() throws JVisaException {
        return getAttributeString(JVisaLibrary.VI_ATTR_MODEL_NAME);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_usb_serial_num.html">VI_ATTR_SERIAL_NUM</a>
     */
    public String getUsbSerialNumber() throws JVisaException {
        return getAttributeString(JVisaLibrary.VI_ATTR_USB_SERIAL_NUM);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viinstallhandler.html">viInstallHandler</a>
     */
    public void addEventHandler(JVisaEventHandler handle) throws JVisaException {
        final NativeLong errorCode = VISA_LIBRARY.viInstallHandler(INSTRUMENT_HANDLE,
                new NativeLong(handle.EVENT_TYPE.VALUE),
                handle.CALLBACK,
                handle.USER_DATA
        );
        RESOURCE_MANAGER.checkError(errorCode, "viInstallHandler");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viuninstallhandler.html">viUninstallHandler</a>
     */
    public void removeEventHandler(JVisaEventHandler handle) throws JVisaException {
        final NativeLong statusUninstall = VISA_LIBRARY.viUninstallHandler(INSTRUMENT_HANDLE,
                new NativeLong(handle.EVENT_TYPE.VALUE),
                handle.CALLBACK,
                handle.USER_DATA
        );
        RESOURCE_MANAGER.checkError(statusUninstall, "viUninstallHandler");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vienableevent.html">viEnableEvent</a>
     */
    public void enableEvent(JVisaEventType eventType) throws JVisaException {

        final NativeLong statusEnableEvent = VISA_LIBRARY.viEnableEvent(
                INSTRUMENT_HANDLE,
                new NativeLong(eventType.VALUE),
                (short) JVisaLibrary.VI_HNDLR, //mechanism
                new NativeLong(0) //context
        );
        RESOURCE_MANAGER.checkError(statusEnableEvent, "viEnableEvent");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vidisableevent.html">viDisableEvent</a>
     */
    public void disableEvent(JVisaEventType eventType) throws JVisaException {

        final NativeLong statusEnableEvent = VISA_LIBRARY.viDisableEvent(
                INSTRUMENT_HANDLE,
                new NativeLong(eventType.VALUE),
                (short) JVisaLibrary.VI_HNDLR //mechanism
        );
        RESOURCE_MANAGER.checkError(statusEnableEvent, "viDisableEvent");
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vidiscardevents.html">viDiscardEvents</a>
     */
    public void discardEvents(JVisaEventType eventType) throws JVisaException {
        final NativeLong status = VISA_LIBRARY.viDiscardEvents(
                INSTRUMENT_HANDLE,
                new NativeLong(eventType.VALUE),
                (short) JVisaLibrary.VI_ALL_MECH //mechanism
        );
        RESOURCE_MANAGER.checkError(status, "viDiscardEvents");
    }

    /**
     * VI_ATTR_TERMCHAR is the termination character. When the termination character is read and VI_ATTR_TERMCHAR_EN is
     * enabled during a read operation, the read operation terminates. The default is '\n' (line feed).
     * <p>
     * For a Serial INSTR session, VI_ATTR_TERMCHAR is Read/Write when the corresponding session is not enabled to
     * receive VI_EVENT_ASRL_TERMCHAR events. When the session is enabled to receive VI_EVENT_ASRL_TERMCHAR events, the
     * attribute VI_ATTR_TERMCHAR is Read Only. For all other session types, the attribute VI_ATTR_TERMCHAR is always
     * Read/Write.
     *
     * @see
     * <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_termchar.html">VI_ATTR_TERMCHAR</a>
     * @see <a href="https://github.com/pyvisa/pyvisa/blob/1.13.0/pyvisa/resources/messagebased.py#L96">read_termination
     * in PyVISA</a>
     * @see <a href="https://pyvisa.readthedocs.io/en/latest/introduction/communication.html">PyVISA docs page
     * "Communicating with your instrument"</a>
     */
    public void setReadTerminationCharacter(char readTerminator) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_TERMCHAR, (int) readTerminator);
    }

    /**
     * VI_ATTR_TERMCHAR is the termination character. When the termination character is read and VI_ATTR_TERMCHAR_EN is
     * enabled during a read operation, the read operation terminates. The default is '\n' (line feed).
     * <p>
     * For a Serial INSTR session, VI_ATTR_TERMCHAR is Read/Write when the corresponding session is not enabled to
     * receive VI_EVENT_ASRL_TERMCHAR events. When the session is enabled to receive VI_EVENT_ASRL_TERMCHAR events, the
     * attribute VI_ATTR_TERMCHAR is Read Only. For all other session types, the attribute VI_ATTR_TERMCHAR is always
     * Read/Write.
     *
     * @see
     * <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_termchar.html">VI_ATTR_TERMCHAR</a>
     */
    public char getReadTerminationCharacter() throws JVisaException {
        return (char) getAttributeBytes(JVisaLibrary.VI_ATTR_TERMCHAR, 1)[0];
    }

    /**
     * VI_ATTR_TERMCHAR_EN is a flag that determines whether the read operation should terminate when a termination
     * character is received. The default is false. This attribute is ignored if VI_ATTR_ASRL_END_IN is set to
     * VI_ASRL_END_TERMCHAR. This attribute is valid for both raw I/O (viRead) and formatted I/O (viScanf).
     *
     * @see
     * <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_termchar_en.html">VI_ATTR_TERMCHAR_EN</a>
     * @see <a href="https://github.com/pyvisa/pyvisa/blob/1.13.0/pyvisa/resources/messagebased.py#L96">read_termination
     * in PyVISA</a>
     * @see <a href="https://pyvisa.readthedocs.io/en/latest/introduction/communication.html">PyVISA docs page
     * "Communicating with your instrument"</a>
     */
    public void setReadTerminationCharacterEnabled(boolean isReadTerminationCharacterEnabled) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_TERMCHAR_EN, isReadTerminationCharacterEnabled ? 1 : 0);
    }

    public boolean isReadTerminationCharacterEnabled() throws JVisaException {
        return getAttributeBytes(JVisaLibrary.VI_ATTR_TERMCHAR_EN, 1)[0] == 1;
    }

    /**
     * Specify a string which will be appended to all commands sent to the instrument, or null to not append a
     * terminator.
     *
     * @see <a href="https://pyvisa.readthedocs.io/en/latest/introduction/communication.html">PyVISA docs page
     * "Communicating with your instrument"</a>
     */
    public void setWriteTerminator(String writeTerminator) {
        this.writeTerminator = writeTerminator;
    }

    public String getWriteTerminator() {
        return writeTerminator;
    }

}
