package programmingtheiot.gda.system;

import java.util.logging.Logger;
import programmingtheiot.common.ConfigConst;
/**
 * Base class for system utilization tasks.
 * Sub-classes should implement getTelemetryValue() to provide
 * the appropriate utilization metric.
 */
public abstract class BaseSystemUtilTask
{
    // Logger (changed to protected so subclasses can access)
    protected static final Logger _Logger =
        Logger.getLogger(BaseSystemUtilTask.class.getName());

    // private variables
    private String name = ConfigConst.NOT_SET;
    private int typeID = ConfigConst.DEFAULT_TYPE_ID;

    // Constructor
    public BaseSystemUtilTask(String name, int typeID)
    {
        super();

        if (name != null) {
            this.name = name;
        }

        this.typeID = typeID;
    }

    // Getters
    public String getName()
    {
        return this.name;
    }

    public int getTypeID()
    {
        return this.typeID;
    }

    /**
     * Template method for telemetry value retrieval.
     * Sub-classes must implement this to provide specific utilization.
     *
     * @return float
     */
    public abstract float getTelemetryValue();
}
