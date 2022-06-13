package xyz.froud.jvisa.eventhandling;

import xyz.froud.jvisa.JVisaLibrary;

import java.util.HashMap;

/**
 * @author Peter Froud
 */
public enum JVisaEventType {

    IO_COMPLETION(JVisaLibrary.VI_EVENT_IO_COMPLETION),
    TRIG(JVisaLibrary.VI_EVENT_TRIG),
    SERVICE_REQ(JVisaLibrary.VI_EVENT_SERVICE_REQ),
    CLEAR(JVisaLibrary.VI_EVENT_CLEAR),
    EXCEPTION(JVisaLibrary.VI_EVENT_EXCEPTION),
    GPIB_CIC(JVisaLibrary.VI_EVENT_GPIB_CIC),
    GPIB_TALK(JVisaLibrary.VI_EVENT_GPIB_TALK),
    GPIB_LISTEN(JVisaLibrary.VI_EVENT_GPIB_LISTEN),
    VXI_VME_SYSFAIL(JVisaLibrary.VI_EVENT_VXI_VME_SYSFAIL),
    VXI_VME_SYSRESET(JVisaLibrary.VI_EVENT_VXI_VME_SYSRESET),
    VXI_SIGP(JVisaLibrary.VI_EVENT_VXI_SIGP),
    VXI_VME_INTR(JVisaLibrary.VI_EVENT_VXI_VME_INTR),
    PXI_INTR(JVisaLibrary.VI_EVENT_PXI_INTR),
    TCPIP_CONNECT(JVisaLibrary.VI_EVENT_TCPIP_CONNECT),
    USB_INTR(JVisaLibrary.VI_EVENT_USB_INTR);

    public final int VALUE;

    JVisaEventType(int value) {
        this.VALUE = value;

    }

    private static final HashMap<Integer, JVisaEventType> valueMap;

    static {
        final JVisaEventType[] allTypes = JVisaEventType.values();
        valueMap = new HashMap<>(allTypes.length, 1);
        for (JVisaEventType type : allTypes) {
            valueMap.put(type.VALUE, type);
        }
    }

    public static JVisaEventType parseInt(int value) {
        return valueMap.get(value);
    }

}
