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
package xyz.froud.jvisa_example.highlevel;

import xyz.froud.jvisa.JVisaException;
import xyz.froud.jvisa.JVisaInstrument;
import xyz.froud.jvisa.JVisaResourceManager;

/**
 * High-level abstraction around JVisaInstrument.
 * <p>
 * This class has object-oriented ways to open instruments, built-in error-checking, and a few pre-defined VISA calls.
 * <p>
 * To use it, make a class extending AbstractInstrument and add commands from your instrument's programming manual. See PowerSupplyExample for an example.
 *
 * @author Peter Froud
 */
public abstract class AbstractInstrument {

    // Change this to private when you're done experimenting
    public final JVisaInstrument JVISA_INSTRUMENT;

    /**
     * Try to open an instrument from a VISA resource name.
     *
     * @param rm the Visa resource manager to use
     * @param visaResourceName name of the instrument, like "USB0::0x1234::0x1234::012345789::INSTR"
     * @throws InstrumentException if the Instrument couldn't be opened
     */
    public AbstractInstrument(JVisaResourceManager rm, String visaResourceName) throws InstrumentException {
        try {
            JVISA_INSTRUMENT = rm.openInstrument(visaResourceName);
        } catch (JVisaException ex) {
            throw new InstrumentException(ex);
        }
        queryErrorState();
    }

    /**
     * Constructor to wrap around a JVisaInstrument that has already been opened
     *
     * @param alreadyOpenInstrument an instrument that has already been opened
     */
    public AbstractInstrument(JVisaInstrument alreadyOpenInstrument) {
        JVISA_INSTRUMENT = alreadyOpenInstrument;
//        queryErrorState();
    }

    /**
     * Ends the Visa session with the instrument.
     *
     * @throws InstrumentException if the instrument couldn't be closed
     */
    public void close() throws InstrumentException {
        try {
            JVISA_INSTRUMENT.close();
        } catch (JVisaException ex) {
            throw new InstrumentException(ex);
        }
    }

    ////////////////////////////////////// set //////////////////////////////////////

    /**
     * Sends a command to the instrument, then verifies the instrument accepted the command.
     *
     * @param command the command to send
     * @throws InstrumentException if the command couldn't be sent
     */
    protected void set(String command) throws InstrumentException {
        setWithoutCheckingErrorState(command);
        queryErrorState();
    }

    /**
     * Sends a command to the instrument, but doesn't do any error checking.
     * <p>
     * You might want to use this if you need the code to continue running immediately after sending a command, and checking the error state takes too long.
     *
     * @param command the command to send
     * @throws InstrumentException if the command couldn't be sent
     */
    protected void setWithoutCheckingErrorState(String command) throws InstrumentException {
        try {
            JVISA_INSTRUMENT.write(command);
        } catch (JVisaException ex) {
            throw new InstrumentException(ex);
        }
    }

    ////////////////////////////// query ///////////////////////////////

    /**
     * Writes a command to the instrument then reads the response.
     *
     * @param command the command to write
     * @return response from the instrument
     * @throws InstrumentException if the command couldn't be sent, or if the response couldn't be read
     */
    protected String queryWithoutCheckingErrorState(String command) throws InstrumentException {
        try {
            return JVISA_INSTRUMENT.sendAndReceiveString(command);
        } catch (JVisaException ex) {
            throw new InstrumentException(ex);
        }
    }

    /**
     * Writes a command to the instrument, reads the response, and verifies the instrument is happy about the whole thing
     *
     * @param command the command to write
     * @return response from the instrument
     * @throws InstrumentException if the command couldn't be sent, or if the response couldn't be read
     */
    protected String query(String command) throws InstrumentException {
        final String response = queryWithoutCheckingErrorState(command);
        queryErrorState();
        return response;
    }

    /**
     * Returns information about the instrument.
     * <p>
     * The "*IDN?" command is a standard IEEE-488 (GPIB) command.
     * <p>
     * The format of the returned string is "manufacturer,modelNumber,serialNumber,softwareVersion".
     *
     * @return identification string generated by the instrument
     * @throws InstrumentException if the identification query failed
     */
    protected String getIdn() throws InstrumentException {
        return query("*IDN?");
    }

    //////////////////////////////// error checking ////////////////////////////////

    /**
     * Checks SYSTEM:ERROR? and *OPC?
     *
     * @throws InstrumentException if checking for errors failed, or if the instrument says it is in an error condition
     */
    protected final void queryErrorState() throws InstrumentException {
        checkSystemError();
        checkOperationComplete();
    }

    /**
     * Queries whether the instrument for the most recent error message.
     * <p>
     * Errors are stored in a FIFO queue in the hardware device.<br>
     * The SYSTem:ERRor? command remove the most recent error from the queue and returns it.<br>
     * There may be more errors in the device's queue after calling this method.
     *
     * @throws InstrumentException if the error couldn't be read
     */
    protected void checkSystemError() throws InstrumentException {
        final String errorMessage = queryWithoutCheckingErrorState("system:error?");

        // You might need to add variants of "no error" here, depending on what your instrument returns
        if (!errorMessage.equals("0, \"No error\"")
                && !errorMessage.equals("0,\"No error\"")
                && !errorMessage.equals("+0,\"NO ERROR\"")) {
            throw new InstrumentException("command \"system:error?\" returned " + errorMessage);
        }
    }

    /**
     * Wait for all pending operations to complete before returning.
     * <p>
     * All the operations we'll be doing are quick. However, you can command an instrument to<br>
     * do stuff which could take seconds, like calibrate or auto-range or something. If you're<br>
     * only doing writes (not queries) then you can send commands as fast as the bus allows and<br>
     * fill up the instrument's command queue. The "*OPC?" query waits for everything to finish<br>
     * then responds with 1 (one).
     * <p>
     * The "*OPC?" command is a standard IEEE-488 (GPIB) command.
     *
     * @see <a href="https://www.rohde-schwarz.com/us/driver-pages/remote-control/measurements-synchronization_231248.html">Measurement Synchronization</a>
     * @see <a href="https://web.archive.org/web/20181017093514/http://www.ni.com/white-paper/4629/en/">Using Service Requests in your GPIB application</a>
     *
     * @throws InstrumentException if checking failed(?)
     */
    protected void checkOperationComplete() throws InstrumentException {
        final String opcStr = queryWithoutCheckingErrorState("*OPC?");
        if (!opcStr.equals("1")) {
            throw new InstrumentException("command \"*OPC?\" returned " + opcStr);
        }
    }

}
