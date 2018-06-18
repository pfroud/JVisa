/**
 * @license
 *
 * Copyright 2014-2018 Günter (gfuchs@acousticmicroscopy.com)
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
 * @version 0.6
 * @todo Catch librevisa::exception.
 */
/**
 * Modifications by Peter Froud, Lumenetix Inc
 * June 2018
 */
package jvisa;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import visatype.VisatypeLibrary;

/**
 * This class provides Java wrapper functions around the native Windows VISA API.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 * 
 * TODO Test instantiating more than one instrument.
 * 
 * TODO Throw exception instead of returning status.
 */
public class JVisa {

    /**
     * version of this library
     */
    public static final String JVISA_VERSION = "JVisa Version 0.6";
    /**
     * What is called viSession in Visa DLL becomes Handle in JVisa so that viSession is not mistaken for a Java object. The handle gets initialized only once when this class is loaded (first time only initialization).
     */
    protected static long visaResourceManagerHandle = 0;
    /**
     * number of bytes read
     */
    long readCount;

    /**
     * This method gets the resource manager handle.
     *
     * @return handle
     */
    public long getResourceManagerHandle() {
        return visaResourceManagerHandle;
    }
    /**
     * handle for one instrument This class handles only one instrument. To control more instruments instantiate one class per instrument. The resource manager is common (static) to all instances.
     */
    protected NativeLong visaInstrumentHandle;

    /**
     * This method returns the handle for the instrument.
     *
     * @return instrument handle
     */
    public long getInstrumentHandle() {
        return visaInstrumentHandle.longValue();
    }
    /**
     * default size for input buffer
     */
    protected int bufferSizeDefault = 1024;
    /**
     * encoding of response when it is a string
     */
    protected String responseEncoding = "UTF8";
    /**
     * the name of this class used by its logger
     */
    protected static final String CLASS_NAME = JVisa.class.getName();
    /**
     * logger of this class
     */
    public static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    /**
     * logger file handler
     */
    public static FileHandler logFileHandler;
    /**
     * logger formatter
     */
    public static SimpleFormatter logFormatter;
    /**
     * Log folder is added to the current path.
     */
    public final String LOG_FOLDER = "log/";
    /**
     * constant when VISA return status is not success
     */
    public static final long VISA_JAVA_ERROR = 0x7FFFFFFF;
    /**
     * JVisa status object
     */
    public JVisaStatus statusObject;
    /**
     * name of VISA library
     */
//  public static final String JNA_LIBRARY_NAME = "visa";
    /**
     * instance of loaded VISA library A user of this library should check this variable not being null after instantiation
     */
    public static JVisaInterface visaLib;
    /**
     * libreVisa has not implemented the entire API. For example, only one attribute is implemented. This flag allows an application to distinguish.
     */
    public static boolean isLibreVisa = false;
    /**
     * true if the libreVisa version currently under local development is used
     */
    public static boolean isLibreVisaDevelop = false;

