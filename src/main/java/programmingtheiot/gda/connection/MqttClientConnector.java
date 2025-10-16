/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It implements the IPubSubClient interface for the Gateway Device Application (GDA),
 * handling MQTT connection, publish, subscribe, and message routing.
 * 
 * Author: Ernest Tete Mensah
 * 
 * Lab Module 07 – GDA MQTT Client Connector
 */

package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.IConnectionListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.gda.connection.IPubSubClient;

public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static logger reference
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());

	// MQTT connection fields
	private String host;
	private String brokerAddr;
	private String clientID;

	private MqttClient mqttClient;
	private MqttConnectOptions connOpts;

	// Class constant
	private static final int DEFAULT_QOS = ConfigConst.DEFAULT_QOS;

	// Data and connection listeners
	private IDataMessageListener dataMsgListener = null;
	private IConnectionListener connListener = null;

	// Constructor
	public MqttClientConnector()
	{
		super();

		try {
			this.host       = ConfigConst.DEFAULT_HOST;
			this.clientID   = MqttClient.generateClientId();
			this.brokerAddr = ConfigConst.DEFAULT_MQTT_PROTOCOL + "://" + this.host + ":" + ConfigConst.DEFAULT_MQTT_PORT;

			this.connOpts = new MqttConnectOptions();
			this.connOpts.setCleanSession(true);
			this.connOpts.setAutomaticReconnect(true);
			this.connOpts.setConnectionTimeout(10);

			this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, new MemoryPersistence());
			this.mqttClient.setCallback(this);

			_Logger.info("MqttClientConnector initialized for broker: " + this.brokerAddr);

		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Failed to initialize MQTT client.", e);
		}
	}

	// ================================================================
	// PUBLIC METHODS
	// ================================================================

	@Override
	public boolean connectClient()
	{
		try {
			if (!this.mqttClient.isConnected()) {
				this.mqttClient.connect(this.connOpts);
				_Logger.info("Connected to MQTT broker: " + this.brokerAddr);
			}
			return true;
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "MQTT connection failed.", e);
			return false;
		}
	}

	@Override
	public boolean disconnectClient()
	{
		try {
			if (this.mqttClient.isConnected()) {
				this.mqttClient.disconnect();
				_Logger.info("Disconnected from MQTT broker: " + this.brokerAddr);
			}
			return true;
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "MQTT disconnect failed.", e);
			return false;
		}
	}

	public boolean isConnected()
	{
		return (this.mqttClient != null && this.mqttClient.isConnected());
	}

	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	{
		if (topicName == null) {
			_Logger.warning("Resource is null. Unable to publish message: " + this.brokerAddr);
			return false;
		}

		if (msg == null || msg.length() == 0) {
			_Logger.warning("Message is null or empty. Unable to publish message: " + this.brokerAddr);
			return false;
		}

		if (qos < 0 || qos > 2) {
			qos = DEFAULT_QOS;
		}

		try {
			byte[] payload = msg.getBytes();
			MqttMessage mqttMsg = new MqttMessage(payload);
			mqttMsg.setQos(qos);
			this.mqttClient.publish(topicName.getResourceName(), mqttMsg);

			_Logger.info("Published message to topic [" + topicName.getResourceName() + "]: " + msg);
			return true;

		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to publish message to topic: " + topicName, e);
		}

		return false;
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		if (topicName == null) {
			_Logger.warning("Resource is null. Unable to subscribe to topic: " + this.brokerAddr);
			return false;
		}

		if (qos < 0 || qos > 2) {
			qos = DEFAULT_QOS;
		}

		try {
			this.mqttClient.subscribe(topicName.getResourceName(), qos);
			_Logger.info("Subscribed to topic: " + topicName.getResourceName());
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName, e);
		}

		return false;
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		if (topicName == null) {
			_Logger.warning("Resource is null. Unable to unsubscribe from topic: " + this.brokerAddr);
			return false;
		}

		try {
			this.mqttClient.unsubscribe(topicName.getResourceName());
			_Logger.info("Unsubscribed from topic: " + topicName.getResourceName());
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to unsubscribe from topic: " + topicName, e);
		}

		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
			return true;
		}
		return false;
	}

	@Override
	public boolean setConnectionListener(IConnectionListener listener)
	{
		if (listener != null) {
			this.connListener = listener;
			return true;
		}
		return false;
	}

	// ================================================================
	// MQTT CALLBACKS
	// ================================================================

	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " + serverURI);
	}

	@Override
	public void connectionLost(Throwable t)
	{
		_Logger.log(Level.WARNING, "Lost connection to MQTT broker: " + this.brokerAddr, t);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		try {
			_Logger.fine("Delivered MQTT message with ID: " + token.getMessageId());
		} catch (Exception e) {
			_Logger.warning("Delivery callback error.");
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
	{
		_Logger.info("MQTT message arrived on topic: '" + topic + "'");
	}
}
