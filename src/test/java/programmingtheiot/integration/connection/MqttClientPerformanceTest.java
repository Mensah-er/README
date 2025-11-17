package programmingtheiot.integration.connection;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.gda.connection.MqttClientConnector;

/**
 * Integration test for MQTT Client performance.
 * Uses synchronous MqttClient only.
 * IMPORTANT: Run tests locally only against a local MQTT broker.
 */
public class MqttClientPerformanceTest {

    private static final Logger _Logger = Logger.getLogger(MqttClientPerformanceTest.class.getName());

    public static final int MAX_TEST_RUNS = 10000; // 10,000 messages for Lab 10

    private MqttClientConnector mqttClient = null;

    @Before
    public void setUp() throws Exception {
        this.mqttClient = new MqttClientConnector(); // synchronous client
    }

    @After
    public void tearDown() throws Exception {
        // nothing to clean up
    }

    @Test
    public void testConnectAndDisconnect() {
        long startMillis = System.currentTimeMillis();

        assertTrue(this.mqttClient.connectClient());
        assertTrue(this.mqttClient.disconnectClient());

        long endMillis = System.currentTimeMillis();
        long elapsedMillis = endMillis - startMillis;

        _Logger.info("Connect and Disconnect: " + elapsedMillis + " ms");
    }

    @Test
    public void testPublishQoS0() {
        execTestPublish(MAX_TEST_RUNS, 0);
    }

    @Test
    public void testPublishQoS1() {
        execTestPublish(MAX_TEST_RUNS, 1);
    }

    @Test
    public void testPublishQoS2() {
        execTestPublish(MAX_TEST_RUNS, 2);
    }

    // --------------------
    // Private helper
    // --------------------
    private void execTestPublish(int maxTestRuns, int qos) {
        assertTrue(this.mqttClient.connectClient());

        SensorData sensorData = new SensorData();
        String payload = DataUtil.getInstance().sensorDataToJson(sensorData);
        int payloadLen = payload.length();

        long startMillis = System.currentTimeMillis();

        for (int sequenceNo = 1; sequenceNo <= maxTestRuns; sequenceNo++) {
            this.mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_CMD_RESOURCE, payload, qos);
        }

        long endMillis = System.currentTimeMillis();
        long elapsedMillis = endMillis - startMillis;

        assertTrue(this.mqttClient.disconnectClient());

        String msg = String.format(
            "\n\tTesting Publish: QoS = %s | msgs = %s | payload size = %s | start = %s | end = %s | elapsed = %s",
            qos, maxTestRuns, payloadLen,
            (float) startMillis / 1000, (float) endMillis / 1000, (float) elapsedMillis / 1000
        );

        _Logger.info(msg);
    }
}
