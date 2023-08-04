package xyz.froud.jvisa_example;

import xyz.froud.jvisa.JVisaException;
import xyz.froud.jvisa.JVisaInstrument;
import xyz.froud.jvisa.JVisaResourceManager;
import xyz.froud.jvisa.SerialFlowControl;
import xyz.froud.jvisa.SerialParity;
import xyz.froud.jvisa.SerialStopBits;

/**
 * Shows how to set serial port parameters.
 * @author Peter Froud
 */
public class SerialPortExample {

    public static void main(String[] args) throws JVisaException {
        final String portName = "COM1";
        try (JVisaResourceManager resourceManager = new JVisaResourceManager()) {
            try (JVisaInstrument instrument = resourceManager.openInstrument(portName)) {
                instrument.setSerialBaudRate(9600);
                instrument.setSerialDataBits(8);
                instrument.setSerialStopBits(SerialStopBits.ONE);
                instrument.setSerialParity(SerialParity.NONE);
                instrument.setSerialFlowControl(SerialFlowControl.NONE);
                instrument.setReadTerminationCharacter('\n');
                instrument.setWriteTerminator("\n");
                System.out.println(instrument.queryString("*IDN?"));
            }
        }
    }
}
