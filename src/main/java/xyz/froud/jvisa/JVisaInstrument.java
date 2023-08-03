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
    public String queryString(String command, int bufferSize) throws JVisaException {
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
    public String queryString(String command) throws JVisaException {
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
    public ByteBuffer queryBytes(String command, int bufferSize) throws JVisaException {
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
    public ByteBuffer queryBytes(String command) throws JVisaException {
        write(command);
        return readBytes(DEFAULT_BUFFER_SIZE);
    }

    public byte[] queryBinaryBlock(String command) throws JVisaException {
        write(command);
        return readBinaryBlock();
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
     * @param byteCount how many bytes to read
     * @return response from instrument as bytes
     * @throws JVisaException if the read operation fails
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/viread.html">viRead</a>
     */
    public ByteBuffer readBytes(int byteCount) throws JVisaException {
        final NativeLongByReference readCountNative = new NativeLongByReference();
        final ByteBuffer responseBuf = ByteBuffer.allocate(byteCount);

        final NativeLong errorCode = VISA_LIBRARY.viRead(INSTRUMENT_HANDLE,
                responseBuf,
                new NativeLong(byteCount),
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
     * @see <a href="http://webuser.unicas.it/misure/MAQ_OLD%20(VO)/Dispense/DISP_7STANDARD%20IEEE%20488_2%201992.pdf">PDF of IEEE Std 488.2-1992</a>
     * @see "section 7.7.6 &lt;ARBITRARY BLOCK PROGRAM DATA&gt; of IEEE Std 488.2-1992"
     * @see "section 8.7.9 &lt;DEFINITE LENGTH ARBITRARY BLOCK RESPONSE DATA&gt; of IEEE Std
     * 488.2-1992"
     * @see
     * <a href="https://github.com/pyvisa/pyvisa/blob/e01a7093b1df28f907631d96ba8699a8f0287023/pyvisa/resources/messagebased.py#L533">Function
     * <code>read_binary_values</code> in PyVISA</a>
     * @see
     * <a href="https://github.com/pyvisa/pyvisa/blob/e01a7093b1df28f907631d96ba8699a8f0287023/pyvisa/util.py#L371">Function
     * <code>parse_ieee_block_header</code> in PyVISA</a>

     */
    public byte[] readBinaryBlock() throws JVisaException {
        /*
        Diagram from section 8.7.9.2 (PDF page 101) of IEEE Std 488.2-1992:

                                  /--------<--------\     /---------<----------\
                  +----------+   |    +---------+    |   |    +------------+    |
                  | <nonzero |    \   |         |   /     \   |  <8 bit    |   /
        ---->#--->|  digit>  |------->| <digit> |------------>| data byte> |--------->
                  | 7.7.6.2  |        | 7.6.1.2 |       \     | 7.7.6.2    |     /
                  +----------+        +---------+        \    +------------+    /
                                                          ----------->----------
        where
        * <digit> is defined as a single ASCII-encoded byte in the range 30-39 (48-57 decimal).
        * <nonzero digit> is defined as a single ASCII encoded byte in the range of 31-39
          (49-57 decimal).
        * <8 bit data byte> is defined as an 8 bit byte in the range of 00-FF (0-255 decimal).

        7.7.6.4 Rules
        The value of the <nonzero digit> element shall equal the number of <digit> elements that
        follow. The value of the <digit> elements taken together as a decimal integer shall equal
        the number of <8 bit data byte> elements that follow.

        Examples with spaces added for readability:
            Response from "waveform:data?":
            #8 00488251 \80\80\80...

            Response from "display:data?":
            #9 001152054 BM6\94\11...
        */
        final byte EXPECTED_FIRST_BYTE = '#';
        final byte actualFirstByte = readBytes(1).get(0);
        if (actualFirstByte != EXPECTED_FIRST_BYTE) {
            throw new JVisaException(String.format(
                    "can't read binary block, the first byte is %d (0x%02X) ('%c'), expected %d (0x%02X) ('%c')",
                    Byte.toUnsignedInt(actualFirstByte), actualFirstByte, (char) actualFirstByte,
                    Byte.toUnsignedInt(EXPECTED_FIRST_BYTE), EXPECTED_FIRST_BYTE, (char) EXPECTED_FIRST_BYTE
            ));
        }

        final byte secondByte = readBytes(1).get(0);
        if (secondByte == '0') {
            throw new UnsupportedOperationException("can't read binary block, indefinite-length not supported");
        }
        if (!Character.isDigit(secondByte)) {
            throw new JVisaException(String.format(
                    "can't read binary block, the second byte is %d (0x%02X) ('%c'), expected a nonzero ASCII digit (49 - 57) (0x31 - 0x39)",
                    Byte.toUnsignedInt(secondByte), secondByte, (char) secondByte
            ));
        }

        final int firstCount = secondByte - '0';
        final String secondCountString = new String(readBytes(firstCount).array());
        final int secondCount;
        try {
            secondCount = Integer.parseInt(secondCountString);
        } catch (NumberFormatException ex) {
            throw new JVisaException("can't read binary block, couldn't parse an integer from string \"" + secondCountString + "\"", ex);
        }

        return readBytes(secondCount).array();
    }

    /**
     * Clears the device input and output buffers. The corresponding VISA function is not
     * implemented in the libreVisa library.
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
    public void setTimeout(long timeoutMilliseconds) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_TMO_VALUE, timeoutMilliseconds);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_baud.html">VI_ATTR_ASRL_BAUD</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setSerialBaudRate(int baudRate) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_ASRL_BAUD, baudRate);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_data_bits.html">VI_ATTR_ASRL_DATA_BITS</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setSerialDataBits(int dataBits) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_ASRL_DATA_BITS, dataBits);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_flow_cntrl.html">VI_ATTR_ASRL_FLOW_CNTRL</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setSerialFlowControl(SerialFlowControl flowControl) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_ASRL_FLOW_CNTRL, flowControl.VALUE);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_parity.html">VI_ATTR_ASRL_PARITY</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setSerialParity(SerialParity parity) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_ASRL_PARITY, parity.VALUE);
    }

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_stop_bits.html">VI_ATTR_ASRL_STOP_BITS</a>
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/visetattribute.html">viSetAttribute</a>
     */
    public void setSerialStopBits(SerialStopBits stopBits) throws JVisaException {
        setAttribute(JVisaLibrary.VI_ATTR_ASRL_STOP_BITS, stopBits.VALUE);
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
    public void setAttribute(int attr, long value) throws JVisaException{
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
