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

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.ConfigConst;

/**
 * MQTT client connector for GDA with TLS support.
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended {

    private static final Logger _Logger = Logger.getLogger(MqttClientConnector.class.getName());

    private String host;
    private int port;
    private String brokerAddr;
    private String clientID;

    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;

    private static final int DEFAULT_QOS = ConfigConst.DEFAULT_QOS;

    private IDataMessageListener dataMsgListener = null;
    private IConnectionListener connListener = null;

    private String cafile = null;
    private String certfile = null;
    private String keyfile = null;
    //private boolean allowAnonymous = true;

    public MqttClientConnector() {
        this(ConfigConst.DEFAULT_HOST, ConfigConst.DEFAULT_MQTT_PORT, null, null, null, true);
    }

    public MqttClientConnector(String host, int port, String cafile, String certfile, String keyfile, boolean allowAnonymous) {
        super();
        try {
            this.host = host;
            this.port = port;
            this.cafile = cafile;
            this.certfile = certfile;
            this.keyfile = keyfile;
      //      this.allowAnonymous = allowAnonymous;

            this.clientID = MqttClient.generateClientId();
            this.brokerAddr = "ssl://" + this.host + ":" + this.port;

            this.connOpts = new MqttConnectOptions();
            this.connOpts.setCleanSession(true);
            this.connOpts.setAutomaticReconnect(true);
            this.connOpts.setConnectionTimeout(10);

            if (this.cafile != null) {
                System.setProperty("javax.net.ssl.trustStore", this.cafile);
            }
            if (this.certfile != null && this.keyfile != null) {
                System.setProperty("javax.net.ssl.keyStore", this.certfile);
                System.setProperty("javax.net.ssl.keyStorePassword", ""); // if key has password, set it here
            }

            this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, new MemoryPersistence());
            this.mqttClient.setCallback(this);

            _Logger.info("MqttClientConnector initialized for broker: " + this.brokerAddr);

        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to initialize MQTT client.", e);
        }
    }

    @Override
    public boolean connectClient() {
        try {
            if (!this.mqttClient.isConnected()) {
                this.mqttClient.connect(this.connOpts);
                _Logger.info("Connected to MQTT broker: " + this.brokerAddr);
                if (connListener != null) connListener.onConnect();
            }
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "MQTT connection failed.", e);
            if (connListener != null) connListener.onConnect();
            return false;
        }
    }

    @Override
    public boolean disconnectClient() {
        try {
            if (this.mqttClient.isConnected()) {
                this.mqttClient.disconnect();
                _Logger.info("Disconnected from MQTT broker: " + this.brokerAddr);
                if (connListener != null) connListener.onDisconnect();
            }
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "MQTT disconnect failed.", e);
            return false;
        }
    }

    @Override
    public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos) {
        if (topicName == null || msg == null || msg.isEmpty()) return false;
        if (qos < 0 || qos > 2) qos = DEFAULT_QOS;

        try {
            MqttMessage mqttMsg = new MqttMessage(msg.getBytes());
            mqttMsg.setQos(qos);
            this.mqttClient.publish(topicName.getResourceName(), mqttMsg);
            _Logger.info("Published message to topic [" + topicName.getResourceName() + "]: " + msg);
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message.", e);
        }
        return false;
    }

    @Override
    public boolean subscribeToTopic(ResourceNameEnum topicName, int qos) {
        if (topicName == null) return false;
        if (qos < 0 || qos > 2) qos = DEFAULT_QOS;

        try {
            this.mqttClient.subscribe(topicName.getResourceName(), qos);
            _Logger.info("Subscribed to topic: " + topicName.getResourceName());
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to subscribe.", e);
        }
        return false;
    }

    @Override
    public boolean unsubscribeFromTopic(ResourceNameEnum topicName) {
        if (topicName == null) return false;
        try {
            this.mqttClient.unsubscribe(topicName.getResourceName());
            _Logger.info("Unsubscribed from topic: " + topicName.getResourceName());
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to unsubscribe.", e);
        }
        return false;
    }

    @Override
    public boolean setDataMessageListener(IDataMessageListener listener) {
        if (listener != null) {
            this.dataMsgListener = listener;
            return true;
        }
        return false;
    }

    @Override
    public boolean setConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            this.connListener = listener;
            return true;
        }
        return false;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        _Logger.info("MQTT connectComplete (reconnect=" + reconnect + ") Broker: " + serverURI);
    }

    @Override
    public void connectionLost(Throwable cause) {
        _Logger.log(Level.WARNING, "MQTT connection lost!", cause);
        if (connListener != null) connListener.onDisconnect();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            _Logger.fine("Delivered message ID: " + token.getMessageId());
        } catch (Exception e) {
            _Logger.warning("Delivery callback error.");
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        _Logger.info("MQTT message arrived on topic: " + topic);
        ResourceNameEnum res = ResourceNameEnum.getEnumFromValue(topic);
        if (res != null && dataMsgListener != null) {
            dataMsgListener.handleIncomingMessage(res, new String(message.getPayload()));
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
