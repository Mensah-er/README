import logging
import socket
import traceback

from coapthon import defines
from coapthon.client.helperclient import HelperClient
from coapthon.messages.option import Option
from coapthon.utils import generate_random_token

import programmingtheiot.common.ConfigConst as ConfigConst
from programmingtheiot.common.ConfigUtil import ConfigUtil
from programmingtheiot.common.ResourceNameEnum import ResourceNameEnum
from programmingtheiot.common.IDataMessageListener import IDataMessageListener
from programmingtheiot.cda.connection.IRequestResponseClient import IRequestResponseClient
from programmingtheiot.data.DataUtil import DataUtil


class CoapClientConnector(IRequestResponseClient):
    """
    Implements a CoAP Client using the CoAPthon3 library.
    Supports GET, POST, PUT, DELETE, DISCOVERY, and OBSERVE.
    """

    def __init__(self, dataMsgListener: IDataMessageListener = None):
        self.config = ConfigUtil()
        self.dataMsgListener = dataMsgListener
        self.enableConfirmedMsgs = False
        self.coapClient = None
        self.observeRequests = {}
        self.includeDebugLogDetail = True

        # Load configuration
        self.host = self.config.getProperty(ConfigConst.COAP_GATEWAY_SERVICE,
                                            ConfigConst.HOST_KEY,
                                            ConfigConst.DEFAULT_HOST)
        self.port = self.config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE,
                                           ConfigConst.PORT_KEY,
                                           ConfigConst.DEFAULT_COAP_PORT)

        try:
            tmpHost = socket.gethostbyname(self.host)
            if tmpHost:
                self.host = tmpHost
                self.uriPath = f"coap://{self.host}:{self.port}/"
                logging.info(f"CoAP client will connect to: {self.uriPath}")
                self._initClient()
            else:
                logging.error(f"Cannot resolve host: {self.host}")
        except socket.gaierror as e:
            logging.error(f"Failed to resolve host: {self.host}")
            raise e

    def _initClient(self):
        try:
            self.coapClient = HelperClient(server=(self.host, self.port))
            logging.info(f"CoAP Client created. Connected to {self.host}:{self.port}")
        except Exception as e:
            logging.error("Failed to initialize CoAP client.")
            traceback.print_exception(type(e), e, e.__traceback__)

    # --------------------------------------------------------
    # Utility and setup methods
    # --------------------------------------------------------
    def setDataMessageListener(self, listener: IDataMessageListener = None) -> bool:
        if listener:
            self.dataMsgListener = listener
            return True
        return False

    def _createResourcePath(self, resource: ResourceNameEnum = None, name: str = None):
        resourcePath = ""
        if resource:
            resourcePath += resource.value
        if name:
            if resourcePath:
                resourcePath += "/"
            resourcePath += name
        return resourcePath

    # --------------------------------------------------------
    # DISCOVERY
    # --------------------------------------------------------
    def sendDiscoveryRequest(self, timeout: int = IRequestResponseClient.DEFAULT_TIMEOUT) -> bool:
        logging.info("Discovering remote CoAP resources...")
        try:
            resourcePath = ".well-known/core"
            request = self.coapClient.mk_request(defines.Codes.GET, path=resourcePath)
            request.token = generate_random_token(2)
            self.coapClient.send_request(request, timeout=timeout, callback=self._onDiscoveryResponse)
            return True
        except Exception as e:
            logging.error(f"Discovery request failed: {e}")
            return False

    def _onDiscoveryResponse(self, response):
        if not response:
            logging.warning("DISCOVERY response invalid. Ignoring.")
            return
        logging.info(f"DISCOVERY response received: {response.payload}")

    # --------------------------------------------------------
    # GET
    # --------------------------------------------------------
    def sendGetRequest(self, resource: ResourceNameEnum = None, name: str = None,
                       enableCON: bool = False, timeout: int = IRequestResponseClient.DEFAULT_TIMEOUT) -> bool:
        try:
            resourcePath = self._createResourcePath(resource, name)
            logging.info(f"Issuing GET request to path: {resourcePath}")
            request = self.coapClient.mk_request(defines.Codes.GET, path=resourcePath)
            request.token = generate_random_token(2)
            if not enableCON:
                request.type = defines.Types["NON"]
            response = self.coapClient.send_request(request, timeout=timeout)
            self._onGetResponse(response, resourcePath)
            return True
        except Exception as e:
            logging.error(f"GET request failed: {e}")
            return False

    def _onGetResponse(self, response, resourcePath: str = None):
        if not response:
            logging.warning("GET response invalid. Ignoring.")
            return
        logging.info("GET response received.")
        jsonData = response.payload
        logging.info(f"Response data: {jsonData}")
        try:
            dataType = resourcePath.split('/')[-1] if resourcePath else ""
            if dataType and ConfigConst.ACTUATOR_CMD in dataType:
                ad = DataUtil().jsonToActuatorData(jsonData)
                if self.dataMsgListener:
                    self.dataMsgListener.handleActuatorCommandMessage(ad)
        except Exception as e:
            logging.warning(f"Failed to decode GET payload: {e}")

    # --------------------------------------------------------
    # PUT
    # --------------------------------------------------------
    def sendPutRequest(self, resource: ResourceNameEnum = None, name: str = None,
                       enableCON: bool = False, payload: str = None,
                       timeout: int = IRequestResponseClient.DEFAULT_TIMEOUT) -> bool:
        try:
            resourcePath = self._createResourcePath(resource, name)
            logging.info(f"Issuing PUT with path: {resourcePath}")
            request = self.coapClient.mk_request(defines.Codes.PUT, path=resourcePath)
            request.token = generate_random_token(2)
            request.payload = payload
            if not enableCON:
                request.type = defines.Types["NON"]
            self.coapClient.send_request(request, callback=self._onPutResponse, timeout=timeout)
            return True
        except Exception as e:
            logging.error(f"PUT request failed: {e}")
            return False

    def _onPutResponse(self, response):
        if not response:
            logging.warning("PUT response invalid. Ignoring.")
            return
        logging.info(f"PUT response received: {response.payload}")

    # --------------------------------------------------------
    # POST
    # --------------------------------------------------------
    def sendPostRequest(self, resource: ResourceNameEnum = None, name: str = None,
                        enableCON: bool = False, payload: str = None,
                        timeout: int = IRequestResponseClient.DEFAULT_TIMEOUT) -> bool:
        try:
            resourcePath = self._createResourcePath(resource, name)
            logging.info(f"Issuing POST with path: {resourcePath}")
            request = self.coapClient.mk_request(defines.Codes.POST, path=resourcePath)
            request.token = generate_random_token(2)
            request.payload = payload
            if not enableCON:
                request.type = defines.Types["NON"]
            self.coapClient.send_request(request, callback=self._onPostResponse, timeout=timeout)
            return True
        except Exception as e:
            logging.error(f"POST request failed: {e}")
            return False

    def _onPostResponse(self, response):
        if not response:
            logging.warning("POST response invalid. Ignoring.")
            return
        logging.info(f"POST response received: {response.payload}")

    # --------------------------------------------------------
    # DELETE
    # --------------------------------------------------------
    def sendDeleteRequest(self, resource: ResourceNameEnum = None, name: str = None,
                          enableCON: bool = False, timeout: int = IRequestResponseClient.DEFAULT_TIMEOUT) -> bool:
        try:
            resourcePath = self._createResourcePath(resource, name)
            logging.info(f"Issuing DELETE with path: {resourcePath}")
            request = self.coapClient.mk_request(defines.Codes.DELETE, path=resourcePath)
            request.token = generate_random_token(2)
            if not enableCON:
                request.type = defines.Types["NON"]
            self.coapClient.send_request(request, callback=self._onDeleteResponse, timeout=timeout)
            return True
        except Exception as e:
            logging.error(f"DELETE request failed: {e}")
            return False

    def _onDeleteResponse(self, response):
        if not response:
            logging.warning("DELETE response invalid. Ignoring.")
            return
        logging.info(f"DELETE response received: {response.payload}")

    # --------------------------------------------------------
    # OBSERVE
    # --------------------------------------------------------
    def startObserver(self, resource: ResourceNameEnum = None, name: str = None,
                      ttl: int = IRequestResponseClient.DEFAULT_TTL) -> bool:
        if resource in self.observeRequests:
            logging.warning(f"Already observing resource {resource}. Ignoring.")
            return False

        resourcePath = self._createResourcePath(resource, name)
        handler = HandleActuatorEvent(listener=self.dataMsgListener,
                                      resource=resource,
                                      requests=self.observeRequests)
        try:
            self.coapClient.observe(path=resourcePath,
                                    callback=handler.handleActuatorResponse)
            self.observeRequests[resource] = handler
            logging.info(f"Started observing resource: {resourcePath}")
            return True
        except Exception as e:
            logging.warning(f"Failed to start observer for {resourcePath}: {e}")
            return False

    def stopObserver(self, resource: ResourceNameEnum = None, name: str = None,
                     timeout: int = IRequestResponseClient.DEFAULT_TIMEOUT) -> bool:
        if not resource or resource not in self.observeRequests:
            logging.warning(f"Resource {resource} not being observed.")
            return False
        handler = self.observeRequests.pop(resource, None)
        if handler:
            logging.info(f"Stopped observing resource {resource}.")
            return True
        return False


class HandleActuatorEvent:
    """
    Internal class for handling ActuatorData observation events.
    """

    def __init__(self, listener: IDataMessageListener = None,
                 resource: ResourceNameEnum = ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
                 requests=None):
        self.listener = listener
        self.resource = resource
        self.observeRequests = requests or {}

    def handleActuatorResponse(self, response):
        if response:
            jsonData = response.payload
            logging.info(f"Received actuator response for {self.resource}: {jsonData}")
            if self.listener:
                try:
                    data = DataUtil().jsonToActuatorData(jsonData)
                    self.listener.handleActuatorCommandMessage(data)
                except Exception as e:
                    logging.warning(f"Failed to decode actuator observe data: {e}")
