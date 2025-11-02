package programmingtheiot.integration.connection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.CoapServerGateway;

public class CoapServerGatewayTest {

    private static final Logger _Logger = Logger.getLogger(CoapServerGatewayTest.class.getName());

    private CoapServerGateway csg = null;
    private IDataMessageListener dml = null;

    private static final int INIT_WAIT_MS = 2000;    // Server initialization wait
    private static final int TEST_WAIT_MS = 10000;   // Test keep-alive wait

    @Before
    public void setUp() throws Exception {
        this.dml = new DefaultDataMessageListener();
        this.csg = new CoapServerGateway(this.dml);
    }

    @After
    public void tearDown() throws Exception {
        if (this.csg != null) {
            this.csg.stopServer();
        }
    }

    @Test
    public void testRunSimpleCoapServerGatewayIntegration() {
        try {
            String url = ConfigConst.DEFAULT_COAP_PROTOCOL + "://" +
                         ConfigConst.DEFAULT_HOST + ":" + ConfigConst.DEFAULT_COAP_PORT;

            // Start server
            assertTrue("Failed to start CoAP server", this.csg.startServer());
            Thread.sleep(INIT_WAIT_MS);

            CoapClient clientConn = new CoapClient(url);

            // Resource discovery
            Set<WebLink> wlSet = clientConn.discover();
            assertNotNull("Discovery returned null", wlSet);
            assertFalse("No WebLinks discovered from CoAP server", wlSet.isEmpty());

            for (WebLink wl : wlSet) {
                _Logger.info("Discovered WebLink: " + wl.getURI() + " Attributes: " + wl.getAttributes());
            }

            // Simple GET requests
            String baseResponse = clientConn.setURI(url + "/" + ConfigConst.PRODUCT_NAME).get().getResponseText();
            assertNotNull("Base GET response is null", baseResponse);
            _Logger.info("Base GET response: " + baseResponse);

            String constrainedResponse = clientConn.setURI(url + "/" + ConfigConst.PRODUCT_NAME + "/" +
                                            ConfigConst.CONSTRAINED_DEVICE).get().getResponseText();
            assertNotNull("Constrained device GET response is null", constrainedResponse);
            _Logger.info("Constrained device GET response: " + constrainedResponse);

            String sysPerfResponse = clientConn.setURI(url + "/" +
                                          ResourceNameEnum.GDA_SYSTEM_PERF_MSG.getResourceName()).get().getResponseText();
            assertNotNull("System performance GET response is null", sysPerfResponse);
            _Logger.info("System performance GET response: " + sysPerfResponse);

            Thread.sleep(TEST_WAIT_MS);

            assertTrue("Failed to stop CoAP server", this.csg.stopServer());
            _Logger.info("CoAP Server Gateway integration test completed successfully.");

        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Exception during CoAP server integration test", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
