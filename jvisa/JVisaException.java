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

/**
 * exception class for VISA exceptions
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 */
public class JVisaException extends Exception {
  String message = "";
  long status;


  /**
   * default constructor
   */ 
  JVisaException() {
  }


  /**
   * constructor with String parameter
   * @param message holds the exception string
   */
  public JVisaException(String message) {
    this.message = message;
  }


  /**
   * constructor with status parameter
   * @param status VISA status with values from the DLL
   */
  public JVisaException(long status) {
    this.status = status;
  }


  /**
   * overrides with its own message
   * @return exception string
   */
  @Override
  public String getMessage() {
    return message;
  }
}
