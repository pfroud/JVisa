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
package jvisa;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import com.sun.jna.NativeLong;
import java.util.logging.Logger;
import java.util.logging.Level;
import visatype.VisatypeLibrary;

/**
 * This class provides routines related to ViStatus declared in visa.h.
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 */
public class JVisaStatus {
  /** VISA status as NativeLong. 
   * visatype.h defines ViStatus as ViInt32, and defines ViInt32 as signed long. */
  public NativeLong visaStatus;
  /**  VISA status as long. */
  public long visaStatusLong;
  /** description of VISA status obtained by calling viStatusDesc */
  private String visaStatusString;
  /** We need this flag because libreVisa (Linux) does not support attributes
   * except one.
   */
  private boolean isLinux;
  /**
   * This method gets the description of the VISA status returned last.
   * @return description of VISA status
   */
  public String getVisaStatus() {
      return visaStatusString;
  }
  /**
   * return value for success
   * Classes which use this class return only success or error. This way
   * such classes do not need to import VISA libraries.
   */
  public static final long VISA_JAVA_SUCCESS = VisatypeLibrary.VI_SUCCESS;
  /**
   * return value for error
   * Classes which use this class return only success or error. This way
   * such classes do not need to import VISA libraries. This value was chosen
   * so that it does not overlap with an error value defined in visa.h which are
   * all negative.
   */
  public static final long VISA_JAVA_ERROR = 0x7FFFFFFF;
  /** size of response buffer */
  int bufferSize;
  /** handle of resource manager */
  long resourceManagerHandle;
  /** encoding string */
  String encoding;
  /** instance of VISA library */
  JVisaInterface visaLib;
  /** Place this logger in the logger hierarchy below the JVisa logger. */
  private final static Logger LOGGER = Logger.getLogger(
          String.format("%s.%s", JVisa.class.getName(), 
          JVisaStatus.class.getSimpleName()));

  
  /**
   * The constructor initializes variables.
   * @param bufferSize size of receiving buffer
   * @param encoding   encoding of response buffer
   */
  public JVisaStatus(int bufferSize, String encoding) {
    init(bufferSize, encoding, null);
  }

  /**
   * This constructor initializes variables.
   * @param bufferSize size of receiving buffer
   * @param encoding   encoding of response buffer
   * @param visaLib    instance of VISA library
   */
  public JVisaStatus(int bufferSize, String encoding, JVisaInterface visaLib) {
    init(bufferSize, encoding, visaLib);
  }
  
  
  /** This method initializes this object.
   * 
   * @param bufferSize size of receiving buffer
   * @param encoding   encoding of response buffer
   * @param visaLib    instance of VISA library
   */
  protected final void init(int bufferSize, String encoding, JVisaInterface visaLib) {
    try {
      visaStatus = null;
      visaStatusLong = VisatypeLibrary.VI_SUCCESS;
      visaStatusString = "OK";
      this.bufferSize = bufferSize;
      this.encoding = encoding;
      this.visaLib = visaLib;
      isLinux = visaLib.toString().contains("libvisa");
    }
    catch(SecurityException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }


  /**
   * This method converts the VISA status from a NativeLong to a long.
   * @param visaStatus return value of VISA method
   * @return VisatypeLibrary.VI_SUCCESS or VISA_JAVA_ERROR
   */
  public long setStatus(NativeLong visaStatus) {
    try {
      this.visaStatus = visaStatus;
      visaStatusLong = visaStatus.longValue();
      printStatusDescription();
      return visaStatusLong == VisatypeLibrary.VI_SUCCESS ? visaStatusLong : VISA_JAVA_ERROR;
    }
    catch(NumberFormatException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      return VISA_JAVA_ERROR;
    }
  }


/**
 * This method converts a VISA status to a descriptive string.
 * @param visaResourceManagerHandle handle of VISA resource manager
 * @return status description
 */
  public String getStatusDescription(long visaResourceManagerHandle) {
    try {
      if (visaLib == null) {
        return "no description for VISA status available";
      }
      String statusString = String.format("VISA status 0x%08X", visaStatusLong);
      if (visaResourceManagerHandle == 0) {
        return String.format("%s: No description is available because no resource " 
            + "manager session is open.", statusString);
      }
      if (isLinux == true) {
        //SEVERE: Error looking up function 'viStatusDesc': /home/gfuchs/jdk1.8.0_65/jre/bin/java: undefined symbol: viStatusDesc
        //java.lang.UnsatisfiedLinkError: Error looking up function 'viStatusDesc': /home/gfuchs/jdk1.8.0_65/jre/bin/java: undefined symbol: viStatusDesc
        return "Function viStatusDesc is not implemented in libreVisa library";
      }
      ByteBuffer pStatusDesc = ByteBuffer.allocate(bufferSize);
      visaLib.viStatusDesc(new NativeLong(visaResourceManagerHandle), visaStatus, pStatusDesc);
      byte[] statusArray = pStatusDesc.array();
      int stringLength;
      for (stringLength = 0; stringLength < statusArray.length; stringLength++) {
        if (statusArray[stringLength] == 0)
          break;
      }
      return String.format("%s: %s", statusString, 
              new String(pStatusDesc.array(), 0, stringLength, encoding));
    }
    catch (UnsupportedEncodingException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }


  /**
  * This method converts a VISA status to a descriptive string.
  * @return status string
  */
  public String getStatusDescription() {
    return getStatusDescription(this.resourceManagerHandle);
  }

  /**
  * This method prints the Visa status in hex and if not null, also its description.
  * @param visaResourceManagerHandle handle of VISA resource manager
  */
  protected void printStatusDescription(long visaResourceManagerHandle) {
    try {
      String status = String.format("viStatus = 0x%08X", visaStatusLong);
      // Log SEVERE if there was a Visa error.
//      LOGGER.log(visaStatusLong == VisatypeLibrary.VI_SUCCESS ? Level.FINE : Level.SEVERE, status);
      if (visaResourceManagerHandle == 0 || isLinux == true) {
        return;
      }
      visaStatusString = getStatusDescription(visaResourceManagerHandle);
      if (visaStatusLong != VisatypeLibrary.VI_SUCCESS) {
        LOGGER.severe(visaStatusString);
      }
    }
    catch (Exception e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }


  /**
  * This method prints Visa status and its description.
  */
  protected void printStatusDescription() {
    printStatusDescription(this.resourceManagerHandle);
  }
}

