package programmingtheiot.gda.system;

import java.io.File;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;

/**
 * Task to measure disk utilization.
 */
public class SystemDiskUtilTask extends BaseSystemUtilTask
{
    private static final Logger _Logger =
        Logger.getLogger(SystemDiskUtilTask.class.getName());

    public SystemDiskUtilTask()
    {
        super(ConfigConst.NOT_SET, ConfigConst.DISK_UTIL_TYPE);
    }

    @Override
    public float getTelemetryValue()
    {
        try {
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();

            if (totalSpace <= 0) return 0.0f;

            float used = ((float)(totalSpace - freeSpace) / totalSpace) * 100.0f;

            _Logger.fine("Disk used: " + used + "%");
            return used;
        } catch (Exception e) {
            _Logger.warning("Failed to get disk utilization: " + e.getMessage());
            return 0.0f;
        }
    }
}
