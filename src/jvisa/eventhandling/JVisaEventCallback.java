package jvisa.eventhandling;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;

/**
 * Info about how to use the JNA Callback interface: https://github.com/java-native-access/jna/blob/master/www/CallbacksAndClosures.md https://github.com/java-native-access/jna/issues/830
 */
public interface JVisaEventCallback extends Callback {

    /**
     * Signature in NI_VISA Help:<br> {@code ViStatus _VI_FUNCH viEventHandler(ViSession vi, ViEventType eventType, ViEvent context, ViAddr userHandle)}
     *
     * Definition in visa.h:<br> {@code typedef ViStatus (_VI_FUNCH _VI_PTR ViHndlr) (ViSession vi, ViEventType eventType, ViEvent event, ViAddr userHandle);}
     *
     * @todo Needs to return ViStatus (probably as a NativeLong) somehow
     *
     * @see http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/userhandleparameter/
     *
     * @param vi Unique logical identifier to a session.
     * @param evertType Logical event identifier
     * @param event A handle specifying the unique occurrence of an event.
     * @param userHandle A value specified by an application that can be used for identifying handlers uniquely in a session for an event.
     *
     */
    public void invoke(NativeLong vi, NativeLong evertType, NativeLong event, NativeLong userHandle);
}
