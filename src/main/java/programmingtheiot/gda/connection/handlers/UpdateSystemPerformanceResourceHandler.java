package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.DataUtil;

/**
 * Handles PUT requests from the CDA that carry SystemPerformanceData.
 * Fully compliant with PIOT-GDA-08-002.
 */
public class UpdateSystemPerformanceResourceHandler extends CoapResource
{
	private static final Logger _Logger =
		Logger.getLogger(UpdateSystemPerformanceResourceHandler.class.getName());

	private IDataMessageListener dataMsgListener = null;

	public UpdateSystemPerformanceResourceHandler(String resourceName)
	{
		super(resourceName);
		getAttributes().setTitle("Update System Performance Resource Handler");
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
			_Logger.info("Received SystemPerformance PUT: " + jsonData);

			SystemPerformanceData sysPerfData =
				DataUtil.getInstance().jsonToSystemPerformanceData(jsonData);

			if (dataMsgListener != null) {
				dataMsgListener.handleSystemPerformanceMessage(
					ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
				code = ResponseCode.CHANGED;
			} else {
				code = ResponseCode.CONTINUE;
			}
		} catch (Exception e) {
			_Logger.warning("Failed to process SystemPerformance PUT: " + e.getMessage());
			code = ResponseCode.BAD_REQUEST;
		}

		context.respond(code, "SystemPerformance PUT handled");
	}
}
