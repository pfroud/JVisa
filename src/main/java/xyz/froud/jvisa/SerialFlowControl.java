package xyz.froud.jvisa;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Peter Froud
 * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_flow_cntrl.html">VI_ATTR_ASRL_FLOW_CNTRL</a>
 */
public enum SerialFlowControl {

    NONE(JVisaLibrary.VI_ASRL_FLOW_NONE),
    XON_XOFF(JVisaLibrary.VI_ASRL_FLOW_XON_XOFF),
    RTS_CTS(JVisaLibrary.VI_ASRL_FLOW_RTS_CTS),
    DTR_DSR(JVisaLibrary.VI_ASRL_FLOW_DTR_DSR);

    public final int VALUE;

    SerialFlowControl(int value) {
        this.VALUE = value;
    }

    private static final Map<Integer, SerialFlowControl> VALUE_MAP
            = Stream.of(SerialFlowControl.values())
                    .collect(Collectors.toMap(e -> e.VALUE, e -> e));

    public static SerialFlowControl parseInt(int value) {
        return VALUE_MAP.get(value);
    }

}
