package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.interceptors.MessageTracer;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;
import programmingtheiot.gda.connection.handlers.GetActuatorCommandResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateSystemPerformanceResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateTelemetryResourceHandler;

/**
 * CoapServerGateway
 *
 * Provides CoAP server functionality for the Gateway Device Application (GDA).
 * Fully compliant with PIOT-GDA-08-001 through PIOT-GDA-08-003.
 *
 * Responsibilities:
 *  - Instantiate and configure CoAP server (Californium)
 *  - Register all required CoAP resource handlers
 *  - Manage start/stop lifecycle
 */
public class CoapServerGateway
{
    private static final Logger _Logger =
        Logger.getLogger(CoapServerGateway.class.getName());

    private CoapServer coapServer = null;
    private IDataMessageListener dataMsgListener = null;

    /**
     * Constructor - receives reference to DeviceDataManager (as IDataMessageListener).
     */
    public CoapServerGateway(IDataMessageListener dataMsgListener)
    {
        this.dataMsgListener = dataMsgListener;

        // Initialize CoAP server with all required resources
        initServer(
            ResourceNameEnum.GDA_SENSOR_MSG,
            ResourceNameEnum.GDA_ACTUATOR_CMD,
            ResourceNameEnum.GDA_UPDATE_NOTIFICATIONS,
            ResourceNameEnum.GDA_SYSTEM_PERF_MSG
        );
    }

    /**
     * Initializes CoAP server and registers all resource handlers.
     */
    private void initServer(ResourceNameEnum... resources)
    {
        try {
        	this.coapServer = new CoapServer(NetworkConfig.getStandard());

            // ====== Generic Handlers (for all declared ResourceNameEnum values) ======
            if (resources != null) {
                for (ResourceNameEnum r : resources) {
                    addResource(r);
                }
            }

            // ====== Specific Handlers for Lab 8 ======

            // System Performance Handler
            UpdateSystemPerformanceResourceHandler sysHandler =
                new UpdateSystemPerformanceResourceHandler(
                    ResourceNameEnum.GDA_SYSTEM_PERF_MSG.getResourceName());
            sysHandler.setDataMessageListener(this.dataMsgListener);
            this.coapServer.add(sysHandler);

            // Telemetry Handler
            UpdateTelemetryResourceHandler telemHandler =
                new UpdateTelemetryResourceHandler(
                    ResourceNameEnum.GDA_SENSOR_MSG.getResourceName());
            telemHandler.setDataMessageListener(this.dataMsgListener);
            this.coapServer.add(telemHandler);

            // Actuator Command Handler (Observable)
            GetActuatorCommandResourceHandler actHandler =
                new GetActuatorCommandResourceHandler(
                    ResourceNameEnum.GDA_ACTUATOR_CMD.getResourceName());
            this.coapServer.add(actHandler);

            _Logger.info("CoAP Server initialized successfully with all resource handlers.");
        }
        catch (Exception e) {
            _Logger.log(Level.SEVERE, "Error initializing CoAP server.", e);
        }
    }

    /**
     * Adds a generic resource handler for a given ResourceNameEnum entry.
     */
    private void addResource(ResourceNameEnum resource)
    {
        if (resource != null && coapServer != null) {
            try {
                CoapResource handler = new GenericCoapResourceHandler(this.dataMsgListener, resource);
                coapServer.add(handler);
                _Logger.info("Added CoAP resource: " + resource.getResourceName());
            } catch (Exception e) {
                _Logger.log(Level.WARNING, "Failed to add resource handler for: " + resource, e);
            }
        }
    }

    /**
     * Starts the CoAP server.
     * Adds a message tracer to each endpoint for debug visibility.
     */
    public boolean startServer()
    {
        try {
            if (this.coapServer != null) {
                this.coapServer.start();

                // For logging and tracing messages
                for (Endpoint ep : this.coapServer.getEndpoints()) {
                    ep.addInterceptor(new MessageTracer());
                }

                _Logger.info("CoAP server started successfully.");
                return true;
            } else {
                _Logger.warning("CoAP server START failed. Not yet initialized.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to start CoAP server.", e);
        }

        return false;
    }

    /**
     * Stops the CoAP server.
     */
    public boolean stopServer()
    {
        try {
            if (this.coapServer != null) {
                this.coapServer.stop();
                _Logger.info("CoAP server stopped successfully.");
                return true;
            } else {
                _Logger.warning("CoAP server STOP failed. Not yet initialized.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to stop CoAP server.", e);
        }

        return false;
    }

    /**
     * Optional method to set or change the data message listener.
     */
    public void setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }
}
