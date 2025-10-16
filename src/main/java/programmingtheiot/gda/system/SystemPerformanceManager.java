package programmingtheiot.gda.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Manages system performance monitoring by retrieving CPU, Memory, and Disk utilization.
 */
public class SystemPerformanceManager
{
    // Logger
    private static final Logger _Logger =
        Logger.getLogger(SystemPerformanceManager.class.getName());

    // Private variables
    private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
    private String locationID = ConfigConst.NOT_SET;
    private IDataMessageListener dataMsgListener = null;

    private ScheduledExecutorService schedExecSvc = null;
    private SystemCpuUtilTask sysCpuUtilTask = null;
    private SystemMemUtilTask sysMemUtilTask = null;
    private SystemDiskUtilTask sysDiskUtilTask = null;

    private Runnable taskRunner = null;
    private boolean isStarted = false;

    // Constructor
    public SystemPerformanceManager()
    {
        // Load location ID from config
        this.locationID =
            ConfigUtil.getInstance().getProperty(
                ConfigConst.GATEWAY_DEVICE,
                ConfigConst.LOCATION_ID_PROP,
                ConfigConst.NOT_SET
            );

        // Set poll rate (seconds)
        this.pollRate = 30;

        // Initialize scheduled executor and telemetry tasks
        this.schedExecSvc   = Executors.newScheduledThreadPool(1);
        this.sysCpuUtilTask = new SystemCpuUtilTask();
        this.sysMemUtilTask = new SystemMemUtilTask();
        this.sysDiskUtilTask = new SystemDiskUtilTask();

        // Task runner to periodically collect telemetry
        this.taskRunner = this::handleTelemetry;

        _Logger.info("SystemPerformanceManager initialized.");
    }

    // Retrieve CPU, Memory, and Disk telemetry
    public void handleTelemetry()
    {
        float cpuUtil = this.sysCpuUtilTask.getTelemetryValue();
        float memUtil = this.sysMemUtilTask.getTelemetryValue();
        float diskUtil = this.sysDiskUtilTask.getTelemetryValue();

        // Log telemetry values
        _Logger.info("CPU utilization: " + cpuUtil +
                     ", Mem utilization: " + memUtil +
                     ", Disk utilization: " + diskUtil);

        // Store data in SystemPerformanceData
        SystemPerformanceData spd = new SystemPerformanceData();
        spd.setLocationID(this.locationID);
        spd.setCpuUtilization(cpuUtil);
        spd.setMemoryUtilization(memUtil);
        spd.setDiskUtilization(diskUtil);

        // Notify listener if available
        if (this.dataMsgListener != null) {
            _Logger.info("Sending SystemPerformanceData to listener...");
            this.dataMsgListener.handleSystemPerformanceMessage(
                ResourceNameEnum.GDA_SYSTEM_PERF_MSG_RESOURCE,
                spd
            );
        } else {
            _Logger.fine("No data message listener set. Data not sent.");
        }
    }

    // Register listener for callbacks
    public void setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            _Logger.info("Data message listener successfully set.");
        }
    }

    // Start scheduled monitoring
    public boolean startManager()
    {
        if (!this.isStarted) {
            _Logger.info("Starting SystemPerformanceManager telemetry collection thread.");

            this.schedExecSvc.scheduleAtFixedRate(
                this.taskRunner,
                1L,             // initial delay 1 second
                this.pollRate,  // repeat every pollRate seconds
                TimeUnit.SECONDS
            );

            this.isStarted = true;
        } else {
            _Logger.info("SystemPerformanceManager already started.");
        }

        return this.isStarted;
    }

    // Stop scheduled monitoring
    public boolean stopManager()
    {
        if (this.schedExecSvc != null && !this.schedExecSvc.isShutdown()) {
            this.schedExecSvc.shutdownNow();
        }

        this.isStarted = false;
        _Logger.info("SystemPerformanceManager stopped.");

        return true;
    }
}
