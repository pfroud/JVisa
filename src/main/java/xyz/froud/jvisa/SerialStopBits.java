package xyz.froud.jvisa;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stop bits for a serial port.
 *
 * @author Peter Froud
 * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/vi_attr_asrl_stop_bits.html">VI_ATTR_ASRL_STOP_BITS</a>
 */
public enum SerialStopBits {

    ONE(JVisaLibrary.VI_ASRL_STOP_ONE),
    ONE_AND_A_HALF(JVisaLibrary.VI_ASRL_STOP_ONE5),
    TWO(JVisaLibrary.VI_ASRL_STOP_TWO);

    public final int VALUE;

    SerialStopBits(int value) {
        this.VALUE = value;
    }

    private static final Map<Integer, SerialStopBits> VALUE_MAP
            = Stream.of(SerialStopBits.values())
                    .collect(Collectors.toMap(e -> e.VALUE, e -> e));

    public static SerialStopBits parseInt(int value) {
        return VALUE_MAP.get(value);
    }

}
