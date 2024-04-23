package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.net.UrlUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Just verify that the host where the tests are run does what we
 * expect it to in respect to hostname resolution/DNS/...
 */
public class MockRESTServerTest {
    private static final Logger log = LoggerFactory.make();

    @AfterEach
    void tearDown() throws InterruptedException {
        ThreadTestHelper.waitForThreadToFinishSubstring("NanoHTTP");
    }

    @Test
    void testLocalhost() throws Exception {
        runWithHostname("localhost");
    }

    @Test
    void testLocalhostIP() throws Exception {
        runWithHostname("127.0.0.1");
    }

    @Test
    void testIP() throws Exception {
        InetAddress localHost = java.net.InetAddress.getLocalHost();
        assertNotNull(localHost, "Should get a local address");
        String ipAddress = localHost.getHostAddress();

        log.info("Had hostname: " + ipAddress + ", address-info: " + localHost);

        assertNotNull(ipAddress, "Should get a local ip-address");
        assertNotEquals("localhost", ipAddress, "Local ip-address should not equal localhost");

        // Travis-CI reports 127.0.0.1 for some reason
        Assumptions.assumeFalse("true".equals(System.getenv("TRAVIS")) && "127.0.0.1".equals(ipAddress),
				"Travis-CI reports an unexpected ipAddress");
        // Github Actions reports an inaccessible hostname
        Assumptions.assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")), "Github Actions report an unexpected ipAddress");

        // cannot assert on startsWith("127.0.0") as e.g. lab13 reports an ip-address of 127.0.0.2
        assertNotEquals("127.0.0.1", ipAddress, "Local ip-address should not equal 127.0.0.1");

        runWithHostname(ipAddress);
    }

    @Test
    void testHostname() throws Exception {
        assertNotNull(InetAddress.getLocalHost());
        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        assertNotNull(hostname);

        // Travis-CI reports 127.0.0.1 for some reason
        Assumptions.assumeFalse("true".equals(System.getenv("TRAVIS")) && "localhost".equals(hostname),
				"Travis-CI reports an unexpected hostname");
		// Github Actions reports an inaccessible hostname
		Assumptions.assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")), "Github Actions report an unexpected ipAddress");

        assertNotEquals("localhost", hostname, "Local hostname should not equal localhost");
        assertFalse(hostname.startsWith("127.0.0"), "Local hostname should not start with 127.0.0");

        runWithHostname(hostname);
    }

    @Test
    void testCanonicalHostname() throws Exception {
        assertNotNull(InetAddress.getLocalHost());
        String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        assertNotNull(hostname);

        // Travis-CI reports 127.0.0.1 for some reason
        Assumptions.assumeFalse("true".equals(System.getenv("TRAVIS")) && "localhost".equals(hostname),
				"Travis-CI reports an unexpected hostname");
		// Github Actions reports an inaccessible hostname
		Assumptions.assumeFalse("true".equals(System.getenv("GITHUB_ACTIONS")), "Github Actions report an unexpected ipAddress");

        assertNotEquals("localhost", hostname);
        assertFalse(hostname.startsWith("127.0.0"));

        runWithHostname(hostname);
    }

    @Disabled("Fails in some environments")
    @Test
    void testAllNetworkInterfaces() throws Exception {
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = interfaces.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                assertNotNull(inetAddress);
                String hostname = inetAddress.getCanonicalHostName();
                assertNotNull(hostname);
                assertNotEquals("localhost", hostname, "Had: " + hostname);
                assertFalse(hostname.startsWith("127.0.0"), "Had: " + hostname);

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
            assertTrue(check,
					"Expect URL to be available. " + url + ": Had: " + check + ", on Windows this might indicate that a VirtualBox related network interface is enabled");

            check = UrlUtils.isAvailable(url, true, 500);
            assertTrue(check, "Expect URL to be available. " + url + ": Had: " + check);

            String checkStr = UrlUtils.retrieveData(url, 500);
			assertFalse(checkStr.isEmpty(), "Expect URL to be available. " + url + ": Had: " + checkStr);

            String data = UrlUtils.retrieveData(url, 500);
            assertEquals("OK", data, "Expect URL to return 'OK'. " + url + ": Had: " + data);
        }
    }

    @Test
    void testStartupTwice() throws IOException {
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
    void testExhaustPorts() {
        MockRESTServer[] servers = new MockRESTServer[100];
        try {
            for (int i = 0; i < 100; i++) {
				//noinspection resource
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
    void testWithRunnable() throws IOException {
        final AtomicBoolean called = new AtomicBoolean();
        try (MockRESTServer server = new MockRESTServer(() -> {
			assertFalse(called.get(), "Should be called exactly once, but was already called before");
			called.set(true);
		}, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>1</html>")) {
            String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
            assertEquals("<html>1</html>", data);
        }

        assertTrue(called.get(), "Should be called now");
    }

    @Test
    void testWithCallable() throws IOException {
        final AtomicBoolean called = new AtomicBoolean();
        try (MockRESTServer server = new MockRESTServer(() -> {
			assertFalse(called.get(), "Should be called exactly once, but was already called before");
			called.set(true);
			return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>1</html>");
		})) {
            String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
            assertEquals("<html>1</html>", data);
        }

        assertTrue(called.get(), "Should be called now");
    }

    @Test
    void testWithCallableException() throws IOException {
        final AtomicBoolean called = new AtomicBoolean();
        try (MockRESTServer server = new MockRESTServer(() -> {
			assertFalse(called.get(), "Should be called exactly once, but was already called before");
			called.set(true);
			throw new RuntimeException("TestException");
		})) {
            try {
                UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
                fail("Should catch HTTP 500 here because of the exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "Error 500");
            }
        }

        assertTrue(called.get(), "Should be called now");
    }

	@Test
	void exceptionWhenPortsAreExhausted() throws IOException {
		List<MockRESTServer> servers = new ArrayList<>();
		// default range is for 10 ports
		for (int i = 0; i < 10; i++) {
			servers.add(new MockRESTServer(NanoHTTPD.HTTP_OK,  NanoHTTPD.MIME_PLAINTEXT, "OK"));
		}

		//noinspection resource
		final String msg = assertThrows(IOException.class,
				() -> new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK")).getMessage();
		TestHelpers.assertContains(msg, "No free port found", "15100", "15110");

		for (int i = 0; i < 10; i++) {
			servers.get(i).close();
		}
	}
}
