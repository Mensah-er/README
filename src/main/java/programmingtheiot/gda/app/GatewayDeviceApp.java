package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main GDA application.
 * DeviceDataManager handles system performance and messaging.
 */
public class GatewayDeviceApp
{
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceApp.class.getName());

    private DeviceDataManager dataMgr = null;

    public GatewayDeviceApp(String[] args)
    {
        super();
        _Logger.info("Initializing GDA...");
        parseArgs(args);
        this.dataMgr = new DeviceDataManager();
    }

    public void startApp()
    {
        _Logger.info("Starting GDA...");

        try {
            if (this.dataMgr != null) {
                this.dataMgr.startManager();
            }
            _Logger.info("GDA started successfully.");
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
            stopApp(-1);
        }
    }

    public void stopApp(int code)
    {
        _Logger.info("Stopping GDA...");

        try {
            if (this.dataMgr != null) {
                this.dataMgr.stopManager();
            }
            _Logger.log(
                Level.INFO,
                "GDA stopped successfully with exit code {0}.",
                code
            );
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to cleanly stop GDA. Exiting.", e);
        }

        System.exit(code);
    }

    /**
     * Stops GDA without calling System.exit(), useful for tests.
     */
    public void stopAppWithoutExit()
    {
        _Logger.info("Stopping GDA without exiting...");

        try {
            if (this.dataMgr != null) {
                this.dataMgr.stopManager();
            }
            _Logger.info("GDA stopped successfully (without exit).");
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to cleanly stop GDA.", e);
        }
    }

    private void initConfig(String fileName)
    {
        _Logger.info(
            "Attempting to load configuration: " +
            (fileName == null ? "Default." : fileName)
        );
    }

    private void parseArgs(String[] args)
    {
        _Logger.info("No command line args to parse.");
        initConfig(null);
    }

    /**
     * Main entry point.
     * Keeps the JVM alive so scheduled publishers can run.
     */
    public static void main(String[] args)
    {
        GatewayDeviceApp gwApp = new GatewayDeviceApp(args);
        gwApp.startApp();

        // Keep application running indefinitely (Ctrl+C to exit)
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // Allow graceful shutdown
        }
    }
}
