package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import org.junit.Test;

import programmingtheiot.gda.connection.CloudClientFactory;
import programmingtheiot.gda.connection.ICloudClient;

public class CloudClientFactoryTest
{
	@Test
	public void testCreateAndTestCloudClient()
	{
		ICloudClient client =
			CloudClientFactory.getInstance().createCloudClient();

		assertNotNull("CloudClient should not be null", client);

		assertTrue("Connect failed", client.connectClient());

		try {
			Thread.sleep(3000L);
		} catch (Exception e) {
			// ignore
		}

		assertTrue("Disconnect failed", client.disconnectClient());
	}
}
