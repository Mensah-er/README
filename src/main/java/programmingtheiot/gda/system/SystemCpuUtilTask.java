package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Logger;
import programmingtheiot.common.ConfigConst;

public class SystemCpuUtilTask extends BaseSystemUtilTask
{
    // Use protected Logger from BaseSystemUtilTask
    private static final Logger _Logger = Logger.getLogger(SystemCpuUtilTask.class.getName());

    public SystemCpuUtilTask()
    {
        super(ConfigConst.NOT_SET, ConfigConst.CPU_UTIL_TYPE);
    }

    @Override
    public float getTelemetryValue()
    {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        double cpuLoad = -1.0;

        try {
            // Only works on some JVMs; returns 0.0-1.0
            cpuLoad = (double) osBean.getSystemLoadAverage() / osBean.getAvailableProcessors();
        } catch (Exception e) {
            _Logger.warning("Failed to get CPU load: " + e.getMessage());
        }

        float cpuUtil = (float) cpuLoad;
        _Logger.fine("CPU used: " + cpuUtil);
        return cpuUtil;
    }
}
