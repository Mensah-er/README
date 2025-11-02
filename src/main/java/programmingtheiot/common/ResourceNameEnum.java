package programmingtheiot.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Lab 8: Standard resource names used across CDA and GDA.
 */
public enum ResourceNameEnum {

    // CDA resources
    CDA_SENSOR_MSG(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.SENSOR_MSG, false, false),
    CDA_ACTUATOR_CMD(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.ACTUATOR_CMD, false, true),
    CDA_UPDATE_NOTIFICATIONS(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.UPDATE_NOTIFICATIONS_MSG, false, true),
    CDA_SYSTEM_PERF_MSG(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.SYSTEM_PERF_MSG, false, false),

    // GDA resources
    GDA_SENSOR_MSG(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, ConfigConst.SENSOR_MSG, true, false),
    GDA_ACTUATOR_CMD(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, ConfigConst.ACTUATOR_CMD, true, true),
    GDA_UPDATE_NOTIFICATIONS(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, ConfigConst.UPDATE_NOTIFICATIONS_MSG, true, true),
    GDA_SYSTEM_PERF_MSG(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, ConfigConst.SYSTEM_PERF_MSG, true, false),

    // Management status and command resources (used by tests)
    GDA_MGMT_STATUS_MSG_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, ConfigConst.MGMT_STATUS_MSG, true, false),
    GDA_MGMT_STATUS_CMD_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, ConfigConst.MGMT_STATUS_CMD, true, true),
    CDA_MGMT_STATUS_MSG_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.MGMT_STATUS_MSG, false, false),
    CDA_MGMT_STATUS_CMD_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.MGMT_STATUS_CMD, false, true),

    // Actuator response alias for test compatibility
    CDA_ACTUATOR_RESPONSE_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.ACTUATOR_RESPONSE, false, true),

    // --- Aliases for older / test constants ---
    CDA_SENSOR_MSG_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.SENSOR_MSG, false, false),
    CDA_SYSTEM_PERF_MSG_RESOURCE(ConfigConst.PRODUCT_NAME, ConfigConst.CONSTRAINED_DEVICE, ConfigConst.SYSTEM_PERF_MSG, false, false),

    // Default fallback
    DEFAULT(ConfigConst.PRODUCT_NAME, ConfigConst.GATEWAY_DEVICE, "DEFAULT", true, false);

    private static final HashMap<String, ResourceNameEnum> lookupMap = new HashMap<>();

    static {
        for (ResourceNameEnum r : ResourceNameEnum.values()) {
            lookupMap.put(r.getResourceName(), r);
        }
    }

    private final String productName;
    private final String deviceName;
    private final String resourceType;
    private final String resourceName;
    private final boolean isLocalToGDA;
    private final boolean isObservable;

    private ResourceNameEnum(String productName, String deviceName, String resourceType,
                             boolean isLocalToGDA, boolean isObservable) {
        this.productName = productName;
        this.deviceName = deviceName;
        this.resourceType = resourceType;
        this.resourceName = productName + "/" + deviceName + "/" + resourceType;
        this.isLocalToGDA = isLocalToGDA;
        this.isObservable = isObservable;
    }

    public String getProductName() { return productName; }
    public String getDeviceName() { return deviceName; }
    public String getResourceType() { return resourceType; }
    public String getResourceName() { return resourceName; }
    public boolean isLocalToGDA() { return isLocalToGDA; }
    public boolean isObservable() { return isObservable; }

    public List<String> getResourceNameChain() {
        String[] parts = resourceName.split("/");
        List<String> list = new ArrayList<>();
        for (String p : parts) list.add(p);
        return list;
    }

    public static ResourceNameEnum getEnumFromValue(String valStr) {
        if (valStr != null && lookupMap.containsKey(valStr)) {
            return lookupMap.get(valStr);
        }
        return DEFAULT;
    }
}
