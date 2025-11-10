import unittest
import logging
import time

from programmingtheiot.cda.connection.CoapClientConnector import CoapClientConnector
from programmingtheiot.common.ResourceNameEnum import ResourceNameEnum
from programmingtheiot.data.DataUtil import DataUtil
from programmingtheiot.data.SensorData import SensorData
from programmingtheiot.data.ActuatorData import ActuatorData


class CoapClientConnectorTest(unittest.TestCase):
    """
    Integration tests for CoapClientConnector.
    Ensure your GDA CoAP Server is running before executing.
    """

    DEFAULT_TIMEOUT = 5

    @classmethod
    def setUpClass(cls):
        logging.basicConfig(level=logging.INFO,
                            format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
        logging.info("=== Starting CoapClientConnector Integration Tests ===")

    def setUp(self):
        self.coapClient = CoapClientConnector()
        logging.info("CoapClientConnector initialized for testing.")

    def tearDown(self):
        logging.info("Tearing down test case.")

    # ----------------------------------------------------------------
    # DISCOVERY
    # ----------------------------------------------------------------
    #@unittest.skip("Ignore for now.")
    def testConnectAndDiscover(self):
        """Test CoAP Discovery (.well-known/core)."""
        result = self.coapClient.sendDiscoveryRequest(timeout=self.DEFAULT_TIMEOUT)
        self.assertTrue(result, "Discovery request failed.")
        time.sleep(2)

    # ----------------------------------------------------------------
    # GET Requests
    # ----------------------------------------------------------------
    #@unittest.skip("Ignore for now.")
    def testGetActuatorCommandCon(self):
        """Test GET request (CON)."""
        result = self.coapClient.sendGetRequest(
            resource=ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
            enableCON=True,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    #@unittest.skip("Ignore for now.")
    def testGetActuatorCommandNon(self):
        """Test GET request (NON)."""
        result = self.coapClient.sendGetRequest(
            resource=ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
            enableCON=False,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    # ----------------------------------------------------------------
    # PUT Requests
    # ----------------------------------------------------------------
    #@unittest.skip("Ignore for now.")
    def testPutSensorMessageCon(self):
        """Test PUT SensorData (CON)."""
        data = SensorData()
        jsonData = DataUtil().sensorDataToJson(data)
        result = self.coapClient.sendPutRequest(
            resource=ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            enableCON=True,
            payload=jsonData,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    #@unittest.skip("Ignore for now.")
    def testPutSensorMessageNon(self):
        """Test PUT SensorData (NON)."""
        data = SensorData()
        jsonData = DataUtil().sensorDataToJson(data)
        result = self.coapClient.sendPutRequest(
            resource=ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            enableCON=False,
            payload=jsonData,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    # ----------------------------------------------------------------
    # POST Requests
    # ----------------------------------------------------------------
    #@unittest.skip("Ignore for now.")
    def testPostSensorMessageCon(self):
        """Test POST SensorData (CON)."""
        data = SensorData()
        jsonData = DataUtil().sensorDataToJson(data)
        result = self.coapClient.sendPostRequest(
            resource=ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            enableCON=True,
            payload=jsonData,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    #@unittest.skip("Ignore for now.")
    def testPostSensorMessageNon(self):
        """Test POST SensorData (NON)."""
        data = SensorData()
        jsonData = DataUtil().sensorDataToJson(data)
        result = self.coapClient.sendPostRequest(
            resource=ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            enableCON=False,
            payload=jsonData,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    # ----------------------------------------------------------------
    # DELETE Requests
    # ----------------------------------------------------------------
    #@unittest.skip("Ignore for now.")
    def testDeleteSensorMessageCon(self):
        """Test DELETE SensorData (CON)."""
        result = self.coapClient.sendDeleteRequest(
            resource=ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            enableCON=True,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    #@unittest.skip("Ignore for now.")
    def testDeleteSensorMessageNon(self):
        """Test DELETE SensorData (NON)."""
        result = self.coapClient.sendDeleteRequest(
            resource=ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            enableCON=False,
            timeout=self.DEFAULT_TIMEOUT
        )
        self.assertTrue(result)
        time.sleep(2)

    # ----------------------------------------------------------------
    # OBSERVE
    # ----------------------------------------------------------------
    #@unittest.skip("Ignore for now.")
    def testStartAndStopObserve(self):
        """Test CoAP Observe functionality."""
        resource = ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE
        started = self.coapClient.startObserver(resource=resource)
        self.assertTrue(started, "Failed to start observer.")

        # Let it observe for 10 seconds
        time.sleep(10)

        stopped = self.coapClient.stopObserver(resource=resource)
        self.assertTrue(stopped, "Failed to stop observer.")
        time.sleep(2)


if __name__ == "__main__":
    unittest.main()
