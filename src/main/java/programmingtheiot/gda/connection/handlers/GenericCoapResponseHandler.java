package programmingtheiot.gda.connection.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.OptionSet;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Generic CoAP response handler for GDA.
 * Handles asynchronous CoAP responses from CDA or cloud clients.
 * Fully aligned with Lab 8 specifications.
 */
public class GenericCoapResponseHandler implements CoapHandler
{
	private static final Logger _Logger =
		Logger.getLogger(GenericCoapResponseHandler.class.getName());

	private IDataMessageListener dataMsgListener = null;
	private ResourceNameEnum resource = null;

	public GenericCoapResponseHandler()
	{
		this(null, null);
	}

	public GenericCoapResponseHandler(IDataMessageListener listener)
	{
		this(listener, null);
	}

	public GenericCoapResponseHandler(IDataMessageListener listener, ResourceNameEnum resource)
	{
		super();
		this.dataMsgListener = listener;
		this.resource = resource;
		_Logger.fine("GenericCoapResponseHandler initialized. Listener set: " + (listener != null));
	}

	@Override
	public void onLoad(CoapResponse response)
	{
		if (response != null) {
			try {
				OptionSet options = response.getOptions();
				String payload = response.getResponseText();
				_Logger.info("[CoAP] Response received. Code: " + response.getCode());
				_Logger.fine("[CoAP] Payload: " + payload);

				if (this.dataMsgListener != null && payload != null) {
					this.dataMsgListener.handleIncomingMessage(this.resource, payload);
				} else {
					_Logger.warning("[CoAP] No listener or payload to process response.");
				}
			}
			catch (Exception e) {
				_Logger.log(Level.SEVERE, "[CoAP] Error processing CoAP response.", e);
			}
		} else {
			_Logger.warning("[CoAP] Received null CoAP response.");
		}
	}

	@Override
	public void onError()
	{
		_Logger.warning("[CoAP] Error during CoAP response handling.");
	}
}
