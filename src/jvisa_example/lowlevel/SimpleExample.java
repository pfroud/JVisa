/**
 * @license
 *
 * Copyright 2018-2020 Peter Froud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jvisa_example.lowlevel;

import jvisa.JVisaException;
import jvisa.JVisaInstrument;
import jvisa.JVisaResourceManager;

/**
 * Example showing how to use JVisaResourceManager and JVisaInstrument.
 *
 * Here's what it does:<br>
 * (1) Opens the default resource manager<br>
 * (2) Searches for USB VISA instruments<br>
 * (3) If any are instruments are found, it does this for each instrument:<br>
 * A. Opens the instrument<br>
 * B. Sends the *IDN? command and prints the response<br>
 * C. Closes the instrument<br>
 * (4) Closes the resource manager
 *
 * This is a low-level example. To see how you could use higher-level classes, look at HighLevelExample.java.
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
