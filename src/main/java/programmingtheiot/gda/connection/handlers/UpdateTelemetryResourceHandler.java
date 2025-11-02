package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.DataUtil;

/**
 * Handles PUT requests from the CDA that carry SensorData.
 * Fully compliant with PIOT-GDA-08-002.
 */
public class UpdateTelemetryResourceHandler extends CoapResource
{
	private static final Logger _Logger =
		Logger.getLogger(UpdateTelemetryResourceHandler.class.getName());

	private IDataMessageListener dataMsgListener = null;

	public UpdateTelemetryResourceHandler(String resourceName)
	{
		super(resourceName);
		getAttributes().setTitle("Update Telemetry Resource Handler");
	}

	public void setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) this.dataMsgListener = listener;
	}

	@Override
	public void handlePUT(CoapExchange context)
	{
		ResponseCode code = ResponseCode.NOT_ACCEPTABLE;
		context.accept();

		try {
			String jsonData = context.getRequestText();
			_Logger.info("Received Telemetry PUT: " + jsonData);

			SensorData sensorData =
				DataUtil.getInstance().jsonToSensorData(jsonData);

			if (dataMsgListener != null) {
				dataMsgListener.handleSensorMessage(
					ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
				code = ResponseCode.CHANGED;
			} else {
				code = ResponseCode.CONTINUE;
			}
		} catch (Exception e) {
			_Logger.warning("Failed to process Telemetry PUT: " + e.getMessage());
			code = ResponseCode.BAD_REQUEST;
		}

		context.respond(code, "Telemetry PUT handled");
	}
}
