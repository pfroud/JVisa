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

/**
 * Exception for VISA operations.
 *
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
 */
public class JVisaException extends Exception {

    public JVisaException() {
        super();
    }

    public JVisaException(String message) {
        super(message);
    }

    public JVisaException(Throwable cause) {
        super(cause);
    }

    public JVisaException(String message, Throwable cause) {
        super(message, cause);
    }

    public JVisaException(long statusCode, String cFunctionName, String errorDescription) {
        super(String.format("in %s(): %s (0x%H)", cFunctionName, errorDescription, statusCode));
    }

}
