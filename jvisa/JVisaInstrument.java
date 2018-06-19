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
 */
package jvisa;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;

/**
 * This class provides basic Visa instrument handling.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 */
public class JVisaInstrument {

    private final static int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * handle for one instrument This class handles only one instrument. To control more instruments instantiate one class per instrument. The resource manager is common (static) to all instances.
     */
    private final NativeLong instrumentHandle;
    private final JVisaResourceManager resourceManager;
    private final JVisaInterface visaLib;
    public final String resourceName;

    public JVisaInstrument(JVisaResourceManager resourceManager, NativeLongByReference instrumentHandle, String resourceName) {
        this.resourceManager = resourceManager;
        this.visaLib = resourceManager.visaLib;
        this.instrumentHandle = instrumentHandle.getValue();
        this.resourceName = resourceName;

    }

    /**
     * sends a command and receives its response. It insists in receiving at least a given number of bytes.
     *
     * @param command string to send
     * @param bufferSize size of buffer to allocate. The size can be set smaller since it gets allocated with readCount.
     * @return status of the operation
     * @throws jvisa.JVisaException
     */
    public String sendAndReceiveString(String command, int bufferSize) throws JVisaException {
        write(command);
        return readString(bufferSize);
    }

    /**
     * sends a command and receives its response. It receives as many bytes as the instrument is sending.
     *
     * TODO peter: investigate receiving as many bytes as instrument sends. actually limited to 1024?
     *
     * @param command string to send
     * @return status of the operation
     * @throws jvisa.JVisaException
     */
    public String sendAndReceiveString(String command) throws JVisaException {
        write(command);
        return readString(DEFAULT_BUFFER_SIZE);
    }

    /**
     * sends a command and receives its response. It insists in receiving at least a given number of bytes.
     *
     * @param command string to send
     * @param bufferSize size of buffer to allocate. The size can be set smaller since it gets allocated with readCount.
     * @return status of the operation
     * @throws jvisa.JVisaException
     */
    public ByteBuffer sendAndReceiveBytes(String command, int bufferSize) throws JVisaException {
        write(command);
        return readBytes(bufferSize);
    }

    /**
     * sends a command and receives its response. It receives as many bytes as the instrument is sending.
     *
     * TODO peter: investigate receiving as many bytes as instrument sends. actually limited to 1024?
     *
     * @param command string to send
     * @return status of the operation
     * @throws jvisa.JVisaException
     */
    public ByteBuffer sendAndReceiveBytes(String command) throws JVisaException {
        write(command);
        return readBytes(DEFAULT_BUFFER_SIZE);
    }

    /**
     * sets the communication timeout.
     *
     * @param timeout in ms
     * @return status of the operation
     */
    /*
    public long setTimeout(int timeout) {
        if (isLibreVisa) {
            System.err.println("setTimeout() is not implemetned in Libre Visa.");
        }
        return setAttribute(JVisaInterface.VI_ATTR_TMO_VALUE,
                timeout, getInstrumentHandle());
    }
     */
    /**
     * sends buffer content (usually a command) to the instrument.
     *
     * @param command command or other ASCII data
     * @throws jvisa.JVisaException if viWrite does not succeed
     */
    public void write(String command) throws JVisaException {
        ByteBuffer buffer = JVisa.stringToByteBuffer(command);
        int commandLength = command.length();

        NativeLongByReference returnCount = new NativeLongByReference();
        NativeLong visaStatus = visaLib.viWrite(instrumentHandle, buffer, new NativeLong(commandLength), returnCount);

        JVisa.throwForStatus(visaStatus, "viWrite");

        long count = returnCount.getValue().longValue();
        if (count != commandLength) {
            throw new JVisaException(String.format("Could only write %d instead of %d bytes.",
                    count, commandLength));
        }
    }

