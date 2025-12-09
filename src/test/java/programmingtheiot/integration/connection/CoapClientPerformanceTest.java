package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.CoapClientConnector;

/**
 * Lab 10 – CoAP Performance Test
 *
 * Tests performance of:
 *   - Confirmable (CON) messages
 *   - Non-confirmable (NON) messages
 *
 * Measures:
 *   - Round Trip Time (RTT)
 *   - Average RTT
 */
public class CoapClientPerformanceTest {

    private static final Logger _Logger = Logger.getLogger(CoapClientPerformanceTest.class.getName());

    // ---------------------------------------------------------
    // Test configuration
    // ---------------------------------------------------------
    private static final int ITERATIONS = 50;
    private static final int WARMUP = 5;
    private static final int TIMEOUT_MS = 3000;

    private static CoapClientConnector client;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        _Logger.info("===== CoAP Performance Test INIT =====");

        client = new CoapClientConnector();  // Use default constructor
        client.setEndpointPath(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE);

        _Logger.info("CoAP Client Connector initialized.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (client != null) {
            client.disconnectClient();
        }
        _Logger.info("===== CoAP Performance Test COMPLETE =====");
    }

    // -----------------------------------------------------------------
    // TEST: Confirmable Messages (CON)
    // -----------------------------------------------------------------
    @Test
    public void testConMessagesPerformance() {
        _Logger.info("===== BEGIN CON PERFORMANCE TEST =====");

        // Warmup
        for (int i = 0; i < WARMUP; i++) {
            client.sendGetRequest(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, null, true, TIMEOUT_MS);
        }

        long totalRtt = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.currentTimeMillis();

            boolean success = client.sendGetRequest(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, null, true, TIMEOUT_MS);

            long end = System.currentTimeMillis();
            long rtt = end - start;

            assertTrue("GET request should succeed", success);

            _Logger.info("CON iteration " + i + " RTT = " + rtt + " ms");

            assertTrue("CON RTT exceeded max allowed time", rtt < TIMEOUT_MS);

            totalRtt += rtt;
        }

        double avgRtt = totalRtt / (double) ITERATIONS;
        _Logger.info("===== CON PERFORMANCE COMPLETE =====");
        _Logger.info("Average CON RTT: " + avgRtt + " ms");

        assertTrue("Average CON RTT is too high", avgRtt < 2000);
    }

    // -----------------------------------------------------------------
    // TEST: Non-Confirmable Messages (NON)
    // -----------------------------------------------------------------
    @Test
    public void testNonMessagesPerformance() {
        _Logger.info("===== BEGIN NON PERFORMANCE TEST =====");

        // Warmup
        for (int i = 0; i < WARMUP; i++) {
            client.sendGetRequest(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, null, false, TIMEOUT_MS);
        }

        long totalRtt = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.currentTimeMillis();

            boolean success = client.sendGetRequest(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, null, false, TIMEOUT_MS);

            long end = System.currentTimeMillis();
            long rtt = end - start;

            assertTrue("GET request should succeed", success);

            _Logger.info("NON iteration " + i + " RTT = " + rtt + " ms");

            assertTrue("NON RTT exceeded max allowed time", rtt < TIMEOUT_MS);

            totalRtt += rtt;
        }

        double avgRtt = totalRtt / (double) ITERATIONS;
        _Logger.info("===== NON PERFORMANCE COMPLETE =====");
        _Logger.info("Average NON RTT: " + avgRtt + " ms");

        assertTrue("Average NON RTT is too high", avgRtt < 2000);
    }
}
