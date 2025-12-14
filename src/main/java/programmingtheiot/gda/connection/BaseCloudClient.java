package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * BaseCloudClient
 *
 * Abstract base class for all cloud client implementations.
 * Provides shared configuration and lifecycle handling.
 */
public abstract class BaseCloudClient implements ICloudClient
{
	protected final Logger _Logger =
		Logger.getLogger(this.getClass().getName());

	protected String cloudGatewaySection = ConfigConst.CLOUD_GATEWAY_SERVICE;
	protected IDataMessageListener dataMsgListener = null;

	protected BaseCloudClient()
	{
		// default uses Cloud.GatewayService
	}

	protected BaseCloudClient(String cloudGatewaySection)
	{
		if (cloudGatewaySection != null) {
			this.cloudGatewaySection = cloudGatewaySection;
		}
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
		return true;
	}

	// -------------------------------------------------
	// Default NO-OPs (override in subclasses)
	// -------------------------------------------------

	@Override
	public boolean sendEdgeDataToCloud(
		ResourceNameEnum resource, SensorData data)
	{
		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(
		ResourceNameEnum resource, SystemPerformanceData data)
	{
		return false;
	}

	@Override
	public boolean subscribeToCloudEvents(ResourceNameEnum resource)
	{
		return false;
	}

	@Override
	public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
	{
		return false;
	}
}
