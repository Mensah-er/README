package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * CloudClientConnector – FULLY COMPLIANT implementation
 * for PIOT-GDA-11-002 / 11-003 / 11-004.
 * Works with Ubidots MQTT API.
 */
public class CloudClientConnector
	extends BaseCloudClient
	implements ICloudClient, IConnectionListener
{
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnector.class.getName());

	private String topicPrefix = "";
	private MqttClientConnector mqttClient = null;
	private IDataMessageListener dataMsgListener = null;

	private int qosLevel = 1;

	// ---------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------

	public CloudClientConnector()
	{
		ConfigUtil cfg = ConfigUtil.getInstance();

		this.topicPrefix = cfg.getProperty(
			ConfigConst.CLOUD_GATEWAY_SERVICE,
			ConfigConst.BASE_TOPIC_KEY);

		if (this.topicPrefix == null)
			this.topicPrefix = "/";
		else if (! this.topicPrefix.endsWith("/"))
			this.topicPrefix += "/";

		_Logger.info("CloudClient initialized. Topic prefix: " + topicPrefix);
	}

	// ---------------------------------------------------------
	// Connection control
	// ---------------------------------------------------------

	
	@Override
	public boolean connectClient()
	{
	    if (mqttClient == null) {
	        String provider =
	            ConfigUtil.getInstance().getProperty(
	                ConfigConst.CLOUD_GATEWAY_SERVICE,
	                ConfigConst.CLOUD_SERVICE_NAME_KEY);

	        mqttClient =
	            new MqttClientConnector(
	                ConfigConst.CLOUD_GATEWAY_SERVICE + "." + provider);
	        mqttClient.setConnectionListener(this);
	    }

	    return mqttClient.connectClient();
	}


	@Override
	public boolean disconnectClient()
	{
		if (this.mqttClient != null && this.mqttClient.isConnected())
			return this.mqttClient.disconnectClient();

		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
		return true;
	}

	// ---------------------------------------------------------
	// Topic helpers
	// ---------------------------------------------------------

	private String createTopicName(ResourceNameEnum res)
	{
		String deviceLabel = ConfigUtil.getInstance().getProperty(
			ConfigConst.GATEWAY_DEVICE,
			ConfigConst.DEVICE_LOCATION_ID);

		return (this.topicPrefix + deviceLabel + "/" +
			res.getResourceName()).toLowerCase();
	}

	private String createTopicName(ResourceNameEnum res, String itemName)
	{
		return (createTopicName(res) + "-" + itemName).toLowerCase();
	}

	// ---------------------------------------------------------
	// Publish helpers
	// ---------------------------------------------------------

	private boolean publishMessageToCloud(
		ResourceNameEnum res, String item, String payload)
	{
		String topic = createTopicName(res, item);
		return publishMessageToCloud(topic, payload);
	}

	private boolean publishMessageToCloud(String topic, String payload)
	{
		try {
			this.mqttClient.publishMessage(
				topic, payload.getBytes(), this.qosLevel);
			return true;
		} catch (Exception e) {
			_Logger.warning("Publish failed: " + topic);
			return false;
		}
	}

	// ---------------------------------------------------------
	// Edge → Cloud
	// ---------------------------------------------------------

	@Override
	public boolean sendEdgeDataToCloud(
		ResourceNameEnum res, SensorData data)
	{
		if (res == null || data == null)
			return false;

		String json =
			DataUtil.getInstance().sensorDataToTimeAndValueJson(data);

		return publishMessageToCloud(res, data.getName(), json);
	}

	@Override
	public boolean sendEdgeDataToCloud(
		ResourceNameEnum res, SystemPerformanceData sp)
	{
		if (res == null || sp == null)
			return false;

		SensorData cpu = new SensorData();
		cpu.updateData(sp);
		cpu.setName(ConfigConst.CPU_UTIL_NAME);
		cpu.setValue(sp.getCpuUtilization());

		SensorData mem = new SensorData();
		mem.updateData(sp);
		mem.setName(ConfigConst.MEM_UTIL_NAME);
		mem.setValue(sp.getMemoryUtilization());

		return sendEdgeDataToCloud(res, cpu)
			&& sendEdgeDataToCloud(res, mem);
	}

	// ---------------------------------------------------------
	// Cloud subscriptions
	// ---------------------------------------------------------

	@Override
	public boolean subscribeToCloudEvents(ResourceNameEnum res)
	{
		if (this.mqttClient != null && this.mqttClient.isConnected())
			return this.mqttClient.subscribeToTopic(
				createTopicName(res), this.qosLevel);

		_Logger.warning("Cannot subscribe, client not connected");
		return false;
	}

	@Override
	public boolean unsubscribeFromCloudEvents(ResourceNameEnum res)
	{
		if (this.mqttClient != null && this.mqttClient.isConnected())
			return this.mqttClient.unsubscribeFromTopic(
				createTopicName(res));

		_Logger.warning("Cannot unsubscribe, client not connected");
		return false;
	}

	// ---------------------------------------------------------
	// IConnectionListener implementation (PIOT-GDA-11-004)
	// ---------------------------------------------------------

	@Override
	public void onConnect()
	{
		_Logger.info("Cloud MQTT connected. Subscribing to LED actuator topic.");

		LedEnablementMessageListener ledListener =
			new LedEnablementMessageListener(this.dataMsgListener);

		String ledTopic = createTopicName(
			ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
			ConfigConst.LED_ACTUATOR_NAME);

		this.mqttClient.subscribeToTopic(
			ledTopic, this.qosLevel, ledListener);
	}

	@Override
	public void onDisconnect()
	{
		_Logger.info("Cloud MQTT disconnected.");
	}

	// ---------------------------------------------------------
	// Cloud → GDA → CDA LED actuation handler
	// ---------------------------------------------------------

	private class LedEnablementMessageListener
		implements IMqttMessageListener
	{
		private IDataMessageListener dataMsgListener;

		LedEnablementMessageListener(IDataMessageListener listener)
		{
			this.dataMsgListener = listener;
		}

		@Override
		public void messageArrived(String topic, MqttMessage msg)
		{
			try {
				String json = new String(msg.getPayload());

				ActuatorData ad =
					DataUtil.getInstance().jsonToActuatorData(json);

				ad.setLocationID(ConfigConst.CONSTRAINED_DEVICE);
				ad.setTypeID(ConfigConst.LED_ACTUATOR_TYPE);
				ad.setName(ConfigConst.LED_ACTUATOR_NAME);

				int val = (int) ad.getValue();

				if (val == ConfigConst.ON_COMMAND)
					ad.setStateData("LED switching ON");
				else if (val == ConfigConst.OFF_COMMAND)
					ad.setStateData("LED switching OFF");
				else
					return;

				if (this.dataMsgListener != null) {
					this.dataMsgListener.handleIncomingMessage(
						ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
						DataUtil.getInstance().actuatorDataToJson(ad));
				}
			} catch (Exception e) {
				_Logger.log(Level.WARNING,
					"Failed to process cloud LED actuation event", e);
			}
		}
	}
}
