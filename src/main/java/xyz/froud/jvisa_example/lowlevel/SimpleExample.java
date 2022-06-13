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
package xyz.froud.jvisa_example.lowlevel;

import xyz.froud.jvisa.JVisaException;
import xyz.froud.jvisa.JVisaInstrument;
import xyz.froud.jvisa.JVisaResourceManager;

/**
 * <p>Example showing how to use JVisaResourceManager and JVisaInstrument.</p>
 *
 * <p>Here's what it does:</p>
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
 *
 * </ol>
 *
 * <p>This is a low-level example. To see how you could use higher-level classes, look at HighLevelExample.java.</p>
 *
 * @author Peter Froud
 */
public class SimpleExample {

    @SuppressWarnings("ConvertToTryWithResources")
    public static void main(String[] args) {
        JVisaResourceManager resourceManager;

        try {
            resourceManager = new JVisaResourceManager();
        } catch (JVisaException ex) {
            System.err.println("Couldn't open the default resource manager");
            ex.printStackTrace();
            return;
        } catch (UnsatisfiedLinkError err) {
            System.err.println("Couldn't load the VISA native shared library (.dll or .so or .dylib file)");
            err.printStackTrace();
            return;
        }

        final String[] resourceNames;
        try {
            resourceNames = resourceManager.findResources();
        } catch (JVisaException ex) {
            System.err.println("Couldn't find any VISA resources");
            ex.printStackTrace();
            return;
        }
        final int foundCount = resourceNames.length;
        if (foundCount < 1) {
            System.err.println("Couldn't find any VISA resources");
            return;
        }

        System.out.printf("Found %d VISA resource(s).\n", foundCount);

        for (int i = 0; i < foundCount; i++) {
            System.out.printf("\nTrying resource %d / %d\n", i + 1, foundCount);
            final String resourceName = resourceNames[i];
            System.out.printf("   Resource name: \"%s\"\n", resourceName);
            try {
                final JVisaInstrument instrument = resourceManager.openInstrument(resourceName);
                System.out.printf("   Identification query returned: \"%s\"\n", instrument.sendAndReceiveString("*IDN?"));
                instrument.close();
            } catch (JVisaException ex) {
                System.out.printf("   Failed to open the resource: %s\n", ex.getMessage());
            }

        }

        try {
            resourceManager.close();
        } catch (JVisaException ex) {
            ex.printStackTrace();
        }

    }
}
