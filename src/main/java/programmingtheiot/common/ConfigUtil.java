package programmingtheiot.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * ConfigUtil handles reading from the PIoT config files.
 * Supports properties and credentials loading.
 */
public class ConfigUtil
{
    // Logger
    private static final Logger _Logger = Logger.getLogger(ConfigUtil.class.getName());

    // Singleton instance
    private static final ConfigUtil _Instance = new ConfigUtil();
    public static final ConfigUtil getInstance() { return _Instance; }

    // Private variables
    private INIConfiguration sectionProperties = null;
    private boolean isLoaded = false;
    private String configFileName = ConfigConst.DEFAULT_CONFIG_FILE_NAME;

    private ConfigUtil()
    {
        super();
        this.reloadConfig();
    }

    // -----------------------
    // Public methods
    // -----------------------

    /** Return full section name for cloud services */
    public String getCloudSectionName(String cloudSvcName)
    {
        if (cloudSvcName != null && cloudSvcName.trim().length() > 0)
        {
            return ConfigConst.CLOUD_GATEWAY_SERVICE + "." + cloudSvcName;
        }
        else
        {
            return ConfigConst.CLOUD_GATEWAY_SERVICE;
        }
    }

    /** Get property value */
    public synchronized String getProperty(String section, String propName)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getString(propName);
    }

    public synchronized String getProperty(String section, String propName, String defaultVal)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getString(propName, defaultVal);
    }

    public synchronized boolean getBoolean(String section, String propName)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getBoolean(propName, false);
    }

    public synchronized int getInteger(String section, String propName)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getInt(propName);
    }

    public synchronized int getInteger(String section, String propName, int defaultVal)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getInt(propName, defaultVal);
    }

    public synchronized float getFloat(String section, String propName)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getFloat(propName);
    }

    public synchronized float getFloat(String section, String propName, float defaultVal)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return subNodeConfig.getFloat(propName, defaultVal);
    }

    public synchronized boolean hasProperty(String section, String propName)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return (subNodeConfig.getProperty(propName) != null);
    }

    public synchronized boolean hasSection(String section)
    {
        SubnodeConfiguration subNodeConfig = sectionProperties.getSection(section);
        return (subNodeConfig != null);
    }

    public boolean isConfigDataLoaded() { return isLoaded; }

    /** Load credentials from the specified section */
    public Properties getCredentials(String section)
    {
        Properties props = null;
        if (hasSection(section))
        {
            // Use restored ConfigConst.CRED_FILE_KEY
            String credFileName = getProperty(section, ConfigConst.CRED_FILE_KEY);
            if (credFileName == null || credFileName.isEmpty())
            {
                _Logger.warning("No cred file specified in config for section: " + section);
                return null;
            }

            File credFile = new File(credFileName);
            if (!credFile.exists())
            {
                _Logger.warning("Credential file does not exist: " + credFileName);
                return null;
            }

            FileInputStream fis = null;
            try
            {
                props = new Properties();
                fis = new FileInputStream(credFile);
                props.load(fis);
                _Logger.info("Successfully loaded credentials from: " + credFileName);
            }
            catch (Exception e)
            {
                _Logger.log(Level.WARNING, "Failed to load credentials from file: " + credFileName, e);
            }
            finally
            {
                try { if (fis != null) fis.close(); } 
                catch (IOException e) { _Logger.warning("Failed to close FileInputStream."); }
            }
        }
        return props;
    }

    /** Reload configuration from file */
    public synchronized void reloadConfig()
    {
        boolean fileExists = false;
        String cfgFileName = null;

        try
        {
            cfgFileName = System.getProperty(ConfigConst.CONFIG_FILE_KEY);
            if (cfgFileName != null)
            {
                File cfgFile = new File(cfgFileName);
                if (cfgFile.exists())
                {
                    this.configFileName = cfgFileName;
                    fileExists = true;
                }
                else
                {
                    _Logger.warning("Specified config file doesn't exist! Using default.\n"
                                    + "\tRequested: " + cfgFileName
                                    + "\n\tDefault: " + ConfigConst.DEFAULT_CONFIG_FILE_NAME);
                }
            }
        }
        catch (SecurityException e)
        {
            _Logger.warning("Security exception reading system property. Using default.\n"
                            + "\tRequested: " + cfgFileName
                            + "\n\tDefault: " + ConfigConst.DEFAULT_CONFIG_FILE_NAME);
        }
        finally
        {
            if (!fileExists) this.configFileName = ConfigConst.DEFAULT_CONFIG_FILE_NAME;
        }

        initBackingProperties();
        loadConfig(this.configFileName);
    }

    // -----------------------
    // Private methods
    // -----------------------
    private void initBackingProperties() { sectionProperties = new INIConfiguration(); }

    private synchronized boolean loadConfig(String configFileName)
    {
        File cfgFile = new File(configFileName);

        if (!cfgFile.exists())
        {
            _Logger.log(Level.WARNING, "Config file '" + cfgFile.getAbsolutePath() + "' doesn't exist. Trying source directory...");
            cfgFile = new File("src/main/java/programmingtheiot/common/" + ConfigConst.DEFAULT_CONFIG_FILE_NAME);
        }

        if (cfgFile.exists())
        {
            try
            {
                initBackingProperties();
                FileReader fReader = new FileReader(cfgFile);
                sectionProperties.read(fReader);
                isLoaded = true;
                _Logger.info("Configuration loaded successfully from: " + cfgFile.getAbsolutePath());
            }
            catch (ConfigurationException e)
            {
                _Logger.log(Level.SEVERE, "Failed to parse config file: " + cfgFile.getAbsolutePath(), e);
            }
            catch (IOException e)
            {
                _Logger.log(Level.SEVERE, "Failed to read config file: " + cfgFile.getAbsolutePath(), e);
            }
        }
        else
        {
            _Logger.log(Level.SEVERE, "Config file not found: " + cfgFile.getAbsolutePath());
        }

        return isLoaded;
    }
}
