/**
 * Copyright 2020 Peter Froud.
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
package xyz.froud.jvisa_example;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import xyz.froud.jvisa.JVisaException;
import xyz.froud.jvisa.JVisaInstrument;
import xyz.froud.jvisa.JVisaResourceManager;
import xyz.froud.jvisa.eventhandling.JVisaEventCallback;
import xyz.froud.jvisa.eventhandling.JVisaEventHandler;
import xyz.froud.jvisa.eventhandling.JVisaEventType;


/**
 * This is a rudimentary example of how to use JVisa event handling.
 * <p>
 * To use Visa event handling, you will need to be familiar with SCPI registers. Here are two articles I found helpful:<br>
 * <a href="http://literature.cdn.keysight.com/litweb/pdf/ads2001/vsaprog/progfeat3.html">(dead link)</a><br>
 * <a href="https://www.envox.hr/eez/bench-power-supply/psu-scpi-reference-manual/psu-scpi-registers-and-queues.html">Registers and queues</a>
 * <p>
 * You will also need to know about the registers used by your instrument. Refer to your instrument's user manual or programming manual.
 * <p>
 * The project was my first exposure to instrument registers and SCPI registers. Further, I think the instrument I used to develop the JVisa event handling code does not implement event handling very well.
 * <p>
 * Consequently, I did not get event handling working reliably.
 *
 * @author Peter Froud
 */
public class EventHandlingExample {

    /*
     * Make sure you keep a strong reference to the event handler so the JVM doesn't garbage collect it!
     * See https://github.com/java-native-access/jna/issues/830
     */
    private final static JVisaEventHandler EVENT_HANDLER;

    static {
        @SuppressWarnings("Convert2Lambda")
        final JVisaEventCallback callback = new JVisaEventCallback() {
            @Override
            public void invoke(NativeLong instrumentHandle, NativeLong eventType, NativeLong eventContext, Pointer userData) {
                System.out.println("+-----------------------------------------------------------------------------------");
                System.out.println("| Event callback happened:");

                // The instrumentHandle argument equals the INSTRUMENT_HANDLE field of the JVisaInstrument on which the event handler is installed
                System.out.println("| instrumentHandle = " + instrumentHandle);

                System.out.println("|        eventType = " + JVisaEventType.parseInt(eventType.intValue()));

                /*
                 * The eventContext argument can be used to get details about the event, by passing it as the first argument to viGetAttribute().
                 * To see which events define what attributes, scroll to the bottom of http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/supportedevents/.
                 * This example uses the SERVICE_REQ event type, which does not define any interesting attributes.
                 */
                final String userDataStr;
                if (userData == Pointer.NULL) {
                    userDataStr = "null";
                } else {
                    userDataStr = "\"" + userData.getString(0) + "\"";
                }
                System.out.println("|         userData = " + userDataStr);
                System.out.println("+-----------------------------------------------------------------------------------");
            }
        };

        /*
         * When installing an event handler, you can supply arbitrary data which will be made available as an argument in the callback.
         * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/userhandleparameter/
         *
         * The userData is a String currently, but you could easily change it to be any Java primitive type:
         * In JVisaEventHandler, call a different set() method on USER_DATA.
         * In the JVisaEventCallback implementation, call a different get() method on userData.
         * https://java-native-access.github.io/jna/4.5.0/javadoc/com/sun/jna/Memory.html
         * https://java-native-access.github.io/jna/4.5.0/javadoc/com/sun/jna/Pointer.html
         *
         * I don't know how to send an Object or C struct.
         */
        final String userData = "Hello, world! This is userData.";

        EVENT_HANDLER = new JVisaEventHandler(JVisaEventType.SERVICE_REQ, callback, userData);
    }

    public static void main(String[] args) throws JVisaException {
        final JVisaResourceManager rm = new JVisaResourceManager();

        // Open the first instrument found
        final JVisaInstrument instr = rm.openInstrument(rm.findResources()[0]);

        // Make sure you keep a strong reference to the event handler so the JVM doesn't garbage collect it!
        // See https://github.com/java-native-access/jna/issues/830
        instr.addEventHandler(EVENT_HANDLER);

        // Event handler must be added before enabling the event type
        instr.enableEvent(JVisaEventType.SERVICE_REQ);

        /*
         * To trigger a service request event, we need to turn on some register bits.
         * Recommended reading:
         * http://literature.cdn.keysight.com/litweb/pdf/ads2001/vsaprog/progfeat3.html
         * https://www.envox.hr/eez/bench-power-supply/psu-scpi-reference-manual/psu-scpi-registers-and-queues.html
         *
         * What bits to turn on will depend on your instrument and what you want to do. Refer to your instrument's manual.
         */
        // Turn on a bit in the Service Request Enable register to
        // enable events from the Questionable Status register.
        instr.write("*SRE " + (1 << 3));

        // Turn on a bit in the Questionable Status Enable register.
        // For the instrument I'm using, the bit is for constant current mode.
        instr.write("status:questionable:enable 1");

        // Turn voltage down for safety
        instr.write("voltage 1V");

        // Do something which will trigger the event. You'll need to change this step for your instrument.
        instr.write("output:state on");
        instr.write("output:state off");

        instr.queryString("*SRE?");

        // Uninstalling the event handler also disabled the event if no handlers remain
        instr.disableEvent(JVisaEventType.SERVICE_REQ);
        instr.removeEventHandler(EVENT_HANDLER);
        instr.close();
        rm.close();

    }

}
