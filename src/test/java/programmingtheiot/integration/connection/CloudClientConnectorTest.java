package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.app.DeviceDataManager;
import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.ConfigConst;

/**
 * Basic integration tests for CloudClientConnector (Lab 8).
 */
public class CloudClientConnectorTest
{
    private static final Logger _Logger = Logger.getLogger(CloudClientConnectorTest.class.getName());

    private ICloudClient cloudClient = null;

    @Before
    public void setUp() throws Exception
    {
        this.cloudClient = new CloudClientConnector();
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testIntegratedCloudClientConnectAndDisconnect()
    {
        DeviceDataManager ddm = new DeviceDataManager();
        ddm.startManager();

        try {
            Thread.sleep(10000L); // shortened for test
        } catch (Exception e) {
            // ignore
        }

        ddm.stopManager();

        _Logger.info("Test complete.");
    }

    @Test
    public void testPublishAndSubscribe()
    {
        this.cloudClient.setDataMessageListener(new DefaultDataMessageListener());

        assertTrue(this.cloudClient.connectClient());

        try {
            Thread.sleep(2000L); // wait for connection
        } catch (Exception e) {
            // ignore
        }

        SensorData sensorData = new SensorData();
        sensorData.setName(ConfigConst.TEMP_SENSOR_NAME);
        sensorData.setValue(92.0f);

        SystemPerformanceData sysPerfData = new SystemPerformanceData();
        sysPerfData.setCpuUtilization(34.7f);
        sysPerfData.setMemoryUtilization(39.8f);

        // Use updated ResourceNameEnum values
        assertTrue(this.cloudClient.subscribeToCloudEvents(ResourceNameEnum.CDA_ACTUATOR_CMD));
        assertTrue(this.cloudClient.sendEdgeDataToCloud(ResourceNameEnum.CDA_SENSOR_MSG, sensorData));
        assertTrue(this.cloudClient.sendEdgeDataToCloud(ResourceNameEnum.CDA_SYSTEM_PERF_MSG, sysPerfData));

        try {
            Thread.sleep(5000L); // allow data processing
        } catch (Exception e) {
            // ignore
        }

        assertTrue(this.cloudClient.unsubscribeFromCloudEvents(ResourceNameEnum.CDA_ACTUATOR_CMD));
        assertTrue(this.cloudClient.disconnectClient());
    }
}
