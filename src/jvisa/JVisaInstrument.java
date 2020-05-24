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

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;
import jvisa.eventhandling.JVisaEventHandle;

/**
 * Represents a Visa instrument. This is a wrapper around the native C instrument handle.
 *
 * To use, call openInstrument() from a JVisaResourceManager instance.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 */
public class JVisaInstrument {

    private final static int DEFAULT_BUFFER_SIZE = 1024;

    private final NativeLong INSTRUMENT_HANDLE;
    private final JVisaResourceManager RESOURCE_MANAGER;
    private final JVisaLibrary VISA_LIBRARY;
    public final String RESOURCE_NAME;

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
     * @throws jvisa.JVisaException if the write fails or the read fails
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
     * @throws jvisa.JVisaException if the write fails or the read fails
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
     * @throws jvisa.JVisaException if the write fails or the read fails
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
     * @throws jvisa.JVisaException if the write fails or the read fails
     */
    public ByteBuffer sendAndReceiveBytes(String command) throws JVisaException {
        write(command);
        return readBytes(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Sends a command to the instrument.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viwrite/
     *
     * @param command the command to send
     * @throws jvisa.JVisaException if the write fails
     */
    public void write(String command) throws JVisaException {
        final ByteBuffer buffer = JVisaUtils.stringToByteBuffer(command);
        final int commandLength = command.length();

        final NativeLongByReference returnCount = new NativeLongByReference();
        final NativeLong visaStatus = VISA_LIBRARY.viWrite(INSTRUMENT_HANDLE, buffer, new NativeLong(commandLength), returnCount);

        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viWrite");

        final long count = returnCount.getValue().longValue();
        if (count != commandLength) {
            throw new JVisaException(String.format("Could only write %d instead of %d bytes.",
                    count, commandLength));
        }
    }

    /**
     * Reads data from the instrument, e.g. a command response or data.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viread/
     *
     * @param bufferSize size of response buffer in bytes
     * @return response from instrument as bytes
     * @throws jvisa.JVisaException if the read fails
     */
    protected ByteBuffer readBytes(int bufferSize) throws JVisaException {
        final NativeLongByReference readCountNative = new NativeLongByReference();
        final ByteBuffer responseBuf = ByteBuffer.allocate(bufferSize);

        final NativeLong visaStatus = VISA_LIBRARY.viRead(INSTRUMENT_HANDLE, responseBuf, new NativeLong(bufferSize), readCountNative);
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viRead");

        final long readCount = readCountNative.getValue().longValue();
        if (readCount < 1) {
            throw new JVisaException("read zero bytes from instrument");
        }

        return responseBuf;
    }

    /**
     * Reads a string from the instrument, e.g. a command response.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viread/
     *
     * @param bufferSize size of response buffer in bytes
     * @return response from the instrument as a String
     * @throws jvisa.JVisaException if the read fails
     */
    public String readString(int bufferSize) throws JVisaException {
        final NativeLongByReference readCountNative = new NativeLongByReference();
        final ByteBuffer responseBuf = ByteBuffer.allocate(bufferSize);

        final NativeLong visaStatus = VISA_LIBRARY.viRead(INSTRUMENT_HANDLE, responseBuf, new NativeLong(bufferSize), readCountNative);
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viRead");

        final long readCount = readCountNative.getValue().longValue();
        if (readCount < 1) {
            throw new JVisaException("read zero bytes from instrument");
        }

        return new String(responseBuf.array(), 0, (int) readCount).trim();
    }

    /**
     * reads a string from the instrument, usually a command response.
     *
     * @return status of the operation
     * @throws jvisa.JVisaException if the read fails
     */
    public String readString() throws JVisaException {
        return readString(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Clears the device input and output buffers. The corresponding VISA function is not implemented in the libreVisa library.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viclear/
     *
     * @throws jvisa.JVisaException if the clear operation failed
     */
    public void clear() throws JVisaException {
        final NativeLong visaStatus = VISA_LIBRARY.viClear(INSTRUMENT_HANDLE);
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viClear");
    }

    /**
     * Closes an instrument session.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viclose/
     *
     * @throws jvisa.JVisaException if the instrument couldn't be closed
     */
    public void close() throws JVisaException {
        final NativeLong visaStatus = VISA_LIBRARY.viClose(INSTRUMENT_HANDLE);
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viClose");
    }

    /*
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vi_attr_tmo_value/
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/visetattribute/
     */
    public void setTimeout(int timeoutMilliseconds) throws JVisaException {
        final NativeLong visaStatus = VISA_LIBRARY.viSetAttribute(INSTRUMENT_HANDLE,
                new NativeLong(JVisaLibrary.VI_ATTR_TMO_VALUE),
                new NativeLong(timeoutMilliseconds)
        );

        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viSetAttribute");
    }

    /*
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vi_attr_tmo_value/
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vigetattribute/
     */
    public String getAttribute(int attr) throws JVisaException {
        final Memory mem = new Memory(256);

        final NativeLong visaStatus = VISA_LIBRARY.viGetAttribute(INSTRUMENT_HANDLE, new NativeLong(attr), mem);
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viGetAttribute");

        // apparently we can't dispose or free or finalize a Memory, just need to let JVM call finalize()
        return mem.getString(0, new String());
    }

    public String getManufacturerName() throws JVisaException {
        return getAttribute(JVisaLibrary.VI_ATTR_MANF_NAME);
    }

    public String getModelName() throws JVisaException {
        return getAttribute(JVisaLibrary.VI_ATTR_MODEL_NAME);
    }

    public String getSerialNumber() throws JVisaException {
        return getAttribute(JVisaLibrary.VI_ATTR_USB_SERIAL_NUM);
    }

    public void installServiceRequestHandler(JVisaEventHandle handle) throws JVisaException {
        final NativeLong visaStatus = VISA_LIBRARY.viInstallHandler(INSTRUMENT_HANDLE,
                new NativeLong(JVisaLibrary.VI_EVENT_SERVICE_REQ),
                handle.CALLBACK,
                handle.POINTER_TO_USER_DATA
        );
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, visaStatus, "viInstallHandler");
    }

    public void uninstallServiceRequestHandler(JVisaEventHandle handle) throws JVisaException {
        final NativeLong statusUninstall = VISA_LIBRARY.viUninstallHandler(INSTRUMENT_HANDLE,
                new NativeLong(JVisaLibrary.VI_EVENT_SERVICE_REQ),
                handle.CALLBACK,
                handle.POINTER_TO_USER_DATA
        );
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, statusUninstall, "viUninstallHandler");
    }

    public void enableServiceRequestEvent() throws JVisaException {

        final NativeLong statusEnableEvent = VISA_LIBRARY.viEnableEvent(
                INSTRUMENT_HANDLE,
                new NativeLong(JVisaLibrary.VI_EVENT_SERVICE_REQ), //event type
                (short) JVisaLibrary.VI_HNDLR, //mechanism
                new NativeLong(0) //context
        );
        JVisaUtils.throwForStatus(RESOURCE_MANAGER, statusEnableEvent, "viEnableEvent");
    }

}
