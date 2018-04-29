package org.dstadler.commons.testing;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class HeapDump {
    // This is the name of the HotSpot Diagnostic MBean
    private static final String HOTSPOT_BEAN_NAME =
            "com.sun.management:type=HotSpotDiagnostic";

    // field to store the hotspot diagnostic MBean
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;

    /**
     * Call this method from your application whenever you
     * want to dump the heap snapshot into a file.
     *
     * @param fileName name of the heap dump file
     * @param live flag that tells whether to dump
     *             only the live objects
     * @throws IOException If accessing the MBeanServer fails or
     *          creation of the heap dump is not successful
     */
    public static void dumpHeap(String fileName, boolean live) throws IOException {
        // initialize hotspot diagnostic MBean
        initHotspotMBean();
        hotspotMBean.dumpHeap(fileName, live);
    }

    // initialize the hotspot diagnostic MBean field
    private static void initHotspotMBean() throws IOException {
        if (hotspotMBean == null) {
            synchronized (HeapDump.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    // get the hotspot diagnostic MBean from the
    // platform MBean server
    private static HotSpotDiagnosticMXBean getHotspotMBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
                        HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
    }
}
