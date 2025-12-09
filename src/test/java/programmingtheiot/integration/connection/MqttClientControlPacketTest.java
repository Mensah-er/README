package programmingtheiot.integration.connection;

import static org.junit.Assert.*;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.ConfigConst;

public class MqttClientControlPacketTest {

    private static final Logger _Logger = Logger.getLogger(MqttClientControlPacketTest.class.getName());
    private MqttClientConnector mqttClient = null;

    @Before
    public void setUp() throws Exception {
        this.mqttClient = new MqttClientConnector();
    }

    @After
    public void tearDown() throws Exception {
        if (this.mqttClient != null && this.mqttClient.isConnected()) {
            this.mqttClient.disconnectClient();
        }
    }

    @Test
    public void testConnectAndDisconnect() {
        int delay = 60; // default keepAlive
        _Logger.info("Starting connect/disconnect test...");
        assertTrue("Failed to connect to broker", mqttClient.connectClient());
        assertFalse("Second connect should fail", mqttClient.connectClient());
        try { Thread.sleep(delay * 1000 + 2000); } catch (InterruptedException e) {}
        assertTrue("Failed to disconnect from broker", mqttClient.disconnectClient());
        assertFalse("Second disconnect should fail", mqttClient.disconnectClient());
        _Logger.info("Connect/disconnect test completed.");
    }

    @Test
    public void testServerPing() {
        _Logger.info("Starting server ping test...");
        assertTrue("Failed to connect to broker", mqttClient.connectClient());
        // TODO: Implement ping logic if supported
        assertTrue("Failed to disconnect from broker", mqttClient.disconnectClient());
        _Logger.info("Server ping test completed.");
    }

    @Test
    public void testPubSub() {
        _Logger.info("Starting publish/subscribe test...");
        int qos1 = 1, qos2 = 2;
        assertTrue("Failed to connect to broker", mqttClient.connectClient());

        // Subscribe
        assertTrue(mqttClient.subscribeToTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, qos1));
        assertTrue(mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos2));

        // Publish
        assertTrue(mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, "Test message QoS 1", qos1));
        assertTrue(mqttClient.publishMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, "Test message QoS 2", qos2));

        // Unsubscribe
        assertTrue(mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE));
        assertTrue(mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE));

        assertTrue(mqttClient.disconnectClient());
        _Logger.info("Publish/subscribe test completed.");
    }
}
