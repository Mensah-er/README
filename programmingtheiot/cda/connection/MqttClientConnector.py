#####
# 
# This class is part of the Programming the Internet of Things
# project, and is available via the MIT License.
# 

import logging
import paho.mqtt.client as mqtt

from programmingtheiot.common.ConfigUtil import ConfigUtil
import programmingtheiot.common.ConfigConst as ConfigConst
from programmingtheiot.common.ResourceNameEnum import ResourceNameEnum
from programmingtheiot.common.IDataMessageListener import IDataMessageListener


class MqttClientConnector:
    """
    MQTT client connector for the Constrained Device Application (CDA).
    Handles connection, disconnection, publish, subscribe, and callbacks.
    """

    DEFAULT_QOS = ConfigConst.DEFAULT_QOS

    def __init__(self, clientID: str = None):
        self.cfg = ConfigUtil()
        self.clientID = clientID if clientID else "constraineddevice001"
        self.brokerHost = self.cfg.getProperty(
            ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST
        )
        self.brokerPort = self.cfg.getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT
        )
        self.keepAlive = self.cfg.getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE
        )

        self.mqttClient = mqtt.Client(client_id=self.clientID)
        self.dataMsgListener: IDataMessageListener = None

        logging.info(f"\tMQTT Client ID:   {self.clientID}")
        logging.info(f"\tMQTT Broker Host: {self.brokerHost}")
        logging.info(f"\tMQTT Broker Port: {self.brokerPort}")
        logging.info(f"\tMQTT Keep Alive:  {self.keepAlive}")

    def setDataMessageListener(self, listener: IDataMessageListener):
        self.dataMsgListener = listener

    # --- MQTT Callbacks ---
    def onConnect(self, client, userdata, flags, rc):
        logging.info(f"MQTT client connected successfully: {client}")

    def onDisconnect(self, client, userdata, rc):
        logging.info(f"MQTT client disconnected from broker: {client} (RC: {rc})")

    def onMessage(self, client, userdata, msg):
        payload = msg.payload
        if payload:
            logging.info(f"MQTT message received on topic {msg.topic} with payload: {payload.decode('utf-8')}")
        else:
            logging.info(f"MQTT message received with no payload: {msg}")

        if self.dataMsgListener:
            self.dataMsgListener.handleMessage(msg.topic, payload)

    def onPublish(self, client, userdata, mid):
        logging.info(f"MQTT message published. Message ID: {mid}")

    def onSubscribe(self, client, userdata, mid, granted_qos):
        logging.info(f"MQTT client subscribed. Message ID: {mid}, Granted QoS: {granted_qos}")

    # --- Connection Management ---
    def connectClient(self):
        self.mqttClient.on_connect = self.onConnect
        self.mqttClient.on_disconnect = self.onDisconnect
        self.mqttClient.on_message = self.onMessage
        self.mqttClient.on_publish = self.onPublish
        self.mqttClient.on_subscribe = self.onSubscribe

        logging.info(f"MQTT client connecting to broker at host: {self.brokerHost}")
        self.mqttClient.connect(self.brokerHost, self.brokerPort, self.keepAlive)
        self.mqttClient.loop_start()

    def disconnectClient(self):
        logging.info(f"Disconnecting MQTT client from broker: {self.brokerHost}")
        self.mqttClient.loop_stop()
        self.mqttClient.disconnect()

    # --- Publish / Subscribe ---
    def publishMessage(self, resource: ResourceNameEnum = None, msg: str = None, qos: int = DEFAULT_QOS) -> bool:
        if not resource:
            logging.warning("No topic specified. Cannot publish message.")
            return False
        if not msg:
            logging.warning(f"No message specified. Cannot publish to topic: {resource.value}")
            return False
        if qos < 0 or qos > 2:
            qos = self.DEFAULT_QOS

        logging.info(f"Publishing message to topic [{resource.value}] with QoS {qos}")
        msgInfo = self.mqttClient.publish(topic=resource.value, payload=msg, qos=qos)
        msgInfo.wait_for_publish()

        if msgInfo.is_published():
            logging.info(f"Message published successfully to {resource.value}")
            return True
        else:
            logging.warning(f"Message failed to publish to {resource.value}")
            return False

    def subscribeToTopic(self, resource: ResourceNameEnum = None, callback=None, qos: int = DEFAULT_QOS) -> bool:
        if not resource:
            logging.warning("No topic specified. Cannot subscribe.")
            return False
        if qos < 0 or qos > 2:
            qos = self.DEFAULT_QOS

        logging.info(f"Subscribing to topic [{resource.value}] with QoS {qos}")
        self.mqttClient.subscribe(resource.value, qos)
        if callback:
            self.mqttClient.message_callback_add(resource.value, callback)
        return True

    def unsubscribeFromTopic(self, resource: ResourceNameEnum = None) -> bool:
        if not resource:
            logging.warning("No topic specified. Cannot unsubscribe.")
            return False

        logging.info(f"Unsubscribing from topic [{resource.value}]")
        self.mqttClient.unsubscribe(resource.value)
        return True