    /**
     * Constructor loads the native library. On Windows, it first tries to load tkVisa64.dll (Tektronix). If not found, it tries to load nivisa64.dll (National Instruments). On Linux, it tries to load libvisa.so (libreVisa).
     */
    public JVisa() {
        try {
            Path logPath = Paths.get(LOG_FOLDER);
            if (Files.notExists(logPath)) {
                Files.createDirectory(logPath);
            }
            logFileHandler = new FileHandler(String.format("%s%sLog.txt", LOG_FOLDER, JVisa.class.getSimpleName()));
            logFormatter = new SimpleFormatter();
            logFileHandler.setFormatter(logFormatter);
            LOGGER.addHandler(logFileHandler);
            String visaLibName = "nivisa64.dll";
            // 32-bit Windows is not supported. See also
            // http://stackoverflow.com/questions/21486086/cant-load-personal-dll-with-jna-from-netbeans

            visaLib = (JVisaInterface) Native.loadLibrary(visaLibName, JVisaInterface.class);
            LOGGER.setLevel(Level.SEVERE);
            LOGGER.log(Level.INFO, String.format("Success loading %s.", visaLibName));
            statusObject = new JVisaStatus(bufferSizeDefault, responseEncoding, visaLib);
        } catch (SecurityException | IOException | UnsatisfiedLinkError e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * This method sets the path and file name for a log file.
     *
     * @param logPath path and full name of log file
     */
    public void setLogFile(String logPath) {
        try {
            // todo Is closing necessary, or does removal close the file?
            logFileHandler.close();
            LOGGER.removeHandler(logFileHandler);
            logFileHandler = new FileHandler(logPath);
            LOGGER.addHandler(logFileHandler);
        } catch (SecurityException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * This method creates a session for a default resource manager.
     *
     * @return status of the operation
     */
    public long openDefaultResourceManager() {
        try {
            LOGGER.info("Opening Default Resource Manager.");
            LOGGER.info(String.format("VISA version 0x%08X", JVisaInterface.VI_SPEC_VERSION));
            LOGGER.info("Open resource manager.");
            NativeLongByReference pViSession = new NativeLongByReference();
            NativeLong visaStatus = visaLib.viOpenDefaultRM(pViSession);
            statusObject.setStatus(visaStatus);
            if (statusObject.visaStatusLong != VisatypeLibrary.VI_SUCCESS) {
                return statusObject.visaStatusLong;
            }
            LOGGER.info(String.format("visaStatus = 0x%08X", statusObject.visaStatusLong));
            visaResourceManagerHandle = pViSession.getValue().longValue();
            if (visaResourceManagerHandle != 0) {
                statusObject.printStatusDescription(visaResourceManagerHandle);
                statusObject.resourceManagerHandle = visaResourceManagerHandle;
            }
        } catch (Error | Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            statusObject.resourceManagerHandle = 0;
            return VISA_JAVA_ERROR;
        }
        return statusObject.visaStatusLong;
    }

    /**
     * This method closes the resource manager.
     *
     * @return status of the operation
     */
    public static long closeResourceManager() {
        try {
            LOGGER.info("Close resource manager.");
            NativeLong visaStatus = visaLib.viClose(new NativeLong(visaResourceManagerHandle));
//      statusObject.setStatus(visaStatus);
            return visaStatus.longValue();
//      statusObject.resourceManagerHandle = visaResourceManagerHandle = 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
//    return statusObject.visaStatusLong;
    }

    /**
     * This method flushes one or more buffers.
     *
     * @param bufferType mask indicating which buffers to flush
     * @return status of the operation
     * @todo Does not work, at least not with libreVisa.
     */
    public long flush(int bufferType) {
        try {
            LOGGER.info(String.format("Flush 0x%04X buffer.", (short) bufferType));
            NativeLong visaStatus = visaLib.viFlush(visaInstrumentHandle, (short) bufferType);
            statusObject.setStatus(visaStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
        return statusObject.visaStatusLong;
    }

    /**
     * This method gets an attribute of type byte (native ViUInt8).
     *
     * @param attribute which attribute to get
     * @param value contains an attribute
     * @param sessionHandle handle of resource manager or instrument
     * @return status of the operation
     */
    public long getAttribute(int attribute, JVisaReturnNumber value, long sessionHandle) {
        try {
            NativeLong visaStatus;
            String formatString = "Attribute value = 0x";
            LOGGER.info(String.format("Get attribute 0x%08X.", attribute));
            NativeLong attributeNative = new NativeLong(attribute);
            NativeLong sessionNative = new NativeLong(sessionHandle);
            if (value.returnNumber instanceof Short) {
                ByteByReference pByte = new ByteByReference();
                visaStatus = visaLib.viGetAttribute(sessionNative, attributeNative, pByte.getPointer());
                value.returnNumber = pByte.getValue();
                formatString += "%02X";
            } else if (value.returnNumber instanceof Integer) {
                IntByReference pInt = new IntByReference();
                visaStatus = visaLib.viGetAttribute(sessionNative, attributeNative, pInt.getPointer());
                value.returnNumber = pInt.getValue();
                formatString += "%04X";
            } else if (value.returnNumber instanceof Long) {
                LongByReference pLong = new LongByReference();
                visaStatus = visaLib.viGetAttribute(sessionNative, attributeNative, pLong.getPointer());
                value.returnNumber = pLong.getValue();
                formatString += "%08X";
            } else {
                return VISA_JAVA_ERROR;
            }
            statusObject.setStatus(visaStatus);
            LOGGER.info(String.format(formatString, value.returnNumber));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
        return statusObject.visaStatusLong;
    }

    /**
     * This method gets an attribute of type String (native ViPChar).
     *
     * @param attribute which attribute to get
     * @param value contains an attribute of type String
     * @param sessionHandle handle of resource manager or instrument
     * @return status of the operation
     */
    public long getAttribute(int attribute, JVisaReturnString value, long sessionHandle) {
        try {
            LOGGER.info(String.format("Get attribute 0x%08X.", attribute));
            Memory responseBuffer = new Memory(bufferSizeDefault);
            NativeLong visaStatus = visaLib.viGetAttribute(
                    new NativeLong(visaResourceManagerHandle),
                    new NativeLong(attribute),
                    (Pointer) responseBuffer);
            statusObject.setStatus(visaStatus);
            if (statusObject.visaStatusLong == VisatypeLibrary.VI_SUCCESS) {
                value.returnString = responseBuffer.getString(0, responseEncoding).trim();
                LOGGER.info(String.format("Attribute value = %s", value.returnString));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
        return statusObject.visaStatusLong;
    }

    /**
     * This method sets an attribute.
     *
     * @param attribute which attribute to get
     * @param value contains an attribute
     * @param sessionHandle handle of resource manager or instrument
     * @return status of the operation
     */
    public long setAttribute(int attribute, int value, long sessionHandle) {
        try {
            LOGGER.info(String.format("Set attribute 0x%08X to 0x%08X.", attribute, value));
            NativeLong visaStatus = visaLib.viSetAttribute(
                    new NativeLong(sessionHandle),
                    new NativeLong(attribute),
                    new NativeLong(value));
            statusObject.setStatus(visaStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
        return statusObject.visaStatusLong;
    }

    /**
     * This method sets an attribute for a resource manager.
     *
     * @param attribute which attribute to get
     * @param value contains an attribute
     * @return status of the operation
     */
    public long setAttribute(int attribute, int value) {
        return setAttribute(attribute, value, visaResourceManagerHandle);
    }

    /**
     * This method sets the communication timeout.
     *
     * @param timeout in ms
     * @return status of the operation
     */
    public long setTimeout(int timeout) {
        try {
            if (isLibreVisa) {
                LOGGER.info("Set timeout is not implemented.");
                return VisatypeLibrary.VI_SUCCESS;
            }
            LOGGER.info("Set timeout.");
            return setAttribute(JVisaInterface.VI_ATTR_TMO_VALUE,
                    timeout, getInstrumentHandle());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
    }

    /**
     * This method gets the version of the resource (for example the version of tkVisa64.dll).
     *
     * @param version resource version number
     * @return status of the operation
     */
    public long getResourceVersion(JVisaReturnNumber version) {
        try {
            LOGGER.info("Get resource version.");
            long visaStatus = getAttribute(JVisaInterface.VI_ATTR_RSRC_SPEC_VERSION,
                    version, visaResourceManagerHandle);
            if (visaStatus == VisatypeLibrary.VI_SUCCESS) {
                LOGGER.info(String.format("Resource version = 0x%08X", version.returnNumber));
            }
            return visaStatus;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
    }

    /**
     * This method converts a Java String to a ByteBuffer / C-type string.
     *
     * @param source string to convert
     * @return Java string converted to C-type string (0 terminated)
     */
    public ByteBuffer stringToByteBuffer(String source) {
        try {
            ByteBuffer dest = ByteBuffer.allocate(source.length() + 1);
            dest.put(source.getBytes(responseEncoding));
            dest.position(0);
            return dest;
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method opens an instrument session.
     *
     * @param instrument string that contains the instrument address and bus interface, for example TCPIP::192.168.1.106::INSTR
     * @return status of the operation
     */
    public long openInstrument(String instrument) {
        NativeLong visaStatus;
        NativeLongByReference pViInstrument = new NativeLongByReference();
        try {
            LOGGER.info(String.format("Open instrument %s.", instrument));
            ByteBuffer pViString = stringToByteBuffer(instrument);
            if (pViString == null) {
                return VISA_JAVA_ERROR;
            }
            visaStatus = visaLib.viOpen(
                    new NativeLong(visaResourceManagerHandle),
                    pViString, // byte buffer for instrument string
                    new NativeLong(0), // access mode (locking or not). 0:Use Visa default
                    new NativeLong(0), // timeout, only when access mode equals locking
                    pViInstrument // pointer to instrument object
            );
            statusObject.setStatus(visaStatus);
            if (statusObject.visaStatusLong == VisatypeLibrary.VI_SUCCESS) {
                visaInstrumentHandle = pViInstrument.getValue();
                LOGGER.info(String.format("viInstrument = 0x%08X.", visaInstrumentHandle.longValue()));
            } else {
                LOGGER.log(Level.SEVERE, String.format("Could not open session for %s.",
                        instrument), (Throwable) null);
                visaInstrumentHandle = new NativeLong(0);
            }
            return statusObject.visaStatusLong;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
    }

    public void findResources() throws UnsupportedEncodingException {

        ByteBuffer regex = stringToByteBuffer("?*");
        if (regex == null) {
            System.err.println("stringToByteBuffer() failed");
            return;
        }

        NativeLongByReference countPtr = new NativeLongByReference();

        NativeLongByReference findListPtr = new NativeLongByReference();

        final int descrLen = 256;
        ByteBuffer descr = ByteBuffer.allocate(descrLen);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindrsrc/
        NativeLong visaStatus = visaLib.viFindRsrc(
                new NativeLong(visaResourceManagerHandle), // ViSession sesn
                regex, // ViString expr
                findListPtr, //ViPFindList findList
                countPtr, //ViPUInt32 retcnt
                descr) //ViChar instrDesc[]
                ;

        statusObject.setStatus(visaStatus);

        long numberFound = countPtr.getValue().longValue();
        if (statusObject.visaStatusLong == VisatypeLibrary.VI_SUCCESS) {
            System.out.println("it worked, number found is " + numberFound);
            String descrStr = new String(descr.array()).trim();
            System.out.printf("!: \"%s\"\n", descrStr);
            numberFound--;
        } else {
            System.err.println("failed");
        }

        for (int i = 0; i < numberFound; i++) {
            ByteBuffer descr2 = ByteBuffer.allocate(descrLen);
            // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindnext/
            NativeLong visaStatus2 = visaLib.viFindNext(findListPtr.getValue(), descr2);
            statusObject.setStatus(visaStatus2);
            String descrStr2 = new String(descr2.array()).trim();
            System.out.printf("%d: \"%s\"\n", i, descrStr2);

        }

    }

    public void getExtendedResourceInformation(String resourceName) {

        ByteBuffer resourceNameBuf = stringToByteBuffer(resourceName);
        if (resourceNameBuf == null) {
            System.err.println("stringToByteBuffer() failed");
            return;
        }

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viparsersrcex/
        ByteBuffer alias = ByteBuffer.allocate(128);

        NativeLong visaStatus = visaLib.viParseRsrcEx(
                 new NativeLong(visaResourceManagerHandle), //ViSession sesn
                resourceNameBuf, //ViRsrc rsrcName
                new NativeLongByReference(), //ViPUInt16 intfType
                new NativeLongByReference(), //ViPUInt16 intfNum
                new NativeLongByReference(), //ViChar rsrcClass[]
                ByteBuffer.allocate(128), //ViChar expandedUnaliasedName[]
                alias //ViChar aliasIfExists[]
        );
        statusObject.setStatus(visaStatus);

        System.out.println("the alias is "+new String(alias.array()));

    }

    /**
     * This method closes an instrument session.
     *
     * @return status of the operation
     */
    public long closeInstrument() {
        NativeLong visaStatus;
        try {
            LOGGER.info("Close instrument.");
            visaStatus = visaLib.viClose(visaInstrumentHandle);
            statusObject.setStatus(visaStatus);
            if (statusObject.visaStatusLong != VisatypeLibrary.VI_SUCCESS) {
                LOGGER.severe("Could not close session.");
                return VISA_JAVA_ERROR;
            }
            return VisatypeLibrary.VI_SUCCESS;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
    }

    /**
     * This getter method returns the instrument handle.
     *
     * @see visaInstrumentHandle
     * @return the visaInstrumentHandle as long
     */
    public long getVisaInstrumentHandle() {
        return visaInstrumentHandle.longValue();
    }

    /**
     * This method sends buffer content (usually a command) to the instrument.
     *
     * @param command command or other ASCII data
     * @return status of the operation
     * @throws jvisa.JVisaException if viWrite does not succeed
     */
    public long write(String command) throws JVisaException {
        NativeLong visaStatus;
        try {
            LOGGER.info(String.format("Write command \"%s\".", command));
            ByteBuffer pBuffer = stringToByteBuffer(command);
            if (pBuffer == null) {
                return VISA_JAVA_ERROR;
            }
            long commandLength = command.length();
            NativeLongByReference returnCount = new NativeLongByReference();
            visaStatus = visaLib.viWrite(
                    visaInstrumentHandle, pBuffer, new NativeLong(commandLength), returnCount);
            statusObject.setStatus(visaStatus);
            if (statusObject.visaStatusLong != VisatypeLibrary.VI_SUCCESS) {
                LOGGER.severe(String.format("Could not write %s because statusLong is %d.", command, statusObject.visaStatusLong));
                throw new JVisaException(statusObject.getVisaStatus());
            }
            long count = returnCount.getValue().longValue();
            if (count != commandLength) {
                String error = String.format("Could only write %d instead of %d bytes.",
                        count, commandLength);
                LOGGER.severe(error);
                throw new JVisaException(error);
            }
            return VisatypeLibrary.VI_SUCCESS;
        } catch (JVisaException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new JVisaException(e.getMessage());
        }
    }

    /**
     * This method reads data from the instrument, e.g. a command response or data.
     *
     * @param response response buffer
     * @param bufferSize size of response buffer in bytes
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    protected long read(ByteBuffer response, int bufferSize) throws JVisaException {
        NativeLong visaStatus;
        try {
            LOGGER.info("Read response.");
            NativeLongByReference returnCount = new NativeLongByReference();
            visaStatus = visaLib.viRead(
                    visaInstrumentHandle, response, new NativeLong(bufferSize), returnCount);
            statusObject.setStatus(visaStatus);
            readCount = returnCount.getValue().longValue();
            if (statusObject.visaStatusLong != VisatypeLibrary.VI_SUCCESS) {
                if (readCount == 0) {
                    LOGGER.severe("Reading count is 0.");
                    return VISA_JAVA_ERROR;
                }
            }
            return VisatypeLibrary.VI_SUCCESS;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new JVisaException(VISA_JAVA_ERROR);
        }
    }

    /**
     * This method reads a string from the instrument, e.g. a command response.
     *
     * @param response response string
     * @param bufferSize size of response buffer in bytes
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    public long read(JVisaReturnString response, int bufferSize) throws JVisaException {
        long visaStatus;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            visaStatus = read(buffer, bufferSize);
            if (visaStatus == VISA_JAVA_ERROR) {
                return visaStatus;
            }
            response.returnString = new String(buffer.array(), 0, (int) readCount, responseEncoding).trim();
            LOGGER.info(response.returnString);
            return VisatypeLibrary.VI_SUCCESS;
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new JVisaException(VISA_JAVA_ERROR);
        }
    }

    /**
     * This method reads a string from the instrument, usually a command response.
     *
     * @param response response string
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    public long read(JVisaReturnString response) throws JVisaException {
        return read(response, bufferSizeDefault);
    }

    /**
     * This method reads a byte array from the instrument, usually a command response.
     *
     * @param response response byte array
     * @param bufferSize size of response buffer in bytes
     * @param expectedCount expected number of bytes in response This parameter is only used under Linux / libreVisa.
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
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

    /**
     * This method reads a byte array from the instrument, usually a command response. It is used when the expected number of bytes in the response is unknown or or a Windows VISA library is used.
     *
     * @param response response byte array
     * @param bufferSize size of response buffer in bytes
     * @return status of the operation
     * @throws jvisa.JVisaException if viRead does not succeed
     */
    public long read(JVisaReturnBytes response, int bufferSize) throws JVisaException {
        return read(response, bufferSize, 0);
    }

    /**
     * This method clears the instrument. The corresponding VISA function is not implemented in the libreVisa library.
     *
     * @return status of the operation
     */
    public long clear() {
        if (isLibreVisa) {
            return VisatypeLibrary.VI_SUCCESS;
        }
        NativeLong visaStatus;
        try {
            LOGGER.info(String.format("Calling viClear(%d).", getInstrumentHandle()));
            visaStatus = visaLib.viClear(visaInstrumentHandle);
            statusObject.setStatus(visaStatus);
            if (statusObject.visaStatusLong != VisatypeLibrary.VI_SUCCESS) {
                LOGGER.severe("Error calling viClear().");
            }
            return statusObject.visaStatusLong;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return VISA_JAVA_ERROR;
        }
    }
}
