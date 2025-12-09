package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;

/**
 * DeviceDataManager (GDA)
 * Handles incoming messages from CDA and triggers actuator ON/OFF events based on humidity.
 */
public class DeviceDataManager implements IDataMessageListener
{
    private static final Logger _Logger = Logger.getLogger(DeviceDataManager.class.getName());

    // defaults
    private float nominalHumiditySetting = 40.0f;
    private float triggerHumidifierFloor = 30.0f;
    private float triggerHumidifierCeiling = 50.0f;

    // feature flags
    private boolean handleHumidityChangeOnDevice = false;
    private boolean enableCoapServer = true;
    private boolean enableMqttClient = false;

    // collaborators
    private MqttClientConnector mqttClient = null;
    private IActuatorDataListener actuatorDataListener = null;
    private CoapServerGateway coapServer = null;

    public DeviceDataManager() {
        parseConfig();
        initManagers();
    }

    private void initManagers() {
        try {
            _Logger.info("Initializing DeviceDataManager...");
            if (enableCoapServer) {
                coapServer = new CoapServerGateway(this);
                _Logger.info("CoAP Server initialized.");
            }
            if (enableMqttClient) {
                mqttClient = new MqttClientConnector(); // fixed constructor
                mqttClient.setDataMessageListener(this); // explicitly set listener
                _Logger.info("MQTT Client initialized.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Error initializing components.", e);
        }
    }

    private void parseConfig() {
        ConfigUtil configUtil = ConfigUtil.getInstance();

        try {
            handleHumidityChangeOnDevice = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

            float cfgNominal = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");
            if (cfgNominal > 0.0f) nominalHumiditySetting = cfgNominal;

            float cfgFloor = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");
            if (cfgFloor >= 0.0f) triggerHumidifierFloor = cfgFloor;

            float cfgCeiling = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");
            if (cfgCeiling >= 0.0f) triggerHumidifierCeiling = cfgCeiling;

        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Failed to parse config, using defaults.", e);
        }
    }

    public void startManager() {
        if (enableCoapServer && coapServer != null) coapServer.startServer();
        if (enableMqttClient && mqttClient != null) {
            mqttClient.connectClient();
            mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, ConfigConst.DEFAULT_QOS);
            mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, ConfigConst.DEFAULT_QOS);
        }
    }

    public void stopManager() {
        if (enableCoapServer && coapServer != null) coapServer.stopServer();
        if (enableMqttClient && mqttClient != null) mqttClient.disconnectClient();
    }

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resource, String msg) {
        if (msg == null || msg.isEmpty()) return false;

        try {
            switch (resource) {
                case CDA_SENSOR_MSG_RESOURCE: {
                    SensorData data = DataUtil.getInstance().jsonToSensorData(msg);
                    if (data != null) return handleSensorMessage(resource, data);
                    break;
                }
                case CDA_ACTUATOR_RESPONSE_RESOURCE: {
                    ActuatorData data = DataUtil.getInstance().jsonToActuatorData(msg);
                    if (data != null) return handleActuatorCommandResponse(resource, data);
                    break;
                }
                case CDA_SYSTEM_PERF_MSG_RESOURCE: {
                    SystemPerformanceData data = DataUtil.getInstance().jsonToSystemPerformanceData(msg);
                    if (data != null) return handleSystemPerformanceMessage(resource, data);
                    break;
                }
                default:
                    _Logger.fine("Unhandled resource: " + resource);
            }
        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Failed to parse message: " + e.getMessage(), e);
        }

        return false;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resource, SensorData data) {
        if (data != null) handleHumiditySensorAnalysis(data);
        return true;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resource, ActuatorData data) {
        // currently not used
        return true;
    }

    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resource, ActuatorData data) {
        // currently not used
        return true;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resource, SystemPerformanceData data) {
        // currently not used
        return true;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener) {
        this.actuatorDataListener = listener;
    }

    private void handleHumiditySensorAnalysis(SensorData data) {
        if (!handleHumidityChangeOnDevice) return;

        boolean isLow = data.getValue() < triggerHumidifierFloor;
        boolean isHigh = data.getValue() > triggerHumidifierCeiling;

        if (isLow || isHigh) {
            ActuatorData ad = new ActuatorData();
            ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
            ad.setLocationID(data.getLocationID());
            ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
            ad.setValue(nominalHumiditySetting);
            ad.setCommand(isLow ? ConfigConst.ON_COMMAND : ConfigConst.OFF_COMMAND);

            sendActuatorCommandToCda(ad);
        }
    }

    private void sendActuatorCommandToCda(ActuatorData data) {
        if (actuatorDataListener != null) actuatorDataListener.onActuatorDataUpdate(data);

        if (enableMqttClient && mqttClient != null) {
            try {
                String jsonData = DataUtil.getInstance().actuatorDataToJson(data);
                mqttClient.publishMessage(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, jsonData, ConfigConst.DEFAULT_QOS);
            } catch (Exception e) {
                _Logger.log(Level.WARNING, "Failed to send actuator command via MQTT.", e);
            }
        }
    }
}
