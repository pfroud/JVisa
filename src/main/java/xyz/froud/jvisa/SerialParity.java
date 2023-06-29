package xyz.froud.jvisa;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Peter Froud
 * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_parity.html">VI_ATTR_ASRL_PARITY</a>
 */
public enum SerialParity {

    NONE(JVisaLibrary.VI_ASRL_PAR_NONE),
    ODD(JVisaLibrary.VI_ASRL_PAR_ODD),
    EVEN(JVisaLibrary.VI_ASRL_PAR_EVEN),
    MARK(JVisaLibrary.VI_ASRL_PAR_MARK),
    SPACE(JVisaLibrary.VI_ASRL_PAR_SPACE);

    public final int VALUE;

    SerialParity(int value) {
        this.VALUE = value;
    }

    private static final Map<Integer, SerialParity> VALUE_MAP
            = Stream.of(SerialParity.values())
                    .collect(Collectors.toMap(e -> e.VALUE, e -> e));

    public static SerialParity parseInt(int value) {
        return VALUE_MAP.get(value);
    }

}
