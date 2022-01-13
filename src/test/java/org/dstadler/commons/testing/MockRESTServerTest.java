package org.dstadler.commons.testing;

import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.net.UrlUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.Assert.*;


/**
 * Just verify that the host where the tests are run does what we
 * expect it to in respect to hostname resolution/DNS/...
 *
 * @author cwat-dstadler
 */
@SuppressWarnings("Convert2Lambda")     // should still compile with Java 7
public class MockRESTServerTest {
    private static final Logger log = LoggerFactory.make();

    @After
    public void tearDown() throws InterruptedException {
        ThreadTestHelper.waitForThreadToFinishSubstring("NanoHTTP");
    }

    @Test
    public void testLocalhost() throws Exception {
        runWithHostname("localhost");
    }

    @Test
    public void testLocalhostIP() throws Exception {
        runWithHostname("127.0.0.1");
    }

    @Test
    public void testIP() throws Exception {
        InetAddress localHost = java.net.InetAddress.getLocalHost();
        assertNotNull("Should get a local address", localHost);
        String ipAddress = localHost.getHostAddress();

        log.info("Had hostname: " + ipAddress + ", address-info: " + localHost);

        assertNotNull("Should get a local ip-address", ipAddress);
        assertNotEquals("Local ip-address should not equal localhost", "localhost", ipAddress);

        // Travis-CI reports 127.0.0.1 for some reason
        Assume.assumeFalse("Travis-CI reports an unexpected ipAddress",
                "true".equals(System.getenv("TRAVIS")) && "127.0.0.1".equals(ipAddress));
        // Github Actions reports an inaccessible hostname
        Assume.assumeFalse("Github Actions report an unexpected ipAddress",
                "true".equals(System.getenv("GITHUB_ACTIONS")));

        // cannot assert on startsWith("127.0.0") as e.g. lab13 reports an ip-address of 127.0.0.2
        assertNotEquals("Local ip-address should not equal 127.0.0.1", "127.0.0.1", ipAddress);

        runWithHostname(ipAddress);
    }

    @Test
    public void testHostname() throws Exception {
        assertNotNull(java.net.InetAddress.getLocalHost());
        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        assertNotNull(hostname);

        // Travis-CI reports 127.0.0.1 for some reason
        Assume.assumeFalse("Travis-CI reports an unexpected hostname",
                "true".equals(System.getenv("TRAVIS")) && "localhost".equals(hostname));
		// Github Actions reports an inaccessible hostname
		Assume.assumeFalse("Github Actions report an unexpected ipAddress",
				"true".equals(System.getenv("GITHUB_ACTIONS")));

        assertNotEquals("Local hostname should not equal localhost", "localhost", hostname);
        assertFalse("Local hostname should not start with 127.0.0", hostname.startsWith("127.0.0"));

        runWithHostname(hostname);
    }

    @Test
    public void testCanonicalHostname() throws Exception {
        assertNotNull(java.net.InetAddress.getLocalHost());
        String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        assertNotNull(hostname);

        // Travis-CI reports 127.0.0.1 for some reason
        Assume.assumeFalse("Travis-CI reports an unexpected hostname",
                "true".equals(System.getenv("TRAVIS")) && "localhost".equals(hostname));
		// Github Actions reports an inaccessible hostname
		Assume.assumeFalse("Github Actions report an unexpected ipAddress",
				"true".equals(System.getenv("GITHUB_ACTIONS")));

        assertNotEquals("localhost", hostname);
        assertFalse(hostname.startsWith("127.0.0"));

        runWithHostname(hostname);
    }

    @Ignore("Fails in some environments")
    @Test
    public void testAllNetworkInterfaces() throws Exception {
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = interfaces.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                assertNotNull(inetAddress);
                String hostname = inetAddress.getCanonicalHostName();
                assertNotNull(hostname);
                assertNotEquals("Had: " + hostname, "localhost", hostname);
                assertFalse("Had: " + hostname, hostname.startsWith("127.0.0"));

                // UrlUtils does not support IPv6 yet
                if(inetAddress instanceof Inet6Address) {
                    continue;
                }

                runWithHostname(hostname);
            }
        }
    }

    private void runWithHostname(String hostname) throws IOException {
        if(hostname.startsWith("169.254")) {
            // don't try to contact this IP-range, it is usually used for VirtualBox network interfaces that might be unavailable
            return;
        }

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK,  NanoHTTPD.MIME_PLAINTEXT, "OK")) {
            final String url = "http://" + hostname + ":" + server.getPort();
            boolean check = UrlUtils.isAvailable(url, false, 500);
            assertTrue("Expect URL to be available. " + url + ": Had: " + check + ", on Windows this might indicate that a VirtualBox related network interface is enabled",
                    check);

            check = UrlUtils.isAvailable(url, true, 500);
            assertTrue("Expect URL to be available. " + url + ": Had: " + check, check);

            String checkStr = UrlUtils.retrieveData(url, 500);
            assertTrue("Expect URL to be available. " + url + ": Had: " + checkStr, checkStr.length() > 0);

            String data = UrlUtils.retrieveData(url, 500);
            assertEquals("Expect URL to return 'OK'. " + url + ": Had: " + data, "OK", data);
        }
    }

    @Test
    public void testStartupTwice() throws IOException {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>1</html>")) {
            try (MockRESTServer server2 = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>2</html>")) {
                assertTrue(server.getPort() != server2.getPort());

                String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
                assertEquals("<html>1</html>", data);

                data = UrlUtils.retrieveData("http://localhost:" + server2.getPort(), 10_000);
                assertEquals("<html>2</html>", data);
            }
        }
    }

    @Test
    public void testExhaustPorts() {
        MockRESTServer[] servers = new MockRESTServer[100];
        try {
            for (int i = 0; i < 100; i++) {
                servers[i] = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>" + i + "</html>");
            }
        } catch (IOException e) {
            TestHelpers.assertContains(e, "No free port found");
        }

        for(int i = 0;i < 100;i++) {
            if(servers[i] != null) {
                servers[i].close();
            }
        }
    }

    @Test
    public void testWithRunnable() throws IOException {
        final AtomicBoolean called = new AtomicBoolean();
        try (MockRESTServer server = new MockRESTServer(new Runnable() {
            @Override
            public void run() {
                assertFalse("Should be called exactly once, but was already called before", called.get());
                called.set(true);
            }
        }, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>1</html>")) {
            String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
            assertEquals("<html>1</html>", data);
        }

        assertTrue("Should be called now", called.get());
    }

    @Test
    public void testWithCallable() throws IOException {
        final AtomicBoolean called = new AtomicBoolean();
        try (MockRESTServer server = new MockRESTServer(new Callable<NanoHTTPD.Response>() {
            @Override
            public NanoHTTPD.Response call() {
                assertFalse("Should be called exactly once, but was already called before", called.get());
                called.set(true);
                return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>1</html>");
            }
        })) {
            String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
            assertEquals("<html>1</html>", data);
        }

        assertTrue("Should be called now", called.get());
    }

    @Test
    public void testWithCallableException() throws IOException {
        final AtomicBoolean called = new AtomicBoolean();
        try (MockRESTServer server = new MockRESTServer(new Callable<NanoHTTPD.Response>() {
            @Override
            public NanoHTTPD.Response call() {
                assertFalse("Should be called exactly once, but was already called before", called.get());
                called.set(true);
                throw new RuntimeException("TestException");
            }
        })) {
            try {
                UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
                fail("Should catch HTTP 500 here because of the exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "Error 500");
            }
        }

        assertTrue("Should be called now", called.get());
    }
}
