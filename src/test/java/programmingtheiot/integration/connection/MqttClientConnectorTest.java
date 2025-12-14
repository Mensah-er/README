package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.MqttClientConnector;

public class MqttClientConnectorTest
{
    private static final Logger _Logger =
        Logger.getLogger(MqttClientConnectorTest.class.getName());

    private MqttClientConnector mqttClient;

    private static final String TLS_BROKER_URI = "ssl://localhost:8883";
    private static final String MQTT_USERNAME = "gdauser";
    private static final String MQTT_PASSWORD = "GdaPass123";

    @Before
    public void setUp() throws Exception
    {
        mqttClient = new MqttClientConnector(false);   // Local gateway MQ config (Option 1)

        mqttClient.setBrokerUri(TLS_BROKER_URI);
        mqttClient.setUsername(MQTT_USERNAME);
        mqttClient.setPassword(MQTT_PASSWORD);
        mqttClient.setUseTls(true);
    }

    @After
    public void tearDown() throws Exception
    {
        if (mqttClient != null && mqttClient.isConnected()) {
            try { Thread.sleep(300); } catch (Exception e) {}
            mqttClient.disconnectClient();
        }
    }

    @Test
    public void testConnectAndDisconnectWithTLS()
    {
        assertTrue("Failed to connect with TLS", mqttClient.connectClient());

        try { Thread.sleep(500); } catch (Exception e) {}

        assertTrue("Failed to disconnect", mqttClient.disconnectClient());
    }

    @Test
    public void testPublishAndSubscribeWithTLS()
    {
        final int qos = 0;

        mqttClient.setDataMessageListener(new IDataMessageListener() {
            @Override
            public boolean handleIncomingMessage(ResourceNameEnum res, String msg) {
                _Logger.info("Received message on " + res + ": " + msg);
                return true;
            }

            @Override public boolean handleSensorMessage(ResourceNameEnum r, SensorData d) { return true; }
            @Override public boolean handleSystemPerformanceMessage(ResourceNameEnum r, SystemPerformanceData d) { return true; }
            @Override public boolean handleActuatorCommandRequest(ResourceNameEnum r, programmingtheiot.data.ActuatorData d) { return true; }
            @Override public boolean handleActuatorCommandResponse(ResourceNameEnum r, programmingtheiot.data.ActuatorData d) { return true; }
            @Override public void setActuatorDataListener(String n, IActuatorDataListener l) {}
        });

        assertTrue("Failed to connect MQTT client", mqttClient.connectClient());
        try { Thread.sleep(700); } catch (Exception e) {}

        // Correct enum names
        ResourceNameEnum mgmtTopic = ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE;
        ResourceNameEnum updateTopic = ResourceNameEnum.CDA_UPDATE_NOTIFICATIONS_MSG;

        // Subscribe
        assertTrue(mqttClient.subscribeToTopic(mgmtTopic, qos));
        try { Thread.sleep(500); } catch (Exception e) {}

        assertTrue(mqttClient.subscribeToTopic(updateTopic, qos));
        try { Thread.sleep(500); } catch (Exception e) {}

        // Publish
        assertTrue(mqttClient.publishMessage(mgmtTopic, "Test TLS message", qos));
        try { Thread.sleep(700); } catch (Exception e) {}

        assertTrue(mqttClient.publishMessage(updateTopic, "Test Update Notification", qos));
        try { Thread.sleep(700); } catch (Exception e) {}

        // Unsubscribe
        assertTrue(mqttClient.unsubscribeFromTopic(mgmtTopic));
        try { Thread.sleep(300); } catch (Exception e) {}

        assertTrue(mqttClient.unsubscribeFromTopic(updateTopic));
        try { Thread.sleep(300); } catch (Exception e) {}

        // Disconnect
        assertTrue(mqttClient.disconnectClient());
    }
}
