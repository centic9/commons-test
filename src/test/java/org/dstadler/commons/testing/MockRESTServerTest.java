package org.dstadler.commons.testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.net.UrlUtils;
import org.junit.After;
import org.junit.Test;


/**
 * Just verify that the host where the tests are run does what we
 * expect it to in respect to hostname resolution/DNS/...
 *
 * @author cwat-dstadler
 */
public class MockRESTServerTest {
    private static final Logger log = LoggerFactory.make();

	@After
	public void tearDown() throws InterruptedException {
		ThreadTestHelper.waitForThreadToFinishSubstring("NanoHTTP");
	}

	@Test
	public void testLocalhost() throws IOException {
		runWithHostname("localhost");
	}

	@Test
	public void testLocalhostIP() throws IOException {
		runWithHostname("127.0.0.1");
	}

	@Test
	public void testIP() throws IOException {
		InetAddress localHost = java.net.InetAddress.getLocalHost();
		assertNotNull("Should get a local address", localHost);
		String ipaddress = localHost.getHostAddress();

		log.info("Had hostname: " + ipaddress + ", address-info: " + localHost);

		assertNotNull("Should get a local ip-address", ipaddress);
		assertFalse("Local ip-address should not equal localhost", ipaddress.equals("localhost"));
		// cannot assert on startsWith("127.0.0") as e.g. lab13 reports an ip-address of 127.0.0.2
		assertFalse("Local ip-address should not equal 127.0.0.1", ipaddress.equals("127.0.0.1"));

		runWithHostname(ipaddress);
	}

	@Test
	public void testHostname() throws IOException {
		assertNotNull(java.net.InetAddress.getLocalHost());
		String hostname = java.net.InetAddress.getLocalHost().getHostName();
		assertNotNull(hostname);
		assertFalse("Local hostname should not equal localhost", hostname.equals("localhost"));
		assertFalse("Local hostname should not start with 127.0.0", hostname.startsWith("127.0.0"));

		runWithHostname(hostname);
	}

	@Test
	public void testCanonicalHostname() throws IOException {
		assertNotNull(java.net.InetAddress.getLocalHost());
		String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		assertNotNull(hostname);
		assertFalse(hostname.equals("localhost"));
		assertFalse(hostname.startsWith("127.0.0"));

		runWithHostname(hostname);
	}

	private void runWithHostname(String hostname) throws IOException {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK,  NanoHTTPD.MIME_PLAINTEXT, "OK")) {
			boolean check = UrlUtils.isAvailable("http://" + hostname + ":" + server.getPort(), false, 500);
			assertTrue("Host: " + hostname + ":" + server.getPort() + ": Had: " + check, check);

			check = UrlUtils.isAvailable("http://" + hostname + ":" + server.getPort(), true, 500);
			assertTrue("Host: " + hostname + ":" + server.getPort() + ": Had: " + check, check);

			String checkStr = UrlUtils.retrieveData("http://" + hostname + ":" + server.getPort(), 500);
			assertTrue("Host: " + hostname + ":" + server.getPort() + ": Had: " + check, checkStr.length() > 0);

			String data = UrlUtils.retrieveData("http://" + hostname + ":" + server.getPort(), 500);
			assertEquals("Host: " + hostname + ":" + server.getPort() + ": Had: " + data, "OK", data);
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testStop() throws IOException {
		@SuppressWarnings("resource")
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK,  NanoHTTPD.MIME_PLAINTEXT, "OK");
		try {
			boolean check = UrlUtils.isAvailable("http://localhost:" + server.getPort(), false, 500);
			assertTrue("Host: localhost: Had: " + check, check);
		} finally {
			server.stop();
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
}
