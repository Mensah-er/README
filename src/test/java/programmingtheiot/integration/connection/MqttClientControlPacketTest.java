/**
 * 
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 - 2025 by Andrew D. King
 */ 

package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;

/**
 * This test case class contains basic integration tests for
 * MqttClientConnector control packets. It serves as a starting
 * point for students to implement additional functionality.
 */
public class MqttClientControlPacketTest
{
    // Logger
    private static final Logger _Logger =
        Logger.getLogger(MqttClientControlPacketTest.class.getName());
    
    // MQTT client instance
    private MqttClientConnector mqttClient = null;
    
    // Test setup methods
    @Before
    public void setUp() throws Exception
    {
        this.mqttClient = new MqttClientConnector();
    }
    
    @After
    public void tearDown() throws Exception
    {
        if (this.mqttClient != null && this.mqttClient.isConnected()) {
            this.mqttClient.disconnectClient();
        }
    }
    
    // Test methods
    
    @Test
    public void testConnectAndDisconnect()
    {
        int delay = ConfigUtil.getInstance().getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE,
            ConfigConst.KEEP_ALIVE_KEY,
            ConfigConst.DEFAULT_KEEP_ALIVE
        );

        _Logger.info("Starting connect/disconnect test...");

        // CONNECT
        assertTrue("Failed to connect to broker", mqttClient.connectClient());

        // Attempt to connect again (should fail or return false)
        assertFalse("Second connect should fail", mqttClient.connectClient());

        try {
            Thread.sleep(delay * 1000 + 2000);
        } catch (InterruptedException e) {
            // ignore
        }

        // DISCONNECT
        assertTrue("Failed to disconnect from broker", mqttClient.disconnectClient());
        assertFalse("Second disconnect should fail", mqttClient.disconnectClient());

        _Logger.info("Connect/disconnect test completed.");
    }
    
    @Test
    public void testServerPing()
    {
        _Logger.info("Starting server ping test...");
        
        assertTrue("Failed to connect to broker", mqttClient.connectClient());

        // TODO: Implement ping logic if supported by your client
        
        assertTrue("Failed to disconnect from broker", mqttClient.disconnectClient());
        _Logger.info("Server ping test completed.");
    }
    
    @Test
    public void testPubSub()
    {
        _Logger.info("Starting publish/subscribe test...");

        int qos1 = 1;
        int qos2 = 2;

        assertTrue("Failed to connect to broker", mqttClient.connectClient());

        // Subscribe to topics
        assertTrue(mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos1));
        assertTrue(mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos2));

        // Publish messages
        assertTrue(mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE,
            "Test message QoS 1", qos1));
        assertTrue(mqttClient.publishMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            "Test message QoS 2", qos2));

        // Unsubscribe
        assertTrue(mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE));
        assertTrue(mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE));

        // Disconnect
        assertTrue(mqttClient.disconnectClient());

        _Logger.info("Publish/subscribe test completed.");
    }
}
