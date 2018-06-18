/**
 * @license

Copyright 2014-2018 Günter Fuchs (gfuchs@acousticmicroscopy.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
/**
 * Modifications by Peter Froud, Lumenetix Inc
 * June 2018
 */
package jvisa;

import java.util.logging.Level;
import visatype.VisatypeLibrary;


/**
 * This class provides basic Visa instrument handling.
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 * @author Peter Froud
*/
public class JVisaInstrument extends JVisa {
  /** return status of VISA functions */
  private long visaStatus = VisatypeLibrary.VI_SUCCESS;
  /** byte array containing a binary response from the instrument */
  protected byte[] binaryResponse;
  /** 
   * This method gets the visa status. 
   * @see JVisaStatus#visaStatus
   * @return status
  */
  public long getVisaStatus() {
    return visaStatus;
  }
  /** response string to a "*IDN?" command */ 
  private String instrumentId = "";
  /**
   * This method returns the instrument ID string. 
   * @see instrumentId
   * @return instrument ID
   */
  public String getId() {
    return instrumentId;
  }
  /** logger of this class */
//  private static final Logger LOGGER_INSTRUMENT = Logger.getLogger(JVisaInstrument.className);


  /**
   * This method sends a command and receives its response string.
   * Since the caller cannot know the response length in advance,
   * the size of the response buffer is hard-coded.
   * @param command string to send
   * @param response string received
   * @param bufferSize size of string (C string) buffer
   * @return status of the operation
   * @throws jvisa.JVisaException if viWrite or viRead does not succeed
   */
  public long sendAndReceive(String command, JVisaReturnString response, int bufferSize) 
          throws JVisaException {
    visaStatus = VISA_JAVA_ERROR;
    try {
      visaStatus = write(command);
      if (visaStatus != VisatypeLibrary.VI_SUCCESS) {
        return visaStatus;
      }
      return read(response, bufferSize);
    }
    catch (JVisaException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new JVisaException(statusObject.getVisaStatus());
    }
  }


  /**
   * This method sends a command and receives its response string.
   * Since the caller cannot know the response length in advance,
   * the size of the response buffer is hard-coded.
   * @param command string to send
   * @param response string received
   * @return status of the operation
   * @throws jvisa.JVisaException if viWrite or viRead does not succeed
   */
  public long sendAndReceive(String command, JVisaReturnString response) 
          throws JVisaException {
    return sendAndReceive(command, response, bufferSizeDefault);
  }


  /**
   * This method sends a command and receives its response. 
   * It insists in receiving at least a given number of bytes.
   * @param command string to send
   * @param response bytes received
   * @param bufferSize size of buffer to allocate. The size can be set smaller 
   *                   since it gets allocated with readCount.
   * @param expectedCount expected number of bytes in response
   * @return status of the operation
   */
  public long sendAndReceive(String command, JVisaReturnBytes response, 
          int bufferSize, int expectedCount) {
    visaStatus = VISA_JAVA_ERROR;
    try {
      visaStatus = write(command);
      if (visaStatus != VisatypeLibrary.VI_SUCCESS) {
        return visaStatus;
      }
      return read(response, bufferSize, expectedCount);
    }
    catch (JVisaException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      return VISA_JAVA_ERROR;
    }
  }


  /**
   * This method sends a command and receives its response.
   * It receives as many bytes as the instrument is sending.
   * @param command string to send
   * @param response bytes received
   * @param bufferSize size of buffer to allocate. The size can be set smaller 
   *                   since it gets allocated with readCount.
   * @return status of the operation
   */
  public long sendAndReceive(String command, JVisaReturnBytes response, int bufferSize) {
    visaStatus = VISA_JAVA_ERROR;
    try {
      return sendAndReceive(command, response, bufferSize, 0);
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      return VISA_JAVA_ERROR;
    }
  }


  /**
   * This method reads the instrument id by sending a "*IDN?" command.
   * @param id response string
   * @return status of the operation
   */
  public long readId(JVisaReturnString id) {
    try {
      visaStatus = sendAndReceive("*IDN?", id);
      if (visaStatus == VisatypeLibrary.VI_SUCCESS) {
        instrumentId = id.returnString;
        LOGGER.info(instrumentId);
      }
      return visaStatus;
    }
    catch (JVisaException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      return VISA_JAVA_ERROR;
    }
  }
}
