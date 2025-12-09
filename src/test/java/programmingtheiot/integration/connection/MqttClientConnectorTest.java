package programmingtheiot.integration.connection;

import static org.junit.Assert.*;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.common.IActuatorDataListener;

public class MqttClientConnectorTest {

    private static final Logger _Logger =
            Logger.getLogger(MqttClientConnectorTest.class.getName());

    private MqttClientConnector mqttClient;

    private static final String TLS_BROKER_URI = "ssl://localhost:8883";
    private static final String MQTT_USERNAME = "gdauser";
    private static final String MQTT_PASSWORD = "GdaPass123";

 
    @Before
    public void setUp() throws Exception {
        mqttClient = new MqttClientConnector();
        mqttClient.setBrokerUri(TLS_BROKER_URI);
        mqttClient.setUsername(MQTT_USERNAME);
        mqttClient.setPassword(MQTT_PASSWORD);
        mqttClient.setUseTls(true);
    }

    @After
    public void tearDown() throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnectClient();
        }
    }

    @Test
    public void testConnectAndDisconnectWithTLS() {
        assertTrue("Failed to connect with TLS", mqttClient.connectClient());
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        assertTrue("Failed to disconnect", mqttClient.disconnectClient());
    }

    @Test
    public void testPublishAndSubscribeWithTLS() {
        final int qos = 0;

        mqttClient.setDataMessageListener(new IDataMessageListener() {
            @Override
            public boolean handleIncomingMessage(ResourceNameEnum resource, String msg) {
                _Logger.info("Received message on " + resource + ": " + msg);
                return true;
            }

            @Override public boolean handleSensorMessage(ResourceNameEnum r, SensorData d) { return true; }
            @Override public boolean handleSystemPerformanceMessage(ResourceNameEnum r, SystemPerformanceData d) { return true; }
            @Override public boolean handleActuatorCommandRequest(ResourceNameEnum r, programmingtheiot.data.ActuatorData d) { return true; }
            @Override public boolean handleActuatorCommandResponse(ResourceNameEnum r, programmingtheiot.data.ActuatorData d) { return true; }
            @Override public void setActuatorDataListener(String n, IActuatorDataListener l) {}
        });

        assertTrue("Failed to connect MQTT client", mqttClient.connectClient());
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Subscribe to all relevant topics
        assertTrue(mqttClient.subscribeToTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, qos));
        assertTrue(mqttClient.subscribeToTopic(ResourceNameEnum.CDA_UPDATE_NOTIFICATIONS_MSG, qos));

        // Publish test messages
        assertTrue(mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, "Test TLS message", qos));
        assertTrue(mqttClient.publishMessage(ResourceNameEnum.CDA_UPDATE_NOTIFICATIONS_MSG, "Test Update Notification", qos));

        // Unsubscribe
        assertTrue(mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE));
        assertTrue(mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_UPDATE_NOTIFICATIONS_MSG));

        assertTrue(mqttClient.disconnectClient());
    }
}
