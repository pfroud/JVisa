package jvisa.eventhandling;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * Programmer must keep a reference to this (actually only the JVisaEventCallback) otherwise it will be garbage collected
 */
public class JVisaEventHandle {

    public final JVisaEventCallback CALLBACK;
    public final Memory USER_DATA;
    public final Pointer POINTER_TO_USER_DATA;

    public JVisaEventHandle(JVisaEventCallback callback, Memory userData) {
        CALLBACK = callback;
        USER_DATA = userData;
        POINTER_TO_USER_DATA = USER_DATA.getPointer(0);
    }

}
