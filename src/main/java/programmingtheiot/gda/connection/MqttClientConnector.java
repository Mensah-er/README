package programmingtheiot.gda.connection;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Fully corrected MQTT Client Connector.
 * Supports:
 * - Cloud.GatewayService or Mqtt.GatewayService
 * - TLS
 * - Credential file loading
 * - ClientID override
 * - Connection listener
 */
public class MqttClientConnector implements IPubSubClient
{
	// -------------------------------------------------------------
	// Static
	// -------------------------------------------------------------

	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());

	// -------------------------------------------------------------
	// Instance vars
	// -------------------------------------------------------------

	private ConfigUtil configUtil = ConfigUtil.getInstance();

	private MqttAsyncClient mqttClient = null;
	private IDataMessageListener dataMsgListener = null;
	private IConnectionListener connListener = null;

	private boolean useCloudGatewayConfig = false;

	// MQTT parameters
	private String host = ConfigConst.DEFAULT_HOST;
	private int    port = ConfigConst.DEFAULT_MQTT_PORT;
	private int    qos = ConfigConst.DEFAULT_QOS;
	private int    keepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
	private boolean enableEncryption = false;
	private int    maxInflight = 10;

	private String clientID = "GdaMqttClient-" + System.currentTimeMillis();
	private String username = null;
	private String password = null;

	// -------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------

	public MqttClientConnector()
	{
		this(false);
	}

	public MqttClientConnector(boolean useCloudGatewayConfig)
	{
		this(useCloudGatewayConfig ? ConfigConst.CLOUD_GATEWAY_SERVICE : null);
	}

	public MqttClientConnector(String configSection)
	{
		if (configSection != null && configSection.trim().length() > 0)
		{
			this.useCloudGatewayConfig = true;
			initClientParameters(configSection);
			initCredentialParameters(configSection);
			initClientId(configSection);
		}
		else
		{
			this.useCloudGatewayConfig = false;
			initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
			initCredentialParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
		}

		_Logger.info("Initialized MQTT Connector | Cloud=" + this.useCloudGatewayConfig +
		             " | Host=" + this.host + ":" + this.port +
		             " | TLS=" + this.enableEncryption +
		             " | ClientID=" + this.clientID);
	}

	// -------------------------------------------------------------
	// Initialization
	// -------------------------------------------------------------

	private void initClientParameters(String section)
	{
		this.host =
			configUtil.getProperty(section, "host", ConfigConst.DEFAULT_HOST);

		int plainPort =
			configUtil.getInteger(section, "port", ConfigConst.DEFAULT_MQTT_PORT);

		int securePort =
			configUtil.getInteger(section, "securePort", ConfigConst.DEFAULT_MQTT_SECURE_PORT);

		this.enableEncryption =
			configUtil.getBoolean(section, ConfigConst.ENABLE_CRYPT_KEY);

		this.port = enableEncryption ? securePort : plainPort;

		this.qos =
			configUtil.getInteger(section, "defaultQoS", ConfigConst.DEFAULT_QOS);

		this.keepAlive =
			configUtil.getInteger(section, "keepAlive", ConfigConst.DEFAULT_KEEP_ALIVE);

		// Optional inline credentials
		this.username = configUtil.getProperty(section, "username", null);
		this.password = configUtil.getProperty(section, "password", null);
	}

	private void initCredentialParameters(String section)
	{
		// If username/password already set (inline), don't override
		if (username != null && password != null)
			return;

		String credFile =
			configUtil.getProperty(section, ConfigConst.CRED_FILE_KEY);

		if (credFile == null)
		{
			_Logger.warning("No credential file for section: " + section);
			return;
		}

		try (FileInputStream fis = new FileInputStream(credFile))
		{
			Properties p = new Properties();
			p.load(fis);

			this.username =
				    p.getProperty("username",
				        p.getProperty("Username",
				            p.getProperty("userToken")));

				this.password =
				    p.getProperty("password",
				        p.getProperty("Password",
				            p.getProperty("authToken")));


			_Logger.info("Credentials loaded from: " + credFile);
		}
		catch (Exception e)
		{
			_Logger.log(Level.SEVERE, "Failed to load credentials from: " + credFile, e);
		}
	}

	private void initClientId(String section)
	{
		String id = configUtil.getProperty(section, "clientID", null);

		if (id != null && id.trim().length() > 0)
		{
			this.clientID = id;
			_Logger.info("ClientID override: " + id);
		}
	}

	// -------------------------------------------------------------
	// Extra setters (used by tests)
	// -------------------------------------------------------------

	/**
	 * Override the broker URI at runtime.
	 * Example: ssl://localhost:8883 or tcp://localhost:1883
	 */
	public void setBrokerUri(String uri)
	{
		if (uri == null || uri.isEmpty())
			return;

		try
		{
			// scheme://host:port
			int schemeEnd = uri.indexOf("://");
			String scheme = uri.substring(0, schemeEnd);
			String rest   = uri.substring(schemeEnd + 3); // after ://

			String[] hp = rest.split(":");
			if (hp.length >= 2)
			{
				this.host = hp[0].trim();
				this.port = Integer.parseInt(hp[1].trim());
			}

			this.enableEncryption = scheme.equalsIgnoreCase("ssl");

			_Logger.info("Broker URI overridden to: " + uri +
			             " | host=" + this.host +
			             " | port=" + this.port +
			             " | TLS=" + this.enableEncryption);
		}
		catch (Exception e)
		{
			_Logger.log(Level.WARNING, "Invalid broker URI: " + uri, e);
		}
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setUseTls(boolean enableTls)
	{
		this.enableEncryption = enableTls;

		// If flipping at runtime, also adjust the port to defaults
		if (enableTls)
		{
			if (this.port == ConfigConst.DEFAULT_MQTT_PORT)
				this.port = ConfigConst.DEFAULT_MQTT_SECURE_PORT;
		}
		else
		{
			if (this.port == ConfigConst.DEFAULT_MQTT_SECURE_PORT)
				this.port = ConfigConst.DEFAULT_MQTT_PORT;
		}
	}

	public void setMaxInflight(int maxInflight)
	{
		this.maxInflight = maxInflight;
	}

	// -------------------------------------------------------------
	// Connection
	// -------------------------------------------------------------

	@Override
	public boolean connectClient()
	{
		try
		{
			if (mqttClient != null && mqttClient.isConnected())
				return true;

			String scheme   = enableEncryption ? "ssl" : "tcp";
			String brokerUri = scheme + "://" + host + ":" + port;

			mqttClient = new MqttAsyncClient(brokerUri, clientID);
			mqttClient.setCallback(mqttCallback);

			MqttConnectOptions opts = new MqttConnectOptions();
			opts.setCleanSession(true);
			opts.setKeepAliveInterval(keepAlive);
			opts.setMaxInflight(maxInflight);

			if (username != null)
			{
				opts.setUserName(username);
				opts.setPassword(password != null ? password.toCharArray() : new char[] {});
			}

			if (enableEncryption)
			{
				SSLContext ctx = SSLContext.getDefault();
				opts.setSocketFactory(ctx.getSocketFactory());
			}

			_Logger.info("Connecting to MQTT broker: " + brokerUri);

			IMqttToken token = mqttClient.connect(opts);
			token.waitForCompletion();

			_Logger.info("MQTT CONNECTED: " + brokerUri);

			return true;
		}
		catch (Exception e)
		{
			_Logger.log(Level.SEVERE, "MQTT connect error", e);
			return false;
		}
	}

	@Override
	public boolean disconnectClient()
	{
		try
		{
			if (mqttClient != null && mqttClient.isConnected())
			{
				IMqttToken t = mqttClient.disconnect();
				t.waitForCompletion();

				if (connListener != null)
					connListener.onDisconnect();

				return true;
			}
		}
		catch (Exception e)
		{
			_Logger.log(Level.SEVERE, "MQTT disconnect error", e);
		}

		return false;
	}

	/**
	 * Allow other classes (CloudClientConnector, tests) to check connection state.
	 */
	public boolean isConnected()
	{
		return mqttClient != null && mqttClient.isConnected();
	}

	// -------------------------------------------------------------
	// Listener setters (required by IPubSubClient)
	// -------------------------------------------------------------

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
		return true;
	}

	@Override
	public boolean setConnectionListener(IConnectionListener listener)
	{
		this.connListener = listener;
		return true;
	}

	// -------------------------------------------------------------
	// Publish / Subscribe / Unsubscribe
	// -------------------------------------------------------------

	@Override
	public boolean publishMessage(ResourceNameEnum topic, String msg, int qos)
	{
		if (topic == null || msg == null || msg.length() == 0)
		{
			_Logger.warning("Invalid publish request. Topic or msg is null / empty.");
			return false;
		}

		return publishMessage(topic.getResourceName(), msg.getBytes(), qos);
	}

	protected boolean publishMessage(String topic, byte[] payload, int qos)
	{
		try
		{
			if (mqttClient == null || !mqttClient.isConnected())
			{
				_Logger.warning("MQTT not connected. Publish failed for topic: " + topic);
				return false;
			}

			if (qos < 0 || qos > 2)
				qos = ConfigConst.DEFAULT_QOS;

			MqttMessage m = new MqttMessage(payload);
			m.setQos(qos);

			IMqttDeliveryToken t = mqttClient.publish(topic, m);
			t.waitForCompletion();

			return true;
		}
		catch (Exception e)
		{
			_Logger.log(Level.SEVERE, "Publish failed: " + topic, e);
			return false;
		}
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topic, int qos)
	{
		if (topic == null)
		{
			_Logger.warning("ResourceNameEnum is null. Cannot subscribe.");
			return false;
		}

		return subscribeToTopic(topic.getResourceName(), qos);
	}

	protected boolean subscribeToTopic(String topic, int qos)
	{
		return subscribeToTopic(topic, qos, null);
	}

	/**
	 * Optional subscribe with message listener (not strictly required by your tests,
	 * but useful and consumes IMqttMessageListener import).
	 */
	protected boolean subscribeToTopic(String topic, int qos, IMqttMessageListener listener)
	{
		try
		{
			if (mqttClient == null || !mqttClient.isConnected())
			{
				_Logger.warning("MQTT not connected. Subscribe failed for topic: " + topic);
				return false;
			}

			if (qos < 0 || qos > 2)
				qos = ConfigConst.DEFAULT_QOS;

			IMqttToken t;

			if (listener != null)
			{
				t = mqttClient.subscribe(topic, qos, null, null, listener);
			}
			else
			{
				t = mqttClient.subscribe(topic, qos);
			}

			t.waitForCompletion();
			return true;
		}
		catch (Exception e)
		{
			_Logger.log(Level.SEVERE, "Subscribe failed: " + topic, e);
			return false;
		}
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topic)
	{
		if (topic == null)
		{
			_Logger.warning("ResourceNameEnum is null. Cannot unsubscribe.");
			return false;
		}

		return unsubscribeFromTopic(topic.getResourceName());
	}

	protected boolean unsubscribeFromTopic(String topic)
	{
		try
		{
			if (mqttClient == null || !mqttClient.isConnected())
			{
				_Logger.warning("MQTT not connected. Unsubscribe failed for topic: " + topic);
				return false;
			}

			IMqttToken t = mqttClient.unsubscribe(topic);
			t.waitForCompletion();
			return true;
		}
		catch (Exception e)
		{
			_Logger.log(Level.SEVERE, "Unsubscribe failed: " + topic, e);
			return false;
		}
	}

	// -------------------------------------------------------------
	// Callback
	// -------------------------------------------------------------

	private MqttCallbackExtended mqttCallback = new MqttCallbackExtended()
	{
		@Override
		public void connectComplete(boolean reconnect, String serverURI)
		{
			_Logger.info("MQTT CONNECT COMPLETE (reconnect=" + reconnect + ")");

			// Only auto-subscribe to CDA topics when using local gateway config
			if (!useCloudGatewayConfig)
			{
				subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
				subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
				subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
			}

			if (connListener != null)
				connListener.onConnect();
		}

		@Override
		public void connectionLost(Throwable cause)
		{
			_Logger.log(Level.WARNING, "MQTT connection lost.", cause);

			if (connListener != null)
				connListener.onDisconnect();
		}

		@Override
		public void messageArrived(String topic, MqttMessage msg) throws Exception
		{
			String payload = new String(msg.getPayload(), "UTF-8");

			_Logger.info("MESSAGE IN: " + topic + " | " + payload);

			if (dataMsgListener != null)
			{
				ResourceNameEnum r = ResourceNameEnum.getEnumFromValue(topic);
				dataMsgListener.handleIncomingMessage(r, payload);
			}
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token)
		{
			// no-op
		}
	};
}
