package xyz.froud.jvisa.eventhandling;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * You (the programmer) must keep a reference to otherwise it will be garbage collected and the callback won't work.
 *
 * https://github.com/java-native-access/jna/blob/master/www/CallbacksAndClosures.md
 *
 * https://github.com/java-native-access/jna/issues/830
 */
public class JVisaEventHandler {

    public final JVisaEventType EVENT_TYPE;
    public final JVisaEventCallback CALLBACK;

    // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/userhandleparameter/
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
