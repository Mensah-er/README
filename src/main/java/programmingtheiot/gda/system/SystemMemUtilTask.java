package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.logging.Logger;
import programmingtheiot.common.ConfigConst;

public class SystemMemUtilTask extends BaseSystemUtilTask
{
    private static final Logger _Logger = Logger.getLogger(SystemMemUtilTask.class.getName());

    public SystemMemUtilTask()
    {
        super(ConfigConst.NOT_SET, ConfigConst.MEM_UTIL_TYPE);
    }

    @Override
    public float getTelemetryValue()
    {
        MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        double memUsed = (double) memUsage.getUsed();
        double memMax  = (double) memUsage.getMax();

        double memUtil = (memUsed / memMax) * 100.0d;

        _Logger.fine("Mem used: " + memUsed + "; Mem Max: " + memMax + "; Mem Util: " + memUtil);
        return (float) memUtil;
    }
}
