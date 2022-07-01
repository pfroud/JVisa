/**
 * @license Copyright 2018-2020 Peter Froud
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
 */
package xyz.froud.jvisa.eventhandling;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * You (the programmer) must keep a reference to otherwise it will be garbage collected and the callback won't work.
 *
 * @see <a href="https://github.com/java-native-access/jna/blob/master/www/CallbacksAndClosures.md">Callbacks, Function Pointers and Closures</a>
 * @see <a href="https://github.com/java-native-access/jna/issues/830">How to receive callback from jna?</a>
 */
public class JVisaEventHandler {

    public final JVisaEventType EVENT_TYPE;
    public final JVisaEventCallback CALLBACK;

    /**
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/userhandleparameter.html">The userHandle Parameter</a>
     */
    public final Pointer USER_DATA;

    public JVisaEventHandler(JVisaEventType eventType, JVisaEventCallback callback, String userData) {
        EVENT_TYPE = eventType;
        CALLBACK = callback;
        USER_DATA = new Memory(userData.length() + 1);
        USER_DATA.setString(0, userData);
    }

    public JVisaEventHandler(JVisaEventType eventType, JVisaEventCallback callback) {
        EVENT_TYPE = eventType;
        CALLBACK = callback;
        USER_DATA = Pointer.NULL;
    }

}
