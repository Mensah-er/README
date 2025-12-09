package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.gda.connection.IConnectionListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.common.ResourceNameEnum;

public class MqttClientConnector implements IPubSubClient
{
    private static final Logger _Logger = Logger.getLogger(MqttClientConnector.class.getName());

    private ConfigUtil config = ConfigUtil.getInstance();
    private MqttAsyncClient mqttClient;
    private IDataMessageListener dataMsgListener;
    private IConnectionListener connectionListener;

    private String host;
    private int port;
    private int qos;
    private int keepAlive;
    private boolean enableEncryption;
    private String clientID;
    private String username;
    private String password;
    private int maxInflight = 10;

    public MqttClientConnector()
    {
        this("GdaMqttClient_" + System.currentTimeMillis());
    }

    public MqttClientConnector(String clientID)
    {
        this.clientID      = clientID;
        this.host          = config.getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, "host", ConfigConst.DEFAULT_HOST);
        this.port          = config.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, "port", ConfigConst.DEFAULT_MQTT_PORT);
        this.qos           = config.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, "defaultQos", ConfigConst.DEFAULT_QOS);
        this.keepAlive     = config.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, "keepAlive", ConfigConst.DEFAULT_KEEP_ALIVE);
        this.enableEncryption = config.getBoolean(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY);

        this.username      = config.getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, "username", null);
        this.password      = config.getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, "password", null);

        _Logger.info("Initialized GDA MQTT Connector with client ID: " + this.clientID);
    }

    // -------------------------
    // Configuration Setters
    // -------------------------
    public void setBrokerUri(String uri) {
        if (uri == null || uri.isEmpty()) return;

        try {
            String noScheme = uri.substring(uri.indexOf("://") + 3);
            String[] parts = noScheme.split(":");
            this.host = parts[0];
            this.port = Integer.parseInt(parts[1]);
            this.enableEncryption = uri.startsWith("ssl://");
        } catch (Exception e) {
            _Logger.warning("Invalid broker URI: " + uri);
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUseTls(boolean enableTls) {
        this.enableEncryption = enableTls;
        this.port = enableTls ? 8883 : 1883;
    }

    public void setMaxInflight(int maxInflight) {
        this.maxInflight = maxInflight;
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    // -------------------------
    // MQTT Operations
    // -------------------------
    @Override
    public boolean connectClient()
    {
        try
        {
            if (mqttClient != null && mqttClient.isConnected()) return true;

            String scheme = enableEncryption ? "ssl" : "tcp";
            String brokerUri = scheme + "://" + host + ":" + port;

            mqttClient = new MqttAsyncClient(brokerUri, clientID);

            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(true);
            opts.setKeepAliveInterval(keepAlive);
            opts.setMaxInflight(maxInflight);

            if (username != null && password != null) {
                opts.setUserName(username);
                opts.setPassword(password.toCharArray());
            }

            if (enableEncryption)
            {
                SSLContext ctx = SSLContext.getDefault();
                SSLSocketFactory fact = ctx.getSocketFactory();
                opts.setSocketFactory(fact);
            }

            mqttClient.setCallback(callback);
            mqttClient.connect(opts).waitForCompletion();

            _Logger.info("MQTT connected to: " + brokerUri);

            // Automatically subscribe to all CDA → GDA topics
            subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
            subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
            subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);

            if (connectionListener != null)
                connectionListener.onConnect();

            return true;
        }
        catch (Exception e)
        {
            _Logger.severe("MQTT connect error: " + e.getMessage());
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
                mqttClient.disconnect().waitForCompletion();
                if (connectionListener != null) connectionListener.onDisconnect();
                return true;
            }
        }
        catch (Exception e)
        {
            _Logger.severe("MQTT disconnect error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean publishMessage(ResourceNameEnum resource, String msg, int qos)
    {
        try
        {
            if (mqttClient == null || !mqttClient.isConnected()) return false;

            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(qos);

            _Logger.info("Publishing to topic: " + resource.getResourceName());

            mqttClient.publish(resource.getResourceName(), message).waitForCompletion();
            return true;
        }
        catch (Exception e)
        {
            _Logger.severe("Publish failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean subscribeToTopic(ResourceNameEnum resource, int qos)
    {
        try
        {
            if (mqttClient == null || !mqttClient.isConnected()) return false;

            mqttClient.subscribe(resource.getResourceName(), qos).waitForCompletion();
            _Logger.info("Subscribed to: " + resource.getResourceName());
            return true;
        }
        catch (Exception e)
        {
            _Logger.severe("Subscribe failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean unsubscribeFromTopic(ResourceNameEnum resource)
    {
        try
        {
            if (mqttClient != null)
            {
                mqttClient.unsubscribe(resource.getResourceName()).waitForCompletion();
                return true;
            }
        }
        catch (Exception e)
        {
            _Logger.severe("Unsubscribe failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        this.dataMsgListener = listener;
        return true;
    }

    @Override
    public boolean setConnectionListener(IConnectionListener listener)
    {
        this.connectionListener = listener;
        return true;
    }

    // --------------------------------------------------------------------------------------
    // MQTT CALLBACK HANDLER
    // --------------------------------------------------------------------------------------
    private MqttCallback callback = new MqttCallback()
    {
        @Override
        public void connectionLost(Throwable cause)
        {
            _Logger.warning("MQTT connection lost: " + cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message)
        {
            String payload = new String(message.getPayload());
            _Logger.info("Message arrived on topic: " + topic + " | Payload: " + payload);

            if (dataMsgListener != null)
            {
                ResourceNameEnum res = ResourceNameEnum.getEnumFromValue(topic);
                dataMsgListener.handleIncomingMessage(res, payload);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token)
        {
            _Logger.info("Delivery complete");
        }
    };
}
