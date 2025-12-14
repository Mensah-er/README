package programmingtheiot.integration.app;

//import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.app.DeviceDataManager;

public class DeviceDataManagerSimpleCdaActuationTest
{
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManagerSimpleCdaActuationTest.class.getName());

    private DeviceDataManager devDataMgr;

    @Before
    public void setUp() throws Exception
    {
        devDataMgr = new DeviceDataManager();
        devDataMgr.startManager();
        _Logger.info("=== DeviceDataManager STARTED ===");
    }

    @After
    public void tearDown() throws Exception
    {
        devDataMgr.stopManager();
        _Logger.info("=== DeviceDataManager STOPPED ===");
    }

    @Test
    public void testSendActuationEventsToCda()
    {
        ConfigUtil cfg = ConfigUtil.getInstance();

        float nominalVal = cfg.getFloat(ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");
        float lowVal     = cfg.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");
        int delay        = cfg.getInteger(ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

        _Logger.info("Test config → nominal=" + nominalVal +
                     ", floor=" + lowVal +
                     ", delay=" + delay);

        SensorData sd = new SensorData();
        sd.setName("TestHumiditySensor");
        sd.setLocationID("constraineddevice001");
        sd.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);

        // ---- normal readings ----
        sd.setValue(nominalVal);
        devDataMgr.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);

        sd.setValue(nominalVal);
        devDataMgr.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(2);

        // ---- abnormal (LOW) triggers ON after delay ----
        sd.setValue(lowVal - 10);
        devDataMgr.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 2);

        sd.setValue(lowVal - 5);
        devDataMgr.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 2);

        // ---- return to normal triggers OFF ----
        sd.setValue(nominalVal);
        devDataMgr.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
        waitForSeconds(delay + 2);
    }

    private void waitForSeconds(int s)
    {
        try { Thread.sleep(s * 1000L); }
        catch (InterruptedException e) {}
    }
}
