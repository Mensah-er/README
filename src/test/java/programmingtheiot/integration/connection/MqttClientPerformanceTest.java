package programmingtheiot.integration.connection;

import static org.junit.Assert.assertTrue;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.common.ResourceNameEnum;

public class MqttClientPerformanceTest {

    private static final Logger _Logger = Logger.getLogger(MqttClientPerformanceTest.class.getName());

    public static final int MAX_TEST_RUNS = 10000;

    private MqttClientConnector mqttClient;

    private static final String BROKER_URI = "tcp://localhost:1883";
    private static final String MQTT_USERNAME = "gdauser";
    private static final String MQTT_PASSWORD = "GdaPass123";

    @Before
    public void setUp() throws Exception {
        mqttClient = new MqttClientConnector();
        mqttClient.setBrokerUri(BROKER_URI);
        mqttClient.setUsername(MQTT_USERNAME);
        mqttClient.setPassword(MQTT_PASSWORD);
        mqttClient.setUseTls(false);
        mqttClient.setMaxInflight(50000); // prevent QoS 1/2 overflow
    }

    @After
    public void tearDown() throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnectClient();
        }
    }

    @Test
    public void testConnectAndDisconnect() {
        long startMillis = System.currentTimeMillis();
        assertTrue("Failed to connect MQTT client", mqttClient.connectClient());
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        assertTrue("Failed to disconnect MQTT client", mqttClient.disconnectClient());
        long endMillis = System.currentTimeMillis();
        _Logger.info("Connect and Disconnect elapsed time: " + (endMillis - startMillis) + " ms");
    }

    @Test
    public void testPublishQoS0() { execTestPublish(MAX_TEST_RUNS, 0); }
    @Test
    public void testPublishQoS1() { execTestPublish(MAX_TEST_RUNS, 1); }
    @Test
    public void testPublishQoS2() { execTestPublish(MAX_TEST_RUNS, 2); }

    private void execTestPublish(int maxTestRuns, int qos) {
        assertTrue("Failed to connect MQTT client", mqttClient.connectClient());
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        String payload = "Test message payload for performance testing";
        int payloadLen = payload.length();
        long startMillis = System.currentTimeMillis();

        for (int i = 1; i <= maxTestRuns; i++) {
            boolean published = mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, payload, qos);
            if (!published) _Logger.warning("Failed to publish message at sequence: " + i);
            if (qos > 0) try { Thread.sleep(1); } catch (Exception e) {}
        }

        long endMillis = System.currentTimeMillis();
        assertTrue("Failed to disconnect MQTT client", mqttClient.disconnectClient());

        _Logger.info(String.format(
                "Published %d messages | QoS = %d | payload size = %d | elapsed = %.3f sec",
                maxTestRuns, qos, payloadLen, (endMillis - startMillis) / 1000.0));
    }
}
