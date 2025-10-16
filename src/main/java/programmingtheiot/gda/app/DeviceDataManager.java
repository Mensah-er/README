package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

import programmingtheiot.common.*;
import programmingtheiot.data.*;
import programmingtheiot.gda.connection.*;
import programmingtheiot.gda.system.*;

/**
 * Core data manager for the Gateway Device Application (GDA).
 * Implements IDataMessageListener for processing incoming messages.
 */
@SuppressWarnings("unused")
public class DeviceDataManager implements IDataMessageListener
{
    // Logger
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManager.class.getName());

    // Private fields
    private boolean enableMqttClient = true;
    private boolean enableCoapServer = false;
    private boolean enableCloudClient = false;
    private boolean enablePersistenceClient = false;
    private boolean enableSystemPerf = false;

    private IPubSubClient mqttClient = null;
    private IPubSubClient cloudClient = null;
    private IPersistenceClient persistenceClient = null;
    private CoapServerGateway coapServer = null;
    private SystemPerformanceManager sysPerfMgr = null;

    // Constructor
    public DeviceDataManager()
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();

        this.enableMqttClient =
            configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);

        this.enableCoapServer =
            configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);

        this.enableCloudClient =
            configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);

        this.enablePersistenceClient =
            configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

        initManager();
    }

    // Initialize manager components
    private void initManager()
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();

        this.enableSystemPerf =
            configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);

        if (this.enableSystemPerf) {
            this.sysPerfMgr = new SystemPerformanceManager();
            this.sysPerfMgr.setDataMessageListener(this);
        }

        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            this.mqttClient.setDataMessageListener(this);
        }

        if (this.enableCoapServer) {
            // TODO: Lab Module 8 CoAP server setup
        }

        if (this.enableCloudClient) {
            // TODO: Lab Module 10 cloud client setup
        }

        if (this.enablePersistenceClient) {
            // TODO: Optional Lab Module 5 persistence client setup
        }
    }

    // Start manager
    public void startManager()
    {
        _Logger.info("Starting DeviceDataManager...");

        if (this.mqttClient != null && this.mqttClient.connectClient()) {
            _Logger.info("MQTT client connected.");
            int qos = ConfigConst.DEFAULT_QOS;
            this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
            this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
            this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
            this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
        }

        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.startManager();
        }
    }

    // Stop manager
    public void stopManager()
    {
        _Logger.info("Stopping DeviceDataManager...");

        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.stopManager();
        }

        if (this.mqttClient != null) {
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);

            if (this.mqttClient.disconnectClient()) {
                _Logger.info("MQTT client disconnected.");
            }
        }
    }

    // IDataMessageListener implementations
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
    {
        if (data != null) {
            _Logger.info("Actuator response: " + data.getName());
            if (data.hasError()) _Logger.warning("Error in actuator data.");
            return true;
        }
        return false;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
    {
        _Logger.info("Actuator request received for: " + (data != null ? data.getName() : "null"));
        return false;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        _Logger.info("Setting actuator data listener for: " + name);
    }

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
    {
        if (msg != null) {
            _Logger.info("Incoming message: " + msg);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
    {
        if (data != null) {
            _Logger.info("Sensor message: " + data.getName());
            if (data.hasError()) _Logger.warning("Error in sensor data.");
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
    {
        if (data != null) {
            _Logger.info("System performance: " + data.getName());
            if (data.hasError()) _Logger.warning("Error in system performance data.");
            return true;
        }
        return false;
    }
}
