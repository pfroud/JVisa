/**
 * @license Copyright 2014-2018 Günter (gfuchs@acousticmicroscopy.com)
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
 * @version 0.6
 * <p>
 * Modifications by Peter Froud, June 2018
 */
package xyz.froud.jvisa;


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
     * Converts a Java String to a ByteBuffer with a zero terminator.
     *
     * @param source string to convert
     * @return Java string converted to C-type string (0 terminated)
     */
    protected static ByteBuffer stringToByteBuffer(String source) {
        final ByteBuffer rv = ByteBuffer.allocate(source.length() + 1);
        rv.put(source.getBytes());
        rv.position(0);
        return rv;
    }

    protected static String byteBufferToString(ByteBuffer buf){
        return new String(buf.array()).trim();
    }



}
