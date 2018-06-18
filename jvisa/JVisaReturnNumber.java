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
 * This class creates a mutable object for a variable of type long so that a 
 * function from this package can return it.
 * @author Günter Fuchs (gfuchs@acousticmicroscopy.com)
 */
public class JVisaReturnNumber {
  /** wrapped immutable type */
  public Number returnNumber;
  /**
   * constructor
   * @param value number to be stored so that it can be returned by reference 
   */
  public JVisaReturnNumber(Number value) {
    returnNumber = value;
  }
}
