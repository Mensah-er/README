package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.CoapServerGateway;
/**
 * DeviceDataManager (GDA)
 *
 * Acts as the core data manager for the Gateway Device Application.
 * Handles incoming messages from CoAP (CDA), MQTT, or other connectors
 * and routes them to appropriate handlers or components.
 *
 * Fully compliant with Lab Module 8.
 */
public class DeviceDataManager implements IDataMessageListener
{
    // Logger
    private static final Logger _Logger =
        Logger.getLogger(DeviceDataManager.class.getName());

    // CoAP server instance
    private CoapServerGateway coapServer = null;

    // Flags
    private boolean enableCoapServer = true;

    /**
     * Default constructor.
     */
    public DeviceDataManager()
    {
        super();
        initManagers();
    }

    /**
     * Initialize communication managers (CoAP, MQTT, etc.)
     */
    private void initManagers()
    {
        try {
            _Logger.info("Initializing DeviceDataManager...");

            if (this.enableCoapServer) {
                this.coapServer = new CoapServerGateway(this);
                _Logger.info("CoAP Server initialized and linked to DeviceDataManager.");
            }

        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Error initializing DeviceDataManager components.", e);
        }
    }

    /**
     * Start all data manager components.
     */
    public void startManager()
    {
        _Logger.info("Starting DeviceDataManager services...");

        if (this.enableCoapServer && this.coapServer != null) {
            this.coapServer.startServer();
        }
    }

    /**
     * Stop all data manager components.
     */
    public void stopManager()
    {
        _Logger.info("Stopping DeviceDataManager services...");

        if (this.enableCoapServer && this.coapServer != null) {
            this.coapServer.stopServer();
        }
    }

    // =========================================================================
    // IDataMessageListener Interface Implementation
    // =========================================================================

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resource, String msg)
    {
        _Logger.info("Received incoming message from resource: " + resource);
        _Logger.fine("Payload: " + msg);
        return true;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resource, SensorData data)
    {
        _Logger.info("Received SensorData message: " + data);
        return true;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resource, ActuatorData data)
    {
        _Logger.info("Received Actuator Command Request: " + data);
        return true;
    }

    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resource, ActuatorData data)
    {
        _Logger.info("Received Actuator Command Response: " + data);
        return true;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resource, SystemPerformanceData data)
    {
        _Logger.info("Received System Performance Data: " + data);
        return true;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        _Logger.info("Setting ActuatorDataListener for: " + name);
    }
}
