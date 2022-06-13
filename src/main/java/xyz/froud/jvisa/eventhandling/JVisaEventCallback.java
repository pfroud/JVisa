package xyz.froud.jvisa.eventhandling;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 *
 * @see <a href="https://github.com/java-native-access/jna/blob/master/www/CallbacksAndClosures.md">Callbacks, Function Pointers and Closures</a><br>
 * @see <a href="https://github.com/java-native-access/jna/issues/830">How to receive callback from jna?</a>
 */
public interface JVisaEventCallback extends Callback {

    /**
     * Original signature: {@code ViStatus viEventHandler(ViSession vi, ViEventType eventType, ViEvent context, ViAddr userHandle)}
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/queuingcallbackmechanismsamplecode.html">Queuing and Callback Mechanism Sample Code</a>
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vieventhandler.html">viEventHandler</a>
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/supportedevents.html">Supported Events</a>
     *
     * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/userhandleparameter.html">The userHandle Parameter</a>
     *
     * @TODO Needs to return ViStatus (probably as a NativeLong) somehow
     *
     * @param instrumentHandle equals the JVisaInstrument.INSTRUMENT_HANDLE that the event handler was installed on
     *
     * @param eventType one of the JVisaLibrary field starting with {@code VI_EVENT_}
     *
     * @param eventContext You can pass this into viGetAttribute() to get more info.
     *
     * @param userHandle A value specified by an application that can be used for identifying handlers uniquely in a session for an event.
     *
     */
    public void invoke(NativeLong instrumentHandle, NativeLong eventType, NativeLong eventContext, Pointer userHandle);
}
