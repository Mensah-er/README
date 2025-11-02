/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 - 2025 by Andrew D. King
 */

package programmingtheiot.integration.data;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.*;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.data.*;
import programmingtheiot.data.DataUtil;

/**
 * Integration test for DataUtil and data classes to verify JSON
 * compatibility between CDA and GDA.
 */
public class DataIntegrationTest
{
	// static

	private static final Logger _Logger =
		Logger.getLogger(DataIntegrationTest.class.getName());

	private static String _CdaDataPath = "";
	private static String _GdaDataPath = "";

	// member vars


	// setup methods

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		_CdaDataPath = ConfigUtil.getInstance().getProperty(ConfigConst.GATEWAY_DEVICE, ConfigConst.TEST_CDA_DATA_PATH_KEY);
		_GdaDataPath = ConfigUtil.getInstance().getProperty(ConfigConst.GATEWAY_DEVICE, ConfigConst.TEST_GDA_DATA_PATH_KEY);

		if (_CdaDataPath == null || _CdaDataPath.isEmpty()) {
			_CdaDataPath = "/tmp/cda-data";
		}
		if (_GdaDataPath == null || _GdaDataPath.isEmpty()) {
			_GdaDataPath = "/tmp/gda-data";
		}

		try {
			Files.createDirectories(Paths.get(_CdaDataPath));
			Files.createDirectories(Paths.get(_GdaDataPath));

			// ✅ Automatically generate CDA test files for reading
			_Logger.info("Generating sample CDA JSON files in: " + _CdaDataPath);

			Files.writeString(Paths.get(_CdaDataPath, "ActuatorData.dat"),
				DataUtil.getInstance().actuatorDataToJson(new ActuatorData()), StandardCharsets.UTF_8);

			Files.writeString(Paths.get(_CdaDataPath, "SensorData.dat"),
				DataUtil.getInstance().sensorDataToJson(new SensorData()), StandardCharsets.UTF_8);

			Files.writeString(Paths.get(_CdaDataPath, "SystemPerformanceData.dat"),
				DataUtil.getInstance().systemPerformanceDataToJson(new SystemPerformanceData()), StandardCharsets.UTF_8);

			_Logger.info("Sample CDA data files created successfully.");
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to create CDA or GDA path hierarchy or files.", e);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	// test methods

	@Test
	public void testWriteActuatorDataToGdaDataPath()
	{
		String fileName = _GdaDataPath + "/ActuatorData.dat";
		_Logger.info("\n\n----- [ActuatorData to JSON to file] -----");

		try {
			Path filePath = FileSystems.getDefault().getPath(fileName);
			String dataStr = DataUtil.getInstance().actuatorDataToJson(new ActuatorData());
			Files.writeString(filePath, dataStr, StandardCharsets.UTF_8);
			_Logger.info("ActuatorData JSON written to: " + filePath);
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to write file: " + fileName, e);
			fail("Failed to write file: " + fileName);
		}
	}

	@Test
	public void testWriteSensorDataToGdaDataPath()
	{
		String fileName = _GdaDataPath + "/SensorData.dat";
		_Logger.info("\n\n----- [SensorData to JSON to file] -----");

		try {
			Path filePath = FileSystems.getDefault().getPath(fileName);
			String dataStr = DataUtil.getInstance().sensorDataToJson(new SensorData());
			Files.writeString(filePath, dataStr, StandardCharsets.UTF_8);
			_Logger.info("SensorData JSON written to: " + filePath);
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to write file: " + fileName, e);
			fail("Failed to write file: " + fileName);
		}
	}

	@Test
	public void testWriteSystemPerformanceDataToGdaDataPath()
	{
		String fileName = _GdaDataPath + "/SystemPerformanceData.dat";
		_Logger.info("\n\n----- [SystemPerformanceData to JSON to file] -----");

		try {
			Path filePath = FileSystems.getDefault().getPath(fileName);
			String dataStr = DataUtil.getInstance().systemPerformanceDataToJson(new SystemPerformanceData());
			Files.writeString(filePath, dataStr, StandardCharsets.UTF_8);
			_Logger.info("SystemPerformanceData JSON written to: " + filePath);
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to write file: " + fileName, e);
			fail("Failed to write file: " + fileName);
		}
	}

	@Test
	public void testReadActuatorDataFromCdaDataPath()
	{
		String fileName = _CdaDataPath + "/ActuatorData.dat";
		_Logger.info("\n\n----- [ActuatorData JSON from file to object] -----");

		try {
			Path filePath = FileSystems.getDefault().getPath(fileName);
			String dataStr = Files.readString(filePath, StandardCharsets.UTF_8);
			ActuatorData dataObj = DataUtil.getInstance().jsonToActuatorData(dataStr);
			assertNotNull(dataObj);
			_Logger.info("ActuatorData object read successfully: " + dataObj);
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to read file: " + fileName, e);
			fail("Failed to read file: " + fileName);
		}
	}

	@Test
	public void testReadSensorDataFromCdaDataPath()
	{
		String fileName = _CdaDataPath + "/SensorData.dat";
		_Logger.info("\n\n----- [SensorData JSON from file to object] -----");

		try {
			Path filePath = FileSystems.getDefault().getPath(fileName);
			String dataStr = Files.readString(filePath, StandardCharsets.UTF_8);
			SensorData dataObj = DataUtil.getInstance().jsonToSensorData(dataStr);
			assertNotNull(dataObj);
			_Logger.info("SensorData object read successfully: " + dataObj);
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to read file: " + fileName, e);
			fail("Failed to read file: " + fileName);
		}
	}

	@Test
	public void testReadSystemPerformanceDataFromCdaDataPath()
	{
		String fileName = _CdaDataPath + "/SystemPerformanceData.dat";
		_Logger.info("\n\n----- [SystemPerformanceData JSON from file to object] -----");

		try {
			Path filePath = FileSystems.getDefault().getPath(fileName);
			String dataStr = Files.readString(filePath, StandardCharsets.UTF_8);
			SystemPerformanceData dataObj = DataUtil.getInstance().jsonToSystemPerformanceData(dataStr);
			assertNotNull(dataObj);
			_Logger.info("SystemPerformanceData object read successfully: " + dataObj);
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Failed to read file: " + fileName, e);
			fail("Failed to read file: " + fileName);
		}
	}
}
