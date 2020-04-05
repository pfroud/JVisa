/**
 * @license
 *
 * Copyright 2018-2020 Peter Froud
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
 */
package jvisa_example;

/**
 * The JVisaException class is from the lower abstraction level, so we don't want to throw that from our higher abstraction level.
 *
 * This exception is just so you can throw something at the same abstraction level as AbstractInstrument.
 *
 * @author Peter Froud
 */
public class InstrumentException extends Exception {

    public InstrumentException() {
        super();
    }

    public InstrumentException(String message) {
        super(message);
    }

    public InstrumentException(Throwable cause) {
        super(cause);
    }

    public InstrumentException(String message, Throwable cause) {
        super(message, cause);
    }

}
