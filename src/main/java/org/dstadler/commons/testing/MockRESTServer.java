package org.dstadler.commons.testing;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.dstadler.commons.http.NanoHTTPD;


/**
 * Simple REST WebServer that can be used to mock REST responses to client-code tests.
 *
 * Use it as follows
 *
 * <code>
 try (MockRESTServer server = new MockRESTServer()) {

     // get the actually used port to use in the client code
     server.getPort();

     // set the response that you want the server to send back
     server.setResponse("whatever you want to return");

     ..
 }
 </code>
 */
public class MockRESTServer implements Closeable {
    private static final Logger log = Logger.getLogger(MockRESTServer.class.getName());

    // The range of ports that we try to use for the listening.
    private static final int PORT_RANGE_START = 15100;
    private static final int PORT_RANGE_END = 15110;

    private NanoHTTPD httpd;
    private int port;

    /**
     * Create a mock server that responds to REST requests with the given HTTP Status Code.
     *
     * The server tries ports in the range listed above to find one that can be used. If none is usable, a IOException
     * is thrown.
     *
     * @param status The HTTP status to return, see NanoHTTPD.HTTP_...
     * @param mime The mime-type to set for the response, see NanoHTTPD.MIME_...
     * @param msg The actual message to return when the HTTP server is called.
     * @throws IOException
     *             If instantiating the Server failed.
     */
    public MockRESTServer(final String status, final String mime, final String msg) throws IOException {
        // first try to get the next free port
        port = getNextFreePort();

        httpd = new NanoHTTPD(port) {
            /**
             * Internal method to provide the response that is set.
             */
            @Override
            public Response serve(String uri, String method, Properties header, Properties params) {
                return new NanoHTTPD.Response(status, mime, msg);
            }
        };
    }

    /**
     * Create a mock server that handles REST requests by running the given Runnable and responding with the given HTTP Status Code.
     *
     * The server tries ports in the range listed above to find one that can be used. If none is usable, a IOException
     * is thrown.
     *
     * @param response A {@link Runnable} which is called whenever the HTTP server is called.
     * @param status The HTTP status to return, see NanoHTTPD.HTTP_...
     * @param mime The mime-type to set for the response, see NanoHTTPD.MIME_...
     * @param msg The actual message to return when the HTTP server is called.
     * @throws IOException If instantiating the Server failed.
     */
    public MockRESTServer(final Runnable response, final String status, final String mime, final String msg) throws IOException {
        // first try to get the next free port
        port = getNextFreePort();

        httpd = new NanoHTTPD(port) {
            /**
             * Internal method to run the provided Runnable
             */
            @Override
            public Response serve(String uri, String method, Properties header, Properties params) {
                response.run();
                return new NanoHTTPD.Response(status, mime, msg);
            }
        };
    }

    /**
     * Create a mock server that responds to REST requests via the given Callable.
     *
     * The server tries ports in the range listed above to find one that can be used. If none is usable, a IOException
     * is thrown.
     *
     * @param response A {@link Callable} which is called whenever the HTTP server is called. The returned
     *                 {@link org.dstadler.commons.http.NanoHTTPD.Response} contains the HTTP Status Code,
     *                 the mime-type and the result-text.
     * @throws IOException If instantiating the Server failed.
     */
    public MockRESTServer(final Callable<NanoHTTPD.Response> response) throws IOException {
        // first try to get the next free port
        port = getNextFreePort();

        httpd = new NanoHTTPD(port) {
            /**
             * Internal method to run the provided Runnable
             */
            @Override
            public Response serve(String uri, String method, Properties header, Properties params) {
                try {
                    return response.call();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    /**
     * Method that is used to find the next available port. It uses the two constants PORT_RANGE_START and
     * PORT_RANGE_END defined above to limit the range of ports that are tried.
     *
     * @return A port number that can be used.
     * @throws IOException
     *             If no available port is found.
     */
    private static int getNextFreePort() throws IOException {
        for (int port = PORT_RANGE_START; port < PORT_RANGE_END; port++) {
            try {
                @SuppressWarnings("resource")
                ServerSocket sock = new ServerSocket(port);
                sock.close();
                //
                return port;
            } catch (IOException e) {
                // seems to be taken, try next one
                log.warning("Port " + port + " seems to be used already, trying next one: " + e);
            }
        }

        throw new IOException("No free port found in the range of [" + PORT_RANGE_START + " - " + PORT_RANGE_END + "]");
    }

    public int getPort() {
        return port;
    }

    @Override
    public void close() throws IOException {
        httpd.stop();
    }
}
