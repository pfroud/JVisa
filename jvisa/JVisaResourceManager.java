package jvisa;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;
import static jvisa.JVisa.stringToByteBuffer;

/**
 *
 * @author Peter Froud
 */
public class JVisaResourceManager {

    /**
     * What is called viSession in Visa DLL becomes Handle in JVisa so that viSession is not mistaken for a Java object. The handle gets initialized only once when this class is loaded (first time only initialization).
     */
    private final NativeLong resourceManagerHandle;
    private final JVisa jVisaInstance;
    public final JVisaInterface visaLib;


    /*
    creates a session for a default resource manager.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viopendefaultrm/
     */
    public JVisaResourceManager(JVisa jVisaInstance) throws JVisaException {
        this.jVisaInstance = jVisaInstance;
        this.visaLib = jVisaInstance.visaLib;

        NativeLongByReference pViSession = new NativeLongByReference();
        NativeLong nativeStatus = visaLib.viOpenDefaultRM(pViSession);

        JVisa.throwForStatus(nativeStatus, "viOpenDefaultRM");
        resourceManagerHandle = pViSession.getValue();
    }

    /**
     * closes the resource manager.
     *
     * http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viclose/
     *
     * @throws jvisa.JVisaException
     */
    public void close() throws JVisaException {
        NativeLong nativeStatus = visaLib.viClose(resourceManagerHandle);
        JVisa.throwForStatus(nativeStatus, "viClose");
    }

    public void getExtendedResourceInformation(String resourceName) {
        // instrument does not need to be open for this to work

        ByteBuffer resourceNameBuf = JVisa.stringToByteBuffer(resourceName);
        if (resourceNameBuf == null) {
            System.err.println("stringToByteBuffer() failed");
            return;
        }

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/viparsersrcex/
        ByteBuffer alias = ByteBuffer.allocate(128);

        NativeLong visaStatus = visaLib.viParseRsrcEx(resourceManagerHandle, //ViSession sesn
                resourceNameBuf, //ViRsrc rsrcName
                new NativeLongByReference(), //ViPUInt16 intfType
                new NativeLongByReference(), //ViPUInt16 intfNum
                new NativeLongByReference(), //ViChar rsrcClass[]
                ByteBuffer.allocate(128), //ViChar expandedUnaliasedName[]
                alias //ViChar aliasIfExists[]
        );

        System.out.println("the alias is " + new String(alias.array()));

    }

    /**
     * opens an instrument session.
     *
     * @param resourceName string that contains the instrument address and bus interface, for example TCPIP::192.168.1.106::INSTR
     * @return status of the operation
     * @throws jvisa.JVisaException
     */
    public JVisaInstrument openInstrument(String resourceName) throws JVisaException {
        NativeLongByReference instrumentHandle = new NativeLongByReference();

        ByteBuffer resourceNameNative = JVisa.stringToByteBuffer(resourceName);

        NativeLong visaStatus = visaLib.viOpen(resourceManagerHandle,
                resourceNameNative, // byte buffer for instrument string
                new NativeLong(0), // access mode (locking or not). 0:Use Visa default
                new NativeLong(0), // timeout, only when access mode equals locking
                instrumentHandle // pointer to instrument object
        );

        JVisa.throwForStatus(visaStatus, "viOpen");

        return new JVisaInstrument(this, instrumentHandle, resourceName);

    }

    public void findResources() throws JVisaException {

        // this is not a regular expression. the question mark is wildcard for a single character
        ByteBuffer searchExpression = stringToByteBuffer("?*");

        NativeLongByReference countPtr = new NativeLongByReference();

        NativeLongByReference findList = new NativeLongByReference();

        final int descrLen = 256;
        ByteBuffer descr = ByteBuffer.allocate(descrLen);

        // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindrsrc/
        NativeLong visaStatus = visaLib.viFindRsrc(
                resourceManagerHandle,
                searchExpression, findList,
                countPtr, descr);

        JVisa.throwForStatus(visaStatus, "viFindRsrc");

        long numberFound = countPtr.getValue().longValue();

        System.out.printf("Found %d VISA resources:\n", numberFound);
        System.out.println(new String(descr.array()).trim());

        for (int i = 1; i < numberFound; i++) {
            descr = ByteBuffer.allocate(descrLen);

            // http://zone.ni.com/reference/en-XX/help/370131S-01/ni-visa/vifindnext/
            visaStatus = visaLib.viFindNext(findList.getValue(), descr);

            JVisa.throwForStatus(visaStatus, "viFindNext");
            System.out.println(new String(descr.array()).trim());

        }

    }

    /**
     * converts a VISA status to a descriptive string.
     *
     * @param statusCode
     * @return status description
     */
    public String getStatusDescription(NativeLong statusCode) {

        ByteBuffer description = ByteBuffer.allocate(256);

        NativeLong errorCode = visaLib.viStatusDesc(resourceManagerHandle, statusCode, description);
        // what do if viStatusDesc returns error?

        return new String(description.array()).trim();
    }

}
