package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main GDA application.
 * DeviceDataManager handles system performance and messaging.
 */
public class GatewayDeviceApp
{
    // Static logger
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceApp.class.getName());

    // Class-scoped DeviceDataManager
    private DeviceDataManager dataMgr = null;

    /**
     * Constructor
     * 
     * @param args Command line arguments
     */
    public GatewayDeviceApp(String[] args)
    {
        super();
        _Logger.info("Initializing GDA...");

        // Parse arguments (currently ignored)
        parseArgs(args);

        // Instantiate DeviceDataManager
        this.dataMgr = new DeviceDataManager();
    }

    /**
     * Start the GDA application.
     */
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

    /**
     * Stop the GDA application.
     * 
     * @param code Exit code
     */
    public void stopApp(int code)
    {
        _Logger.info("Stopping GDA...");

        try {
            if (this.dataMgr != null) {
                this.dataMgr.stopManager();
            }
            _Logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to cleanly stop GDA. Exiting.", e);
        }

        System.exit(code);
    }

    /**
     * Initialize configuration (stub).
     * 
     * @param fileName Configuration file
     */
    private void initConfig(String fileName)
    {
        _Logger.info("Attempting to load configuration: " +
            (fileName == null ? "Default." : fileName));
    }

    /**
     * Parse command line arguments (currently stub).
     * 
     * @param args Command line arguments
     */
    private void parseArgs(String[] args)
    {
        _Logger.info("No command line args to parse.");
        initConfig(null);
    }

    /**
     * Main application entry point.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        GatewayDeviceApp gwApp = new GatewayDeviceApp(args);

        gwApp.startApp();

        try {
            Thread.sleep(65000L); // run for ~65 seconds
        } catch (InterruptedException e) {
            // ignore
        }

        gwApp.stopApp(0);
    }
}
