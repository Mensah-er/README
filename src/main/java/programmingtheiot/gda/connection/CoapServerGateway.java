package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.UpdateSystemPerformanceResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateTelemetryResourceHandler;
import programmingtheiot.gda.connection.handlers.GetActuatorCommandResourceHandler;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;

public class CoapServerGateway
{
    private static final Logger _Logger =
        Logger.getLogger(CoapServerGateway.class.getName());

    private CoapServer coapServer = null;
    private IDataMessageListener dataMsgListener = null;

    public CoapServerGateway(IDataMessageListener dataMsgListener)
    {
        this.dataMsgListener = dataMsgListener;

        // Initialize CoAP server with all required resources
        initServer(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
            ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
            ResourceNameEnum.CDA_UPDATE_NOTIFICATIONS_MSG,
            ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE
        );
    }

    private void initServer(ResourceNameEnum... resources)
    {
        try {
            this.coapServer = new CoapServer();

            if (resources != null) {
                for (ResourceNameEnum r : resources) {
                    addResource(r);
                }
            }

            // Specific handlers
            UpdateSystemPerformanceResourceHandler sysHandler =
                new UpdateSystemPerformanceResourceHandler("SystemPerfMsg");
            sysHandler.setDataMessageListener(this.dataMsgListener);
            coapServer.add(sysHandler);

            UpdateTelemetryResourceHandler telemHandler =
                new UpdateTelemetryResourceHandler("SensorMsg");
            telemHandler.setDataMessageListener(this.dataMsgListener);
            coapServer.add(telemHandler);

            GetActuatorCommandResourceHandler actHandler =
                new GetActuatorCommandResourceHandler("ActuatorCmd");
            coapServer.add(actHandler);

            _Logger.info("CoAP Server initialized successfully with all resource handlers.");
        }
        catch (Exception e) {
            _Logger.log(Level.SEVERE, "Error initializing CoAP server.", e);
        }
    }

    private void addResource(ResourceNameEnum resource)
    {
        if (resource != null && coapServer != null) {
            try {
                // Use ResourceNameEnum constructor instead of String
                CoapResource handler = new GenericCoapResourceHandler(this.dataMsgListener, resource);
                coapServer.add(handler);
                _Logger.info("Added CoAP resource: " + resource.getResourceName());
            } catch (Exception e) {
                _Logger.log(Level.WARNING, "Failed to add resource handler for: " + resource, e);
            }
        }
    }

    public boolean startServer()
    {
        try {
            if (this.coapServer != null) {
                this.coapServer.start();

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

    public void setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }
}
