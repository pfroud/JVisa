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
package jvisa_example;

import jvisa.JVisaException;
import jvisa.JVisaInstrument;
import jvisa.JVisaResourceManager;

/**
 *
 * Example showing how to use the higher-level AbstractInstrument and PowerSupplyExample classes.
 *
 * It starts the default resource manager, then searches for VISA instruments that will work with the PowerSupplyExample.<br>
 * If any are found, a few read and write commands are sent. Then the instrument and resource manager are closed.
 *
 * This is a high-level example. To see how you could use lower-level classes, look at LowLevelExample.java.
 *
 * @author Peter Froud
 */
public class HighLevelExample {

    public static void main(String[] args) {
        JVisaResourceManager resourceManager;

        try {
            resourceManager = new JVisaResourceManager();
        } catch (JVisaException ex) {
            System.err.println("Couldn't open the default resource manager");
            ex.printStackTrace();
            return;
        } catch (UnsatisfiedLinkError err) {
            System.err.println("Couldn't load nivisa.dll");
            err.printStackTrace();
            return;
        }

        try {
            PowerSupplyExample powerSupply = lookForPowerSupply(resourceManager);
            if (powerSupply == null) {
                return;
            }
            powerSupply.setVoltage(12);
            powerSupply.setCurrent(0.75);
            System.out.printf("Voltage: %.1f V\n", powerSupply.measureVoltage());
            System.out.printf("Current: %.1f A\n", powerSupply.measureCurrent());
            powerSupply.close();
        } catch (InstrumentException | JVisaException ex) {
            ex.printStackTrace();
        }

        try {
            resourceManager.close();
        } catch (JVisaException ex) {
            ex.printStackTrace();
        }

    }

    private static PowerSupplyExample lookForPowerSupply(JVisaResourceManager resourceManager) throws InstrumentException, JVisaException {

        String[] foundVisaResources = new String[0];
        try {
            foundVisaResources = resourceManager.findResources();
        } catch (JVisaException ex) {
            System.err.println("Exception in findResources()");
            ex.printStackTrace();
        }

        if (foundVisaResources.length == 0) {
            System.err.println("Zero VISA instruments found.");
        }

        for (String presentResourceName : foundVisaResources) {
            final JVisaInstrument instrument = resourceManager.openInstrument(presentResourceName);
            final String manufacturer = instrument.getManufacturerName();
            final String model = instrument.getModelName();

            if (manufacturer.equals(PowerSupplyExample.DESIRED_MANUFACTURER)
                    && PowerSupplyExample.DESIRED_MODELS.contains(model)) {
                System.out.printf("Found a power supply that will work with this example. Model %s, serial number %s\n", model, instrument.getSerialNumber());
                return new PowerSupplyExample(instrument);
            } else {
                instrument.close();
            }
        }

        System.err.printf("Looked at %d VISA device(s), but none are the correct manufacturer and model to work with this example.\n", foundVisaResources.length);
        return null;
    }

}
