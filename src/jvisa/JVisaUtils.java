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
 */
/**
 * Modifications by Peter Froud, June 2018
 */
package jvisa;

import com.sun.jna.NativeLong;
import java.nio.ByteBuffer;

/**
 * Contains static utility functions.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 *
 */
public class JVisaUtils {

    /**
     * converts a Java String to a ByteBuffer / C-type string.
     *
     * @param source string to convert
     * @return Java string converted to C-type string (0 terminated)
     */
    protected static ByteBuffer stringToByteBuffer(String source) {
        ByteBuffer dest = ByteBuffer.allocate(source.length() + 1);
        dest.put(source.getBytes());
        dest.position(0);
        return dest;
    }

    /**
     * If the status code indicates an error, this method will get a human-readable message for the error code and throw a JVisaException.
     *
     * @param rm the resource manager used for this VISA session
     * @param nativeStatus the value returned by a JVisaLibrary call
     * @param cFunctionName name of the C function corresponding to the DLL call
     * @throws JVisaException if the status code means the call failed
     */
    protected static void throwForStatus(JVisaResourceManager rm, NativeLong nativeStatus, String cFunctionName) throws JVisaException {
        long statusCode = nativeStatus.longValue();
        if (statusCode != 0) {
            throw new JVisaException(statusCode, cFunctionName, rm.getStatusDescription(nativeStatus));
        }
    }

}
