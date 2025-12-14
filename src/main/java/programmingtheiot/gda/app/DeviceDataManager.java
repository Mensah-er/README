package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.ICloudClient;
//import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CloudClientFactory;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.DataUtil;


public class DeviceDataManager implements IDataMessageListener
{
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManager.class.getName());

    // humidity configurable values
    private float nominalHumiditySetting = 40.0f;
    private float triggerHumidifierFloor = 30.0f;
    private float triggerHumidifierCeiling = 50.0f;
    private int humidityMaxTimePastThreshold = 10;   // seconds

    // tracking state
    private long lastExceptionalTime = 0;
    private boolean humidifierOn = false;

    // features
    private boolean handleHumidityChangeOnDevice = false;
    private boolean enableCoapServer = true;
    private boolean enableMqttClient = false;

    private MqttClientConnector mqttClient = null;
    private IActuatorDataListener actuatorDataListener = null;
    private CoapServerGateway coapServer = null;

    // -------------------------------
    // Lab 11 ADDITION: Cloud client
    // -------------------------------
    private ICloudClient cloudClient = null;


    public DeviceDataManager()
    {
        parseConfig();
        initManagers();

        // REQUIRED BY LAB 11
        
        this.cloudClient = CloudClientFactory.getInstance().createCloudClient();

    }

    private void initManagers()
    {
        try
        {
            _Logger.info("Initializing DeviceDataManager...");

            if (enableCoapServer)
            {
                coapServer = new CoapServerGateway(this);
                _Logger.info("CoAP Server initialized.");
            }

            if (enableMqttClient)
            {
                mqttClient = new MqttClientConnector();
                mqttClient.setDataMessageListener(this);
                _Logger.info("MQTT Client initialized.");
            }
        }
        catch (Exception e)
        {
            _Logger.log(Level.SEVERE, "Init error", e);
        }
    }

    private void parseConfig()
    {
        ConfigUtil cfg = ConfigUtil.getInstance();

        try
        {
            handleHumidityChangeOnDevice =
                cfg.getBoolean(ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

            nominalHumiditySetting =
                cfg.getFloat(ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");

            triggerHumidifierFloor =
                cfg.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");

            triggerHumidifierCeiling =
                cfg.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

            humidityMaxTimePastThreshold =
                cfg.getInteger(ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

        }
        catch (Exception e)
        {
            _Logger.log(Level.WARNING, "Config load error, using defaults.", e);
        }
    }

    public void startManager()
    {
        if (enableCoapServer && coapServer != null)
            coapServer.startServer();

        if (enableMqttClient && mqttClient != null)
        {
            mqttClient.connectClient();
            mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, 0);
            mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, 0);
        }

        // -------------------------------
        // Lab 11 – Cloud uplink enabled
        // -------------------------------
        if (cloudClient != null)
        {
            cloudClient.connectClient();
        }
    }

    public void stopManager()
    {
        if (enableCoapServer && coapServer != null)
            coapServer.stopServer();

        if (enableMqttClient && mqttClient != null)
            mqttClient.disconnectClient();

        // -------------------------------
        // Lab 11 – Cloud shutdown
        // -------------------------------
        if (cloudClient != null)
            cloudClient.disconnectClient();
    }

    // --------------------------------------------------------
    // Incoming CDA message routing
    // --------------------------------------------------------
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resource, String msg)
    {
        if (msg == null || msg.isEmpty()) return false;

        switch (resource)
        {
            case CDA_SENSOR_MSG_RESOURCE:
                SensorData sd = DataUtil.getInstance().jsonToSensorData(msg);
                return handleSensorMessage(resource, sd);

            case CDA_ACTUATOR_RESPONSE_RESOURCE:
                ActuatorData ad = DataUtil.getInstance().jsonToActuatorData(msg);
                return handleActuatorCommandResponse(resource, ad);

            case CDA_SYSTEM_PERF_MSG_RESOURCE:
                SystemPerformanceData spd =
                    DataUtil.getInstance().jsonToSystemPerformanceData(msg);
                return handleSystemPerformanceMessage(resource, spd);

            default:
                _Logger.fine("Unhandled resource: " + resource);
        }

        return false;
    }

    // --------------------------------------------------------
    // SENSOR PROCESSING (Humidity + Cloud Uplink)
    // --------------------------------------------------------
    @Override
    public boolean handleSensorMessage(ResourceNameEnum resource, SensorData data)
    {
        if (data == null) return false;

        _Logger.fine("Humidity sensor received → " + data.getValue() + "%");

        handleHumiditySensorAnalysis(data);

        // -------------------------------
        // Lab 11 – send upstream to cloud
        // -------------------------------
        if (cloudClient != null)
            cloudClient.sendEdgeDataToCloud(resource, data);

        return true;
    }

    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resource, ActuatorData data)
    {
        _Logger.fine("Actuator Response received: " + data);
        return true;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum r, ActuatorData d) { return true; }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum r, SystemPerformanceData d)
    {
        if (d == null) return false;

        _Logger.fine("System Performance message received");

        // -------------------------------
        // Lab 11 – Cloud uplink
        // -------------------------------
        if (cloudClient != null)
            cloudClient.sendEdgeDataToCloud(r, d);

        return true;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        this.actuatorDataListener = listener;
    }

    // --------------------------------------------------------
    // HUMIDITY ANALYSIS (unchanged – required by professor)
    // --------------------------------------------------------
    private void handleHumiditySensorAnalysis(SensorData data)
    {
        if (!handleHumidityChangeOnDevice)
            return;

        float humidity = data.getValue();
        long now = System.currentTimeMillis();

        boolean isLow  = humidity < triggerHumidifierFloor;
        boolean isHigh = humidity > triggerHumidifierCeiling;
        boolean abnormal = isLow || isHigh;

        _Logger.info("[HUMIDITY] Value=" + humidity +
                     " | Range=(" + triggerHumidifierFloor + "-" + triggerHumidifierCeiling + ")" +
                     " | abnormal=" + abnormal);

        if (!abnormal)
        {
            if (humidifierOn &&
                (now - lastExceptionalTime >= humidityMaxTimePastThreshold * 1000))
            {
                _Logger.info(">>> HUMIDIFIER OFF TRIGGERED (humidity normalized) <<<");

                sendActuatorCommandToCda(createHumidityActuatorData(data, ConfigConst.OFF_COMMAND));
                humidifierOn = false;
            }

            lastExceptionalTime = 0;
            return;
        }

        if (lastExceptionalTime == 0)
        {
            lastExceptionalTime = now;
            return;
        }

        if (now - lastExceptionalTime < humidityMaxTimePastThreshold * 1000)
            return;

        if (!humidifierOn)
        {
            _Logger.info(">>> HUMIDIFIER ON TRIGGERED (humidity out of range) <<<");

            sendActuatorCommandToCda(createHumidityActuatorData(data, ConfigConst.ON_COMMAND));
            humidifierOn = true;
        }
    }

    private ActuatorData createHumidityActuatorData(SensorData data, int command)
    {
        ActuatorData ad = new ActuatorData();
        ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
        ad.setLocationID(data.getLocationID());
        ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
        ad.setValue(nominalHumiditySetting);
        ad.setCommand(command);
        return ad;
    }

    private void sendActuatorCommandToCda(ActuatorData data)
    {
        if (actuatorDataListener != null)
            actuatorDataListener.onActuatorDataUpdate(data);

        if (enableMqttClient && mqttClient != null)
        {
            try
            {
                mqttClient.publishMessage(
                    ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
                    DataUtil.getInstance().actuatorDataToJson(data),
                    0
                );
            }
            catch (Exception e)
            {
                _Logger.log(Level.WARNING, "MQTT actuator send failure", e);
            }
        }
    }
}
