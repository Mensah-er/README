package programmingtheiot.integration.app;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;

import programmingtheiot.gda.app.DeviceDataManager;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.data.DataUtil;

/**
 * Basic integration tests for DeviceDataManager (Lab 8).
 */
public class DeviceDataManagerWithCommsTest
{
    // Logger
    private static final Logger _Logger = Logger.getLogger(DeviceDataManagerWithCommsTest.class.getName());

    // --- Test setup methods ---
    @BeforeClass
    public static void setUpBeforeClass() throws Exception { }

    @AfterClass
    public static void tearDownAfterClass() throws Exception { }

    @Before
    public void setUp() throws Exception { }

    @After
    public void tearDown() throws Exception { }

    // --- Test methods ---
    @Test
    public void testStartAndStopManagerWithMqtt()
    {
        // Start DeviceDataManager
        DeviceDataManager devDataMgr = new DeviceDataManager();
        devDataMgr.startManager();

        // Connect MQTT client
        IPubSubClient mqttClient = new MqttClientConnector();
        mqttClient.connectClient();

        // Prepare sample SensorData
        SensorData sd = new SensorData();
        sd.setName("Some Sensor");
        sd.setLocationID("constraineddevice001");

        String sdJson = DataUtil.getInstance().sensorDataToJson(sd);

        // Publish message to CDA_SENSOR_MSG
        mqttClient.publishMessage(ResourceNameEnum.CDA_SENSOR_MSG, sdJson, 1);

        try {
            Thread.sleep(10000L); // wait for 10 seconds to allow message processing
        } catch (InterruptedException e) {
            // ignore
        }

        // Disconnect MQTT and stop DeviceDataManager
        mqttClient.disconnectClient();
        devDataMgr.stopManager();
    }

}
