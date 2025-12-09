package programmingtheiot.common;

import java.util.HashMap;
import java.util.Map;

public enum ResourceNameEnum {


    // --- Default fallback ---
    DEFAULT("DEFAULT"),
    
    // --- CDA topics (unchanged) ---
    CDA_UPDATE_NOTIFICATIONS_MSG(ConfigConst.UPDATE_NOTIFICATIONS_RESOURCE),
    CDA_ACTUATOR_CMD_RESOURCE(ConfigConst.CDA_ACTUATOR_CMD_MSG_RESOURCE),
    CDA_ACTUATOR_RESPONSE_RESOURCE(ConfigConst.CDA_ACTUATOR_RESPONSE_MSG_RESOURCE),
    CDA_MGMT_STATUS_MSG_RESOURCE(ConfigConst.CDA_MGMT_STATUS_MSG_RESOURCE),
    CDA_MGMT_STATUS_CMD_RESOURCE(ConfigConst.CDA_MGMT_STATUS_CMD_RESOURCE),
    CDA_MEDIA_DATA_MSG_RESOURCE(ConfigConst.MEDIA_MSG),
    CDA_REGISTRATION_REQUEST_RESOURCE(ConfigConst.RESOURCE_REGISTRATION_REQUEST),
    CDA_SENSOR_MSG_RESOURCE(ConfigConst.CDA_SENSOR_DATA_MSG_RESOURCE),
    CDA_SYSTEM_PERF_MSG_RESOURCE(ConfigConst.CDA_SYSTEM_PERF_MSG_RESOURCE),

    // --- GDA topics: now reuse CDA constants for exact match ---
    GDA_SENSOR_DATA_MSG_RESOURCE(ConfigConst.CDA_SENSOR_DATA_MSG_RESOURCE),
    GDA_ACTUATOR_CMD_RESOURCE(ConfigConst.CDA_ACTUATOR_CMD_MSG_RESOURCE),
    GDA_ACTUATOR_RESPONSE_RESOURCE(ConfigConst.CDA_ACTUATOR_RESPONSE_MSG_RESOURCE),
    GDA_MGMT_STATUS_MSG_RESOURCE(ConfigConst.CDA_MGMT_STATUS_MSG_RESOURCE),
    GDA_MGMT_CMD_MSG_RESOURCE(ConfigConst.CDA_MGMT_STATUS_CMD_RESOURCE),
    GDA_UPDATE_NOTIFICATIONS_MSG_RESOURCE(ConfigConst.UPDATE_NOTIFICATIONS_RESOURCE),
    GDA_REGISTRATION_REQUEST_RESOURCE(ConfigConst.RESOURCE_REGISTRATION_REQUEST),
    GDA_SYSTEM_PERF_MSG_RESOURCE(ConfigConst.CDA_SYSTEM_PERF_MSG_RESOURCE),

    // --- Backwards-compatible aliases for GDA ---
    GDA_SENSOR_MSG(ConfigConst.CDA_SENSOR_DATA_MSG_RESOURCE),
    GDA_ACTUATOR_CMD(ConfigConst.CDA_ACTUATOR_CMD_MSG_RESOURCE),
    GDA_SYSTEM_PERF_MSG(ConfigConst.CDA_SYSTEM_PERF_MSG_RESOURCE);

    // ----------------------
    // Private field + constructor
    // ----------------------
    private final String resourceName;

    private ResourceNameEnum(String resourceName) {
        this.resourceName = resourceName;
    }

    // ----------------------
    // Getter method
    // ----------------------
    public String getResourceName() {
        return this.resourceName;
    }

    // ----------------------
    // Static lookup for getEnumFromValue
    // ----------------------
    private static final Map<String, ResourceNameEnum> lookup = new HashMap<>();
    static {
        for (ResourceNameEnum r : ResourceNameEnum.values()) {
            lookup.put(r.getResourceName(), r);
        }
    }

    public static ResourceNameEnum getEnumFromValue(String value) {
        return lookup.get(value); // returns null if not found
    }
}
