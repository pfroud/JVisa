package jvisa.eventhandling;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * You (the programmer) must keep a reference to otherwise it will be garbage collected and the callback won't work.
 */
public class JVisaEventHandler {

    public final JVisaEventCallback CALLBACK;
    public final Pointer USER_DATA;
    public final JVisaEventType EVENT_TYPE;

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
