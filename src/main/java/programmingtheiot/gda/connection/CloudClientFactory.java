package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

/**
 * Factory for creating ICloudClient instances based on PiotConfig.props.
 */
public class CloudClientFactory
{
    private static final Logger _Logger =
        Logger.getLogger(CloudClientFactory.class.getName());

    private static final CloudClientFactory _Instance =
        new CloudClientFactory();

    private CloudClientFactory() {
        super();
    }

    public static CloudClientFactory getInstance()
    {
        return _Instance;
    }

    public ICloudClient createCloudClient()
    {ICloudClient cloudClient = null;

    ConfigUtil cfg = ConfigUtil.getInstance();

    String cloudSvcName =
        cfg.getProperty(
            ConfigConst.CLOUD_GATEWAY_SERVICE,
            ConfigConst.CLOUD_SERVICE_NAME_KEY);

    if (cloudSvcName != null && cloudSvcName.trim().length() > 0) {
        _Logger.info(
            "Attempting to instance cloud client using cloud service name: "
            + cloudSvcName);

        if (cloudSvcName.equalsIgnoreCase(ConfigConst.UBIDOTS_CLOUD_SVC_NAME)) {
            cloudClient = new UbidotsCloudClientConnector();
        } else {
            _Logger.warning(
                "Cloud service name not recognized: " + cloudSvcName);
        }
    }

    if (cloudClient == null) {
        _Logger.warning(
            "No valid cloud service provider specified. Cloud client NOT created.");
    }

    return cloudClient;
    }
}
