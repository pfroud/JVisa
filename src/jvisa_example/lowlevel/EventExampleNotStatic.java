/*
 * Copyright 2020 Peter Froud.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jvisa_example.lowlevel;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import jvisa.JVisaException;
import jvisa.JVisaInstrument;
import jvisa.JVisaResourceManager;
import jvisa.eventhandling.JVisaEventCallback;
import jvisa.eventhandling.JVisaEventHandler;
import jvisa.eventhandling.JVisaEventType;

/**
 *
 * @author Peter Froud
 */
public class EventExampleNotStatic {

    /*
     * Make sure you keep a strong reference to the event handler so the JVM doesn't getbage collect it!
     * See https://github.com/java-native-access/jna/issues/830
     */
    private final JVisaEventHandler EVENT_HANDLER;

    public EventExampleNotStatic() {
        @SuppressWarnings("Convert2Lambda")
        JVisaEventCallback callback = new JVisaEventCallback() {
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
         * Currently the userData is a String, but you could easily change it to be any Java primitive type:
         * In JVisaEventHandler, call a different set() method on USER_DATA.
         * In the JVisaEventCallback implementation, call a different get() method on userData.
         * https://java-native-access.github.io/jna/4.5.0/javadoc/com/sun/jna/Memory.html
         * https://java-native-access.github.io/jna/4.5.0/javadoc/com/sun/jna/Pointer.html
         *
         * I don't know how to send a an Object or C struct.
         */
        final String userData = "Hello, world! This is userData.";

        EVENT_HANDLER = new JVisaEventHandler(JVisaEventType.SERVICE_REQ, callback, userData);
    }

    public void run() {
        try {
            JVisaResourceManager rm = new JVisaResourceManager();

            // Put your instrument's resource name here
            JVisaInstrument instr = rm.openInstrument("USB0::0xFFFF::0x9200::802243020746910064::INSTR");
            Thread.sleep(100);

            instr.write("voltage 1V");
            instr.sendAndReceiveString("*STB?");
            Thread.sleep(100);
//            fakeRefreshTables(instr);
            Thread.sleep(100);

            instr.addEventHandler(EVENT_HANDLER);
            Thread.sleep(100);
            instr.enableEvent(JVisaEventType.SERVICE_REQ);
            Thread.sleep(100);

            instr.write("output:state on");
            Thread.sleep(100);
//            fakeRefreshTables(instr);
            Thread.sleep(100);
            instr.write("output:state off");
            Thread.sleep(100);
//            fakeRefreshTables(instr);
            Thread.sleep(100);
            instr.sendAndReceiveString("*SRE?");
            Thread.sleep(100);
//            fakeRefreshTables(instr);
            Thread.sleep(100);
            instr.removeEventHandler(EVENT_HANDLER);
            Thread.sleep(100);
            instr.close();
            rm.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fakeRefreshTables(JVisaInstrument instr) throws JVisaException {
//        instr.sendAndReceiveString("status:questionable:condition?");
//        instr.sendAndReceiveString("status:questionable:event?");
//        instr.sendAndReceiveString("status:questionable:enable?");
        instr.sendAndReceiveString("*STB?");
//        instr.sendAndReceiveString("*SRE?");
    }

    public static void main(String[] args) {
        new EventExampleNotStatic().run();
    }

}
