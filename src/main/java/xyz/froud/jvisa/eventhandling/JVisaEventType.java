/**
 * @license Copyright 2018-2020 Peter Froud
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.froud.jvisa.eventhandling;

import xyz.froud.jvisa.JVisaLibrary;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Peter Froud
 * @see <a href="https://www.ni.com/docs/en-US/bundle/ni-visa/page/ni-visa/supportedevents.html">Supported Events</a>
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

    private static final Map<Integer, JVisaEventType> VALUE_MAP
            = Stream.of(JVisaEventType.values())
                    .collect(Collectors.toMap(e -> e.VALUE, e -> e));

    public static JVisaEventType parseInt(int value) {
        return VALUE_MAP.get(value);
    }

}
