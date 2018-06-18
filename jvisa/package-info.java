/**
<h1>JVisa - Java VISA Driver</h1>
<h2>Copyright 2014-2018 Günter Fuchs gfuchs@acousticmicroscopy.com.</h2>
<p>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     <a href="http://www.apache.org/licenses/LICENSE-2.0">this link.</a>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</p>

<h2>Projects</h2>
<p>
    There are two <a href="http://www.netbeans.org">NetBeans 8</a> projects, 
    JVisa and JVisaOscilloscope, which compile into libraries in the 
    "dist" folder, JVisa.jar and JVisaOscilloscope.jar respectively. Use
    JVisaOscilloscope for VISA compliant oscilloscopes and JVisa for all
    other VISA compliant instruments. JNA libraries are archived in 
    "dist/lib/JnaVisa64_13.jar". Under Windows, you have to download a VISA DLL 
    into a classpath folder since I cannot distribute it here for 
    copyright reasons. I got mine from 
    <a href="http://www.tektronix.com">Tektronix</a>. Under Linux, you have to
    download and install libvisa.so.
</p>
@author Günter Fuchs
@version 0.6
@since February 12, 2018
*/
package jvisa;
