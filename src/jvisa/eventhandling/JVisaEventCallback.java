package jvisa.eventhandling;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * Info about how to use the JNA Callback interface: https://github.com/java-native-access/jna/blob/master/www/CallbacksAndClosures.md https://github.com/java-native-access/jna/issues/830
 */
public interface JVisaEventCallback extends Callback {

    /**
     * Signature in NI_VISA Help:<br> {@code ViStatus _VI_FUNCH viEventHandler(ViSession vi, ViEventType eventType, ViEvent context, ViAddr userHandle)}<br>
     *
     * Definition in visa.h:<br> {@code typedef ViStatus (_VI_FUNCH _VI_PTR ViHndlr) (ViSession vi, ViEventType eventType, ViEvent event, ViAddr userHandle);}<br>
     * <br>
     *
     * @todo Needs to return ViStatus (probably as a NativeLong) somehow
     * <br>
     * @see http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/userhandleparameter/
     *
     * @param instrumentHandle equals JVisaInstrument.INSTRUMENT_HANDLE
     * @param eventType one of the JVisaLibrary field starting with {@code VI_EVENT_}
     * @param eventContext You can pass this into viGetAttribute() to get more info, but the SERVICE_REQ only defines VI_ATTR_EVENT_TYPE which we already know from the eventType parameter.
     * @param userHandle A value specified by an application that can be used for identifying handlers uniquely in a session for an event.
     *
     */
    public void invoke(NativeLong instrumentHandle, NativeLong eventType, NativeLong eventContext, Pointer userHandle);
}
