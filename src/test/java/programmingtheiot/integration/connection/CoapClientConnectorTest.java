package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CoapClientConnector;

/**
 * Integration tests for CoapClientConnector.
 * The CoAP server must be running before executing these tests.
 */
public class CoapClientConnectorTest
{
    public static final int DEFAULT_TIMEOUT = 5;

    private static final Logger _Logger =
        Logger.getLogger(CoapClientConnectorTest.class.getName());

    private CoapClientConnector coapClient = null;
    private IDataMessageListener dataMsgListener = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception { }

    @AfterClass
    public static void tearDownAfterClass() throws Exception { }

    @Before
    public void setUp() throws Exception
    {
        this.coapClient = new CoapClientConnector();
        this.dataMsgListener = new DefaultDataMessageListener();
        this.coapClient.setDataMessageListener(this.dataMsgListener);
    }

    @After
    public void tearDown() throws Exception { }

    // ----- Test Methods -----

    @Test
    public void testConnectAndDiscover()
    {
        assertTrue(this.coapClient.sendDiscoveryRequest(DEFAULT_TIMEOUT));
    }

    @Test
    public void testGetRequestCon()
    {
        assertTrue(this.coapClient.sendGetRequest(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, true, DEFAULT_TIMEOUT));
    }

    @Test
    public void testGetRequestNon()
    {
        assertTrue(this.coapClient.sendGetRequest(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, false, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPostRequestCon()
    {
        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(1);
        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        assertTrue(this.coapClient.sendPostRequest(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, true, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPostRequestNon()
    {
        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(1);
        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        assertTrue(this.coapClient.sendPostRequest(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, false, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPutRequestCon()
    {
        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(2);
        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        assertTrue(this.coapClient.sendPutRequest(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, true, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPutRequestNon()
    {
        SystemStateData ssd = new SystemStateData();
        ssd.setCommand(2);
        String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);

        assertTrue(this.coapClient.sendPutRequest(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, false, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testDeleteRequestCon()
    {
        assertTrue(this.coapClient.sendDeleteRequest(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, null, true, DEFAULT_TIMEOUT));
    }

    @Test
    public void testDeleteRequestNon()
    {
        assertTrue(this.coapClient.sendDeleteRequest(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, null, false, DEFAULT_TIMEOUT));
    }
}