    /**
     * reads data from the instrument, e.g. a command response or data.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viread/
     *
     * @param bufferSize size of response buffer in bytes
     * @return
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    protected ByteBuffer readBytes(int bufferSize) throws JVisaException {
        NativeLongByReference readCountNative = new NativeLongByReference();
        ByteBuffer response = ByteBuffer.allocate(bufferSize);

        NativeLong visaStatus = visaLib.viRead(instrumentHandle, response, new NativeLong(bufferSize), readCountNative);
        JVisa.throwForStatus(visaStatus, "viRead");

        long readCount = readCountNative.getValue().longValue();
        if (readCount < 1) {
            throw new JVisaException("read zero bytes from instrument");
        }

        return response;
    }

    /**
     * reads a string from the instrument, e.g. a command response.
     *
     * @param bufferSize size of response buffer in bytes
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    public String readString(int bufferSize) throws JVisaException {
        NativeLongByReference readCountNative = new NativeLongByReference();
        ByteBuffer response = ByteBuffer.allocate(bufferSize);

        NativeLong visaStatus = visaLib.viRead(instrumentHandle, response, new NativeLong(bufferSize), readCountNative);
        JVisa.throwForStatus(visaStatus, "viRead");

        long readCount = readCountNative.getValue().longValue();
        if (readCount < 1) {
            throw new JVisaException("read zero bytes from instrument");
        }

        return new String(response.array(), 0, (int) readCount);
    }

    /**
     * reads a string from the instrument, usually a command response.
     *
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    public String readString() throws JVisaException {
        return readString(DEFAULT_BUFFER_SIZE);
    }

    /**
     * reads a byte array from the instrument, usually a command response.
     *
     * @param response response byte array
     * @param bufferSize size of response buffer in bytes
     * @param expectedCount expected number of bytes in response. This parameter is only used under Linux / libreVisa.
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    /*
    public long read(JVisaReturnBytes response, int bufferSize, int expectedCount)
            throws JVisaException {
        long visaStatus;
        try {
            readCount = 0;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            visaStatus = read(buffer, bufferSize);
            if (visaStatus == VISA_JAVA_ERROR) {
                return visaStatus;
            }
            if (isLibreVisa == false || expectedCount == 0) {
                response.returnBytes = new byte[(int) readCount];
                System.arraycopy(buffer.array(), 0, response.returnBytes, 0, (int) readCount);
                return VisatypeLibrary.VI_SUCCESS;
            }
            // We did not get all data with one read. This happens when using the libreVisa library.
            // The first chunk size is 12288 (0x3000) bytes.
            int bytesLeft = expectedCount - (int) readCount;
            int index = 0;
            response.returnBytes = new byte[expectedCount];
            System.arraycopy(buffer.array(), 0, response.returnBytes, index, (int) readCount);
            while (bytesLeft > 0) {
                index += readCount;
                visaStatus = read(buffer, bufferSize);
                if (visaStatus == VISA_JAVA_ERROR) {
                    return visaStatus;
                }
                System.arraycopy(buffer.array(), 0, response.returnBytes, index, (int) readCount);
                bytesLeft -= readCount;
            }
            // Attempting to read from the instrument when there are no bytes left in
            // its output buffer crashes the libreVisa library.
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      baos.write(buffer.array(), 0, (int) readCount);
//      
//      int index = (int) readCount;
//      while (readCount > 0) {
//        index += readCount;
//        visaStatus = read(buffer, bufferSize);
//        if (visaStatus == VISA_JAVA_ERROR)
//          return visaStatus;
//        baos.write(buffer.array(), 0, (int) readCount);
//      }
//      response.returnBytes = baos.toByteArray();      

            return VisatypeLibrary.VI_SUCCESS;
        } catch (JVisaException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new JVisaException(VISA_JAVA_ERROR);
        }
    }
     */
    /**
     * reads a byte array from the instrument, usually a command response. It is used when the expected number of bytes in the response is unknown or or a Windows VISA library is used.
     *
     * @param response response byte array
     * @param bufferSize size of response buffer in bytes
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    /*
    public long read(JVisaReturnBytes response, int bufferSize) throws JVisaException {
        return read(response, bufferSize, 0);
    }
     */
    /**
     * clears the device input and output buffers. The corresponding VISA function is not implemented in the libreVisa library.
     *
     * @throws jvisa.JVisaException
     */
    public void clear() throws JVisaException {
        NativeLong visaStatus = visaLib.viClear(instrumentHandle);
        JVisa.throwForStatus(visaStatus, "viClear");
    }

    /**
     * closes an instrument session.
     *
     * @throws jvisa.JVisaException
     */
    public void closeInstrument() throws JVisaException {
        NativeLong visaStatus = visaLib.viClose(instrumentHandle);
        JVisa.throwForStatus(visaStatus, "viClose");
    }

}
