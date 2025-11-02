package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import programmingtheiot.gda.app.DeviceDataManager;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.IDataMessageListener;

import java.util.logging.Logger;

/**
 * Generic CoAP resource handler for both CDA and GDA resources.
 * Supports Lab 8 requirements.
 */
public class GenericCoapResourceHandler extends CoapResource {
    private static final Logger _Logger = Logger.getLogger(GenericCoapResourceHandler.class.getName());

    private DeviceDataManager devDataMgr = null;
    private ResourceNameEnum resource = null;

    /**
     * Constructor for String resource name (legacy / simple usage)
     */
    public GenericCoapResourceHandler(String resourceName, DeviceDataManager devDataMgr) {
        super(resourceName);
        this.devDataMgr = devDataMgr;

        try {
            this.resource = ResourceNameEnum.valueOf(resourceName.toUpperCase());
        } catch (Exception e) {
            this.resource = ResourceNameEnum.DEFAULT;
            _Logger.warning("Invalid resource name: " + resourceName + ", using DEFAULT instead.");
        }

        getAttributes().setTitle("Generic CoAP Resource Handler for " + resourceName);
        _Logger.info("GenericCoapResourceHandler created for resource: " + resourceName);
    }

    /**
     * Constructor for ResourceNameEnum usage (preferred for CoapServerGateway)
     */
    public GenericCoapResourceHandler(IDataMessageListener listener, ResourceNameEnum resource) {
        super(resource.getResourceName());

        if (listener instanceof DeviceDataManager) {
            this.devDataMgr = (DeviceDataManager) listener;
        }

        this.resource = resource;

        getAttributes().setTitle("Generic CoAP Resource Handler for " + resource.getResourceName());
        _Logger.info("GenericCoapResourceHandler created for resource: " + resource.getResourceName());
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        _Logger.info("Received CoAP GET request.");
        exchange.respond("GDA CoAP Resource Active");
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        String payload = exchange.getRequestText();
        _Logger.info("Received CoAP POST with payload: " + payload);

        if (devDataMgr != null && resource != null) {
            devDataMgr.handleIncomingMessage(resource, payload);
        }

        exchange.respond("POST received: " + payload);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        String payload = exchange.getRequestText();
        _Logger.info("Received CoAP PUT with payload: " + payload);
        exchange.respond("PUT received: " + payload);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        _Logger.info("Received CoAP DELETE request.");
        exchange.respond("DELETE received");
    }
}
