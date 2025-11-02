package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;

/**
 * Observable CoAP resource that lets the CDA GET actuator commands
 * and automatically receive updates when new ActuatorData arrives.
 * Fully compliant with PIOT-GDA-08-003.
 */
public class GetActuatorCommandResourceHandler extends CoapResource implements IActuatorDataListener
{
	private static final Logger _Logger =
		Logger.getLogger(GetActuatorCommandResourceHandler.class.getName());

	private ActuatorData actuatorData = new ActuatorData();

	public GetActuatorCommandResourceHandler(String resourceName)
	{
		super(resourceName);
		getAttributes().setTitle("Get Actuator Command Resource Handler");
		super.setObservable(true);
	}

	@Override
	public boolean onActuatorDataUpdate(ActuatorData data)
	{
		if (data != null) {
			this.actuatorData.updateData(data);
			super.changed();	// notify observers
			_Logger.info("ActuatorData updated: " + data.getValue());
			return true;
		}
		return false;
	}

	@Override
	public void handleGET(CoapExchange context)
	{
		context.accept();
		String jsonData = DataUtil.getInstance().actuatorDataToJson(this.actuatorData);
		_Logger.info("Sending ActuatorData to CDA: " + jsonData);
		context.respond(ResponseCode.CONTENT, jsonData);
	}
}
