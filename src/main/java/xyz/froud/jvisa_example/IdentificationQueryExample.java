/**
 * @license Copyright 2014-2018 Günter (gfuchs@acousticmicroscopy.com)
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
 * @version 0.6
 * <p>
 * Modifications by Peter Froud, June 2018
 */
package xyz.froud.jvisa_example;

import xyz.froud.jvisa.JVisaException;
import xyz.froud.jvisa.JVisaInstrument;
import xyz.froud.jvisa.JVisaResourceManager;

/**
 * <p>
 * Example showing how to search for instruments and send a query.</p>
 *
 * <p>
 * Here's what it does:</p>
 *
 * <ol>
 *     <li>Opens the default resource manager</li>
 *     <li>Searches for VISA instruments</li>
 *     <li>If any are instruments are found, it does this for each instrument:</li>
 *         <ol>
 *             <li>Opens the instrument</li>
 *             <li>Sends the *IDN? command and prints the response</li>
 *             <li>Closes the instrument</li>
 *         </ol>
 *     <li>Closes the resource manager</li>
 * </ol>
 *
 * <p>
 * This is a low-level example. To see how you could use higher-level classes, look at HighLevelExample.java.</p>
 *
 * @author Peter Froud
 */
public class IdentificationQueryExample {

    public static void main(String[] args) throws JVisaException {
        try (JVisaResourceManager resourceManager = new JVisaResourceManager()) {
            final String[] resourceNames = resourceManager.findResources();
            if (resourceNames.length == 0) {
                System.out.println("No VISA resources found.");
            } else {
                System.out.printf("Found %d VISA resource(s).\n", resourceNames.length);
                for (int i = 0; i < resourceNames.length; i++) {
                    System.out.printf("\nOpening resource %d / %d.\n", i + 1, resourceNames.length);
                    queryIdentification(resourceManager, resourceNames[i]);
                }
            }
        }
    }

    private static void queryIdentification(JVisaResourceManager resourceManager, String resourceName) {
        System.out.printf("   Resource name: \"%s\"\n", resourceName);

        final JVisaInstrument instrument;
        try {
            instrument = resourceManager.openInstrument(resourceName);
        } catch (JVisaException ex) {
            System.out.println("   Failed to open the resource: " + ex);
            return;
        }

        try {
            System.out.printf("   Identification query returned: \"%s\"\n", instrument.queryString("*IDN?"));
        } catch (JVisaException ex) {
            System.out.println("   Identification query failed: " + ex);
            return;
        }

        try {
            instrument.close();
        } catch (JVisaException ex) {
            System.out.println("   Failed to close the resource: " + ex);
        }

    }

}
