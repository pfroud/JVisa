package jvisa.eventhandling;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * Info about how to use the JNA Callback interface: https://github.com/java-native-access/jna/blob/master/www/CallbacksAndClosures.md https://github.com/java-native-access/jna/issues/830
 */
public interface JVisaEventCallback extends Callback {

    /**
     * Original signature: {@code ViStatus viEventHandler(ViSession vi, ViEventType eventType, ViEvent context, ViAddr userHandle)}
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/queuingcallbackmechanismsamplecode/
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vieventhandler/
     *
     * @todo Needs to return ViStatus (probably as a NativeLong) somehow
     *
     * @param instrumentHandle equals the JVisaInstrument.INSTRUMENT_HANDLE that the event handler was installed on
     *
     * @param eventType one of the JVisaLibrary field starting with {@code VI_EVENT_}
     *
     * @param eventContext You can pass this into viGetAttribute() to get more info. http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/supportedevents/
     *
     * @param userHandle A value specified by an application that can be used for identifying handlers uniquely in a session for an event. http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/userhandleparameter/
     *
     */
    public void invoke(NativeLong instrumentHandle, NativeLong eventType, NativeLong eventContext, Pointer userHandle);
}
