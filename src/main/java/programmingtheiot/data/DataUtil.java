/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License.
 * 
 * Copyright (c) 2020 - 2025 by Andrew D. King
 */

package programmingtheiot.data;

import java.util.logging.Logger;
import com.google.gson.Gson;

/**
 * Utility class for converting IoT data objects to and from JSON.
 * 
 * Implements serialization and deserialization for:
 *  - ActuatorData
 *  - SensorData
 *  - SystemPerformanceData
 * (SystemStateData is optional)
 */
public class DataUtil
{
	// static
	private static final Logger _Logger =
		Logger.getLogger(DataUtil.class.getName());

	private static final DataUtil _Instance = new DataUtil();

	public static final DataUtil getInstance()
	{
		return _Instance;
	}

	// constructors
	private DataUtil()
	{
		super();
	}

	// --------------------------------------------------------------------------
	// ACTUATOR DATA
	// --------------------------------------------------------------------------
	public String actuatorDataToJson(ActuatorData data)
	{
		String jsonData = null;

		if (data != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(data);
		}

		return jsonData;
	}

	public ActuatorData jsonToActuatorData(String jsonData)
	{
		ActuatorData data = null;

		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, ActuatorData.class);
		}

		return data;
	}

	// --------------------------------------------------------------------------
	// SENSOR DATA
	// --------------------------------------------------------------------------
	public String sensorDataToJson(SensorData data)
	{
		String jsonData = null;

		if (data != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(data);
		}

		return jsonData;
	}

	public SensorData jsonToSensorData(String jsonData)
	{
		SensorData data = null;

		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, SensorData.class);
		}

		return data;
	}

	// --------------------------------------------------------------------------
	// SYSTEM PERFORMANCE DATA
	// --------------------------------------------------------------------------
	public String systemPerformanceDataToJson(SystemPerformanceData data)
	{
		String jsonData = null;

		if (data != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(data);
		}

		return jsonData;
	}

	public SystemPerformanceData jsonToSystemPerformanceData(String jsonData)
	{
		SystemPerformanceData data = null;

		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, SystemPerformanceData.class);
		}

		return data;
	}

	// --------------------------------------------------------------------------
	// OPTIONAL: SYSTEM STATE DATA
	// --------------------------------------------------------------------------
	public String systemStateDataToJson(SystemStateData data)
	{
		String jsonData = null;

		if (data != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(data);
		}

		return jsonData;
	}

	public SystemStateData jsonToSystemStateData(String jsonData)
	{
		SystemStateData data = null;

		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, SystemStateData.class);
		}

		return data;
	}
}
