package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

public class Mqttv5ClientConnector implements IPubSubClient, MqttCallback {

    private static final Logger _Logger = Logger.getLogger(Mqttv5ClientConnector.class.getName());

    private String brokerAddr = "tcp://localhost:1883";
    private String clientId = "GDAClient-" + UUID.randomUUID().toString();

    private MqttClient mqttClient;
    private MqttConnectionOptions connOpts;

    private IDataMessageListener dataMsgListener;
    private IConnectionListener connListener;

    private static final int DEFAULT_QOS = ConfigConst.DEFAULT_QOS;

    public Mqttv5ClientConnector() {
        try {
            this.connOpts = new MqttConnectionOptions();
            this.connOpts.setCleanStart(true);
            this.connOpts.setAutomaticReconnect(true);

            this.mqttClient = new MqttClient(this.brokerAddr, this.clientId, new MemoryPersistence());
            this.mqttClient.setCallback(this);

            _Logger.info("Mqttv5ClientConnector initialized for broker: " + this.brokerAddr);

        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to initialize MQTT v5 client.", e);
        }
    }

    @Override
    public boolean connectClient() {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect(connOpts);
                _Logger.info("Connected to MQTT v5 broker: " + brokerAddr);
            }
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "MQTT v5 connection failed.", e);
            return false;
        }
    }

    @Override
    public boolean disconnectClient() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                _Logger.info("Disconnected from MQTT v5 broker: " + brokerAddr);
            }
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "MQTT v5 disconnect failed.", e);
            return false;
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    @Override
    public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos) {
        if (topicName == null || msg == null || msg.isEmpty()) return false;
        if (qos < 0 || qos > 2) qos = DEFAULT_QOS;

        try {
            MqttMessage mqttMsg = new MqttMessage(msg.getBytes());
            mqttMsg.setQos(qos);
            mqttClient.publish(topicName.getResourceName(), mqttMsg);
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message: " + topicName, e);
            return false;
        }
    }

    @Override
    public boolean subscribeToTopic(ResourceNameEnum topicName, int qos) {
        if (topicName == null) return false;
        if (qos < 0 || qos > 2) qos = DEFAULT_QOS;

        try {
            mqttClient.subscribe(topicName.getResourceName(), qos);
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName, e);
            return false;
        }
    }

    @Override
    public boolean unsubscribeFromTopic(ResourceNameEnum topicName) {
        if (topicName == null) return false;

        try {
            mqttClient.unsubscribe(topicName.getResourceName());
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to unsubscribe from topic: " + topicName, e);
            return false;
        }
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

    // ====================
    // MQTT v5 Callbacks
    // ====================

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        _Logger.info("MQTT v5 connection complete. Reconnect? " + reconnect + ". Broker: " + serverURI);
    }

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        _Logger.warning("MQTT v5 disconnected. Reason: " + disconnectResponse.getReasonString());
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        _Logger.info("Auth packet arrived. Reason code: " + reasonCode);
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        _Logger.fine("Delivered MQTT v5 message with ID: " + token.getMessageId());
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        _Logger.info("Message arrived: " + topic + " -> " + new String(msg.getPayload()));

        if (dataMsgListener != null) {
            ResourceNameEnum resource = null;
            try {
                resource = ResourceNameEnum.valueOf(topic); // Convert string to enum
            } catch (IllegalArgumentException e) {
                _Logger.warning("Unknown topic: " + topic);
            }

            if (resource != null) {
                dataMsgListener.handleIncomingMessage(resource, new String(msg.getPayload()));
            }
        }
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        _Logger.log(Level.SEVERE, "MQTT v5 error occurred.", exception);
    }
}
