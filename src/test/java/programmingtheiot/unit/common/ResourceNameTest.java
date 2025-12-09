package programmingtheiot.unit.common;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;

/**
 * Basic unit tests for ResourceNameEnum.
 */
public class ResourceNameTest
{
    private static final Logger _Logger =
        Logger.getLogger(ResourceNameTest.class.getName());

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        // Optional setup before all tests
    }

    @Before
    public void setUp() throws Exception
    {
        // Optional setup before each test
    }

    @Test
    public void testListAllResourceNames()
    {
        StringBuilder buf = new StringBuilder();

        for (ResourceNameEnum resource : ResourceNameEnum.values()) {
            buf.append(resource.getResourceName()).append(System.lineSeparator());
        }

        _Logger.info("Resource name listing:\n" + buf.toString());

        // Simple assertion to ensure enum is not empty
        assertTrue(ResourceNameEnum.values().length > 0);
    }

    @Test
    public void testCheckResourceName()
    {
        ResourceNameEnum resourceA = ResourceNameEnum.values()[0];
        String           name      = resourceA.getResourceName();
        ResourceNameEnum resourceB = ResourceNameEnum.getEnumFromValue(name);

        assertTrue(resourceA == resourceB);
    }

    @Test
    public void testDefaultForInvalidValue()
    {
        ResourceNameEnum resource = ResourceNameEnum.getEnumFromValue("INVALID_RESOURCE_NAME");
        assertEquals(ResourceNameEnum.DEFAULT, resource);
    }
}
