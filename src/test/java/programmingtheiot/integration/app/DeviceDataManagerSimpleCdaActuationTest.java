package programmingtheiot.integration.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.app.DeviceDataManager;

/**
 * Simple test for DeviceDataManager humidity actuation logic.
 */
public class DeviceDataManagerSimpleCdaActuationTest
{
    private static final Logger _Logger =
            Logger.getLogger(DeviceDataManagerSimpleCdaActuationTest.class.getName());

    private DeviceDataManager devDataMgr;

    @Before
    public void setUp() throws Exception {
        devDataMgr = new DeviceDataManager();
        devDataMgr.startManager();
    }

    @After
    public void tearDown() throws Exception {
        devDataMgr.stopManager();
    }

    @Test
    public void testSendActuationEventsToCda() {
        ConfigUtil cfgUtil = ConfigUtil.getInstance();

        float nominalVal = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");
        float lowVal     = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");
        float highVal    = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

        int delay = 5; // force test threshold

        generateAndProcessHumiditySensorDataSequence(
                devDataMgr, nominalVal, lowVal, highVal, delay
        );
    }

    private void generateAndProcessHumiditySensorDataSequence(DeviceDataManager ddm,
                                                              float nominalVal,
                                                              float lowVal,
                                                              float highVal,
                                                              int delay)
    {
        SensorData sd = new SensorData();
        sd.setName("Test Humidity Sensor");
        sd.setLocationID("constraineddevice001");
        sd.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);

        // Two normal messages (no actuation)
        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);

        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);

        // Exceptional value triggers ON event after threshold
        sd.setValue(lowVal - 2);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);

        sd.setValue(lowVal - 1);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);

        // Nominal value triggers OFF event after threshold
        sd.setValue(lowVal + 1);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);

        sd.setValue(nominalVal);
        ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 1);
    }

    private void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
