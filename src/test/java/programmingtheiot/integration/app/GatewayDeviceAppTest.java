package programmingtheiot.integration.app;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import programmingtheiot.gda.app.GatewayDeviceApp;

/**
 * This test case class contains very basic integration tests for
 * GatewayDeviceApp. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class GatewayDeviceAppTest
{
	// static
	private static final Logger _Logger = Logger.getLogger(GatewayDeviceAppTest.class.getName());
	
	// member var
	private GatewayDeviceApp gda = null;
	
	// test setup methods
	@BeforeClass
	public static void setUpBeforeClass() throws Exception { }

	@AfterClass
	public static void tearDownAfterClass() throws Exception { }

	@Before
	public void setUp() throws Exception
	{
		// Override stopApp to avoid System.exit() during tests
		gda = new GatewayDeviceApp(new String[0]) {
			@Override
			public void stopApp(int code) {
				super.stopAppWithoutExit();
			}
		};
	}

	@After
	public void tearDown() throws Exception { }

	// test methods
	@Test
	public void testStartAndStopGatewayApp()
	{
		_Logger.info("Running testStartAndStopGatewayApp...");
		this.gda.startApp();
		
		try {
			Thread.sleep(65000L);
		} catch (InterruptedException e) {
			// ignore
		}
		
		this.gda.stopApp(0);
	}
}
