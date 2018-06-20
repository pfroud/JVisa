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

import com.sun.jna.NativeLong;
import java.nio.ByteBuffer;

import visatype.VisatypeLibrary;

/**
 * Contains static utility functions.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud (pfroud@lumenetix.com)
 *
 */
public class JVisaUtils {

    //<editor-fold defaultstate="collapsed" desc="stuff I'm not implementing">
    /**
     * gets an attribute of type byte (native ViUInt8).
     *
     * @param attribute which attribute to get
     * @param value contains an attribute
     * @param sessionHandle handle of resource manager or instrument
     * @return status of the operation
     */
    /*
    public long getAttribute(int attribute, JVisaReturnNumber value, long sessionHandle) {
    try {
    NativeLong visaStatus;
    String formatString = "Attribute value = 0x";
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
    } catch (Exception e) {
    return VISA_JAVA_ERROR;
    }
    return statusObject.visaStatusLong;
    }
     */
    /**
     * gets an attribute of type String (native ViPChar).
     *
     * @param attribute which attribute to get
     * @param value contains an attribute of type String
     * @param sessionHandle handle of resource manager or instrument
     * @return status of the operation
     */
    /*
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
     */
    /**
     * sets an attribute.
     *
     * @param attribute which attribute to get
     * @param value contains an attribute
     * @param sessionHandle handle of resource manager or instrument
     * @return status of the operation
     */
    /*
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
     */
    /**
     * sets an attribute for a resource manager.
     *
     * @param attribute which attribute to get
     * @param value contains an attribute
     * @return status of the operation
     */
    /*
    public long setAttribute(int attribute, int value) {
    return setAttribute(attribute, value, visaResourceManagerHandle);
    }
     */
    /**
     * gets the version of the resource (for example the version of tkVisa64.dll).
     *
     * @param version resource version number
     * @return status of the operation
     */
    /*
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
     */
    /**
     * flushes one or more buffers.
     *
     * @param bufferType mask indicating which buffers to flush
     * @return status of the operation
     * @todo Does not work, at least not with libreVisa.
     */
    /*
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
     */
    //</editor-fold>
    /**
     * converts a Java String to a ByteBuffer / C-type string.
     *
     * @param source string to convert
     * @return Java string converted to C-type string (0 terminated)
     */
    public static ByteBuffer stringToByteBuffer(String source) {
        ByteBuffer dest = ByteBuffer.allocate(source.length() + 1);
        dest.put(source.getBytes());
        dest.position(0);
        return dest;
    }


    /**
     *
     * @param nativeStatus
     * @param cFunctionName
     * @throws JVisaException
     */
    public static void throwForStatus(NativeLong nativeStatus, String cFunctionName) throws JVisaException {
        long statusCode = nativeStatus.longValue();
        if (statusCode != VisatypeLibrary.VI_SUCCESS) {
            //TODO need to get status description here somehow, but that needs the resource manager
            throw new JVisaException(statusCode, cFunctionName);
        }
    }

}
