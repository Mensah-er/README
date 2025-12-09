package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ConfigUtil;

/**
 * CoAP client implementation for the GDA.
 * Fully compatible with Lab 10 performance tests.
 */
public class CoapClientConnector implements IRequestResponseClient
{
    private static final Logger _Logger =
        Logger.getLogger(CoapClientConnector.class.getName());

    private CoapClient clientConn = null;
    private IDataMessageListener dataMsgListener = null;

    // CoAP endpoint configuration
    private String host = "localhost";
    private int port = 5683;
    private boolean enableCON = true;      // Used for Lab 10 tests

    public CoapClientConnector() {
        super();
        initClient();
    }

    private void initClient() {
        ConfigUtil cfg = ConfigUtil.getInstance();

        try { this.host = cfg.getProperty(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, "localhost"); }
        catch (Exception e) { this.host = "localhost"; }

        try { this.port = cfg.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, 5683); }
        catch (Exception e) { this.port = 5683; }

        try { this.enableCON = cfg.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CON_MSGS_KEY); }
        catch (Exception e) { this.enableCON = true; }

        String baseUri = "coap://" + this.host + ":" + this.port;
        this.clientConn = new CoapClient(baseUri);
    }

    private String buildUri(ResourceNameEnum resource) {
        return "coap://" + this.host + ":" + this.port + "/" + resource.getResourceName();
    }

    // ======================================================
    // Interface: IRequestResponseClient
    // ======================================================

    @Override
    public void setEndpointPath(ResourceNameEnum resource) {
        if (resource != null) {
            this.clientConn.setURI(buildUri(resource));
        }
    }

    @Override
    public void clearEndpointPath() {
        this.clientConn.setURI("coap://" + this.host + ":" + this.port);
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
    public boolean sendGetRequest(ResourceNameEnum resource, String name, boolean useCON, int timeout) {
        try {
            setEndpointPath(resource);

            Request req = new Request(CoAP.Code.GET, (useCON ? CoAP.Type.CON : CoAP.Type.NON));
            CoapResponse resp = this.clientConn.advanced(req);

            if (resp != null && this.dataMsgListener != null) {
                this.dataMsgListener.handleIncomingMessage(resource, resp.getResponseText());
            }

            return resp != null;
        }
        catch (Exception e) {
            _Logger.warning("GET request failed: " + e.getMessage());
            return false;
        }
        finally {
            clearEndpointPath();
        }
    }

    @Override
    public boolean sendPostRequest(ResourceNameEnum resource, String name, boolean useCON,
                                   String payload, int timeout) {
        try {
            setEndpointPath(resource);

            Request req = new Request(CoAP.Code.POST, (useCON ? CoAP.Type.CON : CoAP.Type.NON));
            req.setPayload(payload);

            CoapResponse resp = this.clientConn.advanced(req);

            if (resp != null && this.dataMsgListener != null) {
                this.dataMsgListener.handleIncomingMessage(resource, resp.getResponseText());
            }

            return resp != null;
        }
        catch (Exception e) {
            _Logger.warning("POST request failed: " + e.getMessage());
            return false;
        }
        finally {
            clearEndpointPath();
        }
    }

    @Override
    public boolean sendPutRequest(ResourceNameEnum resource, String name, boolean useCON,
                                  String payload, int timeout) {
        try {
            setEndpointPath(resource);

            Request req = new Request(CoAP.Code.PUT, (useCON ? CoAP.Type.CON : CoAP.Type.NON));
            req.setPayload(payload);

            CoapResponse resp = this.clientConn.advanced(req);

            if (resp != null && this.dataMsgListener != null) {
                this.dataMsgListener.handleIncomingMessage(resource, resp.getResponseText());
            }

            return resp != null;
        }
        catch (Exception e) {
            _Logger.warning("PUT request failed: " + e.getMessage());
            return false;
        }
        finally {
            clearEndpointPath();
        }
    }

    @Override
    public boolean sendDeleteRequest(ResourceNameEnum resource, String name, boolean useCON, int timeout) {
        try {
            setEndpointPath(resource);

            Request req = new Request(CoAP.Code.DELETE, (useCON ? CoAP.Type.CON : CoAP.Type.NON));
            CoapResponse resp = this.clientConn.advanced(req);

            if (resp != null && this.dataMsgListener != null) {
                this.dataMsgListener.handleIncomingMessage(resource, resp.getResponseText());
            }

            return resp != null;
        }
        catch (Exception e) {
            _Logger.warning("DELETE request failed: " + e.getMessage());
            return false;
        }
        finally {
            clearEndpointPath();
        }
    }

    @Override
    public boolean sendDiscoveryRequest(int timeout) {
        try {
            this.clientConn.setURI("coap://" + this.host + ":" + this.port + "/.well-known/core");
            CoapResponse resp = this.clientConn.get();

            if (resp != null) {
                _Logger.info("Discovery response: " + resp.getResponseText());
                return true;
            }
        }
        catch (Exception e) {
            _Logger.warning("Discovery request failed: " + e.getMessage());
        }
        finally {
            clearEndpointPath();
        }

        return false;
    }

    @Override
    public boolean startObserver(ResourceNameEnum resource, String name, int ttl) {
        try {
            setEndpointPath(resource);

            this.clientConn.observe(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse resp) {
                    if (resp != null && dataMsgListener != null) {
                        dataMsgListener.handleIncomingMessage(resource, resp.getResponseText());
                    }
                }

                @Override
                public void onError() {
                    _Logger.warning("Observer failed for resource: " + resource.getResourceName());
                }
            });

            return true;
        }
        catch (Exception e) {
            _Logger.warning("Observer setup failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean stopObserver(ResourceNameEnum resource, String name, int timeout) {
        try {
            this.clientConn.shutdown();
            return true;
        }
        catch (Exception e) {
            _Logger.warning("Stop observer failed: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------
    // Helper: connect / disconnect client
    // ---------------------------------------------------------
    public void connectClient() {
        if (this.clientConn == null) initClient();
    }

    public void disconnectClient() {
        if (this.clientConn != null) {
            this.clientConn.shutdown();
            this.clientConn = null;
        }
    }

    // Optional getters/setters
    public void setUseConMessages(boolean useCon) { this.enableCON = useCon; }
    public boolean getUseConMessages() { return this.enableCON; }
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
}
